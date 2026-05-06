package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.service.LabStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin(origins = "http://localhost:7070")
@RestController
@RequestMapping("/lab-statistics")
public class LabStatisticsController {

    @Autowired
    private LabStatisticsService labStatisticsService;

    @GetMapping("/overview")
    public Result overview(@RequestParam(required = false) String rangePreset,
                           @RequestParam(required = false) String startDate,
                           @RequestParam(required = false) String endDate) {
        return Result.success(labStatisticsService.buildOverview(rangePreset, startDate, endDate));
    }
}
