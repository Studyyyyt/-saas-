package com.example.springboot.entity;

import lombok.Data;

@Data
public class BusinessAnalysisChatRequest {
    private Long account_id;
    private String account_name;
    private String session_id;
    private String message;
    /** 调用该接口的前端功能标识，如 home-assistant、patient-insight、business-analysis */
    private String functionKey;
}
