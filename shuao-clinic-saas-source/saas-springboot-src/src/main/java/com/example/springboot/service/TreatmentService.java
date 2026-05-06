package com.example.springboot.service;

import com.example.springboot.entity.Appointment;
import com.example.springboot.entity.MedicalRecord;
import com.example.springboot.entity.Patient;
import com.example.springboot.entity.Treatment;
import com.example.springboot.entity.TreatmentBatchCreateRequest;
import com.example.springboot.entity.TreatmentBatchItemRequest;
import com.example.springboot.entity.TreatmentProject;
import com.example.springboot.mapper.MedicalRecordMapper;
import com.example.springboot.mapper.PatientMapper;
import com.example.springboot.mapper.TreatmentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

@Service
public class TreatmentService {

    @Autowired
    private TreatmentMapper treatmentMapper;

    @Autowired
    private FinanceService financeService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private TreatmentProjectService treatmentProjectService;

    @Autowired
    private PatientMapper patientMapper;

    @Autowired
    private MedicalRecordMapper medicalRecordMapper;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private TreatmentOperationAllocationService treatmentOperationAllocationService;

    @Autowired
    private PatientInsightSummaryService patientInsightSummaryService;

    public List<Treatment> selectAll() {
        return treatmentMapper.selectAll();
    }

    public List<Treatment> selectRecentByPatientId(Long patientId, Integer limit) {
        int safeLimit = limit == null || limit <= 0 ? 10 : Math.min(limit, 50);
        return treatmentMapper.selectRecentByPatientId(patientId, safeLimit);
    }

    public List<Treatment> selectByPatientReference(Long patientId) {
        if (patientId == null || patientId <= 0) {
            return List.of();
        }
        return treatmentMapper.selectByPatientId(patientId);
    }

    public List<Treatment> selectById(Long id) {
        return treatmentMapper.selectById(id);
    }

    public List<Treatment> selectByName(String name) {
        return treatmentMapper.selectByName(name);
    }

    public void addTreatment(Treatment treatment) {
        normalizeTreatmentPatient(treatment);
        normalizeTreatmentDoctor(treatment, null, null);
        normalizeTreatmentProject(treatment, null);
        if (treatment == null || treatment.getPatient_id() == null || treatment.getPatient_id() <= 0) {
            throw new IllegalArgumentException("患者ID不能为空");
        }
        if (isBlank(treatment.getPatient_name())) {
            throw new IllegalArgumentException("患者姓名不能为空");
        }
        if (isBlank(treatment.getDoctor_name())) {
            throw new IllegalArgumentException("医生不能为空");
        }
        treatmentMapper.addTreatment(treatment);
        if (treatmentOperationAllocationService != null) {
            treatmentOperationAllocationService.replaceByTreatment(
                    treatment,
                    normalizePositiveId(treatment.getMedical_record_id()),
                    buildBatchItemFromTreatment(treatment)
            );
        }
        syncTodayAppointmentsFromTreatments(List.of(treatment), normalizePositiveId(treatment.getMedical_record_id()));
        refreshPatientInsight(treatment.getPatient_id());
    }

    @Transactional
    public List<Treatment> addTreatmentsBatch(TreatmentBatchCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("处置数据不能为空");
        }
        if (request.getPatient_id() == null || request.getPatient_id() <= 0) {
            throw new IllegalArgumentException("患者ID不能为空");
        }
        Patient patient = requireExistingPatient(request.getPatient_id());
        request.setPatient_name(patient.getName());
        if (request.getTreatment_date() == null) {
            throw new IllegalArgumentException("治疗日期不能为空");
        }
        List<TreatmentBatchItemRequest> items = safeItems(request.getItems());
        if (items.isEmpty()) {
            throw new IllegalArgumentException("请至少添加一条处置");
        }

        List<Double> originalFees = new ArrayList<>();
        double originalTotal = 0D;
        for (int index = 0; index < items.size(); index++) {
            TreatmentBatchItemRequest item = items.get(index);
            if (isBlank(item.getAppointment_purpose()) && normalizePositiveId(item.getProject_id()) == null) {
                throw new IllegalArgumentException("第" + (index + 1) + "条处置缺少治疗项目");
            }
            if (!isBlank(item.getTreatment_content()) && isBlank(item.getTooth_positions())) {
                throw new IllegalArgumentException("第" + (index + 1) + "条处置请选择牙位");
            }
            double fee = round2(item.getTreatment_fee() == null ? 0D : item.getTreatment_fee());
            if (fee < 0) {
                throw new IllegalArgumentException("第" + (index + 1) + "条处置费用不能小于0");
            }
            originalFees.add(fee);
            originalTotal += fee;
        }
        originalTotal = round2(originalTotal);
        if (originalTotal <= 0) {
            throw new IllegalArgumentException("汇总价格必须大于0");
        }

