package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.config.ClinicContext;
import com.example.springboot.service.ClinicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 诊所相关接口（面向普通用户）
 * 包括：获取我的诊所列表、切换当前诊所
 */
@RestController
@RequestMapping("/clinics")
public class ClinicController {

    @Autowired
    private ClinicService clinicService;

    /**
     * 获取当前用户关联的诊所列表
     */
    @GetMapping("/mine")
    public Result getMyClinics(@RequestParam Integer userId) {
        if (userId == null || userId <= 0) {
            return Result.error("400", "用户ID非法");
        }
        List<Map<String, Object>> clinics = clinicService.getUserClinics(userId);
        return Result.success(clinics);
    }

    /**
     * 切换当前诊所
     */
    @PostMapping("/switch")
    public Result switchClinic(@RequestBody Map<String, Object> request) {
        Integer userId = request.get("userId") instanceof Number
                ? ((Number) request.get("userId")).intValue() : null;
        String clinicId = request.get("clinicId") instanceof String
                ? (String) request.get("clinicId") : null;

        if (userId == null || userId <= 0 || clinicId == null || clinicId.isEmpty()) {
            return Result.error("400", "参数非法");
        }

        // 验证用户是否有该诊所的权限
        List<Map<String, Object>> userClinics = clinicService.getUserClinics(userId);
        boolean hasPermission = userClinics.stream()
                .anyMatch(c -> clinicId.equals(c.get("clinicId")));
        if (!hasPermission) {
            return Result.error("403", "无权访问该诊所");
        }

        // 设置为默认诊所
        clinicService.setDefaultClinic(userId, clinicId);

        return Result.success(Map.of("clinicId", clinicId));
    }

    /**
     * 获取当前诊所上下文（调试用）
     */
    @GetMapping("/current")
    public Result getCurrentClinic() {
        String clinicId = ClinicContext.get();
        return Result.success(Map.of("clinicId", clinicId));
    }
}
