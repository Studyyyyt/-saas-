package com.example.springboot.entity;

import lombok.Data;

import java.util.Date;

@Data
public class Purchase {
    private int id;//序号
    private String product_name;  //产品名称
    private String category;      //类别
    private String brand;         //品牌
    private String supplier;      //供应商
    private String specification; //规格
    private String unit;          //单位
    private int quantity;         //数量
    private String price;         //价格
    private String status;         //状态
    private Date createdate;       //创建日期
    private Date purchasedate;     //购买日期
    private Date indate;           //入库日期
}
