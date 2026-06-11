package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.RoleMenuPermissionSaveRequest;
import com.example.springboot.service.RoleMenuPermissionService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/role-menu-permissions")
public class RoleMenuPermissionController {

    private final RoleMenuPermissionService roleMenuPermissionService;

    public RoleMenuPermissionController(RoleMenuPermissionService roleMenuPermissionService) {
        this.roleMenuPermissionService = roleMenuPermissionService;
    }

    @GetMapping("/roles")
    public Result roles() {
        return Result.success(roleMenuPermissionService.getAllRoles());
    }

    @GetMapping("/overview")
    public Result overview() {
        return Result.success(roleMenuPermissionService.buildPermissionOverview());
    }

    @GetMapping("/byRole")
    public Result byRole(@RequestParam String role) {
        return Result.success(roleMenuPermissionService.getPermissionsByRole(role));
    }

    @PostMapping("/save")
    public Result save(@RequestBody RoleMenuPermissionSaveRequest request) {
        try {
            roleMenuPermissionService.savePermissions(request);
            return Result.success("保存成功");
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }
}
