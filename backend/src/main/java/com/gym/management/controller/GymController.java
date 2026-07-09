package com.gym.management.controller;

import com.gym.management.entity.*;
import com.gym.management.enums.AvailabilityStatus;
import com.gym.management.exception.BusinessException;
import com.gym.management.repository.*;
import com.gym.management.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for branch, trainer, class, equipment, and occupancy operations.
 */
@RestController
@RequestMapping("/api/gym")
@RequiredArgsConstructor
public class GymController {

    private final BranchRepository branchRepository;
    private final TrainerRepository trainerRepository;
    private final GymClassRepository gymClassRepository;
    private final EquipmentUsageRepository equipmentUsageRepository;
    private final AttendanceRepository attendanceRepository;
    private final MemberRepository memberRepository;
    private final WebSocketService webSocketService;

    // ──── Branches ────

    @GetMapping("/branches")
    public ResponseEntity<List<Branch>> getAllBranches() {
        return ResponseEntity.ok(branchRepository.findAll());
    }

    @GetMapping("/branches/{id}")
    public ResponseEntity<Branch> getBranch(@PathVariable Long id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new BusinessException("BRANCH_NOT_FOUND", "Branch not found"));
        return ResponseEntity.ok(branch);
    }

    @PostMapping("/branches")
    public ResponseEntity<Branch> createBranch(@RequestBody Branch branch) {
        Branch saved = branchRepository.save(branch);
        return ResponseEntity.ok(saved);
    }

    // ──── Trainers ────

    @GetMapping("/trainers")
    public ResponseEntity<List<Trainer>> getAllTrainers(
            @RequestParam(value = "branchId", required = false) Long branchId,
            @RequestParam(value = "status", required = false) AvailabilityStatus status) {
        List<Trainer> trainers;
        if (branchId != null && status != null) {
            trainers = trainerRepository.findByBranchIdAndAvailabilityStatus(branchId, status);
        } else if (branchId != null) {
            trainers = trainerRepository.findByBranchId(branchId);
        } else if (status != null) {
            trainers = trainerRepository.findByAvailabilityStatus(status);
        } else {
            trainers = trainerRepository.findAll();
        }
        return ResponseEntity.ok(trainers);
    }

    @PostMapping("/trainers")
    public ResponseEntity<Trainer> createTrainer(@RequestBody Map<String, Object> request) {
        Trainer trainer = new Trainer();
        trainer.setName((String) request.get("name"));
        trainer.setSpecialty((String) request.get("specialty"));
        trainer.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);
        if (request.containsKey("branchId")) {
            Long branchId = Long.valueOf(request.get("branchId").toString());
            trainer.setBranch(branchRepository.findById(branchId)
                    .orElseThrow(() -> new BusinessException("BRANCH_NOT_FOUND", "Branch not found")));
        }
        Trainer saved = trainerRepository.save(trainer);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/trainers/{id}/availability")
    public ResponseEntity<Trainer> updateTrainerAvailability(
            @PathVariable Long id,
            @RequestParam("status") AvailabilityStatus status) {
        Trainer trainer = trainerRepository.findById(id)
                .orElseThrow(() -> new BusinessException("TRAINER_NOT_FOUND", "Trainer not found"));
        trainer.setAvailabilityStatus(status);
        Trainer saved = trainerRepository.save(trainer);
        webSocketService.broadcastTrainerUpdate(saved.getId(), saved.getName(), saved.getAvailabilityStatus().name());
        return ResponseEntity.ok(saved);
    }

    // ──── Classes ────

    @GetMapping("/classes")
    public ResponseEntity<List<GymClass>> getAllClasses(
            @RequestParam(value = "branchId", required = false) Long branchId) {
        List<GymClass> classes = (branchId != null)
                ? gymClassRepository.findByBranchId(branchId)
                : gymClassRepository.findAll();
        return ResponseEntity.ok(classes);
    }

    @PostMapping("/classes")
    public ResponseEntity<GymClass> createClass(@RequestBody Map<String, Object> request) {
        GymClass gymClass = new GymClass();
        gymClass.setName((String) request.get("name"));
        gymClass.setScheduleTime(LocalDateTime.parse((String) request.get("scheduleTime")));
        gymClass.setMaxOccupancy(Integer.parseInt(request.get("maxOccupancy").toString()));
        gymClass.setCurrentOccupancy(0);

        if (request.containsKey("trainerId")) {
            Long trainerId = Long.valueOf(request.get("trainerId").toString());
            gymClass.setTrainer(trainerRepository.findById(trainerId).orElse(null));
        }
        if (request.containsKey("branchId")) {
            Long branchId = Long.valueOf(request.get("branchId").toString());
            gymClass.setBranch(branchRepository.findById(branchId).orElse(null));
        }

        GymClass saved = gymClassRepository.save(gymClass);
        return ResponseEntity.ok(saved);
    }

    // ──── Check-In / Check-Out (Occupancy) ────

    @PostMapping("/checkin")
    public ResponseEntity<Map<String, Object>> checkIn(@RequestBody Map<String, Object> request) {
        Long memberId = Long.valueOf(request.get("memberId").toString());
        Long branchId = Long.valueOf(request.get("branchId").toString());

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException("MEMBER_NOT_FOUND", "Member not found"));
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new BusinessException("BRANCH_NOT_FOUND", "Branch not found"));

        Attendance attendance = new Attendance();
        attendance.setMember(member);
        attendance.setBranch(branch);
        attendance.setAttendanceDate(LocalDate.now());
        attendance.setCheckInTime(LocalDateTime.now());
        attendanceRepository.save(attendance);

        // Compute and broadcast live occupancy
        long occupancy = attendanceRepository.countCurrentOccupancy(branchId, LocalDate.now());
        webSocketService.broadcastOccupancy(branchId, branch.getName(), occupancy, branch.getCapacity());
        webSocketService.broadcastCheckIn(memberId, member.getName(), branchId, branch.getName());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Checked in successfully");
        response.put("memberId", memberId);
        response.put("branchId", branchId);
        response.put("currentOccupancy", occupancy);
        response.put("capacity", branch.getCapacity());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/checkout")
    public ResponseEntity<Map<String, Object>> checkOut(@RequestBody Map<String, Object> request) {
        Long memberId = Long.valueOf(request.get("memberId").toString());
        Long branchId = Long.valueOf(request.get("branchId").toString());

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException("MEMBER_NOT_FOUND", "Member not found"));
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new BusinessException("BRANCH_NOT_FOUND", "Branch not found"));

        // Find today's attendance record without checkout
        List<Attendance> records = attendanceRepository.findByAttendanceDateOrderByCheckInTime(LocalDate.now());
        Attendance attendance = records.stream()
                .filter(a -> a.getMember().getId().equals(memberId) &&
                             a.getBranch().getId().equals(branchId) &&
                             a.getCheckOutTime() == null)
                .findFirst()
                .orElseThrow(() -> new BusinessException("NO_CHECKIN", "No active check-in found"));

        attendance.setCheckOutTime(LocalDateTime.now());
        attendanceRepository.save(attendance);

        long occupancy = attendanceRepository.countCurrentOccupancy(branchId, LocalDate.now());
        webSocketService.broadcastOccupancy(branchId, branch.getName(), occupancy, branch.getCapacity());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Checked out successfully");
        response.put("currentOccupancy", occupancy);
        return ResponseEntity.ok(response);
    }

    // ──── Live Occupancy ────

    @GetMapping("/occupancy")
    public ResponseEntity<List<Map<String, Object>>> getLiveOccupancy() {
        List<Branch> branches = branchRepository.findAll();
        List<Map<String, Object>> result = branches.stream().map(branch -> {
            long occupancy = attendanceRepository.countCurrentOccupancy(branch.getId(), LocalDate.now());
            Map<String, Object> map = new HashMap<>();
            map.put("branchId", branch.getId());
            map.put("branchName", branch.getName());
            map.put("location", branch.getLocation());
            map.put("currentOccupancy", occupancy);
            map.put("capacity", branch.getCapacity());
            map.put("utilizationPercent", branch.getCapacity() > 0
                    ? Math.round((double) occupancy / branch.getCapacity() * 100) : 0);
            return map;
        }).toList();
        return ResponseEntity.ok(result);
    }

    // ──── Equipment Usage ────

    @PostMapping("/equipment/start")
    public ResponseEntity<EquipmentUsage> startEquipmentUsage(@RequestBody Map<String, Object> request) {
        Long memberId = Long.valueOf(request.get("memberId").toString());
        Long branchId = Long.valueOf(request.get("branchId").toString());
        String equipmentName = (String) request.get("equipmentName");

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException("MEMBER_NOT_FOUND", "Member not found"));
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new BusinessException("BRANCH_NOT_FOUND", "Branch not found"));

        EquipmentUsage usage = new EquipmentUsage();
        usage.setMember(member);
        usage.setBranch(branch);
        usage.setEquipmentName(equipmentName);
        usage.setStartTime(LocalDateTime.now());

        return ResponseEntity.ok(equipmentUsageRepository.save(usage));
    }

    @PostMapping("/equipment/{id}/stop")
    public ResponseEntity<EquipmentUsage> stopEquipmentUsage(@PathVariable Long id) {
        EquipmentUsage usage = equipmentUsageRepository.findById(id)
                .orElseThrow(() -> new BusinessException("USAGE_NOT_FOUND", "Equipment usage not found"));
        usage.setEndTime(LocalDateTime.now());
        return ResponseEntity.ok(equipmentUsageRepository.save(usage));
    }

    @GetMapping("/equipment/active")
    public ResponseEntity<List<EquipmentUsage>> getActiveEquipmentUsage() {
        return ResponseEntity.ok(equipmentUsageRepository.findByEndTimeIsNull());
    }
}
