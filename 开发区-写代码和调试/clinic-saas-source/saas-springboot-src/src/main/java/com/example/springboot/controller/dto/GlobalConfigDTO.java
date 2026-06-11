package com.example.springboot.controller.dto;

/**
 * AI 全局配置更新请求 DTO
 */
public class GlobalConfigDTO {
    private Boolean globalEnabled;
    private Boolean debugMode;

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
