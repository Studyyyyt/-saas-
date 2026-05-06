package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.LabBillConfirmRequest;
import com.example.springboot.entity.LabBillResolutionRequest;
import com.example.springboot.service.LabBillService;
import com.example.springboot.util.PagingSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin(origins = "http://localhost:7070")
@RestController
@RequestMapping("/lab-bills")
public class LabBillController {

    @Autowired
    private LabBillService labBillService;

    @GetMapping("/search")
    public Result search(@RequestParam(required = false) Long factoryId,
                         @RequestParam(required = false) String status,
                         @RequestParam(required = false) String billMonth,
                         @RequestParam(defaultValue = "1") int page,
                         @RequestParam(defaultValue = "20") int size) {
        try {
            return Result.success(PagingSupport.buildPageResult(labBillService.searchBills(factoryId, status, billMonth), page, size));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result detail(@PathVariable Long id) {
        try {
            return Result.success(labBillService.getBillDetail(id));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @PostMapping("/import")
    public Result importBill(@RequestParam("file") MultipartFile file,
                             @RequestParam Long factoryId,
                             @RequestParam String billMonth,
                             @RequestParam(required = false) Long templateId,
                             @RequestParam(required = false) Long importedBy,
                             @RequestParam(required = false) String importedByName,
                             @RequestParam String parsedItemsJson) {
        try {
            return Result.success(labBillService.importBill(file, factoryId, billMonth, templateId, importedBy, importedByName, parsedItemsJson));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @PutMapping("/items/{id}/resolution")
    public Result updateItemResolution(@PathVariable Long id, @RequestBody LabBillResolutionRequest request) {
        try {
            return Result.success(labBillService.updateBillItemResolution(id, request));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @PutMapping("/unmatched-orders/{id}/resolution")
    public Result updateUnmatchedResolution(@PathVariable Long id, @RequestBody LabBillResolutionRequest request) {
        try {
            return Result.success(labBillService.updateUnmatchedOrderResolution(id, request));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @PostMapping("/confirm/{id}")
    public Result confirm(@PathVariable Long id, @RequestBody(required = false) LabBillConfirmRequest request) {
        try {
            return Result.success(labBillService.confirmBill(id, request));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @GetMapping("/file/{id}")
    public ResponseEntity<Resource> file(@PathVariable Long id) {
        return labBillService.getBillFile(id);
    }
}
