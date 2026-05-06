package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.TreatmentOperation;
import com.example.springboot.service.TreatmentOperationService;
import com.example.springboot.util.PagingSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/treatment-operations")
public class TreatmentOperationController {

    @Autowired
    private TreatmentOperationService operationService;

    @GetMapping("/search")
    public Result search(@RequestParam(required = false) String keyword,
                         @RequestParam(required = false) String category,
                         @RequestParam(required = false) Integer needLabProcessing,
                         @RequestParam(required = false) String status,
                         @RequestParam(defaultValue = "1") int page,
                         @RequestParam(defaultValue = "20") int size) {
        List<TreatmentOperation> rows = operationService.search(keyword, category, needLabProcessing, status);
        Map<String, Object> result = PagingSupport.buildPageResult(rows, page, size);
        return Result.success(result);
    }

    @GetMapping("/selectEnabled")
    public Result selectEnabled() {
        return Result.success(operationService.selectEnabled());
    }

    @GetMapping("/selectById")
    public Result selectById(@RequestParam Long id) {
        TreatmentOperation item = operationService.selectById(id);
        return item == null ? Result.error("操作不存在") : Result.success(item);
    }

    @PostMapping("/add")
    public Result add(@RequestBody TreatmentOperation item) {
        try {
            return Result.success(operationService.add(item));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @PutMapping("/edit")
    public Result edit(@RequestBody TreatmentOperation item) {
        try {
            return Result.success(operationService.edit(item));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        try {
            operationService.delete(id);
            return Result.success("删除成功");
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @PostMapping("/importBatch")
    public Result importBatch(@RequestBody List<TreatmentOperation> items) {
        try {
            int count = operationService.importBatch(items);
            return Result.success("导入成功，共" + count + "条");
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }
}
