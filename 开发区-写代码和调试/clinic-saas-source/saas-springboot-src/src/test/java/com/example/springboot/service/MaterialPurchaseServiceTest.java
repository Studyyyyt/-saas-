package com.example.springboot.service;

import com.example.springboot.entity.Finance;
import com.example.springboot.entity.Material;
import com.example.springboot.entity.MaterialPurchase;
import com.example.springboot.entity.MaterialPurchaseItem;
import com.example.springboot.entity.MaterialPurchaseVoidRequest;
import com.example.springboot.mapper.MaterialMapper;
import com.example.springboot.mapper.MaterialPurchaseItemMapper;
import com.example.springboot.mapper.MaterialPurchaseMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MaterialPurchaseServiceTest {

    private MaterialPurchaseMapper purchaseMapper;
    private MaterialPurchaseItemMapper itemMapper;
    private MaterialMapper materialMapper;
    private MaterialService materialService;
    private FinanceService financeService;
    private MaterialPurchaseService purchaseService;

    @BeforeEach
    void setUp() {
        purchaseMapper = mock(MaterialPurchaseMapper.class);
        itemMapper = mock(MaterialPurchaseItemMapper.class);
        materialMapper = mock(MaterialMapper.class);
        materialService = mock(MaterialService.class);
        financeService = mock(FinanceService.class);
        purchaseService = new MaterialPurchaseService(
                purchaseMapper,
                itemMapper,
                materialMapper,
                materialService,
                financeService
        );
    }

    @Test
    void addPurchaseShouldIncreaseStockAndWriteExpense() {
        Material material = new Material();
        material.setId(7L);
        material.setName("种植体");
        material.setSpec("4.0x10");
        when(materialMapper.selectById(7L)).thenReturn(material);

        doAnswer(invocation -> {
            MaterialPurchase saved = invocation.getArgument(0);
            saved.setId(11L);
            return null;
        }).when(purchaseMapper).insert(any(MaterialPurchase.class));

        when(financeService.recordExpense(
                eq(null),
                eq(null),
                eq(new BigDecimal("1998.00")),
                eq("耗材采购"),
                eq("采购自 舒澳供应链"),
                eq("material_purchase"),
                eq("11")
        )).thenReturn(88L);

        Finance finance = new Finance();
        finance.setId(88);
        when(financeService.getFinanceByid(88L)).thenReturn(List.of(finance));
        doNothing().when(financeService).editFinance(any(Finance.class));

        MaterialPurchase purchase = new MaterialPurchase();
        purchase.setSupplier_name("舒澳供应链");
        purchase.setPurchase_date(Date.valueOf("2026-04-29"));
        purchase.setPayment_method("转账");
        MaterialPurchaseItem item = new MaterialPurchaseItem();
        item.setMaterial_id(7L);
        item.setUnit_price(new BigDecimal("999"));
        item.setQuantity(2);
        purchase.setItems(List.of(item));
        when(itemMapper.selectByPurchaseId(11L)).thenReturn(List.of(item));
        when(purchaseMapper.selectById(11L)).thenAnswer(invocation -> purchase);

        MaterialPurchase saved = purchaseService.add(purchase);

        assertEquals(11L, saved.getId());
        assertEquals("有效", saved.getStatus());
        assertEquals(new BigDecimal("1998.00"), saved.getTotal_amount());
        verify(materialService).adjustStock(7L, 2);
        verify(financeService).recordExpense(null, null, new BigDecimal("1998.00"), "耗材采购", "采购自 舒澳供应链", "material_purchase", "11");
    }

    @Test
    void voidPurchaseShouldDecreaseStockAndDeleteFinance() {
        MaterialPurchase purchase = new MaterialPurchase();
        purchase.setId(12L);
        purchase.setStatus("有效");
        purchase.setFinance_record_id(66L);
        when(purchaseMapper.selectById(12L)).thenReturn(purchase);

        MaterialPurchaseItem item = new MaterialPurchaseItem();
        item.setMaterial_id(9L);
        item.setQuantity(3);
        when(itemMapper.selectByPurchaseId(12L)).thenReturn(List.of(item));

        doNothing().when(materialService).adjustStock(9L, -3);
        doNothing().when(financeService).deleteFinance(66);

        MaterialPurchaseVoidRequest request = new MaterialPurchaseVoidRequest();
        request.setVoided_by(3L);
        request.setVoided_by_name("管理员");
        request.setRemark("测试作废");

        MaterialPurchase voided = purchaseService.voidPurchase(12L, request);

        assertEquals("已作废", voided.getStatus());
        verify(materialService).adjustStock(9L, -3);
        verify(financeService).deleteFinance(66);
        verify(purchaseMapper).update(any(MaterialPurchase.class));
    }
}
