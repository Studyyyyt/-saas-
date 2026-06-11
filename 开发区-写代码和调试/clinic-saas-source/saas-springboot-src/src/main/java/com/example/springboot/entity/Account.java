package com.example.springboot.entity;

public class Account {
    private int id;
    private String username;
    private String password;
    private String name;
    private String role;
    private String avatar;
    private String wechat_openid;

    /** 诊所角色分配列表（仅用于前端交互，非数据库字段） */
    private java.util.List<java.util.Map<String, Object>> clinicRoles;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getWechat_openid() {
        return wechat_openid;
    }

    public void setWechat_openid(String wechat_openid) {
        this.wechat_openid = wechat_openid;
    }

    public java.util.List<java.util.Map<String, Object>> getClinicRoles() {
        return clinicRoles;
    }

    public void setClinicRoles(java.util.List<java.util.Map<String, Object>> clinicRoles) {
        this.clinicRoles = clinicRoles;
    }
}
