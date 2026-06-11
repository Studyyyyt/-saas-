package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.Account;
import com.example.springboot.entity.Finance;
import com.example.springboot.service.AccountService;
import com.example.springboot.service.FinanceService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Calendar;
import java.util.List;
import com.example.springboot.util.PagingSupport;

@RestController
@RequestMapping("/finances")
public class FinanceController {

    private static final String OPERATOR_ACCOUNT_ID_HEADER = "X-Operator-Account-Id";
    private static final String SECONDARY_PASSWORD_HEADER = "X-Secondary-Password";
    @Value("${security.patient-admin-secondary-password:246810}")
    private String patientAdminSecondaryPassword;

    @Autowired
    private FinanceService financeService;

    @Autowired
    private AccountService accountService;


    @GetMapping("/selectAll")
    public Result selectAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String recordType,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) String keyword) {
        PageHelper.startPage(page, size);
        List<Finance> financeList = financeService.searchFinances(recordType, startDate, endDate, patientId, keyword);
        PageInfo<Finance> pageInfo = new PageInfo<>(financeList);
        return Result.success(pageInfo);
    }

    @GetMapping("/recentByPatientId")
    public Result getRecentFinancesByPatientId(@RequestParam Long patientId,
                                               @RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(financeService.getRecentFinancesByPatientId(patientId, limit));
    }

    @GetMapping("/selectById")
    public Result getFinanceByid(@RequestParam Long id,@RequestParam int page, @RequestParam int size) {
        PageHelper.startPage(page, size);
        List<Finance> financeList = financeService.getFinanceByid(id);
        PageInfo<Finance> pageInfo = new PageInfo<>(financeList);
        return Result.success(pageInfo);
    }

    @GetMapping("/selectByName")
    public Result getFinanceByname(@RequestParam String name,@RequestParam int page, @RequestParam int size) {
        PageHelper.startPage(page, size);
        List<Finance> financeList = financeService.getFinanceByname(name);
        PageInfo<Finance> pageInfo = new PageInfo<>(financeList);
        return Result.success(pageInfo);
    }

    @GetMapping("/selectByamount")
    public Result getFinanceByamount(@RequestParam int amount,@RequestParam int page, @RequestParam int size) {
        PageHelper.startPage(page, size);
        List<Finance> financeList = financeService.getFinanceByamount(amount);
        PageInfo<Finance> pageInfo = new PageInfo<>(financeList);
        return Result.success(pageInfo);
    }

    @GetMapping("/selectBytype")
    public Result getFinanceBytype(@RequestParam String type,@RequestParam int page, @RequestParam int size) {
        PageHelper.startPage(page, size);
        List<Finance> financeList = financeService.getFinanceBytype(type);
        PageInfo<Finance> pageInfo = new PageInfo<>(financeList);
        return Result.success(pageInfo);
    }

    @GetMapping("/selectBydate")
    public Result getFinanceBydate(@RequestParam String date,@RequestParam int page, @RequestParam int size) {
        PageHelper.startPage(page, size);
        List<Finance> financeList = financeService.getFinanceBydate(date);
        PageInfo<Finance> pageInfo = new PageInfo<>(financeList);
        return Result.success(pageInfo);
    }

    @GetMapping("/selectByMonth")
    public Result getFinancesByMonth(@RequestParam int year, @RequestParam int month) {
        List<Finance> financeList = financeService.getFinancesByMonth(year, month);
        return Result.success(financeList);
    }

    @GetMapping("/doctorPerformance")
    public Result getDoctorPerformance(@RequestParam(required = false) String startDate,
                                       @RequestParam(required = false) String endDate,
                                       @RequestParam(required = false) Long doctorAccountId,
                                       @RequestParam(required = false) String doctorName) {
        try {
            return Result.success(financeService.getDoctorPerformance(startDate, endDate, doctorAccountId, doctorName));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @GetMapping("/expenseOverview")
    public Result getExpenseOverview(@RequestParam(required = false) String startDate,
                                     @RequestParam(required = false) String endDate) {
        try {
            return Result.success(financeService.buildExpenseOverview(startDate, endDate));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @GetMapping("/manualExpenseSearch")
    public Result searchManualExpenses(@RequestParam(required = false) String startDate,
                                       @RequestParam(required = false) String endDate,
                                       @RequestParam(required = false) String keyword,
                                       @RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "20") int size) {
        try {
            return Result.success(PagingSupport.buildPageResult(
                    financeService.searchManualExpenses(startDate, endDate, keyword),
                    page,
                    size
            ));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @PostMapping("/manualExpense/add")
    public Result addManualExpense(@RequestBody Finance finance,
                                   @RequestHeader(value = OPERATOR_ACCOUNT_ID_HEADER, required = false) Long operatorAccountId) {
        String roleValidation = validateOperatorRole(operatorAccountId);
        if (roleValidation != null) {
            return Result.error("403", roleValidation);
        }
        try {
            return Result.success(financeService.addManualExpense(finance));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @PutMapping("/manualExpense/edit")
    public Result editManualExpense(@RequestBody Finance finance,
                                    @RequestHeader(value = OPERATOR_ACCOUNT_ID_HEADER, required = false) Long operatorAccountId) {
        String roleValidation = validateOperatorRole(operatorAccountId);
        if (roleValidation != null) {
            return Result.error("403", roleValidation);
        }
        try {
            return Result.success(financeService.editManualExpense(finance));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @DeleteMapping("/manualExpense/delete/{id}")
    public Result deleteManualExpense(@PathVariable Long id,
                                      @RequestHeader(value = OPERATOR_ACCOUNT_ID_HEADER, required = false) Long operatorAccountId,
                                      @RequestHeader(value = SECONDARY_PASSWORD_HEADER, required = false) String secondaryPassword) {
        String validation = validateSensitiveOperation(operatorAccountId, secondaryPassword);
        if (validation != null) {
            return Result.error("403", validation);
        }
        try {
            financeService.deleteManualExpense(id);
            return Result.success("删除成功");
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @GetMapping("/select1Byid")
    public Result getFinanceByidAndMonth(@RequestParam Long id, @RequestParam(required = false) Integer year,
                                         @RequestParam(required = false) Integer month) {
        if (year == null || month == null) {
            // 如果未提供年份和月份，则默认使用当前年份和月份
            Calendar calendar = Calendar.getInstance();
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH) + 1; // Calendar.MONTH 返回 0-11，需要 +1
        }
        List<Finance> financeList = financeService.getFinanceByidAndMonth(id, year, month);
        return Result.success(financeList);
    }

    @GetMapping("/select1Byamount")
    public Result getFinanceByamountAndMonth(@RequestParam String amount, @RequestParam(required = false) Integer year,
                                         @RequestParam(required = false) Integer month) {
        if (year == null || month == null) {
            // 如果未提供年份和月份，则默认使用当前年份和月份
            Calendar calendar = Calendar.getInstance();
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH) + 1; // Calendar.MONTH 返回 0-11，需要 +1
        }
        List<Finance> financeList = financeService.getFinanceByamountAndMonth(amount, year, month);
        return Result.success(financeList);
    }

    @GetMapping("/select1Byname")
    public Result getFinanceBynameAndMonth(@RequestParam String name, @RequestParam(required = false) Integer year,
                                           @RequestParam(required = false) Integer month) {
        if (year == null || month == null) {
            // 如果未提供年份和月份，则默认使用当前年份和月份
            Calendar calendar = Calendar.getInstance();
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH) + 1; // Calendar.MONTH 返回 0-11，需要 +1
        }
        List<Finance> financeList = financeService.getFinanceBynameAndMonth(name, year, month);
        return Result.success(financeList);
    }

    @GetMapping("/select1Bydate")
    public Result getFinanceBydateAndMonth(@RequestParam String date, @RequestParam(required = false) Integer year,
                                           @RequestParam(required = false) Integer month) {
        if (year == null || month == null) {
            // 如果未提供年份和月份，则默认使用当前年份和月份
            Calendar calendar = Calendar.getInstance();
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH) + 1; // Calendar.MONTH 返回 0-11，需要 +1
        }
        List<Finance> financeList = financeService.getFinanceBydateAndMonth(date, year, month);
        return Result.success(financeList);
    }

    @GetMapping("/select1Bytype")
    public Result getFinanceBytypeAndMonth(@RequestParam String type, @RequestParam(required = false) Integer year,
                                           @RequestParam(required = false) Integer month) {
        if (year == null || month == null) {
            // 如果未提供年份和月份，则默认使用当前年份和月份
            Calendar calendar = Calendar.getInstance();
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH) + 1; // Calendar.MONTH 返回 0-11，需要 +1
        }
        List<Finance> financeList = financeService.getFinanceBytypeAndMonth(type, year, month);
        return Result.success(financeList);
    }

     //分页默认月份查询
//    @GetMapping("/all1")
//    public Result selectAll(@RequestParam int page, @RequestParam int size,
//                            @RequestParam(required = false) Integer year,
//                            @RequestParam(required = false) Integer month) {
//        if (year == null || month == null) {
//            // 获取当前年份和月份
//            Calendar calendar = Calendar.getInstance();
//            year = calendar.get(Calendar.YEAR);
//            month = calendar.get(Calendar.MONTH) + 1; // Calendar.MONTH 返回 0-11，需要 +1
//        }
//
//        PageHelper.startPage(page, size);
//        List<Finance> financeList = financeService.getFinancesByMonth(year, month);
//        PageInfo<Finance> pageInfo = new PageInfo<>(financeList);
//        return Result.success(pageInfo);
//    }

      //默认月份查询
//    @GetMapping("/all1")
//    public Result selectAll(@RequestParam(required = false) Integer year,
//                            @RequestParam(required = false) Integer month) {
//        if (year == null || month == null) {
//            // 获取当前年份和月份
//            Calendar calendar = Calendar.getInstance();
//            year = calendar.get(Calendar.YEAR);
//            month = calendar.get(Calendar.MONTH) + 1; // Calendar.MONTH 返回 0-11，需要 +1
//        }
//
//        List<Finance> financeList = financeService.getFinancesByMonth(year, month);
//        return Result.success(financeList);
//    }

      //默认月份id查询
//    @GetMapping("/selectByid")
//    public Result getFinanceByid(@RequestParam Long id, @RequestParam(required = false) Integer year,
//                                 @RequestParam(required = false) Integer month, @RequestParam int page, @RequestParam int size) {
//        if (year == null || month == null) {
//            // 如果未提供年份和月份，则默认使用当前年份和月份
//            Calendar calendar = Calendar.getInstance();
//            year = calendar.get(Calendar.YEAR);
//            month = calendar.get(Calendar.MONTH) + 1; // Calendar.MONTH 返回 0-11，需要 +1
//        }
//
//        PageHelper.startPage(page, size);
//        List<Finance> financeList = financeService.getFinanceByidAndMonth(id, year, month);
//        PageInfo<Finance> pageInfo = new PageInfo<>(financeList);
//        return Result.success(pageInfo);
//    }



    @PostMapping("/add")
    public Result addFinance(@RequestBody Finance finance,
                             @RequestHeader(value = OPERATOR_ACCOUNT_ID_HEADER, required = false) Long operatorAccountId) {
        String roleValidation = validateOperatorRole(operatorAccountId);
        if (roleValidation != null) {
            return Result.error("403", roleValidation);
        }
        financeService.addFinance(finance);
        return Result.success("新增成功");
    }

    @PutMapping("/edit")
    public Result editFinance(@RequestBody Finance finance,
                              @RequestHeader(value = OPERATOR_ACCOUNT_ID_HEADER, required = false) Long operatorAccountId) {
        String roleValidation = validateOperatorRole(operatorAccountId);
        if (roleValidation != null) {
            return Result.error("403", roleValidation);
        }
        financeService.editFinance(finance);
        return Result.success("编辑成功");
    }


    @PutMapping("/update")
    public String updateFinance(@RequestBody Finance finance) {
        financeService.updateFinance(finance);
        return "Finance record updated successfully!";
    }

    @DeleteMapping("/delete/{id}")
    public String deleteFinance(@PathVariable int id,
                                @RequestHeader(value = OPERATOR_ACCOUNT_ID_HEADER, required = false) Long operatorAccountId,
                                @RequestHeader(value = SECONDARY_PASSWORD_HEADER, required = false) String secondaryPassword) {
        String validation = validateSensitiveOperation(operatorAccountId, secondaryPassword);
        if (validation != null) {
            return validation;
        }
        financeService.deleteFinance(id);
        return "Finance record deleted successfully!";
    }

    private String validateSensitiveOperation(Long operatorAccountId, String secondaryPassword) {
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

//    @GetMapping("/selectByyearmonth/{year}/{month}")
//    public Result getFinanceByYearMonth(@RequestParam int year, @RequestParam int month) {
//        List<Finance> financeList = financeService.getFinanceByYearMonth(year, month);
//        return Result.success(financeList);
//    }
}
