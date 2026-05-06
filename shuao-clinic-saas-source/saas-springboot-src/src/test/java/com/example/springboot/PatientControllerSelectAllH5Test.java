package com.example.springboot;

import com.example.springboot.controller.PatientController;
import com.example.springboot.common.Result;
import com.example.springboot.entity.Patient;
import com.example.springboot.service.PatientService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PatientControllerSelectAllH5Test {

    @Test
    void selectAllForH5ShouldReturnPlainListWithoutPagination() {
        PatientService patientService = mock(PatientService.class);
        PatientController controller = new PatientController();
        ReflectionTestUtils.setField(controller, "patientService", patientService);

        Patient patient = new Patient();
        patient.setId(1);
        patient.setName("张三");
        patient.setPhone("13800138000");
        when(patientService.selectAll()).thenReturn(List.of(patient));

        Result result = controller.selectAllForH5();

        @SuppressWarnings("unchecked")
        List<Patient> data = (List<Patient>) result.getData();
        assertEquals("200", result.getCode());
        assertEquals(1, data.size());
        assertEquals("张三", data.get(0).getName());
    }
}
