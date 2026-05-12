package com.example.springboot.service;

import com.example.springboot.entity.TreatmentScene;
import com.example.springboot.entity.TreatmentSceneStep;
import com.example.springboot.mapper.TreatmentSceneMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 治疗场景服务
 */
@Service
public class TreatmentSceneService {

    private final TreatmentSceneMapper sceneMapper;

    public TreatmentSceneService(TreatmentSceneMapper sceneMapper) {
        this.sceneMapper = sceneMapper;
    }

    public List<TreatmentScene> listAll() {
        return sceneMapper.selectAll();
    }

    public List<TreatmentScene> listEnabled() {
        return sceneMapper.selectAllEnabled();
    }

    public Map<String, Object> getSceneDetail(Long id) {
        Map<String, Object> result = new HashMap<>();
        TreatmentScene scene = sceneMapper.selectById(id);
        if (scene == null) {
            return null;
        }
        result.put("scene", scene);
        result.put("steps", sceneMapper.selectAllStepsBySceneId(id));
        return result;
    }

    @Transactional
    public void saveScene(TreatmentScene scene, List<TreatmentSceneStep> steps) {
        if (scene.getId() == null) {
            sceneMapper.insert(scene);
        } else {
            sceneMapper.update(scene);
        }
        Long sceneId = scene.getId();

        if (steps != null) {
            // 简单策略：先删除该场景下所有步骤，再重新插入
            sceneMapper.deleteStepsBySceneId(sceneId);
            for (TreatmentSceneStep step : steps) {
                step.setSceneId(sceneId);
                if (step.getEnabled() == null) {
                    step.setEnabled(true);
                }
                sceneMapper.insertStep(step);
            }
        }
    }

    @Transactional
    public void deleteScene(Long id) {
        sceneMapper.deleteStepsBySceneId(id);
        sceneMapper.deleteById(id);
    }

    public List<TreatmentSceneStep> getStepsBySceneId(Long sceneId) {
        return sceneMapper.selectStepsBySceneId(sceneId);
    }
}
