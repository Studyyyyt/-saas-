package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.TreatmentProjectCategory;
import com.example.springboot.service.TreatmentProjectCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/treatment-project-categories")
public class TreatmentProjectCategoryController {

    @Autowired
    private TreatmentProjectCategoryService categoryService;

    @GetMapping("/tree")
    public Result tree(@RequestParam(required = false, defaultValue = "false") boolean includeDisabled) {
        return Result.success(categoryService.selectTree(includeDisabled));
    }

    @GetMapping("/selectEnabled")
    public Result selectEnabled() {
        return Result.success(categoryService.selectAllActiveFlat());
    }

    @PostMapping("/add")
    public Result add(@RequestBody TreatmentProjectCategory item) {
        try {
            return Result.success(categoryService.add(item));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @PutMapping("/edit")
    public Result edit(@RequestBody TreatmentProjectCategory item) {
        try {
            return Result.success(categoryService.edit(item));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        try {
            categoryService.delete(id);
            return Result.success("删除成功");
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }
}
