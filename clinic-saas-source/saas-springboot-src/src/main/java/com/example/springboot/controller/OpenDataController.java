package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.*;
import com.example.springboot.mapper.*;
import com.example.springboot.service.*;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * 开放数据接口控制器，供 n8n 等外部系统通过 API Key 调用
 */
@Slf4j
@RestController
@RequestMapping("/api/open/clinic/{clinicId}")
@Tag(name = "开放数据接口", description = "供 n8n 等外部系统通过 API Key 调用的诊所数据接口")
public class OpenDataController {

    private final BusinessDailyAnalysisService businessDailyAnalysisService;
    private final PatientFollowupMapper patientFollowupMapper;
    private final AppointmentService appointmentService;
    private final PatientWorkbenchService patientWorkbenchService;
    private final DoctorService doctorService;
    private final MaterialService materialService;
    private final FinanceService financeService;
    private final TreatmentService treatmentService;
    private final ConsultationRecordMapper consultationRecordMapper;
    private final ConsultationFollowupMapper consultationFollowupMapper;
    private final LabOrderMapper labOrderMapper;
    private final LabFactoryMapper labFactoryMapper;
    private final MaterialPurchaseMapper materialPurchaseMapper;
    private final AdvertisingSpendingMapper advertisingSpendingMapper;
    private final TreatmentCatalogMapper treatmentCatalogMapper;
    private final PaymentChannelMapper paymentChannelMapper;
    private final InventoryMapper inventoryMapper;
    private final MedicalRecordMapper medicalRecordMapper;
    private final OpenPatientService openPatientService;

    public OpenDataController(BusinessDailyAnalysisService businessDailyAnalysisService,
                              PatientFollowupMapper patientFollowupMapper,
                              AppointmentService appointmentService,
                              PatientWorkbenchService patientWorkbenchService,
                              DoctorService doctorService,
                              MaterialService materialService,
                              FinanceService financeService,
                              TreatmentService treatmentService,
                              ConsultationRecordMapper consultationRecordMapper,
                              ConsultationFollowupMapper consultationFollowupMapper,
                              LabOrderMapper labOrderMapper,
                              LabFactoryMapper labFactoryMapper,
                              MaterialPurchaseMapper materialPurchaseMapper,
                              AdvertisingSpendingMapper advertisingSpendingMapper,
                              TreatmentCatalogMapper treatmentCatalogMapper,
                              PaymentChannelMapper paymentChannelMapper,
                              InventoryMapper inventoryMapper,
                              MedicalRecordMapper medicalRecordMapper,
                              OpenPatientService openPatientService) {
        this.businessDailyAnalysisService = businessDailyAnalysisService;
        this.patientFollowupMapper = patientFollowupMapper;
        this.appointmentService = appointmentService;
        this.patientWorkbenchService = patientWorkbenchService;
        this.doctorService = doctorService;
        this.materialService = materialService;
        this.financeService = financeService;
        this.treatmentService = treatmentService;
        this.consultationRecordMapper = consultationRecordMapper;
        this.consultationFollowupMapper = consultationFollowupMapper;
        this.labOrderMapper = labOrderMapper;
        this.labFactoryMapper = labFactoryMapper;
        this.materialPurchaseMapper = materialPurchaseMapper;
        this.advertisingSpendingMapper = advertisingSpendingMapper;
        this.treatmentCatalogMapper = treatmentCatalogMapper;
        this.paymentChannelMapper = paymentChannelMapper;
        this.inventoryMapper = inventoryMapper;
        this.medicalRecordMapper = medicalRecordMapper;
        this.openPatientService = openPatientService;
    }

