package com.gym.management.dto;

import com.gym.management.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private UserRole role;
    private Boolean active;
    private LocalDateTime createdAt;
}
