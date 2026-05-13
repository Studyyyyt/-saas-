package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.MedicalRecordAIConfigDTO;
import com.example.springboot.entity.MedicalRecordExpandDTO;
import com.example.springboot.entity.TreatmentSceneExpandRequest;
import com.example.springboot.service.MedicalRecordAIService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 病历 AI 扩写配置与调用控制器
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:7070")
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

    @PostMapping("/ai/medical-record/expand")
    public Result expand(@RequestBody TreatmentSceneExpandRequest dto) {
        try {
            Map<String, String> result = medicalRecordAIService.expand(dto);
            return Result.success(result);
        } catch (IllegalStateException e) {
            return Result.error(e.getMessage());
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("AI 扩写失败：" + e.getMessage());
        }
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
