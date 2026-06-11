package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.Clinic;
import com.example.springboot.service.ClinicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 诊所管理接口（仅超级管理员）
 */
@RestController
@RequestMapping("/clinics/manage")
public class ClinicManageController {

    @Autowired
    private ClinicService clinicService;

    @GetMapping
    public Result list(@RequestParam(required = false) String name) {
        List<Clinic> list = clinicService.listClinics(name);
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result getById(@PathVariable String id) {
        Clinic clinic = clinicService.getById(id);
        if (clinic == null) {
            return Result.error("404", "诊所不存在");
        }
        return Result.success(clinic);
    }

    @PostMapping
    public Result create(@RequestBody Clinic clinic) {
        try {
            // 从请求体中获取创建者用户ID，默认为1（admin）
            Integer creatorUserId = clinic.getCreatorUserId();
            if (creatorUserId == null) {
                creatorUserId = 1;
            }
            clinicService.createClinic(clinic, creatorUserId);
            return Result.success();
        } catch (IllegalArgumentException e) {
            return Result.error("400", e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable String id, @RequestBody Clinic clinic) {
        clinic.setId(id);
        try {
            clinicService.updateClinic(clinic);
            return Result.success();
        } catch (IllegalArgumentException e) {
            return Result.error("400", e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable String id) {
        try {
            clinicService.deleteClinic(id);
            return Result.success();
        } catch (IllegalArgumentException e) {
            return Result.error("400", e.getMessage());
        }
    }

    @PostMapping("/{id}/toggle")
    public Result toggleStatus(@PathVariable String id, @RequestBody Map<String, Integer> request) {
        Integer status = request.get("status");
        try {
            clinicService.toggleStatus(id, status);
            return Result.success();
        } catch (IllegalArgumentException e) {
            return Result.error("400", e.getMessage());
        }
    }

    @GetMapping("/{id}/stats")
    public Result getStats(@PathVariable String id) {
        Map<String, Object> stats = clinicService.getClinicStats(id);
        return Result.success(stats);
    }
}
