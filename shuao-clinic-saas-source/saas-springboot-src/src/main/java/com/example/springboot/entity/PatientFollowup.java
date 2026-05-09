package com.example.springboot.entity;

import java.util.Date;

public class PatientFollowup {
    private Long id;
    private Long patient_id;
    private Long doctor_account_id;
    private String doctor_name;
    private Date followup_date;
    private String followup_type;
    private String followup_project;
    private String summary;
    private Date next_followup_date;
    private Date created_at;
    private Date updated_at;
    private String patient_name;
    private String patient_phone;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPatient_id() {
        return patient_id;
    }

    public void setPatient_id(Long patient_id) {
        this.patient_id = patient_id;
    }

    public Long getDoctor_account_id() {
        return doctor_account_id;
    }

    public void setDoctor_account_id(Long doctor_account_id) {
        this.doctor_account_id = doctor_account_id;
    }

    public String getDoctor_name() {
        return doctor_name;
    }

    public void setDoctor_name(String doctor_name) {
        this.doctor_name = doctor_name;
    }

    public Date getFollowup_date() {
        return followup_date;
    }

    public void setFollowup_date(Date followup_date) {
        this.followup_date = followup_date;
    }

    public String getFollowup_type() {
        return followup_type;
    }

    public void setFollowup_type(String followup_type) {
        this.followup_type = followup_type;
    }

    public String getFollowup_project() {
        return followup_project;
    }

    public void setFollowup_project(String followup_project) {
        this.followup_project = followup_project;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Date getNext_followup_date() {
        return next_followup_date;
    }

    public void setNext_followup_date(Date next_followup_date) {
        this.next_followup_date = next_followup_date;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }

    public Date getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(Date updated_at) {
        this.updated_at = updated_at;
    }

    public String getPatient_name() {
        return patient_name;
    }

    public void setPatient_name(String patient_name) {
        this.patient_name = patient_name;
    }

    public String getPatient_phone() {
        return patient_phone;
    }

    public void setPatient_phone(String patient_phone) {
        this.patient_phone = patient_phone;
    }
}
