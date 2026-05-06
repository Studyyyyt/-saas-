package com.example.springboot.entity;

public class InsuranceOverview {
    private InsuranceConfig config;
    private Integer patientProfileCount;
    private Integer settlementCount;
    private Integer pendingSettlementCount;
    private Integer failedSettlementCount;

    public InsuranceConfig getConfig() {
        return config;
    }

    public void setConfig(InsuranceConfig config) {
        this.config = config;
    }

    public Integer getPatientProfileCount() {
        return patientProfileCount;
    }

    public void setPatientProfileCount(Integer patientProfileCount) {
        this.patientProfileCount = patientProfileCount;
    }

    public Integer getSettlementCount() {
        return settlementCount;
    }

    public void setSettlementCount(Integer settlementCount) {
        this.settlementCount = settlementCount;
    }

    public Integer getPendingSettlementCount() {
        return pendingSettlementCount;
    }

    public void setPendingSettlementCount(Integer pendingSettlementCount) {
        this.pendingSettlementCount = pendingSettlementCount;
    }

    public Integer getFailedSettlementCount() {
        return failedSettlementCount;
    }

    public void setFailedSettlementCount(Integer failedSettlementCount) {
        this.failedSettlementCount = failedSettlementCount;
    }
}
