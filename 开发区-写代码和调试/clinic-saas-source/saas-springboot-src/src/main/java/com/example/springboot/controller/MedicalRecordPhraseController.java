package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.MedicalRecordPhrase;
import com.example.springboot.service.MedicalRecordPhraseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/medical-record-phrases")
public class MedicalRecordPhraseController {

    private final MedicalRecordPhraseService service;

    @Autowired
    public MedicalRecordPhraseController(MedicalRecordPhraseService service) {
        this.service = service;
    }

    @GetMapping("/selectByFieldType")
    public Result selectByFieldType(@RequestParam String fieldType) {
        return Result.success(service.selectByFieldType(fieldType));
    }

    @GetMapping("/selectAll")
    public Result selectAll() {
        return Result.success(service.selectAll());
    }

    @PostMapping("/add")
    public Result add(@RequestBody MedicalRecordPhrase item) {
        try {
            return Result.success(service.add(item));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @PutMapping("/edit")
    public Result edit(@RequestBody MedicalRecordPhrase item) {
        try {
            return Result.success(service.edit(item));
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
