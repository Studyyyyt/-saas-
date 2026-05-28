package com.example.springboot.service;

import com.example.springboot.entity.Appointment;
import com.example.springboot.entity.Patient;
import com.example.springboot.mapper.AppointmentMapper;
import com.example.springboot.mapper.PatientMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
public class AppointmentService {
    private static final Set<String> CANCELLABLE_STATUSES = Set.of("待治疗", "已预约", "待就诊", "已改约");


    @Autowired
    private AppointmentMapper appointmentMapper;

    @Autowired
    private PatientMapper patientMapper;

    @Autowired
    private AccountService accountService;

    @Autowired
    private TreatmentBillingService treatmentBillingService;

    public List<Appointment> getAllAppointments() {
        List<Appointment> appointments = appointmentMapper.selectAll();
        treatmentBillingService.enrichAppointments(appointments);
        return appointments;
    }

    public List<Appointment> getScheduleEntries() {
        List<Appointment> result = appointmentMapper.selectAll();
        if (result == null) {
            result = new ArrayList<>();
        }
        result.sort(Comparator
                .comparing(Appointment::getAppointment_date, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(Appointment::getAppointment_time, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Appointment::getId));
        treatmentBillingService.enrichAppointments(result);
        return result;
    }
    public List<Appointment> getAllAppointmentsByStatus(String status) {
        List<Appointment> appointments = appointmentMapper.findAllByStatus(status);
        treatmentBillingService.enrichAppointments(appointments);
        return appointments;
    }

    /**
     * 多条件组合查询预约记录
     *
     * @param status 预约状态
     * @param appointmentDate 预约日期，格式 yyyy-MM-dd
     * @param doctorAccountId 医生账号ID
     * @return 预约列表
     */
    public List<Appointment> searchAppointments(String status, String appointmentDate, String startDate, String endDate, Long doctorAccountId) {
        List<Appointment> appointments = appointmentMapper.searchAppointments(status, appointmentDate, startDate, endDate, doctorAccountId);
        treatmentBillingService.enrichAppointments(appointments);
        return appointments;
    }

    public List<Appointment> selectById(Long id) {
        List<Appointment> appointments = appointmentMapper.selectById(id);
        treatmentBillingService.enrichAppointments(appointments);
        return appointments;
    }
    public List<Appointment> selectByIdAndStatus(Long id, String status) {
        List<Appointment> appointments = appointmentMapper.findByIdAndStatus(id, status);
        treatmentBillingService.enrichAppointments(appointments);
        return appointments;
    }

    public List<Appointment> selectByName(String name) {
        List<Appointment> appointments = appointmentMapper.selectByName(name);
        treatmentBillingService.enrichAppointments(appointments);
        return appointments;
    }
    public List<Appointment> selectByNameAndStatus(String name, String status) {
        List<Appointment> appointments = appointmentMapper.findByNameAndStatus(name, status);
        treatmentBillingService.enrichAppointments(appointments);
        return appointments;
    }

    public void addAppointment(Appointment appointment) {
        List<Patient> matchedPatients = populatePatientReference(appointment);
        populateDoctorReference(appointment);
        validateAppointment(appointment);
        checkAppointmentConflict(appointment);
        normalizeAppointmentForSave(appointment);
        System.out.println("[APPOINTMENT_ADD] patient=" + appointment.getPatient_name()
                + ", date=" + appointment.getAppointment_date()
                + ", time=" + appointment.getAppointment_time()
                + ", doctorAccountId=" + appointment.getDoctor_account_id()
                + ", doctor=" + appointment.getDoctor_name()
                + ", purpose=" + appointment.getAppointment_purpose());
        appointmentMapper.insert(appointment);
    }

    @Transactional
    public Appointment ensureTreatmentGeneratedAppointment(Appointment appointment) {
        populatePatientReference(appointment);
        populateDoctorReference(appointment);
        validateAppointment(appointment);
        normalizeAppointmentForSave(appointment);
        Appointment existing = findExistingSameDayDoctorAppointment(appointment);
        if (existing != null) {
            return existing;
        }
        appointmentMapper.insert(appointment);
        return appointment;
    }

    public void editAppointment(Appointment appointment) {
        populatePatientReference(appointment);
        populateDoctorReference(appointment);
        validateAppointment(appointment);
        checkAppointmentConflict(appointment);
        normalizeAppointmentForSave(appointment);
        appointmentMapper.update(appointment);
    }

    public Appointment cancelPatientAppointment(Long id, String reason) {
        List<Appointment> appointments = appointmentMapper.selectById(id);
        if (appointments == null || appointments.isEmpty()) {
            throw new IllegalArgumentException("预约不存在");
        }
        Appointment appointment = appointments.get(0);
        String status = appointment.getStatus() == null ? "" : appointment.getStatus().trim();
        if (!CANCELLABLE_STATUSES.contains(status)) {
            throw new IllegalArgumentException("当前预约状态不可取消");
        }
        String finalReason = (reason == null || reason.trim().isEmpty()) ? "患者计划变更" : reason.trim();
        appointment.setStatus("已取消");
        appointment.setCancel_reason(finalReason);
        appointmentMapper.update(appointment);
        return appointment;
    }

    public void deleteAppointment(int id) {
        appointmentMapper.delete(id);
    }

    public void deleteByPatientReference(Long patientId) {
        if (patientId == null || patientId <= 0) {
            return;
        }
        appointmentMapper.deleteByPatientReference(patientId);
    }

    @Transactional
    public void deleteAppointmentBatch(List<Long> ids) {
        appointmentMapper.deleteAppointmentBatch(ids);
    }

    public void updateStatus(Long id, String status) {
        appointmentMapper.updateStatus(id, status);
    }

    public void updateClinicStatus(Long id, String clinicStatus) {
        List<Appointment> list = appointmentMapper.selectById(id);
        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException("预约不存在");
        }
        Appointment appointment = list.get(0);
        appointment.setClinic_status(clinicStatus);
        if ("已挂号".equals(clinicStatus) && appointment.getCheck_in_time() == null) {
            appointment.setCheck_in_time(new java.util.Date());
        }
        appointmentMapper.update(appointment);
    }

