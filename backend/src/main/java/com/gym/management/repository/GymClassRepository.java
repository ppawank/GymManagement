package com.gym.management.repository;

import com.gym.management.entity.GymClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GymClassRepository extends JpaRepository<GymClass, Long> {

    List<GymClass> findByBranchId(Long branchId);

    List<GymClass> findByTrainerId(Long trainerId);
}
