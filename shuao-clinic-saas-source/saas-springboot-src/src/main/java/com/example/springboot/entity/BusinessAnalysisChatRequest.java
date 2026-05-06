package com.example.springboot.entity;

import lombok.Data;

@Data
public class BusinessAnalysisChatRequest {
    private Long account_id;
    private String account_name;
    private String session_id;
    private String message;
}
