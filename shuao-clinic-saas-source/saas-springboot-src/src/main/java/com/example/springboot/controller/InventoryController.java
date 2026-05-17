package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.Inventory;
import com.example.springboot.service.InventoryService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;
    /**
     * 查询全部用户信息
     */
    @GetMapping("/selectAll1")
    public Result selectAll() {
        List<Inventory> InventoryList = inventoryService.selectAll();
        return Result.success(InventoryList);
    }
    /**
     * 添加分页功能PageHelper
     */
    @GetMapping("/selectAll")
    public Result selectAll(@RequestParam int page, @RequestParam int size) {
        PageHelper.startPage(page, size);
        List<Inventory> inventoryList = inventoryService.selectAll();
        PageInfo<Inventory> pageInfo = new PageInfo<>(inventoryList);
        return Result.success(pageInfo);
    }


//    @GetMapping("/selectByid")
//    public Result selectById(@RequestParam Long id) {
//        List<Inventory> InventoryList = inventoryService.selectById(id);
//        return Result.success(InventoryList);
//    }
    @GetMapping("/selectByid")
    public Result selectById(@RequestParam Long id,@RequestParam int page, @RequestParam int size) {
        PageHelper.startPage(page, size);
        List<Inventory> inventoryList = inventoryService.selectById(id);
        PageInfo<Inventory> pageInfo = new PageInfo<>(inventoryList);
        return Result.success(pageInfo);
    }

//    @GetMapping("/selectByname")
//    public Result selectByProductName(@RequestParam String name) {
//        List<Inventory> InventoryList = inventoryService.selectByProductName(name);
//        return Result.success(InventoryList);
//    }
    @GetMapping("/selectByname")
    public Result selectByProductName(@RequestParam String name,@RequestParam int page, @RequestParam int size) {
        PageHelper.startPage(page, size);
        List<Inventory> inventoryList = inventoryService.selectByProductName(name);
        PageInfo<Inventory> pageInfo = new PageInfo<>(inventoryList);
        return Result.success(pageInfo);
    }

//    @GetMapping("/selectBycategory")
//    public Result selectByCategory(@RequestParam String category) {
//        List<Inventory> InventoryList = inventoryService.selectByCategory(category);
//        return Result.success(InventoryList);
//    }
    @GetMapping("/selectBycategory")
    public Result selectByCategory(@RequestParam String category,@RequestParam int page, @RequestParam int size) {
        PageHelper.startPage(page, size);
        List<Inventory> inventoryList = inventoryService.selectByCategory(category);
        PageInfo<Inventory> pageInfo = new PageInfo<>(inventoryList);
        return Result.success(pageInfo);
    }

//    @GetMapping("/selectBybrand")
//    public Result selectByBrand(@RequestParam String brand) {
//        List<Inventory> InventoryList = inventoryService.selectByBrand(brand);
//        return Result.success(InventoryList);
//    }
    @GetMapping("/selectBybrand")
    public Result selectByBrand(@RequestParam String brand,@RequestParam int page, @RequestParam int size) {
        PageHelper.startPage(page, size);
        List<Inventory> inventoryList = inventoryService.selectByBrand(brand);
        PageInfo<Inventory> pageInfo = new PageInfo<>(inventoryList);
        return Result.success(pageInfo);
    }

