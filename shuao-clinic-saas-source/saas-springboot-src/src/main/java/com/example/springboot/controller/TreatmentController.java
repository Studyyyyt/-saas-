package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.Finance;
import com.example.springboot.entity.Treatment;
import com.example.springboot.entity.TreatmentBatchCreateRequest;
import com.example.springboot.entity.TreatmentBillingRequest;
import com.example.springboot.service.TreatmentService;
import com.example.springboot.service.TreatmentBillingService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/treatments")
public class TreatmentController {

    @Autowired
    private TreatmentService treatmentService;

    @Autowired
    private TreatmentBillingService treatmentBillingService;

    /**
     * 添加分页功能PageHelper
     */
    @GetMapping("/selectAll")
    public Result selectAll(@RequestParam int page, @RequestParam int size) {
        PageHelper.startPage(page, size);
        List<Treatment> treatmentList = treatmentService.selectAll();
        PageInfo<Treatment> pageInfo = new PageInfo<>(treatmentList);
        return Result.success(pageInfo);
    }

    @GetMapping("/recentByPatientId")
    public Result selectRecentByPatientId(@RequestParam Long patientId,
                                          @RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(treatmentService.selectRecentByPatientId(patientId, limit));
    }

    @GetMapping("/selectByid")
    public Result selectById(@RequestParam Long id,@RequestParam int page, @RequestParam int size) {
        PageHelper.startPage(page, size);
        List<Treatment> treatmentList = treatmentService.selectById(id);
        PageInfo<Treatment> pageInfo = new PageInfo<>(treatmentList);
        return Result.success(pageInfo);
    }

    @GetMapping("/selectByname")
    public Result selectByName(@RequestParam String name,@RequestParam int page, @RequestParam int size) {
        PageHelper.startPage(page, size);
        List<Treatment> treatmentList = treatmentService.selectByName(name);
        PageInfo<Treatment> pageInfo = new PageInfo<>(treatmentList);
        return Result.success(pageInfo);
    }

    @PostMapping("/add")
    public Result addTreatment(@RequestBody Treatment treatment) {
        if (treatment.getPatient_id() == null || treatment.getPatient_id() <= 0) {
            return Result.error("患者ID不能为空");
        }
        if (isBlank(treatment.getPatient_name())) {
            return Result.error("患者姓名不能为空");
        }
        if (isBlank(treatment.getAppointment_purpose()) && (treatment.getProject_id() == null || treatment.getProject_id() <= 0)) {
            return Result.error("预约目的不能为空");
        }
        if ((treatment.getDoctor_account_id() == null || treatment.getDoctor_account_id() <= 0)
                && isBlank(treatment.getDoctor_name())) {
            return Result.error("医生不能为空");
        }
        if (treatment.getTreatment_date() == null) {
            return Result.error("治疗日期不能为空");
        }
        if (!isBlank(treatment.getTreatment_content()) && isBlank(treatment.getTooth_positions())) {
            return Result.error("请选择牙位");
        }
        try {
            treatmentService.addTreatment(treatment);
            if (treatmentBillingService != null) {
                treatmentBillingService.enrichTreatments(List.of(treatment));
            }
            return Result.success(treatment);
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @PostMapping("/batchAdd")
    public Result batchAddTreatment(@RequestBody TreatmentBatchCreateRequest request) {
        try {
            List<Treatment> treatments = treatmentService.addTreatmentsBatch(request);
            if (treatmentBillingService != null) {
                treatmentBillingService.enrichTreatments(treatments);
            }
            return Result.success(treatments);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/edit")
    public Result editTreatment(@RequestBody Treatment treatment) {
        if (treatment.getId() == null) {
            return Result.error("治疗记录ID不能为空");
        }
        if (isBlank(treatment.getPatient_name())) {
            return Result.error("患者姓名不能为空");
        }
        if (isBlank(treatment.getAppointment_purpose()) && (treatment.getProject_id() == null || treatment.getProject_id() <= 0)) {
            return Result.error("预约目的不能为空");
        }
        if ((treatment.getDoctor_account_id() == null || treatment.getDoctor_account_id() <= 0)
                && isBlank(treatment.getDoctor_name())) {
            return Result.error("医生不能为空");
        }
        try {
            treatmentService.editTreatment(treatment);
            return Result.success("编辑成功");
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @PostMapping("/charge/{id}")
    public Result chargeTreatment(@PathVariable Long id,
                                  @RequestBody(required = false) TreatmentBillingRequest request) {
        try {
            Finance finance = treatmentBillingService.chargeTreatment(id, request);
            return Result.success(finance);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/chargeBatch/{batchNo}")
    public Result chargeTreatmentBatch(@PathVariable String batchNo,
                                       @RequestBody(required = false) TreatmentBillingRequest request) {
        try {
            List<Finance> finances = treatmentBillingService.chargeTreatmentBatch(batchNo, request);
            return Result.success(finances);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/refund/{id}")
    public Result refundTreatment(@PathVariable Long id,
                                  @RequestBody(required = false) TreatmentBillingRequest request) {
        try {
            Finance finance = treatmentBillingService.refundTreatment(id, request);
            return Result.success(finance);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public Result deleteTreatment(@PathVariable Long id) {
        treatmentService.deleteTreatment(id);
        return Result.success("删除成功");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
