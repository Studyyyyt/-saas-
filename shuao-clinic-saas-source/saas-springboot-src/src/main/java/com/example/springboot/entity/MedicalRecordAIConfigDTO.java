package com.example.springboot.entity;

import java.util.List;
import java.util.Map;

/**
 * 病历扩写配置保存请求 DTO
 */
public class MedicalRecordAIConfigDTO {
    private Map<String, Object> config;
    private List<MedicalRecordAIField> fields;
    private List<FewShotItem> fewShots;

    public Map<String, Object> getConfig() { return config; }
    public void setConfig(Map<String, Object> config) { this.config = config; }

    public List<MedicalRecordAIField> getFields() { return fields; }
    public void setFields(List<MedicalRecordAIField> fields) { this.fields = fields; }

    public List<FewShotItem> getFewShots() { return fewShots; }
    public void setFewShots(List<FewShotItem> fewShots) { this.fewShots = fewShots; }

    public static class FewShotItem {
        private Long id;
        private String input;
        private String output;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getInput() { return input; }
        public void setInput(String input) { this.input = input; }

        public String getOutput() { return output; }
        public void setOutput(String output) { this.output = output; }
    }
}
