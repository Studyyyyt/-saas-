package com.example.springboot.controller;
import com.example.springboot.common.Result;
import com.example.springboot.entity.Account;
import com.example.springboot.service.AccountService;
import com.example.springboot.service.RoleMenuPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:7070")
@RestController
@RequestMapping("/loginController")
public class loginController {
    @Autowired
    private AccountService accountService;

    @Autowired
    private RoleMenuPermissionService roleMenuPermissionService;

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
        data.put("role", roleCode);
        data.put("roleLabel", resolveRoleLabel(roleCode));
        data.put("allowedMenuKeys", roleMenuPermissionService.getAllowedMenuKeys(roleCode));
        data.put("roleMenuPermissionsLoaded", true);
        return Result.success(data);
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
