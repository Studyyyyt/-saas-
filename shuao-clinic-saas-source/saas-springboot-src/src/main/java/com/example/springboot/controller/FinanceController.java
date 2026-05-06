package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.Finance;
import com.example.springboot.service.FinanceService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Calendar;
import java.util.List;
import com.example.springboot.util.PagingSupport;

@CrossOrigin(origins = "http://localhost:7070")
@RestController
@RequestMapping("/finances")
public class FinanceController {

    @Autowired
    private FinanceService financeService;


    @GetMapping("/all")
    public Result selectAll(@RequestParam int page, @RequestParam int size) {
        PageHelper.startPage(page, size);
        List<Finance> financeList = financeService.getAllFinances();
        PageInfo<Finance> pageInfo = new PageInfo<>(financeList);
        return Result.success(pageInfo);
    }

    @GetMapping("/recentByPatientId")
    public Result getRecentFinancesByPatientId(@RequestParam Long patientId,
                                               @RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(financeService.getRecentFinancesByPatientId(patientId, limit));
    }

    @GetMapping("/selectByid")
    public Result getFinanceByid(@RequestParam Long id,@RequestParam int page, @RequestParam int size) {
        PageHelper.startPage(page, size);
        List<Finance> financeList = financeService.getFinanceByid(id);
        PageInfo<Finance> pageInfo = new PageInfo<>(financeList);
        return Result.success(pageInfo);
    }

    @GetMapping("/selectByname")
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
    public Result addManualExpense(@RequestBody Finance finance) {
        try {
            return Result.success(financeService.addManualExpense(finance));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @PutMapping("/manualExpense/edit")
    public Result editManualExpense(@RequestBody Finance finance) {
        try {
            return Result.success(financeService.editManualExpense(finance));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @DeleteMapping("/manualExpense/delete/{id}")
    public Result deleteManualExpense(@PathVariable Long id) {
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
    public Result addFinance(@RequestBody Finance finance) {
        financeService.addFinance(finance);
        return Result.success("新增成功");
    }

    @PutMapping("/edit")
    public Result editFinance(@RequestBody Finance finance) {
        financeService.editFinance(finance);
        return Result.success("编辑成功");
    }


    @PutMapping("/update")
    public String updateFinance(@RequestBody Finance finance) {
        financeService.updateFinance(finance);
        return "Finance record updated successfully!";
    }

    @DeleteMapping("/delete/{id}")
    public String deleteFinance(@PathVariable int id) {
        financeService.deleteFinance(id);
        return "Finance record deleted successfully!";
    }

//    @GetMapping("/selectByyearmonth/{year}/{month}")
//    public Result getFinanceByYearMonth(@RequestParam int year, @RequestParam int month) {
//        List<Finance> financeList = financeService.getFinanceByYearMonth(year, month);
//        return Result.success(financeList);
//    }
}
