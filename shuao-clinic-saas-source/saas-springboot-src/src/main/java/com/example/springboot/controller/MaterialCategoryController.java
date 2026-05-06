package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.MaterialCategory;
import com.example.springboot.service.MaterialCategoryService;
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
@RequestMapping("/material-categories")
public class MaterialCategoryController {

    @Autowired
    private MaterialCategoryService materialCategoryService;

    @GetMapping("/tree")
    public Result tree(@RequestParam(required = false, defaultValue = "false") boolean includeDisabled) {
        return Result.success(materialCategoryService.selectTree(includeDisabled));
    }

    @GetMapping("/selectEnabled")
    public Result selectEnabled() {
        return Result.success(materialCategoryService.selectAllActiveFlat());
    }

    @PostMapping("/add")
    public Result add(@RequestBody MaterialCategory item) {
        try {
            return Result.success(materialCategoryService.add(item));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @PutMapping("/edit")
    public Result edit(@RequestBody MaterialCategory item) {
        try {
            return Result.success(materialCategoryService.edit(item));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        try {
            materialCategoryService.delete(id);
            return Result.success("删除成功");
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }
}
