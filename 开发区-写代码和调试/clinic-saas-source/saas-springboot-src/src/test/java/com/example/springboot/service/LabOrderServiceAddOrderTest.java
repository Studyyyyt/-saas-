package com.example.springboot.service;

import com.example.springboot.entity.LabFactory;
import com.example.springboot.entity.LabOrder;
import com.example.springboot.entity.Patient;
import com.example.springboot.mapper.LabFactoryMapper;
import com.example.springboot.mapper.LabOrderMapper;
import com.example.springboot.mapper.PatientMapper;
import com.example.springboot.mapper.TreatmentMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LabOrderServiceAddOrderTest {

    private LabOrderMapper labOrderMapper;
    private LabFactoryMapper labFactoryMapper;
    private PatientMapper patientMapper;
    private TreatmentMapper treatmentMapper;
    private MedicalRecordService medicalRecordService;
    private MedicalRecordOperationService medicalRecordOperationService;
    private LabOrderService labOrderService;

    @BeforeEach
    void setUp() {
        labOrderMapper = mock(LabOrderMapper.class);
        labFactoryMapper = mock(LabFactoryMapper.class);
        patientMapper = mock(PatientMapper.class);
        treatmentMapper = mock(TreatmentMapper.class);
        medicalRecordService = mock(MedicalRecordService.class);
        medicalRecordOperationService = mock(MedicalRecordOperationService.class);
        labOrderService = new LabOrderService(
                labOrderMapper,
                labFactoryMapper,
                patientMapper,
                treatmentMapper,
                medicalRecordService,
                medicalRecordOperationService
        );
    }

    @Test
    void addOrderShouldForceCreatedStatusAndCalculateTotalAmount() {
        LabFactory factory = new LabFactory();
        factory.setId(2L);
        factory.setName("舒澳义齿厂");
        when(labFactoryMapper.selectById(2L)).thenReturn(factory);

        Patient patient = new Patient();
        patient.setId(5);
        patient.setName("张三");
        when(patientMapper.selectById(5L)).thenReturn(List.of(patient));

        doAnswer(invocation -> {
            LabOrder saved = invocation.getArgument(0);
            saved.setId(8L);
            assertEquals("已下单", saved.getStatus());
            assertEquals("舒澳义齿厂", saved.getFactory_name());
            assertEquals("张三", saved.getPatient_name());
            assertEquals(new BigDecimal("1360.00"), saved.getTotal_amount());
            return null;
        }).when(labOrderMapper).insert(any(LabOrder.class));

        LabOrder order = new LabOrder();
        order.setFactory_id(2L);
        order.setPatient_id(5L);
        order.setProduct_name("全瓷冠");
        order.setUnit_price(new BigDecimal("680"));
        order.setQuantity(2);
        order.setOrder_date(Date.valueOf("2026-04-29"));
        order.setStatus("加工中");

        LabOrder saved = labOrderService.addOrder(order);

        assertEquals(8L, saved.getId());
        assertEquals("已下单", saved.getStatus());
    }
}
