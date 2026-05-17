package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.Account;
import com.example.springboot.entity.MedicalRecord;
import com.example.springboot.service.AccountService;
import com.example.springboot.service.MedicalRecordService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/medical-records")
public class MedicalRecordController {

    private static final String OPERATOR_ACCOUNT_ID_HEADER = "X-Operator-Account-Id";
    private static final String SECONDARY_PASSWORD_HEADER = "X-Secondary-Password";
    @Value("${security.patient-admin-secondary-password:246810}")
    private String patientAdminSecondaryPassword;

    @Autowired
    private MedicalRecordService service;

    @Autowired
    private AccountService accountService;

    @GetMapping("/selectAll")
    public Result selectAll(@RequestParam int page, @RequestParam int size,
                            @RequestParam(required = false) Long doctorAccountId,
                            @RequestParam(required = false) String recordStatus,
                            @RequestParam(required = false) String startDate,
                            @RequestParam(required = false) String endDate) {
        PageHelper.startPage(page, size);
        List<MedicalRecord> list = service.selectAll(doctorAccountId, recordStatus, startDate, endDate);
        return Result.success(new PageInfo<>(list));
    }

    @GetMapping("/selectByPatientId")
    public Result selectByPatientId(@RequestParam Long patientId,
                                     @RequestParam int page, @RequestParam int size) {
        PageHelper.startPage(page, size);
        List<MedicalRecord> list = service.selectByPatientId(patientId);
        return Result.success(new PageInfo<>(list));
    }

    @GetMapping("/selectByPatientName")
    public Result selectByPatientName(@RequestParam String name,
                                       @RequestParam int page, @RequestParam int size) {
        PageHelper.startPage(page, size);
        List<MedicalRecord> list = service.selectByPatientName(name);
        return Result.success(new PageInfo<>(list));
    }

    @GetMapping("/selectById")
    public Result selectById(@RequestParam Long id) {
        return Result.success(service.selectById(id));
    }

    @PostMapping("/add")
    public Result add(@RequestBody MedicalRecord record,
                      @RequestHeader(value = OPERATOR_ACCOUNT_ID_HEADER, required = false) Long operatorAccountId) {
        String roleValidation = validateOperatorRole(operatorAccountId);
        if (roleValidation != null) {
            return Result.error("403", roleValidation);
        }
        try {
            service.add(record);
            return Result.success(record);
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @PutMapping("/edit")
    public Result edit(@RequestBody MedicalRecord record,
                       @RequestHeader(value = OPERATOR_ACCOUNT_ID_HEADER, required = false) Long operatorAccountId) {
        String roleValidation = validateOperatorRole(operatorAccountId);
        if (roleValidation != null) {
            return Result.error("403", roleValidation);
        }
        try {
            service.update(record);
            return Result.success(record);
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id,
                         @RequestHeader(value = OPERATOR_ACCOUNT_ID_HEADER, required = false) Long operatorAccountId,
                         @RequestHeader(value = SECONDARY_PASSWORD_HEADER, required = false) String secondaryPassword) {
        String validation = validateSensitiveOperation(operatorAccountId, secondaryPassword);
        if (validation != null) {
            return Result.error("403", validation);
        }
        service.delete(id);
        return Result.success("删除成功");
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
