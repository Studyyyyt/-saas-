package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.Inventory;
import com.example.springboot.entity.Patient;
import com.example.springboot.entity.Purchase;
import com.example.springboot.service.PurchaseService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/purchases")
public class PurchaseController {

    @Autowired
    private PurchaseService purchaseService;

    @GetMapping("/selectAll")
    public Result getPurchaseList(@RequestParam int page, @RequestParam int size) {
        PageHelper.startPage(page, size);
        List<Purchase> PurchaseList = purchaseService.selectAll();
        PageInfo<Purchase> pageInfo = new PageInfo<>(PurchaseList);
        return Result.success(pageInfo);
    }

    @GetMapping("/selectByid")
    public Result selectById(@RequestParam Long id,@RequestParam int page, @RequestParam int size) {
        PageHelper.startPage(page, size);
        List<Purchase> PurchaseList = purchaseService.selectById(id);
        PageInfo<Purchase> pageInfo = new PageInfo<>(PurchaseList);
        return Result.success(pageInfo);
    }

    @GetMapping("/selectByname")
    public Result selectByProductName(@RequestParam String name,@RequestParam int page, @RequestParam int size) {
        PageHelper.startPage(page, size);
        List<Purchase> PurchaseList = purchaseService.selectByProductName(name);
        PageInfo<Purchase> pageInfo = new PageInfo<>(PurchaseList);
        return Result.success(pageInfo);
    }

    @GetMapping("/selectBycategory")
    public Result selectByCategory(@RequestParam String category,@RequestParam int page, @RequestParam int size) {
        PageHelper.startPage(page, size);
        List<Purchase> PurchaseList = purchaseService.selectByCategory(category);
        PageInfo<Purchase> pageInfo = new PageInfo<>(PurchaseList);
        return Result.success(pageInfo);
    }

    @GetMapping("/selectBybrand")
    public Result selectByBrand(@RequestParam String brand,@RequestParam int page, @RequestParam int size) {
        PageHelper.startPage(page, size);
        List<Purchase> PurchaseList = purchaseService.selectByBrand(brand);
        PageInfo<Purchase> pageInfo = new PageInfo<>(PurchaseList);
        return Result.success(pageInfo);
    }

    @GetMapping("/selectBysupplier")
    public Result selectBySupplier(@RequestParam String supplier,@RequestParam int page, @RequestParam int size) {
        PageHelper.startPage(page, size);
        List<Purchase> PurchaseList = purchaseService.selectBySupplier(supplier);
        PageInfo<Purchase> pageInfo = new PageInfo<>(PurchaseList);
        return Result.success(pageInfo);
    }
//    @GetMapping("/list")
//    public List<Purchase> getPurchaseList(@RequestParam int page, @RequestParam int size) {
//        return purchaseService.getPurchaseList(page, size);
//    }

//    @GetMapping("/search")
//    public List<Purchase> searchPurchase(@RequestParam String type, @RequestParam String keyword, @RequestParam int page, @RequestParam int size) {
//        return purchaseService.searchPurchase(type, keyword, page, size);
//    }


    @PostMapping("/add")
    public Result addPurchase(@RequestBody Purchase purchase) {
        purchaseService.addPurchase(purchase);
        return Result.success("新增成功");
    }

//    @PutMapping("/update")
//    public String updatePurchase(@RequestBody Purchase purchase) {
//        purchaseService.updatePurchase(purchase);
//        return "Update successful";
//    }

    @DeleteMapping("/delete/{id}")
    public Result deletePurchase(@PathVariable Long id) {
        purchaseService.deletePurchase(id);
        return Result.success("删除成功");
    }

    // 批量删除
    @DeleteMapping("/deleteBatch")
    public Result deletePurchaseBatch(@RequestBody List<Long> ids) {
        try {
            purchaseService.deletePurchaseBatch(ids);
            return Result.success("批量删除成功");
        } catch (Exception e) {
            return Result.error("批量删除失败：" + e.getMessage());
        }
    }

//    @GetMapping("/{id}")
//    public Purchase getPurchaseById(@PathVariable int id) {
//        return purchaseService.getPurchaseById(id);
//    }

//    @PutMapping("/updateStatus/{id}")
//    public Purchase updatePurchaseStatus(@PathVariable int id) {
//        return purchaseService.updatePurchaseStatus(id);
//    }

    @PutMapping("/updateStatus")
    public Result updatePurchaseStatus(@RequestBody Purchase purchase) {
        purchaseService.updatePurchaseStatus(purchase);
        return Result.success("编辑成功");
    }

}
