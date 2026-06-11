package com.example.springboot.entity;

import lombok.Data;

@Data
public class LabBillResolutionRequest {
    private String resolution_status;
    private String resolution_remark;
    private Long resolved_by;
    private String resolved_by_name;
}
