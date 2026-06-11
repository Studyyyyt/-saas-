package com.example.springboot.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class Patient {
    private int id;
    private String name;
    private String name_pinyin;
    private String name_initials;
    private String gender;
    private Integer age;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date date_of_birth;
    private String phone;
    private String email;
    private String address;
    private String relation_type;
    private Long related_patient_id;
    private String related_patient_name;
    private String wechat_openid;
    private String customer_source;
    private String referrer_type;
    private Long referrer_patient_id;
    private String referrer_patient_name;
    private String external_referrer_type;
    private String external_referrer_name;
    private String external_referrer_contact;
    private String referral_remark;
    private Boolean has_arrears;
    private Double arrears_amount;
    private String latest_visit_doctor;
    private String latest_treatment;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date created_at;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updated_at;
    private Long consultation_record_id;
    private String clinic_id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName_pinyin() {
        return name_pinyin;
    }

    public void setName_pinyin(String name_pinyin) {
        this.name_pinyin = name_pinyin;
    }

    public String getName_initials() {
        return name_initials;
    }

    public void setName_initials(String name_initials) {
        this.name_initials = name_initials;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Date getDate_of_birth() {
        return date_of_birth;
    }

    public void setDate_of_birth(Date date_of_birth) {
        this.date_of_birth = date_of_birth;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getWechat_openid() {
        return wechat_openid;
    }

    public String getRelation_type() {
        return relation_type;
    }

    public void setRelation_type(String relation_type) {
        this.relation_type = relation_type;
    }

    public Long getRelated_patient_id() {
        return related_patient_id;
    }

    public void setRelated_patient_id(Long related_patient_id) {
        this.related_patient_id = related_patient_id;
    }

    public String getRelated_patient_name() {
        return related_patient_name;
    }

    public void setRelated_patient_name(String related_patient_name) {
        this.related_patient_name = related_patient_name;
    }

    public void setWechat_openid(String wechat_openid) {
        this.wechat_openid = wechat_openid;
    }

    public String getCustomer_source() {
        return customer_source;
    }

    public void setCustomer_source(String customer_source) {
        this.customer_source = customer_source;
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

    public String getReferral_remark() {
        return referral_remark;
    }

    public void setReferral_remark(String referral_remark) {
        this.referral_remark = referral_remark;
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

    public String getLatest_visit_doctor() {
        return latest_visit_doctor;
    }

    public void setLatest_visit_doctor(String latest_visit_doctor) {
        this.latest_visit_doctor = latest_visit_doctor;
    }

    public String getLatest_treatment() {
        return latest_treatment;
    }

    public void setLatest_treatment(String latest_treatment) {
        this.latest_treatment = latest_treatment;
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

    public Long getConsultation_record_id() {
        return consultation_record_id;
    }

    public void setConsultation_record_id(Long consultation_record_id) {
        this.consultation_record_id = consultation_record_id;
    }

    public String getClinic_id() {
        return clinic_id;
    }

    public void setClinic_id(String clinic_id) {
        this.clinic_id = clinic_id;
    }
}
