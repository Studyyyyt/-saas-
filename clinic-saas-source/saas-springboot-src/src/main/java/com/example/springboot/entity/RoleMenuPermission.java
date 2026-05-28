package com.example.springboot.entity;

import lombok.Data;

import java.util.Date;

@Data
public class RoleMenuPermission {
    private Long id;
    private String role_code;
    private String menu_key;
    private Integer enabled;
    private Date created_at;
    private Date updated_at;
}
