package com.gym.management.repository;

import com.gym.management.entity.Attendance;
import com.gym.management.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByMemberAndAttendanceDate(Member member, LocalDate attendanceDate);

    boolean existsByMemberAndAttendanceDate(Member member, LocalDate attendanceDate);

    List<Attendance> findByMemberOrderByAttendanceDateDesc(Member member);

    List<Attendance> findByAttendanceDateOrderByCheckInTime(LocalDate attendanceDate);
}