        double discountedTotal = request.getDiscounted_total_fee() == null
                ? originalTotal
                : round2(request.getDiscounted_total_fee());
        if (discountedTotal < 0) {
            throw new IllegalArgumentException("折后价格不能小于0");
        }
        if (discountedTotal - originalTotal > 0.0001) {
            throw new IllegalArgumentException("折后价格不能大于汇总价格");
        }

        List<Double> finalFees = allocateFinalFees(originalFees, originalTotal, discountedTotal);
        List<Treatment> created = new ArrayList<>();
        String status = isBlank(request.getStatus()) ? "进行中" : request.getStatus().trim();
        String batchNo = items.size() > 1 ? generateBatchNo() : null;
        for (int index = 0; index < items.size(); index++) {
            TreatmentBatchItemRequest item = items.get(index);
            if (normalizeDoctorAccountId(item.getDoctor_account_id()) == null
                    && normalizeDoctorAccountId(request.getDoctor_account_id()) == null) {
                throw new IllegalArgumentException("第" + (index + 1) + "条处置缺少处置医生");
            }
            TreatmentProject project = resolveBatchProject(item.getProject_id());
            Treatment treatment = new Treatment();
            treatment.setPatient_id(request.getPatient_id());
            treatment.setPatient_name(request.getPatient_name().trim());
            treatment.setBatch_no(batchNo);
            treatment.setMedical_record_id(normalizePositiveId(request.getMedical_record_id()));
            treatment.setProject_id(project == null ? null : project.getId());
            treatment.setDoctor_account_id(item.getDoctor_account_id());
            treatment.setDoctor_name(item.getDoctor_name());
            treatment.setTreatment_date(request.getTreatment_date());
            treatment.setStatus(status);
            treatment.setAppointment_purpose(resolveAppointmentPurpose(item.getAppointment_purpose(), project));
            treatment.setTreatment_content(trimToEmpty(item.getTreatment_content()));
            treatment.setTooth_positions(trimToEmpty(item.getTooth_positions()));
            treatment.setTreatment_product(trimToEmpty(item.getTreatment_product()));
            treatment.setTreatment_fee(formatAmount(finalFees.get(index)));
            normalizeTreatmentDoctor(treatment, request.getDoctor_account_id(), request.getDoctor_name());
            if (isBlank(treatment.getDoctor_name())) {
                throw new IllegalArgumentException("第" + (index + 1) + "条处置缺少处置医生");
            }
            treatmentMapper.addTreatment(treatment);
            created.add(treatment);
        }
        if (treatmentOperationAllocationService != null) {
            treatmentOperationAllocationService.replaceByBatch(created, request);
        }
        syncTodayAppointmentsFromTreatments(created, normalizePositiveId(request.getMedical_record_id()));
        refreshPatientInsight(request.getPatient_id());
        return created;
    }

    public void editTreatment(Treatment treatment) {
        normalizeTreatmentPatient(treatment);
        normalizeTreatmentDoctor(treatment, null, null);
        normalizeTreatmentProject(treatment, null);
        if (treatment == null || treatment.getPatient_id() == null || treatment.getPatient_id() <= 0) {
            throw new IllegalArgumentException("患者ID不能为空");
        }
        if (isBlank(treatment.getPatient_name())) {
            throw new IllegalArgumentException("患者姓名不能为空");
        }
        if (isBlank(treatment.getDoctor_name())) {
            throw new IllegalArgumentException("医生不能为空");
        }
        treatmentMapper.editTreatment(treatment);
        if (treatmentOperationAllocationService != null) {
            treatmentOperationAllocationService.replaceByTreatment(
                    treatment,
                    normalizePositiveId(treatment.getMedical_record_id()),
                    buildBatchItemFromTreatment(treatment)
            );
        }
        refreshPatientInsight(treatment.getPatient_id());
    }

    @Transactional
    public void deleteTreatment(Long id) {
        Long patientId = null;
        List<Treatment> existingTreatments = id == null ? List.of() : treatmentMapper.selectById(id);
        if (existingTreatments != null && !existingTreatments.isEmpty() && existingTreatments.get(0) != null) {
            patientId = existingTreatments.get(0).getPatient_id();
        }
        if (treatmentOperationAllocationService != null) {
            treatmentOperationAllocationService.deleteByTreatmentId(id);
        }
        financeService.deleteByTreatmentId(id);
        treatmentMapper.deleteTreatment(id);
        refreshPatientInsight(patientId);
    }

    @Transactional
    public void deleteByPatientReference(Long patientId) {
        if (patientId == null || patientId <= 0) {
            return;
        }
        List<Treatment> treatments = selectByPatientReference(patientId);
        if (treatments != null) {
            for (Treatment treatment : treatments) {
                if (treatment != null && treatment.getId() != null) {
                    if (treatmentOperationAllocationService != null) {
                        treatmentOperationAllocationService.deleteByTreatmentId(treatment.getId());
                    }
                    financeService.deleteByTreatmentId(treatment.getId());
                }
            }
        }
        treatmentMapper.deleteByPatientReference(patientId);
    }

    private void refreshPatientInsight(Long patientId) {
        if (patientInsightSummaryService != null && patientId != null && patientId > 0) {
            patientInsightSummaryService.refreshPatients(List.of(patientId));
        }
    }

    private void normalizeTreatmentPatient(Treatment treatment) {
        if (treatment == null || treatment.getPatient_id() == null || treatment.getPatient_id() <= 0 || patientMapper == null) {
            return;
        }
        Patient patient = requireExistingPatient(treatment.getPatient_id());
        treatment.setPatient_id((long) patient.getId());
        treatment.setPatient_name(trimToEmpty(patient.getName()));
    }

    private Patient requireExistingPatient(Long patientId) {
        if (patientId == null || patientId <= 0 || patientMapper == null) {
            throw new IllegalArgumentException("患者ID不能为空");
        }
        List<Patient> patients = patientMapper.selectById(patientId);
        if (patients == null || patients.isEmpty() || patients.get(0) == null) {
            throw new IllegalArgumentException("患者不存在");
        }
        return patients.get(0);
    }

    private List<TreatmentBatchItemRequest> safeItems(List<TreatmentBatchItemRequest> items) {
        if (items == null) {
            return List.of();
        }
        return items.stream().filter(Objects::nonNull).toList();
    }

    private List<Double> allocateFinalFees(List<Double> originalFees, double originalTotal, double discountedTotal) {
        List<Double> result = new ArrayList<>();
        if (originalFees.isEmpty()) {
            return result;
        }
        if (discountedTotal <= 0) {
            for (int i = 0; i < originalFees.size(); i++) {
                result.add(0D);
            }
            return result;
        }
        double allocated = 0D;
        for (int i = 0; i < originalFees.size(); i++) {
            double finalFee;
            if (i == originalFees.size() - 1) {
                finalFee = round2(discountedTotal - allocated);
            } else {
                finalFee = round2(discountedTotal * originalFees.get(i) / originalTotal);
                allocated = round2(allocated + finalFee);
            }
            result.add(Math.max(0D, finalFee));
        }
        return result;
    }

    private double round2(double value) {
        return Math.round(value * 100D) / 100D;
    }

    private String formatAmount(double value) {
        return String.format(Locale.ROOT, "%.2f", round2(value));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private String resolveAppointmentPurpose(String rawPurpose, TreatmentProject project) {
        if (!isBlank(rawPurpose)) {
            return rawPurpose.trim();
        }
        if (project != null && !isBlank(project.getProject_name())) {
            return project.getProject_name().trim();
        }
        throw new IllegalArgumentException("处置项目名称不能为空");
    }

    private TreatmentProject resolveBatchProject(Long projectId) {
        Long normalizedProjectId = normalizePositiveId(projectId);
        if (normalizedProjectId == null) {
            return null;
        }
        TreatmentProject project = treatmentProjectService == null ? null : treatmentProjectService.selectById(normalizedProjectId);
        if (project == null) {
            throw new IllegalArgumentException("选择的项目不存在");
        }
        return project;
    }

    private void normalizeTreatmentProject(Treatment treatment, Long fallbackProjectId) {
        if (treatment == null) {
            return;
        }
        Long projectId = normalizePositiveId(treatment.getProject_id());
        if (projectId == null) {
            projectId = normalizePositiveId(fallbackProjectId);
        }
        if (projectId == null) {
            treatment.setProject_id(null);
            if (!isBlank(treatment.getAppointment_purpose())) {
                treatment.setAppointment_purpose(treatment.getAppointment_purpose().trim());
            }
            return;
        }
        TreatmentProject project = treatmentProjectService == null ? null : treatmentProjectService.selectById(projectId);
        if (project == null) {
            throw new IllegalArgumentException("选择的项目不存在");
        }
        treatment.setProject_id(projectId);
        if (isBlank(treatment.getAppointment_purpose())) {
            treatment.setAppointment_purpose(project.getProject_name());
        } else {
            treatment.setAppointment_purpose(treatment.getAppointment_purpose().trim());
        }
    }

    private Long normalizePositiveId(Long value) {
        return value != null && value > 0 ? value : null;
    }

    private TreatmentBatchItemRequest buildBatchItemFromTreatment(Treatment treatment) {
        TreatmentBatchItemRequest item = new TreatmentBatchItemRequest();
        if (treatment == null) {
            return item;
        }
        item.setProject_id(treatment.getProject_id());
        item.setDoctor_account_id(treatment.getDoctor_account_id());
        item.setDoctor_name(treatment.getDoctor_name());
        item.setAppointment_purpose(treatment.getAppointment_purpose());
        item.setTreatment_content(treatment.getTreatment_content());
        item.setTooth_positions(treatment.getTooth_positions());
        item.setTreatment_product(treatment.getTreatment_product());
        item.setTreatment_fee(parseAmountToDouble(treatment.getTreatment_fee()));
        return item;
    }

    private Double parseAmountToDouble(String value) {
        if (isBlank(value)) {
            return 0D;
        }
        try {
            return round2(Double.parseDouble(value.trim()));
        } catch (Exception exception) {
            return 0D;
        }
    }

    private void syncTodayAppointmentsFromTreatments(List<Treatment> treatments, Long medicalRecordId) {
        if (appointmentService == null || treatments == null || treatments.isEmpty()) {
            return;
        }
        Date treatmentDate = treatments.stream()
                .filter(Objects::nonNull)
                .map(Treatment::getTreatment_date)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
        if (!isToday(treatmentDate)) {
            return;
        }
        Time appointmentTime = resolveAutoAppointmentTime(treatmentDate, medicalRecordId);
        Map<String, List<Treatment>> groupedTreatments = new LinkedHashMap<>();
        for (Treatment treatment : treatments) {
            if (treatment == null || !isToday(treatment.getTreatment_date())) {
                continue;
            }
            if (treatment.getPatient_id() == null || treatment.getPatient_id() <= 0 || isBlank(treatment.getDoctor_name())) {
                continue;
            }
            groupedTreatments
                    .computeIfAbsent(buildTreatmentDoctorKey(treatment), key -> new ArrayList<>())
                    .add(treatment);
        }
        for (List<Treatment> groupedItems : groupedTreatments.values()) {
            if (groupedItems == null || groupedItems.isEmpty()) {
                continue;
            }
            Treatment first = groupedItems.get(0);
            Appointment appointment = new Appointment();
            appointment.setPatient_id(first.getPatient_id());
            appointment.setPatient_name(trimToEmpty(first.getPatient_name()));
            appointment.setAppointment_date(first.getTreatment_date());
            appointment.setAppointment_time(appointmentTime);
            appointment.setDuration_minutes(60);
            appointment.setDoctor_account_id(normalizeDoctorAccountId(first.getDoctor_account_id()));
            appointment.setDoctor_name(trimToEmpty(first.getDoctor_name()));
            appointment.setAppointment_purpose(joinTreatmentPurposes(groupedItems));
            appointment.setStatus("已就诊");
            appointmentService.ensureTreatmentGeneratedAppointment(appointment);
        }
    }

    private boolean isToday(Date date) {
        return date != null && LocalDate.now().equals(date.toLocalDate());
    }

    private Time resolveAutoAppointmentTime(Date treatmentDate, Long medicalRecordId) {
        LocalDate targetDate = treatmentDate == null ? LocalDate.now() : treatmentDate.toLocalDate();
        if (medicalRecordMapper != null && medicalRecordId != null && medicalRecordId > 0) {
            MedicalRecord record = medicalRecordMapper.selectById(medicalRecordId);
            if (record != null && record.getVisit_date() != null) {
                LocalDateTime visitDateTime = LocalDateTime.ofInstant(record.getVisit_date().toInstant(), ZoneId.systemDefault());
                if (targetDate.equals(visitDateTime.toLocalDate())) {
                    return Time.valueOf(visitDateTime.toLocalTime().withSecond(0).withNano(0));
                }
            }
        }
        return Time.valueOf(LocalTime.now().withSecond(0).withNano(0));
    }

    private String buildTreatmentDoctorKey(Treatment treatment) {
        Long doctorAccountId = normalizeDoctorAccountId(treatment == null ? null : treatment.getDoctor_account_id());
        if (doctorAccountId != null) {
            return "doctor-id:" + doctorAccountId;
        }
        return "doctor-name:" + trimToEmpty(treatment == null ? null : treatment.getDoctor_name());
    }

    private String joinTreatmentPurposes(List<Treatment> treatments) {
        Set<String> purposes = treatments.stream()
                .filter(Objects::nonNull)
                .map(Treatment::getAppointment_purpose)
                .map(this::trimToEmpty)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
        if (purposes.isEmpty()) {
            return "当日处置";
        }
        return String.join(" / ", purposes);
    }

    private void normalizeTreatmentDoctor(Treatment treatment,
                                          Long fallbackDoctorAccountId,
                                          String fallbackDoctorName) {
        if (treatment == null) {
            throw new IllegalArgumentException("处置数据不能为空");
        }
        Long resolvedDoctorAccountId = resolveDoctorAccountId(
                treatment.getDoctor_account_id(),
                treatment.getDoctor_name(),
                fallbackDoctorAccountId,
                fallbackDoctorName
        );
        String resolvedDoctorName = resolveDoctorName(
                resolvedDoctorAccountId,
                treatment.getDoctor_name(),
                fallbackDoctorAccountId,
                fallbackDoctorName
        );
        treatment.setDoctor_account_id(resolvedDoctorAccountId);
        treatment.setDoctor_name(resolvedDoctorName);
    }

    private Long resolveDoctorAccountId(Long doctorAccountId,
                                        String doctorName,
                                        Long fallbackDoctorAccountId,
                                        String fallbackDoctorName) {
        Long resolved = normalizeDoctorAccountId(doctorAccountId);
        if (resolved != null) {
            String doctorDisplayName = accountService.findDoctorDisplayNameByAccountId(resolved);
            if (!isBlank(doctorDisplayName)) {
                return resolved;
            }
            resolved = null;
        }
        resolved = resolveDoctorAccountIdByName(doctorName);
        if (resolved != null) {
            return resolved;
        }
        resolved = normalizeDoctorAccountId(fallbackDoctorAccountId);
        if (resolved != null) {
            String doctorDisplayName = accountService.findDoctorDisplayNameByAccountId(resolved);
            if (!isBlank(doctorDisplayName)) {
                return resolved;
            }
        }
        return resolveDoctorAccountIdByName(fallbackDoctorName);
    }

    private String resolveDoctorName(Long resolvedDoctorAccountId,
                                     String doctorName,
                                     Long fallbackDoctorAccountId,
                                     String fallbackDoctorName) {
        if (resolvedDoctorAccountId != null && resolvedDoctorAccountId > 0) {
            String displayName = accountService.findDoctorDisplayNameByAccountId(resolvedDoctorAccountId);
            if (!isBlank(displayName)) {
                return displayName.trim();
            }
        }
        String directName = trimToEmpty(doctorName);
        if (!directName.isEmpty()) {
            return directName;
        }
        if (fallbackDoctorAccountId != null && fallbackDoctorAccountId > 0) {
            String displayName = accountService.findDoctorDisplayNameByAccountId(fallbackDoctorAccountId);
            if (!isBlank(displayName)) {
                return displayName.trim();
            }
        }
        return trimToEmpty(fallbackDoctorName);
    }

    private Long resolveDoctorAccountIdByName(String doctorName) {
        String normalizedName = trimToEmpty(doctorName);
        if (normalizedName.isEmpty()) {
            return null;
        }
        return accountService.findDoctorAccountIdByName(normalizedName);
    }

    private Long normalizeDoctorAccountId(Long doctorAccountId) {
        return doctorAccountId != null && doctorAccountId > 0 ? doctorAccountId : null;
    }

    private String generateBatchNo() {
        return "TB" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase(Locale.ROOT);
    }
}
