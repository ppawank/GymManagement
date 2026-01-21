package com.gym.management.service;

import com.gym.management.dto.AttendanceRequest;
import com.gym.management.entity.Attendance;
import com.gym.management.entity.Member;
import com.gym.management.enums.MemberStatus;
import com.gym.management.exception.BusinessException;
import com.gym.management.repository.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final MemberService memberService;

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

        Attendance attendance = new Attendance();
        attendance.setMember(member);
        attendance.setAttendanceDate(request.getAttendanceDate());
        attendance.setCheckInTime(request.getCheckInTime());

        return attendanceRepository.save(attendance);
    }

    @Transactional
    public List<Attendance> markBulkAttendance(List<Long> memberIds) {
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalTime now = java.time.LocalTime.now();

        List<Attendance> attendances = new java.util.ArrayList<>();

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

            Attendance attendance = new Attendance();
            attendance.setMember(member);
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
