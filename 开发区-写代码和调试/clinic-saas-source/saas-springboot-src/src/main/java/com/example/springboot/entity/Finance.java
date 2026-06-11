package com.example.springboot.entity;

import lombok.Data;

@Data
public class Finance {
    private int id;
    private Long patient_id;
    private Long treatment_id;
    private Long payment_channel_id;
    private String payment_channel_name;
    private String name;
    private double amount;
    private String date;
    private String type;
    private String biz_type;
    private String remark;
}
