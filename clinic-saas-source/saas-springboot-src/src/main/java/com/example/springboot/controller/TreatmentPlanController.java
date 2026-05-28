package com.example.springboot.controller;


import com.example.springboot.common.Result;
import com.example.springboot.entity.Treatment_plans;
import com.example.springboot.service.TreatmentService;
import com.example.springboot.service.Treatment_plansService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/treatment-plans")
public class TreatmentPlanController {

    @Autowired
    private Treatment_plansService treatment_plansService;

    @GetMapping("/selectAll")
    public Result selectAll() {
        List<Treatment_plans> treatment_plans = treatment_plansService.selectAll();
        return Result.success(treatment_plans);
    }

}
