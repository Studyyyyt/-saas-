package com.example.springboot.service;

import com.example.springboot.entity.ConsultationRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConsultationDashboardServiceOverviewTest {

    private ConsultationRecordService consultationRecordService;
    private ConsultationDashboardService consultationDashboardService;

    @BeforeEach
    void setUp() {
        consultationRecordService = mock(ConsultationRecordService.class);
        consultationDashboardService = new ConsultationDashboardService();
        ReflectionTestUtils.setField(consultationDashboardService, "consultationRecordService", consultationRecordService);
    }

    @Test
    void buildOverviewShouldAggregateCountsRatesAndHighIntentPendingMetric() {
        ConsultationRecord currentPendingA = consultation("高", "待跟进", false, false);
        ConsultationRecord currentPendingB = consultation("高", "待跟进", false, false);
        ConsultationRecord currentDeal = consultation("中", "已预约到店", true, true);
        ConsultationRecord previousPending = consultation("高", "待跟进", false, false);

        when(consultationRecordService.search(any()))
                .thenReturn(List.of(currentPendingA, currentPendingB, currentDeal))
                .thenReturn(List.of(previousPending));

        Map<String, Object> result = consultationDashboardService.buildOverview(null, null, "today");

        @SuppressWarnings("unchecked")
        Map<String, Object> summary = (Map<String, Object>) result.get("summary");
        assertEquals(3, summary.get("currentConsultationCount"));
        assertEquals(1, summary.get("currentArrivedCount"));
        assertEquals(1, summary.get("currentDealCount"));
        assertEquals(2, summary.get("currentHighIntentPendingCount"));
        assertEquals(1, summary.get("previousHighIntentPendingCount"));

        @SuppressWarnings("unchecked")
        Map<String, Object> highIntentMetric = (Map<String, Object>) result.get("highIntentPendingCount");
        assertEquals(2.0, highIntentMetric.get("current_value"));
        assertEquals(1.0, highIntentMetric.get("previous_value"));
        assertEquals(1.0, highIntentMetric.get("change_value"));
        assertEquals("up", highIntentMetric.get("direction"));

        @SuppressWarnings("unchecked")
        Map<String, Object> arrivalMetric = (Map<String, Object>) result.get("arrivalRate");
        assertEquals(33.33, arrivalMetric.get("current_value"));
    }

    private ConsultationRecord consultation(String intentLevel, String handlingResult, boolean arrived, boolean deal) {
        ConsultationRecord record = new ConsultationRecord();
        record.setIntent_level(intentLevel);
        record.setHandling_result(handlingResult);
        if (arrived) {
            record.setArrived_at(dateOf("2026-05-01T10:00:00"));
        }
        if (deal) {
            record.setDeal_at(dateOf("2026-05-01T15:00:00"));
        }
        return record;
    }

    private Date dateOf(String value) {
        return Date.from(LocalDateTime.parse(value).atZone(ZoneId.systemDefault()).toInstant());
    }
}
