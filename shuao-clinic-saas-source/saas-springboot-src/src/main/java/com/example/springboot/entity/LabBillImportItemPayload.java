package com.example.springboot.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LabBillImportItemPayload {
    private Integer raw_row_number;
    private String product_name;
    private String product_spec;
    private Integer quantity;
    private BigDecimal unit_price;
    private BigDecimal total_amount;
    private String delivery_date;
    private String patient_name;
}
