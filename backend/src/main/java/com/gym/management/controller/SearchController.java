package com.gym.management.controller;

import com.gym.management.service.ElasticsearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Proxies search queries to Elasticsearch.
 * Provides instant, fuzzy search for trainers, classes, and members.
 */
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final ElasticsearchService elasticsearchService;

    /**
     * Global search across all gym indices.
     * GET /api/search?q=yoga&size=10
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> searchAll(
            @RequestParam("q") String query,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        List<Map<String, Object>> results = elasticsearchService.searchAll(query, size);
        return ResponseEntity.ok(results);
    }

    /**
     * Search within a specific index (trainers, classes, members).
     * GET /api/search/trainers?q=boxing&size=5
     */
    @GetMapping("/{index}")
    public ResponseEntity<List<Map<String, Object>>> searchIndex(
            @PathVariable String index,
            @RequestParam("q") String query,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        List<Map<String, Object>> results = elasticsearchService.search(index, query, size);
        return ResponseEntity.ok(results);
    }

    /**
     * Elasticsearch health check.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        boolean healthy = elasticsearchService.isHealthy();
        return ResponseEntity.ok(Map.of("elasticsearch", healthy ? "UP" : "DOWN"));
    }
}
