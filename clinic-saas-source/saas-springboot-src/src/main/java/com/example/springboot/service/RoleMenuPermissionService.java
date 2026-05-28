package com.example.springboot.service;

import com.example.springboot.entity.RoleMenuPermission;
import com.example.springboot.entity.RoleMenuPermissionSaveRequest;
import com.example.springboot.mapper.RoleMenuPermissionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoleMenuPermissionService {

    private final RoleMenuPermissionMapper roleMenuPermissionMapper;

    public RoleMenuPermissionService(RoleMenuPermissionMapper roleMenuPermissionMapper) {
        this.roleMenuPermissionMapper = roleMenuPermissionMapper;
    }

    public Map<String, Object> buildPermissionOverview() {
        Map<String, Map<String, Boolean>> rolePermissions = new LinkedHashMap<>();
        for (String roleCode : RoleMenuPermissionCatalog.ROLE_CODES) {
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
        result.put("roles", RoleMenuPermissionCatalog.ROLE_CODES);
        result.put("catalog", catalog);
        result.put("role_permissions", rolePermissions);
        return result;
    }

    public Map<String, Boolean> getPermissionsByRole(String roleCode) {
        String normalizedRole = RoleMenuPermissionCatalog.normalizeRole(roleCode);
        Map<String, Boolean> merged = new LinkedHashMap<>(RoleMenuPermissionCatalog.buildDefaultRolePermissions(normalizedRole));
        List<RoleMenuPermission> rows = roleMenuPermissionMapper.selectByRoleCode(normalizedRole);
        for (RoleMenuPermission row : rows) {
            if (row == null || !RoleMenuPermissionCatalog.containsMenuKey(row.getMenu_key())) {
                continue;
            }
            merged.put(row.getMenu_key(), row.getEnabled() != null && row.getEnabled() == 1);
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
        for (String roleCode : RoleMenuPermissionCatalog.ROLE_CODES) {
            Map<String, Boolean> submitted = request.getRole_permissions().get(roleCode);
            if (submitted == null) {
                continue;
            }
            replaceRolePermissions(roleCode, submitted);
        }
    }

    private void replaceRolePermissions(String roleCode, Map<String, Boolean> submitted) {
        String normalizedRole = RoleMenuPermissionCatalog.normalizeRole(roleCode);
        if (!RoleMenuPermissionCatalog.ROLE_CODES.contains(normalizedRole)) {
            throw new IllegalArgumentException("角色非法：" + roleCode);
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
            permission.setRole_code(normalizedRole);
            permission.setMenu_key(entry.getKey());
            permission.setEnabled(Boolean.TRUE.equals(entry.getValue()) ? 1 : 0);
            roleMenuPermissionMapper.insert(permission);
        }
    }
}
