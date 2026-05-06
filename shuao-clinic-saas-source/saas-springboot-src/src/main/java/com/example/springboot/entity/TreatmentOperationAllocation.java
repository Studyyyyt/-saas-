package com.example.springboot.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class TreatmentOperationAllocation {
    private Long id;
    private Long treatment_id;
    private Long medical_record_id;
    private Long medical_record_operation_id;
    private Long patient_id;
    private Long doctor_account_id;
    private String doctor_name;
    private Long project_id;
    private String project_name;
    private Long operation_id;
    private String operation_name;
    private Double performance_weight;
    private Double allocation_ratio;
    private Double allocated_turnover_amount;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date created_at;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updated_at;
}
