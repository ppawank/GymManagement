package com.gym.management.service;

import com.gym.management.dto.PaymentRequest;
import com.gym.management.dto.PaymentResponse;
import com.gym.management.entity.Member;
import com.gym.management.entity.Payment;
import com.gym.management.entity.User;
import com.gym.management.exception.BusinessException;
import com.gym.management.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final MemberService memberService;
    private final UserService userService;
    private final AuthService authService;

    @Transactional
    public PaymentResponse recordPayment(PaymentRequest request, String token) {
        Member member = memberService.getMemberById(request.getMemberId());

        // Validate month and year
        if (request.getPaymentMonth() < 1 || request.getPaymentMonth() > 12) {
            throw new BusinessException("INVALID_MONTH",
                    "Payment month must be between 1 and 12");
        }

        // Business Rule: One payment per member per month
        if (paymentRepository.existsByMemberAndPaymentMonthAndPaymentYear(
                member, request.getPaymentMonth(), request.getPaymentYear())) {
            throw new BusinessException("DUPLICATE_PAYMENT",
                    "Payment already recorded for " + request.getPaymentMonth() +
                            "/" + request.getPaymentYear());
        }

        Payment payment = new Payment();
        payment.setMember(member);
        payment.setAmount(request.getAmount());
        payment.setPaymentMonth(request.getPaymentMonth());
        payment.setPaymentYear(request.getPaymentYear());
        payment.setPaymentDate(request.getPaymentDate());

        // Auto-verify if created by ADMIN
        if (authService.isAdmin(token)) {
            payment.setVerified(true);
            String username = authService.getUsernameFromToken(token);
            User admin = userService.findByUsername(username);
            payment.setVerifiedBy(admin);
            payment.setVerifiedAt(LocalDateTime.now());
        } else {
            payment.setVerified(false);
        }

        Payment savedPayment = paymentRepository.save(payment);
        return mapToResponse(savedPayment);
    }

    @Transactional
    public PaymentResponse verifyPayment(Long paymentId, String token) {
        if (!authService.isAdmin(token)) {
            throw new BusinessException("UNAUTHORIZED",
                    "Only admins can verify payments");
        }

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException("PAYMENT_NOT_FOUND",
                        "Payment with ID " + paymentId + " not found"));

        if (payment.getVerified()) {
            throw new BusinessException("ALREADY_VERIFIED",
                    "Payment is already verified");
        }

        String username = authService.getUsernameFromToken(token);
        User admin = userService.findByUsername(username);

        payment.setVerified(true);
        payment.setVerifiedBy(admin);
        payment.setVerifiedAt(LocalDateTime.now());

        Payment updatedPayment = paymentRepository.save(payment);
        return mapToResponse(updatedPayment);
    }

    public List<PaymentResponse> getPendingVerifications() {
        return paymentRepository.findByVerifiedFalseOrderByCreatedAtDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<PaymentResponse> getMemberPayments(Long memberId) {
        Member member = memberService.getMemberById(memberId);
        return paymentRepository.findByMemberOrderByPaymentYearDescPaymentMonthDesc(member).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private PaymentResponse mapToResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setMemberId(payment.getMember().getId());
        response.setMemberName(payment.getMember().getName());
        response.setAmount(payment.getAmount());
        response.setPaymentMonth(payment.getPaymentMonth());
        response.setPaymentYear(payment.getPaymentYear());
        response.setPaymentDate(payment.getPaymentDate());
        response.setVerified(payment.getVerified());
        if (payment.getVerifiedBy() != null) {
            response.setVerifiedByUsername(payment.getVerifiedBy().getUsername());
        }
        response.setVerifiedAt(payment.getVerifiedAt());
        response.setCreatedAt(payment.getCreatedAt());
        return response;
    }
}
