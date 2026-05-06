package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.Treatment;
import com.example.springboot.service.TreatmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.Date;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TreatmentControllerToothPositionTest {

    private final StubTreatmentService treatmentService = new StubTreatmentService();
    private TreatmentController controller;

    @BeforeEach
    void setUp() {
        controller = new TreatmentController();
        ReflectionTestUtils.setField(controller, "treatmentService", treatmentService);
    }

    @Test
    void addTreatment_shouldRejectMissingToothPositionsWhenTreatmentContentFilled() {
        Treatment treatment = new Treatment();
        treatment.setPatient_id(1L);
        treatment.setPatient_name("张三");
        treatment.setAppointment_purpose("补牙");
        treatment.setDoctor_name("孔凡瑞");
        treatment.setTreatment_date(Date.valueOf("2026-04-25"));
        treatment.setTreatment_content("树脂充填");
        treatment.setTooth_positions("   ");

        Result result = controller.addTreatment(treatment);

        assertEquals("请选择牙位", result.getMsg());
        assertEquals("500", result.getCode());
    }

    @Test
    void addTreatment_shouldRejectMissingPatientId() {
        Treatment treatment = new Treatment();
        treatment.setPatient_name("张三");
        treatment.setAppointment_purpose("补牙");
        treatment.setDoctor_name("孔凡瑞");
        treatment.setTreatment_date(Date.valueOf("2026-04-25"));

        Result result = controller.addTreatment(treatment);

        assertEquals("患者ID不能为空", result.getMsg());
        assertEquals("500", result.getCode());
    }

    @Test
    void addTreatment_shouldPassWhenPatientIdAndToothPositionsProvided() {
        Treatment treatment = new Treatment();
        treatment.setPatient_id(1L);
        treatment.setPatient_name("张三");
        treatment.setAppointment_purpose("补牙");
        treatment.setDoctor_name("孔凡瑞");
        treatment.setTreatment_date(Date.valueOf("2026-04-25"));
        treatment.setTreatment_content("树脂充填");
        treatment.setTooth_positions("11,12");

        Result result = controller.addTreatment(treatment);

        assertEquals("200", result.getCode());
        assertEquals(treatment, result.getData());
        assertEquals(1L, treatmentService.saved.getPatient_id());
        assertEquals("11,12", treatmentService.saved.getTooth_positions());
    }

    static class StubTreatmentService extends TreatmentService {
        Treatment saved;

        @Override public List<Treatment> selectAll() { return Collections.emptyList(); }
        @Override public List<Treatment> selectRecentByPatientId(Long patientId, Integer limit) { return Collections.emptyList(); }
        @Override public List<Treatment> selectById(Long id) { return Collections.emptyList(); }
        @Override public List<Treatment> selectByName(String name) { return Collections.emptyList(); }
        @Override public void addTreatment(Treatment treatment) { this.saved = treatment; }
        @Override public void editTreatment(Treatment treatment) { this.saved = treatment; }
        @Override public void deleteTreatment(Long id) { }
    }
}
