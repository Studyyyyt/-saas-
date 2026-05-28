package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.ConsultationCreateResponse;
import com.example.springboot.entity.ConsultationQuery;
import com.example.springboot.entity.ConsultationRecord;
import com.example.springboot.service.ConsultationDashboardService;
import com.example.springboot.service.ConsultationRecordService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/consultations")
public class ConsultationRecordController {

    @Autowired
    private ConsultationRecordService consultationRecordService;

    @Autowired
    private ConsultationDashboardService consultationDashboardService;

    // 权限：护士/医生/老板管理员
    @GetMapping("/search")
    public Result search(@RequestParam(defaultValue = "1") Integer page,
                         @RequestParam(defaultValue = "20") Integer size,
                         @RequestParam(required = false) String keyword,
                         @RequestParam(required = false) String startTime,
                         @RequestParam(required = false) String endTime,
                         @RequestParam(required = false) String rangePreset,
                         @RequestParam(required = false) String channel,
                         @RequestParam(required = false) String chiefProject,
                         @RequestParam(required = false) String intentLevel,
                         @RequestParam(required = false) String handlingResult,
                         @RequestParam(required = false) Boolean hasDeal,
                         @RequestParam(required = false) Long createdBy) {
        PageHelper.startPage(Math.max(page, 1), Math.max(size, 1));
        ConsultationQuery query = new ConsultationQuery();
        query.setKeyword(keyword);
        query.setStartTime(startTime);
        query.setEndTime(endTime);
        query.setRangePreset(rangePreset);
        query.setChannel(channel);
        query.setChiefProject(chiefProject);
        query.setIntentLevel(intentLevel);
        query.setHandlingResult(handlingResult);
        query.setHasDeal(hasDeal);
        query.setCreatedBy(createdBy);
        List<ConsultationRecord> list = consultationRecordService.search(query);
        return Result.success(new PageInfo<>(list));
    }

    // 权限：护士/医生/老板管理员
    @GetMapping("/selectById")
    public Result selectById(@RequestParam Long id) {
        ConsultationRecord record = consultationRecordService.selectById(id);
        return record == null ? Result.error("咨询记录不存在") : Result.success(record);
    }

    // 权限：护士/医生/老板管理员
    @GetMapping("/selectByPatientId")
    public Result selectByPatientId(@RequestParam Long patientId) {
        return Result.success(consultationRecordService.selectByPatientId(patientId));
    }

    // 权限：护士/老板管理员
    @PostMapping("/add")
    public Result add(@RequestBody ConsultationRecord record) {
        try {
            normalizeConsultationTime(record);
            ConsultationCreateResponse response = consultationRecordService.add(record);
            return Result.success(response);
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        } catch (Exception exception) {
            return Result.error("保存失败：" + exception.getMessage());
        }
    }

    // 权限：录入人本人/老板管理员
    @PutMapping("/edit")
    public Result edit(@RequestBody ConsultationRecord record) {
        try {
            normalizeConsultationTime(record);
            return Result.success(consultationRecordService.update(record));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        } catch (Exception exception) {
            return Result.error("更新失败：" + exception.getMessage());
        }
    }

    // 权限：护士/医生/老板管理员
    @GetMapping("/matchPatientByPhone")
    public Result matchPatientByPhone(@RequestParam(required = false) String phone) {
        return Result.success(consultationRecordService.matchPatientByPhone(phone));
    }

    // 权限：护士/老板管理员
    @GetMapping("/matchForPatientCreate")
    public Result matchForPatientCreate(@RequestParam(required = false) String phone,
                                        @RequestParam(required = false) String name,
                                        @RequestParam(required = false) String startTime,
                                        @RequestParam(required = false) String endTime,
                                        @RequestParam(defaultValue = "1") Integer page,
                                        @RequestParam(defaultValue = "20") Integer size) {
        PageHelper.startPage(Math.max(page, 1), Math.max(size, 1));
        ConsultationQuery query = new ConsultationQuery();
        query.setPhone(phone);
        query.setName(name);
        query.setStartTime(startTime);
        query.setEndTime(endTime);
        List<ConsultationRecord> list = consultationRecordService.searchForPatientCreate(query);
        return Result.success(new PageInfo<>(list));
    }

