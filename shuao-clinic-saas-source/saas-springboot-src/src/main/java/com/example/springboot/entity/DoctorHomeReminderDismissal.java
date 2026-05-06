package com.example.springboot.entity;

public class DoctorHomeReminderDismissal {
    private Long id;
    private Long doctor_account_id;
    private String doctor_name;
    private Long patient_id;
    private String patient_name;
    private String reminder_key;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getReminder_key() {
        return reminder_key;
    }

    public void setReminder_key(String reminder_key) {
        this.reminder_key = reminder_key;
    }
}
