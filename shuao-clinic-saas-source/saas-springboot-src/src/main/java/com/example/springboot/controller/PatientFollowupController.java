package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.PatientFollowup;
import com.example.springboot.service.PatientFollowupService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "http://localhost:7070")
@RestController
@RequestMapping("/followup")
public class PatientFollowupController {

    @Autowired
    private PatientFollowupService service;

    @GetMapping("/selectAll")
    public Result selectAll(@RequestParam int page, @RequestParam int size) {
        PageHelper.startPage(page, size);
        return Result.success(new PageInfo<>(service.selectAll()));
    }

    @GetMapping("/selectAllDetail")
    public Result selectAllDetail() {
        return Result.success(service.selectAllDetail());
    }

    @GetMapping("/selectByPatientId")
    public Result selectByPatientId(@RequestParam Long patientId) {
        return Result.success(service.selectByPatientId(patientId));
    }

    @PostMapping("/add")
    public Result add(@RequestBody PatientFollowup f) {
        try {
            service.add(f);
            return Result.success("新增成功");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/edit")
    public Result edit(@RequestBody PatientFollowup f) {
        try {
            service.update(f);
            return Result.success("编辑成功");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        service.delete(id);
        return Result.success("删除成功");
    }
}
