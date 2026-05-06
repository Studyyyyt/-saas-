package com.example.springboot.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class ConsultationRecord {
    private Long id;
    private Long patient_id;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date consultation_time;
    private String consultation_channel;
    private String referrer_type;
    private Long referrer_patient_id;
    private String referrer_patient_name;
    private String external_referrer_type;
    private String external_referrer_name;
    private String external_referrer_contact;
    private String chief_project;
    private String intent_level;
    private String handling_result;
    private String contact_name;
    private String contact_phone;
    private String remarks;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date arrived_at;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date deal_at;
    private Long created_by;
    private String created_by_name;
    private Long updated_by;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date created_at;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updated_at;
    private String patient_name;
    private String patient_phone;
    private String patient_customer_source;
    private Double total_deal_amount;
    private Boolean has_deal;

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

    public Date getConsultation_time() {
        return consultation_time;
    }

    public void setConsultation_time(Date consultation_time) {
        this.consultation_time = consultation_time;
    }

    public String getConsultation_channel() {
        return consultation_channel;
    }

    public void setConsultation_channel(String consultation_channel) {
        this.consultation_channel = consultation_channel;
    }

    public String getReferrer_type() {
        return referrer_type;
    }

    public void setReferrer_type(String referrer_type) {
        this.referrer_type = referrer_type;
    }

    public Long getReferrer_patient_id() {
        return referrer_patient_id;
    }

    public void setReferrer_patient_id(Long referrer_patient_id) {
        this.referrer_patient_id = referrer_patient_id;
    }

    public String getReferrer_patient_name() {
        return referrer_patient_name;
    }

    public void setReferrer_patient_name(String referrer_patient_name) {
        this.referrer_patient_name = referrer_patient_name;
    }

    public String getExternal_referrer_type() {
        return external_referrer_type;
    }

    public void setExternal_referrer_type(String external_referrer_type) {
        this.external_referrer_type = external_referrer_type;
    }

    public String getExternal_referrer_name() {
        return external_referrer_name;
    }

    public void setExternal_referrer_name(String external_referrer_name) {
        this.external_referrer_name = external_referrer_name;
    }

    public String getExternal_referrer_contact() {
        return external_referrer_contact;
    }

    public void setExternal_referrer_contact(String external_referrer_contact) {
        this.external_referrer_contact = external_referrer_contact;
    }

    public String getChief_project() {
        return chief_project;
    }

    public void setChief_project(String chief_project) {
        this.chief_project = chief_project;
    }

    public String getIntent_level() {
        return intent_level;
    }

    public void setIntent_level(String intent_level) {
        this.intent_level = intent_level;
    }

    public String getHandling_result() {
        return handling_result;
    }

    public void setHandling_result(String handling_result) {
        this.handling_result = handling_result;
    }

    public String getContact_name() {
        return contact_name;
    }

    public void setContact_name(String contact_name) {
        this.contact_name = contact_name;
    }

    public String getContact_phone() {
        return contact_phone;
    }

    public void setContact_phone(String contact_phone) {
        this.contact_phone = contact_phone;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Date getArrived_at() {
        return arrived_at;
    }

    public void setArrived_at(Date arrived_at) {
        this.arrived_at = arrived_at;
    }

    public Date getDeal_at() {
        return deal_at;
    }

    public void setDeal_at(Date deal_at) {
        this.deal_at = deal_at;
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

    public Long getUpdated_by() {
        return updated_by;
    }

    public void setUpdated_by(Long updated_by) {
        this.updated_by = updated_by;
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

    public String getPatient_name() {
        return patient_name;
    }

    public void setPatient_name(String patient_name) {
        this.patient_name = patient_name;
    }

    public String getPatient_phone() {
        return patient_phone;
    }

    public void setPatient_phone(String patient_phone) {
        this.patient_phone = patient_phone;
    }

    public String getPatient_customer_source() {
        return patient_customer_source;
    }

    public void setPatient_customer_source(String patient_customer_source) {
        this.patient_customer_source = patient_customer_source;
    }

    public Double getTotal_deal_amount() {
        return total_deal_amount;
    }

    public void setTotal_deal_amount(Double total_deal_amount) {
        this.total_deal_amount = total_deal_amount;
    }

    public Boolean getHas_deal() {
        return has_deal;
    }

    public void setHas_deal(Boolean has_deal) {
        this.has_deal = has_deal;
    }
}
