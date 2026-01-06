package com.gym.management.dto;

import com.gym.management.enums.MemberStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MemberResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private LocalDate joinDate;
    private MemberStatus status;
    private String feeStatus; // "PAID" or "PENDING"
}
