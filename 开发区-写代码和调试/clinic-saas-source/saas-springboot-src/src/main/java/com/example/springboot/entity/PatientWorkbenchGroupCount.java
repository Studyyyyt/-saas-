package com.example.springboot.entity;

public class PatientWorkbenchGroupCount {
    private String group_key;
    private Long total_count;

    public String getGroup_key() {
        return group_key;
    }

    public void setGroup_key(String group_key) {
        this.group_key = group_key;
    }

    public Long getTotal_count() {
        return total_count;
    }

    public void setTotal_count(Long total_count) {
        this.total_count = total_count;
    }
}
