package com.example.springboot.service;

import com.example.springboot.entity.Patient;
import com.example.springboot.entity.PatientWechatBindScene;
import com.example.springboot.mapper.PatientWechatBindSceneMapper;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WechatPatientBindSceneServiceTest {

    @Test
    void ensureSceneForPatient_shouldReuseOfficialQrUrl() {
        StubSceneMapper mapper = new StubSceneMapper();
        mapper.scene.setId(1L);
        mapper.scene.setPatient_id(3L);
        mapper.scene.setScene_key("patient_bind_3");
        mapper.scene.setQr_url("https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=ticket-3");
        WechatPatientBindSceneService service = new WechatPatientBindSceneService(mapper, null, null, "wx-app", "wx-secret");

        PatientWechatBindScene scene = service.ensureSceneForPatient(3L);

        assertEquals("https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=ticket-3", scene.getQr_url());
    }

    static class StubSceneMapper implements PatientWechatBindSceneMapper {
        PatientWechatBindScene scene = new PatientWechatBindScene();
        @Override public PatientWechatBindScene selectLatestByPatientId(Long patientId) { return scene; }
        @Override public PatientWechatBindScene selectBySceneKey(String sceneKey) { return scene; }
        @Override public void insert(PatientWechatBindScene scene) { this.scene = scene; }
        @Override public void updateQrInfo(PatientWechatBindScene scene) { this.scene = scene; }
        @Override public void markBound(PatientWechatBindScene scene) { this.scene = scene; }
    }
}
