package com.example.springboot.controller;


import com.example.springboot.common.Result;
import com.example.springboot.entity.Account;
import com.example.springboot.entity.Patient;
import com.example.springboot.entity.PatientWorkbenchQuery;
import com.example.springboot.service.AccountService;
import com.example.springboot.service.ConsultationRecordService;
import com.example.springboot.service.PatientService;
import com.example.springboot.service.PatientWorkbenchService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/patients")
public class PatientController {

    private static final Pattern PATIENT_PHONE_PATTERN = Pattern.compile("^\\d{11}$");
    private static final String OPERATOR_ACCOUNT_ID_HEADER = "X-Operator-Account-Id";
    private static final String SECONDARY_PASSWORD_HEADER = "X-Secondary-Password";
    @Value("${security.patient-admin-secondary-password:246810}")
    private String patientAdminSecondaryPassword;

    @Autowired
    private PatientService patientService;

    @Autowired
    private PatientWorkbenchService patientWorkbenchService;

    @Autowired
    private AccountService accountService;

    @GetMapping("/selectAll")
    public Result getPatientList(@RequestParam int page, @RequestParam int size) {
        PageHelper.startPage(page, size);
        List<Patient> PatientList = patientService.selectAll();
        PageInfo<Patient> pageInfo = new PageInfo<>(PatientList);
        return Result.success(pageInfo);
    }

    @GetMapping("/selectAllForH5")
    public Result selectAllForH5() {
        return Result.success(patientService.selectAll());
    }

    @GetMapping("/selectByid")
    public Result selectById(@RequestParam Long id,@RequestParam int page, @RequestParam int size) {
        PageHelper.startPage(page, size);
        List<Patient> PatientList = patientService.selectById(id);
        PageInfo<Patient> pageInfo = new PageInfo<>(PatientList);
        return Result.success(pageInfo);
    }

    @GetMapping("/selectByname")
    public Result selectByName(@RequestParam String name,@RequestParam int page, @RequestParam int size) {
        return Result.success(buildPageResult(patientService.searchPatients(name), page, size));
    }

    @GetMapping("/search")
    public Result search(@RequestParam(required = false) String keyword,
                         @RequestParam(defaultValue = "1") int page,
                         @RequestParam(defaultValue = "20") int size) {
        return Result.success(buildPageResult(patientService.searchPatients(keyword), page, size));
    }

    @GetMapping("/workbench")
    public Result workbench(PatientWorkbenchQuery query) {
        return Result.success(patientWorkbenchService.search(query));
    }

    @GetMapping("/workbench/export")
    public Result exportWorkbench(PatientWorkbenchQuery query,
                                  @RequestHeader(value = OPERATOR_ACCOUNT_ID_HEADER, required = false) Long operatorAccountId,
                                  @RequestHeader(value = SECONDARY_PASSWORD_HEADER, required = false) String secondaryPassword) {
        String validation = validateSensitivePatientOperation(operatorAccountId, secondaryPassword);
        if (validation != null) {
            return Result.error("403", validation);
        }
        return Result.success(patientWorkbenchService.search(query));
    }

