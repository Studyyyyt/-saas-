package com.example.springboot.service;

import com.example.springboot.common.Result;
import com.example.springboot.entity.Inventory;
import com.example.springboot.entity.Patient;
import com.example.springboot.entity.Purchase;
import com.example.springboot.mapper.PurchaseMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class PurchaseService {

    @Autowired
    private PurchaseMapper purchaseMapper;

    public List<Purchase> selectAll() {
        return purchaseMapper.selectAll();
    }

    public void addPurchase(Purchase purchase) {
        purchaseMapper.addPurchase(purchase);
    }

    public void deletePurchase(Long id) {
        purchaseMapper.deletePurchase(id);
    }

    @Transactional
    public void deletePurchaseBatch(List<Long> ids) {
        // 根据传入的 ID 列表批量删除对应的库存信息
        purchaseMapper.deletePurchaseBatch(ids);
    }

    public void updatePurchaseStatus(Purchase purchase) {
        purchaseMapper.updatePurchase(purchase);
    }

    public List<Purchase> selectById(Long id) {
        return purchaseMapper.selectById(id);
    }

    public List<Purchase> selectByProductName(String name) {
        return purchaseMapper.selectByProductName(name);
    }

    public List<Purchase> selectByCategory(String category) {
        return purchaseMapper.selectByCategory(category);
    }

    public List<Purchase> selectByBrand(String brand) {
        return purchaseMapper.selectByBrand(brand);
    }

    public List<Purchase> selectBySupplier(String supplier) {
        return purchaseMapper.selectBySupplier(supplier);
    }



}
