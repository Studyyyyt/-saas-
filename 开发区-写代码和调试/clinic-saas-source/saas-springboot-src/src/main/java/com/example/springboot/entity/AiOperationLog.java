package com.example.springboot.entity;

import java.util.Date;

/**
 * AI 操作日志实体
 */
public class AiOperationLog {
    private Long id;
    private String functionKey;
    private Long accountId;
    private String inputSnapshot;
    private String aiOutput;
    private Boolean isAdopted;
    private Integer tokenUsed;
    private String errorMsg;
    private Date createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFunctionKey() { return functionKey; }
    public void setFunctionKey(String functionKey) { this.functionKey = functionKey; }

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public String getInputSnapshot() { return inputSnapshot; }
    public void setInputSnapshot(String inputSnapshot) { this.inputSnapshot = inputSnapshot; }

    public String getAiOutput() { return aiOutput; }
    public void setAiOutput(String aiOutput) { this.aiOutput = aiOutput; }

    public Boolean getIsAdopted() { return isAdopted; }
    public void setIsAdopted(Boolean isAdopted) { this.isAdopted = isAdopted; }

    public Integer getTokenUsed() { return tokenUsed; }
    public void setTokenUsed(Integer tokenUsed) { this.tokenUsed = tokenUsed; }

    public String getErrorMsg() { return errorMsg; }
    public void setErrorMsg(String errorMsg) { this.errorMsg = errorMsg; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
