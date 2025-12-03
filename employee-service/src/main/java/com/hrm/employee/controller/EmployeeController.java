package com.hrm.employee.controller;

import com.hrm.employee.mapper.EmployeeMapper;
import com.hrm.employee.model.Employee;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/employees")
public class EmployeeController {
    private final EmployeeMapper employeeMapper;

    public EmployeeController(EmployeeMapper employeeMapper) {
        this.employeeMapper = employeeMapper;
    }

    // API để ADMIN/MANAGER xem danh sách
    @GetMapping("/list")
    public List<Employee> findAllEmployees(@RequestHeader("X-User-Role") String userRole) {
        if (userRole.equals("ADMIN") || userRole.equals("MANAGER")) {
            return employeeMapper.selectAll();
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }

    // API để nhân viên xem hồ sơ của chính mình
    @GetMapping("/profile")
    public Employee getMyProfile(@RequestHeader("X-User-ID") String authUserId) {
        UUID userId = UUID.fromString(authUserId);
        Employee employee = employeeMapper.selectByPrimaryKey(userId);
        if (ObjectUtils.isEmpty(employee)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee profile not found");
        }
        return employee;
    }

    // API Thêm mới (Chỉ ADMIN)
    @PostMapping
    public ResponseEntity<Void> createEmployee(@RequestHeader("X-User-Role") String userRole, @RequestBody Employee employee) {
        if (!userRole.equals("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        employeeMapper.insert(employee);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
