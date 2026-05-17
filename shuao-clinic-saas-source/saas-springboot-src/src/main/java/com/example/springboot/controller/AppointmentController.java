package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.Account;
import com.example.springboot.entity.Appointment;
import com.example.springboot.service.AccountService;
import com.example.springboot.service.AppointmentService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private static final String OPERATOR_ACCOUNT_ID_HEADER = "X-Operator-Account-Id";
    private static final String SECONDARY_PASSWORD_HEADER = "X-Secondary-Password";
    @Value("${security.patient-admin-secondary-password:246810}")
    private String patientAdminSecondaryPassword;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private AccountService accountService;

//    @GetMapping("/all")
//    public Result getAllAppointments() {
//        List<Appointment> appointments = appointmentService.getAllAppointments();
//        return Result.success(appointments);
//    }
    /**
     * 添加分页功能PageHelper
     */
//    @GetMapping("/selectAll")
//    public Result selectAll(@RequestParam int page, @RequestParam int size) {
//        PageHelper.startPage(page, size);
//        List<Appointment> appointments = appointmentService.getAllAppointments();
//        PageInfo<Appointment> pageInfo = new PageInfo<>(appointments);
//        return Result.success(pageInfo);
//    }
    /**
     * 根据状态过滤
     */
    @GetMapping("/selectAll")
    public Result selectAll(@RequestParam int page, @RequestParam int size, @RequestParam(required = false) String status) {
        PageHelper.startPage(page, size);
        List<Appointment> appointments;
        if (status != null && !status.isEmpty()) {
            appointments = appointmentService.getAllAppointmentsByStatus(status);
        } else {
            appointments = appointmentService.getAllAppointments();
        }
        PageInfo<Appointment> pageInfo = new PageInfo<>(appointments);
        return Result.success(pageInfo);
    }

    @GetMapping("/scheduleEntries")
    public Result scheduleEntries() {
        return Result.success(appointmentService.getScheduleEntries());
    }

    @GetMapping("/selectByid")
    public Result selectById(@RequestParam Long id, @RequestParam int page, @RequestParam int size, @RequestParam(required = false) String status) {
        PageHelper.startPage(page, size);
        List<Appointment> appointments;
        if (status != null && !status.isEmpty()) {
            appointments = appointmentService.selectByIdAndStatus(id, status);
        } else {
            appointments = appointmentService.selectById(id);
        }
        PageInfo<Appointment> pageInfo = new PageInfo<>(appointments);
        return Result.success(pageInfo);
    }

    @GetMapping("/public/detail")
    public Result publicDetail(@RequestParam Long id) {
        List<Appointment> appointments = appointmentService.selectById(id);
        if (appointments == null || appointments.isEmpty()) {
            return Result.error("未找到预约信息");
        }
        return Result.success(appointments.get(0));
    }

    @GetMapping("/selectByname")
    public Result selectByName(@RequestParam String name, @RequestParam int page, @RequestParam int size, @RequestParam(required = false) String status) {
        PageHelper.startPage(page, size);
        List<Appointment> appointments;
        if (status != null && !status.isEmpty()) {
            appointments = appointmentService.selectByNameAndStatus(name, status);
        } else {
            appointments = appointmentService.selectByName(name);
        }
        PageInfo<Appointment> pageInfo = new PageInfo<>(appointments);
        return Result.success(pageInfo);
    }

//    @GetMapping("/selectByid")
//    public Result selectById(@RequestParam Long id,@RequestParam int page, @RequestParam int size) {
//        PageHelper.startPage(page, size);
//        List<Appointment> appointments = appointmentService.selectById(id);
//        PageInfo<Appointment> pageInfo = new PageInfo<>(appointments);
//        return Result.success(pageInfo);
//    }

