package com.example.springboot.entity;

import java.util.Date;
import java.util.List;

public class AiAgentConfig {
    private Long id;
    private Long accountId;
    private String agentKey;
    private String name;
    private String icon;
    private String description;
    private String gradient;
    private List<String> chips;
    private String systemPrompt;
    private List<String> enabledTools;
    private Integer sortOrder;
    private Boolean isSystemDefault;
    private Date createdAt;
    private Date updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public String getAgentKey() { return agentKey; }
    public void setAgentKey(String agentKey) { this.agentKey = agentKey; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getGradient() { return gradient; }
    public void setGradient(String gradient) { this.gradient = gradient; }

    public List<String> getChips() { return chips; }
    public void setChips(List<String> chips) { this.chips = chips; }

    public String getSystemPrompt() { return systemPrompt; }
    public void setSystemPrompt(String systemPrompt) { this.systemPrompt = systemPrompt; }

    public List<String> getEnabledTools() { return enabledTools; }
    public void setEnabledTools(List<String> enabledTools) { this.enabledTools = enabledTools; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public Boolean getIsSystemDefault() { return isSystemDefault; }
    public void setIsSystemDefault(Boolean isSystemDefault) { this.isSystemDefault = isSystemDefault; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
