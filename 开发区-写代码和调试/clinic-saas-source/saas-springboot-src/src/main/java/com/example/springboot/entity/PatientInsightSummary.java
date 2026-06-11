package com.example.springboot.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class PatientInsightSummary {
    private Long patient_id;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date last_visit_date;
    private Integer total_visit_count;
    private BigDecimal total_spent;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date last_treatment_date;
    private Integer visit_count_last_6m;
    private Boolean high_value_flag;
    private Boolean lost_risk_flag;
    private Integer referred_count;
    private BigDecimal referred_revenue;
    private Boolean word_of_mouth_flag;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updated_at;
}
