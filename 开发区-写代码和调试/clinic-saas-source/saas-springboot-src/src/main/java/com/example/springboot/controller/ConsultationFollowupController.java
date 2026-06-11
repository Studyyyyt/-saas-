package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.ConsultationFollowup;
import com.example.springboot.service.ConsultationFollowupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/consultations/followups")
public class ConsultationFollowupController {

    @Autowired
    private ConsultationFollowupService consultationFollowupService;

    // 权限：护士/医生/老板管理员
    @GetMapping("/list")
    public Result list(@RequestParam Long consultationId) {
        List<ConsultationFollowup> list = consultationFollowupService.listByConsultationId(consultationId);
        return Result.success(list);
    }

    // 权限：护士/老板管理员
    @PostMapping("/add")
    public Result add(@RequestBody ConsultationFollowup followup) {
        try {
            ConsultationFollowup created = consultationFollowupService.add(followup);
            return Result.success(created);
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    // 权限：录入人本人/老板管理员（简化实现，不校验权限）
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        try {
            consultationFollowupService.deleteById(id);
            return Result.success();
        } catch (Exception exception) {
            return Result.error("删除失败：" + exception.getMessage());
        }
    }
}
