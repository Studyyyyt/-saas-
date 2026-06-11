package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.Role;
import com.example.springboot.service.RoleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理控制器
 */
@RestController
@RequestMapping("/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public Result list() {
        List<Role> roles = roleService.listAll();
        return Result.success(roles);
    }

    @PostMapping
    public Result create(@RequestBody Role role) {
        try {
            Role created = roleService.create(role);
            return Result.success(created);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @RequestBody Role role) {
        try {
            role.setId(id);
            Role updated = roleService.update(role);
            return Result.success(updated);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        try {
            roleService.delete(id);
            return Result.success("删除成功");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }
}
