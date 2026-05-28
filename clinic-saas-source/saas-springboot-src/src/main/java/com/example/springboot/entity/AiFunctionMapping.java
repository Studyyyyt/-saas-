package com.example.springboot.entity;

import java.util.Date;

/**
 * AI 系统功能与 Agent 绑定映射实体
 * 用于将系统功能（如病历扩写）动态绑定到任意 AgentKey，并控制页面入口显示
 */
public class AiFunctionMapping {
    private Long id;
    /** 所属用户ID，NULL表示系统默认 */
    private Long accountId;
    /** 系统功能编码，如 medical-record-expand */
    private String functionCode;
    /** 功能名称，如 病历扩写 */
    private String functionName;
    /** 绑定的 AgentKey，NULL表示未绑定 */
    private String agentKey;
    /** 是否在所在页面显示入口 0-隐藏 1-显示 */
    private Boolean isVisibleOnPage;
    /** 是否在首页AI下拉框显示 0-隐藏 1-显示 */
    private Boolean isVisibleOnHome;
    /** 排序号 */
    private Integer sortOrder;
    private Date createdAt;
    private Date updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public String getFunctionCode() { return functionCode; }
    public void setFunctionCode(String functionCode) { this.functionCode = functionCode; }

    public String getFunctionName() { return functionName; }
    public void setFunctionName(String functionName) { this.functionName = functionName; }

    public String getAgentKey() { return agentKey; }
    public void setAgentKey(String agentKey) { this.agentKey = agentKey; }

    public Boolean getIsVisibleOnPage() { return isVisibleOnPage; }
    public void setIsVisibleOnPage(Boolean isVisibleOnPage) { this.isVisibleOnPage = isVisibleOnPage; }

    public Boolean getIsVisibleOnHome() { return isVisibleOnHome; }
    public void setIsVisibleOnHome(Boolean isVisibleOnHome) { this.isVisibleOnHome = isVisibleOnHome; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
