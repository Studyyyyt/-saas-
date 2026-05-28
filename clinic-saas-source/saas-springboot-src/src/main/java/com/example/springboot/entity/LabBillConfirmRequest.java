package com.example.springboot.entity;

import lombok.Data;

@Data
public class LabBillConfirmRequest {
    private Long confirmed_by;
    private String confirmed_by_name;
}
