package com.example.springboot.service;

import com.example.springboot.entity.MedicalRecord;
import com.example.springboot.entity.MedicalRecordOperation;
import com.example.springboot.entity.LabOrder;
import com.example.springboot.entity.LabFactory;
import com.example.springboot.entity.TreatmentOperation;
import com.example.springboot.entity.TreatmentProject;
import com.example.springboot.mapper.LabOrderMapper;
import com.example.springboot.mapper.MedicalRecordOperationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MedicalRecordOperationService {

    private final MedicalRecordOperationMapper operationMapper;
    private final TreatmentOperationService treatmentOperationService;
    private final TreatmentProjectService treatmentProjectService;
    private final LabFactoryService labFactoryService;
    private final LabOrderMapper labOrderMapper;

    @Autowired
    public MedicalRecordOperationService(MedicalRecordOperationMapper operationMapper,
                                         TreatmentOperationService treatmentOperationService,
                                         TreatmentProjectService treatmentProjectService,
                                         LabFactoryService labFactoryService,
                                         LabOrderMapper labOrderMapper) {
        this.operationMapper = operationMapper;
        this.treatmentOperationService = treatmentOperationService;
        this.treatmentProjectService = treatmentProjectService;
        this.labFactoryService = labFactoryService;
        this.labOrderMapper = labOrderMapper;
    }

    public List<MedicalRecordOperation> selectByMedicalRecordId(Long medicalRecordId) {
        if (medicalRecordId == null || medicalRecordId <= 0) {
            return List.of();
        }
        return operationMapper.selectByMedicalRecordId(medicalRecordId);
    }

    public MedicalRecordOperation selectPlainById(Long id) {
        if (id == null || id <= 0) {
            return null;
        }
        return operationMapper.selectPlainById(id);
    }

    public List<MedicalRecordOperation> searchPendingLabList(Long patientId, Long doctorAccountId) {
        return operationMapper.selectPendingLabList(patientId, doctorAccountId);
    }

    @Transactional
    public int backfillPendingLabOrders(Long patientId, Long doctorAccountId) {
        List<MedicalRecordOperation> operations = searchPendingLabList(patientId, doctorAccountId);
        if (operations == null || operations.isEmpty()) {
            return 0;
        }
        int createdCount = 0;
        for (MedicalRecordOperation operation : operations) {
            if (!shouldSyncLabOrder(operation) || !hasFactory(operation)) {
                continue;
            }
            List<LabOrder> linkedOrders = findLinkedLabOrders(operation.getId());
            if (linkedOrders.isEmpty()) {
                LabOrder autoOrder = buildAutoLabOrder(operation);
                if (autoOrder == null) {
                    continue;
                }
                labOrderMapper.insert(autoOrder);
                createdCount += 1;
            } else {
                for (LabOrder linkedOrder : linkedOrders) {
                    syncLinkedLabOrder(operation, linkedOrder);
                }
            }
            if (operation.getLab_order_status() == null || operation.getLab_order_status() != 1) {
                markLabOrderRegistered(operation.getId(), operation.getDoctor_account_id(), operation.getDoctor_name());
            }
        }
        return createdCount;
    }

    public void enrichMedicalRecords(List<MedicalRecord> records, boolean includeItems) {
        if (records == null || records.isEmpty()) {
            return;
        }
        List<Long> recordIds = records.stream()
                .filter(Objects::nonNull)
                .map(MedicalRecord::getId)
                .filter(Objects::nonNull)
                .filter(id -> id > 0)
                .distinct()
                .toList();
        if (recordIds.isEmpty()) {
            return;
        }
        List<MedicalRecordOperation> operations = operationMapper.selectByMedicalRecordIds(recordIds);
        Map<Long, List<MedicalRecordOperation>> grouped = operations.stream()
                .collect(Collectors.groupingBy(MedicalRecordOperation::getMedical_record_id, LinkedHashMap::new, Collectors.toList()));
        for (MedicalRecord record : records) {
            if (record == null || record.getId() == null) {
                continue;
            }
            List<MedicalRecordOperation> current = grouped.getOrDefault(record.getId(), List.of());
            record.setOperation_count(current.size());
            record.setPending_lab_count(countPendingLab(current));
            record.setOperation_summary(buildOperationSummary(current));
            if (includeItems) {
                record.setOperation_items(new ArrayList<>(current));
            }
        }
    }

    public void enrichMedicalRecord(MedicalRecord record, boolean includeItems) {
        if (record == null) {
            return;
        }
        List<MedicalRecordOperation> operations = selectByMedicalRecordId(record.getId());
        record.setOperation_count(operations.size());
        record.setPending_lab_count(countPendingLab(operations));
        record.setOperation_summary(buildOperationSummary(operations));
        if (includeItems) {
            record.setOperation_items(operations);
        }
    }

    @Transactional
    public void replaceByMedicalRecord(Long medicalRecordId,
                                       List<MedicalRecordOperation> items,
                                       Long operatorId,
                                       String operatorName) {
        if (medicalRecordId == null || medicalRecordId <= 0) {
            throw new IllegalArgumentException("病历ID不能为空");
        }
        List<MedicalRecordOperation> existingItems = operationMapper.selectByMedicalRecordId(medicalRecordId);
        Map<Long, MedicalRecordOperation> existingMap = existingItems.stream()
                .filter(Objects::nonNull)
                .filter(item -> item.getId() != null && item.getId() > 0)
                .collect(Collectors.toMap(MedicalRecordOperation::getId, item -> item, (left, right) -> left, LinkedHashMap::new));
        Set<Long> retainedIds = new HashSet<>();

        if (items != null) {
            for (MedicalRecordOperation source : items) {
                MedicalRecordOperation normalized = normalizeForSave(medicalRecordId, source, operatorId, operatorName);
                if (normalized == null) {
                    continue;
                }
                Long sourceId = normalizeReferenceId(source == null ? null : source.getId());
                MedicalRecordOperation existing = sourceId == null ? null : existingMap.get(sourceId);
                if (existing != null) {
                    normalized.setId(existing.getId());
                    normalized.setLab_order_status(resolveRetainedLabOrderStatus(source, existing));
                    normalized.setSkip_reason(resolveRetainedSkipReason(source, existing));
                    normalized.setLab_order_registered_at(resolveRetainedRegisteredAt(source, existing));
                    operationMapper.update(normalized);
                    retainedIds.add(existing.getId());
                    continue;
                }
                operationMapper.insert(normalized);
                if (normalized.getId() != null && normalized.getId() > 0) {
                    retainedIds.add(normalized.getId());
                }
            }
        }

        for (MedicalRecordOperation existing : existingItems) {
            Long existingId = normalizeReferenceId(existing == null ? null : existing.getId());
            if (existingId == null || retainedIds.contains(existingId)) {
                continue;
            }
            detachLinkedLabOrders(existingId);
            operationMapper.deleteById(existingId);
        }
    }

    @Transactional
    public MedicalRecordOperation markSkip(Long id, String skipReason, Long updatedBy, String updatedByName) {
        MedicalRecordOperation item = requireExisting(id);
        item.setLab_order_status(2);
        item.setSkip_reason(StringUtils.hasText(skipReason) ? skipReason.trim() : null);
        item.setUpdated_by(updatedBy);
        item.setUpdated_by_name(StringUtils.hasText(updatedByName) ? updatedByName.trim() : null);
        operationMapper.update(item);
        return requireExisting(id);
    }

    @Transactional
    public void markLabOrderRegistered(Long id, Long updatedBy, String updatedByName) {
        MedicalRecordOperation item = requireExisting(id);
        item.setLab_order_status(1);
        item.setSkip_reason(null);
        item.setLab_order_registered_at(new Date());
        item.setUpdated_by(updatedBy);
        item.setUpdated_by_name(StringUtils.hasText(updatedByName) ? updatedByName.trim() : null);
        operationMapper.update(item);
    }

    @Transactional
    public void resetLabOrderRegistration(Long id, Long updatedBy, String updatedByName) {
        MedicalRecordOperation item = requireExisting(id);
        item.setLab_order_status(0);
        item.setSkip_reason(null);
        item.setLab_order_registered_at(null);
        item.setUpdated_by(updatedBy);
        item.setUpdated_by_name(StringUtils.hasText(updatedByName) ? updatedByName.trim() : null);
        operationMapper.update(item);
    }

    @Transactional
    public void syncLabOrdersForMedicalRecord(MedicalRecord record) {
        if (record == null || record.getId() == null || record.getId() <= 0 || operationMapper == null) {
            return;
        }
        List<MedicalRecordOperation> operations = operationMapper.selectByMedicalRecordId(record.getId());
        if (operations == null || operations.isEmpty()) {
            return;
        }
        for (MedicalRecordOperation operation : operations) {
            if (!shouldSyncLabOrder(operation)) {
                continue;
            }
            List<LabOrder> linkedOrders = findLinkedLabOrders(operation.getId());
            if (linkedOrders.isEmpty()) {
                if (!hasFactory(operation)) {
                    continue;
                }
                LabOrder autoOrder = buildAutoLabOrder(record, operation);
                if (autoOrder == null) {
                    continue;
                }
                labOrderMapper.insert(autoOrder);
            } else {
                for (LabOrder linkedOrder : linkedOrders) {
                    syncLinkedLabOrder(record, operation, linkedOrder);
                }
            }
            if (operation.getLab_order_status() == null || operation.getLab_order_status() != 1) {
                markLabOrderRegistered(operation.getId(), record.getDoctor_account_id(), record.getDoctor_name());
            }
        }
    }

    private MedicalRecordOperation normalizeForSave(Long medicalRecordId,
                                                    MedicalRecordOperation source,
                                                    Long operatorId,
                                                    String operatorName) {
        if (source == null) {
            return null;
        }
        // 支持自由文本操作名称（不强制绑定操作字典）
        boolean hasOperationId = source.getOperation_id() != null && source.getOperation_id() > 0;
        boolean hasOperationName = StringUtils.hasText(source.getOperation_name());
        if (!hasOperationId && !hasOperationName) {
            return null;
        }
        MedicalRecordOperation item = new MedicalRecordOperation();
        item.setMedical_record_id(medicalRecordId);
        item.setProject_id(source.getProject_id() != null && source.getProject_id() > 0 ? source.getProject_id() : null);
        item.setProject_name(resolveProjectName(source));
        item.setOperation_id(hasOperationId ? source.getOperation_id() : null);
        TreatmentOperation operation = hasOperationId ? treatmentOperationService.selectById(source.getOperation_id()) : null;
        item.setOperation_name(resolveOperationName(source, operation));
        applyFactorySnapshot(item, source, operation);
        item.setTooth_positions(trimToNull(source.getTooth_positions()));
        item.setRemark(trimToNull(source.getRemark()));
        item.setLab_order_status(normalizeLabOrderStatus(source.getLab_order_status()));
        item.setSkip_reason(trimToNull(source.getSkip_reason()));
        item.setLab_order_registered_at(item.getLab_order_status() != null && item.getLab_order_status() == 1 ? source.getLab_order_registered_at() : null);
        item.setCreated_by(operatorId);
        item.setCreated_by_name(StringUtils.hasText(operatorName) ? operatorName.trim() : null);
        item.setUpdated_by(operatorId);
        item.setUpdated_by_name(StringUtils.hasText(operatorName) ? operatorName.trim() : null);
        return item;
    }

    private Integer resolveRetainedLabOrderStatus(MedicalRecordOperation source, MedicalRecordOperation existing) {
        if (source != null && source.getLab_order_status() != null) {
            return normalizeLabOrderStatus(source.getLab_order_status());
        }
        return normalizeLabOrderStatus(existing == null ? null : existing.getLab_order_status());
    }

    private String resolveRetainedSkipReason(MedicalRecordOperation source, MedicalRecordOperation existing) {
        if (source != null && source.getSkip_reason() != null) {
            return trimToNull(source.getSkip_reason());
        }
        return trimToNull(existing == null ? null : existing.getSkip_reason());
    }

    private Date resolveRetainedRegisteredAt(MedicalRecordOperation source, MedicalRecordOperation existing) {
        if (source != null && source.getLab_order_registered_at() != null) {
            return source.getLab_order_registered_at();
        }
        return existing == null ? null : existing.getLab_order_registered_at();
    }

    private String resolveProjectName(MedicalRecordOperation source) {
        if (source == null) {
            return null;
        }
        if (source.getProject_id() != null && source.getProject_id() > 0) {
            TreatmentProject project = treatmentProjectService.selectById(source.getProject_id());
            if (project != null && StringUtils.hasText(project.getProject_name())) {
                return project.getProject_name().trim();
            }
        }
        return trimToNull(source.getProject_name());
    }

    private String resolveOperationName(MedicalRecordOperation source, TreatmentOperation operation) {
        if (source == null) {
            return null;
        }
        if (operation != null && StringUtils.hasText(operation.getOperation_name())) {
            return operation.getOperation_name().trim();
        }
        return trimToNull(source.getOperation_name());
    }

    private void applyFactorySnapshot(MedicalRecordOperation target,
                                      MedicalRecordOperation source,
                                      TreatmentOperation operation) {
        if (target == null || source == null) {
            return;
        }
        boolean needLabProcessing = (operation != null && operation.getNeed_lab_processing() != null && operation.getNeed_lab_processing() == 1)
                || (source.getNeed_lab_processing() != null && source.getNeed_lab_processing() == 1);
        if (!needLabProcessing) {
            target.setFactory_id(null);
            target.setFactory_name(null);
            target.setNeed_lab_processing(0);
            return;
        }
        target.setNeed_lab_processing(1);
        Long factoryId = source.getFactory_id() != null && source.getFactory_id() > 0 ? source.getFactory_id() : null;
        if (factoryId == null) {
            target.setFactory_id(null);
            target.setFactory_name(null);
            return;
        }
        LabFactory factory = labFactoryService == null ? null : labFactoryService.selectById(factoryId);
        if (factory == null || !StringUtils.hasText(factory.getName())) {
            throw new IllegalArgumentException("所选加工厂不存在");
        }
        target.setFactory_id(factory.getId());
        target.setFactory_name(factory.getName().trim());
    }

    private Integer normalizeLabOrderStatus(Integer value) {
        if (value == null) {
            return 0;
        }
        return switch (value) {
            case 1 -> 1;
            case 2 -> 2;
            default -> 0;
        };
    }

    private boolean shouldSyncLabOrder(MedicalRecordOperation operation) {
        if (operation == null || operation.getId() == null || operation.getId() <= 0) {
            return false;
        }
        if (operation.getNeed_lab_processing() == null || operation.getNeed_lab_processing() != 1) {
            return false;
        }
        return operation.getLab_order_status() == null || operation.getLab_order_status() != 2;
    }

    private boolean hasFactory(MedicalRecordOperation operation) {
        return normalizeReferenceId(operation == null ? null : operation.getFactory_id()) != null;
    }

    private List<LabOrder> findLinkedLabOrders(Long medicalRecordOperationId) {
        if (labOrderMapper == null || medicalRecordOperationId == null || medicalRecordOperationId <= 0) {
            return List.of();
        }
        List<LabOrder> linkedOrders = labOrderMapper.selectByMedicalRecordOperationId(medicalRecordOperationId);
        return linkedOrders == null ? List.of() : linkedOrders.stream().filter(Objects::nonNull).toList();
    }

    private void detachLinkedLabOrders(Long medicalRecordOperationId) {
        if (labOrderMapper == null || medicalRecordOperationId == null || medicalRecordOperationId <= 0) {
            return;
        }
        for (LabOrder linkedOrder : findLinkedLabOrders(medicalRecordOperationId)) {
            linkedOrder.setMedical_record_operation_id(null);
            labOrderMapper.update(linkedOrder);
        }
    }

    private LabOrder buildAutoLabOrder(MedicalRecord record, MedicalRecordOperation operation) {
        if (record == null || operation == null || labOrderMapper == null) {
            return null;
        }
        Long factoryId = normalizeReferenceId(operation.getFactory_id());
        if (factoryId == null) {
            return null;
        }
        String factoryName = trimToNull(operation.getFactory_name());
        if (!StringUtils.hasText(factoryName) && labFactoryService != null) {
            LabFactory factory = labFactoryService.selectById(factoryId);
            factoryName = factory != null ? trimToNull(factory.getName()) : null;
        }
        if (!StringUtils.hasText(factoryName)) {
            return null;
        }
        Date orderDate = record.getVisit_date() != null ? record.getVisit_date() : new Date();
        LabOrder order = new LabOrder();
        order.setFactory_id(factoryId);
        order.setFactory_name(factoryName);
        order.setPatient_id(record.getPatient_id());
        order.setPatient_name(trimToNull(record.getPatient_name()));
        order.setTreatment_id(null);
        order.setMedical_record_operation_id(operation.getId());
        order.setMedical_record_id(record.getId());
        order.setProject_id(normalizeReferenceId(operation.getProject_id()));
        order.setProject_name(trimToNull(operation.getProject_name()));
        order.setOperation_id(normalizeReferenceId(operation.getOperation_id()));
        order.setOperation_name(trimToNull(operation.getOperation_name()));
        order.setTooth_positions(trimToNull(operation.getTooth_positions()));
        order.setProduct_name(resolveAutoProductName(operation));
        order.setProduct_spec(null);
        order.setUnit_price(BigDecimal.ZERO.setScale(2));
        order.setQuantity(1);
        order.setTotal_amount(BigDecimal.ZERO.setScale(2));
        order.setOrder_date(orderDate);
        order.setExpected_delivery_date(resolveExpectedDeliveryDate(orderDate, operation.getDefault_processing_days()));
        order.setActual_delivery_date(null);
        order.setStatus(LabOrderService.ORDER_STATUS_CREATED);
        order.setRemark(trimToNull(operation.getRemark()));
        order.setCreated_by(record.getDoctor_account_id());
        order.setCreated_by_name(trimToNull(record.getDoctor_name()));
        return order;
    }

    private LabOrder buildAutoLabOrder(MedicalRecordOperation operation) {
        if (operation == null || operation.getMedical_record_id() == null || operation.getMedical_record_id() <= 0 || labOrderMapper == null) {
            return null;
        }
        Long factoryId = normalizeReferenceId(operation.getFactory_id());
        if (factoryId == null) {
            return null;
        }
        String factoryName = trimToNull(operation.getFactory_name());
        if (!StringUtils.hasText(factoryName) && labFactoryService != null) {
            LabFactory factory = labFactoryService.selectById(factoryId);
            factoryName = factory != null ? trimToNull(factory.getName()) : null;
        }
        if (!StringUtils.hasText(factoryName)) {
            return null;
        }
        Date orderDate = operation.getVisit_date() != null ? operation.getVisit_date() : new Date();
        LabOrder order = new LabOrder();
        order.setFactory_id(factoryId);
        order.setFactory_name(factoryName);
        order.setPatient_id(operation.getPatient_id());
        order.setPatient_name(trimToNull(operation.getPatient_name()));
        order.setTreatment_id(null);
        order.setMedical_record_operation_id(operation.getId());
        order.setMedical_record_id(operation.getMedical_record_id());
        order.setProject_id(normalizeReferenceId(operation.getProject_id()));
        order.setProject_name(trimToNull(operation.getProject_name()));
        order.setOperation_id(normalizeReferenceId(operation.getOperation_id()));
        order.setOperation_name(trimToNull(operation.getOperation_name()));
        order.setTooth_positions(trimToNull(operation.getTooth_positions()));
        order.setProduct_name(resolveAutoProductName(operation));
        order.setProduct_spec(null);
        order.setUnit_price(BigDecimal.ZERO.setScale(2));
        order.setQuantity(1);
        order.setTotal_amount(BigDecimal.ZERO.setScale(2));
        order.setOrder_date(orderDate);
        order.setExpected_delivery_date(resolveExpectedDeliveryDate(orderDate, operation.getDefault_processing_days()));
        order.setActual_delivery_date(null);
        order.setStatus(LabOrderService.ORDER_STATUS_CREATED);
        order.setRemark(trimToNull(operation.getRemark()));
        order.setCreated_by(operation.getDoctor_account_id());
        order.setCreated_by_name(trimToNull(operation.getDoctor_name()));
        return order;
    }

    private void syncLinkedLabOrder(MedicalRecord record, MedicalRecordOperation operation, LabOrder linkedOrder) {
        if (record == null || operation == null || linkedOrder == null || labOrderMapper == null) {
            return;
        }
        linkedOrder.setMedical_record_id(record.getId());
        linkedOrder.setMedical_record_operation_id(operation.getId());
        linkedOrder.setPatient_id(record.getPatient_id());
        linkedOrder.setPatient_name(trimToNull(record.getPatient_name()));
        linkedOrder.setProject_id(normalizeReferenceId(operation.getProject_id()));
        linkedOrder.setProject_name(trimToNull(operation.getProject_name()));
        linkedOrder.setOperation_id(normalizeReferenceId(operation.getOperation_id()));
        linkedOrder.setOperation_name(trimToNull(operation.getOperation_name()));
        linkedOrder.setTooth_positions(trimToNull(operation.getTooth_positions()));

        Long factoryId = normalizeReferenceId(operation.getFactory_id());
        if (factoryId != null) {
            linkedOrder.setFactory_id(factoryId);
            String factoryName = trimToNull(operation.getFactory_name());
            if (!StringUtils.hasText(factoryName) && labFactoryService != null) {
                LabFactory factory = labFactoryService.selectById(factoryId);
                factoryName = factory != null ? trimToNull(factory.getName()) : null;
            }
            if (StringUtils.hasText(factoryName)) {
                linkedOrder.setFactory_name(factoryName);
            }
        }
        if (!StringUtils.hasText(linkedOrder.getProduct_name())) {
            linkedOrder.setProduct_name(resolveAutoProductName(operation));
        }
        if (linkedOrder.getUnit_price() == null) {
            linkedOrder.setUnit_price(BigDecimal.ZERO.setScale(2));
        }
        if (linkedOrder.getQuantity() == null || linkedOrder.getQuantity() <= 0) {
            linkedOrder.setQuantity(1);
        }
        if (linkedOrder.getTotal_amount() == null) {
            linkedOrder.setTotal_amount(BigDecimal.ZERO.setScale(2));
        }
        if (linkedOrder.getOrder_date() == null) {
            linkedOrder.setOrder_date(record.getVisit_date() != null ? record.getVisit_date() : new Date());
        }
        if (linkedOrder.getExpected_delivery_date() == null) {
            linkedOrder.setExpected_delivery_date(resolveExpectedDeliveryDate(linkedOrder.getOrder_date(), operation.getDefault_processing_days()));
        }
        if (!StringUtils.hasText(linkedOrder.getRemark())) {
            linkedOrder.setRemark(trimToNull(operation.getRemark()));
        }
        labOrderMapper.update(linkedOrder);
    }

    private void syncLinkedLabOrder(MedicalRecordOperation operation, LabOrder linkedOrder) {
        if (operation == null || linkedOrder == null || labOrderMapper == null) {
            return;
        }
        linkedOrder.setMedical_record_id(operation.getMedical_record_id());
        linkedOrder.setMedical_record_operation_id(operation.getId());
        linkedOrder.setPatient_id(operation.getPatient_id());
        linkedOrder.setPatient_name(trimToNull(operation.getPatient_name()));
        linkedOrder.setProject_id(normalizeReferenceId(operation.getProject_id()));
        linkedOrder.setProject_name(trimToNull(operation.getProject_name()));
        linkedOrder.setOperation_id(normalizeReferenceId(operation.getOperation_id()));
        linkedOrder.setOperation_name(trimToNull(operation.getOperation_name()));
        linkedOrder.setTooth_positions(trimToNull(operation.getTooth_positions()));

        Long factoryId = normalizeReferenceId(operation.getFactory_id());
        if (factoryId != null) {
            linkedOrder.setFactory_id(factoryId);
            String factoryName = trimToNull(operation.getFactory_name());
            if (!StringUtils.hasText(factoryName) && labFactoryService != null) {
                LabFactory factory = labFactoryService.selectById(factoryId);
                factoryName = factory != null ? trimToNull(factory.getName()) : null;
            }
            if (StringUtils.hasText(factoryName)) {
                linkedOrder.setFactory_name(factoryName);
            }
        }
        if (!StringUtils.hasText(linkedOrder.getProduct_name())) {
            linkedOrder.setProduct_name(resolveAutoProductName(operation));
        }
        if (linkedOrder.getUnit_price() == null) {
            linkedOrder.setUnit_price(BigDecimal.ZERO.setScale(2));
        }
        if (linkedOrder.getQuantity() == null || linkedOrder.getQuantity() <= 0) {
            linkedOrder.setQuantity(1);
        }
        if (linkedOrder.getTotal_amount() == null) {
            linkedOrder.setTotal_amount(BigDecimal.ZERO.setScale(2));
        }
        if (linkedOrder.getOrder_date() == null) {
            linkedOrder.setOrder_date(operation.getVisit_date() != null ? operation.getVisit_date() : new Date());
        }
        if (linkedOrder.getExpected_delivery_date() == null) {
            linkedOrder.setExpected_delivery_date(resolveExpectedDeliveryDate(linkedOrder.getOrder_date(), operation.getDefault_processing_days()));
        }
        if (!StringUtils.hasText(linkedOrder.getRemark())) {
            linkedOrder.setRemark(trimToNull(operation.getRemark()));
        }
        labOrderMapper.update(linkedOrder);
    }

    private String resolveAutoProductName(MedicalRecordOperation operation) {
        String projectName = trimToNull(operation == null ? null : operation.getProject_name());
        if (projectName != null) {
            return projectName;
        }
        String operationName = trimToNull(operation == null ? null : operation.getOperation_name());
        if (operationName != null) {
            return operationName;
        }
        return "义齿加工";
    }

    private Date resolveExpectedDeliveryDate(Date orderDate, Integer defaultProcessingDays) {
        int days = defaultProcessingDays == null ? 0 : Math.max(defaultProcessingDays, 0);
        if (orderDate == null || days <= 0) {
            return null;
        }
        LocalDate expectedDate = Instant.ofEpochMilli(orderDate.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .plusDays(days);
        return Date.from(expectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private int countPendingLab(List<MedicalRecordOperation> operations) {
        if (operations == null || operations.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (MedicalRecordOperation item : operations) {
            if (item == null) {
                continue;
            }
            boolean needLab = item.getNeed_lab_processing() != null && item.getNeed_lab_processing() == 1;
            boolean pending = item.getLab_order_status() == null || item.getLab_order_status() == 0;
            if (needLab && pending) {
                count += 1;
            }
        }
        return count;
    }

    private String buildOperationSummary(List<MedicalRecordOperation> operations) {
        if (operations == null || operations.isEmpty()) {
            return "";
        }
        List<String> names = operations.stream()
                .filter(Objects::nonNull)
                .map(MedicalRecordOperation::getOperation_name)
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .toList();
        if (names.isEmpty()) {
            return "";
        }
        if (names.size() <= 3) {
            return String.join("、", names);
        }
        return String.join("、", names.subList(0, 3)) + "等" + names.size() + "项";
    }

    private MedicalRecordOperation requireExisting(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("病历操作ID不能为空");
        }
        MedicalRecordOperation item = operationMapper.selectPlainById(id);
        if (item == null) {
            throw new IllegalArgumentException("病历操作不存在");
        }
        return item;
    }

    private Long normalizeReferenceId(Long value) {
        return value != null && value > 0 ? value : null;
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
