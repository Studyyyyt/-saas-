package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.LabBillTemplate;
import com.example.springboot.entity.LabFactory;
import com.example.springboot.entity.LabFactoryProduct;
import com.example.springboot.service.LabFactoryService;
import com.example.springboot.util.PagingSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:7070")
@RestController
@RequestMapping("/lab-factories")
public class LabFactoryController {

    @Autowired
    private LabFactoryService labFactoryService;

    @GetMapping("/dashboard/overview")
    public Result overview() {
        return Result.success(labFactoryService.buildOverview());
    }

    @GetMapping("/search")
    public Result search(@RequestParam(required = false) String keyword,
                         @RequestParam(required = false) String status,
                         @RequestParam(defaultValue = "1") int page,
                         @RequestParam(defaultValue = "20") int size) {
        try {
            return Result.success(PagingSupport.buildPageResult(labFactoryService.searchFactories(keyword, status), page, size));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @GetMapping("/selectEnabled")
    public Result selectEnabled() {
        return Result.success(labFactoryService.selectEnabled());
    }

    @GetMapping("/{id}")
    public Result selectById(@PathVariable Long id) {
        LabFactory factory = labFactoryService.selectById(id);
        return factory == null ? Result.error("加工厂不存在") : Result.success(factory);
    }

    @PostMapping("/add")
    public Result add(@RequestBody LabFactory item) {
        try {
            return Result.success(labFactoryService.addFactory(item));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @PutMapping("/edit")
    public Result edit(@RequestBody LabFactory item) {
        try {
            return Result.success(labFactoryService.editFactory(item));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        try {
            labFactoryService.deleteFactory(id);
            return Result.success("删除成功");
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @GetMapping("/{factoryId}/products")
    public Result selectProducts(@PathVariable Long factoryId,
                                 @RequestParam(required = false, defaultValue = "false") boolean enabledOnly) {
        try {
            return Result.success(labFactoryService.selectProducts(factoryId, enabledOnly));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @PostMapping("/{factoryId}/products/add")
    public Result addProduct(@PathVariable Long factoryId, @RequestBody LabFactoryProduct item) {
        try {
            return Result.success(labFactoryService.addProduct(factoryId, item));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @PutMapping("/{factoryId}/products/edit")
    public Result editProduct(@PathVariable Long factoryId, @RequestBody LabFactoryProduct item) {
        try {
            return Result.success(labFactoryService.editProduct(factoryId, item));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @PostMapping("/{factoryId}/products/batchSave")
    public Result batchSaveProducts(@PathVariable Long factoryId, @RequestBody List<LabFactoryProduct> items) {
        try {
            labFactoryService.batchSaveProducts(factoryId, items);
            return Result.success("保存成功");
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @DeleteMapping("/{factoryId}/products/delete/{productId}")
    public Result deleteProduct(@PathVariable Long factoryId, @PathVariable Long productId) {
        try {
            labFactoryService.deleteProduct(factoryId, productId);
            return Result.success("删除成功");
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @GetMapping("/{factoryId}/templates")
    public Result selectTemplates(@PathVariable Long factoryId) {
        try {
            return Result.success(labFactoryService.selectTemplates(factoryId));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @PostMapping("/{factoryId}/templates/add")
    public Result addTemplate(@PathVariable Long factoryId, @RequestBody LabBillTemplate item) {
        try {
            return Result.success(labFactoryService.addTemplate(factoryId, item));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @PutMapping("/{factoryId}/templates/edit")
    public Result editTemplate(@PathVariable Long factoryId, @RequestBody LabBillTemplate item) {
        try {
            return Result.success(labFactoryService.editTemplate(factoryId, item));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @DeleteMapping("/{factoryId}/templates/delete/{templateId}")
    public Result deleteTemplate(@PathVariable Long factoryId, @PathVariable Long templateId) {
        try {
            labFactoryService.deleteTemplate(factoryId, templateId);
            return Result.success("删除成功");
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }
}
