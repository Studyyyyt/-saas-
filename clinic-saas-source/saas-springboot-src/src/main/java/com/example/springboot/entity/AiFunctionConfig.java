package com.example.springboot.entity;

import java.util.Date;

/**
 * AI 功能配置实体
 */
public class AiFunctionConfig {
    private Long id;
    private String functionKey;
    private String functionName;
    private String pagePath;
    private String icon;
    private Boolean isEnabled;
    private String modelName;
    private Long promptTemplateId;
    private String extraConfig;
    private Integer sortOrder;
    private Date createTime;
    private Date updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFunctionKey() { return functionKey; }
    public void setFunctionKey(String functionKey) { this.functionKey = functionKey; }

    public String getFunctionName() { return functionName; }
    public void setFunctionName(String functionName) { this.functionName = functionName; }

    public String getPagePath() { return pagePath; }
    public void setPagePath(String pagePath) { this.pagePath = pagePath; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public Boolean getIsEnabled() { return isEnabled; }
    public void setIsEnabled(Boolean isEnabled) { this.isEnabled = isEnabled; }

    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }

    public Long getPromptTemplateId() { return promptTemplateId; }
    public void setPromptTemplateId(Long promptTemplateId) { this.promptTemplateId = promptTemplateId; }

    public String getExtraConfig() { return extraConfig; }
    public void setExtraConfig(String extraConfig) { this.extraConfig = extraConfig; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }

    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
}
