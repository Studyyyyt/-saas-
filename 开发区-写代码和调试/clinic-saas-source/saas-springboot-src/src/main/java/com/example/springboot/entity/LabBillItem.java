package com.example.springboot.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class LabBillItem {
    private Long id;
    private Long bill_id;
    private Integer raw_row_number;
    private String product_name;
    private String product_spec;
    private Integer quantity;
    private BigDecimal unit_price;
    private BigDecimal total_amount;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date delivery_date;
    private String patient_name;
    private String match_status;
    private Long matched_lab_order_id;
    private String resolution_status;
    private String resolution_remark;
    private Long resolved_by;
    private String resolved_by_name;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date resolved_at;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date created_at;
}
