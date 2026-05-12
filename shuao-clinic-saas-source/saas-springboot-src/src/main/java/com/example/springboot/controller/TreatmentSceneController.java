package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.TreatmentScene;
import com.example.springboot.entity.TreatmentSceneStep;
import com.example.springboot.service.TreatmentSceneService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 治疗场景（病种）配置控制器
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:7070")
public class TreatmentSceneController {

    private final TreatmentSceneService sceneService;

    public TreatmentSceneController(TreatmentSceneService sceneService) {
        this.sceneService = sceneService;
    }

    @GetMapping("/treatment-scenes")
    public Result list() {
        return Result.success(sceneService.listAll());
    }

    @GetMapping("/treatment-scenes/enabled")
    public Result listEnabled() {
        return Result.success(sceneService.listEnabled());
    }

    @GetMapping("/treatment-scenes/{id}")
    public Result detail(@PathVariable Long id) {
        Map<String, Object> detail = sceneService.getSceneDetail(id);
        if (detail == null) {
            return Result.error("场景不存在");
        }
        return Result.success(detail);
    }

    @PostMapping("/treatment-scenes")
    public Result save(@RequestBody Map<String, Object> payload) {
        try {
            TreatmentScene scene = new TreatmentScene();
            Object id = payload.get("id");
            if (id != null) {
                scene.setId(Long.valueOf(String.valueOf(id)));
            }
            scene.setName(String.valueOf(payload.getOrDefault("name", "")));
            scene.setCategory(String.valueOf(payload.getOrDefault("category", "其他")));
            Object level = payload.get("level");
            scene.setLevel(level != null ? Integer.valueOf(String.valueOf(level)) : 1);
            Object enabled = payload.get("enabled");
            scene.setEnabled(enabled != null ? Boolean.valueOf(String.valueOf(enabled)) : true);
            Object sortOrder = payload.get("sortOrder");
            scene.setSortOrder(sortOrder != null ? Integer.valueOf(String.valueOf(sortOrder)) : 0);

            List<Map<String, Object>> stepList = (List<Map<String, Object>>) payload.get("steps");
            java.util.List<TreatmentSceneStep> steps = new java.util.ArrayList<>();
            if (stepList != null) {
                for (Map<String, Object> item : stepList) {
                    TreatmentSceneStep step = new TreatmentSceneStep();
                    Object sid = item.get("id");
                    if (sid != null) {
                        step.setId(Long.valueOf(String.valueOf(sid)));
                    }
                    step.setName(String.valueOf(item.getOrDefault("name", "")));
                    Object sSort = item.get("sortOrder");
                    step.setSortOrder(sSort != null ? Integer.valueOf(String.valueOf(sSort)) : 0);
                    step.setForbiddenKeywords(String.valueOf(item.getOrDefault("forbiddenKeywords", "")));
                    step.setRequiredKeywords(String.valueOf(item.getOrDefault("requiredKeywords", "")));
                    Object sEnabled = item.get("enabled");
                    step.setEnabled(sEnabled != null ? Boolean.valueOf(String.valueOf(sEnabled)) : true);
                    steps.add(step);
                }
            }

            sceneService.saveScene(scene, steps);
            return Result.success(scene);
        } catch (Exception e) {
            return Result.error("保存失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/treatment-scenes/{id}")
    public Result delete(@PathVariable Long id) {
        try {
            sceneService.deleteScene(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    @GetMapping("/treatment-scenes/{id}/steps")
    public Result steps(@PathVariable Long id) {
        return Result.success(sceneService.getStepsBySceneId(id));
    }
}
