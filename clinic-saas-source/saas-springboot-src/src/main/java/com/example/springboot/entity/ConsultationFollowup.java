package com.example.springboot.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

/**
 * 咨询跟进记录实体（销售跟进维度，区别于 PatientFollowup 医疗回访）
 */
public class ConsultationFollowup {
    private Long id;
    private Long consultation_id;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date followup_time;
    private String content;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date next_followup_time;
    private Long created_by;
    private String created_by_name;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date created_at;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updated_at;

    // 扩展字段（JOIN consultation_records 用）
    private String consultation_contact_name;
    private String consultation_contact_phone;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getConsultation_id() {
        return consultation_id;
    }

    public void setConsultation_id(Long consultation_id) {
        this.consultation_id = consultation_id;
    }

    public Date getFollowup_time() {
        return followup_time;
    }

    public void setFollowup_time(Date followup_time) {
        this.followup_time = followup_time;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getNext_followup_time() {
        return next_followup_time;
    }

    public void setNext_followup_time(Date next_followup_time) {
        this.next_followup_time = next_followup_time;
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

    public String getConsultation_contact_name() {
        return consultation_contact_name;
    }

    public void setConsultation_contact_name(String consultation_contact_name) {
        this.consultation_contact_name = consultation_contact_name;
    }

    public String getConsultation_contact_phone() {
        return consultation_contact_phone;
    }

    public void setConsultation_contact_phone(String consultation_contact_phone) {
        this.consultation_contact_phone = consultation_contact_phone;
    }
}
