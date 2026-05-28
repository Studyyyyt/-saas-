package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.TreatmentCatalog;
import com.example.springboot.service.TreatmentCatalogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/treatment-catalog")
public class TreatmentCatalogController {

    @Autowired
    private TreatmentCatalogService treatmentCatalogService;

    @GetMapping("/selectAll")
    public Result selectAll() {
        return Result.success(treatmentCatalogService.selectAll());
    }

    @GetMapping("/selectEnabled")
    public Result selectEnabled() {
        return Result.success(treatmentCatalogService.selectEnabled());
    }

    @PostMapping("/add")
    public Result add(@RequestBody TreatmentCatalog item) {
        if (item == null || item.getItem_name() == null || item.getItem_name().trim().isEmpty()) {
            return Result.error("项目名称不能为空");
        }
        treatmentCatalogService.add(item);
        return Result.success("新增成功");
    }

    @PutMapping("/edit")
    public Result edit(@RequestBody TreatmentCatalog item) {
        if (item == null || item.getId() == null) {
            return Result.error("项目ID不能为空");
        }
        if (item.getItem_name() == null || item.getItem_name().trim().isEmpty()) {
            return Result.error("项目名称不能为空");
        }
        treatmentCatalogService.edit(item);
        return Result.success("编辑成功");
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        treatmentCatalogService.delete(id);
        return Result.success("删除成功");
    }
}
