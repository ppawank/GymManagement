package com.gym.management.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Broadcasts real-time events to WebSocket-connected clients.
 *
 * Topics:
 *   /topic/occupancy   – live branch occupancy updates
 *   /topic/checkins    – individual check-in events
 *   /topic/trainers    – trainer availability changes
 *   /topic/classes     – class occupancy changes
 *   /topic/cdc-events  – raw CDC event stream for debugging / dashboard
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastOccupancy(Long branchId, String branchName, long currentOccupancy, int capacity) {
        Map<String, Object> payload = Map.of(
                "branchId", branchId,
                "branchName", branchName,
                "currentOccupancy", currentOccupancy,
                "capacity", capacity,
                "timestamp", LocalDateTime.now().toString()
        );
        messagingTemplate.convertAndSend("/topic/occupancy", payload);
        log.debug("Broadcasted occupancy update for branch {}: {}/{}", branchId, currentOccupancy, capacity);
    }

    public void broadcastCheckIn(Long memberId, String memberName, Long branchId, String branchName) {
        Map<String, Object> payload = Map.of(
                "memberId", memberId,
                "memberName", memberName,
                "branchId", branchId,
                "branchName", branchName,
                "type", "CHECK_IN",
                "timestamp", LocalDateTime.now().toString()
        );
        messagingTemplate.convertAndSend("/topic/checkins", payload);
        log.debug("Broadcasted check-in for member {} at branch {}", memberName, branchName);
    }

    public void broadcastTrainerUpdate(Long trainerId, String trainerName, String status) {
        Map<String, Object> payload = Map.of(
                "trainerId", trainerId,
                "trainerName", trainerName,
                "status", status,
                "timestamp", LocalDateTime.now().toString()
        );
        messagingTemplate.convertAndSend("/topic/trainers", payload);
        log.debug("Broadcasted trainer update: {} is now {}", trainerName, status);
    }

    public void broadcastClassUpdate(Long classId, String className, int currentOccupancy, int maxOccupancy) {
        Map<String, Object> payload = Map.of(
                "classId", classId,
                "className", className,
                "currentOccupancy", currentOccupancy,
                "maxOccupancy", maxOccupancy,
                "timestamp", LocalDateTime.now().toString()
        );
        messagingTemplate.convertAndSend("/topic/classes", payload);
    }

    public void broadcastCdcEvent(String table, String operation, Map<String, Object> data) {
        Map<String, Object> payload = Map.of(
                "table", table,
                "operation", operation,
                "data", data,
                "timestamp", LocalDateTime.now().toString()
        );
        messagingTemplate.convertAndSend("/topic/cdc-events", payload);
        log.debug("Broadcasted CDC event: {} on {}", operation, table);
    }
}
