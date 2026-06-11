package com.example.springboot.entity;

import java.time.LocalDateTime;

/**
 * 诊所实体类
 */
public class Clinic {

    /** 诊所唯一标识，支持自定义（如拼音缩写） */
    private String id;

    /** 诊所名称 */
    private String name;

    /** 地址 */
    private String address;

    /** 联系电话 */
    private String contactPhone;

    /** 状态：0=禁用，1=启用 */
    private Integer status;

    /** 绑定的激活码 */
    private String activationCode;

    /** 授权过期时间 */
    private LocalDateTime licenseExpiresAt;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** 创建者用户ID（仅用于请求传递，不持久化到数据库） */
    private Integer creatorUserId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getActivationCode() {
        return activationCode;
    }

    public void setActivationCode(String activationCode) {
        this.activationCode = activationCode;
    }

    public LocalDateTime getLicenseExpiresAt() {
        return licenseExpiresAt;
    }

    public void setLicenseExpiresAt(LocalDateTime licenseExpiresAt) {
        this.licenseExpiresAt = licenseExpiresAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getCreatorUserId() {
        return creatorUserId;
    }

    public void setCreatorUserId(Integer creatorUserId) {
        this.creatorUserId = creatorUserId;
    }
}
