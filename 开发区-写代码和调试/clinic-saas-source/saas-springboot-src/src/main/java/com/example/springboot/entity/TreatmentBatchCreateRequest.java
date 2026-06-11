package com.example.springboot.entity;

import java.sql.Date;
import java.util.List;

public class TreatmentBatchCreateRequest {
    private Long patient_id;
    private Long medical_record_id;
    private String patient_name;
    private Long doctor_account_id;
    private String doctor_name;
    private Date treatment_date;
    private String status;
    private Double discount_rate;
    private Double discounted_total_fee;
    private List<TreatmentBatchItemRequest> items;

    public Long getPatient_id() {
        return patient_id;
    }

    public void setPatient_id(Long patient_id) {
        this.patient_id = patient_id;
    }

    public Long getMedical_record_id() {
        return medical_record_id;
    }

    public void setMedical_record_id(Long medical_record_id) {
        this.medical_record_id = medical_record_id;
    }

    public String getPatient_name() {
        return patient_name;
    }

    public void setPatient_name(String patient_name) {
        this.patient_name = patient_name;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getDiscount_rate() {
        return discount_rate;
    }

    public void setDiscount_rate(Double discount_rate) {
        this.discount_rate = discount_rate;
    }

    public Double getDiscounted_total_fee() {
        return discounted_total_fee;
    }

    public void setDiscounted_total_fee(Double discounted_total_fee) {
        this.discounted_total_fee = discounted_total_fee;
    }

    public List<TreatmentBatchItemRequest> getItems() {
        return items;
    }

    public void setItems(List<TreatmentBatchItemRequest> items) {
        this.items = items;
    }
}
