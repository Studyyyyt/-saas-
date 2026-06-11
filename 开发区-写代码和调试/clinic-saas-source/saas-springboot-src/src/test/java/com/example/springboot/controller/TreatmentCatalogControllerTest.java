package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.TreatmentCatalog;
import com.example.springboot.service.TreatmentCatalogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TreatmentCatalogControllerTest {

    private final StubTreatmentCatalogService service = new StubTreatmentCatalogService();
    private TreatmentCatalogController controller;

    @BeforeEach
    void setUp() {
        controller = new TreatmentCatalogController();
        ReflectionTestUtils.setField(controller, "treatmentCatalogService", service);
    }

    @Test
    void add_shouldRejectBlankItemName() {
        TreatmentCatalog item = new TreatmentCatalog();
        item.setItem_name("   ");

        Result result = controller.add(item);

        assertEquals("500", result.getCode());
        assertEquals("项目名称不能为空", result.getMsg());
    }

    @Test
    void add_shouldSucceedWithMinimalFields() {
        TreatmentCatalog item = new TreatmentCatalog();
        item.setItem_name("洁牙");
        item.setDefault_fee("200");
        item.setDefault_content("超声洁治");
        item.setDefault_product("抛光膏");
        item.setStatus(1);
        item.setSort_order(1);

        Result result = controller.add(item);

        assertEquals("200", result.getCode());
        assertEquals("新增成功", result.getData());
        assertEquals("洁牙", service.saved.getItem_name());
    }

    static class StubTreatmentCatalogService extends TreatmentCatalogService {
        TreatmentCatalog saved;

        @Override public List<TreatmentCatalog> selectAll() { return Collections.emptyList(); }
        @Override public List<TreatmentCatalog> selectEnabled() { return Collections.emptyList(); }
        @Override public TreatmentCatalog selectById(Long id) { return null; }
        @Override public void add(TreatmentCatalog item) { this.saved = item; }
        @Override public void edit(TreatmentCatalog item) { this.saved = item; }
        @Override public void delete(Long id) { }
    }
}
