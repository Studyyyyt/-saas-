package com.example.springboot.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class PatientReferralRecord {
    private Long id;
    private Long patient_id;
    private Long consultation_record_id;
    private String referrer_type;
    private Long referrer_patient_id;
    private String referrer_patient_name;
    private String external_referrer_type;
    private String external_referrer_name;
    private String external_referrer_contact;
    private String remark;
    private Long created_by;
    private String created_by_name;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date created_at;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updated_at;
}
