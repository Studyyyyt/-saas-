package com.example.springboot.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;
import java.util.List;

public class PatientWorkbenchRow extends Patient {
    private Long latest_visit_doctor_account_id;
    private String latest_visit_doctor_name;
    private String latest_visit_source;
    private Long followup_doctor_account_id;
    private String followup_doctor_name;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date last_visit_date;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date last_followup_date;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date next_followup_date;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date last_activity_at;
    private Boolean next_followup_overdue;
    private Integer visit_count;
    private Double total_spent;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date last_treatment_date;
    private Integer visit_count_last_6m;
    private Boolean high_value_flag;
    private Boolean lost_risk_flag;
    private Integer referred_count;
    private Double referred_revenue;
    private Boolean word_of_mouth_flag;
    private List<String> group_keys;
    private List<String> custom_group_keys;
    private String primary_group_key;
    private String primary_group_label;
    private List<PatientRiskTag> risk_tags;
    private List<PatientWorkbenchTag> patient_tags;

    public Long getLatest_visit_doctor_account_id() {
        return latest_visit_doctor_account_id;
    }

    public void setLatest_visit_doctor_account_id(Long latest_visit_doctor_account_id) {
        this.latest_visit_doctor_account_id = latest_visit_doctor_account_id;
    }

    public String getLatest_visit_doctor_name() {
        return latest_visit_doctor_name;
    }

    public void setLatest_visit_doctor_name(String latest_visit_doctor_name) {
        this.latest_visit_doctor_name = latest_visit_doctor_name;
    }

    public String getLatest_visit_source() {
        return latest_visit_source;
    }

    public void setLatest_visit_source(String latest_visit_source) {
        this.latest_visit_source = latest_visit_source;
    }

    public Long getFollowup_doctor_account_id() {
        return followup_doctor_account_id;
    }

    public void setFollowup_doctor_account_id(Long followup_doctor_account_id) {
        this.followup_doctor_account_id = followup_doctor_account_id;
    }

    public String getFollowup_doctor_name() {
        return followup_doctor_name;
    }

    public void setFollowup_doctor_name(String followup_doctor_name) {
        this.followup_doctor_name = followup_doctor_name;
    }

    public Date getLast_visit_date() {
        return last_visit_date;
    }

    public void setLast_visit_date(Date last_visit_date) {
        this.last_visit_date = last_visit_date;
    }

    public Date getLast_followup_date() {
        return last_followup_date;
    }

    public void setLast_followup_date(Date last_followup_date) {
        this.last_followup_date = last_followup_date;
    }

    public Date getNext_followup_date() {
        return next_followup_date;
    }

    public void setNext_followup_date(Date next_followup_date) {
        this.next_followup_date = next_followup_date;
    }

    public Date getLast_activity_at() {
        return last_activity_at;
    }

    public void setLast_activity_at(Date last_activity_at) {
        this.last_activity_at = last_activity_at;
    }

    public Boolean getNext_followup_overdue() {
        return next_followup_overdue;
    }

    public void setNext_followup_overdue(Boolean next_followup_overdue) {
        this.next_followup_overdue = next_followup_overdue;
    }

    public Integer getVisit_count() {
        return visit_count;
    }

    public void setVisit_count(Integer visit_count) {
        this.visit_count = visit_count;
    }

    public Double getTotal_spent() {
        return total_spent;
    }

    public void setTotal_spent(Double total_spent) {
        this.total_spent = total_spent;
    }

    public Date getLast_treatment_date() {
        return last_treatment_date;
    }

    public void setLast_treatment_date(Date last_treatment_date) {
        this.last_treatment_date = last_treatment_date;
    }

    public Integer getVisit_count_last_6m() {
        return visit_count_last_6m;
    }

    public void setVisit_count_last_6m(Integer visit_count_last_6m) {
        this.visit_count_last_6m = visit_count_last_6m;
    }

    public Boolean getHigh_value_flag() {
        return high_value_flag;
    }

    public void setHigh_value_flag(Boolean high_value_flag) {
        this.high_value_flag = high_value_flag;
    }

    public Boolean getLost_risk_flag() {
        return lost_risk_flag;
    }

    public void setLost_risk_flag(Boolean lost_risk_flag) {
        this.lost_risk_flag = lost_risk_flag;
    }

    public Integer getReferred_count() {
        return referred_count;
    }

    public void setReferred_count(Integer referred_count) {
        this.referred_count = referred_count;
    }

    public Double getReferred_revenue() {
        return referred_revenue;
    }

    public void setReferred_revenue(Double referred_revenue) {
        this.referred_revenue = referred_revenue;
    }

    public Boolean getWord_of_mouth_flag() {
        return word_of_mouth_flag;
    }

    public void setWord_of_mouth_flag(Boolean word_of_mouth_flag) {
        this.word_of_mouth_flag = word_of_mouth_flag;
    }

    public List<String> getGroup_keys() {
        return group_keys;
    }

    public void setGroup_keys(List<String> group_keys) {
        this.group_keys = group_keys;
    }

    public List<String> getCustom_group_keys() {
        return custom_group_keys;
    }

    public void setCustom_group_keys(List<String> custom_group_keys) {
        this.custom_group_keys = custom_group_keys;
    }

    public String getPrimary_group_key() {
        return primary_group_key;
    }

    public void setPrimary_group_key(String primary_group_key) {
        this.primary_group_key = primary_group_key;
    }

    public String getPrimary_group_label() {
        return primary_group_label;
    }

    public void setPrimary_group_label(String primary_group_label) {
        this.primary_group_label = primary_group_label;
    }

    public List<PatientRiskTag> getRisk_tags() {
        return risk_tags;
    }

    public void setRisk_tags(List<PatientRiskTag> risk_tags) {
        this.risk_tags = risk_tags;
    }

    public List<PatientWorkbenchTag> getPatient_tags() {
        return patient_tags;
    }

    public void setPatient_tags(List<PatientWorkbenchTag> patient_tags) {
        this.patient_tags = patient_tags;
    }
}
