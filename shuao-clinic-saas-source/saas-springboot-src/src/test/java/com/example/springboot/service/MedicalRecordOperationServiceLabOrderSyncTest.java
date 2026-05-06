package com.example.springboot.service;

import com.example.springboot.entity.LabFactory;
import com.example.springboot.entity.LabOrder;
import com.example.springboot.entity.MedicalRecord;
import com.example.springboot.entity.MedicalRecordOperation;
import com.example.springboot.entity.TreatmentOperation;
import com.example.springboot.mapper.LabOrderMapper;
import com.example.springboot.mapper.MedicalRecordOperationMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MedicalRecordOperationServiceLabOrderSyncTest {

    private MedicalRecordOperationMapper operationMapper;
    private TreatmentOperationService treatmentOperationService;
    private TreatmentProjectService treatmentProjectService;
    private LabFactoryService labFactoryService;
    private LabOrderMapper labOrderMapper;
    private MedicalRecordOperationService service;

    @BeforeEach
    void setUp() {
        operationMapper = mock(MedicalRecordOperationMapper.class);
        treatmentOperationService = mock(TreatmentOperationService.class);
        treatmentProjectService = mock(TreatmentProjectService.class);
        labFactoryService = mock(LabFactoryService.class);
        labOrderMapper = mock(LabOrderMapper.class);
        service = spy(new MedicalRecordOperationService(
                operationMapper,
                treatmentOperationService,
                treatmentProjectService,
                labFactoryService,
                labOrderMapper
        ));
    }

    @Test
    void syncLabOrdersForMedicalRecordShouldCreatePlaceholderOrderAndMarkRegistered() {
        MedicalRecord record = new MedicalRecord();
        record.setId(9L);
        record.setPatient_id(3L);
        record.setPatient_name("李四");
        record.setDoctor_account_id(7L);
        record.setDoctor_name("王医生");
        record.setVisit_date(Date.valueOf("2026-05-02"));

        MedicalRecordOperation operation = new MedicalRecordOperation();
        operation.setId(15L);
        operation.setMedical_record_id(9L);
        operation.setProject_id(11L);
        operation.setProject_name("全瓷冠");
        operation.setOperation_id(22L);
        operation.setOperation_name("取模");
        operation.setFactory_id(2L);
        operation.setFactory_name("舒澳义齿厂");
        operation.setTooth_positions("11");
        operation.setRemark("急件");
        operation.setNeed_lab_processing(1);
        operation.setDefault_processing_days(5);
        operation.setLab_order_status(0);
        when(operationMapper.selectByMedicalRecordId(9L)).thenReturn(List.of(operation));
        when(labOrderMapper.selectByMedicalRecordOperationId(15L)).thenReturn(List.of());
        doNothing().when(service).markLabOrderRegistered(15L, 7L, "王医生");
        doAnswer(invocation -> {
            LabOrder saved = invocation.getArgument(0);
            assertEquals(2L, saved.getFactory_id());
            assertEquals("舒澳义齿厂", saved.getFactory_name());
            assertEquals(3L, saved.getPatient_id());
            assertEquals("李四", saved.getPatient_name());
            assertEquals(9L, saved.getMedical_record_id());
            assertEquals(15L, saved.getMedical_record_operation_id());
            assertEquals("全瓷冠", saved.getProduct_name());
            assertEquals(new BigDecimal("0.00"), saved.getUnit_price());
            assertEquals(1, saved.getQuantity());
            assertEquals(new BigDecimal("0.00"), saved.getTotal_amount());
            assertEquals("已下单", saved.getStatus());
            assertEquals("急件", saved.getRemark());
            assertNotNull(saved.getExpected_delivery_date());
            assertEquals(LocalDate.of(2026, 5, 7), toLocalDate(saved.getExpected_delivery_date()));
            return null;
        }).when(labOrderMapper).insert(any(LabOrder.class));

        service.syncLabOrdersForMedicalRecord(record);

        verify(labOrderMapper).insert(any(LabOrder.class));
        verify(service).markLabOrderRegistered(15L, 7L, "王医生");
    }

    @Test
    void replaceByMedicalRecordShouldRetainRegisteredStatusWhenUpdatingExistingOperation() {
        MedicalRecordOperation existing = new MedicalRecordOperation();
        existing.setId(21L);
        existing.setMedical_record_id(9L);
        existing.setLab_order_status(1);
        existing.setLab_order_registered_at(Date.valueOf("2026-05-01"));
        when(operationMapper.selectByMedicalRecordId(9L)).thenReturn(List.of(existing));

        TreatmentOperation updatedOperation = new TreatmentOperation();
        updatedOperation.setId(22L);
        updatedOperation.setOperation_name("戴牙");
        updatedOperation.setNeed_lab_processing(1);
        when(treatmentOperationService.selectById(22L)).thenReturn(updatedOperation);

        LabFactory factory = new LabFactory();
        factory.setId(2L);
        factory.setName("舒澳义齿厂");
        when(labFactoryService.selectById(2L)).thenReturn(factory);

        MedicalRecordOperation source = new MedicalRecordOperation();
        source.setId(21L);
        source.setProject_id(11L);
        source.setProject_name("全瓷冠");
        source.setOperation_id(22L);
        source.setFactory_id(2L);
        source.setTooth_positions("11");
        source.setRemark("复诊");

        service.replaceByMedicalRecord(9L, List.of(source), 8L, "护士A");

        verify(operationMapper).update(argThat(item ->
                Long.valueOf(21L).equals(item.getId())
                        && Long.valueOf(11L).equals(item.getProject_id())
                        && "全瓷冠".equals(item.getProject_name())
                        && Long.valueOf(22L).equals(item.getOperation_id())
                        && "戴牙".equals(item.getOperation_name())
                        && Long.valueOf(2L).equals(item.getFactory_id())
                        && "舒澳义齿厂".equals(item.getFactory_name())
                        && Integer.valueOf(1).equals(item.getLab_order_status())
                        && item.getLab_order_registered_at() != null
        ));
        verify(operationMapper, never()).insert(any(MedicalRecordOperation.class));
        verify(operationMapper, never()).deleteById(any());
    }

    private LocalDate toLocalDate(java.util.Date value) {
        return Instant.ofEpochMilli(value.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
