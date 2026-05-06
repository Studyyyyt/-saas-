package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.Account;
import com.example.springboot.service.AccountService;
import com.example.springboot.service.WechatOAuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/staff-portal")
public class StaffPortalController {

    private final AccountService accountService;
    private final WechatOAuthService wechatOAuthService;

    public StaffPortalController(AccountService accountService,
                                 WechatOAuthService wechatOAuthService) {
        this.accountService = accountService;
        this.wechatOAuthService = wechatOAuthService;
    }

    @GetMapping("/entry")
    public RedirectView entry() {
        return new RedirectView(wechatOAuthService.buildStaffAuthorizeUrl());
    }

    @GetMapping("/callback")
    public RedirectView callback(@RequestParam String code, @RequestParam String state) {
        if (!wechatOAuthService.isStaffPortalState(state)) {
            return new RedirectView("/staff-portal-auth-error");
        }
        String openid = wechatOAuthService.exchangeCodeForOpenid(code);
        Account account = accountService.selectByWechatOpenid(openid);
        if (account == null) {
            String bindToken = wechatOAuthService.issueStaffBindToken(openid);
            return new RedirectView("/staff-portal-bind?token=" + bindToken);
        }
        return new RedirectView(wechatOAuthService.buildStaffHomeUrl((long) account.getId()));
    }

    @PostMapping("/bind")
    public Result bind(@RequestParam String token,
                       @RequestBody Map<String, String> payload) {
        String openid = wechatOAuthService.consumeStaffBindToken(token);
        if (openid == null || openid.trim().isEmpty()) {
            return Result.error("绑定链接已失效，请重新从公众号菜单进入");
        }
        Account existed = accountService.selectByWechatOpenid(openid);
        if (existed != null) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("bound", true);
            result.put("accountId", existed.getId());
            result.put("redirectUrl", wechatOAuthService.buildStaffHomeUrl((long) existed.getId()));
            return Result.success(result);
        }

        String username = payload == null ? null : payload.get("username");
        String password = payload == null ? null : payload.get("password");
        Account account = accountService.authenticateByUsernameAndPassword(username, password);
        if (account == null) {
            return Result.error("账号名称或密码错误");
        }
        if (account.getWechat_openid() != null && !account.getWechat_openid().trim().isEmpty()) {
            return Result.error("该员工账号已绑定其他微信，请联系管理员处理");
        }

        Account bound = accountService.bindWechatOpenid((long) account.getId(), openid);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("bound", true);
        result.put("accountId", bound.getId());
        result.put("redirectUrl", wechatOAuthService.buildStaffHomeUrl((long) bound.getId()));
        return Result.success(result);
    }

    @GetMapping("/overview")
    public Result overview(@RequestParam Long accountId,
                           @RequestParam String staffToken) {
        if (accountId == null || accountId <= 0) {
            return Result.error("员工账号不存在");
        }
        Long authorizedAccountId = wechatOAuthService.resolveStaffPortalToken(staffToken);
        if (authorizedAccountId == null || !authorizedAccountId.equals(accountId)) {
            return Result.error("员工身份校验失败，请重新从公众号进入");
        }
        Account account = accountService.selectById(accountId).stream().findFirst().orElse(null);
        if (account == null) {
            return Result.error("员工账号不存在");
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("account", account);
        payload.put("summary", buildSummary(account));
        payload.put("quickActions", buildQuickActions(account));
        return Result.success(payload);
    }

    private Map<String, Object> buildSummary(Account account) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("displayName", account.getName());
        summary.put("doctorName", "doctor".equals(resolveRoleCode(account.getRole())) ? account.getName() : null);
        summary.put("roleLabel", resolveRoleLabel(account.getRole()));
        summary.put("username", account.getUsername());
        summary.put("wechatBound", account.getWechat_openid() != null && !account.getWechat_openid().trim().isEmpty());
        return summary;
    }

    private Map<String, Object> buildQuickActions(Account account) {
        Map<String, Object> quickActions = new LinkedHashMap<>();
        String accountId = String.valueOf(account.getId());
        String doctorName = "doctor".equals(resolveRoleCode(account.getRole())) && account.getName() != null
                ? account.getName().trim()
                : "";
        quickActions.put("appointments", "/staff-h5/appointments?accountId=" + accountId);
        quickActions.put("consultations", "/staff-h5/consultations?accountId=" + accountId + (doctorName.isEmpty() ? "" : "&doctorName=" + doctorName));
        quickActions.put("patients", "/staff-h5/patients?accountId=" + accountId + (doctorName.isEmpty() ? "" : "&doctorName=" + doctorName));
        quickActions.put("patient360", "/staff-h5/patient360?accountId=" + accountId + (doctorName.isEmpty() ? "" : "&doctorName=" + doctorName));
        return quickActions;
    }

    private String resolveRoleLabel(String role) {
        String normalized = resolveRoleCode(role);
        if (normalized.isEmpty()) {
            return "员工";
        }
        switch (normalized) {
            case "admin":
                return "管理员";
            case "doctor":
                return "医生";
            case "nurse":
                return "护士";
            default:
                return role;
        }
    }

    private String resolveRoleCode(String role) {
        if (role == null) {
            return "";
        }
        String normalized = role.trim();
        switch (normalized) {
            case "管理员":
                return "admin";
            case "医生":
                return "doctor";
            case "护士":
                return "nurse";
            default:
                return normalized;
        }
    }
}
