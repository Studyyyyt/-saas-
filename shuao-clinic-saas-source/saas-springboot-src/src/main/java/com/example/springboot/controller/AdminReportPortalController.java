package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.Account;
import com.example.springboot.service.AccountService;
import com.example.springboot.service.BusinessAlertService;
import com.example.springboot.service.BusinessDailyAnalysisService;
import com.example.springboot.service.BusinessPeriodReportService;
import com.example.springboot.service.WechatOAuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin-report-portal")
public class AdminReportPortalController {

    private final AccountService accountService;
    private final WechatOAuthService wechatOAuthService;
    private final BusinessDailyAnalysisService businessDailyAnalysisService;
    private final BusinessPeriodReportService businessPeriodReportService;
    private final BusinessAlertService businessAlertService;

    public AdminReportPortalController(AccountService accountService,
                                       WechatOAuthService wechatOAuthService,
                                       BusinessDailyAnalysisService businessDailyAnalysisService,
                                       BusinessPeriodReportService businessPeriodReportService,
                                       BusinessAlertService businessAlertService) {
        this.accountService = accountService;
        this.wechatOAuthService = wechatOAuthService;
        this.businessDailyAnalysisService = businessDailyAnalysisService;
        this.businessPeriodReportService = businessPeriodReportService;
        this.businessAlertService = businessAlertService;
    }

    @GetMapping("/overview")
    public Result overview(@RequestParam Long accountId,
                           @RequestParam String reportToken) {
        if (accountId == null || accountId <= 0) {
            return Result.error("管理员账号不存在");
        }
        Long authorizedAccountId = wechatOAuthService.resolveAdminReportToken(reportToken);
        if (authorizedAccountId == null || !authorizedAccountId.equals(accountId)) {
            return Result.error("报表访问身份校验失败，请重新从公众号消息进入");
        }
        Account account = accountService.selectById(accountId).stream().findFirst().orElse(null);
        if (account == null) {
            return Result.error("管理员账号不存在");
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("account", account);
        payload.put("summary", buildSummary(account));
        payload.put("latestDaily", businessDailyAnalysisService.getLatestAnalysis());
        payload.put("latestWeekly", businessPeriodReportService.getLatestWeeklyReport());
        payload.put("latestMonthly", businessPeriodReportService.getLatestMonthlyReport());
        payload.put("recentAlerts", businessAlertService.getRecentAlerts(10));
        return Result.success(payload);
    }

    private Map<String, Object> buildSummary(Account account) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("displayName", account.getName());
        summary.put("roleLabel", "管理员");
        summary.put("username", account.getUsername());
        summary.put("wechatBound", account.getWechat_openid() != null && !account.getWechat_openid().trim().isEmpty());
        return summary;
    }
}
