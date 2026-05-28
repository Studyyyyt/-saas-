package com.example.springboot.service;

import com.example.springboot.entity.Finance;
import com.example.springboot.mapper.FinanceMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class FinanceServiceRecordExpenseTest {

    private FinanceMapper financeMapper;
    private FinanceService financeService;

    @BeforeEach
    void setUp() {
        financeMapper = mock(FinanceMapper.class);
        financeService = new FinanceService();
        ReflectionTestUtils.setField(financeService, "financeMapper", financeMapper);
    }

    @Test
    void recordExpenseShouldWriteNormalizedExpenseFinanceAndReturnId() {
        doAnswer(invocation -> {
            Finance finance = invocation.getArgument(0);
            finance.setId(501);
            return null;
        }).when(financeMapper).addFinance(any(Finance.class));

        Long recordId = financeService.recordExpense(
                12L,
                34L,
                new BigDecimal("88.5"),
                "义齿加工",
                "外协加工费",
                "lab_order",
                "LAB-20260429-001"
        );

        assertEquals(501L, recordId);
        verify(financeMapper).addFinance(any(Finance.class));
    }

    @Test
    void recordExpenseShouldRejectNonPositiveAmount() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                financeService.recordExpense(
                        null,
                        null,
                        BigDecimal.ZERO,
                        "耗材采购",
                        "测试",
                        "material_purchase",
                        "PO-001"
                )
        );

        assertEquals("支出金额必须大于0", exception.getMessage());
    }

    @Test
    void recordExpenseShouldWriteBizIdIntoRemarkAndNormalizeBlankReferences() {
        doAnswer(invocation -> {
            Finance finance = invocation.getArgument(0);
            assertEquals("其他", finance.getName());
            assertEquals("支出", finance.getType());
            assertEquals("other_expense", finance.getBiz_type());
            assertEquals("bizId=BIZ-9", finance.getRemark());
            assertEquals(12.35D, finance.getAmount());
            assertNull(finance.getPatient_id());
            assertNull(finance.getTreatment_id());
            assertTrue(finance.getDate() != null && !finance.getDate().isEmpty());
            finance.setId(9);
            return null;
        }).when(financeMapper).addFinance(any(Finance.class));

        Long recordId = financeService.recordExpense(
                0L,
                -2L,
                new BigDecimal("12.345"),
                " ",
                " ",
                " other_expense ",
                " BIZ-9 "
        );

        assertEquals(9L, recordId);
    }
}
