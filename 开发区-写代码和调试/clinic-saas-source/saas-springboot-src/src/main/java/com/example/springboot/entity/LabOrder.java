package com.example.springboot.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class LabOrder {
    private Long id;
    private Long factory_id;
    private String factory_name;
    private Long patient_id;
    private String patient_name;
    private Long treatment_id;
    private Long medical_record_operation_id;
    private Long medical_record_id;
    private Long project_id;
    private String project_name;
    private Long operation_id;
    private String operation_name;
    private String tooth_positions;
    private String product_name;
    private String product_spec;
    private BigDecimal unit_price;
    private Integer quantity;
    private BigDecimal total_amount;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date order_date;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date expected_delivery_date;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date actual_delivery_date;
    private String status;
    private String remark;
    private Long created_by;
    private String created_by_name;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date created_at;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updated_at;
}
