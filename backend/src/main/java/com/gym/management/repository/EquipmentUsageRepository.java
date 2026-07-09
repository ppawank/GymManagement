package com.gym.management.repository;

import com.gym.management.entity.EquipmentUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipmentUsageRepository extends JpaRepository<EquipmentUsage, Long> {

    List<EquipmentUsage> findByBranchId(Long branchId);

    List<EquipmentUsage> findByMemberId(Long memberId);

    List<EquipmentUsage> findByEndTimeIsNull();
}
