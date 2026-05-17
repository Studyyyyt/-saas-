package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.Patient;
import com.example.springboot.entity.PatientFollowup;
import com.example.springboot.mapper.PatientFollowupMapper;
import com.example.springboot.service.BusinessDailyAnalysisService;
import com.example.springboot.service.BusinessPeriodReportService;
import com.example.springboot.service.PatientService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 开放数据接口控制器，供 n8n 等外部系统通过 API Key 调用
 */
@RestController
@RequestMapping("/api/open/clinic/{clinicId}")
public class OpenDataController {

    private final BusinessDailyAnalysisService businessDailyAnalysisService;
    private final BusinessPeriodReportService businessPeriodReportService;
    private final PatientService patientService;
    private final PatientFollowupMapper patientFollowupMapper;

    public OpenDataController(BusinessDailyAnalysisService businessDailyAnalysisService,
                              BusinessPeriodReportService businessPeriodReportService,
                              PatientService patientService,
                              PatientFollowupMapper patientFollowupMapper) {
        this.businessDailyAnalysisService = businessDailyAnalysisService;
        this.businessPeriodReportService = businessPeriodReportService;
        this.patientService = patientService;
        this.patientFollowupMapper = patientFollowupMapper;
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
    @GetMapping("/business-stats")
    public Result getBusinessStats(@PathVariable("clinicId") Long clinicId,
                                   @RequestParam(name = "period", defaultValue = "day") String period,
                                   HttpServletRequest request) {
        if (!validateClinicId(request, clinicId)) {
            return Result.error("403", "无权访问该诊所数据");
        }

        String p = period == null ? "day" : period.trim().toLowerCase();

        Map<String, Object> data;
        switch (p) {
            case "week" -> {
                // 获取最近一周的周报
                data = businessPeriodReportService.getLatestWeeklyReport();
            }
            case "month" -> {
                // 获取最近一月的月报
                data = businessPeriodReportService.getLatestMonthlyReport();
            }
            default -> {
                // 获取最近一日的日报
                data = businessDailyAnalysisService.getLatestAnalysis();
            }
        }

        return Result.success(data);
    }

    /**
     * 获取回访列表
     *
     * @param clinicId     诊所ID（路径参数）
     * @param patientName  患者姓名（可选模糊匹配）
     * @param startDate    回访日期起始（可选，格式 yyyy-MM-dd）
     * @param endDate      回访日期截止（可选，格式 yyyy-MM-dd）
     * @param request      HTTP请求
     * @return 回访列表
     */
    @GetMapping("/followups")
    public Result getFollowups(@PathVariable("clinicId") Long clinicId,
                               @RequestParam(name = "patientName", required = false) String patientName,
                               @RequestParam(name = "startDate", required = false) String startDate,
                               @RequestParam(name = "endDate", required = false) String endDate,
                               HttpServletRequest request) {
        if (!validateClinicId(request, clinicId)) {
            return Result.error("403", "无权访问该诊所数据");
        }

        // 获取全部回访详情（含患者姓名、电话）
        List<PatientFollowup> allFollowups = patientFollowupMapper.selectAllDetail();
        List<PatientFollowup> result = new ArrayList<>();

        LocalDate sDate = parseDate(startDate);
        LocalDate eDate = parseDate(endDate);

        for (PatientFollowup followup : allFollowups) {
            // 按患者姓名过滤
            if (StringUtils.hasText(patientName)) {
                String name = followup.getPatient_name();
                if (name == null || !name.contains(patientName.trim())) {
                    continue;
                }
            }

            // 按回访日期范围过滤
            if (followup.getFollowup_date() != null) {
                LocalDate fDate = followup.getFollowup_date().toInstant()
                        .atZone(ZoneId.of("Asia/Shanghai"))
                        .toLocalDate();
                if (sDate != null && fDate.isBefore(sDate)) {
                    continue;
                }
                if (eDate != null && fDate.isAfter(eDate)) {
                    continue;
                }
            } else {
                // 如果要求按日期过滤但记录无日期，则跳过
                if (sDate != null || eDate != null) {
                    continue;
                }
            }

            result.add(followup);
        }

        return Result.success(result);
    }

    /**
     * 获取患者列表
     *
     * @param clinicId 诊所ID（路径参数）
     * @param name     患者姓名（可选模糊匹配）
     * @param request  HTTP请求
     * @return 患者列表
     */
    @GetMapping("/patients")
    public Result getPatients(@PathVariable("clinicId") Long clinicId,
                              @RequestParam(name = "name", required = false) String name,
                              HttpServletRequest request) {
        if (!validateClinicId(request, clinicId)) {
            return Result.error("403", "无权访问该诊所数据");
        }

        List<Patient> patients;
        if (StringUtils.hasText(name)) {
            // 通过 PatientService 的搜索方法进行模糊匹配
            patients = patientService.searchPatients(name.trim());
        } else {
            patients = patientService.selectAll();
        }

        return Result.success(patients);
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
            return null;
        }
    }
}
