package com.gym.management.service;

import com.gym.management.dto.AttendanceRequest;
import com.gym.management.entity.Attendance;
import com.gym.management.entity.Branch;
import com.gym.management.entity.Member;
import com.gym.management.enums.MemberStatus;
import com.gym.management.exception.BusinessException;
import com.gym.management.repository.AttendanceRepository;
import com.gym.management.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final MemberService memberService;
    private final BranchRepository branchRepository;

    @Transactional
    public Attendance markAttendance(AttendanceRequest request) {
        Member member = memberService.getMemberById(request.getMemberId());

        // Business Rule: Only ACTIVE members can mark attendance
        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new BusinessException("MEMBER_NOT_ACTIVE",
                    "Only active members can mark attendance");
        }

        // Business Rule: One attendance per member per day
        if (attendanceRepository.existsByMemberAndAttendanceDate(
                member, request.getAttendanceDate())) {
            throw new BusinessException("DUPLICATE_ATTENDANCE",
                    "Attendance already marked for this member on " + request.getAttendanceDate());
        }

        // Resolve branch: use request branchId, or fallback to member's branch
        Branch branch = null;
        if (request.getBranchId() != null) {
            branch = branchRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new BusinessException("BRANCH_NOT_FOUND", "Branch not found"));
        } else if (member.getBranch() != null) {
            branch = member.getBranch();
        } else {
            // Default to first branch if none specified
            branch = branchRepository.findAll().stream().findFirst()
                    .orElseThrow(() -> new BusinessException("NO_BRANCHES", "No branches configured"));
        }

        Attendance attendance = new Attendance();
        attendance.setMember(member);
        attendance.setBranch(branch);
        attendance.setAttendanceDate(request.getAttendanceDate());
        attendance.setCheckInTime(request.getCheckInTime() != null
                ? request.getCheckInTime()
                : LocalDateTime.now());

        return attendanceRepository.save(attendance);
    }

    @Transactional
    public List<Attendance> markBulkAttendance(List<Long> memberIds) {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        List<Attendance> attendances = new ArrayList<>();

        for (Long memberId : memberIds) {
            Member member = memberService.getMemberById(memberId);

            // Skip if not active
            if (member.getStatus() != MemberStatus.ACTIVE) {
                continue;
            }

            // Skip if already marked today
            if (attendanceRepository.existsByMemberAndAttendanceDate(member, today)) {
                continue;
            }

            // Resolve branch
            Branch branch = member.getBranch();
            if (branch == null) {
                branch = branchRepository.findAll().stream().findFirst().orElse(null);
            }

            Attendance attendance = new Attendance();
            attendance.setMember(member);
            attendance.setBranch(branch);
            attendance.setAttendanceDate(today);
            attendance.setCheckInTime(now);

            attendances.add(attendanceRepository.save(attendance));
        }

        return attendances;
    }

    public List<Attendance> getAllAttendance() {
        return attendanceRepository.findAll();
    }

    public List<Attendance> getMemberAttendance(Long memberId) {
        Member member = memberService.getMemberById(memberId);
        return attendanceRepository.findByMemberOrderByAttendanceDateDesc(member);
    }
}
