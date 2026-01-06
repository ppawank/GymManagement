package com.gym.management.repository;

import com.gym.management.entity.Member;
import com.gym.management.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByMemberAndPaymentMonthAndPaymentYear(Member member, Integer month, Integer year);

    boolean existsByMemberAndPaymentMonthAndPaymentYear(Member member, Integer month, Integer year);

    List<Payment> findByMemberOrderByPaymentYearDescPaymentMonthDesc(Member member);

    List<Payment> findByPaymentMonthAndPaymentYearOrderByPaymentDate(Integer month, Integer year);
}
