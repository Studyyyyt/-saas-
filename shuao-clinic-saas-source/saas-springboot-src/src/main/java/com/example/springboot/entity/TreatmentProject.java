package com.example.springboot.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class TreatmentProject {
    private Long id;
    private Long legacy_treatment_catalog_id;
    private String project_code;
    private String project_name;
    private Long category_id;
    private String category_name;
    private String category_path;
    private BigDecimal default_price;
    private Integer estimated_visit_count;
    private Integer estimated_cycle_days;
    private String status;
    private Integer sort_order;
    private String remark;
    private Long created_by;
    private String created_by_name;
    private Long updated_by;
    private String updated_by_name;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date created_at;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updated_at;
    private List<ProjectOperationRelation> operation_relations = new ArrayList<>();
}
