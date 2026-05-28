package com.example.springboot.entity;

import lombok.Data;

@Data
public class MaterialPurchaseVoidRequest {
    private Long voided_by;
    private String voided_by_name;
    private String remark;
}
