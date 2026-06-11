package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.MedicalRecordOperation;
import com.example.springboot.service.MedicalRecordOperationService;
import com.example.springboot.util.PagingSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/medical-record-operations")
public class MedicalRecordOperationController {

    @Autowired
    private MedicalRecordOperationService operationService;

    @GetMapping("/selectByMedicalRecordId")
    public Result selectByMedicalRecordId(@RequestParam Long medicalRecordId) {
        return Result.success(operationService.selectByMedicalRecordId(medicalRecordId));
    }

    @GetMapping("/pendingLabList")
    public Result pendingLabList(@RequestParam(required = false) Long patientId,
                                 @RequestParam(required = false) Long doctorAccountId,
                                 @RequestParam(defaultValue = "1") int page,
                                 @RequestParam(defaultValue = "20") int size) {
        List<MedicalRecordOperation> rows = operationService.searchPendingLabList(patientId, doctorAccountId);
        Map<String, Object> result = new LinkedHashMap<>(PagingSupport.buildPageResult(rows, page, size));
        result.put("pendingTotal", rows.size());
        return Result.success(result);
    }

    @PutMapping("/markSkip")
    public Result markSkip(@RequestBody MedicalRecordOperation item) {
        try {
            if (item == null || item.getId() == null || item.getId() <= 0) {
                return Result.error("病历操作ID不能为空");
            }
            return Result.success(operationService.markSkip(item.getId(), item.getSkip_reason(), item.getUpdated_by(), item.getUpdated_by_name()));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }
}
