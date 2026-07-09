package com.gym.management.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Consumes Debezium CDC events from Kafka topics and:
 *   1. Indexes the changed data into Elasticsearch
 *   2. Broadcasts real-time updates via WebSockets
 *
 * Debezium topic naming convention with prefix "gym":
 *   gym.public.members, gym.public.trainers, gym.public.classes,
 *   gym.public.attendance, gym.public.subscriptions, gym.public.payments,
 *   gym.public.equipment_usage
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CdcKafkaConsumer {

    private final ElasticsearchService elasticsearchService;
    private final WebSocketService webSocketService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "gym.public.members", groupId = "gym-backend-group")
    public void consumeMemberEvents(String message) {
        processEvent("members", message);
    }

    @KafkaListener(topics = "gym.public.trainers", groupId = "gym-backend-group")
    public void consumeTrainerEvents(String message) {
        processEvent("trainers", message);
    }

    @KafkaListener(topics = "gym.public.classes", groupId = "gym-backend-group")
    public void consumeClassEvents(String message) {
        processEvent("classes", message);
    }

    @KafkaListener(topics = "gym.public.attendance", groupId = "gym-backend-group")
    public void consumeAttendanceEvents(String message) {
        processEvent("attendance", message);
    }

    @KafkaListener(topics = "gym.public.subscriptions", groupId = "gym-backend-group")
    public void consumeSubscriptionEvents(String message) {
        processEvent("subscriptions", message);
    }

    @KafkaListener(topics = "gym.public.payments", groupId = "gym-backend-group")
    public void consumePaymentEvents(String message) {
        processEvent("payments", message);
    }

    @KafkaListener(topics = "gym.public.equipment_usage", groupId = "gym-backend-group")
    public void consumeEquipmentUsageEvents(String message) {
        processEvent("equipment_usage", message);
    }

    private void processEvent(String table, String message) {
        try {
            if (message == null || message.isBlank()) {
                return;
            }

            Map<String, Object> event = objectMapper.readValue(message,
                    new TypeReference<Map<String, Object>>() {});

            // Debezium envelope structure
            Map<String, Object> payload = event.containsKey("payload")
                    ? castToMap(event.get("payload"))
                    : event;

            String operation = determineOperation(payload);
            Map<String, Object> after = castToMap(payload.get("after"));
            Map<String, Object> before = castToMap(payload.get("before"));

            // Determine the document to index
            Map<String, Object> document = (after != null) ? after : before;

            if (document == null) {
                log.debug("Skipping CDC event for {}: no data payload", table);
                return;
            }

            String docId = String.valueOf(document.getOrDefault("id", "unknown"));

            // 1. Index into Elasticsearch
            if ("DELETE".equals(operation)) {
                elasticsearchService.deleteDocument(table, docId);
            } else {
                elasticsearchService.indexDocument(table, docId, document);
            }

            // 2. Broadcast via WebSocket
            webSocketService.broadcastCdcEvent(table, operation, document);

            // 3. Table-specific real-time broadcasts
            handleTableSpecificBroadcast(table, operation, document);

            log.info("Processed CDC event: {} on {} (id={})", operation, table, docId);

        } catch (Exception e) {
            log.error("Failed to process CDC event for {}: {}", table, e.getMessage(), e);
        }
    }

    private void handleTableSpecificBroadcast(String table, String operation, Map<String, Object> document) {
        switch (table) {
            case "trainers" -> {
                if ("CREATE".equals(operation) || "UPDATE".equals(operation)) {
                    Long trainerId = toLong(document.get("id"));
                    String trainerName = toString(document.get("name"));
                    String status = toString(document.get("availability_status"));
                    webSocketService.broadcastTrainerUpdate(trainerId, trainerName, status);
                }
            }
            case "classes" -> {
                if ("UPDATE".equals(operation)) {
                    Long classId = toLong(document.get("id"));
                    String className = toString(document.get("name"));
                    int currentOccupancy = toInt(document.get("current_occupancy"));
                    int maxOccupancy = toInt(document.get("max_occupancy"));
                    webSocketService.broadcastClassUpdate(classId, className, currentOccupancy, maxOccupancy);
                }
            }
            case "attendance" -> {
                if ("CREATE".equals(operation)) {
                    Long memberId = toLong(document.get("member_id"));
                    Long branchId = toLong(document.get("branch_id"));
                    webSocketService.broadcastCheckIn(memberId, "Member #" + memberId, branchId, "Branch #" + branchId);
                }
            }
            default -> { /* no special handling */ }
        }
    }

    private String determineOperation(Map<String, Object> payload) {
        Object op = payload.get("op");
        if (op == null) return "UNKNOWN";
        return switch (op.toString()) {
            case "c", "r" -> "CREATE";  // 'r' = read (snapshot)
            case "u" -> "UPDATE";
            case "d" -> "DELETE";
            default -> "UNKNOWN";
        };
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> castToMap(Object obj) {
        if (obj instanceof Map) {
            return (Map<String, Object>) obj;
        }
        return null;
    }

    private Long toLong(Object obj) {
        if (obj instanceof Number n) return n.longValue();
        if (obj instanceof String s) {
            try { return Long.parseLong(s); } catch (NumberFormatException e) { return 0L; }
        }
        return 0L;
    }

    private int toInt(Object obj) {
        if (obj instanceof Number n) return n.intValue();
        if (obj instanceof String s) {
            try { return Integer.parseInt(s); } catch (NumberFormatException e) { return 0; }
        }
        return 0;
    }

    private String toString(Object obj) {
        return obj != null ? obj.toString() : "";
    }
}
