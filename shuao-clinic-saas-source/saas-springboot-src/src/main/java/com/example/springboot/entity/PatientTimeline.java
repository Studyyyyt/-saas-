package com.example.springboot.entity;

import lombok.Data;

import java.util.Date;

@Data
public class PatientTimeline {
    private Long id;
    private Long patient_id;
    private Date event_time;
    private String event_type;
    private String event_title;
    private String event_content;
    private String source_table;
    private Long source_id;
    private Date created_at;
    private Date updated_at;
}
