package com.example.springboot.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class LabBillUnmatchedOrder {
    private Long id;
    private Long bill_id;
    private Long lab_order_id;
    private String resolution_status;
    private String resolution_remark;
    private Long resolved_by;
    private String resolved_by_name;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date resolved_at;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date created_at;
}
