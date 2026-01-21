package com.gym.management.service;

import com.gym.management.entity.User;
import com.gym.management.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;

    // In-memory session storage: token -> username
    private final Map<String, String> sessions = new ConcurrentHashMap<>();

    public String login(String username, String password) {
        try {
            User user = userService.findByUsername(username);

            if (!user.getActive()) {
                return null; // User is deactivated
            }

            if (userService.verifyPassword(password, user.getPassword())) {
                String token = UUID.randomUUID().toString();
                sessions.put(token, username);
                return token;
            }
        } catch (Exception e) {
            // User not found or other error
            return null;
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

    public UserRole getUserRoleFromToken(String token) {
        String username = sessions.get(token);
        if (username != null) {
            try {
                User user = userService.findByUsername(username);
                return user.getRole();
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    public boolean hasRole(String token, UserRole requiredRole) {
        UserRole userRole = getUserRoleFromToken(token);
        return userRole != null && userRole == requiredRole;
    }

    public boolean isAdmin(String token) {
        return hasRole(token, UserRole.ADMIN);
    }
}
