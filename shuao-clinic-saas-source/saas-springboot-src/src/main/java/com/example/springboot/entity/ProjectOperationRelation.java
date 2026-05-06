package com.example.springboot.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class ProjectOperationRelation {
    private Long id;
    private Long project_id;
    private Long operation_id;
    private Integer operation_order;
    private Integer is_required;
    private Double performance_weight;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date created_at;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updated_at;
    private String operation_code;
    private String operation_name;
    private String operation_category;
    private Integer need_lab_processing;
    private Integer default_processing_days;
}
