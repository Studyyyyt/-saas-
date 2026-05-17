package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.PatientRiskTag;
import com.example.springboot.service.PatientRiskTagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/risk-tags")
public class PatientRiskTagController {

    @Autowired
    private PatientRiskTagService service;

    @GetMapping("/selectByPatientId")
    public Result selectByPatientId(@RequestParam Long patientId) {
        return Result.success(service.selectActiveByPatientId(patientId));
    }

    @PostMapping("/add")
    public Result add(@RequestBody PatientRiskTag t) {
        t.setStatus(1);
        service.add(t);
        return Result.success("新增成功");
    }

    @PutMapping("/edit")
    public Result edit(@RequestBody PatientRiskTag t) {
        service.update(t);
        return Result.success("编辑成功");
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        service.delete(id);
        return Result.success("删除成功");
    }
}
