package com.example.springboot.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class TreatmentOperation {
    private Long id;
    private String operation_code;
    private String operation_name;
    private String operation_category;
    private Integer need_lab_processing;
    private Integer default_processing_days;
    private String status;
    private Integer sort_order;
    private String remark;
    private Long created_by;
    private String created_by_name;
    private Long updated_by;
    private String updated_by_name;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date created_at;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updated_at;
}
