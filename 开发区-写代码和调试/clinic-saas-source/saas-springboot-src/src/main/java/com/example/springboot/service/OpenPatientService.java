package com.example.springboot.service;

import com.example.springboot.entity.*;
import com.example.springboot.mapper.*;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;

/**
 * 开放数据接口中的患者相关查询服务
 * 从 OpenDataController 下沉，减少 Controller 构造函数参数
 */
@Service
public class OpenPatientService {

    private final PatientMapper patientMapper;
    private final MedicalRecordService medicalRecordService;
    private final TreatmentService treatmentService;
    private final TreatmentBillingService treatmentBillingService;
    private final PatientInsightSummaryService patientInsightSummaryService;
    private final PatientRiskTagMapper patientRiskTagMapper;
    private final PatientCustomGroupMapper patientCustomGroupMapper;
    private final PatientImageMapper patientImageMapper;
    private final PatientTimelineMapper patientTimelineMapper;

    public OpenPatientService(PatientMapper patientMapper,
                              MedicalRecordService medicalRecordService,
                              TreatmentService treatmentService,
                              TreatmentBillingService treatmentBillingService,
                              PatientInsightSummaryService patientInsightSummaryService,
                              PatientRiskTagMapper patientRiskTagMapper,
                              PatientCustomGroupMapper patientCustomGroupMapper,
                              PatientImageMapper patientImageMapper,
                              PatientTimelineMapper patientTimelineMapper) {
        this.patientMapper = patientMapper;
        this.medicalRecordService = medicalRecordService;
        this.treatmentService = treatmentService;
        this.treatmentBillingService = treatmentBillingService;
        this.patientInsightSummaryService = patientInsightSummaryService;
        this.patientRiskTagMapper = patientRiskTagMapper;
        this.patientCustomGroupMapper = patientCustomGroupMapper;
        this.patientImageMapper = patientImageMapper;
        this.patientTimelineMapper = patientTimelineMapper;
    }

    /**
     * 获取患者列表（支持分页和姓名模糊匹配）
     *
     * @param name 患者姓名（可选模糊匹配）
     * @param page 页码（默认1）
     * @param size 每页条数（默认10）
     * @return 分页患者列表
     */
    public PageInfo<Patient> getPatients(String name, int page, int size) {
        PageHelper.startPage(page, size);
        List<Patient> patients;
        if (StringUtils.hasText(name)) {
            patients = patientMapper.searchByKeyword(name.trim());
        } else {
            patients = patientMapper.selectAll();
        }
        return new PageInfo<>(patients);
    }

    /**
     * 获取患者基础详情（就诊次数、总费用、欠款等）
     *
     * @param patientId 患者ID
     * @return 患者详情数据
     */
    public Map<String, Object> getPatientDetails(Long patientId) {
        List<Patient> patients = patientMapper.selectById(patientId);
        if (patients == null || patients.isEmpty()) {
            return null;
        }
        Patient patient = patients.get(0);

        PatientInsightSummary insight = patientInsightSummaryService.getOrRefresh(patientId);

        List<Treatment> treatments = treatmentService.selectByPatientReference(patientId);
        treatmentBillingService.enrichTreatments(treatments);
        BigDecimal arrearsAmount = treatments.stream()
                .map(Treatment::getArrears_amount)
                .filter(Objects::nonNull)
                .map(a -> BigDecimal.valueOf(a))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("patient", patient);
        result.put("visitCount", insight == null || insight.getTotal_visit_count() == null ? 0 : insight.getTotal_visit_count());
        result.put("lastVisit", insight == null ? null : insight.getLast_visit_date());
        result.put("totalFee", insight == null || insight.getTotal_spent() == null ? 0D : insight.getTotal_spent().doubleValue());
        result.put("hasArrears", arrearsAmount.compareTo(BigDecimal.ZERO) > 0);
        result.put("arrearsAmount", arrearsAmount.doubleValue());
        return result;
    }

    /**
     * 按患者查询病历列表
     *
     * @param patientId 患者ID
     * @param page      页码（默认1）
     * @param size      每页条数（默认10）
     * @return 分页病历列表
     */
    public PageInfo<MedicalRecord> getPatientMedicalRecords(Long patientId, int page, int size) {
        PageHelper.startPage(page, size);
        List<MedicalRecord> list = medicalRecordService.selectByPatientId(patientId);
        return new PageInfo<>(list);
    }

    /**
     * 查询指定患者的风险标签列表
     *
     * @param id 患者ID
     * @return 风险标签列表（不分页）
     */
    public List<PatientRiskTag> getPatientRiskTags(Long id) {
        return patientRiskTagMapper.selectActiveByPatientId(id);
    }

    /**
     * 查询患者自定义分组列表（支持分页和关键词模糊匹配）
     *
     * @param keyword 关键词（匹配分组名称或分组标识）
     * @param page    页码（默认1）
     * @param size    每页条数（默认10）
     * @return 分页患者自定义分组列表
     */
    public PageInfo<PatientCustomGroup> getPatientGroups(String keyword, int page, int size) {
        PageHelper.startPage(page, size);
        List<PatientCustomGroup> list = patientCustomGroupMapper.search(
                StringUtils.hasText(keyword) ? keyword.trim() : null);
        return new PageInfo<>(list);
    }

    /**
     * 查询指定患者的影像列表
     *
     * @param id 患者ID
     * @return 患者影像列表（不分页）
     */
    public List<PatientImage> getPatientImages(Long id) {
        return patientImageMapper.selectByPatientId(id);
    }

    /**
     * 查询指定患者的时间轴事件列表
     *
     * @param id 患者ID
     * @return 患者时间轴事件列表（不分页）
     */
    public List<PatientTimeline> getPatientTimeline(Long id) {
        return patientTimelineMapper.selectByPatientId(id);
    }

    /**
     * 查询指定患者的洞察摘要
     *
     * @param id 患者ID
     * @return 患者洞察摘要
     */
    public PatientInsightSummary getPatientInsight(Long id) {
        return patientInsightSummaryService.getOrRefresh(id);
    }
}
