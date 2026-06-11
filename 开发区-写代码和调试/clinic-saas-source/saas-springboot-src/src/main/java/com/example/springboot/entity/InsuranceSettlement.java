package com.example.springboot.entity;

import java.util.Date;

public class InsuranceSettlement {
    private Long id;
    private Long patient_id;
    private Integer finance_id;
    private Long treatment_id;
    private String settlement_no;
    private String visit_no;
    private String biz_type;
    private String settlement_status;
    private Double total_amount;
    private Double insurance_amount;
    private Double personal_amount;
    private Double cash_amount;
    private String upload_status;
    private String upload_payload;
    private String response_payload;
    private String remark;
    private Date settlement_time;
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

    public Integer getFinance_id() {
        return finance_id;
    }

    public void setFinance_id(Integer finance_id) {
        this.finance_id = finance_id;
    }

    public Long getTreatment_id() {
        return treatment_id;
    }

    public void setTreatment_id(Long treatment_id) {
        this.treatment_id = treatment_id;
    }

    public String getSettlement_no() {
        return settlement_no;
    }

    public void setSettlement_no(String settlement_no) {
        this.settlement_no = settlement_no;
    }

    public String getVisit_no() {
        return visit_no;
    }

    public void setVisit_no(String visit_no) {
        this.visit_no = visit_no;
    }

    public String getBiz_type() {
        return biz_type;
    }

    public void setBiz_type(String biz_type) {
        this.biz_type = biz_type;
    }

    public String getSettlement_status() {
        return settlement_status;
    }

    public void setSettlement_status(String settlement_status) {
        this.settlement_status = settlement_status;
    }

    public Double getTotal_amount() {
        return total_amount;
    }

    public void setTotal_amount(Double total_amount) {
        this.total_amount = total_amount;
    }

    public Double getInsurance_amount() {
        return insurance_amount;
    }

    public void setInsurance_amount(Double insurance_amount) {
        this.insurance_amount = insurance_amount;
    }

    public Double getPersonal_amount() {
        return personal_amount;
    }

    public void setPersonal_amount(Double personal_amount) {
        this.personal_amount = personal_amount;
    }

    public Double getCash_amount() {
        return cash_amount;
    }

    public void setCash_amount(Double cash_amount) {
        this.cash_amount = cash_amount;
    }

    public String getUpload_status() {
        return upload_status;
    }

    public void setUpload_status(String upload_status) {
        this.upload_status = upload_status;
    }

    public String getUpload_payload() {
        return upload_payload;
    }

    public void setUpload_payload(String upload_payload) {
        this.upload_payload = upload_payload;
    }

    public String getResponse_payload() {
        return response_payload;
    }

    public void setResponse_payload(String response_payload) {
        this.response_payload = response_payload;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Date getSettlement_time() {
        return settlement_time;
    }

    public void setSettlement_time(Date settlement_time) {
        this.settlement_time = settlement_time;
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
