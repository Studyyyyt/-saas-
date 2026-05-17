package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.MedicalRecordAIConfigDTO;
import com.example.springboot.entity.TreatmentSceneExpandRequest;
import com.example.springboot.service.MedicalRecordAIService;
import org.springframework.web.bind.annotation.*;

/**
 * 病历 AI 扩写配置与调用控制器
 */
@RestController
@RequestMapping("/api")
public class MedicalRecordAIController {

    private final MedicalRecordAIService medicalRecordAIService;

    public MedicalRecordAIController(MedicalRecordAIService medicalRecordAIService) {
        this.medicalRecordAIService = medicalRecordAIService;
    }

    @GetMapping("/ai-config/medical-record")
    public Result getConfig() {
        return Result.success(medicalRecordAIService.getFullConfig());
    }

    @PutMapping("/ai-config/medical-record")
    public Result saveConfig(@RequestBody MedicalRecordAIConfigDTO dto) {
        medicalRecordAIService.saveConfig(dto);
        return Result.success("保存成功");
    }

    /**
     * 病历 AI 扩写旧接口（已废弃）
     * 前端已统一走 /api/ai/proxy/medical-expand 统一代理
     * 返回 410 Gone，提示调用方迁移到新接口
     */
    @Deprecated
    @PostMapping("/ai/medical-record/expand")
    @ResponseStatus(org.springframework.http.HttpStatus.GONE)
    public Result expand() {
        return Result.error("410", "该接口已废弃，请使用 POST /api/ai/proxy/medical-expand");
    }

    @PostMapping("/ai-config/medical-record/preview")
    public Result previewPrompt(@RequestBody TreatmentSceneExpandRequest dto) {
        try {
            String prompt = medicalRecordAIService.previewPrompt(dto);
            return Result.success(prompt);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("Prompt 预览失败：" + e.getMessage());
        }
    }
}
