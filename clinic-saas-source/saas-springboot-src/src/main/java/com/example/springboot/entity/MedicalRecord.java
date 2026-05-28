package com.example.springboot.entity;

import com.example.springboot.common.json.FlexibleDateDeserializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Date;
import java.util.List;

public class MedicalRecord {
    private Long id;
    private Long patient_id;
    private String patient_name;
    private Long doctor_account_id;
    private String doctor_name;
    private String nurse_name;
    private String assistant_name;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonDeserialize(using = FlexibleDateDeserializer.class)
    private Date visit_date;
    private String record_type;
    private String chief_complaint;
    private String present_illness_history;
    private String past_history;
    private String infectious_history;
    private String allergy_history;
    private String general_condition;
    private String examination;
    private String auxiliary_examination;
    private String diagnosis;
    private String treatment_plan;
    private String treatment;
    private String tooth_positions;
    private String medical_advice;
    private String prescription;
    private String record_tags;
    private String image_summary;
    private String notes;
    private String record_status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonDeserialize(using = FlexibleDateDeserializer.class)
    private Date created_at;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonDeserialize(using = FlexibleDateDeserializer.class)
    private Date updated_at;
    private List<MedicalRecordOperation> operation_items;
    private Integer operation_count;
    private Integer pending_lab_count;
    private String operation_summary;

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

    public String getNurse_name() {
        return nurse_name;
    }

    public void setNurse_name(String nurse_name) {
        this.nurse_name = nurse_name;
    }

    public String getAssistant_name() {
        return assistant_name;
    }

    public void setAssistant_name(String assistant_name) {
        this.assistant_name = assistant_name;
    }

    public Date getVisit_date() {
        return visit_date;
    }

    public void setVisit_date(Date visit_date) {
        this.visit_date = visit_date;
    }

    public String getRecord_type() {
        return record_type;
    }

    public void setRecord_type(String record_type) {
        this.record_type = record_type;
    }

    public String getChief_complaint() {
        return chief_complaint;
    }

    public void setChief_complaint(String chief_complaint) {
        this.chief_complaint = chief_complaint;
    }

    public String getPresent_illness_history() {
        return present_illness_history;
    }

    public void setPresent_illness_history(String present_illness_history) {
        this.present_illness_history = present_illness_history;
    }

    public String getPast_history() {
        return past_history;
    }

    public void setPast_history(String past_history) {
        this.past_history = past_history;
    }

    public String getInfectious_history() {
        return infectious_history;
    }

    public void setInfectious_history(String infectious_history) {
        this.infectious_history = infectious_history;
    }

    public String getAllergy_history() {
        return allergy_history;
    }

    public void setAllergy_history(String allergy_history) {
        this.allergy_history = allergy_history;
    }

    public String getGeneral_condition() {
        return general_condition;
    }

    public void setGeneral_condition(String general_condition) {
        this.general_condition = general_condition;
    }

    public String getExamination() {
        return examination;
    }

    public void setExamination(String examination) {
        this.examination = examination;
    }

    public String getAuxiliary_examination() {
        return auxiliary_examination;
    }

    public void setAuxiliary_examination(String auxiliary_examination) {
        this.auxiliary_examination = auxiliary_examination;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getTreatment_plan() {
        return treatment_plan;
    }

    public void setTreatment_plan(String treatment_plan) {
        this.treatment_plan = treatment_plan;
    }

    public String getTreatment() {
        return treatment;
    }

    public void setTreatment(String treatment) {
        this.treatment = treatment;
    }

    public String getTooth_positions() {
        return tooth_positions;
    }

    public void setTooth_positions(String tooth_positions) {
        this.tooth_positions = tooth_positions;
    }

    public String getMedical_advice() {
        return medical_advice;
    }

    public void setMedical_advice(String medical_advice) {
        this.medical_advice = medical_advice;
    }

    public String getPrescription() {
        return prescription;
    }

    public void setPrescription(String prescription) {
        this.prescription = prescription;
    }

    public String getRecord_tags() {
        return record_tags;
    }

    public void setRecord_tags(String record_tags) {
        this.record_tags = record_tags;
    }

    public String getImage_summary() {
        return image_summary;
    }

    public void setImage_summary(String image_summary) {
        this.image_summary = image_summary;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getRecord_status() {
        return record_status;
    }

    public void setRecord_status(String record_status) {
        this.record_status = record_status;
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

    public List<MedicalRecordOperation> getOperation_items() {
        return operation_items;
    }

    public void setOperation_items(List<MedicalRecordOperation> operation_items) {
        this.operation_items = operation_items;
    }

    public Integer getOperation_count() {
        return operation_count;
    }

    public void setOperation_count(Integer operation_count) {
        this.operation_count = operation_count;
    }

    public Integer getPending_lab_count() {
        return pending_lab_count;
    }

    public void setPending_lab_count(Integer pending_lab_count) {
        this.pending_lab_count = pending_lab_count;
    }

    public String getOperation_summary() {
        return operation_summary;
    }

    public void setOperation_summary(String operation_summary) {
        this.operation_summary = operation_summary;
    }
}
