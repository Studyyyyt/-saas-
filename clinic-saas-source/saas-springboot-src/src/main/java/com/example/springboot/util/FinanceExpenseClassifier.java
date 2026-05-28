package com.example.springboot.util;

import com.example.springboot.entity.Finance;
import org.springframework.util.StringUtils;

import java.util.Locale;

public final class FinanceExpenseClassifier {

    public static final String BIZ_TYPE_MATERIAL_PURCHASE = "material_purchase";
    public static final String BIZ_TYPE_LAB_BILL = "lab_bill";
    public static final String BIZ_TYPE_MANUAL_EXPENSE = "manual_expense";

    private FinanceExpenseClassifier() {
    }

    public enum ExpenseScope {
        MATERIAL,
        LAB,
        OTHER,
        NONE
    }

    public static ExpenseScope resolveOperatingExpenseScope(Finance finance) {
        if (finance == null || !isOperatingExpense(finance)) {
            return ExpenseScope.NONE;
        }
        String bizType = normalize(finance.getBiz_type());
        if (BIZ_TYPE_MATERIAL_PURCHASE.equals(bizType)) {
            return ExpenseScope.MATERIAL;
        }
        if (BIZ_TYPE_LAB_BILL.equals(bizType)) {
            return ExpenseScope.LAB;
        }
        return ExpenseScope.OTHER;
    }

    public static boolean isOperatingExpense(Finance finance) {
        return finance != null && "支出".equals(trim(finance.getType()));
    }

    public static boolean isManualExpenseEditable(Finance finance) {
        if (!isOperatingExpense(finance)) {
            return false;
        }
        String bizType = normalize(finance.getBiz_type());
        return bizType.isEmpty() || BIZ_TYPE_MANUAL_EXPENSE.equals(bizType);
    }

    public static String normalizeBizType(String value) {
        return normalize(value);
    }

    private static String normalize(String value) {
        return trim(value).toLowerCase(Locale.ROOT);
    }

    private static String trim(String value) {
        return StringUtils.hasText(value) ? value.trim() : "";
    }
}
