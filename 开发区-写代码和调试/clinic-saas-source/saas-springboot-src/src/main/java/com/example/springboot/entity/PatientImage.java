package com.example.springboot.entity;

import java.util.Date;

public class PatientImage {
    private Long id;
    private Long patient_id;
    private String patient_name;
    private String image_name;
    private String image_type;
    private Date image_date;
    private String file_path;
    private String notes;
    private Boolean sent_to_patient;
    private Date sent_at;
    private Date created_at;

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

    public String getImage_name() {
        return image_name;
    }

    public void setImage_name(String image_name) {
        this.image_name = image_name;
    }

    public String getImage_type() {
        return image_type;
    }

    public void setImage_type(String image_type) {
        this.image_type = image_type;
    }

    public Date getImage_date() {
        return image_date;
    }

    public void setImage_date(Date image_date) {
        this.image_date = image_date;
    }

    public String getFile_path() {
        return file_path;
    }

    public void setFile_path(String file_path) {
        this.file_path = file_path;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Boolean getSent_to_patient() {
        return sent_to_patient;
    }

    public void setSent_to_patient(Boolean sent_to_patient) {
        this.sent_to_patient = sent_to_patient;
    }

    public Date getSent_at() {
        return sent_at;
    }

    public void setSent_at(Date sent_at) {
        this.sent_at = sent_at;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }
}