//    @GetMapping("/selectBysupplier")
//    public Result selectBySupplier(@RequestParam String supplier) {
//        List<Inventory> InventoryList = inventoryService.selectBySupplier(supplier);
//        return Result.success(InventoryList);
//    }
    @GetMapping("/selectBysupplier")
    public Result selectBySupplier(@RequestParam String supplier,@RequestParam int page, @RequestParam int size) {
        PageHelper.startPage(page, size);
        List<Inventory> inventoryList = inventoryService.selectBySupplier(supplier);
        PageInfo<Inventory> pageInfo = new PageInfo<>(inventoryList);
        return Result.success(pageInfo);
    }

    @PostMapping("/add")
    public Result addInventory(@RequestBody Inventory inventory) {
        inventoryService.addInventory(inventory);
        return Result.success("新增成功");
    }

    @PutMapping("/edit")
    public Result editInventory(@RequestBody Inventory inventory) {
        inventoryService.editInventory(inventory);
        return Result.success("编辑成功");
    }

    @DeleteMapping("/delete/{id}")
    public Result deleteInventory(@PathVariable Long id) {
        inventoryService.deleteInventory(id);
        return Result.success("删除成功");
    }

    // 批量删除
    @DeleteMapping("/deleteBatch")
    public Result deleteInventoryBatch(@RequestBody List<Long> ids) {
        try {
            inventoryService.deleteInventoryBatch(ids);
            return Result.success("批量删除成功");
        } catch (Exception e) {
            return Result.error("批量删除失败：" + e.getMessage());
        }
    }

    @PutMapping("/update/{id}")
    public Result updateInventory(@PathVariable Long id, @RequestBody Inventory inventory) {
        Inventory existingInventory = inventoryService.getInventoryItemById(id);
        if (existingInventory != null) {
            existingInventory.setQuantity(inventory.getQuantity());
            inventoryService.updateInventoryItem(existingInventory);
            return Result.success("库存更新成功");
        } else {
            return Result.error("库存物品未找到");
        }
    }

    @PostMapping("/addBatch")
    public Result addBatch(@RequestBody List<Inventory> inventoryList) {
        try {
            inventoryService.addBatch(inventoryList);
            return Result.success("批量新增成功");
        } catch (Exception e) {
            return Result.error("库存物品未找到");
        }
    }


    @GetMapping("/selectLowStock")
    public Result selectLowStock(@RequestParam int page, @RequestParam int size) {
        PageHelper.startPage(page, size);
        List<Inventory> inventoryList = inventoryService.selectLowStock();
        PageInfo<Inventory> pageInfo = new PageInfo<>(inventoryList);
        return Result.success(pageInfo);
    }

    @GetMapping("/selectById")
    public Result select1Byid(@RequestParam Long id,@RequestParam int page, @RequestParam int size) {
        PageHelper.startPage(page, size);
        List<Inventory> inventoryList = inventoryService.select1Byid(id);
        PageInfo<Inventory> pageInfo = new PageInfo<>(inventoryList);
        return Result.success(pageInfo);
    }

    @GetMapping("/selectByName")
    public Result select1ByProductName(@RequestParam String name,@RequestParam int page, @RequestParam int size) {
        PageHelper.startPage(page, size);
        List<Inventory> inventoryList = inventoryService.select1ByProductName(name);
        PageInfo<Inventory> pageInfo = new PageInfo<>(inventoryList);
        return Result.success(pageInfo);
    }

    @GetMapping("/select1Bycategory")
    public Result select1ByCategory(@RequestParam String category,@RequestParam int page, @RequestParam int size) {
        PageHelper.startPage(page, size);
        List<Inventory> inventoryList = inventoryService.select1ByCategory(category);
        PageInfo<Inventory> pageInfo = new PageInfo<>(inventoryList);
        return Result.success(pageInfo);
    }

    @GetMapping("/select1Bybrand")
    public Result select1ByBrand(@RequestParam String brand,@RequestParam int page, @RequestParam int size) {
        PageHelper.startPage(page, size);
        List<Inventory> inventoryList = inventoryService.select1ByBrand(brand);
        PageInfo<Inventory> pageInfo = new PageInfo<>(inventoryList);
        return Result.success(pageInfo);
    }

    @GetMapping("/select1Bysupplier")
    public Result select1BySupplier(@RequestParam String supplier,@RequestParam int page, @RequestParam int size) {
        PageHelper.startPage(page, size);
        List<Inventory> inventoryList = inventoryService.select1BySupplier(supplier);
        PageInfo<Inventory> pageInfo = new PageInfo<>(inventoryList);
        return Result.success(pageInfo);
    }


}