    public List<Appointment> selectPatientAppointments(Long patientId) {
        if (patientId == null || patientId <= 0) {
            return List.of();
        }
        List<Appointment> appointments = appointmentMapper.selectByPatientReference(patientId);
        if (appointments == null) {
            return List.of();
        }
        List<Appointment> orderedAppointments = new ArrayList<>(appointments);
        orderedAppointments.sort(Comparator
                .comparing(Appointment::getAppointment_date, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(Appointment::getAppointment_time, Comparator.nullsLast(Comparator.reverseOrder())));
        if (treatmentBillingService != null) {
            treatmentBillingService.enrichAppointments(orderedAppointments);
        }
        return orderedAppointments;
    }

    public List<Appointment> selectPatientAppointments(String patientName) {
        return List.of();
    }

    public List<Appointment> selectByDoctorName(String doctorName) {
        if (doctorName == null || doctorName.trim().isEmpty()) {
            return List.of();
        }
        List<Appointment> appointments = appointmentMapper.selectAll();
        if (appointments == null) {
            return List.of();
        }
        String doctor = doctorName.trim();
        List<Appointment> result = new ArrayList<>();
        for (Appointment appointment : appointments) {
            if (appointment != null && appointment.getDoctor_name() != null && doctor.equals(appointment.getDoctor_name().trim())) {
                result.add(appointment);
            }
        }
        result.sort(Comparator
                .comparing(Appointment::getAppointment_date, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(Appointment::getAppointment_time, Comparator.nullsLast(Comparator.reverseOrder())));
        if (treatmentBillingService != null) {
            treatmentBillingService.enrichAppointments(result);
        }
        return result;
    }

    public void sendNextDayAppointmentReminders() {
        Date targetDate = Date.valueOf(LocalDate.now().plusDays(1));
        List<Appointment> appointments = appointmentMapper.selectByAppointmentDate(targetDate);
        System.out.println("[WECHAT_REMINDER_RUN] targetDate=" + targetDate
                + ", appointments=" + (appointments == null ? 0 : appointments.size()));
        if (appointments == null || appointments.isEmpty()) {
            return;
        }
        for (Appointment appointment : appointments) {
            if (appointment == null) {
                continue;
            }
            String status = appointment.getStatus() == null ? "" : appointment.getStatus().trim();
            if ("已取消".equals(status)) {
                continue;
            }
        }
    }

    private void validateAppointment(Appointment appointment) {
        if (appointment == null) {
            throw new IllegalArgumentException("预约信息不能为空");
        }
        if (appointment.getPatient_name() == null || appointment.getPatient_name().trim().isEmpty()) {
            throw new IllegalArgumentException("患者姓名必填");
        }
        if (appointment.getPatient_id() == null || appointment.getPatient_id() <= 0) {
            throw new IllegalArgumentException("患者ID必填");
        }
        if (appointment.getAppointment_date() == null) {
            throw new IllegalArgumentException("预约日期必填");
        }
        if (appointment.getAppointment_time() == null) {
            throw new IllegalArgumentException("预约时间必填");
        }
        if (appointment.getDuration_minutes() != null && appointment.getDuration_minutes() <= 0) {
            throw new IllegalArgumentException("预约时长必须大于0");
        }
        if ((appointment.getDoctor_account_id() == null || appointment.getDoctor_account_id() <= 0)
                && !StringUtils.hasText(appointment.getDoctor_name())) {
            throw new IllegalArgumentException("医生账号必填");
        }
        if (appointment.getAppointment_purpose() == null || appointment.getAppointment_purpose().trim().isEmpty()) {
            throw new IllegalArgumentException("预约目的必填");
        }
    }

    private List<Patient> populatePatientReference(Appointment appointment) {
        if (appointment == null) {
            return List.of();
        }
        if (patientMapper == null) {
            return List.of();
        }
        if (appointment.getPatient_id() != null && appointment.getPatient_id() > 0) {
            List<Patient> patients = patientMapper.selectById(appointment.getPatient_id());
            if (patients != null && !patients.isEmpty() && patients.get(0) != null) {
                Patient patient = patients.get(0);
                appointment.setPatient_id((long) patient.getId());
                if (StringUtils.hasText(patient.getName())) {
                    appointment.setPatient_name(patient.getName().trim());
                }
                return patients;
            }
        }
        return List.of();
    }

    private void populateDoctorReference(Appointment appointment) {
        if (appointment == null) {
            return;
        }
        if (StringUtils.hasText(appointment.getDoctor_name())) {
            appointment.setDoctor_name(appointment.getDoctor_name().trim());
        }
        if ((appointment.getDoctor_account_id() == null || appointment.getDoctor_account_id() <= 0)
                && accountService != null
                && StringUtils.hasText(appointment.getDoctor_name())) {
            Long doctorAccountId = accountService.findDoctorAccountIdByName(appointment.getDoctor_name());
            if (doctorAccountId != null && doctorAccountId > 0) {
                appointment.setDoctor_account_id(doctorAccountId);
            }
        }
    }

    private void normalizeAppointmentForSave(Appointment appointment) {
        if (appointment.getStatus() == null || appointment.getStatus().trim().isEmpty()) {
            appointment.setStatus("待治疗");
        } else {
            appointment.setStatus(appointment.getStatus().trim());
        }
        appointment.setPatient_name(appointment.getPatient_name().trim());
        appointment.setAppointment_purpose(appointment.getAppointment_purpose().trim());
        if (appointment.getDuration_minutes() == null || appointment.getDuration_minutes() <= 0) {
            appointment.setDuration_minutes(60);
        }
        if (appointment.getDoctor_account_id() == null || appointment.getDoctor_account_id() <= 0) {
            if (!StringUtils.hasText(appointment.getDoctor_name())) {
                throw new IllegalArgumentException("医生账号必填");
            }
            appointment.setDoctor_name(appointment.getDoctor_name().trim());
            return;
        }
        if (accountService == null) {
            if (!StringUtils.hasText(appointment.getDoctor_name())) {
                throw new IllegalArgumentException("医生姓名必填");
            }
            appointment.setDoctor_name(appointment.getDoctor_name().trim());
            return;
        }
        String doctorName = accountService.findDoctorDisplayNameByAccountId(appointment.getDoctor_account_id());
        if (!StringUtils.hasText(doctorName)) {
            throw new IllegalArgumentException("医生账号不存在或未启用");
        }
        appointment.setDoctor_name(doctorName.trim());
    }

    private Appointment findExistingSameDayDoctorAppointment(Appointment target) {
        if (target == null || target.getPatient_id() == null || target.getPatient_id() <= 0) {
            return null;
        }
        List<Appointment> appointments = appointmentMapper.selectByPatientReference(target.getPatient_id());
        if (appointments == null || appointments.isEmpty()) {
            return null;
        }
        for (Appointment appointment : appointments) {
            if (appointment == null) {
                continue;
            }
            if (!sameDate(appointment.getAppointment_date(), target.getAppointment_date())) {
                continue;
            }
            if (!sameDoctor(appointment, target)) {
                continue;
            }
            String status = appointment.getStatus() == null ? "" : appointment.getStatus().trim();
            if ("已取消".equals(status)) {
                continue;
            }
            return appointment;
        }
        return null;
    }

    private boolean sameDate(Date left, Date right) {
        if (left == null || right == null) {
            return false;
        }
        return left.toLocalDate().equals(right.toLocalDate());
    }

    private void checkAppointmentConflict(Appointment appointment) {
        if (appointment == null || appointment.getAppointment_date() == null || appointment.getAppointment_time() == null) {
            return;
        }
        List<Appointment> existingAppointments = appointmentMapper.selectByAppointmentDate(appointment.getAppointment_date());
        if (existingAppointments == null || existingAppointments.isEmpty()) {
            return;
        }
        for (Appointment existing : existingAppointments) {
            if (existing == null || existing.getId() == appointment.getId()) {
                continue;
            }
            String status = existing.getStatus() == null ? "" : existing.getStatus().trim();
            if ("已取消".equals(status)) {
                continue;
            }
            if (!sameDoctor(existing, appointment)) {
                continue;
            }
            if (appointment.getAppointment_time().equals(existing.getAppointment_time())) {
                throw new IllegalArgumentException("该医生在该时段已被预约，请选择其他时间");
            }
        }
    }

    private boolean sameDoctor(Appointment left, Appointment right) {
        Long leftDoctorAccountId = normalizePositiveDoctorAccountId(left == null ? null : left.getDoctor_account_id());
        Long rightDoctorAccountId = normalizePositiveDoctorAccountId(right == null ? null : right.getDoctor_account_id());
        if (leftDoctorAccountId != null && rightDoctorAccountId != null) {
            return leftDoctorAccountId.equals(rightDoctorAccountId);
        }
        String leftDoctorName = normalizeDoctorName(left == null ? null : left.getDoctor_name());
        String rightDoctorName = normalizeDoctorName(right == null ? null : right.getDoctor_name());
        return !leftDoctorName.isEmpty() && leftDoctorName.equals(rightDoctorName);
    }

    private Long normalizePositiveDoctorAccountId(Long doctorAccountId) {
        return doctorAccountId != null && doctorAccountId > 0 ? doctorAccountId : null;
    }

    private String normalizeDoctorName(String doctorName) {
        return doctorName == null ? "" : doctorName.trim();
    }

}
