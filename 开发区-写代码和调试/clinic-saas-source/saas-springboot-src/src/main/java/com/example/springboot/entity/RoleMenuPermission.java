package com.example.springboot.entity;

import lombok.Data;

import java.util.Date;

@Data
public class RoleMenuPermission {
    private Long id;
    private String roleCode;
    private String menuKey;
    private Integer enabled;
    private Date createdAt;
    private Date updatedAt;
}
