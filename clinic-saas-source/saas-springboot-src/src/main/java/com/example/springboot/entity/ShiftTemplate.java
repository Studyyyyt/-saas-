package com.example.springboot.entity;

import java.sql.Timestamp;

/**
 * 排班模板实体
 */
public class ShiftTemplate {
    private Long id;
    private String name;
    private String doctor_name;
    private String pattern_json;
    private Timestamp created_at;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDoctor_name() {
        return doctor_name;
    }

    public void setDoctor_name(String doctor_name) {
        this.doctor_name = doctor_name;
    }

    public String getPattern_json() {
        return pattern_json;
    }

    public void setPattern_json(String pattern_json) {
        this.pattern_json = pattern_json;
    }

    public Timestamp getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Timestamp created_at) {
        this.created_at = created_at;
    }
}
