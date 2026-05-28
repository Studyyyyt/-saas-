package com.example.springboot.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MedicalRecordTemplate {
    private Long id;
    private String template_name;
    private String template_category;
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
    private String record_type;
    private String operation_items_json;
    private Integer status;
    private Long created_by;
    private String created_by_name;
    private Date created_at;
    private Date updated_at;
    private List<MedicalRecordOperation> operation_items = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTemplate_name() {
        return template_name;
    }

    public void setTemplate_name(String template_name) {
        this.template_name = template_name;
    }

    public String getTemplate_category() {
        return template_category;
    }

    public void setTemplate_category(String template_category) {
        this.template_category = template_category;
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

    public String getRecord_type() {
        return record_type;
    }

    public void setRecord_type(String record_type) {
        this.record_type = record_type;
    }

    public String getOperation_items_json() {
        return operation_items_json;
    }

    public void setOperation_items_json(String operation_items_json) {
        this.operation_items_json = operation_items_json;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getCreated_by() {
        return created_by;
    }

    public void setCreated_by(Long created_by) {
        this.created_by = created_by;
    }

    public String getCreated_by_name() {
        return created_by_name;
    }

    public void setCreated_by_name(String created_by_name) {
        this.created_by_name = created_by_name;
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
}
