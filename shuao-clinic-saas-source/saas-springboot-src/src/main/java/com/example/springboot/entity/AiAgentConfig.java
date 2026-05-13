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
    /** 外部工作流端点地址 */
    private String endpointUrl;
    /** 认证类型：bearer / basic / api_key / 自定义header名 */
    private String authType;
    /** 认证令牌 */
    private String authToken;
    /** 请求体模板，支持 {{变量}} 替换 */
    private String requestTemplate;
    /** 响应类型：sse（流式）/ json（一次性） */
    private String responseType;
    /** 超时秒数，默认60 */
    private Integer timeoutSeconds;
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

    public String getEndpointUrl() { return endpointUrl; }
    public void setEndpointUrl(String endpointUrl) { this.endpointUrl = endpointUrl; }

    public String getAuthType() { return authType; }
    public void setAuthType(String authType) { this.authType = authType; }

    public String getAuthToken() { return authToken; }
    public void setAuthToken(String authToken) { this.authToken = authToken; }

    public String getRequestTemplate() { return requestTemplate; }
    public void setRequestTemplate(String requestTemplate) { this.requestTemplate = requestTemplate; }

    public String getResponseType() { return responseType; }
    public void setResponseType(String responseType) { this.responseType = responseType; }

    public Integer getTimeoutSeconds() { return timeoutSeconds; }
    public void setTimeoutSeconds(Integer timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public Boolean getIsSystemDefault() { return isSystemDefault; }
    public void setIsSystemDefault(Boolean isSystemDefault) { this.isSystemDefault = isSystemDefault; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
