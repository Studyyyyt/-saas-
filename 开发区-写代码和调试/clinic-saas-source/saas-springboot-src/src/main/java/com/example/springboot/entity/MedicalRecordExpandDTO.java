package com.example.springboot.entity;

import java.util.Map;

/**
 * 病历扩写请求 DTO
 */
public class MedicalRecordExpandDTO {
    private Map<String, String> fields;
    private Boolean testMode;

    public Map<String, String> getFields() { return fields; }
    public void setFields(Map<String, String> fields) { this.fields = fields; }

    public Boolean getTestMode() { return testMode; }
    public void setTestMode(Boolean testMode) { this.testMode = testMode; }
}
