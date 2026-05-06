package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.MaterialPurchase;
import com.example.springboot.entity.MaterialPurchaseVoidRequest;
import com.example.springboot.service.MaterialPurchaseService;
import com.example.springboot.util.PagingSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin(origins = "http://localhost:7070")
@RestController
@RequestMapping("/material-purchases")
public class MaterialPurchaseController {

    @Autowired
    private MaterialPurchaseService materialPurchaseService;

    @GetMapping("/search")
    public Result search(@RequestParam(required = false) String supplierKeyword,
                         @RequestParam(required = false) String startDate,
                         @RequestParam(required = false) String endDate,
                         @RequestParam(required = false) String status,
                         @RequestParam(defaultValue = "1") int page,
                         @RequestParam(defaultValue = "20") int size) {
        try {
            return Result.success(PagingSupport.buildPageResult(materialPurchaseService.search(supplierKeyword, startDate, endDate, status), page, size));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result selectById(@PathVariable Long id) {
        MaterialPurchase purchase = materialPurchaseService.selectById(id);
        return purchase == null ? Result.error("采购单不存在") : Result.success(purchase);
    }

    @PostMapping("/add")
    public Result add(@RequestBody MaterialPurchase purchase) {
        try {
            return Result.success(materialPurchaseService.add(purchase));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @PutMapping("/edit")
    public Result edit(@RequestBody MaterialPurchase purchase) {
        try {
            return Result.success(materialPurchaseService.edit(purchase));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @PostMapping("/void/{id}")
    public Result voidPurchase(@PathVariable Long id, @RequestBody(required = false) MaterialPurchaseVoidRequest request) {
        try {
            return Result.success(materialPurchaseService.voidPurchase(id, request));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @PostMapping("/uploadInvoice")
    public Result uploadInvoice(@RequestParam("file") MultipartFile file) {
        try {
            return Result.success(materialPurchaseService.uploadInvoice(file));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @GetMapping("/invoice/{purchaseId}")
    public ResponseEntity<Resource> invoice(@PathVariable Long purchaseId) {
        return materialPurchaseService.getInvoiceFile(purchaseId);
    }
}