    /**
     * 校验 API Key 对应的 clinicId 是否与路径中的 clinicId 一致
     */
    private boolean validateClinicId(HttpServletRequest request, Long pathClinicId) {
        Object attr = request.getAttribute("apiKeyClinicId");
        if (attr == null) {
            return false;
        }
        Long apiKeyClinicId;
        if (attr instanceof Long) {
            apiKeyClinicId = (Long) attr;
        } else {
            try {
                apiKeyClinicId = Long.valueOf(attr.toString());
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return apiKeyClinicId.equals(pathClinicId);
    }

    /**
     * 获取经营统计（日报/周报/月报）
     *
     * @param clinicId 诊所ID（路径参数）
     * @param period   统计周期：day（默认）| week | month
     * @param request  HTTP请求（用于获取apiKeyClinicId）
     * @return 经营统计数据
     */
    @Operation(summary = "经营统计", description = "获取经营日报核心统计数据")
    @GetMapping("/business-stats")
    public Result getBusinessStats(
            @Parameter(description = "诊所ID", required = true) @PathVariable("clinicId") Long clinicId,
            HttpServletRequest request) {
        if (!validateClinicId(request, clinicId)) {
            return Result.error("403", "无权访问该诊所数据");
        }

        Map<String, Object> data = businessDailyAnalysisService.getLatestAnalysis();

        if (data == null) {
            return Result.success(null);
        }

        Map<String, Object> trimmed = new LinkedHashMap<>();
        trimmed.put("analysis_date", data.get("analysis_date"));
        trimmed.put("operating_score", data.get("operating_score"));
        trimmed.put("trend", data.get("trend"));
        trimmed.put("highlights", data.get("highlights"));
        trimmed.put("risks", data.get("risks"));
        trimmed.put("opportunities", data.get("opportunities"));
        trimmed.put("actions", data.get("actions"));
        trimmed.put("daily_metrics_summary", data.get("metrics"));
        return Result.success(trimmed);
    }

    /**
     * 获取最新经营日报（原始完整视图）
     */
    @Operation(summary = "最新经营日报", description = "获取最新经营日报完整数据")
    @GetMapping("/business-analysis/latest")
    public Result getBusinessAnalysisLatest(
            @Parameter(description = "诊所ID", required = true) @PathVariable("clinicId") Long clinicId,
            HttpServletRequest request) {
        if (!validateClinicId(request, clinicId)) {
            return Result.error("403", "无权访问该诊所数据");
        }
        return Result.success(businessDailyAnalysisService.getLatestAnalysis());
    }

    /**
     * 获取指定日期的原始经营日报指标数据（metrics）
     * 供 n8n 工作流调用，AI 分析前获取原始数据使用
     *
     * @param clinicId 诊所ID（路径参数）
     * @param date     分析日期（格式 yyyy-MM-dd，默认昨日）
     * @param request  HTTP请求
     * @return DailyBusinessMetrics 原始经营指标
     */
    @Operation(summary = "原始经营指标", description = "获取指定日期的原始经营日报指标数据")
    @GetMapping("/daily-metrics")
    public Result getDailyMetrics(
            @Parameter(description = "诊所ID", required = true) @PathVariable("clinicId") Long clinicId,
            @Parameter(description = "分析日期（yyyy-MM-dd，默认昨日") @RequestParam(name = "date", required = false) String date,
            HttpServletRequest request) {
        if (!validateClinicId(request, clinicId)) {
            return Result.error("403", "无权访问该诊所数据");
        }

        LocalDate targetDate = parseDate(date);
        if (targetDate == null) {
            targetDate = LocalDate.now(ZoneId.of("Asia/Shanghai")).minusDays(1);
        }

        Map<String, Object> metrics =
                businessDailyAnalysisService.buildSimplifiedDailyMetrics(targetDate);
        return Result.success(metrics);
    }

    /**
     * 获取回访列表（支持分页和日期过滤）
     *
     * @param clinicId     诊所ID（路径参数）
     * @param patientName  患者姓名（可选模糊匹配）
     * @param startDate    回访日期起始（可选，格式 yyyy-MM-dd）
     * @param endDate      回访日期截止（可选，格式 yyyy-MM-dd）
     * @param page         页码（默认1）
     * @param size         每页条数（默认10）
     * @param request      HTTP请求
     * @return 分页回访列表
     */
    @Operation(summary = "回访列表", description = "查询患者回访列表，支持姓名模糊匹配和日期范围筛选")
    @GetMapping("/followups")
    public Result getFollowups(
            @Parameter(description = "诊所ID", required = true) @PathVariable("clinicId") Long clinicId,
                               @RequestParam(name = "patientName", required = false) String patientName,
                               @RequestParam(name = "startDate", required = false) String startDate,
                               @RequestParam(name = "endDate", required = false) String endDate,
                               @RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "10") int size,
                               HttpServletRequest request) {
        if (!validateClinicId(request, clinicId)) {
            return Result.error("403", "无权访问该诊所数据");
        }

        LocalDate sDate = parseDate(startDate);
        LocalDate eDate = parseDate(endDate);
        Date sqlStartDate = sDate == null ? null : java.sql.Date.valueOf(sDate);
        Date sqlEndDate = eDate == null ? null : java.sql.Date.valueOf(eDate);

        PageHelper.startPage(page, size);
        List<PatientFollowup> list = patientFollowupMapper.search(
                StringUtils.hasText(patientName) ? patientName.trim() : null,
                sqlStartDate,
                sqlEndDate);
        return Result.success(new PageInfo<>(list));
    }

    /**
     * 获取患者列表（支持分页和姓名模糊匹配）
     *
     * @param clinicId 诊所ID（路径参数）
     * @param name     患者姓名（可选模糊匹配）
     * @param page     页码（默认1）
     * @param size     每页条数（默认10）
     * @param request  HTTP请求
     * @return 分页患者列表
     */
    @Operation(summary = "患者列表", description = "查询患者列表，支持姓名模糊匹配")
    @GetMapping("/patients")
    public Result getPatients(
            @Parameter(description = "诊所ID", required = true) @PathVariable("clinicId") Long clinicId,
            @Parameter(description = "患者姓名（模糊匹配）") @RequestParam(name = "name", required = false) String name,
            @Parameter(description = "页码（默认1）") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数（默认10）") @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        if (!validateClinicId(request, clinicId)) {
            return Result.error("403", "无权访问该诊所数据");
        }

        return Result.success(openPatientService.getPatients(name, page, size));
    }

