package com.example.springboot.entity;

import lombok.Data;

import java.util.Date;

/**
 * 角色定义实体
 */
@Data
public class Role {
    private Long id;
    private String code;
    private String name;
    private String description;
    private Integer sortOrder;
    private Integer status;
    private Date createdAt;
    private Date updatedAt;
}
