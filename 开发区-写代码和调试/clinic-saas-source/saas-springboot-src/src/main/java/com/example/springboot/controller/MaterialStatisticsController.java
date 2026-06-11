package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.service.MaterialStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/material-statistics")
public class MaterialStatisticsController {

    @Autowired
    private MaterialStatisticsService materialStatisticsService;

    @GetMapping("/overview")
    public Result overview(@RequestParam(required = false) String rangePreset,
                           @RequestParam(required = false) String startDate,
                           @RequestParam(required = false) String endDate) {
        try {
            return Result.success(materialStatisticsService.overview(rangePreset, startDate, endDate));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }
}