//    @GetMapping("/selectByname")
//    public Result selectByName(@RequestParam String name,@RequestParam int page, @RequestParam int size) {
//        PageHelper.startPage(page, size);
//        List<Appointment> appointments = appointmentService.selectByName(name);
//        PageInfo<Appointment> pageInfo = new PageInfo<>(appointments);
//        return Result.success(pageInfo);
//    }

    @PutMapping("/updateStatus/{id}")
    public Result updateStatus(@PathVariable Long id, @RequestBody Appointment appointment,
                               @RequestHeader(value = OPERATOR_ACCOUNT_ID_HEADER, required = false) Long operatorAccountId) {
        String roleValidation = validateOperatorRole(operatorAccountId);
        if (roleValidation != null) {
            return Result.error("403", roleValidation);
        }
        appointmentService.updateStatus(id, appointment.getStatus());
        return Result.success("状态更新成功");
    }

    @PutMapping("/updateClinicStatus/{id}")
    public Result updateClinicStatus(@PathVariable Long id, @RequestBody Appointment appointment,
                                     @RequestHeader(value = OPERATOR_ACCOUNT_ID_HEADER, required = false) Long operatorAccountId) {
        String roleValidation = validateOperatorRole(operatorAccountId);
        if (roleValidation != null) {
            return Result.error("403", roleValidation);
        }
        try {
            appointmentService.updateClinicStatus(id, appointment.getClinic_status());
            return Result.success("接诊状态更新成功");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/add")
    public Result addAppointment(@RequestBody Appointment appointment,
                                 @RequestHeader(value = OPERATOR_ACCOUNT_ID_HEADER, required = false) Long operatorAccountId) {
        String roleValidation = validateOperatorRole(operatorAccountId);
        if (roleValidation != null) {
            return Result.error("403", roleValidation);
        }
        try {
            appointmentService.addAppointment(appointment);
            return Result.success("新增成功");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/manual-next-day-reminder")
    public Result manualNextDayReminder() {
        appointmentService.sendNextDayAppointmentReminders();
        return Result.success("已手动触发次日预约提醒");
    }

    @PutMapping("/edit")
    public Result editAppointment(@RequestBody Appointment appointment,
                                  @RequestHeader(value = OPERATOR_ACCOUNT_ID_HEADER, required = false) Long operatorAccountId) {
        String roleValidation = validateOperatorRole(operatorAccountId);
        if (roleValidation != null) {
            return Result.error("403", roleValidation);
        }
        try {
            appointmentService.editAppointment(appointment);
            return Result.success("编辑成功");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/cancel/{id}")
    public Result cancelAppointment(@PathVariable Long id, @RequestBody(required = false) Map<String, String> payload,
                                    @RequestHeader(value = OPERATOR_ACCOUNT_ID_HEADER, required = false) Long operatorAccountId) {
        String roleValidation = validateOperatorRole(operatorAccountId);
        if (roleValidation != null) {
            return Result.error("403", roleValidation);
        }
        try {
            String reason = payload == null ? null : payload.get("reason");
            Appointment appointment = appointmentService.cancelPatientAppointment(id, reason);
            return Result.success(appointment);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public Result deleteAppointment(@PathVariable int id,
                                    @RequestHeader(value = OPERATOR_ACCOUNT_ID_HEADER, required = false) Long operatorAccountId,
                                    @RequestHeader(value = SECONDARY_PASSWORD_HEADER, required = false) String secondaryPassword) {
        String validation = validateSensitiveOperation(operatorAccountId, secondaryPassword);
        if (validation != null) {
            return Result.error("403", validation);
        }
        appointmentService.deleteAppointment(id);
        return Result.success("删除成功");
    }

    // 批量删除
    @DeleteMapping("/deleteBatch")
    public Result deleteAppointmentBatch(@RequestBody List<Long> ids,
                                         @RequestHeader(value = OPERATOR_ACCOUNT_ID_HEADER, required = false) Long operatorAccountId,
                                         @RequestHeader(value = SECONDARY_PASSWORD_HEADER, required = false) String secondaryPassword) {
        String validation = validateSensitiveOperation(operatorAccountId, secondaryPassword);
        if (validation != null) {
            return Result.error("403", validation);
        }
        try {
            appointmentService.deleteAppointmentBatch(ids);
            return Result.success("批量删除成功");
        } catch (Exception e) {
            return Result.error("批量删除失败：" + e.getMessage());
        }
    }

    private String validateSensitiveOperation(Long operatorAccountId, String secondaryPassword) {
        if (operatorAccountId == null || operatorAccountId <= 0) {
            return "请重新登录管理员账号后再试";
        }
        List<Account> accounts = accountService.selectById(operatorAccountId);
        if (accounts == null || accounts.isEmpty() || accounts.get(0) == null) {
            return "操作账号不存在";
        }
        Account account = accounts.get(0);
        if (!"admin".equals(normalizeRoleCode(account.getRole()))) {
            return "只有管理员账号可以执行该操作";
        }
        if (!patientAdminSecondaryPassword.equals(StringUtils.hasText(secondaryPassword) ? secondaryPassword.trim() : "")) {
            return "二级密码错误";
        }
        return null;
    }

    private String validateOperatorRole(Long operatorAccountId) {
        if (operatorAccountId == null || operatorAccountId <= 0) {
            return "请重新登录后再试";
        }
        List<Account> accounts = accountService.selectById(operatorAccountId);
        if (accounts == null || accounts.isEmpty() || accounts.get(0) == null) {
            return "操作账号不存在";
        }
        Account account = accounts.get(0);
        String roleCode = normalizeRoleCode(account.getRole());
        if (!"admin".equals(roleCode) && !"doctor".equals(roleCode) && !"nurse".equals(roleCode)) {
            return "当前账号无权执行该操作";
        }
        return null;
    }

    private String normalizeRoleCode(String role) {
        if (!StringUtils.hasText(role)) {
            return "";
        }
        String normalized = role.trim();
        switch (normalized) {
            case "管理员":
                return "admin";
            case "医生":
                return "doctor";
            case "护士":
                return "nurse";
            default:
                return normalized;
        }
    }
}
