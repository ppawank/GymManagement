package com.gym.management.controller;

import com.gym.management.dto.LoginRequest;
import com.gym.management.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(
            @Valid @RequestBody LoginRequest request, HttpSession session) {
        String token = authService.login(request.getUsername(), request.getPassword());

        if (token != null) {
            session.setAttribute("authToken", token);
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            response.put("message", "Login successful");
            return ResponseEntity.ok(response);
        }

        Map<String, String> error = new HashMap<>();
        error.put("error", "Invalid credentials");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpSession session) {
        String token = (String) session.getAttribute("authToken");
        authService.logout(token);
        session.invalidate();

        Map<String, String> response = new HashMap<>();
        response.put("message", "Logout successful");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validate(HttpSession session) {
        String token = (String) session.getAttribute("authToken");
        boolean valid = authService.validateToken(token);

        Map<String, Object> response = new HashMap<>();
        response.put("valid", valid);
        if (valid) {
            response.put("username", authService.getUsernameFromToken(token));
        }
        return ResponseEntity.ok(response);
    }
}
