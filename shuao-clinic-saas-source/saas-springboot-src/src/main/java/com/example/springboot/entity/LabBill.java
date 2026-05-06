package com.example.springboot.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class LabBill {
    private Long id;
    private Long factory_id;
    private String factory_name;
    private Long template_id;
    private String bill_month;
    private BigDecimal total_amount;
    private String bill_file_url;
    private String status;
    private Integer matched_count;
    private Integer mismatched_count;
    private Integer only_in_system_count;
    private Integer only_in_bill_count;
    private Long imported_by;
    private String imported_by_name;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date imported_at;
    private Long confirmed_by;
    private String confirmed_by_name;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date confirmed_at;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date created_at;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updated_at;
}
