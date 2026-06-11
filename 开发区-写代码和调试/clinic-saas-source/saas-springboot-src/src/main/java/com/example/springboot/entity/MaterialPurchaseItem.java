package com.example.springboot.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class MaterialPurchaseItem {
    private Long id;
    private Long purchase_id;
    private Long material_id;
    private String material_name;
    private String material_spec;
    private BigDecimal unit_price;
    private Integer quantity;
    private BigDecimal subtotal;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date created_at;
}
