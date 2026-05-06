package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.LabOrder;
import com.example.springboot.entity.LabOrderBatchStatusUpdateRequest;
import com.example.springboot.service.LabOrderService;
import com.example.springboot.util.PagingSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:7070")
@RestController
@RequestMapping("/lab-orders")
public class LabOrderController {

    @Autowired
    private LabOrderService labOrderService;

    @GetMapping("/search")
    public Result search(@RequestParam(required = false) String keyword,
                         @RequestParam(required = false) Long factoryId,
                         @RequestParam(required = false) String status,
                         @RequestParam(required = false) Long patientId,
                         @RequestParam(required = false) String startDate,
                         @RequestParam(required = false) String endDate,
                         @RequestParam(defaultValue = "1") int page,
                         @RequestParam(defaultValue = "20") int size) {
        try {
            return Result.success(PagingSupport.buildPageResult(
                    labOrderService.searchOrders(keyword, factoryId, status, patientId, startDate, endDate),
                    page,
                    size
            ));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result selectById(@PathVariable Long id) {
        LabOrder order = labOrderService.selectById(id);
        return order == null ? Result.error("订单不存在") : Result.success(order);
    }

    @PostMapping("/add")
    public Result add(@RequestBody LabOrder item) {
        try {
            return Result.success(labOrderService.addOrder(item));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @PutMapping("/edit")
    public Result edit(@RequestBody LabOrder item) {
        try {
            return Result.success(labOrderService.editOrder(item));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @PostMapping("/batchStatus")
    public Result batchStatus(@RequestBody LabOrderBatchStatusUpdateRequest request) {
        try {
            labOrderService.batchUpdateStatus(request);
            return Result.success("批量更新成功");
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        try {
            labOrderService.deleteOrder(id);
            return Result.success("删除成功");
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }
}
