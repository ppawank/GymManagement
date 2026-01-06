package com.gym.management.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    // Hardcoded credentials
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin";

    // In-memory session storage (for simplicity, can be replaced with Redis/DB
    // later)
    private final Map<String, String> sessions = new ConcurrentHashMap<>();

    public String login(String username, String password) {
        if (ADMIN_USERNAME.equals(username) && ADMIN_PASSWORD.equals(password)) {
            String token = UUID.randomUUID().toString();
            sessions.put(token, username);
            return token;
        }
        return null;
    }

    public boolean validateToken(String token) {
        return token != null && sessions.containsKey(token);
    }

    public void logout(String token) {
        if (token != null) {
            sessions.remove(token);
        }
    }

    public String getUsernameFromToken(String token) {
        return sessions.get(token);
    }
}
