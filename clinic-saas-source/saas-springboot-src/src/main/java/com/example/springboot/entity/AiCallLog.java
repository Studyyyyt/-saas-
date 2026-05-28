package com.example.springboot.entity;

import java.util.Date;

/**
 * AI 调用日志实体
 * 记录每次 AI Agent 调用的请求、响应与性能数据
 */
public class AiCallLog {
    private Long id;
    /** 调用者用户ID */
    private Long accountId;
    /** 调用者姓名 */
    private String accountName;
    /** 使用的 AgentKey */
    private String agentKey;
    /** Agent 名称 */
    private String agentName;
    /** 会话ID */
    private String sessionId;
    /** 用户发送的消息内容（前500字） */
    private String requestMessage;
    /** 响应状态：success / error / timeout */
    private String responseStatus;
    /** AI 回复内容摘要（前500字） */
    private String responseContent;
    /** 错误信息 */
    private String errorMsg;
    /** 调用耗时（毫秒） */
    private Integer durationMs;
    /** 调用来源：web / page / api */
    private String source;
    private Date createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }

    public String getAgentKey() { return agentKey; }
    public void setAgentKey(String agentKey) { this.agentKey = agentKey; }

    public String getAgentName() { return agentName; }
    public void setAgentName(String agentName) { this.agentName = agentName; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getRequestMessage() { return requestMessage; }
    public void setRequestMessage(String requestMessage) { this.requestMessage = requestMessage; }

    public String getResponseStatus() { return responseStatus; }
    public void setResponseStatus(String responseStatus) { this.responseStatus = responseStatus; }

    public String getResponseContent() { return responseContent; }
    public void setResponseContent(String responseContent) { this.responseContent = responseContent; }

    public String getErrorMsg() { return errorMsg; }
    public void setErrorMsg(String errorMsg) { this.errorMsg = errorMsg; }

    public Integer getDurationMs() { return durationMs; }
    public void setDurationMs(Integer durationMs) { this.durationMs = durationMs; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
