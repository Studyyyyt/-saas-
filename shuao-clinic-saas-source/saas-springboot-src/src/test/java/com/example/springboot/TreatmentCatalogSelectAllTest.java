package com.example.springboot;

import com.example.springboot.common.Result;
import com.example.springboot.controller.TreatmentCatalogController;
import com.example.springboot.entity.TreatmentCatalog;
import com.example.springboot.service.TreatmentCatalogService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TreatmentCatalogSelectAllTest {

    @Test
    void selectAllShouldReturnCatalogItems() {
        TreatmentCatalogService service = mock(TreatmentCatalogService.class);
        TreatmentCatalogController controller = new TreatmentCatalogController();
        ReflectionTestUtils.setField(controller, "treatmentCatalogService", service);

        TreatmentCatalog item = new TreatmentCatalog();
        item.setId(1L);
        item.setItem_name("树脂补牙");
        item.setDefault_fee("500");
        when(service.selectAll()).thenReturn(List.of(item));

        Result result = controller.selectAll();
        List<?> data = (List<?>) result.getData();

        assertEquals("200", result.getCode());
        assertEquals(1, data.size());
    }
}
