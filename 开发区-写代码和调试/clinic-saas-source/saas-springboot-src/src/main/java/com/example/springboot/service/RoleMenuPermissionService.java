package com.example.springboot.service;

import com.example.springboot.entity.Role;
import com.example.springboot.entity.RoleMenuPermission;
import com.example.springboot.entity.RoleMenuPermissionSaveRequest;
import com.example.springboot.mapper.RoleMapper;
import com.example.springboot.mapper.RoleMenuPermissionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoleMenuPermissionService {

    private final RoleMenuPermissionMapper roleMenuPermissionMapper;
    private final RoleMapper roleMapper;

    public RoleMenuPermissionService(RoleMenuPermissionMapper roleMenuPermissionMapper, RoleMapper roleMapper) {
        this.roleMenuPermissionMapper = roleMenuPermissionMapper;
        this.roleMapper = roleMapper;
    }

    public Map<String, Object> buildPermissionOverview() {
        List<String> allRoleCodes = getAllRoleCodes();

        Map<String, Map<String, Boolean>> rolePermissions = new LinkedHashMap<>();
        for (String roleCode : allRoleCodes) {
            rolePermissions.put(roleCode, getPermissionsByRole(roleCode));
        }

        List<Map<String, Object>> catalog = new ArrayList<>();
        for (RoleMenuPermissionCatalog.MenuItem item : RoleMenuPermissionCatalog.items()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("menu_key", item.menuKey());
            row.put("menu_label", item.menuLabel());
            row.put("group_label", item.groupLabel());
            row.put("default_roles", item.defaultRoles());
            catalog.add(row);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("roles", allRoleCodes);
        result.put("catalog", catalog);
        result.put("role_permissions", rolePermissions);
        return result;
    }

    /**
     * 获取系统中所有活跃角色的角色码列表
     */
    public List<String> getAllRoleCodes() {
        return roleMapper.selectAllActive().stream()
                .map(Role::getCode)
                .collect(Collectors.toList());
    }

    /**
     * 获取系统中所有活跃角色的完整信息
     */
    public List<Role> getAllRoles() {
        return roleMapper.selectAllActive();
    }

    public Map<String, Boolean> getPermissionsByRole(String roleCode) {
        String normalizedRole = RoleMenuPermissionCatalog.normalizeRole(roleCode);
        Map<String, Boolean> merged = new LinkedHashMap<>(RoleMenuPermissionCatalog.buildDefaultRolePermissions(normalizedRole));
        List<RoleMenuPermission> rows = roleMenuPermissionMapper.selectByRoleCode(normalizedRole);
        for (RoleMenuPermission row : rows) {
            if (row == null || !RoleMenuPermissionCatalog.containsMenuKey(row.getMenuKey())) {
                continue;
            }
            merged.put(row.getMenuKey(), row.getEnabled() != null && row.getEnabled() == 1);
        }
        if (RoleMenuPermissionCatalog.ROLE_ADMIN.equals(normalizedRole)) {
            merged.put("/SystemAccountPermission", true);
        }
        return merged;
    }

    public List<String> getAllowedMenuKeys(String roleCode) {
        return getPermissionsByRole(roleCode).entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    @Transactional
    public void savePermissions(RoleMenuPermissionSaveRequest request) {
        if (request == null || request.getRole_permissions() == null || request.getRole_permissions().isEmpty()) {
            throw new IllegalArgumentException("权限配置不能为空");
        }
        for (Map.Entry<String, Map<String, Boolean>> entry : request.getRole_permissions().entrySet()) {
            String roleCode = entry.getKey();
            Map<String, Boolean> submitted = entry.getValue();
            if (roleCode == null || submitted == null) {
                continue;
            }
            replaceRolePermissions(roleCode, submitted);
        }
    }

    private void replaceRolePermissions(String roleCode, Map<String, Boolean> submitted) {
        String normalizedRole = RoleMenuPermissionCatalog.normalizeRole(roleCode);
        if (normalizedRole == null || normalizedRole.isEmpty()) {
            throw new IllegalArgumentException("角色码不能为空");
        }
        roleMenuPermissionMapper.deleteByRoleCode(normalizedRole);

        Map<String, Boolean> sanitized = new LinkedHashMap<>();
        for (RoleMenuPermissionCatalog.MenuItem item : RoleMenuPermissionCatalog.items()) {
            Boolean enabled = submitted.get(item.menuKey());
            sanitized.put(item.menuKey(), Boolean.TRUE.equals(enabled));
        }
        if (RoleMenuPermissionCatalog.ROLE_ADMIN.equals(normalizedRole)) {
            sanitized.put("/SystemAccountPermission", true);
        }

        Set<String> seenKeys = new LinkedHashSet<>();
        for (Map.Entry<String, Boolean> entry : sanitized.entrySet()) {
            if (!seenKeys.add(entry.getKey())) {
                continue;
            }
            RoleMenuPermission permission = new RoleMenuPermission();
            permission.setRoleCode(normalizedRole);
            permission.setMenuKey(entry.getKey());
            permission.setEnabled(Boolean.TRUE.equals(entry.getValue()) ? 1 : 0);
            roleMenuPermissionMapper.insert(permission);
        }
    }
}
