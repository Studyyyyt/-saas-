package com.example.springboot.service;

import com.example.springboot.entity.LabFactory;
import com.example.springboot.entity.LabOrder;
import com.example.springboot.entity.LabOrderBatchStatusUpdateRequest;
import com.example.springboot.entity.MedicalRecord;
import com.example.springboot.entity.MedicalRecordOperation;
import com.example.springboot.entity.Patient;
import com.example.springboot.entity.Treatment;
import com.example.springboot.mapper.LabFactoryMapper;
import com.example.springboot.mapper.LabOrderMapper;
import com.example.springboot.mapper.PatientMapper;
import com.example.springboot.mapper.TreatmentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class LabOrderService {

    public static final String ORDER_STATUS_CREATED = "已下单";
    public static final String ORDER_STATUS_PROCESSING = "加工中";
    public static final String ORDER_STATUS_COMPLETED = "已完成";
    public static final String ORDER_STATUS_RECEIVED = "已收货";
    public static final String ORDER_STATUS_RECONCILED = "已对账";

    private final LabOrderMapper labOrderMapper;
    private final LabFactoryMapper labFactoryMapper;
    private final PatientMapper patientMapper;
    private final TreatmentMapper treatmentMapper;
    private final MedicalRecordService medicalRecordService;
    private final MedicalRecordOperationService medicalRecordOperationService;

    @Autowired
    public LabOrderService(LabOrderMapper labOrderMapper,
                           LabFactoryMapper labFactoryMapper,
                           PatientMapper patientMapper,
                           TreatmentMapper treatmentMapper,
                           MedicalRecordService medicalRecordService,
                           MedicalRecordOperationService medicalRecordOperationService) {
        this.labOrderMapper = labOrderMapper;
        this.labFactoryMapper = labFactoryMapper;
        this.patientMapper = patientMapper;
        this.treatmentMapper = treatmentMapper;
        this.medicalRecordService = medicalRecordService;
        this.medicalRecordOperationService = medicalRecordOperationService;
    }

    public List<LabOrder> searchOrders(String keyword,
                                       Long factoryId,
                                       String status,
                                       Long patientId,
                                       String startDate,
                                       String endDate) {
        if (medicalRecordOperationService != null) {
            medicalRecordOperationService.backfillPendingLabOrders(patientId, null);
        }
        String normalizedKeyword = normalizeText(keyword).toLowerCase(Locale.ROOT);
        String normalizedStatus = normalizeOrderStatus(status, false, false);
        LocalDate start = parseDate(startDate);
        LocalDate end = parseDate(endDate);
        if (start != null && end != null && end.isBefore(start)) {
            throw new IllegalArgumentException("结束日期不能早于开始日期");
        }

        Map<Long, String> patientPhoneMap = new HashMap<>();
        if (!normalizedKeyword.isEmpty()) {
            for (Patient patient : patientMapper.selectAll()) {
                if (patient != null) {
                    patientPhoneMap.put((long) patient.getId(), normalizeText(patient.getPhone()));
                }
            }
        }

        return labOrderMapper.selectAll().stream()
                .filter(Objects::nonNull)
                .filter(item -> factoryId == null || factoryId <= 0 || factoryId.equals(item.getFactory_id()))
                .filter(item -> normalizedStatus.isEmpty() || normalizedStatus.equals(item.getStatus()))
                .filter(item -> patientId == null || patientId <= 0 || patientId.equals(item.getPatient_id()))
                .filter(item -> inDateRange(item.getOrder_date(), start, end))
                .filter(item -> normalizedKeyword.isEmpty() || matchesKeyword(item, normalizedKeyword, patientPhoneMap))
                .sorted(Comparator.comparing(LabOrder::getOrder_date, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(LabOrder::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    public LabOrder selectById(Long id) {
        return id == null ? null : labOrderMapper.selectById(id);
    }

    public List<LabOrder> selectAll() {
        return labOrderMapper.selectAll();
    }

    public List<LabOrder> findReconciliationCandidates(Long factoryId, String billMonth) {
        YearMonth targetMonth = parseYearMonth(billMonth);
        return labOrderMapper.selectAll().stream()
                .filter(Objects::nonNull)
                .filter(item -> factoryId.equals(item.getFactory_id()))
                .filter(item -> !ORDER_STATUS_RECONCILED.equals(item.getStatus()))
                .filter(item -> belongsToMonth(item, targetMonth))
                .sorted(Comparator.comparing(LabOrder::getOrder_date, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(LabOrder::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    @Transactional
    public LabOrder addOrder(LabOrder item) {
        if (item == null) {
            throw new IllegalArgumentException("订单信息不能为空");
        }
        MedicalRecordOperation operation = resolveMedicalRecordOperationForSave(item.getMedical_record_operation_id(), null);
        LabFactory factory = resolveFactoryForSave(item, operation);
        Patient patient = resolvePatientForSave(item, operation);
        Treatment treatment = requireMatchingTreatment(item.getTreatment_id(), patient);
        validateBaseOrderFields(item);

        item.setFactory_name(factory.getName());
        item.setPatient_name(patient.getName());
        item.setTreatment_id(treatment == null ? null : treatment.getId());
        applyMedicalRecordOperationSnapshot(item, operation);
        item.setProduct_name(item.getProduct_name().trim());
        item.setProduct_spec(trimToNull(item.getProduct_spec()));
        item.setRemark(trimToNull(item.getRemark()));
        item.setCreated_by_name(trimToNull(item.getCreated_by_name()));
        item.setUnit_price(normalizeMoney(item.getUnit_price()));
        item.setQuantity(normalizeQuantity(item.getQuantity()));
        item.setTotal_amount(calculateTotal(item.getUnit_price(), item.getQuantity()));
        item.setStatus(ORDER_STATUS_CREATED);
        labOrderMapper.insert(item);
        if (item.getMedical_record_operation_id() != null && item.getMedical_record_operation_id() > 0) {
            medicalRecordOperationService.markLabOrderRegistered(item.getMedical_record_operation_id(), item.getCreated_by(), item.getCreated_by_name());
        }
        return item;
    }

    @Transactional
    public LabOrder editOrder(LabOrder item) {
        if (item == null || item.getId() == null || item.getId() <= 0) {
            throw new IllegalArgumentException("订单ID不能为空");
        }
        LabOrder existing = requireExistingOrder(item.getId());
        if (ORDER_STATUS_RECONCILED.equals(existing.getStatus())) {
            throw new IllegalArgumentException("已对账订单不允许编辑");
        }

        if (ORDER_STATUS_CREATED.equals(existing.getStatus())) {
            Long previousOperationId = normalizeReferenceId(existing.getMedical_record_operation_id());
            MedicalRecordOperation operation = resolveMedicalRecordOperationForSave(item.getMedical_record_operation_id(), existing.getId());
            LabFactory factory = resolveFactoryForSave(item, operation);
            Patient patient = resolvePatientForSave(item, operation);
            Treatment treatment = requireMatchingTreatment(item.getTreatment_id(), patient);
            validateBaseOrderFields(item);

            existing.setFactory_id(factory.getId());
            existing.setFactory_name(factory.getName());
            existing.setPatient_id((long) patient.getId());
            existing.setPatient_name(patient.getName());
            existing.setTreatment_id(treatment == null ? null : treatment.getId());
            applyMedicalRecordOperationSnapshot(existing, operation);
            existing.setProduct_name(item.getProduct_name().trim());
            existing.setProduct_spec(trimToNull(item.getProduct_spec()));
            existing.setUnit_price(normalizeMoney(item.getUnit_price()));
            existing.setQuantity(normalizeQuantity(item.getQuantity()));
            existing.setTotal_amount(calculateTotal(existing.getUnit_price(), existing.getQuantity()));
            existing.setOrder_date(item.getOrder_date());
            existing.setExpected_delivery_date(item.getExpected_delivery_date());
            existing.setActual_delivery_date(item.getActual_delivery_date());
            existing.setRemark(trimToNull(item.getRemark()));
            existing.setStatus(resolveNextManualStatus(existing.getStatus(), item.getStatus()));
            labOrderMapper.update(existing);
            syncMedicalRecordOperationRegistration(previousOperationId, normalizeReferenceId(existing.getMedical_record_operation_id()), existing.getCreated_by(), existing.getCreated_by_name());
            return labOrderMapper.selectById(existing.getId());
        }

        String nextStatus = resolveNextManualStatus(existing.getStatus(), item.getStatus());
        existing.setStatus(nextStatus);
        existing.setActual_delivery_date(item.getActual_delivery_date());
        labOrderMapper.update(existing);
        return labOrderMapper.selectById(existing.getId());
    }

    @Transactional
    public void batchUpdateStatus(LabOrderBatchStatusUpdateRequest request) {
        if (request == null || request.getIds() == null || request.getIds().isEmpty()) {
            throw new IllegalArgumentException("请选择需要更新的订单");
        }
        java.util.Date actualDeliveryDate = parseUtilDate(request.getActual_delivery_date());
        Set<Long> ids = new LinkedHashSet<>(request.getIds());
        String targetStatus = normalizeOrderStatus(request.getStatus(), true, true);
        for (Long id : ids) {
            if (id == null || id <= 0) {
                continue;
            }
            LabOrder existing = requireExistingOrder(id);
            if (ORDER_STATUS_RECONCILED.equals(existing.getStatus())) {
                throw new IllegalArgumentException("已对账订单不允许手动改状态");
            }
            String nextStatus = resolveNextManualStatus(existing.getStatus(), targetStatus);
            labOrderMapper.updateStatus(id, nextStatus, actualDeliveryDate != null ? actualDeliveryDate : existing.getActual_delivery_date());
        }
    }

    @Transactional
    public void markOrdersReconciled(List<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return;
        }
        Set<Long> ids = new LinkedHashSet<>(orderIds);
        for (Long id : ids) {
            if (id == null || id <= 0) {
                continue;
            }
            LabOrder existing = requireExistingOrder(id);
            labOrderMapper.updateStatus(id, ORDER_STATUS_RECONCILED, existing.getActual_delivery_date());
        }
    }

    @Transactional
    public void deleteOrder(Long id) {
        LabOrder existing = requireExistingOrder(id);
        if (ORDER_STATUS_RECONCILED.equals(existing.getStatus())) {
            throw new IllegalArgumentException("已对账订单不允许删除");
        }
        labOrderMapper.delete(id);
        if (existing.getMedical_record_operation_id() != null && existing.getMedical_record_operation_id() > 0) {
            medicalRecordOperationService.resetLabOrderRegistration(existing.getMedical_record_operation_id(), existing.getCreated_by(), existing.getCreated_by_name());
        }
    }

    @Transactional
    public void deleteByPatientId(Long patientId) {
        if (patientId == null || patientId <= 0) {
            return;
        }
        List<LabOrder> orders = labOrderMapper.selectAll().stream()
                .filter(Objects::nonNull)
                .filter(item -> patientId.equals(item.getPatient_id()))
                .toList();
        for (LabOrder order : orders) {
            if (order == null || order.getId() == null || order.getId() <= 0) {
                continue;
            }
            deleteOrder(order.getId());
        }
    }

    private void validateBaseOrderFields(LabOrder item) {
        if (item.getFactory_id() == null || item.getFactory_id() <= 0) {
            throw new IllegalArgumentException("请选择加工厂");
        }
        if (item.getPatient_id() == null || item.getPatient_id() <= 0) {
            throw new IllegalArgumentException("请选择患者");
        }
        if (!StringUtils.hasText(item.getProduct_name())) {
            throw new IllegalArgumentException("产品名称不能为空");
        }
        if (item.getOrder_date() == null) {
            throw new IllegalArgumentException("下单日期不能为空");
        }
        if (normalizeQuantity(item.getQuantity()) <= 0) {
            throw new IllegalArgumentException("数量必须大于0");
        }
        if (normalizeMoney(item.getUnit_price()).compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("单价不能小于0");
        }
    }

    private Patient resolvePatientForSave(LabOrder item, MedicalRecordOperation operation) {
        if (operation != null) {
            MedicalRecord record = medicalRecordService == null ? null : medicalRecordService.selectById(operation.getMedical_record_id());
            if (record == null || record.getPatient_id() == null || record.getPatient_id() <= 0) {
                throw new IllegalArgumentException("关联病历不存在或患者信息缺失");
            }
            item.setMedical_record_id(record.getId());
            item.setPatient_id(record.getPatient_id());
            item.setPatient_name(record.getPatient_name());
            return requireExistingPatient(record.getPatient_id());
        }
        item.setMedical_record_id(null);
        return requireExistingPatient(item.getPatient_id());
    }

    private LabFactory resolveFactoryForSave(LabOrder item, MedicalRecordOperation operation) {
        Long factoryId = normalizeReferenceId(item == null ? null : item.getFactory_id());
        if (factoryId == null && operation != null) {
            factoryId = normalizeReferenceId(operation.getFactory_id());
            if (factoryId != null) {
                item.setFactory_id(factoryId);
            }
        }
        return requireExistingFactory(factoryId);
    }

    private MedicalRecordOperation resolveMedicalRecordOperationForSave(Long medicalRecordOperationId, Long currentOrderId) {
        Long targetId = normalizeReferenceId(medicalRecordOperationId);
        if (targetId == null) {
            return null;
        }
        MedicalRecordOperation operation = medicalRecordOperationService == null ? null : medicalRecordOperationService.selectPlainById(targetId);
        if (operation == null) {
            throw new IllegalArgumentException("病历操作不存在");
        }
        ensureMedicalRecordOperationAvailable(targetId, currentOrderId);
        return operation;
    }

    private void ensureMedicalRecordOperationAvailable(Long medicalRecordOperationId, Long currentOrderId) {
        List<LabOrder> linkedOrders = labOrderMapper.selectByMedicalRecordOperationId(medicalRecordOperationId);
        if (linkedOrders == null || linkedOrders.isEmpty()) {
            return;
        }
        boolean occupied = linkedOrders.stream()
                .filter(Objects::nonNull)
                .anyMatch(item -> currentOrderId == null || !currentOrderId.equals(item.getId()));
        if (occupied) {
            throw new IllegalArgumentException("该病历操作已登记加工订单");
        }
    }

    private void applyMedicalRecordOperationSnapshot(LabOrder item, MedicalRecordOperation operation) {
        if (item == null) {
            return;
        }
        if (operation == null) {
            item.setMedical_record_operation_id(null);
            item.setMedical_record_id(null);
            item.setProject_id(null);
            item.setProject_name(null);
            item.setOperation_id(null);
            item.setOperation_name(null);
            item.setTooth_positions(null);
            return;
        }
        item.setMedical_record_operation_id(operation.getId());
        item.setMedical_record_id(operation.getMedical_record_id());
        item.setProject_id(normalizeReferenceId(operation.getProject_id()));
        item.setProject_name(trimToNull(operation.getProject_name()));
        item.setOperation_id(normalizeReferenceId(operation.getOperation_id()));
        item.setOperation_name(trimToNull(operation.getOperation_name()));
        item.setTooth_positions(trimToNull(operation.getTooth_positions()));
    }

    private void syncMedicalRecordOperationRegistration(Long previousOperationId,
                                                        Long currentOperationId,
                                                        Long operatorId,
                                                        String operatorName) {
        if (Objects.equals(previousOperationId, currentOperationId)) {
            if (currentOperationId != null) {
                medicalRecordOperationService.markLabOrderRegistered(currentOperationId, operatorId, operatorName);
            }
            return;
        }
        if (previousOperationId != null) {
            medicalRecordOperationService.resetLabOrderRegistration(previousOperationId, operatorId, operatorName);
        }
        if (currentOperationId != null) {
            medicalRecordOperationService.markLabOrderRegistered(currentOperationId, operatorId, operatorName);
        }
    }

    private boolean matchesKeyword(LabOrder item, String keyword, Map<Long, String> patientPhoneMap) {
        String patientName = normalizeText(item.getPatient_name()).toLowerCase(Locale.ROOT);
        String phone = item.getPatient_id() == null ? "" : normalizeText(patientPhoneMap.get(item.getPatient_id())).toLowerCase(Locale.ROOT);
        return patientName.contains(keyword) || phone.contains(keyword);
    }

    private boolean inDateRange(java.util.Date value, LocalDate start, LocalDate end) {
        if (value == null) {
            return start == null && end == null;
        }
        LocalDate current = value.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        if (start != null && current.isBefore(start)) {
            return false;
        }
        return end == null || !current.isAfter(end);
    }

    private boolean belongsToMonth(LabOrder item, YearMonth month) {
        LocalDate relatedDate = resolveRelatedDate(item);
        return relatedDate != null && YearMonth.from(relatedDate).equals(month);
    }

    private LocalDate resolveRelatedDate(LabOrder item) {
        if (item.getActual_delivery_date() != null) {
            return item.getActual_delivery_date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        if (item.getExpected_delivery_date() != null) {
            return item.getExpected_delivery_date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        if (item.getOrder_date() != null) {
            return item.getOrder_date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        return null;
    }

    private String resolveNextManualStatus(String currentStatus, String requestedStatus) {
        String normalizedCurrent = normalizeOrderStatus(currentStatus, true, true);
        String normalizedRequested = normalizeOrderStatus(requestedStatus, true, true);
        if (ORDER_STATUS_RECONCILED.equals(normalizedRequested)) {
            throw new IllegalArgumentException("已对账状态只能由系统对账自动设置");
        }
        if (statusRank(normalizedRequested) < statusRank(normalizedCurrent)) {
            throw new IllegalArgumentException("订单状态不能回退");
        }
        return normalizedRequested;
    }

    private int statusRank(String status) {
        return switch (status) {
            case ORDER_STATUS_CREATED -> 0;
            case ORDER_STATUS_PROCESSING -> 1;
            case ORDER_STATUS_COMPLETED -> 2;
            case ORDER_STATUS_RECEIVED -> 3;
            case ORDER_STATUS_RECONCILED -> 4;
            default -> -1;
        };
    }

    private String normalizeOrderStatus(String status, boolean applyDefault, boolean allowReconciled) {
        String normalized = normalizeText(status);
        if (normalized.isEmpty() && applyDefault) {
            return ORDER_STATUS_CREATED;
        }
        if (normalized.isEmpty()) {
            return "";
        }
        boolean valid = ORDER_STATUS_CREATED.equals(normalized)
                || ORDER_STATUS_PROCESSING.equals(normalized)
                || ORDER_STATUS_COMPLETED.equals(normalized)
                || ORDER_STATUS_RECEIVED.equals(normalized)
                || (allowReconciled && ORDER_STATUS_RECONCILED.equals(normalized));
        if (!valid) {
            throw new IllegalArgumentException("订单状态不合法");
        }
        return normalized;
    }

    private BigDecimal normalizeMoney(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value.setScale(2, RoundingMode.HALF_UP);
    }

    private Long normalizeReferenceId(Long value) {
        return value != null && value > 0 ? value : null;
    }

    private int normalizeQuantity(Integer value) {
        return value == null ? 0 : value;
    }

    private BigDecimal calculateTotal(BigDecimal unitPrice, Integer quantity) {
        return normalizeMoney(unitPrice).multiply(BigDecimal.valueOf(normalizeQuantity(quantity))).setScale(2, RoundingMode.HALF_UP);
    }

    private LabFactory requireExistingFactory(Long factoryId) {
        if (factoryId == null || factoryId <= 0) {
            throw new IllegalArgumentException("加工厂不能为空");
        }
        LabFactory factory = labFactoryMapper.selectById(factoryId);
        if (factory == null) {
            throw new IllegalArgumentException("加工厂不存在");
        }
        return factory;
    }

    private Patient requireExistingPatient(Long patientId) {
        if (patientId == null || patientId <= 0) {
            throw new IllegalArgumentException("患者不能为空");
        }
        List<Patient> patients = patientMapper.selectById(patientId);
        if (patients == null || patients.isEmpty() || patients.get(0) == null) {
            throw new IllegalArgumentException("患者不存在");
        }
        return patients.get(0);
    }

    private Treatment requireMatchingTreatment(Long treatmentId, Patient patient) {
        if (treatmentId == null || treatmentId <= 0) {
            return null;
        }
        List<Treatment> treatments = treatmentMapper.selectById(treatmentId);
        if (treatments == null || treatments.isEmpty() || treatments.get(0) == null) {
            throw new IllegalArgumentException("治疗记录不存在");
        }
        Treatment treatment = treatments.get(0);
        if (patient != null) {
            boolean patientMatched = treatment.getPatient_id() != null
                    && treatment.getPatient_id() > 0
                    && patient.getId() == treatment.getPatient_id();
            if (!patientMatched) {
                throw new IllegalArgumentException("治疗记录与患者不匹配");
            }
        }
        return treatment;
    }

    private LabOrder requireExistingOrder(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("订单ID不能为空");
        }
        LabOrder order = labOrderMapper.selectById(id);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        return order;
    }

    private LocalDate parseDate(String value) {
        String normalized = normalizeText(value);
        if (normalized.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(normalized);
        } catch (Exception error) {
            throw new IllegalArgumentException("日期格式应为yyyy-MM-dd");
        }
    }

    private java.util.Date parseUtilDate(String value) {
        LocalDate date = parseDate(value);
        if (date == null) {
            return null;
        }
        return java.sql.Date.valueOf(date);
    }

    private YearMonth parseYearMonth(String billMonth) {
        String normalized = normalizeText(billMonth);
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("账单月份不能为空");
        }
        try {
            return YearMonth.parse(normalized);
        } catch (Exception error) {
            throw new IllegalArgumentException("账单月份格式应为yyyy-MM");
        }
    }

    private String normalizeText(String value) {
        return value == null ? "" : value.trim();
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
