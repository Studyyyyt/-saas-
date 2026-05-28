package com.example.springboot.entity;

import lombok.Data;

@Data
public class DoctorPerformanceStat {
    private Long doctor_account_id;
    private String doctor_name;
    private Integer project_count;
    private Double turnover_amount;
    private Double received_amount;
    private Double refunded_amount;
    private Double arrears_amount;
}
