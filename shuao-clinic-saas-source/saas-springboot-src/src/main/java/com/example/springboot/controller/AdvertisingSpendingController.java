package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.AdvertisingSpending;
import com.example.springboot.service.AdvertisingSpendingService;
import com.example.springboot.util.PagingSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:7070")
@RestController
@RequestMapping("/advertising-spending")
public class AdvertisingSpendingController {

    @Autowired
    private AdvertisingSpendingService advertisingSpendingService;

    @GetMapping("/search")
    public Result search(@RequestParam(required = false) String platform,
                         @RequestParam(required = false) String keyword,
                         @RequestParam(required = false) String startDate,
                         @RequestParam(required = false) String endDate,
                         @RequestParam(required = false) Long createdBy,
                         @RequestParam(defaultValue = "1") int page,
                         @RequestParam(defaultValue = "20") int size) {
        try {
            return Result.success(PagingSupport.buildPageResult(
                    advertisingSpendingService.search(platform, keyword, startDate, endDate, createdBy),
                    page,
                    size
            ));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @PostMapping("/add")
    public Result add(@RequestBody AdvertisingSpending spending) {
        try {
            return Result.success(advertisingSpendingService.add(spending));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @PutMapping("/edit")
    public Result edit(@RequestBody AdvertisingSpending spending) {
        try {
            return Result.success(advertisingSpendingService.edit(spending));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        try {
            advertisingSpendingService.delete(id);
            return Result.success("删除成功");
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @GetMapping("/dashboard/overview")
    public Result overview(@RequestParam(required = false) String startDate,
                           @RequestParam(required = false) String endDate) {
        try {
            return Result.success(advertisingSpendingService.buildOverview(startDate, endDate));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }
}
