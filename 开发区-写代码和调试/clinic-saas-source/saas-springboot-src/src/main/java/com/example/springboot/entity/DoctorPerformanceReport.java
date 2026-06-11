package com.example.springboot.entity;

import lombok.Data;

import java.util.List;

@Data
public class DoctorPerformanceReport {
    private String start_date;
    private String end_date;
    private Integer doctor_count;
    private List<DoctorPerformanceStat> list;
    private DoctorPerformanceStat summary;
}
