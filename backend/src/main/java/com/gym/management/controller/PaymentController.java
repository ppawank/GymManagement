package com.gym.management.controller;

import com.gym.management.dto.PaymentRequest;
import com.gym.management.dto.PaymentResponse;
import com.gym.management.service.PaymentService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> recordPayment(
            @Valid @RequestBody PaymentRequest request,
            HttpSession session) {
        String token = (String) session.getAttribute("authToken");
        PaymentResponse payment = paymentService.recordPayment(request, token);
        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }

    @PostMapping("/{id}/verify")
    public ResponseEntity<PaymentResponse> verifyPayment(
            @PathVariable Long id,
            HttpSession session) {
        String token = (String) session.getAttribute("authToken");
        PaymentResponse payment = paymentService.verifyPayment(id, token);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/pending-verification")
    public ResponseEntity<List<PaymentResponse>> getPendingVerifications() {
        List<PaymentResponse> payments = paymentService.getPendingVerifications();
        return ResponseEntity.ok(payments);
    }

    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {
        List<PaymentResponse> payments = paymentService.getAllPayments();
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<PaymentResponse>> getMemberPayments(@PathVariable Long memberId) {
        List<PaymentResponse> payments = paymentService.getMemberPayments(memberId);
        return ResponseEntity.ok(payments);
    }
}
