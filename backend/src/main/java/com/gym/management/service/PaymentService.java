package com.gym.management.service;

import com.gym.management.dto.PaymentRequest;
import com.gym.management.entity.Member;
import com.gym.management.entity.Payment;
import com.gym.management.exception.BusinessException;
import com.gym.management.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final MemberService memberService;

    @Transactional
    public Payment recordPayment(PaymentRequest request) {
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

        return paymentRepository.save(payment);
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public List<Payment> getMemberPayments(Long memberId) {
        Member member = memberService.getMemberById(memberId);
        return paymentRepository.findByMemberOrderByPaymentYearDescPaymentMonthDesc(member);
    }
}
