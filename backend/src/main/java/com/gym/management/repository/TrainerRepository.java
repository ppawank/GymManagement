package com.gym.management.repository;

import com.gym.management.entity.Trainer;
import com.gym.management.enums.AvailabilityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainerRepository extends JpaRepository<Trainer, Long> {

    List<Trainer> findByBranchId(Long branchId);

    List<Trainer> findByAvailabilityStatus(AvailabilityStatus status);

    List<Trainer> findByBranchIdAndAvailabilityStatus(Long branchId, AvailabilityStatus status);
}
