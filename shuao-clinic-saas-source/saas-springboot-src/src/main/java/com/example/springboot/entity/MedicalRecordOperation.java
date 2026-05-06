package com.example.springboot.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class MedicalRecordOperation {
    private Long id;
    private Long medical_record_id;
    private Long project_id;
    private String project_name;
    private Long operation_id;
    private String operation_name;
    private Long factory_id;
    private String factory_name;
    private String tooth_positions;
    private String remark;
    private Integer lab_order_status;
    private String skip_reason;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lab_order_registered_at;
    private Long created_by;
    private String created_by_name;
    private Long updated_by;
    private String updated_by_name;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date created_at;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updated_at;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date visit_date;
    private Long patient_id;
    private String patient_name;
    private Long doctor_account_id;
    private String doctor_name;
    private String operation_category;
    private Integer need_lab_processing;
    private Integer default_processing_days;
}
