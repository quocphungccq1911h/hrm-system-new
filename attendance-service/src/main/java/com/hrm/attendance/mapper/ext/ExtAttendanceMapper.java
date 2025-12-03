package com.hrm.attendance.mapper.ext;

import com.hrm.attendance.model.AttendanceRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Mapper
public interface ExtAttendanceMapper {
    // Tìm hồ sơ chấm công trong ngày của một user
    AttendanceRecord findTodayRecord(@Param("authUserId") UUID authUserId, @Param("date") Date date);
    // Cập nhật hồ sơ (check-out)
    void updateCheckOutTime(AttendanceRecord record);
    // Tìm tất cả hồ sơ của một user
    List<AttendanceRecord> findAllByAuthUserId(@Param("authUserId") UUID authUserId);
}
