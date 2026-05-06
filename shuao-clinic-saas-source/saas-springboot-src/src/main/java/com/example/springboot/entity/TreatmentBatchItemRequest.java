package com.example.springboot.entity;

public class TreatmentBatchItemRequest {
    private Long catalog_id;
    private Long project_id;
    private Long doctor_account_id;
    private String doctor_name;
    private String appointment_purpose;
    private String treatment_content;
    private String tooth_positions;
    private String treatment_product;
    private Double treatment_fee;

    public Long getCatalog_id() {
        return catalog_id;
    }

    public void setCatalog_id(Long catalog_id) {
        this.catalog_id = catalog_id;
    }

    public Long getProject_id() {
        return project_id;
    }

    public void setProject_id(Long project_id) {
        this.project_id = project_id;
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

    public String getAppointment_purpose() {
        return appointment_purpose;
    }

    public void setAppointment_purpose(String appointment_purpose) {
        this.appointment_purpose = appointment_purpose;
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

    public Double getTreatment_fee() {
        return treatment_fee;
    }

    public void setTreatment_fee(Double treatment_fee) {
        this.treatment_fee = treatment_fee;
    }
}
