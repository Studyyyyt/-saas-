package com.example.springboot.entity;

import lombok.Data;

@Data
public class PatientConsentSignRequest {
    private String signature_name;
    private String signature_data;
    private String signature_remark;
}
