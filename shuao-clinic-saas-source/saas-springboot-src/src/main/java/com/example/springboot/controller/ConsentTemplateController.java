package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.ConsentTemplate;
import com.example.springboot.service.ConsentTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/consent-template")
public class ConsentTemplateController {

    @Autowired
    private ConsentTemplateService consentTemplateService;

    @GetMapping("/selectAll")
    public Result selectAll() {
        return Result.success(consentTemplateService.selectAll());
    }

    @GetMapping("/selectEnabled")
    public Result selectEnabled() {
        return Result.success(consentTemplateService.selectEnabled());
    }

    @PostMapping("/add")
    public Result add(@RequestBody ConsentTemplate item) {
        if (item == null || item.getTitle() == null || item.getTitle().trim().isEmpty()) {
            return Result.error("模板标题不能为空");
        }
        if (item.getContent() == null || item.getContent().trim().isEmpty()) {
            return Result.error("模板正文不能为空");
        }
        consentTemplateService.add(item);
        return Result.success("新增成功");
    }

    @PutMapping("/edit")
    public Result edit(@RequestBody ConsentTemplate item) {
        if (item == null || item.getId() == null) {
            return Result.error("模板ID不能为空");
        }
        if (item.getTitle() == null || item.getTitle().trim().isEmpty()) {
            return Result.error("模板标题不能为空");
        }
        if (item.getContent() == null || item.getContent().trim().isEmpty()) {
            return Result.error("模板正文不能为空");
        }
        consentTemplateService.edit(item);
        return Result.success("编辑成功");
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        consentTemplateService.delete(id);
        return Result.success("删除成功");
    }
}
