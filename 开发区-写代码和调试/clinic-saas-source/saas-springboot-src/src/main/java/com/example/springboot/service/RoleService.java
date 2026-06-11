package com.example.springboot.service;

import com.example.springboot.entity.Role;
import com.example.springboot.mapper.AccountMapper;
import com.example.springboot.mapper.RoleMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 角色管理服务
 */
@Service
public class RoleService {

    private final RoleMapper roleMapper;
    private final AccountMapper accountMapper;

    public RoleService(RoleMapper roleMapper, AccountMapper accountMapper) {
        this.roleMapper = roleMapper;
        this.accountMapper = accountMapper;
    }

    public List<Role> listAll() {
        return roleMapper.selectAll();
    }

    public List<Role> listActive() {
        return roleMapper.selectAllActive();
    }

    public Role getById(Long id) {
        return roleMapper.selectById(id);
    }

    public Role create(Role role) {
        if (role.getCode() == null || role.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("角色码不能为空");
        }
        if (!role.getCode().matches("^[a-z][a-z0-9_]*$")) {
            throw new IllegalArgumentException("角色码格式错误：必须以字母开头，只能包含小写字母、数字和下划线");
        }
        if (role.getName() == null || role.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("角色名称不能为空");
        }
        String code = role.getCode().trim();
        Role existing = roleMapper.selectByCode(code);
        if (existing != null) {
            throw new IllegalArgumentException("角色码已存在：" + code);
        }
        role.setCode(code);
        role.setName(role.getName().trim());
        if (role.getSortOrder() == null) {
            role.setSortOrder(0);
        }
        if (role.getStatus() == null) {
            role.setStatus(1);
        }
        roleMapper.insert(role);
        return role;
    }

    public Role update(Role role) {
        if (role.getId() == null) {
            throw new IllegalArgumentException("角色ID不能为空");
        }
        Role existing = roleMapper.selectById(role.getId());
        if (existing == null) {
            throw new IllegalArgumentException("角色不存在");
        }
        if (role.getName() == null || role.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("角色名称不能为空");
        }
        // 检查 code 是否被修改且与其他角色冲突
        String newCode = role.getCode() != null ? role.getCode().trim() : existing.getCode();
        if (!newCode.equals(existing.getCode())) {
            Role conflict = roleMapper.selectByCode(newCode);
            if (conflict != null && !conflict.getId().equals(role.getId())) {
                throw new IllegalArgumentException("角色码已存在：" + newCode);
            }
        }
        role.setCode(newCode);
        role.setName(role.getName().trim());
        roleMapper.update(role);
        return roleMapper.selectById(role.getId());
    }

    public void delete(Long id) {
        Role existing = roleMapper.selectById(id);
        if (existing == null) {
            throw new IllegalArgumentException("角色不存在");
        }
        // 检查是否有账号使用该角色（同时匹配 code 和 name，因为 users.role 字段可能存储中文或英文）
        int countByCode = accountMapper.countByRole(existing.getCode());
        int countByName = accountMapper.countByRole(existing.getName());
        int count = countByCode + countByName;
        if (count > 0) {
            throw new IllegalArgumentException("该角色下有 " + count + " 个账号正在使用，无法删除。请先调整这些账号的角色。");
        }
        roleMapper.deleteById(id);
    }
}
