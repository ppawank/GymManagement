package com.gym.management.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

/**
 * Lightweight Elasticsearch client using Java's built-in HttpClient.
 * Indexes documents and executes search queries against the gym indices.
 */
@Service
@Slf4j
public class ElasticsearchService {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String esUrl;

    public ElasticsearchService(@Value("${elasticsearch.url:http://localhost:9200}") String esUrl) {
        this.esUrl = esUrl;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    // ──── Index Operations ────

    public void indexDocument(String index, String id, Map<String, Object> document) {
        try {
            String json = objectMapper.writeValueAsString(document);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(esUrl + "/" + index + "/_doc/" + id))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.debug("Indexed doc {}/{}: status={}", index, id, response.statusCode());
        } catch (Exception e) {
            log.error("Failed to index document {}/{}: {}", index, id, e.getMessage());
        }
    }

    public void deleteDocument(String index, String id) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(esUrl + "/" + index + "/_doc/" + id))
                    .DELETE()
                    .build();
            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.debug("Deleted doc {}/{}", index, id);
        } catch (Exception e) {
            log.error("Failed to delete document {}/{}: {}", index, id, e.getMessage());
        }
    }

    // ──── Search Operations ────

    /**
     * Fuzzy multi-field search across an index.
     * Supports partial/misspelled terms (e.g. "yga" → "Yoga").
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> search(String index, String query, int size) {
        try {
            Map<String, Object> searchBody = Map.of(
                    "size", size,
                    "query", Map.of(
                            "multi_match", Map.of(
                                    "query", query,
                                    "fields", List.of("name^3", "specialty^2", "equipmentName", "planName", "location"),
                                    "fuzziness", "AUTO",
                                    "type", "best_fields"
                            )
                    )
            );

            String json = objectMapper.writeValueAsString(searchBody);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(esUrl + "/" + index + "/_search"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            Map<String, Object> result = objectMapper.readValue(response.body(), Map.class);

            Map<String, Object> hits = (Map<String, Object>) result.get("hits");
            List<Map<String, Object>> hitList = (List<Map<String, Object>>) hits.get("hits");

            return hitList.stream()
                    .map(hit -> {
                        Map<String, Object> source = (Map<String, Object>) hit.get("_source");
                        source.put("_id", hit.get("_id"));
                        source.put("_index", hit.get("_index"));
                        source.put("_score", hit.get("_score"));
                        return source;
                    })
                    .toList();
        } catch (Exception e) {
            log.error("Search failed on index {}: {}", index, e.getMessage());
            return List.of();
        }
    }

    /**
     * Search across all gym indices (trainers, classes, members).
     */
    public List<Map<String, Object>> searchAll(String query, int size) {
        return search("trainers,classes,members", query, size);
    }

    // ──── Health Check ────

    public boolean isHealthy() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(esUrl + "/_cluster/health"))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }
}