    @PostMapping("/add")
    public Result addPatient(@RequestBody Patient patient,
                             @RequestHeader(value = OPERATOR_ACCOUNT_ID_HEADER, required = false) Long operatorAccountId) {
        String roleValidation = validateOperatorRole(operatorAccountId);
        if (roleValidation != null) {
            return Result.error("403", roleValidation);
        }
        String validation = validatePatient(patient, true);
        if (validation != null) {
            return Result.error(validation);
        }
        try {
            patientService.addPatient(patient);
            return Result.success(patient);
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @PutMapping("/edit")
    public Result updatePatient(@RequestBody Patient patient,
                                @RequestHeader(value = OPERATOR_ACCOUNT_ID_HEADER, required = false) Long operatorAccountId) {
        String roleValidation = validateOperatorRole(operatorAccountId);
        if (roleValidation != null) {
            return Result.error("403", roleValidation);
        }
        String validation = validatePatient(patient, false);
        if (validation != null) {
            return Result.error(validation);
        }
        try {
            patientService.updatePatient(patient);
            return Result.success(patient);
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public Result deletePatient(@PathVariable int id,
                                @RequestHeader(value = OPERATOR_ACCOUNT_ID_HEADER, required = false) Long operatorAccountId,
                                @RequestHeader(value = SECONDARY_PASSWORD_HEADER, required = false) String secondaryPassword) {
        String validation = validateSensitivePatientOperation(operatorAccountId, secondaryPassword);
        if (validation != null) {
            return Result.error("403", validation);
        }
        patientService.deletePatient(id);
        return Result.success("删除成功");
    }

    @DeleteMapping("/deleteBatch")
    public Result deletePatientBatch(@RequestBody List<Long> ids,
                                     @RequestHeader(value = OPERATOR_ACCOUNT_ID_HEADER, required = false) Long operatorAccountId,
                                     @RequestHeader(value = SECONDARY_PASSWORD_HEADER, required = false) String secondaryPassword) {
        String validation = validateSensitivePatientOperation(operatorAccountId, secondaryPassword);
        if (validation != null) {
            return Result.error("403", validation);
        }
        try {
            patientService.deletePatientBatch(ids);
            return Result.success("批量删除成功");
        } catch (Exception e) {
            return Result.error("批量删除失败：" + e.getMessage());
        }
    }

    private String validateSensitivePatientOperation(Long operatorAccountId, String secondaryPassword) {
        if (operatorAccountId == null || operatorAccountId <= 0) {
            return "请重新登录管理员账号后再试";
        }
        List<Account> accounts = accountService.selectById(operatorAccountId);
        if (accounts == null || accounts.isEmpty() || accounts.get(0) == null) {
            return "操作账号不存在";
        }
        Account account = accounts.get(0);
        if (!"admin".equals(normalizeRoleCode(account.getRole()))) {
            return "只有管理员账号可以执行该操作";
        }
        if (!patientAdminSecondaryPassword.equals(StringUtils.hasText(secondaryPassword) ? secondaryPassword.trim() : "")) {
            return "二级密码错误";
        }
        return null;
    }

    private String validateOperatorRole(Long operatorAccountId) {
        if (operatorAccountId == null || operatorAccountId <= 0) {
            return "请重新登录后再试";
        }
        List<Account> accounts = accountService.selectById(operatorAccountId);
        if (accounts == null || accounts.isEmpty() || accounts.get(0) == null) {
            return "操作账号不存在";
        }
        Account account = accounts.get(0);
        String roleCode = normalizeRoleCode(account.getRole());
        if (!"admin".equals(roleCode) && !"doctor".equals(roleCode) && !"nurse".equals(roleCode)) {
            return "当前账号无权执行该操作";
        }
        return null;
    }

    private String normalizeRoleCode(String role) {
        if (!StringUtils.hasText(role)) {
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

    private String validatePatient(Patient patient, boolean requireCustomerSource) {
        if (patient == null) {
            return "患者信息不能为空";
        }
        if (!StringUtils.hasText(patient.getName())) {
            return "患者姓名必填";
        }
        if (!StringUtils.hasText(patient.getGender())) {
            return "患者性别必填";
        }
        if (patient.getAge() == null) {
            return "患者年龄必填";
        }
        if (patient.getAge() < 0 || patient.getAge() > 150) {
            return "患者年龄需在0到150之间";
        }
        if (!StringUtils.hasText(patient.getPhone())) {
            return "手机号码必填";
        }
        String normalizedPhone = patient.getPhone().trim();
        if (!PATIENT_PHONE_PATTERN.matcher(normalizedPhone).matches()) {
            return "手机号码需为11位数字";
        }
        patient.setPhone(normalizedPhone);
        String normalizedCustomerSource = patient.getCustomer_source() == null ? "" : patient.getCustomer_source().trim();
        boolean hasReferralPayload = hasReferralPayload(patient);
        if (requireCustomerSource && !StringUtils.hasText(normalizedCustomerSource) && !hasReferralPayload) {
            return "客户来源必填";
        }
        if (StringUtils.hasText(normalizedCustomerSource)
                && !ConsultationRecordService.CUSTOMER_SOURCE_OPTIONS.contains(normalizedCustomerSource)) {
            return "客户来源不合法";
        }
        boolean hasRelationType = StringUtils.hasText(patient.getRelation_type());
        boolean hasRelatedPatient = patient.getRelated_patient_id() != null || StringUtils.hasText(patient.getRelated_patient_name());
        if (!hasRelationType && hasRelatedPatient) {
            return "请选择患者关系";
        }
        if (hasRelatedPatient && (patient.getRelated_patient_id() == null || patient.getRelated_patient_id() <= 0)) {
            return "请选择有效的关联患者";
        }
        if (patient.getRelated_patient_id() != null && patient.getRelated_patient_id() > 0) {
            if (!StringUtils.hasText(patient.getRelated_patient_name())) {
                return "关联患者姓名不能为空";
            }
            if (patient.getId() > 0 && patient.getRelated_patient_id().longValue() == patient.getId()) {
                return "关联患者不能是本人";
            }
        }
        return null;
    }

    private boolean hasReferralPayload(Patient patient) {
        if (patient == null) {
            return false;
        }
        return patient.getReferrer_patient_id() != null && patient.getReferrer_patient_id() > 0
                || StringUtils.hasText(patient.getReferrer_patient_name())
                || StringUtils.hasText(patient.getExternal_referrer_type())
                || StringUtils.hasText(patient.getExternal_referrer_name())
                || StringUtils.hasText(patient.getExternal_referrer_contact())
                || StringUtils.hasText(patient.getReferrer_type())
                || StringUtils.hasText(patient.getReferral_remark());
    }

    private Map<String, Object> buildPageResult(List<Patient> patients, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        List<Patient> safePatients = patients == null ? List.of() : patients;
        int total = safePatients.size();
        int fromIndex = Math.min((safePage - 1) * safeSize, total);
        int toIndex = Math.min(fromIndex + safeSize, total);
        int pages = total == 0 ? 0 : (int) Math.ceil(total / (double) safeSize);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", total);
        result.put("list", safePatients.subList(fromIndex, toIndex));
        result.put("pageNum", safePage);
        result.put("pageSize", safeSize);
        result.put("size", toIndex - fromIndex);
        result.put("pages", pages);
        result.put("isFirstPage", safePage <= 1);
        result.put("isLastPage", pages == 0 || safePage >= pages);
        result.put("hasPreviousPage", safePage > 1);
        result.put("hasNextPage", safePage < pages);
        return result;
    }

}
