package com.example.springboot.entity;

public class PatientArrearsSummary {
    private Long patient_id;
    private Double arrears_amount;

    public Long getPatient_id() {
        return patient_id;
    }

    public void setPatient_id(Long patient_id) {
        this.patient_id = patient_id;
    }

    public Double getArrears_amount() {
        return arrears_amount;
    }

    public void setArrears_amount(Double arrears_amount) {
        this.arrears_amount = arrears_amount;
    }
}
