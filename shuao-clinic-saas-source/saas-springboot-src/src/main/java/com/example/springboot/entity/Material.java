package com.example.springboot.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class Material {
    private Long id;
    private String name;
    private String spec;
    private String brand;
    private Long category_id;
    private String category_name;
    private String unit;
    private Integer min_stock_alert;
    private Integer current_stock;
    private String status;
    private String remark;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date created_at;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updated_at;
    private Integer alert_gap;
}
