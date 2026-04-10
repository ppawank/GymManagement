package com.gym.management.dto;

import com.gym.management.enums.MemberStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberPaymentExcelDTO {

    // Member fields
    private Long memberId;
    private String name;
    private String email;
    private String phone;
    private MemberStatus status;
    private LocalDate joinDate;

    // Latest payment fields
    private BigDecimal latestPaymentAmount;
    private Integer latestPaymentMonth;
    private Integer latestPaymentYear;
    private LocalDate latestPaymentDate;
    private Boolean paymentVerified;

    // Constructor for members without payments
    public MemberPaymentExcelDTO(Long memberId, String name, String email, String phone,
            MemberStatus status, LocalDate joinDate) {
        this.memberId = memberId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.status = status;
        this.joinDate = joinDate;
    }
}
