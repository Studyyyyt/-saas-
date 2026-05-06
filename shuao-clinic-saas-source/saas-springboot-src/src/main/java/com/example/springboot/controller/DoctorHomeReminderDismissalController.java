package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.DoctorHomeReminderDismissal;
import com.example.springboot.service.DoctorHomeReminderDismissalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/doctor-home-reminders")
public class DoctorHomeReminderDismissalController {

    @Autowired
    private DoctorHomeReminderDismissalService doctorHomeReminderDismissalService;

    @GetMapping("/dismissed")
    public Result selectDismissed(@RequestParam Long doctorAccountId) {
        try {
            return Result.success(doctorHomeReminderDismissalService.selectReminderKeysByDoctorAccountId(doctorAccountId));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @PostMapping("/dismiss")
    public Result dismiss(@RequestBody DoctorHomeReminderDismissal item) {
        try {
            doctorHomeReminderDismissalService.dismiss(item);
            return Result.success();
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @DeleteMapping("/dismissed")
    public Result clearDismissed(@RequestParam Long doctorAccountId,
                                 @RequestParam String reminderKey) {
        try {
            doctorHomeReminderDismissalService.clear(doctorAccountId, reminderKey);
            return Result.success();
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }
}
