package com.gym.management.controller;

import com.gym.management.service.AuthService;
import com.gym.management.service.ExcelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/excel")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class ExcelController {

    private final ExcelService excelService;
    private final AuthService authService;

    /**
     * Export all members with payment data to Excel
     * Admin only
     */
    @GetMapping("/export")
    public ResponseEntity<?> exportMembers(@RequestHeader("Authorization") String token) {
        log.info("Export request received");

        // Verify admin access
        if (!authService.isAdmin(token)) {
            log.warn("Unauthorized export attempt - non-admin user");
            Map<String, String> error = new HashMap<>();
            error.put("message", "Access denied. Admin privileges required.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        try {
            log.debug("Processing export request for admin user");
            ByteArrayInputStream in = excelService.exportMembersWithPayments();

            // Generate filename with timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = "members_export_" + timestamp + ".xlsx";

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=" + filename);

            log.info("Export completed successfully - file: {}", filename);
            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentType(MediaType
                            .parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(new InputStreamResource(in));

        } catch (Exception e) {
            log.error("Export failed with error", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to export data: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Import members from Excel file
     * Admin only
     */
    @PostMapping("/import")
    public ResponseEntity<?> importMembers(
            @RequestHeader("Authorization") String token,
            @RequestParam("file") MultipartFile file) {

        log.info("Import request received - file: {}", file.getOriginalFilename());

        // Verify admin access
        if (!authService.isAdmin(token)) {
            log.warn("Unauthorized import attempt - non-admin user");
            Map<String, String> error = new HashMap<>();
            error.put("message", "Access denied. Admin privileges required.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        // Validate file
        if (file.isEmpty()) {
            log.warn("Import failed - empty file uploaded");
            Map<String, String> error = new HashMap<>();
            error.put("message", "Please select a file to upload");
            return ResponseEntity.badRequest().body(error);
        }

        // Validate file type
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            log.warn("Import failed - invalid file type: {}", filename);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Invalid file type. Please upload an Excel file (.xlsx or .xls)");
            return ResponseEntity.badRequest().body(error);
        }

        try {
            log.debug("Processing import request for file: {}", filename);
            Map<String, Object> result = excelService.importMembers(file);

            if ((Boolean) result.get("success")) {
                log.info("Import completed successfully - {}", result.get("message"));
                return ResponseEntity.ok(result);
            } else {
                log.warn("Import completed with errors - {}", result.get("message"));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
            }

        } catch (Exception e) {
            log.error("Import failed with exception for file: {}", filename, e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to import data: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
