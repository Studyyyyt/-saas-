package com.example.springboot.service;

import com.example.springboot.entity.Inventory;
import com.example.springboot.mapper.InventoryMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

//注入springboot容器
@Service
public class InventoryService {
    @Autowired
    private InventoryMapper inventoryMapper;

    /**
     * 查询库存信息
     */
    public List<Inventory> selectAll() {
        return inventoryMapper.selectAll();
    }

    public List<Inventory> selectById(Long id) {
        return inventoryMapper.selectById(id);

    }

    public List<Inventory> selectByProductName(String name) {
        return inventoryMapper.selectByProductName(name);
    }

    public List<Inventory> selectByCategory(String category) {
        return inventoryMapper.selectByCategory(category);
    }

    public List<Inventory> selectByBrand(String brand) {
        return inventoryMapper.selectByBrand(brand);
    }

    public List<Inventory> selectBySupplier(String supplier) {
        return inventoryMapper.selectBySupplier(supplier);
    }

    public void addInventory(Inventory inventory) {
        inventoryMapper.addInventory(inventory);
    }

    public void editInventory(Inventory inventory) {
        inventoryMapper.editInventory(inventory);
    }

    public void deleteInventory(Long id) {
        inventoryMapper.deleteInventory(id);
    }

    @Transactional
    public void deleteInventoryBatch(List<Long> ids) {
        // 根据传入的 ID 列表批量删除对应的库存信息
        inventoryMapper.deleteInventoryBatch(ids);
    }

    public Inventory getInventoryItemById(Long id) {
        return inventoryMapper.getInventoryItemById(id);
    }

    @Transactional
    public void updateInventoryItem(Inventory inventory) {
        inventoryMapper.updateInventoryItem(inventory);
    }

    public void addBatch(List<Inventory> inventoryList) {
        inventoryMapper.addInventoryBatch(inventoryList);
    }

    public List<Inventory> selectLowStock() {
        return inventoryMapper.selectLowStock();
    }

    public List<Inventory> select1Byid(Long id) {
        return inventoryMapper.select1ById(id);
    }

    public List<Inventory> select1ByProductName(String name) {
        return inventoryMapper.select1ByProductName(name);
    }

    public List<Inventory> select1ByCategory(String category) {
        return inventoryMapper.select1ByCategory(category);
    }

    public List<Inventory> select1ByBrand(String brand) {
        return inventoryMapper.select1ByBrand(brand);
    }

    public List<Inventory> select1BySupplier(String supplier) {
        return inventoryMapper.select1BySupplier(supplier);
    }

}
