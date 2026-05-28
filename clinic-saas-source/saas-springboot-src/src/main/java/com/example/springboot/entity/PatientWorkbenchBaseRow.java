package com.example.springboot.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class PatientWorkbenchBaseRow extends PatientWorkbenchRow {
    @JsonIgnore
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date latest_medical_record_visit_date;
    @JsonIgnore
    private Long latest_medical_record_doctor_account_id;
    @JsonIgnore
    private String latest_medical_record_doctor_name;
    @JsonIgnore
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date latest_treatment_visit_date;
    @JsonIgnore
    private Long latest_treatment_doctor_account_id;
    @JsonIgnore
    private String latest_treatment_doctor_name;
    @JsonIgnore
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date latest_appointment_visit_date;
    @JsonIgnore
    private Long latest_appointment_doctor_account_id;
    @JsonIgnore
    private String latest_appointment_doctor_name;
    @JsonIgnore
    private String group_signal_text;
    @JsonIgnore
    private String custom_group_keys_text;

    public Date getLatest_medical_record_visit_date() {
        return latest_medical_record_visit_date;
    }

    public void setLatest_medical_record_visit_date(Date latest_medical_record_visit_date) {
        this.latest_medical_record_visit_date = latest_medical_record_visit_date;
    }

    public Long getLatest_medical_record_doctor_account_id() {
        return latest_medical_record_doctor_account_id;
    }

    public void setLatest_medical_record_doctor_account_id(Long latest_medical_record_doctor_account_id) {
        this.latest_medical_record_doctor_account_id = latest_medical_record_doctor_account_id;
    }

    public String getLatest_medical_record_doctor_name() {
        return latest_medical_record_doctor_name;
    }

    public void setLatest_medical_record_doctor_name(String latest_medical_record_doctor_name) {
        this.latest_medical_record_doctor_name = latest_medical_record_doctor_name;
    }

    public Date getLatest_treatment_visit_date() {
        return latest_treatment_visit_date;
    }

    public void setLatest_treatment_visit_date(Date latest_treatment_visit_date) {
        this.latest_treatment_visit_date = latest_treatment_visit_date;
    }

    public Long getLatest_treatment_doctor_account_id() {
        return latest_treatment_doctor_account_id;
    }

    public void setLatest_treatment_doctor_account_id(Long latest_treatment_doctor_account_id) {
        this.latest_treatment_doctor_account_id = latest_treatment_doctor_account_id;
    }

    public String getLatest_treatment_doctor_name() {
        return latest_treatment_doctor_name;
    }

    public void setLatest_treatment_doctor_name(String latest_treatment_doctor_name) {
        this.latest_treatment_doctor_name = latest_treatment_doctor_name;
    }

    public Date getLatest_appointment_visit_date() {
        return latest_appointment_visit_date;
    }

    public void setLatest_appointment_visit_date(Date latest_appointment_visit_date) {
        this.latest_appointment_visit_date = latest_appointment_visit_date;
    }

    public Long getLatest_appointment_doctor_account_id() {
        return latest_appointment_doctor_account_id;
    }

    public void setLatest_appointment_doctor_account_id(Long latest_appointment_doctor_account_id) {
        this.latest_appointment_doctor_account_id = latest_appointment_doctor_account_id;
    }

    public String getLatest_appointment_doctor_name() {
        return latest_appointment_doctor_name;
    }

    public void setLatest_appointment_doctor_name(String latest_appointment_doctor_name) {
        this.latest_appointment_doctor_name = latest_appointment_doctor_name;
    }

    public String getGroup_signal_text() {
        return group_signal_text;
    }

    public void setGroup_signal_text(String group_signal_text) {
        this.group_signal_text = group_signal_text;
    }

    public String getCustom_group_keys_text() {
        return custom_group_keys_text;
    }

    public void setCustom_group_keys_text(String custom_group_keys_text) {
        this.custom_group_keys_text = custom_group_keys_text;
    }
}
