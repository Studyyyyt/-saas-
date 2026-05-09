package com.example.springboot.entity;

import java.sql.Date;
import java.sql.Time;

public class Appointment {
    private int id;
    private Long patient_id;
    private String patient_name;
    private Date appointment_date;
    private Time appointment_time;
    private Integer duration_minutes;
    private Long doctor_account_id;
    private String doctor_name;
    private String appointment_purpose;
    private String cancel_reason;
    private String status;
    private String clinic_status;
    private java.util.Date check_in_time;
    private Boolean has_arrears;
    private Double arrears_amount;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Long getPatient_id() {
        return patient_id;
    }

    public void setPatient_id(Long patient_id) {
        this.patient_id = patient_id;
    }

    public String getPatient_name() {
        return patient_name;
    }

    public void setPatient_name(String patient_name) {
        this.patient_name = patient_name;
    }

    public Date getAppointment_date() {
        return appointment_date;
    }

    public void setAppointment_date(Date appointment_date) {
        this.appointment_date = appointment_date;
    }

    public Time getAppointment_time() {
        return appointment_time;
    }

    public void setAppointment_time(Time appointment_time) {
        this.appointment_time = appointment_time;
    }

    public Integer getDuration_minutes() {
        return duration_minutes;
    }

    public void setDuration_minutes(Integer duration_minutes) {
        this.duration_minutes = duration_minutes;
    }

    public String getDoctor_name() {
        return doctor_name;
    }

    public void setDoctor_name(String doctor_name) {
        this.doctor_name = doctor_name;
    }

    public Long getDoctor_account_id() {
        return doctor_account_id;
    }

    public void setDoctor_account_id(Long doctor_account_id) {
        this.doctor_account_id = doctor_account_id;
    }

    public String getAppointment_purpose() {
        return appointment_purpose;
    }

    public void setAppointment_purpose(String appointment_purpose) {
        this.appointment_purpose = appointment_purpose;
    }

    public String getCancel_reason() {
        return cancel_reason;
    }

    public void setCancel_reason(String cancel_reason) {
        this.cancel_reason = cancel_reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getClinic_status() {
        return clinic_status;
    }

    public void setClinic_status(String clinic_status) {
        this.clinic_status = clinic_status;
    }

    public java.util.Date getCheck_in_time() {
        return check_in_time;
    }

    public void setCheck_in_time(java.util.Date check_in_time) {
        this.check_in_time = check_in_time;
    }

    public Boolean getHas_arrears() {
        return has_arrears;
    }

    public void setHas_arrears(Boolean has_arrears) {
        this.has_arrears = has_arrears;
    }

    public Double getArrears_amount() {
        return arrears_amount;
    }

    public void setArrears_amount(Double arrears_amount) {
        this.arrears_amount = arrears_amount;
    }
}
