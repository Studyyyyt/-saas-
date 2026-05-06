package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.PatientCustomGroup;
import com.example.springboot.entity.PatientCustomGroupAssignRequest;
import com.example.springboot.service.PatientCustomGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:7070")
@RestController
@RequestMapping("/patient-groups")
public class PatientCustomGroupController {

    @Autowired
    private PatientCustomGroupService service;

    @PostMapping("/add")
    public Result add(@RequestBody PatientCustomGroup group) {
        try {
            return Result.success(service.create(group));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @PostMapping("/assign")
    public Result assign(@RequestBody PatientCustomGroupAssignRequest request) {
        try {
            service.assignPatients(request == null ? null : request.getGroup_id(),
                    request == null ? null : request.getPatient_ids());
            return Result.success("分组分配成功");
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }
}
