package com.example.springboot.entity;

import java.sql.Date;

public class Treatment {
    private Long id;
    private Long patient_id;
    private String patient_name;
    private String batch_no;
    private Long medical_record_id;
    private Long project_id;
    private String appointment_purpose;
    private String status;
    private Long doctor_account_id;
    private String doctor_name;
    private Date treatment_date;
    private String treatment_content;
    private String tooth_positions;
    private String treatment_product;
    private String treatment_fee;
    private Double charged_amount;
    private Double refunded_amount;
    private Double arrears_amount;
    private String billing_status;
    private Boolean can_charge;
    private Boolean can_refund;
    private java.util.Date created_at;
    private java.util.Date updated_at;

    public Long getId() {
        return id;
    }

    public Long getPatient_id() {
        return patient_id;
    }

    public void setPatient_id(Long patient_id) {
        this.patient_id = patient_id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPatient_name() {
        return patient_name;
    }

    public void setPatient_name(String patient_name) {
        this.patient_name = patient_name;
    }

    public String getBatch_no() {
        return batch_no;
    }

    public void setBatch_no(String batch_no) {
        this.batch_no = batch_no;
    }

    public Long getMedical_record_id() {
        return medical_record_id;
    }

    public void setMedical_record_id(Long medical_record_id) {
        this.medical_record_id = medical_record_id;
    }

    public String getAppointment_purpose() {
        return appointment_purpose;
    }

    public void setAppointment_purpose(String appointment_purpose) {
        this.appointment_purpose = appointment_purpose;
    }

    public Long getProject_id() {
        return project_id;
    }

    public void setProject_id(Long project_id) {
        this.project_id = project_id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public Date getTreatment_date() {
        return treatment_date;
    }

    public void setTreatment_date(Date treatment_date) {
        this.treatment_date = treatment_date;
    }

    public String getTreatment_content() {
        return treatment_content;
    }

    public void setTreatment_content(String treatment_content) {
        this.treatment_content = treatment_content;
    }

    public String getTooth_positions() {
        return tooth_positions;
    }

    public void setTooth_positions(String tooth_positions) {
        this.tooth_positions = tooth_positions;
    }

    public String getTreatment_product() {
        return treatment_product;
    }

    public void setTreatment_product(String treatment_product) {
        this.treatment_product = treatment_product;
    }

    public String getTreatment_fee() {
        return treatment_fee;
    }

    public void setTreatment_fee(String treatment_fee) {
        this.treatment_fee = treatment_fee;
    }

    public Double getCharged_amount() {
        return charged_amount;
    }

    public void setCharged_amount(Double charged_amount) {
        this.charged_amount = charged_amount;
    }

    public Double getRefunded_amount() {
        return refunded_amount;
    }

    public void setRefunded_amount(Double refunded_amount) {
        this.refunded_amount = refunded_amount;
    }

    public Double getArrears_amount() {
        return arrears_amount;
    }

    public void setArrears_amount(Double arrears_amount) {
        this.arrears_amount = arrears_amount;
    }

    public String getBilling_status() {
        return billing_status;
    }

    public void setBilling_status(String billing_status) {
        this.billing_status = billing_status;
    }

    public Boolean getCan_charge() {
        return can_charge;
    }

    public void setCan_charge(Boolean can_charge) {
        this.can_charge = can_charge;
    }

    public Boolean getCan_refund() {
        return can_refund;
    }

    public void setCan_refund(Boolean can_refund) {
        this.can_refund = can_refund;
    }

    public java.util.Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(java.util.Date created_at) {
        this.created_at = created_at;
    }

    public java.util.Date getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(java.util.Date updated_at) {
        this.updated_at = updated_at;
    }
}