    /**
     * 查询预约列表（支持日期范围）
     */
    @Operation(summary = "预约列表", description = "查询预约列表，支持日期范围、状态、医生筛选")
    @GetMapping("/appointments")
    public Result getAppointments(
            @Parameter(description = "诊所ID", required = true) @PathVariable("clinicId") Long clinicId,
            @Parameter(description = "页码（默认1）") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数（默认10）") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "预约状态") @RequestParam(required = false) String status,
            @Parameter(description = "指定日期（yyyy-MM-dd）") @RequestParam(required = false) String appointmentDate,
            @Parameter(description = "开始日期（yyyy-MM-dd）") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期（yyyy-MM-dd）") @RequestParam(required = false) String endDate,
            @Parameter(description = "医生账号ID") @RequestParam(required = false) Long doctorAccountId,
            HttpServletRequest request) {
        if (!validateClinicId(request, clinicId)) {
            return Result.error("403", "无权访问该诊所数据");
        }
        PageHelper.startPage(page, size);
        List<Appointment> appointments = appointmentService.searchAppointments(status, appointmentDate, startDate, endDate, doctorAccountId);
        PageInfo<Appointment> pageInfo = new PageInfo<>(appointments);
        return Result.success(pageInfo);
    }

    /**
     * 获取患者基础详情（就诊次数、总费用、欠款等）
     */
    @Operation(summary = "患者详情", description = "获取患者基础详情，包含就诊次数、总费用、欠款等聚合数据")
    @GetMapping("/patients/{patientId}/details")
    public Result getPatientDetails(
            @Parameter(description = "诊所ID", required = true) @PathVariable("clinicId") Long clinicId,
            @Parameter(description = "患者ID", required = true) @PathVariable("patientId") Long patientId,
            HttpServletRequest request) {
        if (!validateClinicId(request, clinicId)) {
            return Result.error("403", "无权访问该诊所数据");
        }
        Map<String, Object> result = openPatientService.getPatientDetails(patientId);
        if (result == null) {
            return Result.error("患者不存在");
        }
        return Result.success(result);
    }

