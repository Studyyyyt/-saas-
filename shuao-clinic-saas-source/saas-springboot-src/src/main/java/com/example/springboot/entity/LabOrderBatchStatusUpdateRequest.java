package com.example.springboot.entity;

import lombok.Data;

import java.util.List;

@Data
public class LabOrderBatchStatusUpdateRequest {
    private List<Long> ids;
    private String status;
    private String actual_delivery_date;
}
