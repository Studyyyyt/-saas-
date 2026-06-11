package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.Account;
import com.example.springboot.service.AccountService;
import com.example.springboot.service.ClinicService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private ClinicService clinicService;

    @GetMapping("/search")
    public Result searchAccounts(@RequestParam int page, @RequestParam int size) {
        PageHelper.startPage(page, size);
        List<Account> accounts = accountService.searchAccounts();
        PageInfo<Account> pageInfo = new PageInfo<>(accounts);
        return Result.success(pageInfo);
    }

    @GetMapping("/doctors/active")
    public Result activeDoctors() {
        return Result.success(accountService.findActiveDoctorAccounts());
    }

    @GetMapping("/selectByid")
    public Result selectById(@RequestParam Long id,@RequestParam int page, @RequestParam int size) {
        PageHelper.startPage(page, size);
        List<Account> accounts = accountService.selectById(id);
        PageInfo<Account> pageInfo = new PageInfo<>(accounts);
        return Result.success(pageInfo);
    }

    @GetMapping("/selectByname")
    public Result selectByName(@RequestParam String name,@RequestParam int page, @RequestParam int size) {
        PageHelper.startPage(page, size);
        List<Account> accounts = accountService.selectByName(name);
        PageInfo<Account> pageInfo = new PageInfo<>(accounts);
        return Result.success(pageInfo);
    }

    @PostMapping("/add")
    public Result addAccount(@RequestBody Account account) {
        try {
            accountService.addAccount(account);
            // 处理诊所分配
            saveClinicRoles(account.getId(), account.getClinicRoles());
            return Result.success("新增成功");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/edit")
    public Result updateAccount(@RequestBody Account account) {
        try {
            accountService.updateAccount(account);
            // 处理诊所分配
            saveClinicRoles(account.getId(), account.getClinicRoles());
            return Result.success("编辑成功");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/avatar")
    public Result updateAvatar(@PathVariable int id, @RequestBody Map<String, String> request) {
        String avatarUrl = request != null ? request.get("avatar") : null;
        if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
            return Result.error("400", "头像地址不能为空");
        }
        try {
            accountService.updateAvatar(id, avatarUrl.trim());
            return Result.success("头像更新成功");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/{id}/clinics")
    public Result getAccountClinics(@PathVariable int id) {
        return Result.success(clinicService.getUserClinics(id));
    }

    @DeleteMapping("/delete/{id}")
    public Result deleteAccount(@PathVariable int id) {
        accountService.deleteAccount(id);
        return Result.success("删除成功");
    }

    @DeleteMapping("/deleteBatch")
    public Result deleteAccountBatch(@RequestBody List<Long> ids) {
        try {
            accountService.deleteAccountBatch(ids);
            return Result.success("批量删除成功");
        } catch (Exception e) {
            return Result.error("批量删除失败：" + e.getMessage());
        }
    }

    private void saveClinicRoles(int userId, List<Map<String, Object>> clinicRoles) {
        if (clinicRoles == null || clinicRoles.isEmpty()) {
            return;
        }
        // 全量替换：先删除该用户的所有诊所分配，再重新插入
        clinicService.removeAllClinicsFromUser(userId);
        for (Map<String, Object> item : clinicRoles) {
            String clinicId = item.get("clinicId") != null ? String.valueOf(item.get("clinicId")) : null;
            String role = item.get("role") != null ? String.valueOf(item.get("role")) : null;
            Integer isDefault = item.get("isDefault") != null ? Integer.valueOf(String.valueOf(item.get("isDefault"))) : 0;
            if (clinicId == null || clinicId.isEmpty() || role == null || role.isEmpty()) {
                continue;
            }
            clinicService.assignClinicToUser(userId, clinicId, role, isDefault);
        }
    }
}
