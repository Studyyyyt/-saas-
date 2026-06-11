package com.example.springboot.entity;

import lombok.Data;

import java.util.Map;

@Data
public class RoleMenuPermissionSaveRequest {
    private Map<String, Map<String, Boolean>> role_permissions;
}
