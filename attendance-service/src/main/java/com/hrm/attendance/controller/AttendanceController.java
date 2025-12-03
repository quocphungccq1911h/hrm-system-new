package com.hrm.attendance.controller;

import com.hrm.attendance.mapper.AttendanceRecordMapper;
import com.hrm.attendance.mapper.ext.ExtAttendanceMapper;
import com.hrm.attendance.model.AttendanceRecord;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    private final AttendanceRecordMapper attendanceMapper;
    private final ExtAttendanceMapper extAttendanceMapper;

    public AttendanceController(AttendanceRecordMapper attendanceMapper,
                                ExtAttendanceMapper extAttendanceMapper) {
        this.attendanceMapper = attendanceMapper;
        this.extAttendanceMapper = extAttendanceMapper;
    }

    // Endpoint Check-in
    @PostMapping("/check-in")
    public ResponseEntity<String> checkIn(@RequestHeader("X-User-ID") String authUserId) {
        UUID userId = UUID.fromString(authUserId);
        Date today = new java.sql.Date(System.currentTimeMillis());

        // 1. Kiểm tra đã check-in chưa
        AttendanceRecord existingRecord = extAttendanceMapper.findTodayRecord(userId, today);

        if (!ObjectUtils.isEmpty(existingRecord)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Already checked in today.");
        }

        // 2. Thực hiện Check-in
        AttendanceRecord newRecord = new AttendanceRecord();
        newRecord.setAuthUserId(userId);
        newRecord.setRecordDate(today);
        newRecord.setCheckInTime(new Date());

        attendanceMapper.insert(newRecord);
        return ResponseEntity.ok("Check-in successful at " + newRecord.getCheckInTime());
    }

    @PostMapping("/check-out")
    public ResponseEntity<String> checkOut(@RequestHeader("X-User-ID") String authUserId) {
        UUID userId = UUID.fromString(authUserId);

        // 💡 Thay đổi từ LocalDate.now() sang java.sql.Date
        Date today = new java.sql.Date(System.currentTimeMillis());

        // 1. Tìm hồ sơ check-in hôm nay
        AttendanceRecord record = extAttendanceMapper.findTodayRecord(userId, today);

        if (record == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Must check in first.");
        }
        // LƯU Ý: checkOutTime trong Model (AttendanceRecord) phải là java.util.Date
        if (record.getCheckOutTime() != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Already checked out today.");
        }

        // 2. Thực hiện Check-out
        record.setCheckOutTime(new Date()); // Sử dụng java.util.Date
        extAttendanceMapper.updateCheckOutTime(record);

        return ResponseEntity.ok("Check-out successful at " + record.getCheckOutTime());
    }
}
