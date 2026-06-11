package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.config.LicenseCheckInterceptor;
import com.example.springboot.entity.Account;
import com.example.springboot.entity.Clinic;
import com.example.springboot.mapper.AccountMapper;
import com.example.springboot.service.AccountService;
import com.example.springboot.service.ClinicService;
import com.example.springboot.service.LicenseVerificationService;
import com.example.springboot.service.RoleMenuPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AccountService accountService;

    @Autowired
    private RoleMenuPermissionService roleMenuPermissionService;

    @Autowired
    private ClinicService clinicService;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private LicenseVerificationService licenseVerificationService;

    @Autowired
    private LicenseCheckInterceptor licenseCheckInterceptor;

    /**
     * 检查系统是否需要初始化（users 表为空）
     */
    @GetMapping("/needs-init")
    public Result needsInit() {
        int count = accountMapper.countAll();
        return Result.success(Map.of("needsInit", count == 0));
    }

    /**
     * 系统初始化：验证激活码后创建第一个超级管理员和默认诊所
     */
    @PostMapping("/init")
    @Transactional
    public Result initSystem(@RequestBody Map<String, String> request) {
        // 如果已有用户，拒绝重复初始化
        if (accountMapper.countAll() > 0) {
            return Result.error("400", "系统已初始化，无法重复操作");
        }

        String username = request == null ? null : request.get("username");
        String password = request == null ? null : request.get("password");
        String clinicName = request == null ? null : request.get("clinicName");
        String activationCode = request == null ? null : request.get("activationCode");

        if (username == null || username.trim().isEmpty()) {
            return Result.error("400", "管理员账号不能为空");
        }
        if (password == null || password.length() < 6) {
            return Result.error("400", "密码长度至少6位");
        }
        if (clinicName == null || clinicName.trim().isEmpty()) {
            return Result.error("400", "诊所名称不能为空");
        }
        if (activationCode == null || activationCode.trim().isEmpty()) {
            return Result.error("400", "激活码不能为空");
        }

        // 先验证激活码（传入即将创建的管理员用户名作为 machine_id，确保与后续登录一致）
        Clinic tempClinic = new Clinic();
        tempClinic.setId("default");
        tempClinic.setActivationCode(activationCode.trim());
        LicenseVerificationService.LicenseVerifyResult verifyResult =
                licenseVerificationService.verifyClinicLicense(tempClinic, username.trim());
        if (!verifyResult.isValid()) {
            return Result.error("403", "激活码验证失败: " + verifyResult.getMessage());
        }

        // 创建或更新默认诊所（绑定激活码）
        Clinic existingClinic = clinicService.getById("default");
        if (existingClinic != null) {
            // V67 迁移已创建占位诊所，更新为实际信息
            existingClinic.setName(clinicName.trim());
            existingClinic.setStatus(1);
            existingClinic.setActivationCode(activationCode.trim());
            existingClinic.setLicenseExpiresAt(verifyResult.getExpiresAt());
            clinicService.updateClinic(existingClinic);
        } else {
            Clinic clinic = new Clinic();
            clinic.setId("default");
            clinic.setName(clinicName.trim());
            clinic.setStatus(1);
            clinic.setActivationCode(activationCode.trim());
            clinic.setLicenseExpiresAt(verifyResult.getExpiresAt());
            // 注册流程中创建首个诊所，尚无用户ID，传 null 避免自动绑定
            clinicService.createClinic(clinic, null);
        }

        // 创建超级管理员账号
        Account account = new Account();
        account.setUsername(username.trim());
        account.setPassword(password);
        account.setName("管理员");
        account.setRole("admin");
        accountService.addAccount(account);

        // 关联管理员与默认诊所
        clinicService.assignClinicToUser(account.getId(), "default", "admin", 1);

        return Result.success("系统初始化成功");
    }

    @PostMapping("/login")
    public Result login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest == null ? null : loginRequest.get("username");
        String password = loginRequest == null ? null : loginRequest.get("password");

        Account account = accountService.authenticateByUsernameAndPassword(username, password);
        if (account == null) {
            return Result.error("401", "账号名称或密码错误");
        }

        String roleCode = normalizeRoleCode(account.getRole());
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", account.getId());
        data.put("username", account.getUsername() == null ? "" : account.getUsername().trim());
        data.put("name", account.getName() == null ? "" : account.getName().trim());
        data.put("avatar", account.getAvatar());
        data.put("role", roleCode);
        data.put("roleLabel", resolveRoleLabel(roleCode));
        data.put("allowedMenuKeys", roleMenuPermissionService.getAllowedMenuKeys(roleCode));
        data.put("roleMenuPermissionsLoaded", true);

        // 查询用户关联的诊所列表
        List<Map<String, Object>> clinics = clinicService.getUserClinics(account.getId());
        data.put("clinics", clinics);

        // 验证系统级激活码授权状态（绑定在 default 诊所上）
        LicenseVerificationService.LicenseVerifyResult verifyResult =
                licenseVerificationService.verifyByClinicId("default");
        if (!verifyResult.isValid()) {
            // 管理员可以进入续期流程，普通用户直接拒绝
            if ("admin".equals(roleCode)) {
                data.put("needsRenewal", true);
                data.put("licenseMessage", verifyResult.getMessage());
                return Result.error("403", "系统授权已过期: " + verifyResult.getMessage(), data);
            }
            return Result.error("403", "系统授权已过期: " + verifyResult.getMessage());
        }

        // 将系统授权信息加入登录响应
        Clinic defaultClinic = clinicService.getById("default");
        if (defaultClinic != null) {
            Map<String, Object> licenseInfo = new LinkedHashMap<>();
            licenseInfo.put("activationCode", defaultClinic.getActivationCode());
            licenseInfo.put("expiresAt", defaultClinic.getLicenseExpiresAt());
            licenseInfo.put("isValid", true);
            data.put("licenseInfo", licenseInfo);
        }

        // 如果用户只有一个诊所，自动设置默认诊所ID
        if (clinics != null && clinics.size() == 1) {
            data.put("currentClinicId", clinics.get(0).get("clinicId"));
            data.put("currentClinicName", clinics.get(0).get("clinicName"));
        }

        // 登录成功时清除授权缓存，确保获取最新授权状态
        licenseCheckInterceptor.invalidateCache();

        return Result.success(data);
    }

    /**
     * 管理员续期：输入新激活码更新系统授权
     */
    @PostMapping("/renew-license")
    @Transactional
    public Result renewLicense(@RequestBody Map<String, String> request) {
        String username = request == null ? null : request.get("username");
        String password = request == null ? null : request.get("password");
        String activationCode = request == null ? null : request.get("activationCode");

        // 验证管理员身份
        Account account = accountService.authenticateByUsernameAndPassword(username, password);
        if (account == null) {
            return Result.error("401", "账号名称或密码错误");
        }
        if (!"admin".equals(normalizeRoleCode(account.getRole()))) {
            return Result.error("403", "仅管理员可操作");
        }

        if (activationCode == null || activationCode.trim().isEmpty()) {
            return Result.error("400", "激活码不能为空");
        }

        // 验证新激活码
        Clinic tempClinic = new Clinic();
        tempClinic.setId("default");
        tempClinic.setActivationCode(activationCode.trim());
        LicenseVerificationService.LicenseVerifyResult verifyResult =
                licenseVerificationService.verifyClinicLicense(tempClinic);
        if (!verifyResult.isValid()) {
            return Result.error("403", "激活码验证失败: " + verifyResult.getMessage());
        }

        // 更新 default 诊所的激活码
        Clinic defaultClinic = clinicService.getById("default");
        if (defaultClinic == null) {
            return Result.error("500", "系统异常：默认诊所不存在");
        }
        defaultClinic.setActivationCode(activationCode.trim());
        defaultClinic.setLicenseExpiresAt(verifyResult.getExpiresAt());
        clinicService.updateClinic(defaultClinic);

        // 续期成功，清除授权拦截器缓存，避免旧缓存导致后续请求被拒绝
        licenseCheckInterceptor.invalidateCache();

        // 续期成功后，自动完成登录流程返回用户数据
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", account.getId());
        data.put("username", account.getUsername() == null ? "" : account.getUsername().trim());
        data.put("name", account.getName() == null ? "" : account.getName().trim());
        data.put("avatar", account.getAvatar());
        String roleCode = normalizeRoleCode(account.getRole());
        data.put("role", roleCode);
        data.put("roleLabel", resolveRoleLabel(roleCode));
        data.put("allowedMenuKeys", roleMenuPermissionService.getAllowedMenuKeys(roleCode));
        data.put("roleMenuPermissionsLoaded", true);

        List<Map<String, Object>> clinics = clinicService.getUserClinics(account.getId());
        data.put("clinics", clinics);

        Map<String, Object> licenseInfo = new LinkedHashMap<>();
        licenseInfo.put("activationCode", activationCode.trim());
        licenseInfo.put("expiresAt", verifyResult.getExpiresAt());
        licenseInfo.put("isValid", true);
        data.put("licenseInfo", licenseInfo);

        if (clinics != null && clinics.size() == 1) {
            data.put("currentClinicId", clinics.get(0).get("clinicId"));
            data.put("currentClinicName", clinics.get(0).get("clinicName"));
        }

        return Result.success(data);
    }

    /**
     * 手动刷新并检查授权状态（供前端调用）
     */
    @GetMapping("/check-license")
    public Result checkLicense() {
        // 清除缓存，强制重新验证
        licenseCheckInterceptor.invalidateCache();

        LicenseVerificationService.LicenseVerifyResult result =
                licenseVerificationService.verifyByClinicId("default");

        if (!result.isValid()) {
            return Result.error("403", "授权无效: " + result.getMessage());
        }

        Clinic defaultClinic = clinicService.getById("default");
        Map<String, Object> licenseInfo = new LinkedHashMap<>();
        if (defaultClinic != null) {
            licenseInfo.put("activationCode", defaultClinic.getActivationCode());
            licenseInfo.put("expiresAt", defaultClinic.getLicenseExpiresAt());
        }
        licenseInfo.put("isValid", true);
        return Result.success(licenseInfo);
    }

    private String normalizeRoleCode(String role) {
        if (role == null) {
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

    private String resolveRoleLabel(String roleCode) {
        switch (roleCode) {
            case "admin":
                return "管理员";
            case "doctor":
                return "医生";
            case "nurse":
                return "护士";
            default:
                return roleCode;
        }
    }
}
