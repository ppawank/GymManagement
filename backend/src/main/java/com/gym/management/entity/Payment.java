package com.gym.management.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments", uniqueConstraints = {
                @UniqueConstraint(columnNames = { "member_id", "payment_month", "payment_year" })
}, indexes = {
                @Index(name = "idx_payment_month_year", columnList = "payment_month, payment_year")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @NotNull(message = "Member is required")
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "member_id", nullable = false)
        private Member member;

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        @Column(nullable = false, precision = 10, scale = 2)
        private BigDecimal amount;

        @NotNull(message = "Payment month is required")
        @Column(name = "payment_month", nullable = false)
        private Integer paymentMonth;

        @NotNull(message = "Payment year is required")
        @Column(name = "payment_year", nullable = false)
        private Integer paymentYear;

        @NotNull(message = "Payment date is required")
        @Column(name = "payment_date", nullable = false)
        private LocalDate paymentDate;

        @Column(name = "verified", nullable = false)
        private Boolean verified = false;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "verified_by")
        private com.gym.management.entity.User verifiedBy;

        @Column(name = "verified_at")
        private LocalDateTime verifiedAt;

        @CreatedDate
        @Column(name = "created_at", nullable = false, updatable = false)
        private LocalDateTime createdAt;
}
