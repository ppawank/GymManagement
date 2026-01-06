package com.gym.management.controller;

import com.gym.management.dto.AttendanceRequest;
import com.gym.management.entity.Attendance;
import com.gym.management.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping
    public ResponseEntity<Attendance> markAttendance(@Valid @RequestBody AttendanceRequest request) {
        Attendance attendance = attendanceService.markAttendance(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(attendance);
    }

    @GetMapping
    public ResponseEntity<List<Attendance>> getAllAttendance() {
        List<Attendance> attendanceList = attendanceService.getAllAttendance();
        return ResponseEntity.ok(attendanceList);
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<Attendance>> getMemberAttendance(@PathVariable Long memberId) {
        List<Attendance> attendanceList = attendanceService.getMemberAttendance(memberId);
        return ResponseEntity.ok(attendanceList);
    }
}
