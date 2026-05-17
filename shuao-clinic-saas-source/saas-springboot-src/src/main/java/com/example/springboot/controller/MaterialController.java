package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.Material;
import com.example.springboot.service.MaterialService;
import com.example.springboot.util.PagingSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/materials")
public class MaterialController {

    @Autowired
    private MaterialService materialService;

    @GetMapping("/selectAll")
    public Result search(@RequestParam(required = false) String keyword,
                         @RequestParam(required = false) Long categoryId,
                         @RequestParam(required = false) Boolean lowStockOnly,
                         @RequestParam(required = false) String status,
                         @RequestParam(defaultValue = "1") int page,
                         @RequestParam(defaultValue = "20") int size) {
        try {
            return Result.success(PagingSupport.buildPageResult(materialService.search(keyword, categoryId, lowStockOnly, status), page, size));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @GetMapping("/searchLite")
    public Result searchLite(@RequestParam(required = false) String keyword,
                             @RequestParam(required = false) Long categoryId,
                             @RequestParam(required = false) Integer limit) {
        try {
            return Result.success(materialService.selectEnabled(keyword, categoryId, limit));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result selectById(@PathVariable Long id) {
        Material material = materialService.selectById(id);
        return material == null ? Result.error("耗材不存在") : Result.success(material);
    }

    @PostMapping("/add")
    public Result add(@RequestBody Material item) {
        try {
            return Result.success(materialService.add(item));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @PutMapping("/edit")
    public Result edit(@RequestBody Material item) {
        try {
            return Result.success(materialService.edit(item));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }
}
