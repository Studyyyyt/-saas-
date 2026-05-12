package com.example.springboot.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 病历扩写字段规则实体
 */
public class MedicalRecordAIField {
    private Long id;
    private String fieldKey;
    private String fieldName;
    @JsonProperty("enabled")
    private Boolean isEnabled;
    private Integer maxLength;
    @JsonProperty("required")
    private Boolean isRequired;
    private String validationRule;
    private String validationHint;
    private String defaultValue;
    private Integer sortOrder;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFieldKey() { return fieldKey; }
    public void setFieldKey(String fieldKey) { this.fieldKey = fieldKey; }

    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }

    public Boolean getIsEnabled() { return isEnabled; }
    public void setIsEnabled(Boolean isEnabled) { this.isEnabled = isEnabled; }
    @JsonProperty("enabled")
    public void setEnabled(Boolean enabled) { this.isEnabled = enabled; }

    public Integer getMaxLength() { return maxLength; }
    public void setMaxLength(Integer maxLength) { this.maxLength = maxLength; }

    public Boolean getIsRequired() { return isRequired; }
    public void setIsRequired(Boolean isRequired) { this.isRequired = isRequired; }
    @JsonProperty("required")
    public void setRequired(Boolean required) { this.isRequired = required; }

    public String getValidationRule() { return validationRule; }
    public void setValidationRule(String validationRule) { this.validationRule = validationRule; }

    public String getValidationHint() { return validationHint; }
    public void setValidationHint(String validationHint) { this.validationHint = validationHint; }

    public String getDefaultValue() { return defaultValue; }
    public void setDefaultValue(String defaultValue) { this.defaultValue = defaultValue; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
