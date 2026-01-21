package com.gym.management.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Long id;
    private Long memberId;
    private String memberName;
    private BigDecimal amount;
    private Integer paymentMonth;
    private Integer paymentYear;
    private LocalDate paymentDate;
    private Boolean verified;
    private String verifiedByUsername;
    private LocalDateTime verifiedAt;
    private LocalDateTime createdAt;
}
