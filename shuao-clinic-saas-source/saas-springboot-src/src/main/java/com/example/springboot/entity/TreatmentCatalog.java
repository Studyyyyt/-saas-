package com.example.springboot.entity;

import java.util.Date;

public class TreatmentCatalog {
    private Long id;
    private String item_name;
    private String default_fee;
    private String default_content;
    private String default_product;
    private Integer status;
    private Integer sort_order;
    private String medical_insurance_code;
    private String medical_insurance_name;
    private String medical_insurance_category;
    private Double self_pay_ratio;
    private Date created_at;
    private Date updated_at;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getItem_name() { return item_name; }
    public void setItem_name(String item_name) { this.item_name = item_name; }
    public String getDefault_fee() { return default_fee; }
    public void setDefault_fee(String default_fee) { this.default_fee = default_fee; }
    public String getDefault_content() { return default_content; }
    public void setDefault_content(String default_content) { this.default_content = default_content; }
    public String getDefault_product() { return default_product; }
    public void setDefault_product(String default_product) { this.default_product = default_product; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Integer getSort_order() { return sort_order; }
    public void setSort_order(Integer sort_order) { this.sort_order = sort_order; }
    public String getMedical_insurance_code() { return medical_insurance_code; }
    public void setMedical_insurance_code(String medical_insurance_code) { this.medical_insurance_code = medical_insurance_code; }
    public String getMedical_insurance_name() { return medical_insurance_name; }
    public void setMedical_insurance_name(String medical_insurance_name) { this.medical_insurance_name = medical_insurance_name; }
    public String getMedical_insurance_category() { return medical_insurance_category; }
    public void setMedical_insurance_category(String medical_insurance_category) { this.medical_insurance_category = medical_insurance_category; }
    public Double getSelf_pay_ratio() { return self_pay_ratio; }
    public void setSelf_pay_ratio(Double self_pay_ratio) { this.self_pay_ratio = self_pay_ratio; }
    public Date getCreated_at() { return created_at; }
    public void setCreated_at(Date created_at) { this.created_at = created_at; }
    public Date getUpdated_at() { return updated_at; }
    public void setUpdated_at(Date updated_at) { this.updated_at = updated_at; }
}
