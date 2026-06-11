package com.example.springboot.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class TreatmentProjectCategory {
    private Long id;
    private String name;
    private Long parent_id;
    private Integer sort_order;
    private String status;
    private Long created_by;
    private String created_by_name;
    private Long updated_by;
    private String updated_by_name;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date created_at;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updated_at;
    private List<TreatmentProjectCategory> children = new ArrayList<>();
}
