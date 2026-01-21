package com.gym.management.service;

import com.gym.management.dto.MemberRequest;
import com.gym.management.entity.Member;
import com.gym.management.enums.MemberStatus;
import com.gym.management.exception.BusinessException;
import com.gym.management.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public Member registerMember(MemberRequest request) {
        // Check if email already exists
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("DUPLICATE_EMAIL",
                    "Member with email " + request.getEmail() + " already exists");
        }

        Member member = new Member();
        member.setName(request.getName());
        member.setEmail(request.getEmail());
        member.setPhone(request.getPhone());
        member.setJoinDate(request.getJoinDate());
        member.setStatus(MemberStatus.ACTIVE);

        return memberRepository.save(member);
    }

    private final com.gym.management.repository.PaymentRepository paymentRepository;

    public List<com.gym.management.dto.MemberResponse> getAllMembers() {
        return memberRepository.findAll().stream()
                .map(this::mapToMemberResponse)
                .collect(java.util.stream.Collectors.toList());
    }

    public List<com.gym.management.dto.MemberResponse> getPendingFeeMembers() {
        java.time.LocalDate now = java.time.LocalDate.now();
        java.time.LocalDate sevenDaysFromNow = now.plusDays(7);

        return memberRepository.findAll().stream()
                .map(this::mapToMemberResponse)
                .filter(memberResponse -> {
                    // Include if fee is pending for current month
                    boolean hasPendingFee = "PENDING".equals(memberResponse.getFeeStatus());

                    // Include if membership expires in next 7 days
                    boolean expiringInSevenDays = memberResponse.getExpiringInSevenDays() != null
                            && memberResponse.getExpiringInSevenDays();

                    return hasPendingFee || expiringInSevenDays;
                })
                .collect(java.util.stream.Collectors.toList());
    }

    private com.gym.management.dto.MemberResponse mapToMemberResponse(Member member) {
        com.gym.management.dto.MemberResponse response = new com.gym.management.dto.MemberResponse();
        response.setId(member.getId());
        response.setName(member.getName());
        response.setEmail(member.getEmail());
        response.setPhone(member.getPhone());
        response.setJoinDate(member.getJoinDate());
        response.setStatus(member.getStatus());

        // Calculate fee status for current month
        java.time.LocalDate now = java.time.LocalDate.now();
        boolean isPaid = paymentRepository.existsByMemberAndPaymentMonthAndPaymentYear(
                member, now.getMonthValue(), now.getYear());

        response.setFeeStatus(isPaid ? "PAID" : "PENDING");

        // Calculate membership expiry date (last payment + 1 month)
        java.util.List<com.gym.management.entity.Payment> payments = paymentRepository
                .findByMemberOrderByPaymentYearDescPaymentMonthDesc(member);

        if (!payments.isEmpty()) {
            com.gym.management.entity.Payment lastPayment = payments.get(0);
            java.time.LocalDate expiryDate = java.time.LocalDate.of(
                    lastPayment.getPaymentYear(),
                    lastPayment.getPaymentMonth(),
                    1).plusMonths(1).minusDays(1); // Last day of the paid month

            response.setMembershipExpiryDate(expiryDate);

            // Check if expiring in next 7 days
            java.time.LocalDate sevenDaysFromNow = now.plusDays(7);
            response.setExpiringInSevenDays(
                    expiryDate.isAfter(now) && !expiryDate.isAfter(sevenDaysFromNow));
        } else {
            // No payments yet - consider as expiring
            response.setMembershipExpiryDate(member.getJoinDate());
            response.setExpiringInSevenDays(true);
        }

        return response;
    }

    @SuppressWarnings("null")
    public Member getMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new BusinessException("MEMBER_NOT_FOUND",
                        "Member with ID " + id + " not found"));
    }

    @Transactional
    public Member activateMember(Long id) {
        Member member = getMemberById(id);

        if (member.getStatus() == MemberStatus.ACTIVE) {
            throw new BusinessException("ALREADY_ACTIVE",
                    "Member is already active");
        }

        member.setStatus(MemberStatus.ACTIVE);
        return memberRepository.save(member);
    }

    @Transactional
    public Member deactivateMember(Long id) {
        Member member = getMemberById(id);

        if (member.getStatus() == MemberStatus.INACTIVE) {
            throw new BusinessException("ALREADY_INACTIVE",
                    "Member is already inactive");
        }

        member.setStatus(MemberStatus.INACTIVE);
        return memberRepository.save(member);
    }
}
