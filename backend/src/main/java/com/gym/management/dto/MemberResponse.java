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
    private MemberStatus status;
    private LocalDate joinDate;
    private String feeStatus; // PAID or PENDING for current month
    private LocalDate membershipExpiryDate; // Last payment month + 1 month
    private Boolean expiringInSevenDays; // Flag for UI highlighting
}
