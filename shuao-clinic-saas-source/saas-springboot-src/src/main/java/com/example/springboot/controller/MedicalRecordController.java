package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.MedicalRecord;
import com.example.springboot.service.MedicalRecordService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "http://localhost:7070")
@RestController
@RequestMapping("/medical-records")
public class MedicalRecordController {

    @Autowired
    private MedicalRecordService service;

    @GetMapping("/selectAll")
    public Result selectAll(@RequestParam int page, @RequestParam int size) {
        PageHelper.startPage(page, size);
        List<MedicalRecord> list = service.selectAll();
        return Result.success(new PageInfo<>(list));
    }

    @GetMapping("/selectByPatientId")
    public Result selectByPatientId(@RequestParam Long patientId,
                                     @RequestParam int page, @RequestParam int size) {
        PageHelper.startPage(page, size);
        List<MedicalRecord> list = service.selectByPatientId(patientId);
        return Result.success(new PageInfo<>(list));
    }

    @GetMapping("/selectByPatientName")
    public Result selectByPatientName(@RequestParam String name,
                                       @RequestParam int page, @RequestParam int size) {
        PageHelper.startPage(page, size);
        List<MedicalRecord> list = service.selectByPatientName(name);
        return Result.success(new PageInfo<>(list));
    }

    @GetMapping("/selectById")
    public Result selectById(@RequestParam Long id) {
        return Result.success(service.selectById(id));
    }

    @PostMapping("/add")
    public Result add(@RequestBody MedicalRecord record) {
        try {
            service.add(record);
            return Result.success(record);
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @PutMapping("/edit")
    public Result edit(@RequestBody MedicalRecord record) {
        try {
            service.update(record);
            return Result.success(record);
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        service.delete(id);
        return Result.success("删除成功");
    }
}
