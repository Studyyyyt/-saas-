package com.example.springboot.service;

import com.example.springboot.entity.Treatment;
import com.example.springboot.entity.TreatmentBillingRequest;
import com.example.springboot.mapper.FinanceMapper;
import com.example.springboot.mapper.TreatmentMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TreatmentBillingServiceConsultationWritebackTest {

    @Test
    void chargeTreatmentShouldWriteBackFirstDealUsingBillingDate() {
        TreatmentMapper treatmentMapper = mock(TreatmentMapper.class);
        FinanceMapper financeMapper = mock(FinanceMapper.class);
        PaymentChannelService paymentChannelService = mock(PaymentChannelService.class);
        ConsultationRecordService consultationRecordService = mock(ConsultationRecordService.class);

        TreatmentBillingService service = new TreatmentBillingService(
                treatmentMapper,
                financeMapper,
                paymentChannelService
        );
        ReflectionTestUtils.setField(service, "consultationRecordService", consultationRecordService);

        Treatment treatment = new Treatment();
        treatment.setId(31L);
        treatment.setPatient_id(9L);
        treatment.setPatient_name("王五");
        treatment.setAppointment_purpose("种植");
        treatment.setTreatment_fee("6800.00");
        treatment.setStatus("进行中");

        when(treatmentMapper.selectById(31L)).thenReturn(List.of(treatment));
        when(financeMapper.getFinancesByTreatmentId(31L)).thenReturn(List.of());

        TreatmentBillingRequest request = new TreatmentBillingRequest();
        request.setAmount(6800D);
        request.setDate("2026-05-06");
        request.setRemark("首单收费");

        service.chargeTreatment(31L, request);

        verify(financeMapper).addFinance(any());
        ArgumentCaptor<Date> dealAtCaptor = ArgumentCaptor.forClass(Date.class);
        verify(consultationRecordService).markFirstDealByPatientId(eq(9L), dealAtCaptor.capture());

        LocalDateTime dealAt = LocalDateTime.ofInstant(dealAtCaptor.getValue().toInstant(), ZoneId.systemDefault());
        assertEquals(LocalDate.of(2026, 5, 6), dealAt.toLocalDate());
        assertEquals(LocalTime.of(23, 59, 59), dealAt.toLocalTime().withNano(0));
    }
}
