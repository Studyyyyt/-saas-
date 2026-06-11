package com.example.springboot.controller.dto;

/**
 * AI 功能状态更新请求 DTO
 */
public class FunctionStatusDTO {
    private Boolean enabled;

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
