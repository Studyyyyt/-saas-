package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.Account;
import com.example.springboot.service.AccountService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

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
            return Result.success("新增成功");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/edit")
    public Result updateAccount(@RequestBody Account account) {
        try {
            accountService.updateAccount(account);
            return Result.success("编辑成功");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
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
}
