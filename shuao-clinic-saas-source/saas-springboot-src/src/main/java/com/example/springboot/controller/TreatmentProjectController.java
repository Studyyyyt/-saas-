package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.TreatmentProject;
import com.example.springboot.service.TreatmentProjectService;
import com.example.springboot.util.PagingSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/treatment-projects")
public class TreatmentProjectController {

    @Autowired
    private TreatmentProjectService projectService;

    @GetMapping("/search")
    public Result search(@RequestParam(required = false) String keyword,
                         @RequestParam(required = false) Long categoryId,
                         @RequestParam(required = false) String status,
                         @RequestParam(defaultValue = "1") int page,
                         @RequestParam(defaultValue = "20") int size) {
        List<TreatmentProject> rows = projectService.search(keyword, categoryId, status);
        Map<String, Object> result = PagingSupport.buildPageResult(rows, page, size);
        return Result.success(result);
    }

    @GetMapping("/selectEnabled")
    public Result selectEnabled() {
        return Result.success(projectService.selectEnabled());
    }

    @GetMapping("/selectById")
    public Result selectById(@RequestParam Long id) {
        TreatmentProject item = projectService.selectById(id);
        return item == null ? Result.error("项目不存在") : Result.success(item);
    }

    @PostMapping("/add")
    public Result add(@RequestBody TreatmentProject item) {
        try {
            return Result.success(projectService.add(item));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @PutMapping("/edit")
    public Result edit(@RequestBody TreatmentProject item) {
        try {
            return Result.success(projectService.edit(item));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        try {
            projectService.delete(id);
            return Result.success("删除成功");
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @PostMapping("/importBatch")
    public Result importBatch(@RequestBody List<TreatmentProject> items) {
        try {
            int count = projectService.importBatch(items);
            return Result.success("导入成功，共" + count + "条");
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }
}
