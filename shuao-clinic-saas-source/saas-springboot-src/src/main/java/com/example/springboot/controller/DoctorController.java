package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.Doctor;
import com.example.springboot.service.DoctorService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:7070")
@RestController
@RequestMapping("/doctors")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    @GetMapping("/selectAll")
    public Result selectAll(@RequestParam int page, @RequestParam int size) {
        PageHelper.startPage(page, size);
        List<Doctor> doctorList = doctorService.selectAll();
        PageInfo<Doctor> pageInfo = new PageInfo<>(doctorList);
        return Result.success(pageInfo);
    }

    @GetMapping("/scheduleEntries")
    public Result scheduleEntries() {
        return Result.success(doctorService.selectAll());
    }

    @GetMapping("/selectByid")
    public Result selectById(@RequestParam Long id,@RequestParam int page, @RequestParam int size) {
        PageHelper.startPage(page, size);
        List<Doctor> doctorsList = doctorService.selectById(id);
        PageInfo<Doctor> pageInfo = new PageInfo<>(doctorsList);
        return Result.success(pageInfo);
    }

//    @GetMapping("/selectByid")
//    public Result selectById(@RequestParam Long id, @RequestParam int page, @RequestParam int size, @RequestParam(required = false) String status) {
//        PageHelper.startPage(page, size);
//        List<Doctor> doctorsList = (List<Doctor>) doctorService.getDoctorById(id);
//        PageInfo<Doctor> pageInfo = new PageInfo<>(doctorsList);
//        return Result.success(pageInfo);
//    }

    @GetMapping("/selectByname")
    public Result selectByName(@RequestParam String name, @RequestParam int page, @RequestParam int size, @RequestParam(required = false) String status) {
        PageHelper.startPage(page, size);
        List<Doctor> doctors;
        if (status != null && !status.isEmpty()) {
            doctors = doctorService.getDoctorsByNameAndStatus(name, status);
        } else {
            doctors = doctorService.getDoctorsByName(name);
        }
        PageInfo<Doctor> pageInfo = new PageInfo<>(doctors);
        return Result.success(pageInfo);
    }

    @PutMapping("/updateStatus/{id}")
    public Result updateStatus(@PathVariable Long id, @RequestBody Doctor doctor) {
        doctorService.updateDoctorStatus(id, doctor.getStatus());
        return Result.success("状态更新成功");
    }

    @PostMapping("/add")
    public Result addDoctor(@RequestBody Doctor doctor) {
        doctorService.addDoctor(doctor);
        return Result.success("新增成功");
    }


    @PutMapping("/edit")
    public Result editDoctor(@RequestBody Doctor doctor) {
        doctorService.updateDoctor(doctor);
        return Result.success("编辑成功");
    }

    @DeleteMapping("/delete/{id}")
    public Result deleteDoctor(@PathVariable Long id) {
        doctorService.deleteDoctorById(id);
        return Result.success("删除成功");
    }

    // 批量删除
    @DeleteMapping("/deleteBatch")
    public Result deleteDoctorBatch(@RequestBody List<Long> ids) {
        try {
            doctorService.deleteDoctorsByIds(ids);
            return Result.success("批量删除成功");
        } catch (Exception e) {
            return Result.error("批量删除失败：" + e.getMessage());
        }
    }

    // 按日期范围查询排班
    @GetMapping("/schedules")
    public Result getSchedulesByDateRange(@RequestParam String startDate, @RequestParam String endDate) {
        return Result.success(doctorService.getSchedulesByDateRange(startDate, endDate));
    }

    // 批量保存排班
    @PostMapping("/batchSave")
    public Result batchSaveSchedules(@RequestBody List<Doctor> schedules) {
        try {
            doctorService.batchSaveSchedules(schedules);
            return Result.success("批量保存成功");
        } catch (Exception e) {
            return Result.error("批量保存失败：" + e.getMessage());
        }
    }
}
