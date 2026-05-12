package com.example.springboot.entity;

import java.util.List;
import java.util.Map;

/**
 * 带治疗场景的病历扩写请求 DTO
 */
public class TreatmentSceneExpandRequest {
    private Map<String, String> fields;
    private Long sceneId;
    private List<String> operations;
    private Boolean testMode;
    /** 用于预览未保存的系统提示词 */
    private String systemPrompt;
    /** 用于预览未保存的 Few-shot 示例 [{"input":"...","output":"..."}] */
    private List<Map<String, String>> fewShots;
    /** 用于预览未保存的空字段策略 */
    private String emptyFieldStrategy;

    public Map<String, String> getFields() { return fields; }
    public void setFields(Map<String, String> fields) { this.fields = fields; }

    public Long getSceneId() { return sceneId; }
    public void setSceneId(Long sceneId) { this.sceneId = sceneId; }

    public List<String> getOperations() { return operations; }
    public void setOperations(List<String> operations) { this.operations = operations; }

    public Boolean getTestMode() { return testMode; }
    public void setTestMode(Boolean testMode) { this.testMode = testMode; }

    public String getSystemPrompt() { return systemPrompt; }
    public void setSystemPrompt(String systemPrompt) { this.systemPrompt = systemPrompt; }

    public List<Map<String, String>> getFewShots() { return fewShots; }
    public void setFewShots(List<Map<String, String>> fewShots) { this.fewShots = fewShots; }

    public String getEmptyFieldStrategy() { return emptyFieldStrategy; }
    public void setEmptyFieldStrategy(String emptyFieldStrategy) { this.emptyFieldStrategy = emptyFieldStrategy; }
}
