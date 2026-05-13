package com.example.springboot.controller.vo;

/**
 * AI 总览统计响应 VO
 */
public class OverviewVO {
    private Integer todayCalls;
    private Integer todayTokens;
    private String todayTokensFormatted;
    private Integer activeFunctions;
    private Integer totalFunctions;
    private Double errorRate;
    private Boolean globalEnabled;
    private Boolean debugMode;

    public Integer getTodayCalls() {
        return todayCalls;
    }

    public void setTodayCalls(Integer todayCalls) {
        this.todayCalls = todayCalls;
    }

    public Integer getTodayTokens() {
        return todayTokens;
    }

    public void setTodayTokens(Integer todayTokens) {
        this.todayTokens = todayTokens;
    }

    public String getTodayTokensFormatted() {
        return todayTokensFormatted;
    }

    public void setTodayTokensFormatted(String todayTokensFormatted) {
        this.todayTokensFormatted = todayTokensFormatted;
    }

    public Integer getActiveFunctions() {
        return activeFunctions;
    }

    public void setActiveFunctions(Integer activeFunctions) {
        this.activeFunctions = activeFunctions;
    }

    public Integer getTotalFunctions() {
        return totalFunctions;
    }

    public void setTotalFunctions(Integer totalFunctions) {
        this.totalFunctions = totalFunctions;
    }

    public Double getErrorRate() {
        return errorRate;
    }

    public void setErrorRate(Double errorRate) {
        this.errorRate = errorRate;
    }

    public Boolean getGlobalEnabled() {
        return globalEnabled;
    }

    public void setGlobalEnabled(Boolean globalEnabled) {
        this.globalEnabled = globalEnabled;
    }

    public Boolean getDebugMode() {
        return debugMode;
    }

    public void setDebugMode(Boolean debugMode) {
        this.debugMode = debugMode;
    }
}
