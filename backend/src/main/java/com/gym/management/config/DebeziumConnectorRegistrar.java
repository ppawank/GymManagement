package com.gym.management.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

/**
 * Registers the Debezium PostgreSQL CDC connector with Kafka Connect
 * after the application starts. Retries until the Debezium Connect REST
 * API becomes available.
 */
@Component
@Slf4j
public class DebeziumConnectorRegistrar {

    @Value("${debezium.connect.url:http://localhost:8083}")
    private String connectUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @EventListener(ApplicationReadyEvent.class)
    @Async
    public void registerConnector() {
        String connectorName = "gym-postgres-connector";
        int maxRetries = 30;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                // Check if connector already exists
                HttpRequest checkRequest = HttpRequest.newBuilder()
                        .uri(URI.create(connectUrl + "/connectors/" + connectorName))
                        .GET()
                        .build();
                HttpResponse<String> checkResponse = httpClient.send(checkRequest,
                        HttpResponse.BodyHandlers.ofString());

                if (checkResponse.statusCode() == 200) {
                    log.info("Debezium connector '{}' already registered.", connectorName);
                    return;
                }

                // Register the connector
                Map<String, Object> connectorConfig = Map.of(
                        "name", connectorName,
                        "config", Map.ofEntries(
                                Map.entry("connector.class", "io.debezium.connector.postgresql.PostgresConnector"),
                                Map.entry("database.hostname", "postgres"),
                                Map.entry("database.port", "5432"),
                                Map.entry("database.user", "gymuser"),
                                Map.entry("database.password", "gympass123"),
                                Map.entry("database.dbname", "gym_management"),
                                Map.entry("topic.prefix", "gym"),
                                Map.entry("schema.include.list", "public"),
                                Map.entry("table.include.list",
                                        "public.members,public.trainers,public.classes," +
                                        "public.attendance,public.subscriptions,public.payments," +
                                        "public.equipment_usage"),
                                Map.entry("plugin.name", "pgoutput"),
                                Map.entry("slot.name", "gym_slot"),
                                Map.entry("publication.name", "gym_publication"),
                                Map.entry("key.converter", "org.apache.kafka.connect.json.JsonConverter"),
                                Map.entry("key.converter.schemas.enable", "false"),
                                Map.entry("value.converter", "org.apache.kafka.connect.json.JsonConverter"),
                                Map.entry("value.converter.schemas.enable", "false")
                        )
                );

                String json = objectMapper.writeValueAsString(connectorConfig);
                HttpRequest createRequest = HttpRequest.newBuilder()
                        .uri(URI.create(connectUrl + "/connectors"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();

                HttpResponse<String> createResponse = httpClient.send(createRequest,
                        HttpResponse.BodyHandlers.ofString());

                if (createResponse.statusCode() == 201 || createResponse.statusCode() == 200) {
                    log.info("Successfully registered Debezium connector '{}'. Response: {}",
                            connectorName, createResponse.statusCode());
                    return;
                } else {
                    log.warn("Failed to register connector (attempt {}/{}): HTTP {} – {}",
                            attempt, maxRetries, createResponse.statusCode(), createResponse.body());
                }
            } catch (Exception e) {
                log.warn("Debezium Connect not ready (attempt {}/{}): {}", attempt, maxRetries, e.getMessage());
            }

            try {
                Thread.sleep(5000); // Wait 5 seconds before retry
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return;
            }
        }

        log.error("Failed to register Debezium connector after {} attempts. " +
                  "CDC events will not be captured. Check that Debezium Connect is running.", maxRetries);
    }
}
