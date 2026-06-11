package com.example.springboot.service;

import com.example.springboot.entity.MedicalRecord;
import com.example.springboot.entity.MedicalRecordOperation;
import com.example.springboot.entity.ProjectOperationRelation;
import com.example.springboot.entity.Treatment;
import com.example.springboot.entity.TreatmentBatchCreateRequest;
import com.example.springboot.entity.TreatmentBatchItemRequest;
import com.example.springboot.entity.TreatmentOperationAllocation;
import com.example.springboot.mapper.TreatmentOperationAllocationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class TreatmentOperationAllocationService {

    private final TreatmentOperationAllocationMapper allocationMapper;
    private final MedicalRecordService medicalRecordService;
    private final MedicalRecordOperationService medicalRecordOperationService;
    private final ProjectOperationRelationService relationService;

    @Autowired
    public TreatmentOperationAllocationService(TreatmentOperationAllocationMapper allocationMapper,
                                               MedicalRecordService medicalRecordService,
                                               MedicalRecordOperationService medicalRecordOperationService,
                                               ProjectOperationRelationService relationService) {
        this.allocationMapper = allocationMapper;
        this.medicalRecordService = medicalRecordService;
        this.medicalRecordOperationService = medicalRecordOperationService;
        this.relationService = relationService;
    }

    public List<TreatmentOperationAllocation> selectByTreatmentIds(List<Long> treatmentIds) {
        List<Long> normalizedIds = (treatmentIds == null ? List.<Long>of() : treatmentIds).stream()
                .filter(Objects::nonNull)
                .filter(id -> id > 0)
                .distinct()
                .toList();
        if (normalizedIds.isEmpty()) {
            return List.of();
        }
        return allocationMapper.selectByTreatmentIds(normalizedIds);
    }

    public List<TreatmentOperationAllocation> selectByTreatmentId(Long treatmentId) {
        if (treatmentId == null || treatmentId <= 0) {
            return List.of();
        }
        return allocationMapper.selectByTreatmentId(treatmentId);
    }

    @Transactional
    public void replaceByBatch(List<Treatment> treatments, TreatmentBatchCreateRequest request) {
        if (treatments == null || treatments.isEmpty()) {
            return;
        }
        List<TreatmentBatchItemRequest> items = request == null || request.getItems() == null ? List.of() : request.getItems();
        Long preferredMedicalRecordId = normalizePositiveId(request == null ? null : request.getMedical_record_id());
        for (int index = 0; index < treatments.size(); index++) {
            Treatment treatment = treatments.get(index);
            TreatmentBatchItemRequest item = index < items.size() ? items.get(index) : null;
            replaceByTreatment(treatment, preferredMedicalRecordId, item);
        }
    }

    @Transactional
    public void replaceByTreatment(Treatment treatment, Long preferredMedicalRecordId, TreatmentBatchItemRequest item) {
        if (treatment == null || treatment.getId() == null || treatment.getId() <= 0) {
            return;
        }
        allocationMapper.deleteByTreatmentId(treatment.getId());

        Long projectId = normalizePositiveId(item != null ? item.getProject_id() : treatment.getProject_id());
        if (projectId == null) {
            return;
        }

        Long medicalRecordId = resolveMedicalRecordId(treatment, preferredMedicalRecordId, projectId, item);
        if (medicalRecordId == null) {
            return;
        }

        List<MedicalRecordOperation> operations = medicalRecordOperationService.selectByMedicalRecordId(medicalRecordId);
        List<MedicalRecordOperation> candidates = filterCandidateOperations(operations, projectId, item);
        if (candidates.isEmpty()) {
            return;
        }

        Map<Long, ProjectOperationRelation> relationByOperationId = buildRelationMap(projectId);
        List<Double> weights = candidates.stream()
                .map(itemRow -> resolvePerformanceWeight(relationByOperationId.get(itemRow.getOperation_id())))
                .toList();
        List<Double> ratios = allocateRatios(weights);
        List<Double> turnoverAmounts = allocateAmounts(ratios, parseAmount(treatment.getTreatment_fee()));

        for (int index = 0; index < candidates.size(); index++) {
            MedicalRecordOperation current = candidates.get(index);
            TreatmentOperationAllocation allocation = new TreatmentOperationAllocation();
            allocation.setTreatment_id(treatment.getId());
            allocation.setMedical_record_id(medicalRecordId);
            allocation.setMedical_record_operation_id(current.getId());
            allocation.setPatient_id(treatment.getPatient_id());
            allocation.setDoctor_account_id(treatment.getDoctor_account_id());
            allocation.setDoctor_name(trimToEmpty(treatment.getDoctor_name()));
            allocation.setProject_id(projectId);
            allocation.setProject_name(trimToEmpty(current.getProject_name()));
            allocation.setOperation_id(current.getOperation_id());
            allocation.setOperation_name(trimToEmpty(current.getOperation_name()));
            allocation.setPerformance_weight(weights.get(index));
            allocation.setAllocation_ratio(ratios.get(index));
            allocation.setAllocated_turnover_amount(turnoverAmounts.get(index));
            allocationMapper.insert(allocation);
        }
    }

    @Transactional
    public void deleteByTreatmentId(Long treatmentId) {
        if (treatmentId == null || treatmentId <= 0) {
            return;
        }
        allocationMapper.deleteByTreatmentId(treatmentId);
    }

    private Long resolveMedicalRecordId(Treatment treatment,
                                        Long preferredMedicalRecordId,
                                        Long projectId,
                                        TreatmentBatchItemRequest item) {
        Long treatmentMedicalRecordId = normalizePositiveId(treatment.getMedical_record_id());
        if (treatmentMedicalRecordId != null) {
            return treatmentMedicalRecordId;
        }
        if (preferredMedicalRecordId != null) {
            return preferredMedicalRecordId;
        }
        Long patientId = normalizePositiveId(treatment.getPatient_id());
        if (patientId == null) {
            return null;
        }
        List<MedicalRecord> records = medicalRecordService.selectByPatientId(patientId);
        for (MedicalRecord record : records) {
            if (record == null || record.getId() == null || record.getId() <= 0) {
                continue;
            }
            List<MedicalRecordOperation> operations = medicalRecordOperationService.selectByMedicalRecordId(record.getId());
            if (!filterCandidateOperations(operations, projectId, item).isEmpty()) {
                return record.getId();
            }
        }
        return null;
    }

    private List<MedicalRecordOperation> filterCandidateOperations(List<MedicalRecordOperation> operations,
                                                                   Long projectId,
                                                                   TreatmentBatchItemRequest item) {
        List<MedicalRecordOperation> sameProject = (operations == null ? List.<MedicalRecordOperation>of() : operations).stream()
                .filter(Objects::nonNull)
                .filter(row -> Objects.equals(normalizePositiveId(row.getProject_id()), projectId))
                .toList();
        if (sameProject.isEmpty()) {
            return List.of();
        }
        String treatmentToothPositions = trimToEmpty(item == null ? null : item.getTooth_positions());
        if (treatmentToothPositions.isEmpty()) {
            return sameProject;
        }
        List<MedicalRecordOperation> matchedByTooth = sameProject.stream()
                .filter(row -> hasToothOverlap(treatmentToothPositions, row.getTooth_positions()))
                .toList();
        return matchedByTooth.isEmpty() ? sameProject : matchedByTooth;
    }

    private boolean hasToothOverlap(String left, String right) {
        Set<String> leftSet = splitToothPositions(left);
        Set<String> rightSet = splitToothPositions(right);
        if (leftSet.isEmpty() || rightSet.isEmpty()) {
            return false;
        }
        for (String value : leftSet) {
            if (rightSet.contains(value)) {
                return true;
            }
        }
        return false;
    }

    private Set<String> splitToothPositions(String value) {
        Set<String> result = new LinkedHashSet<>();
        if (!StringUtils.hasText(value)) {
            return result;
        }
        for (String part : value.split(",")) {
            String normalized = trimToEmpty(part);
            if (!normalized.isEmpty()) {
                result.add(normalized);
            }
        }
        return result;
    }

    private Map<Long, ProjectOperationRelation> buildRelationMap(Long projectId) {
        Map<Long, ProjectOperationRelation> result = new LinkedHashMap<>();
        for (ProjectOperationRelation relation : relationService.selectByProjectId(projectId)) {
            if (relation == null || relation.getOperation_id() == null || relation.getOperation_id() <= 0) {
                continue;
            }
            result.putIfAbsent(relation.getOperation_id(), relation);
        }
        return result;
    }

    private Double resolvePerformanceWeight(ProjectOperationRelation relation) {
        if (relation == null || relation.getPerformance_weight() == null) {
            return 1D;
        }
        return relation.getPerformance_weight() < 0 ? 0D : relation.getPerformance_weight();
    }

    private List<Double> allocateRatios(List<Double> weights) {
        List<Double> result = new ArrayList<>();
        if (weights == null || weights.isEmpty()) {
            return result;
        }
        double positiveTotal = weights.stream().mapToDouble(item -> item != null && item > 0 ? item : 0D).sum();
        if (positiveTotal <= 0) {
            for (int index = 0; index < weights.size(); index++) {
                result.add(0D);
            }
            return result;
        }
        double allocated = 0D;
        int lastPositiveIndex = -1;
        for (int index = 0; index < weights.size(); index++) {
            if (weights.get(index) != null && weights.get(index) > 0) {
                lastPositiveIndex = index;
            }
        }
        for (int index = 0; index < weights.size(); index++) {
            double weight = weights.get(index) == null ? 0D : weights.get(index);
            double ratio;
            if (weight <= 0) {
                ratio = 0D;
            } else if (index == lastPositiveIndex) {
                ratio = round6(1D - allocated);
            } else {
                ratio = round6(weight / positiveTotal);
                allocated = round6(allocated + ratio);
            }
            result.add(Math.max(0D, ratio));
        }
        return result;
    }

    private List<Double> allocateAmounts(List<Double> ratios, double totalAmount) {
        List<Double> result = new ArrayList<>();
        if (ratios == null || ratios.isEmpty()) {
            return result;
        }
        if (totalAmount <= 0) {
            for (int index = 0; index < ratios.size(); index++) {
                result.add(0D);
            }
            return result;
        }
        double allocated = 0D;
        int lastPositiveIndex = -1;
        for (int index = 0; index < ratios.size(); index++) {
            if (ratios.get(index) != null && ratios.get(index) > 0) {
                lastPositiveIndex = index;
            }
        }
        if (lastPositiveIndex < 0) {
            for (int index = 0; index < ratios.size(); index++) {
                result.add(0D);
            }
            return result;
        }
        for (int index = 0; index < ratios.size(); index++) {
            double ratio = ratios.get(index) == null ? 0D : ratios.get(index);
            double amount;
            if (ratio <= 0) {
                amount = 0D;
            } else if (index == lastPositiveIndex) {
                amount = round2(totalAmount - allocated);
            } else {
                amount = round2(totalAmount * ratio);
                allocated = round2(allocated + amount);
            }
            result.add(Math.max(0D, amount));
        }
        return result;
    }

    private Long normalizePositiveId(Long value) {
        return value != null && value > 0 ? value : null;
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private double parseAmount(String value) {
        if (!StringUtils.hasText(value)) {
            return 0D;
        }
        try {
            return round2(Double.parseDouble(value.trim()));
        } catch (Exception exception) {
            return 0D;
        }
    }

    private double round2(double value) {
        return Math.round(value * 100D) / 100D;
    }

    private double round6(double value) {
        return Math.round(value * 1_000_000D) / 1_000_000D;
    }
}