    /**
     * 按患者查询病历列表
     */
    @Operation(summary = "患者病历列表", description = "按患者查询历史病历列表")
    @GetMapping("/patients/{patientId}/medical-records")
    public Result getPatientMedicalRecords(
            @Parameter(description = "诊所ID", required = true) @PathVariable("clinicId") Long clinicId,
            @Parameter(description = "患者ID", required = true) @PathVariable("patientId") Long patientId,
            @Parameter(description = "页码（默认1）") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数（默认10）") @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        if (!validateClinicId(request, clinicId)) {
            return Result.error("403", "无权访问该诊所数据");
        }
        return Result.success(openPatientService.getPatientMedicalRecords(patientId, page, size));
    }

    /**
     * 查询医生排班列表
     */
    @Operation(summary = "医生排班", description = "查询医生排班列表")
    @GetMapping("/doctors")
    public Result getDoctors(
            @Parameter(description = "诊所ID", required = true) @PathVariable("clinicId") Long clinicId,
            @Parameter(description = "页码（默认1）") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数（默认10）") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "排班状态") @RequestParam(required = false) String status,
            @Parameter(description = "排班日期（yyyy-MM-dd）") @RequestParam(required = false) String scheduleDate,
            HttpServletRequest request) {
        if (!validateClinicId(request, clinicId)) {
            return Result.error("403", "无权访问该诊所数据");
        }
        PageHelper.startPage(page, size);
        List<Doctor> doctorList = doctorService.searchDoctors(status, scheduleDate);
        PageInfo<Doctor> pageInfo = new PageInfo<>(doctorList);
        return Result.success(pageInfo);
    }

    /**
     * 查询耗材列表（支持低库存预警）
     */
    @Operation(summary = "耗材列表", description = "查询耗材列表，支持低库存预警筛选")
    @GetMapping("/materials")
    public Result getMaterials(
            @Parameter(description = "诊所ID", required = true) @PathVariable("clinicId") Long clinicId,
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "分类ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "仅低库存") @RequestParam(required = false) Boolean lowStockOnly,
            @Parameter(description = "状态") @RequestParam(required = false) String status,
            @Parameter(description = "页码（默认1）") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数（默认20）") @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        if (!validateClinicId(request, clinicId)) {
            return Result.error("403", "无权访问该诊所数据");
        }
        // MaterialService.search 已下沉为数据库过滤，PageHelper 自动拦截分页
        PageHelper.startPage(page, size);
        List<Material> list = materialService.search(keyword, categoryId, lowStockOnly, status);
        return Result.success(new PageInfo<>(list));
    }

    /**
     * 患者工作台查询
     */
    @GetMapping("/patients/workbench")
    public Result getPatientsWorkbench(@PathVariable("clinicId") Long clinicId,
                                       PatientWorkbenchQuery query,
                                       HttpServletRequest request) {
        if (!validateClinicId(request, clinicId)) {
            return Result.error("403", "无权访问该诊所数据");
        }
        if (query == null) {
            query = new PatientWorkbenchQuery();
        }
        if (query.getPage() == null || query.getPage() <= 0) {
            query.setPage(1);
        }
        if (query.getSize() == null || query.getSize() <= 0) {
            query.setSize(20);
        }
        return Result.success(patientWorkbenchService.search(query));
    }

    /**
     * 医生业绩统计
     */
    @Operation(summary = "医生业绩", description = "按医生统计营业额、实收、退费、欠款")
    @GetMapping("/finances/doctor-performance")
    public Result getDoctorPerformance(
            @Parameter(description = "诊所ID", required = true) @PathVariable("clinicId") Long clinicId,
            @Parameter(description = "开始日期（yyyy-MM-dd）") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期（yyyy-MM-dd）") @RequestParam(required = false) String endDate,
            @Parameter(description = "医生账号ID") @RequestParam(required = false) Long doctorAccountId,
            @Parameter(description = "医生姓名") @RequestParam(required = false) String doctorName,
            HttpServletRequest request) {
        if (!validateClinicId(request, clinicId)) {
            return Result.error("403", "无权访问该诊所数据");
        }
        try {
            return Result.success(financeService.getDoctorPerformance(startDate, endDate, doctorAccountId, doctorName));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    /**
     * 查询财务收支记录（支持多条件组合查询）
     *
     * @param clinicId  诊所ID（路径参数）
     * @param startDate 开始日期（yyyy-MM-dd）
     * @param endDate   结束日期（yyyy-MM-dd）
     * @param type      收支类型：收入/支出
     * @param patientId 患者ID
     * @param keyword   关键词（匹配名称或备注）
     * @param page      页码（默认1）
     * @param size      每页条数（默认10）
     * @param request   HTTP请求
     * @return 分页财务记录
     */
    @Operation(summary = "财务收支", description = "查询财务收支记录，支持日期范围、收支类型、患者筛选")
    @GetMapping("/finances")
    public Result getFinances(
            @Parameter(description = "诊所ID", required = true) @PathVariable("clinicId") Long clinicId,
                              @RequestParam(required = false) String startDate,
                              @RequestParam(required = false) String endDate,
                              @RequestParam(required = false) String type,
                              @RequestParam(required = false) Long patientId,
                              @RequestParam(required = false) String keyword,
                              @RequestParam(defaultValue = "1") int page,
                              @RequestParam(defaultValue = "10") int size,
                              HttpServletRequest request) {
        if (!validateClinicId(request, clinicId)) {
            return Result.error("403", "无权访问该诊所数据");
        }
        PageHelper.startPage(page, size);
        List<Finance> list = financeService.searchFinances(type, startDate, endDate, patientId, keyword);
        return Result.success(new PageInfo<>(list));
    }

    /**
     * 查询咨询记录列表（支持多条件组合查询）
     *
     * @param clinicId       诊所ID（路径参数）
     * @param startDate      咨询日期起始（yyyy-MM-dd）
     * @param endDate        咨询日期截止（yyyy-MM-dd）
     * @param channel        咨询渠道
     * @param intentLevel    意向等级
     * @param handlingResult 跟进结果
     * @param keyword        关键词
     * @param page           页码（默认1）
     * @param size           每页条数（默认10）
     * @param request        HTTP请求
     * @return 分页咨询记录
     */
    @Operation(summary = "咨询记录", description = "查询咨询记录列表，支持日期范围、渠道、意向等级、处理结果筛选")
    @GetMapping("/consultations")
    public Result getConsultations(
            @Parameter(description = "诊所ID", required = true) @PathVariable("clinicId") Long clinicId,
                                   @RequestParam(required = false) String startDate,
                                   @RequestParam(required = false) String endDate,
                                   @RequestParam(required = false) String channel,
                                   @RequestParam(required = false) String intentLevel,
                                   @RequestParam(required = false) String handlingResult,
                                   @RequestParam(required = false) String keyword,
                                   @RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "10") int size,
                                   HttpServletRequest request) {
        if (!validateClinicId(request, clinicId)) {
            return Result.error("403", "无权访问该诊所数据");
        }

        ConsultationQuery query = new ConsultationQuery();
        query.setKeyword(keyword);
        query.setChannel(channel);
        query.setIntentLevel(intentLevel);
        query.setHandlingResult(handlingResult);
        // Mapper 中使用 startTime/endTime（datetime），需拼接时分秒
        if (StringUtils.hasText(startDate)) {
            query.setStartTime(startDate.trim() + " 00:00:00");
        }
        if (StringUtils.hasText(endDate)) {
            query.setEndTime(endDate.trim() + " 23:59:59");
        }

        PageHelper.startPage(page, size);
        List<ConsultationRecord> list = consultationRecordMapper.search(query);
        return Result.success(new PageInfo<>(list));
    }

    /**
     * 查询指定咨询的跟进记录列表
     *
     * @param clinicId      诊所ID（路径参数）
     * @param id            咨询ID
     * @param request       HTTP请求
     * @return 跟进记录列表（不分页）
     */
    @GetMapping("/consultations/{id}/followups")
    public Result getConsultationFollowups(@PathVariable("clinicId") Long clinicId,
                                           @PathVariable("id") Long id,
                                           HttpServletRequest request) {
        if (!validateClinicId(request, clinicId)) {
            return Result.error("403", "无权访问该诊所数据");
        }
        List<ConsultationFollowup> list = consultationFollowupMapper.selectByConsultationId(id);
        return Result.success(list);
    }

    /**
     * 查询义齿加工订单列表（支持多条件组合查询）
     *
     * @param clinicId  诊所ID（路径参数）
     * @param startDate 下单日期起始（yyyy-MM-dd）
     * @param endDate   下单日期截止（yyyy-MM-dd）
     * @param status    订单状态
     * @param factoryId 加工厂ID
     * @param keyword   关键词（患者姓名、订单编号/ID、加工项目、产品名称）
     * @param page      页码（默认1）
     * @param size      每页条数（默认10）
     * @param request   HTTP请求
     * @return 分页义齿加工订单
     */
    @Operation(summary = "义齿加工订单", description = "查询义齿加工订单列表，支持日期范围、状态、加工厂筛选")
    @GetMapping("/lab-orders")
    public Result getLabOrders(
            @Parameter(description = "诊所ID", required = true) @PathVariable("clinicId") Long clinicId,
                               @RequestParam(required = false) String startDate,
                               @RequestParam(required = false) String endDate,
                               @RequestParam(required = false) String status,
                               @RequestParam(required = false) Long factoryId,
                               @RequestParam(required = false) String keyword,
                               @RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "10") int size,
                               HttpServletRequest request) {
        if (!validateClinicId(request, clinicId)) {
            return Result.error("403", "无权访问该诊所数据");
        }
        PageHelper.startPage(page, size);
        List<LabOrder> list = labOrderMapper.search(startDate, endDate, status, factoryId, keyword);
        return Result.success(new PageInfo<>(list));
    }

    /**
     * 查询义齿加工厂列表
     *
     * @param clinicId 诊所ID（路径参数）
     * @param request  HTTP请求
     * @return 加工厂列表（不分页）
     */
    @Operation(summary = "义齿加工厂", description = "查询义齿加工厂列表")
    @GetMapping("/lab-factories")
    public Result getLabFactories(
            @Parameter(description = "诊所ID", required = true) @PathVariable("clinicId") Long clinicId,
            HttpServletRequest request) {
        if (!validateClinicId(request, clinicId)) {
            return Result.error("403", "无权访问该诊所数据");
        }
        List<LabFactory> list = labFactoryMapper.selectAll();
        return Result.success(list);
    }

    /**
     * 查询治疗记录列表
     */
    @Operation(summary = "治疗记录", description = "查询治疗记录列表，支持患者、医生、日期范围筛选")
    @GetMapping("/treatments")
    public Result getTreatments(
            @Parameter(description = "诊所ID", required = true) @PathVariable("clinicId") Long clinicId,
            @Parameter(description = "页码（默认1）") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数（默认10）") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "患者ID") @RequestParam(required = false) Long patientId,
            @Parameter(description = "医生账号ID") @RequestParam(required = false) Long doctorAccountId,
            @Parameter(description = "开始日期（yyyy-MM-dd）") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期（yyyy-MM-dd）") @RequestParam(required = false) String endDate,
            @Parameter(description = "状态") @RequestParam(required = false) String status,
            HttpServletRequest request) {
        if (!validateClinicId(request, clinicId)) {
            return Result.error("403", "无权访问该诊所数据");
        }
        PageHelper.startPage(page, size);
        List<Treatment> treatmentList = treatmentService.searchTreatments(patientId, doctorAccountId, startDate, endDate, status);
        PageInfo<Treatment> pageInfo = new PageInfo<>(treatmentList);
        return Result.success(pageInfo);
    }

    /**
     * 查询耗材采购记录列表（支持多条件组合查询）
     *
     * @param clinicId     诊所ID（路径参数）
     * @param startDate    采购日期起始（yyyy-MM-dd）
     * @param endDate      采购日期截止（yyyy-MM-dd）
     * @param supplierName 供应商名称（可选模糊匹配）
     * @param status       采购状态
     * @param keyword      关键词（匹配供应商名称或备注）
     * @param page         页码（默认1）
     * @param size         每页条数（默认10）
     * @param request      HTTP请求
     * @return 分页耗材采购记录
     */
    @Operation(summary = "耗材采购记录", description = "查询耗材采购记录列表，支持日期范围、供应商筛选")
    @GetMapping("/material-purchases")
    public Result getMaterialPurchases(
            @Parameter(description = "诊所ID", required = true) @PathVariable("clinicId") Long clinicId,
                                       @RequestParam(required = false) String startDate,
                                       @RequestParam(required = false) String endDate,
                                       @RequestParam(required = false) String supplierName,
                                       @RequestParam(required = false) String status,
                                       @RequestParam(required = false) String keyword,
                                       @RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "10") int size,
                                       HttpServletRequest request) {
        if (!validateClinicId(request, clinicId)) {
            return Result.error("403", "无权访问该诊所数据");
        }
        PageHelper.startPage(page, size);
        List<MaterialPurchase> list = materialPurchaseMapper.search(startDate, endDate, supplierName, status, keyword);
        return Result.success(new PageInfo<>(list));
    }

    /**
     * 查询治疗项目目录列表
     *
     * @param clinicId    诊所ID（路径参数）
     * @param enabledOnly 是否只返回启用状态（默认false返回全部）
     * @param request     HTTP请求
     * @return 治疗项目目录列表
     */
    @Operation(summary = "治疗项目目录", description = "查询治疗项目目录列表，可选择仅返回启用状态的项目")
    @GetMapping("/treatment-catalog")
    public Result getTreatmentCatalog(
            @Parameter(description = "诊所ID", required = true) @PathVariable("clinicId") Long clinicId,
            @Parameter(description = "是否只返回启用状态") @RequestParam(required = false, defaultValue = "false") boolean enabledOnly,
            HttpServletRequest request) {
        if (!validateClinicId(request, clinicId)) {
            return Result.error("403", "无权访问该诊所数据");
        }
        List<TreatmentCatalog> list = enabledOnly ? treatmentCatalogMapper.selectEnabled() : treatmentCatalogMapper.selectAll();
        return Result.success(list);
    }

    /**
     * 查询广告投放花费记录列表（支持多条件组合查询）
     *
     * @param clinicId  诊所ID（路径参数）
     * @param startDate 投放开始日期起始（yyyy-MM-dd）
     * @param endDate   投放结束日期截止（yyyy-MM-dd）
     * @param platform  投放平台
     * @param keyword   关键词（匹配平台、活动名称或备注）
     * @param page      页码（默认1）
     * @param size      每页条数（默认10）
     * @param request   HTTP请求
     * @return 分页广告投放花费记录
     */
    @Operation(summary = "广告投放花费", description = "查询广告投放花费记录列表，支持日期范围、平台筛选")
    @GetMapping("/advertising-spending")
    public Result getAdvertisingSpending(
            @Parameter(description = "诊所ID", required = true) @PathVariable("clinicId") Long clinicId,
                                         @RequestParam(required = false) String startDate,
                                         @RequestParam(required = false) String endDate,
                                         @RequestParam(required = false) String platform,
                                         @RequestParam(required = false) String keyword,
                                         @RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "10") int size,
                                         HttpServletRequest request) {
        if (!validateClinicId(request, clinicId)) {
            return Result.error("403", "无权访问该诊所数据");
        }
        PageHelper.startPage(page, size);
        List<AdvertisingSpending> list = advertisingSpendingMapper.search(startDate, endDate, platform, keyword);
        return Result.success(new PageInfo<>(list));
    }

    /**
     * 查询指定患者的风险标签列表
     *
     * @param clinicId 诊所ID（路径参数）
     * @param id       患者ID
     * @param request  HTTP请求
     * @return 风险标签列表（不分页）
     */
    @Operation(summary = "患者风险标签", description = "查询指定患者的风险标签列表")
    @GetMapping("/patients/{id}/risk-tags")
    public Result getPatientRiskTags(
            @Parameter(description = "诊所ID", required = true) @PathVariable("clinicId") Long clinicId,
            @Parameter(description = "患者ID", required = true) @PathVariable("id") Long id,
            HttpServletRequest request) {
        if (!validateClinicId(request, clinicId)) {
            return Result.error("403", "无权访问该诊所数据");
        }
        return Result.success(openPatientService.getPatientRiskTags(id));
    }

    /**
     * 查询患者自定义分组列表（支持分页和关键词模糊匹配）
     *
     * @param clinicId 诊所ID（路径参数）
     * @param keyword  关键词（匹配分组名称或分组标识）
     * @param page     页码（默认1）
     * @param size     每页条数（默认10）
     * @param request  HTTP请求
     * @return 分页患者自定义分组列表
     */
    @GetMapping("/patient-groups")
    public Result getPatientGroups(@PathVariable("clinicId") Long clinicId,
                                   @RequestParam(required = false) String keyword,
                                   @RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "10") int size,
                                   HttpServletRequest request) {
        if (!validateClinicId(request, clinicId)) {
            return Result.error("403", "无权访问该诊所数据");
        }
        return Result.success(openPatientService.getPatientGroups(keyword, page, size));
    }

    /**
     * 查询指定患者的影像列表
     *
     * @param clinicId 诊所ID（路径参数）
     * @param id       患者ID
     * @param request  HTTP请求
     * @return 患者影像列表（不分页）
     */
    @GetMapping("/patients/{id}/images")
    public Result getPatientImages(@PathVariable("clinicId") Long clinicId,
                                   @PathVariable("id") Long id,
                                   HttpServletRequest request) {
        if (!validateClinicId(request, clinicId)) {
            return Result.error("403", "无权访问该诊所数据");
        }
        return Result.success(openPatientService.getPatientImages(id));
    }

    /**
     * 查询指定患者的时间轴事件列表
     *
     * @param clinicId 诊所ID（路径参数）
     * @param id       患者ID
     * @param request  HTTP请求
     * @return 患者时间轴事件列表（不分页）
     */
    @Operation(summary = "患者时间轴", description = "查询指定患者的就诊时间轴事件列表")
    @GetMapping("/patients/{id}/timeline")
    public Result getPatientTimeline(
            @Parameter(description = "诊所ID", required = true) @PathVariable("clinicId") Long clinicId,
            @Parameter(description = "患者ID", required = true) @PathVariable("id") Long id,
            HttpServletRequest request) {
        if (!validateClinicId(request, clinicId)) {
            return Result.error("403", "无权访问该诊所数据");
        }
        return Result.success(openPatientService.getPatientTimeline(id));
    }

    /**
     * 查询指定患者的洞察摘要
     *
     * @param clinicId 诊所ID（路径参数）
     * @param id       患者ID
     * @param request  HTTP请求
     * @return 患者洞察摘要
     */
    @Operation(summary = "患者洞察摘要", description = "查询指定患者的智能洞察摘要，包含就诊统计、高价值标记、流失风险等")
    @GetMapping("/patients/{id}/insight")
    public Result getPatientInsight(
            @Parameter(description = "诊所ID", required = true) @PathVariable("clinicId") Long clinicId,
            @Parameter(description = "患者ID", required = true) @PathVariable("id") Long id,
            HttpServletRequest request) {
        if (!validateClinicId(request, clinicId)) {
            return Result.error("403", "无权访问该诊所数据");
        }
        return Result.success(openPatientService.getPatientInsight(id));
    }

    /**
     * 查询病历列表（支持多条件组合查询）
     *
     * @param clinicId        诊所ID（路径参数）
     * @param startDate       就诊日期起始（yyyy-MM-dd）
     * @param endDate         就诊日期截止（yyyy-MM-dd）
     * @param doctorAccountId 医生账号ID
     * @param keyword         关键词（匹配患者姓名等）
     * @param page            页码（默认1）
     * @param size            每页条数（默认10）
     * @param request         HTTP请求
     * @return 分页病历列表
     */
    @Operation(summary = "病历列表", description = "查询病历列表，支持日期范围、医生、患者姓名筛选")
    @GetMapping("/medical-records")
    public Result getMedicalRecords(
            @Parameter(description = "诊所ID", required = true) @PathVariable("clinicId") Long clinicId,
                                    @RequestParam(required = false) String startDate,
                                    @RequestParam(required = false) String endDate,
                                    @RequestParam(required = false) Long doctorAccountId,
                                    @RequestParam(required = false) String keyword,
                                    @RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "10") int size,
                                    HttpServletRequest request) {
        if (!validateClinicId(request, clinicId)) {
            return Result.error("403", "无权访问该诊所数据");
        }
        PageHelper.startPage(page, size);
        List<MedicalRecord> list = medicalRecordMapper.selectAllWithFilter(doctorAccountId, null, startDate, endDate,
                StringUtils.hasText(keyword) ? keyword.trim() : null);
        return Result.success(new PageInfo<>(list));
    }

    /**
     * 查询库存物品列表（支持多条件组合查询）
     *
     * @param clinicId 诊所ID（路径参数）
     * @param category 物品分类
     * @param brand    品牌
     * @param supplier 供应商
     * @param keyword  关键词（匹配物品名称、分类、品牌）
     * @param page     页码（默认1）
     * @param size     每页条数（默认10）
     * @param request  HTTP请求
     * @return 分页库存物品列表
     */
    @Operation(summary = "库存物品", description = "查询库存物品列表，支持分类、品牌、供应商筛选")
    @GetMapping("/inventory")
    public Result getInventory(
            @Parameter(description = "诊所ID", required = true) @PathVariable("clinicId") Long clinicId,
            @Parameter(description = "物品分类") @RequestParam(required = false) String category,
            @Parameter(description = "品牌") @RequestParam(required = false) String brand,
            @Parameter(description = "供应商") @RequestParam(required = false) String supplier,
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "页码（默认1）") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数（默认10）") @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        if (!validateClinicId(request, clinicId)) {
            return Result.error("403", "无权访问该诊所数据");
        }
        PageHelper.startPage(page, size);
        List<Inventory> list = inventoryMapper.searchInventory(category, brand, supplier, keyword);
        return Result.success(new PageInfo<>(list));
    }

    /**
     * 查询支付渠道列表
     *
     * @param clinicId 诊所ID（路径参数）
     * @param request  HTTP请求
     * @return 支付渠道列表（不分页）
     */
    @Operation(summary = "支付渠道", description = "查询支付渠道列表")
    @GetMapping("/payment-channels")
    public Result getPaymentChannels(
            @Parameter(description = "诊所ID", required = true) @PathVariable("clinicId") Long clinicId,
            HttpServletRequest request) {
        if (!validateClinicId(request, clinicId)) {
            return Result.error("403", "无权访问该诊所数据");
        }
        List<PaymentChannel> list = paymentChannelMapper.selectAll();
        return Result.success(list);
    }

    /**
     * 解析日期字符串为 LocalDate
     *
     * @param dateStr 日期字符串（yyyy-MM-dd）
     * @return LocalDate 或 null
     */
    private LocalDate parseDate(String dateStr) {
        if (!StringUtils.hasText(dateStr)) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr.trim());
        } catch (DateTimeParseException e) {
            log.warn("日期解析失败: {}", dateStr);
            return null;
        }
    }
}
