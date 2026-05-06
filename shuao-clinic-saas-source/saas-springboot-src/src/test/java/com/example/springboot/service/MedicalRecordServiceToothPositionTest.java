package com.example.springboot.service;

import com.example.springboot.entity.Account;
import com.example.springboot.entity.MedicalRecord;
import com.example.springboot.entity.MedicalRecordOperation;
import com.example.springboot.mapper.MedicalRecordMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.Date;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MedicalRecordServiceToothPositionTest {

    private final StubMedicalRecordMapper mapper = new StubMedicalRecordMapper();
    private final StubAccountService accountService = new StubAccountService();
    private final StubMedicalRecordOperationService medicalRecordOperationService = new StubMedicalRecordOperationService();
    private MedicalRecordService service;

    @BeforeEach
    void setUp() {
        service = new MedicalRecordService();
        ReflectionTestUtils.setField(service, "mapper", mapper);
        ReflectionTestUtils.setField(service, "accountService", accountService);
        ReflectionTestUtils.setField(service, "medicalRecordOperationService", medicalRecordOperationService);
    }

    @Test
    void add_shouldAppendToothPositionIntoTreatmentAndResolveDoctorName() {
        accountService.displayName = "孔凡瑞";
        MedicalRecord record = new MedicalRecord();
        record.setPatient_id(1L);
        record.setPatient_name("张三");
        record.setDoctor_account_id(2L);
        record.setVisit_date(Date.valueOf("2026-04-25"));
        record.setDiagnosis("龋坏");
        record.setTreatment("树脂充填");
        record.setTooth_positions("16,17");

        service.add(record);

        assertEquals("孔凡瑞", mapper.saved.getDoctor_name());
        assertEquals(2L, mapper.saved.getDoctor_account_id());
        assertEquals("16,17", mapper.saved.getTooth_positions());
        assertEquals("树脂充填（牙位：16,17）", mapper.saved.getTreatment());
    }

    @Test
    void update_shouldRejectBlankToothPositionWhenTreatmentFilled() {
        accountService.displayName = "孔凡瑞";
        MedicalRecord record = new MedicalRecord();
        record.setId(9L);
        record.setPatient_id(1L);
        record.setPatient_name("张三");
        record.setDoctor_account_id(2L);
        record.setVisit_date(Date.valueOf("2026-04-25"));
        record.setTreatment("拔除");
        record.setTooth_positions("   ");

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> service.update(record));

        assertEquals("请选择牙位", error.getMessage());
    }

    static class StubMedicalRecordMapper implements MedicalRecordMapper {
        MedicalRecord saved;

        @Override public List<MedicalRecord> selectAll() { return Collections.emptyList(); }
        @Override public List<MedicalRecord> selectByPatientId(Long patientId) { return Collections.emptyList(); }
        @Override public List<MedicalRecord> selectByPatientName(String name) { return Collections.emptyList(); }
        @Override public MedicalRecord selectById(Long id) { return null; }
        @Override public void insert(MedicalRecord record) { this.saved = copy(record); }
        @Override public void update(MedicalRecord record) { this.saved = copy(record); }
        @Override public void deleteById(Long id) { }
        @Override public void deleteByPatientId(Long patientId) { }
        @Override public void updatePatientNameByPatientId(Long patientId, String patientName) { }

        private MedicalRecord copy(MedicalRecord source) {
            MedicalRecord target = new MedicalRecord();
            target.setId(source.getId());
            target.setPatient_id(source.getPatient_id());
            target.setPatient_name(source.getPatient_name());
            target.setDoctor_account_id(source.getDoctor_account_id());
            target.setDoctor_name(source.getDoctor_name());
            target.setVisit_date(source.getVisit_date());
            target.setDiagnosis(source.getDiagnosis());
            target.setTreatment(source.getTreatment());
            target.setTooth_positions(source.getTooth_positions());
            return target;
        }
    }

    static class StubAccountService extends AccountService {
        String displayName;

        @Override
        public String findDoctorDisplayNameByAccountId(Long accountId) {
            return displayName;
        }

        @Override
        public List<Account> findActiveDoctorAccounts() {
            return Collections.emptyList();
        }
    }

    static class StubMedicalRecordOperationService extends MedicalRecordOperationService {
        StubMedicalRecordOperationService() {
            super(null, null, null, null, null);
        }

        @Override
        public void replaceByMedicalRecord(Long medicalRecordId, List<MedicalRecordOperation> items, Long doctorAccountId, String doctorName) {
        }

        @Override
        public void enrichMedicalRecord(MedicalRecord record, boolean includeDisabled) {
        }

        @Override
        public void enrichMedicalRecords(List<MedicalRecord> records, boolean includeDisabled) {
        }
    }
}
