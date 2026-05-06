package com.example.springboot.service;

import com.example.springboot.entity.LabBill;
import com.example.springboot.entity.LabBillConfirmRequest;
import com.example.springboot.entity.LabBillItem;
import com.example.springboot.mapper.LabBillItemMapper;
import com.example.springboot.mapper.LabBillMapper;
import com.example.springboot.mapper.LabBillTemplateMapper;
import com.example.springboot.mapper.LabBillUnmatchedOrderMapper;
import com.example.springboot.mapper.LabFactoryMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LabBillServiceConfirmTest {

    private LabBillMapper billMapper;
    private LabBillItemMapper billItemMapper;
    private LabBillUnmatchedOrderMapper unmatchedOrderMapper;
    private LabBillTemplateMapper templateMapper;
    private LabFactoryMapper factoryMapper;
    private LabOrderService labOrderService;
    private FinanceService financeService;
    private LabBillService labBillService;

    @BeforeEach
    void setUp() {
        billMapper = mock(LabBillMapper.class);
        billItemMapper = mock(LabBillItemMapper.class);
        unmatchedOrderMapper = mock(LabBillUnmatchedOrderMapper.class);
        templateMapper = mock(LabBillTemplateMapper.class);
        factoryMapper = mock(LabFactoryMapper.class);
        labOrderService = mock(LabOrderService.class);
        financeService = mock(FinanceService.class);
        labBillService = new LabBillService(
                billMapper,
                billItemMapper,
                unmatchedOrderMapper,
                templateMapper,
                factoryMapper,
                labOrderService,
                financeService
        );
    }

    @Test
    void confirmBillShouldMarkOrdersAndWriteExpense() {
        LabBill bill = new LabBill();
        bill.setId(10L);
        bill.setFactory_id(2L);
        bill.setFactory_name("舒澳义齿厂");
        bill.setBill_month("2026-04");
        bill.setTotal_amount(new BigDecimal("4200.00"));
        bill.setStatus("对账中");
        when(billMapper.selectById(10L)).thenReturn(bill);

        LabBillItem matchedItem = new LabBillItem();
        matchedItem.setId(1L);
        matchedItem.setMatch_status("完全匹配");
        matchedItem.setMatched_lab_order_id(66L);
        matchedItem.setResolution_status("无需处理");
        when(billItemMapper.selectByBillId(10L)).thenReturn(List.of(matchedItem));
        when(unmatchedOrderMapper.selectByBillId(10L)).thenReturn(List.of());
        doNothing().when(labOrderService).markOrdersReconciled(List.of(66L));
        when(financeService.recordExpense(
                eq(null),
                eq(null),
                eq(new BigDecimal("4200.00")),
                eq("义齿加工"),
                eq("舒澳义齿厂 2026-04月度账单"),
                eq("lab_bill"),
                eq("10")
        )).thenReturn(99L);

        LabBillConfirmRequest request = new LabBillConfirmRequest();
        request.setConfirmed_by(8L);
        request.setConfirmed_by_name("管理员");

        LabBill confirmed = labBillService.confirmBill(10L, request);

        assertEquals("已完成对账", confirmed.getStatus());
        verify(labOrderService).markOrdersReconciled(List.of(66L));
        verify(financeService).recordExpense(null, null, new BigDecimal("4200.00"), "义齿加工", "舒澳义齿厂 2026-04月度账单", "lab_bill", "10");
        verify(billMapper).update(any(LabBill.class));
    }

    @Test
    void confirmBillShouldRejectWhenPendingIssuesExist() {
        LabBill bill = new LabBill();
        bill.setId(11L);
        bill.setStatus("对账中");
        when(billMapper.selectById(11L)).thenReturn(bill);

        LabBillItem mismatchItem = new LabBillItem();
        mismatchItem.setId(2L);
        mismatchItem.setMatch_status("金额不符");
        mismatchItem.setResolution_status("待处理");
        when(billItemMapper.selectByBillId(11L)).thenReturn(List.of(mismatchItem));
        when(unmatchedOrderMapper.selectByBillId(11L)).thenReturn(List.of());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                labBillService.confirmBill(11L, new LabBillConfirmRequest())
        );

        assertEquals("请先处理完所有异常项目后再完成对账", exception.getMessage());
        verify(financeService, never()).recordExpense(any(), any(), any(), any(), any(), any(), any());
        verify(labOrderService, never()).markOrdersReconciled(any());
    }
}
