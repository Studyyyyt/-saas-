package com.example.springboot.util;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class PagingSupport {

    private PagingSupport() {
    }

    public static <T> Map<String, Object> buildPageResult(List<T> rows, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        List<T> safeRows = rows == null ? List.of() : rows;
        int total = safeRows.size();
        int fromIndex = Math.min((safePage - 1) * safeSize, total);
        int toIndex = Math.min(fromIndex + safeSize, total);
        int pages = total == 0 ? 0 : (int) Math.ceil(total / (double) safeSize);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", total);
        result.put("list", safeRows.subList(fromIndex, toIndex));
        result.put("pageNum", safePage);
        result.put("pageSize", safeSize);
        result.put("size", toIndex - fromIndex);
        result.put("pages", pages);
        result.put("isFirstPage", safePage <= 1);
        result.put("isLastPage", pages == 0 || safePage >= pages);
        result.put("hasPreviousPage", safePage > 1);
        result.put("hasNextPage", safePage < pages);
        return result;
    }
}
