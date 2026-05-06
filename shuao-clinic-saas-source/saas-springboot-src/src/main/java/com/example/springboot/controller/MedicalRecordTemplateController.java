package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.MedicalRecordTemplate;
import com.example.springboot.service.MedicalRecordTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/medical-record-templates")
public class MedicalRecordTemplateController {

    @Autowired
    private MedicalRecordTemplateService medicalRecordTemplateService;

    @GetMapping("/selectAll")
    public Result selectAll() {
        return Result.success(medicalRecordTemplateService.selectAll());
    }

    @GetMapping("/selectEnabled")
    public Result selectEnabled() {
        return Result.success(medicalRecordTemplateService.selectEnabled());
    }

    @PostMapping("/add")
    public Result add(@RequestBody MedicalRecordTemplate item) {
        try {
            return Result.success(medicalRecordTemplateService.add(item));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @PutMapping("/edit")
    public Result edit(@RequestBody MedicalRecordTemplate item) {
        try {
            return Result.success(medicalRecordTemplateService.edit(item));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        medicalRecordTemplateService.delete(id);
        return Result.success("删除成功");
    }
}
