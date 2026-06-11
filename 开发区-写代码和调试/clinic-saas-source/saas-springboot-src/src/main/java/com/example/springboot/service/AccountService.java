package com.example.springboot.service;

import com.example.springboot.entity.Account;
import com.example.springboot.mapper.AccountMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class AccountService {

    @Autowired
    private AccountMapper accountMapper;

    public List<Account> searchAccounts() {
        return accountMapper.searchAccounts();
    }

    public List<Account> findActiveDoctorAccounts() {
        List<Account> accounts = searchAccounts();
        if (accounts == null || accounts.isEmpty()) {
            return List.of();
        }
        return accounts.stream()
                .filter(account -> account != null
                        && "doctor".equals(normalizeRoleCode(account.getRole()))
                        && StringUtils.hasText(account.getName()))
                .peek(account -> account.setName(account.getName().trim()))
                .toList();
    }

    public List<Account> findAdminAccountsWithWechatBinding() {
        List<Account> accounts = searchAccounts();
        if (accounts == null || accounts.isEmpty()) {
            return List.of();
        }
        return accounts.stream()
                .filter(account -> account != null
                        && "admin".equals(normalizeRoleCode(account.getRole()))
                        && StringUtils.hasText(account.getWechat_openid()))
                .peek(account -> {
                    account.setName(StringUtils.hasText(account.getName()) ? account.getName().trim() : "");
                    account.setWechat_openid(account.getWechat_openid().trim());
                })
                .toList();
    }

    public void addAccount(Account account) {
        validateAccountForCreate(account);
        if (accountMapper.countByUsername(account.getUsername().trim()) > 0) {
            throw new IllegalArgumentException("账号名称已存在");
        }
        account.setUsername(account.getUsername().trim());
        account.setName(account.getName().trim());
        account.setPassword(account.getPassword().trim());
        account.setRole(normalizeRoleCode(account.getRole()));
        account.setWechat_openid(normalizeOpenid(account.getWechat_openid()));
        accountMapper.addAccount(account);
    }

    public void updateAccount(Account account) {
        validateAccountForUpdate(account);
        account.setUsername(account.getUsername().trim());
        account.setName(account.getName().trim());
        account.setRole(normalizeRoleCode(account.getRole()));
        account.setWechat_openid(normalizeOpenid(account.getWechat_openid()));
        if (StringUtils.hasText(account.getPassword())) {
            account.setPassword(account.getPassword().trim());
        }
        accountMapper.updateAccount(account);
    }

    public void deleteAccount(int id) {
        accountMapper.deleteAccount(id);
    }

    public List<Account> selectById(Long id) {
        return accountMapper.selectById(id);
    }

    public void deleteAccountBatch(List<Long> ids) {
        accountMapper.deleteAccountBatch(ids);
    }

    public List<Account> selectByName(String name) {
        return accountMapper.selectByName(name);
    }

    public Account bindWechatOpenid(Long id, String openid) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("员工账号不存在");
        }
        List<Account> accounts = accountMapper.selectById(id);
        if (accounts == null || accounts.isEmpty()) {
            throw new IllegalArgumentException("员工账号不存在");
        }
        Account account = accounts.get(0);
        account.setWechat_openid(normalizeOpenid(openid));
        accountMapper.bindWechatOpenid(account);
        return account;
    }

    public Account selectByWechatOpenid(String openid) {
        if (!StringUtils.hasText(openid)) {
            return null;
        }
        return accountMapper.selectByWechatOpenid(openid.trim());
    }

    public void updateAvatar(int id, String avatarUrl) {
        if (id <= 0) {
            throw new IllegalArgumentException("账号ID非法");
        }
        accountMapper.updateAvatar(id, avatarUrl);
    }

    public Account authenticateByNameAndPassword(String name, String password) {
        if (!StringUtils.hasText(name) || !StringUtils.hasText(password)) {
            return null;
        }
        List<Account> accounts = accountMapper.selectByName(name.trim());
        if (accounts == null || accounts.isEmpty()) {
            return null;
        }
        String passwordText = password.trim();
        for (Account account : accounts) {
            if (account != null && StringUtils.hasText(account.getPassword())
                    && passwordText.equals(account.getPassword().trim())) {
                return account;
            }
        }
        return null;
    }

    public Account authenticateByUsernameAndPassword(String username, String password) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            return null;
        }
        List<Account> accounts = searchAccounts();
        if (accounts == null || accounts.isEmpty()) {
            return null;
        }
        String usernameText = username.trim();
        String passwordText = password.trim();
        for (Account account : accounts) {
            if (account != null
                    && StringUtils.hasText(account.getUsername())
                    && StringUtils.hasText(account.getPassword())
                    && usernameText.equals(account.getUsername().trim())
                    && passwordText.equals(account.getPassword().trim())) {
                return account;
            }
        }
        return null;
    }

    public String findDoctorDisplayNameByAccountId(Long accountId) {
        if (accountId == null || accountId <= 0) {
            return null;
        }
        List<Account> accounts = accountMapper.selectById(accountId);
        if (accounts == null || accounts.isEmpty()) {
            return null;
        }
        Account account = accounts.get(0);
        if (account == null || !"doctor".equals(normalizeRoleCode(account.getRole()))) {
            return null;
        }
        return StringUtils.hasText(account.getName()) ? account.getName().trim() : null;
    }

    public Long findDoctorAccountIdByName(String doctorName) {
        if (!StringUtils.hasText(doctorName)) {
            return null;
        }
        String doctorNameText = doctorName.trim();
        List<Account> accounts = searchAccounts();
        if (accounts == null || accounts.isEmpty()) {
            return null;
        }
        for (Account account : accounts) {
            if (account != null
                    && "doctor".equals(normalizeRoleCode(account.getRole()))
                    && StringUtils.hasText(account.getName())
                    && doctorNameText.equals(account.getName().trim())) {
                return (long) account.getId();
            }
        }
        return null;
    }

    private void validateAccountForCreate(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("账号信息不能为空");
        }
        if (!StringUtils.hasText(account.getUsername())) {
            throw new IllegalArgumentException("账号名称必填");
        }
        if (!StringUtils.hasText(account.getName())) {
            throw new IllegalArgumentException("姓名必填");
        }
        if (!StringUtils.hasText(account.getPassword())) {
            throw new IllegalArgumentException("密码必填");
        }
        validateRole(account.getRole());
    }

    private void validateAccountForUpdate(Account account) {
        if (account == null || account.getId() <= 0) {
            throw new IllegalArgumentException("账号ID非法");
        }
        if (!StringUtils.hasText(account.getUsername())) {
            throw new IllegalArgumentException("账号名称必填");
        }
        if (!StringUtils.hasText(account.getName())) {
            throw new IllegalArgumentException("姓名必填");
        }
        validateRole(account.getRole());
    }

    private void validateRole(String role) {
        if (!StringUtils.hasText(role)) {
            throw new IllegalArgumentException("角色必填");
        }
        String normalized = normalizeRoleCode(role);
        if (!("admin".equals(normalized) || "doctor".equals(normalized) || "nurse".equals(normalized))) {
            throw new IllegalArgumentException("角色非法");
        }
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

    private String normalizeOpenid(String openid) {
        if (!StringUtils.hasText(openid)) {
            return null;
        }
        return openid.trim();
    }
}
