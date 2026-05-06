package com.example.springboot.entity;

import java.util.Date;

public class PatientWechatBindScene {
    private Long id;
    private Long patient_id;
    private String scene_key;
    private String qr_ticket;
    private String qr_url;
    private Integer expire_seconds;
    private String status;
    private Date bound_at;
    private String bound_openid;
    private Date created_at;
    private Date updated_at;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPatient_id() { return patient_id; }
    public void setPatient_id(Long patient_id) { this.patient_id = patient_id; }

    public String getScene_key() { return scene_key; }
    public void setScene_key(String scene_key) { this.scene_key = scene_key; }

    public String getQr_ticket() { return qr_ticket; }
    public void setQr_ticket(String qr_ticket) { this.qr_ticket = qr_ticket; }

    public String getQr_url() { return qr_url; }
    public void setQr_url(String qr_url) { this.qr_url = qr_url; }

    public Integer getExpire_seconds() { return expire_seconds; }
    public void setExpire_seconds(Integer expire_seconds) { this.expire_seconds = expire_seconds; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getBound_at() { return bound_at; }
    public void setBound_at(Date bound_at) { this.bound_at = bound_at; }

    public String getBound_openid() { return bound_openid; }
    public void setBound_openid(String bound_openid) { this.bound_openid = bound_openid; }

    public Date getCreated_at() { return created_at; }
    public void setCreated_at(Date created_at) { this.created_at = created_at; }

    public Date getUpdated_at() { return updated_at; }
    public void setUpdated_at(Date updated_at) { this.updated_at = updated_at; }
}
