package com.example.springboot.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class MaterialPurchase {
    private Long id;
    private String supplier_name;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date purchase_date;
    private BigDecimal total_amount;
    private String payment_method;
    private String invoice_image_url;
    private String remark;
    private Long finance_record_id;
    private String status;
    private Long created_by;
    private String created_by_name;
    private Long voided_by;
    private String voided_by_name;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date voided_at;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date created_at;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updated_at;
    private List<MaterialPurchaseItem> items = new ArrayList<>();
}