    // 权限：护士/老板管理员
    @PostMapping("/linkPatient")
    public Result linkPatient(@RequestBody Map<String, Object> payload) {
        try {
            Long consultationId = parseLong(payload.get("consultationId"));
            Long patientId = parseLong(payload.get("patientId"));
            Long updatedBy = parseLong(payload.get("updatedBy"));
            return Result.success(consultationRecordService.linkPatient(consultationId, patientId, updatedBy));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    // 权限：护士/医生/老板管理员
    @GetMapping("/{id}/followups")
    public Result followups(@PathVariable Long id) {
        try {
            return Result.success(consultationRecordService.selectFollowups(id));
        } catch (Exception exception) {
            return Result.error("查询跟进历史失败：" + exception.getMessage());
        }
    }

    // 权限：护士/医生/老板管理员
    @PostMapping("/aiAnalyze")
    public Result aiAnalyze(@RequestBody Map<String, Object> payload) {
        try {
            Long consultationId = parseLong(payload.get("consultationId"));
            String chiefProject = payload.get("chiefProject") == null ? "" : String.valueOf(payload.get("chiefProject"));
            String intentLevel = payload.get("intentLevel") == null ? "" : String.valueOf(payload.get("intentLevel"));
            String remarks = payload.get("remarks") == null ? "" : String.valueOf(payload.get("remarks"));
            String customerConcerns = payload.get("customerConcerns") == null ? "" : String.valueOf(payload.get("customerConcerns"));

            // 当前返回模拟数据，后续接入真实 AI 服务
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("consultationId", consultationId);
            result.put("intentScore", 78);
            result.put("summary", "客户对" + chiefProject + "有明确需求，" + (remarks.isEmpty() ? "" : "沟通内容显示其关注度高。"));
            result.put("suggestedNextFollowup", "建议明天电话回访，强调本院分期免息政策。");
            result.put("riskPoints", customerConcerns.isEmpty() ? Arrays.asList("价格敏感") : Arrays.asList(customerConcerns.split("，|,|;|；")));
            result.put("suggestedActions", Arrays.asList("发送分期方案资料", "预约院长面诊"));

            return Result.success(result);
        } catch (Exception exception) {
            return Result.error("AI分析失败：" + exception.getMessage());
        }
    }

    // 权限：仅老板/管理员
    @GetMapping("/dashboard/overview")
    public Result overview(@RequestParam(required = false) String startTime,
                           @RequestParam(required = false) String endTime,
                           @RequestParam(required = false) String rangePreset) {
        try {
            return Result.success(consultationDashboardService.buildOverview(startTime, endTime, rangePreset));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    // 权限：仅老板/管理员
    @GetMapping("/dashboard/funnel")
    public Result funnel(@RequestParam(required = false) String startTime,
                         @RequestParam(required = false) String endTime,
                         @RequestParam(required = false) String rangePreset) {
        try {
            return Result.success(consultationDashboardService.buildFunnel(startTime, endTime, rangePreset));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    // 权限：仅老板/管理员
    @GetMapping("/dashboard/channelAnalysis")
    public Result channelAnalysis(@RequestParam(required = false) String startTime,
                                  @RequestParam(required = false) String endTime,
                                  @RequestParam(required = false) String rangePreset) {
        try {
            return Result.success(consultationDashboardService.buildChannelAnalysis(startTime, endTime, rangePreset));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    // 权限：仅老板/管理员
    @GetMapping("/dashboard/projectAnalysis")
    public Result projectAnalysis(@RequestParam(required = false) String startTime,
                                  @RequestParam(required = false) String endTime,
                                  @RequestParam(required = false) String rangePreset) {
        try {
            return Result.success(consultationDashboardService.buildProjectAnalysis(startTime, endTime, rangePreset));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    // 权限：仅老板/管理员
    @GetMapping("/dashboard/hourHeatmap")
    public Result hourHeatmap(@RequestParam(required = false) String startTime,
                              @RequestParam(required = false) String endTime,
                              @RequestParam(required = false) String rangePreset) {
        try {
            return Result.success(consultationDashboardService.buildHourHeatmap(startTime, endTime, rangePreset));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    // 权限：仅老板/管理员
    @GetMapping("/dashboard/nursePerformance")
    public Result nursePerformance(@RequestParam(required = false) String startTime,
                                   @RequestParam(required = false) String endTime,
                                   @RequestParam(required = false) String rangePreset) {
        try {
            return Result.success(consultationDashboardService.buildNursePerformance(startTime, endTime, rangePreset));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    // 权限：仅老板/管理员
    @GetMapping("/dashboard/referralAnalysis")
    public Result referralAnalysis(@RequestParam(required = false) String startTime,
                                   @RequestParam(required = false) String endTime,
                                   @RequestParam(required = false) String rangePreset) {
        try {
            return Result.success(consultationDashboardService.buildReferralAnalysis(startTime, endTime, rangePreset));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    private void normalizeConsultationTime(ConsultationRecord record) {
        if (record == null || record.getConsultation_time() != null) {
            return;
        }
        // Jackson 反序列化失败时 consultation_time 可能为 null，此处做兜底
        // 实际前端传的是正确格式，若仍有问题则走 Service 层校验
    }

    private Long parseLong(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(String.valueOf(value).trim());
        } catch (Exception ignored) {
            return null;
        }
    }
}
