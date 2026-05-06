package com.example.springboot.entity;

import java.util.Date;

public class InsurancePatientProfile {
    private Long id;
    private Long patient_id;
    private String insurance_person_no;
    private String id_card_no;
    private String insured_region_code;
    private String insured_type;
    private String card_no;
    private String card_type;
    private String person_name;
    private String gender;
    private String birthday;
    private String phone;
    private Integer status;
    private String last_auth_no;
    private Date last_verified_at;
    private String ext_json;
    private Date created_at;
    private Date updated_at;

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

    public String getInsurance_person_no() {
        return insurance_person_no;
    }

    public void setInsurance_person_no(String insurance_person_no) {
        this.insurance_person_no = insurance_person_no;
    }

    public String getId_card_no() {
        return id_card_no;
    }

    public void setId_card_no(String id_card_no) {
        this.id_card_no = id_card_no;
    }

    public String getInsured_region_code() {
        return insured_region_code;
    }

    public void setInsured_region_code(String insured_region_code) {
        this.insured_region_code = insured_region_code;
    }

    public String getInsured_type() {
        return insured_type;
    }

    public void setInsured_type(String insured_type) {
        this.insured_type = insured_type;
    }

    public String getCard_no() {
        return card_no;
    }

    public void setCard_no(String card_no) {
        this.card_no = card_no;
    }

    public String getCard_type() {
        return card_type;
    }

    public void setCard_type(String card_type) {
        this.card_type = card_type;
    }

    public String getPerson_name() {
        return person_name;
    }

    public void setPerson_name(String person_name) {
        this.person_name = person_name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getLast_auth_no() {
        return last_auth_no;
    }

    public void setLast_auth_no(String last_auth_no) {
        this.last_auth_no = last_auth_no;
    }

    public Date getLast_verified_at() {
        return last_verified_at;
    }

    public void setLast_verified_at(Date last_verified_at) {
        this.last_verified_at = last_verified_at;
    }

    public String getExt_json() {
        return ext_json;
    }

    public void setExt_json(String ext_json) {
        this.ext_json = ext_json;
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
}
