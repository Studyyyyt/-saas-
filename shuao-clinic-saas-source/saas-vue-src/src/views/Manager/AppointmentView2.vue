<template>
  <div class="page-wrap">
    <div class="page-title-bar">
      <div>
        <div class="page-kicker">预约管理</div>
        <h2>日程预约</h2>
      </div>
      <div class="page-desc">支持按项目展示预约块，灰色区域表示医生非工作时间，拖动预约块上下边缘可直接修改开始时间和时长。</div>
    </div>

    <div class="schedule-layout">
      <el-card class="calendar-card" shadow="never">
        <div class="calendar-header">
          <div>
            <div class="calendar-title">当月日历</div>
            <div class="calendar-subtitle">点击某一天，右侧自动切换到当日预约。</div>
          </div>
          <el-tag size="mini" type="info">{{ selectedDate || '未选择日期' }}</el-tag>
        </div>
        <el-calendar v-model="calendarDate" @input="handleCalendarInput">
          <template slot="dateCell" slot-scope="{ data }">
            <div
              class="calendar-date-cell"
              :class="{
                'is-selected': data.day === selectedDate,
                'has-appointments': appointmentCountForDate(data.day) > 0
              }"
              @click.stop="handleCalendarDatePick(data.day)"
            >
              <span class="calendar-date-cell__day">{{ displayCalendarDay(data.day) }}</span>
              <span v-if="appointmentCountForDate(data.day)" class="calendar-date-cell__count">
                {{ appointmentCountForDate(data.day) }}单
              </span>
            </div>
          </template>
        </el-calendar>
      </el-card>

      <div class="schedule-main">
        <el-card class="filter-card" shadow="never">
          <div class="filter-row">
            <el-date-picker
              v-model="selectedDate"
              type="date"
              placeholder="选择日期"
              value-format="yyyy-MM-dd"
              format="yyyy-MM-dd"
              class="date-picker"
              @change="handleSelectedDateChange"
            />
            <el-select
              v-model="selectedDoctor"
              placeholder="选择医生"
              clearable
              class="doctor-filter"
              @change="handleDoctorFilterChange"
            >
              <el-option label="全部医生" value="ALL"></el-option>
              <el-option
                v-for="doctor in mergedDoctors"
                :key="doctor.id"
                :label="doctor.name"
                :value="doctor.id"
              ></el-option>
            </el-select>
            <el-button type="primary" icon="el-icon-refresh" @click="refreshBoard">刷新当天</el-button>
            <el-button type="primary" plain icon="el-icon-circle-plus-outline" @click="showAddDialogByHeader">新增预约</el-button>
          </div>
          <el-alert
            v-if="pendingPatientPrefill"
            class="pending-patient-alert"
            type="info"
            :closable="true"
            show-icon
            :title="`待带入患者：${pendingPatientPrefill.patient_name}`"
            :description="pendingPatientAlertDescription"
            @close="clearPendingPatientPrefill"
          />
        </el-card>

        <el-card class="schedule-card" shadow="never">
          <div class="schedule-header">
            <div>
              <div class="schedule-title">{{ selectedDate || '请选择日期' }}</div>
              <div class="schedule-subtitle">点击空白时段新增；灰色为非工作时间；点击预约块可快速改状态或进入编辑。</div>
            </div>
            <div class="schedule-tip">
              <span class="legend waiting"></span>未就诊
              <span class="legend visited"></span>已就诊
              <span class="legend left"></span>已离开
              <span class="legend rescheduled"></span>已改约
              <span class="legend arrears"></span>患者欠费
            </div>
          </div>

          <div v-if="doctorColumns.length" class="schedule-scroll">
            <div class="schedule-board" :style="boardStyle">
              <div class="schedule-corner">时间</div>
              <div
                v-for="doctor in doctorColumns"
                :key="`head-${doctor.id}`"
                class="doctor-head"
              >
                <span class="doctor-head__name">{{ doctor.name }}</span>
                <span class="doctor-head__shift" :class="doctorHeadShiftClass(doctor)">{{ doctorHeadShiftLabel(doctor) }}</span>
              </div>

              <div class="time-axis" :style="axisStyle">
                <div
                  v-for="label in timeLabels"
                  :key="label.key"
                  class="time-label"
                  :style="{ top: `${label.top}px` }"
                >{{ label.label }}</div>
              </div>

              <div
                v-for="doctor in doctorColumns"
                :key="`lane-${doctor.id}`"
                class="schedule-lane"
                :style="axisStyle"
                @click="handleLaneClick($event, doctor)"
              >
                <div
                  v-for="mask in nonWorkingMasksForDoctor(doctor)"
                  :key="`mask-${doctor.id}-${mask.key}`"
                  class="non-working-mask"
                  :class="nonWorkingMaskClass(mask)"
                  :style="nonWorkingMaskStyle(mask)"
                >
                  <span v-if="mask.showLabel" class="non-working-mask__label">{{ mask.label }}</span>
                </div>

                <div
                  v-for="marker in hourMarkers"
                  :key="`hour-${doctor.id}-${marker.key}`"
                  class="hour-line"
                  :style="{ top: `${marker.top}px` }"
                ></div>

                <div
                  v-for="item in appointmentsForDoctor(doctor.id)"
                  :key="item.id"
                  class="appointment-block"
                  :class="appointmentBlockClass(item)"
                  :style="appointmentBlockStyle(item)"
                  :title="appointmentTooltip(item)"
                  @click.stop="openQuickStatus(item)"
                >
                  <div class="resize-handle resize-handle--top" @mousedown.stop.prevent="startResize($event, item, 'top')"></div>
                  <div class="appointment-block__title-row">
                    <div class="appointment-block__name">{{ item.patient_name || '未命名患者' }}</div>
                  <div v-if="showAppointmentBadges(item)" class="appointment-block__badges">
                    <span class="appointment-block__status-chip">{{ displayStatus(item.status) }}</span>
                    <span v-if="item.has_arrears" class="appointment-block__arrears-chip">欠费</span>
                    <span v-if="item._offline" class="appointment-block__offline-chip" :class="{ 'is-failed': item._offline.failed }">
                      {{ item._offline.label }}
                    </span>
                  </div>
                </div>
                  <div v-if="showAppointmentMeta(item)" class="appointment-block__meta">{{ formatTimeRange(item) }}</div>
                  <div v-if="showAppointmentProject(item)" class="appointment-block__project">{{ item.appointment_purpose || '未填写预约项目' }}</div>
                  <div class="resize-handle resize-handle--bottom" @mousedown.stop.prevent="startResize($event, item, 'bottom')"></div>
                </div>
              </div>
            </div>
          </div>
          <el-empty v-else description="当天暂无可展示的医生排班"></el-empty>
        </el-card>
      </div>
    </div>

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="560px" append-to-body>
      <el-form :model="editItem" label-width="100px">
        <el-form-item label="患者姓名" prop="patient_name" class="patient-name-form-item">
          <div class="patient-suggest-wrap" @click.stop>
            <el-input
              v-model="editItem.patient_name"
              placeholder="请输入患者姓名/手机号/首字母"
              @input="handlePatientNameInput"
              @focus="handlePatientNameFocus"
              @blur="handlePatientNameBlur"
            ></el-input>
            <div v-if="patientSuggestionVisible && filteredPatientSuggestions.length" class="patient-suggestion-panel">
              <div
                v-for="patient in filteredPatientSuggestions"
                :key="patient.id || patient.name"
                class="patient-suggestion-item"
                @mousedown.prevent="selectPatientSuggestion(patient)"
              >
                <div class="patient-suggestion-name">{{ patient.name }}</div>
                <div v-if="patient.phone" class="patient-suggestion-meta">{{ patient.phone }}</div>
              </div>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input
            v-model="editItem.patient_phone"
            readonly
            placeholder="从咨询记录或患者档案带入，仅用于核对"
          ></el-input>
        </el-form-item>
        <el-form-item label="预约日期" prop="appointment_date">
          <el-date-picker v-model="editItem.appointment_date" type="date" placeholder="选择日期" value-format="yyyy-MM-dd" format="yyyy-MM-dd" style="width:100%"></el-date-picker>
        </el-form-item>
        <el-form-item label="预约时间" prop="appointment_time">
          <el-time-picker v-model="editItem.appointment_time" placeholder="选择时间" value-format="HH:mm:ss" format="HH:mm" style="width:100%"></el-time-picker>
        </el-form-item>
        <el-form-item label="时长(分钟)" prop="duration_minutes">
          <el-input-number v-model="editItem.duration_minutes" :min="15" :step="15" controls-position="right" style="width:100%"></el-input-number>
        </el-form-item>
        <el-form-item label="接诊医生" prop="doctor_account_id">
          <el-select v-model="editItem.doctor_account_id" placeholder="请选择医生" style="width:100%">
            <el-option
              v-for="doctor in mergedDoctors"
              :key="doctor.id"
              :label="doctor.name"
              :value="doctor.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <div
          v-if="editDoctorScheduleMeta"
          class="schedule-form-tip"
          :class="editDoctorScheduleMeta.tone"
        >{{ editDoctorScheduleMeta.text }}</div>
        <el-form-item label="预约项目" prop="appointment_purpose">
          <el-input v-model="editItem.appointment_purpose" type="textarea" :rows="2" placeholder="请输入预约项目"></el-input>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="editItem.status" placeholder="请选择状态" style="width:100%">
            <el-option
              v-for="option in editStatusOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            ></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button
          v-if="isEditing && editItem.id"
          type="danger"
          plain
          @click="handleDeleteFromDialog"
        >删除预约</el-button>
        <el-button @click="closeDialog">取消</el-button>
        <el-button type="primary" @click="handleSaveEdit">{{ isEditing ? '保存' : '新增' }}</el-button>
      </span>
    </el-dialog>

    <el-dialog :visible.sync="quickStatusVisible" width="420px" append-to-body title="预约状态">
      <div v-if="quickStatusItem" class="quick-status-box">
        <div class="quick-status-patient">{{ quickStatusItem.patient_name || '未命名患者' }}</div>
        <div class="quick-status-detail">{{ quickStatusItem.appointment_purpose || '未填写预约项目' }}</div>
        <div class="quick-status-detail">{{ formatTimeRange(quickStatusItem) }}</div>
        <div class="quick-status-actions">
          <el-button
            v-for="option in quickStatusOptions"
            :key="`quick-${option.value}`"
            size="mini"
            :type="statusButtonType(option.value)"
            :plain="quickStatusItem.status !== option.value"
            @click="applyQuickStatus(option.value)"
          >{{ option.label }}</el-button>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="quickStatusVisible = false">关闭</el-button>
        <el-button v-if="quickStatusItem && quickStatusItem.patient_id" type="success" plain @click="goPatient360(quickStatusItem)">患者360</el-button>
        <el-button type="primary" plain @click="openEditFromQuickStatus">编辑预约</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios'
import {
  clearPendingAppointmentPatient,
  consumePendingAppointmentPatient,
  readPendingAppointmentPatient
} from '@/utils/appointmentPrefill'
import { fetchCachedResource, saveAppointment } from '@/utils/offline/apiClient'
import { showApiError } from '@/utils/errorMessage'
import {
  SHIFT_CODE_REST,
  buildNonWorkingMasks,
  getWorkingRanges,
  isTimeRangeWithinSchedule,
  normalizeScheduleRecord,
  scheduleDisplayLabel,
  scheduleKey,
  scheduleTimeDescription,
  scheduleTone
} from '@/utils/doctorSchedule'

const SCHEDULE_START_HOUR = 9
const SCHEDULE_END_HOUR = 22
const SLOT_MINUTES = 30
const SLOT_HEIGHT_MIN = 16
const SLOT_HEIGHT_MAX = 24
const VIEWPORT_RESERVED_HEIGHT = 300
const MIN_DURATION_MINUTES = 15
const WAITING_STATUSES = ['待治疗', '已预约', '待就诊']
const EDIT_STATUS_OPTIONS = [
  { label: '未就诊', value: '待治疗' },
  { label: '已预约', value: '已预约' },
  { label: '待就诊', value: '待就诊' },
  { label: '已就诊', value: '已就诊' },
  { label: '已离开', value: '已离开' },
  { label: '已改约', value: '已改约' },
  { label: '已取消', value: '已取消' }
]
const QUICK_STATUS_OPTIONS = [
  { label: '未就诊', value: '待治疗' },
  { label: '已就诊', value: '已就诊' },
  { label: '已离开', value: '已离开' },
  { label: '已改约', value: '已改约' },
  { label: '已取消', value: '已取消' }
]

export default {
  name: 'AppointmentView2',
  data() {
    return {
      selectedDate: '',
      calendarDate: new Date(),
      allAppointments: [],
      appointments: [],
      dialogVisible: false,
      editItem: {
        id: null,
        patient_id: null,
        patient_name: '',
        patient_phone: '',
        appointment_date: '',
        appointment_time: '',
        duration_minutes: 60,
        doctor_account_id: null,
        doctor_name: '',
        appointment_purpose: '',
        status: '待治疗'
      },
      isEditing: false,
      allDoctors: [],
      doctorSchedules: [],
      selectedDoctor: 'ALL',
      allPatients: [],
      patientSuggestionVisible: false,
      patientSuggestionBlurTimer: null,
      pendingPatientPrefill: null,
      viewportHeight: typeof window !== 'undefined' ? window.innerHeight : 900,
      resizeState: null,
      resizeJustEndedAt: 0,
      quickStatusVisible: false,
      quickStatusItem: null,
      editStatusOptions: EDIT_STATUS_OPTIONS,
      quickStatusOptions: QUICK_STATUS_OPTIONS
    }
  },
  computed: {
    dialogTitle() {
      return this.isEditing ? '编辑预约信息' : '新增预约信息'
    },
    pendingPatientAlertDescription() {
      if (!this.pendingPatientPrefill) {
        return '下一次新增预约时会自动带入该患者；切换日期不会清除。'
      }
      const parts = [
        this.pendingPatientPrefill.patient_phone ? `手机号：${this.pendingPatientPrefill.patient_phone}` : '',
        this.pendingPatientPrefill.appointment_purpose ? `预约目的：${this.pendingPatientPrefill.appointment_purpose}` : ''
      ].filter(Boolean)
      const suffix = parts.length ? `（${parts.join('；')}）` : ''
      return `下一次新增预约时会自动带入姓名、手机号和预约目的${suffix}；日期、时间和医生仍需手动确认。`
    },
    scheduleStartMinutes() {
      return SCHEDULE_START_HOUR * 60
    },
    scheduleEndMinutes() {
      return SCHEDULE_END_HOUR * 60
    },
    totalSlots() {
      return (this.scheduleEndMinutes - this.scheduleStartMinutes) / SLOT_MINUTES
    },
    slotHeight() {
      const totalSlots = Math.max(this.totalSlots, 1)
      const availableHeight = Math.max(totalSlots * SLOT_HEIGHT_MIN, this.viewportHeight - VIEWPORT_RESERVED_HEIGHT)
      return Math.max(SLOT_HEIGHT_MIN, Math.min(SLOT_HEIGHT_MAX, Math.floor(availableHeight / totalSlots)))
    },
    scheduleHeight() {
      return this.totalSlots * this.slotHeight
    },
    axisStyle() {
      return {
        height: `${this.scheduleHeight}px`,
        '--slot-height': `${this.slotHeight}px`
      }
    },
    boardStyle() {
      return {
        gridTemplateColumns: `82px repeat(${this.doctorColumns.length || 1}, minmax(240px, 1fr))`,
        minWidth: `${82 + Math.max(this.doctorColumns.length, 1) * 240}px`
      }
    },
    mergedDoctors() {
      const merged = new Map()
      ;(this.allDoctors || []).forEach(item => {
        if (item && item.id && item.name) {
          merged.set(Number(item.id), item)
        }
      })
      ;(this.allAppointments || []).forEach(item => {
        if (item && item.doctor_account_id && item.doctor_name) {
          const id = Number(item.doctor_account_id)
          if (!merged.has(id)) {
            merged.set(id, { id, name: String(item.doctor_name || '').trim() })
          }
        }
      })
      return Array.from(merged.values())
    },
    doctorColumns() {
      if (this.selectedDoctor && this.selectedDoctor !== 'ALL') {
        const doctor = this.currentDoctorById(this.selectedDoctor)
        return doctor ? [doctor] : []
      }
      return this.mergedDoctors
    },
    appointmentCountMap() {
      const map = {}
      ;(this.allAppointments || []).forEach(item => {
        const day = String((item && item.appointment_date) || '').trim()
        const doctorId = Number(item && item.doctor_account_id)
        if (!day) return
        if (this.selectedDoctor !== 'ALL' && Number(this.selectedDoctor) !== doctorId) {
          return
        }
        map[day] = (map[day] || 0) + 1
      })
      return map
    },
    filteredPatientSuggestions() {
      const keyword = String(this.editItem.patient_name || '').trim().toLowerCase()
      const patients = Array.isArray(this.allPatients) ? this.allPatients : []
      if (!keyword) {
        return patients.slice(0, 8)
      }
      return patients
        .filter(item => {
          const name = String(item.name || '').toLowerCase()
          const phone = String(item.phone || '').toLowerCase()
          return name.includes(keyword) || phone.includes(keyword)
        })
        .slice(0, 8)
    },
    doctorScheduleMap() {
      const map = {}
      ;(this.doctorSchedules || []).forEach(item => {
        if (!item) return
        map[scheduleKey(item.doctor_name, item.schedule_date)] = item
      })
      return map
    },
    appointmentLayoutMap() {
      const map = {}
      const grouped = {}
      ;(this.appointments || []).forEach(item => {
        if (!item || item.id === null || item.id === undefined) return
        const doctorId = String(item.doctor_account_id || '')
        if (!grouped[doctorId]) {
          grouped[doctorId] = []
        }
        grouped[doctorId].push(item)
      })

      Object.keys(grouped).forEach(doctorId => {
        const items = grouped[doctorId]
          .slice()
          .sort((left, right) => {
            const startDiff = this.startMinutesForItem(left) - this.startMinutesForItem(right)
            if (startDiff !== 0) return startDiff
            return this.durationForItem(right) - this.durationForItem(left)
          })

        let currentGroup = []
        let currentGroupEnd = -1

        const flushGroup = () => {
          if (!currentGroup.length) return
          const columnEndMinutes = []
          const assignments = []
          let maxColumns = 1

          currentGroup.forEach(entry => {
            let columnIndex = columnEndMinutes.findIndex(endMinutes => endMinutes <= entry.startMinutes)
            if (columnIndex < 0) {
              columnIndex = columnEndMinutes.length
              columnEndMinutes.push(entry.endMinutes)
            } else {
              columnEndMinutes[columnIndex] = entry.endMinutes
            }
            maxColumns = Math.max(maxColumns, columnEndMinutes.length)
            assignments.push({
              item: entry.item,
              columnIndex
            })
          })

          assignments.forEach(assignment => {
            map[String(assignment.item.id)] = {
              columnIndex: assignment.columnIndex,
              columnCount: maxColumns
            }
          })

          currentGroup = []
          currentGroupEnd = -1
        }

        items.forEach(item => {
          const startMinutes = this.startMinutesForItem(item)
          const endMinutes = startMinutes + this.durationForItem(item)
          const entry = { item, startMinutes, endMinutes }
          if (!currentGroup.length) {
            currentGroup.push(entry)
            currentGroupEnd = endMinutes
            return
          }
          if (startMinutes < currentGroupEnd) {
            currentGroup.push(entry)
            currentGroupEnd = Math.max(currentGroupEnd, endMinutes)
            return
          }
          flushGroup()
          currentGroup.push(entry)
          currentGroupEnd = endMinutes
        })

        flushGroup()
      })

      return map
    },
    editDoctorScheduleMeta() {
      const doctor = this.currentDoctorById(this.editItem.doctor_account_id)
      const doctorName = doctor && doctor.name ? doctor.name : this.editItem.doctor_name
      const appointmentDate = this.editItem.appointment_date
      if (!doctorName || !appointmentDate) {
        return null
      }
      const scheduleRecord = this.resolveDoctorScheduleByName(doctorName, appointmentDate)
      if (!scheduleRecord) {
        return {
          tone: 'is-empty',
          text: '当前日期未设置排班，请先到排班管理中设置班次。'
        }
      }
      const label = scheduleDisplayLabel(scheduleRecord)
      const timeText = scheduleTimeDescription(scheduleRecord)
      const tone = scheduleTone(scheduleRecord.shiftType || label)
      if (label === SHIFT_CODE_REST) {
        return {
          tone: 'is-rest',
          text: `当前排班：${label}，当天不能新增预约。`
        }
      }
      return {
        tone: `is-${tone}`,
        text: `当前排班：${label}，工作时间 ${timeText}。`
      }
    },
    timeLabels() {
      const labels = []
      for (let hour = SCHEDULE_START_HOUR; hour <= SCHEDULE_END_HOUR; hour++) {
        labels.push({
          key: hour,
          label: `${this.pad2(hour)}:00`,
          top: ((hour * 60) - this.scheduleStartMinutes) / SLOT_MINUTES * this.slotHeight
        })
      }
      return labels
    },
    hourMarkers() {
      return this.timeLabels.filter(item => item.top < this.scheduleHeight)
    }
  },
  mounted() {
    window.addEventListener('resize', this.updateViewportHeight)
    this.updateViewportHeight()
    this.selectedDate = this.formatDate(new Date())
    this.syncCalendarDateFromSelectedDate()
    this.restorePendingPatientPrefill()
    this.loadDoctors()
    this.loadDoctorSchedules()
    this.loadAppointments()
    this.loadPatients()
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.updateViewportHeight)
    this.removeResizeListeners()
    if (this.patientSuggestionBlurTimer) {
      clearTimeout(this.patientSuggestionBlurTimer)
      this.patientSuggestionBlurTimer = null
    }
  },
  methods: {
    updateViewportHeight() {
      if (typeof window === 'undefined') return
      this.viewportHeight = window.innerHeight || 900
    },
    pad2(value) {
      return String(value).padStart(2, '0')
    },
    formatDate(date) {
      const d = new Date(date)
      if (Number.isNaN(d.getTime())) return ''
      const y = d.getFullYear()
      const m = this.pad2(d.getMonth() + 1)
      const day = this.pad2(d.getDate())
      return `${y}-${m}-${day}`
    },
    parseDateValue(value) {
      const text = String(value || '').trim()
      const dateMatched = text.match(/^(\d{4})-(\d{2})-(\d{2})$/)
      if (dateMatched) {
        return new Date(Number(dateMatched[1]), Number(dateMatched[2]) - 1, Number(dateMatched[3]))
      }
      const dateTimeMatched = text.match(/^(\d{4})-(\d{2})-(\d{2})[ T](\d{2}):(\d{2})(?::(\d{2}))?$/)
      if (dateTimeMatched) {
        return new Date(
          Number(dateTimeMatched[1]),
          Number(dateTimeMatched[2]) - 1,
          Number(dateTimeMatched[3]),
          Number(dateTimeMatched[4]),
          Number(dateTimeMatched[5]),
          Number(dateTimeMatched[6] || 0)
        )
      }
      const parsed = new Date(value)
      if (Number.isNaN(parsed.getTime())) {
        return null
      }
      return parsed
    },
    syncCalendarDateFromSelectedDate() {
      const parsed = this.parseDateValue(this.selectedDate)
      if (parsed) {
        this.calendarDate = parsed
      }
    },
    restorePendingPatientPrefill() {
      this.pendingPatientPrefill = readPendingAppointmentPatient()
    },
    clearPendingPatientPrefill() {
      clearPendingAppointmentPatient()
      this.pendingPatientPrefill = null
    },
    consumePendingPatientPrefill() {
      const payload = consumePendingAppointmentPatient()
      this.pendingPatientPrefill = null
      return payload
    },
    handleSelectedDateChange() {
      this.syncCalendarDateFromSelectedDate()
      this.applySelectedDateAppointments()
    },
    handleCalendarInput(value) {
      const parsed = this.parseDateValue(value)
      if (!parsed) return
      this.calendarDate = parsed
      this.selectedDate = this.formatDate(parsed)
      this.applySelectedDateAppointments()
    },
    handleCalendarDatePick(day) {
      const parsed = this.parseDateValue(day)
      if (!parsed) return
      this.calendarDate = parsed
      this.selectedDate = this.formatDate(parsed)
      this.applySelectedDateAppointments()
    },
    displayCalendarDay(day) {
      const text = String(day || '')
      return text.length >= 10 ? String(Number(text.slice(8, 10))) : text
    },
    appointmentCountForDate(day) {
      return this.appointmentCountMap[String(day || '').trim()] || 0
    },
    normalizeDoctor(item) {
      if (!item || !item.id || !item.name) return null
      return {
        id: Number(item.id),
        name: String(item.name).trim()
      }
    },
    normalizeDurationMinutes(value) {
      const duration = Number(value || 0)
      return Number.isFinite(duration) && duration > 0 ? duration : 60
    },
    normalizeAppointment(item) {
      return {
        ...item,
        appointment_date: item && item.appointment_date ? this.formatDate(item.appointment_date) : '',
        patient_id: item && item.patient_id ? Number(item.patient_id) : null,
        doctor_account_id: item && item.doctor_account_id ? Number(item.doctor_account_id) : null,
        duration_minutes: this.normalizeDurationMinutes(item && item.duration_minutes),
        status: String((item && item.status) || '待治疗').trim() || '待治疗'
      }
    },
    normalizeTimeValue(value) {
      const text = String(value || '').trim()
      if (!text) return ''
      if (text.length >= 5) return text.slice(0, 5)
      return text
    },
    timeToMinutes(value) {
      const text = this.normalizeTimeValue(value)
      const parts = text.split(':')
      const hour = Number(parts[0])
      const minute = Number(parts[1] || 0)
      if (!Number.isFinite(hour) || !Number.isFinite(minute)) {
        return this.scheduleStartMinutes
      }
      return (hour * 60) + minute
    },
    formatMinutes(minutes) {
      const safe = Math.max(this.scheduleStartMinutes, Math.min(this.scheduleEndMinutes, Number(minutes || 0)))
      const hour = Math.floor(safe / 60)
      const minute = safe % 60
      return `${this.pad2(hour)}:${this.pad2(minute)}`
    },
    currentDoctorById(id) {
      return (this.mergedDoctors || []).find(item => Number(item.id) === Number(id)) || null
    },
    resolveDoctorScheduleByName(doctorName, date = this.selectedDate) {
      const key = scheduleKey(doctorName, date)
      return this.doctorScheduleMap[key] || null
    },
    doctorScheduleFor(doctor, date = this.selectedDate) {
      if (!doctor || !doctor.name || !date) return null
      return this.resolveDoctorScheduleByName(doctor.name, date)
    },
    doctorHeadShiftLabel(doctor) {
      const scheduleRecord = this.doctorScheduleFor(doctor)
      return scheduleDisplayLabel(scheduleRecord)
    },
    doctorHeadShiftClass(doctor) {
      const scheduleRecord = this.doctorScheduleFor(doctor)
      const tone = scheduleTone(scheduleRecord ? (scheduleRecord.shiftType || scheduleDisplayLabel(scheduleRecord)) : '')
      return `is-${tone}`
    },
    nonWorkingMasksForDoctor(doctor) {
      const scheduleRecord = this.doctorScheduleFor(doctor)
      return buildNonWorkingMasks(scheduleRecord, this.scheduleStartMinutes, this.scheduleEndMinutes)
        .map((mask, index) => ({
          ...mask,
          key: `${mask.type}-${index}`,
          showLabel: (mask.endMinutes - mask.startMinutes) >= (SLOT_MINUTES * 2)
        }))
    },
    nonWorkingMaskStyle(mask) {
      const top = ((mask.startMinutes - this.scheduleStartMinutes) / SLOT_MINUTES) * this.slotHeight
      const height = ((mask.endMinutes - mask.startMinutes) / SLOT_MINUTES) * this.slotHeight
      return {
        top: `${top}px`,
        height: `${Math.max(height, 0)}px`
      }
    },
    nonWorkingMaskClass(mask) {
      if (mask.type === 'rest') return 'is-rest'
      if (mask.type === 'empty') return 'is-empty'
      return 'is-off'
    },
    primaryWorkingRange(record) {
      const ranges = getWorkingRanges(record)
      return ranges.length ? ranges[0] : null
    },
    defaultStartMinutesForDoctor(doctor, date = this.selectedDate) {
      const range = this.primaryWorkingRange(this.doctorScheduleFor(doctor, date))
      return range ? range.startMinutes : this.scheduleStartMinutes
    },
    defaultDurationForDoctor(doctor, date, minutes) {
      const range = this.primaryWorkingRange(this.doctorScheduleFor(doctor, date))
      if (!range) return 60
      const remaining = Math.max(range.endMinutes - Number(minutes || 0), MIN_DURATION_MINUTES)
      const stepped = Math.floor(remaining / MIN_DURATION_MINUTES) * MIN_DURATION_MINUTES
      return Math.max(MIN_DURATION_MINUTES, Math.min(60, stepped))
    },
    scheduleAvailabilityMessage(doctorName, date, startValue, durationMinutes) {
      const normalizedDoctorName = String(doctorName || '').trim()
      const normalizedDate = String(date || '').trim()
      if (!normalizedDoctorName || !normalizedDate) {
        return ''
      }
      const scheduleRecord = this.resolveDoctorScheduleByName(normalizedDoctorName, normalizedDate)
      if (!scheduleRecord) {
        return `${normalizedDoctorName}在${normalizedDate}未设置排班，请先到排班管理页面设置班次。`
      }
      if (scheduleDisplayLabel(scheduleRecord) === SHIFT_CODE_REST) {
        return `${normalizedDoctorName}在${normalizedDate}休息，不能预约。`
      }
      const startMinutes = Number.isFinite(Number(startValue)) ? Number(startValue) : this.timeToMinutes(startValue)
      const safeDuration = this.normalizeDurationMinutes(durationMinutes)
      if (!isTimeRangeWithinSchedule(scheduleRecord, startMinutes, safeDuration)) {
        return `${normalizedDoctorName}在${normalizedDate}的工作时间为${scheduleTimeDescription(scheduleRecord)}，当前预约超出工作时间。`
      }
      return ''
    },
    waitingStatus(status) {
      return WAITING_STATUSES.includes(String(status || '').trim())
    },
    displayStatus(status) {
      if (this.waitingStatus(status)) return '未就诊'
      return String(status || '未就诊').trim()
    },
    appointmentsForDoctor(doctorId) {
      return (this.appointments || [])
        .filter(item => Number(item.doctor_account_id) === Number(doctorId))
        .sort((a, b) => this.timeToMinutes(a.appointment_time) - this.timeToMinutes(b.appointment_time))
    },
    startMinutesForItem(item) {
      if (this.resizeState && this.resizeState.id === item.id) {
        return this.resizeState.previewStartMinutes
      }
      const startMinutes = this.timeToMinutes(item.appointment_time)
      return Math.max(this.scheduleStartMinutes, Math.min(startMinutes, this.scheduleEndMinutes - MIN_DURATION_MINUTES))
    },
    durationForItem(item) {
      if (this.resizeState && this.resizeState.id === item.id) {
        return this.resizeState.previewDurationMinutes
      }
      return this.normalizeDurationMinutes(item.duration_minutes)
    },
    blockHeightForItem(item) {
      const duration = this.durationForItem(item)
      return Math.max((duration / SLOT_MINUTES) * this.slotHeight, this.slotHeight)
    },
    appointmentLayoutMeta(item) {
      return this.appointmentLayoutMap[String(item && item.id)] || {
        columnIndex: 0,
        columnCount: 1
      }
    },
    appointmentBlockStyle(item) {
      const startMinutes = this.startMinutesForItem(item)
      const top = ((startMinutes - this.scheduleStartMinutes) / SLOT_MINUTES) * this.slotHeight
      const height = this.blockHeightForItem(item)
      const layoutMeta = this.appointmentLayoutMeta(item)
      const columnCount = Math.max(Number(layoutMeta.columnCount || 1), 1)
      const columnIndex = Math.max(Number(layoutMeta.columnIndex || 0), 0)
      const widthPercent = 100 / columnCount
      return {
        top: `${top}px`,
        height: `${height}px`,
        left: `calc(${(widthPercent * columnIndex).toFixed(6)}% + 6px)`,
        width: `calc(${widthPercent.toFixed(6)}% - 12px)`
      }
    },
    appointmentBlockClass(item) {
      const status = String(item.status || '').trim()
      const classes = []
      if (status === '已取消') classes.push('is-cancelled')
      else if (status === '已就诊' || status === '已治疗' || status === '已完成') classes.push('is-visited')
      else if (status === '已离开') classes.push('is-left')
      else if (status === '已改约') classes.push('is-rescheduled')
      else classes.push('is-waiting')
      if (this.appointmentLayoutMeta(item).columnCount > 1) {
        classes.push('is-overlap')
      }
      return classes
    },
    formatTimeRange(item) {
      const startMinutes = this.startMinutesForItem(item)
      const endMinutes = Math.min(this.scheduleEndMinutes, startMinutes + this.durationForItem(item))
      return `${this.formatMinutes(startMinutes)} - ${this.formatMinutes(endMinutes)}`
    },
    showAppointmentBadges(item) {
      return this.blockHeightForItem(item) >= 32
    },
    showAppointmentMeta(item) {
      return this.blockHeightForItem(item) >= 32
    },
    showAppointmentProject(item) {
      return this.blockHeightForItem(item) >= 46
    },
    appointmentTooltip(item) {
      return [
        item.patient_name || '未命名患者',
        this.displayStatus(item.status),
        this.formatTimeRange(item),
        item.appointment_purpose || ''
      ].filter(Boolean).join('\n')
    },
    applySelectedDateAppointments() {
      this.appointments = (this.allAppointments || [])
        .filter(item => item && item.appointment_date === this.selectedDate)
        .map(this.normalizeAppointment)
      if (this.selectedDoctor !== 'ALL' && !this.currentDoctorById(this.selectedDoctor)) {
        this.selectedDoctor = 'ALL'
      }
    },
    formatArrears(value) {
      const amount = Number(value || 0)
      return Number.isFinite(amount) ? amount.toFixed(2) : '0.00'
    },
    statusButtonType(status) {
      if (status === '已取消') return 'danger'
      if (status === '已就诊') return 'success'
      if (status === '已离开') return 'info'
      if (status === '已改约') return 'warning'
      return 'primary'
    },
    refreshBoard() {
      this.loadDoctorSchedules()
      this.loadAppointments()
    },
    loadAppointments() {
      if (!this.selectedDoctor || !String(this.selectedDoctor).trim()) {
        this.selectedDoctor = 'ALL'
      }
      fetchCachedResource({
        cacheKey: 'page:appointments:schedule-board',
        scope: 'appointmentsBoard',
        url: '/appointments/scheduleEntries',
        loader: () => axios.get('/appointments/scheduleEntries'),
        notifier: message => this.$message.warning(message)
      }).then(result => {
        const payload = result && result.data ? result.data : []
        const list = Array.isArray(payload) ? payload : []
        this.allAppointments = list.map(this.normalizeAppointment)
        this.applySelectedDateAppointments()
      }).catch(error => {
        console.error('Error fetching appointments:', error)
        showApiError(this, '加载预约数据', error)
      })
    },
    loadDoctorSchedules() {
      fetchCachedResource({
        cacheKey: 'ref:doctor-schedules',
        scope: '',
        url: '/doctors/scheduleEntries',
        loader: () => axios.get('/doctors/scheduleEntries')
      }).then(result => {
        const list = Array.isArray(result && result.data) ? result.data : []
        this.doctorSchedules = list
          .map(normalizeScheduleRecord)
          .filter(Boolean)
          .sort((a, b) => Number(a.id || 0) - Number(b.id || 0))
      }).catch(error => {
        console.error('Error fetching doctor schedules:', error)
        this.doctorSchedules = []
      })
    },
    loadDoctors() {
      fetchCachedResource({
        cacheKey: 'ref:doctors-active',
        scope: '',
        url: '/accounts/doctors/active',
        loader: () => axios.get('/accounts/doctors/active')
      }).then(result => {
        const list = Array.isArray(result && result.data) ? result.data : []
        this.allDoctors = list.map(this.normalizeDoctor).filter(Boolean)
      }).catch(() => {
        this.allDoctors = []
      })
    },
    loadPatients(keyword = '') {
      const params = {
        keyword: String(keyword || '').trim(),
        page: 1,
        size: 1000
      }
      fetchCachedResource({
        cacheKey: 'ref:appointment-patients',
        scope: '',
        url: '/patients/search',
        params,
        loader: () => axios.get('/patients/search', { params })
      }).then(result => {
        const data = result && result.data ? result.data : {}
        const list = Array.isArray(data.list) ? data.list : (Array.isArray(data) ? data : [])
        this.allPatients = list
          .map(item => ({
            id: item.id,
            name: item.name || '',
            phone: item.phone || ''
          }))
          .filter(item => item.name)
        this.hydrateEditPatientPhone()
      }).catch(() => {
        this.allPatients = []
      })
    },
    handlePatientNameInput() {
      const hadLinkedPatient = !!this.editItem.patient_id
      this.editItem.patient_id = null
      if (hadLinkedPatient) {
        this.editItem.patient_phone = ''
      }
      this.patientSuggestionVisible = true
      this.loadPatients(this.editItem.patient_name)
    },
    handlePatientNameFocus() {
      if (this.patientSuggestionBlurTimer) {
        clearTimeout(this.patientSuggestionBlurTimer)
        this.patientSuggestionBlurTimer = null
      }
      this.patientSuggestionVisible = true
      this.loadPatients(this.editItem.patient_name)
    },
    handlePatientNameBlur() {
      this.patientSuggestionBlurTimer = setTimeout(() => {
        this.patientSuggestionVisible = false
      }, 120)
    },
    selectPatientSuggestion(patient) {
      this.editItem.patient_id = patient && patient.id ? patient.id : null
      this.editItem.patient_name = patient && patient.name ? patient.name : ''
      this.editItem.patient_phone = patient && patient.phone ? patient.phone : ''
      this.patientSuggestionVisible = false
    },
    handleDoctorFilterChange() {
      if (!this.selectedDoctor || !String(this.selectedDoctor).trim()) {
        this.selectedDoctor = 'ALL'
      }
    },
    showAddDialogByHeader() {
      const doctor = this.selectedDoctor !== 'ALL' ? this.currentDoctorById(this.selectedDoctor) : null
      const startMinutes = doctor ? this.defaultStartMinutesForDoctor(doctor, this.selectedDate) : this.scheduleStartMinutes
      this.openNewAppointmentAtMinutes(startMinutes, doctor, {
        usePendingPatient: true,
        preserveScheduleContext: false
      })
    },
    openNewAppointmentAtMinutes(minutes, doctor = null, options = {}) {
      const safeDoctor = doctor && doctor.id ? doctor : (this.selectedDoctor !== 'ALL' ? this.currentDoctorById(this.selectedDoctor) : null)
      const numericMinutes = Number(minutes)
      const doctorStartMinutes = safeDoctor ? this.defaultStartMinutesForDoctor(safeDoctor, this.selectedDate) : this.scheduleStartMinutes
      const safeMinutes = safeDoctor
        ? Math.max(doctorStartMinutes, Number.isFinite(numericMinutes) ? numericMinutes : doctorStartMinutes)
        : (Number.isFinite(numericMinutes) ? numericMinutes : this.scheduleStartMinutes)
      const defaultDuration = safeDoctor ? this.defaultDurationForDoctor(safeDoctor, this.selectedDate, safeMinutes) : 60
      const pendingPatient = options.usePendingPatient ? this.consumePendingPatientPrefill() : null
      const shouldPresetScheduleContext = !pendingPatient || options.preserveScheduleContext !== false
      this.isEditing = false
      this.editItem = {
        id: null,
        patient_id: pendingPatient ? pendingPatient.patient_id : null,
        patient_name: pendingPatient ? pendingPatient.patient_name : '',
        patient_phone: pendingPatient ? pendingPatient.patient_phone || '' : '',
        appointment_date: shouldPresetScheduleContext ? this.selectedDate : '',
        appointment_time: shouldPresetScheduleContext ? `${this.formatMinutes(safeMinutes)}:00` : '',
        duration_minutes: shouldPresetScheduleContext ? defaultDuration : 60,
        doctor_account_id: shouldPresetScheduleContext && safeDoctor && safeDoctor.id ? Number(safeDoctor.id) : null,
        doctor_name: shouldPresetScheduleContext && safeDoctor && safeDoctor.name ? safeDoctor.name : '',
        appointment_purpose: pendingPatient ? pendingPatient.appointment_purpose || '' : '',
        status: '待治疗'
      }
      this.dialogVisible = true
    },
    handleLaneClick(event, doctor) {
      if (Date.now() - this.resizeJustEndedAt < 150) {
        return
      }
      const rect = event.currentTarget.getBoundingClientRect()
      const offsetY = Math.max(0, Math.min(rect.height, event.clientY - rect.top))
      const slotIndex = Math.max(0, Math.min(this.totalSlots - 1, Math.floor(offsetY / this.slotHeight)))
      const minutes = this.scheduleStartMinutes + (slotIndex * SLOT_MINUTES)
      const scheduleMessage = this.scheduleAvailabilityMessage(doctor && doctor.name, this.selectedDate, minutes, MIN_DURATION_MINUTES)
      if (scheduleMessage) {
        this.$message.warning(scheduleMessage)
        return
      }
      this.openNewAppointmentAtMinutes(minutes, doctor, { usePendingPatient: true })
    },
    handleSlotClick(item) {
      this.editItem = Object.assign({
        id: null,
        patient_id: null,
        patient_name: '',
        patient_phone: '',
        appointment_date: this.selectedDate,
        appointment_time: `${this.formatMinutes(this.scheduleStartMinutes)}:00`,
        duration_minutes: 60,
        doctor_account_id: null,
        doctor_name: '',
        appointment_purpose: '',
        status: '待治疗'
      }, this.normalizeAppointment(item))
      this.hydrateEditPatientPhone()
      this.isEditing = true
      this.dialogVisible = true
    },
    resolvePatientPhone(patientId, patientName) {
      const matchedById = (this.allPatients || []).find(item => Number(item.id) === Number(patientId) && Number(patientId) > 0)
      if (matchedById && matchedById.phone) {
        return matchedById.phone
      }
      const normalizedName = String(patientName || '').trim()
      if (!normalizedName) {
        return ''
      }
      const matchedByName = (this.allPatients || []).find(item => String(item.name || '').trim() === normalizedName)
      return matchedByName && matchedByName.phone ? matchedByName.phone : ''
    },
    hydrateEditPatientPhone() {
      if (!this.editItem) return
      const resolvedPhone = this.resolvePatientPhone(this.editItem.patient_id, this.editItem.patient_name)
      if (resolvedPhone || !this.editItem.patient_id) {
        this.editItem.patient_phone = resolvedPhone || this.editItem.patient_phone || ''
      }
    },
    openQuickStatus(item) {
      if (Date.now() - this.resizeJustEndedAt < 150) {
        return
      }
      this.quickStatusItem = Object.assign({}, this.normalizeAppointment(item))
      this.quickStatusVisible = true
    },
    applyQuickStatus(status) {
      if (!this.quickStatusItem) return
      const payload = {
        ...this.quickStatusItem,
        status
      }
      saveAppointment(payload, {
        isEdit: true,
        notifier: message => this.$message.success(message)
      }).then(result => {
        if (!result.offline) {
          this.$message.success('预约状态已更新')
        }
        this.quickStatusVisible = false
        this.quickStatusItem = null
        this.loadAppointments()
      }).catch(error => {
        this.$message.error((error && error.message) || '状态更新失败')
      })
    },
    openEditFromQuickStatus() {
      if (!this.quickStatusItem) return
      const target = (this.appointments || []).find(item => Number(item.id) === Number(this.quickStatusItem.id)) || this.quickStatusItem
      this.quickStatusVisible = false
      this.quickStatusItem = null
      this.handleSlotClick(target)
    },
    goPatient360(source = null) {
      const current = source || this.editItem
      if (!current || !current.patient_id) {
        this.$message.warning('当前预约缺少患者ID，无法进入患者360')
        return
      }
      this.closeDialog()
      this.quickStatusVisible = false
      this.quickStatusItem = null
      this.$router.push({ path: '/Patient360', query: { id: current.patient_id } })
    },
    closeDialog() {
      this.dialogVisible = false
      this.isEditing = false
      this.patientSuggestionVisible = false
      if (this.patientSuggestionBlurTimer) {
        clearTimeout(this.patientSuggestionBlurTimer)
        this.patientSuggestionBlurTimer = null
      }
    },
    confirmDeleteAppointment(id, options = {}) {
      const appointmentId = Number(id)
      if (!appointmentId) {
        this.$message.warning('当前预约缺少有效ID，无法删除')
        return Promise.resolve(false)
      }
      return this.$confirm('删除后无法恢复，确认删除这条预约吗？', '删除预约', {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        return axios.delete(`/appointments/delete/${appointmentId}`).then(() => {
          this.$message.success('删除成功')
          if (options.closeDialog) {
            this.closeDialog()
          }
          if (options.closeQuickStatus) {
            this.quickStatusVisible = false
            this.quickStatusItem = null
          }
          this.loadAppointments()
          return true
        })
      }).catch(error => {
        if (error === 'cancel' || error === 'close') {
          return false
        }
        console.error('Error deleting appointment:', error)
        this.$message.error((error.response && error.response.data && error.response.data.msg) || '删除失败')
        return false
      })
    },
    handleDeleteFromDialog() {
      if (!this.isEditing || !this.editItem || !this.editItem.id) {
        this.$message.warning('当前没有可删除的预约')
        return
      }
      this.confirmDeleteAppointment(this.editItem.id, { closeDialog: true })
    },
    validateAppointmentForm() {
      if (!this.editItem.patient_name || !String(this.editItem.patient_name).trim()) {
        return '患者姓名必填'
      }
      if (!this.editItem.appointment_date || !String(this.editItem.appointment_date).trim()) {
        return '预约日期必填'
      }
      if (!this.editItem.appointment_time || !String(this.editItem.appointment_time).trim()) {
        return '预约时间必填'
      }
      if (!this.editItem.duration_minutes || Number(this.editItem.duration_minutes) <= 0) {
        return '预约时长必填'
      }
      if (!this.editItem.doctor_account_id) {
        return '接诊医生必填'
      }
      if (!this.editItem.appointment_purpose || !String(this.editItem.appointment_purpose).trim()) {
        return '预约项目必填'
      }
      const doctor = this.currentDoctorById(this.editItem.doctor_account_id)
      this.editItem.doctor_name = doctor && doctor.name ? doctor.name : (this.editItem.doctor_name || '')
      if (!this.editItem.status || !String(this.editItem.status).trim()) {
        this.editItem.status = '待治疗'
      }
      const scheduleMessage = this.scheduleAvailabilityMessage(
        this.editItem.doctor_name,
        this.editItem.appointment_date,
        this.editItem.appointment_time,
        this.editItem.duration_minutes
      )
      if (scheduleMessage) {
        return scheduleMessage
      }
      return ''
    },
    buildAppointmentPayload() {
      return {
        id: this.editItem.id,
        patient_id: this.editItem.patient_id || null,
        patient_name: String(this.editItem.patient_name || '').trim(),
        appointment_date: this.editItem.appointment_date,
        appointment_time: this.editItem.appointment_time,
        duration_minutes: this.editItem.duration_minutes,
        doctor_account_id: this.editItem.doctor_account_id || null,
        doctor_name: String(this.editItem.doctor_name || '').trim(),
        appointment_purpose: String(this.editItem.appointment_purpose || '').trim(),
        cancel_reason: String(this.editItem.cancel_reason || '').trim(),
        status: String(this.editItem.status || '').trim()
      }
    },
    handleAdd() {
      const validationMessage = this.validateAppointmentForm()
      if (validationMessage) {
        this.$message.warning(validationMessage)
        return
      }
      saveAppointment(this.buildAppointmentPayload(), {
        notifier: message => this.$message.success(message)
      }).then(result => {
        if (!result.offline) {
          this.$message.success('新增成功')
        }
        this.closeDialog()
        this.loadAppointments()
      }).catch(error => {
        console.error('Error adding appointment:', error)
        this.$message.error((error && error.message) || '新增失败')
      })
    },
    handleSaveEdit() {
      const validationMessage = this.validateAppointmentForm()
      if (validationMessage) {
        this.$message.warning(validationMessage)
        return
      }
      saveAppointment(this.buildAppointmentPayload(), {
        isEdit: this.isEditing,
        notifier: message => this.$message.success(message)
      }).then(result => {
        if (!result.offline) {
          this.$message.success(this.isEditing ? '编辑成功' : '新增成功')
        }
        this.closeDialog()
        this.loadAppointments()
      }).catch(error => {
        console.error('Error saving appointment:', error)
        this.$message.error((error && error.message) || (this.isEditing ? '编辑失败' : '新增失败'))
      })
    },
    startResize(event, item, handle) {
      const normalizedItem = this.normalizeAppointment(item)
      const scheduleRecord = this.resolveDoctorScheduleByName(normalizedItem.doctor_name, normalizedItem.appointment_date)
      const workingRange = this.primaryWorkingRange(scheduleRecord)
      if (!workingRange) {
        this.$message.warning('当前日期未设置有效排班，暂不支持拖动调整预约时间')
        return
      }
      const startMinutes = this.startMinutesForItem(normalizedItem)
      const durationMinutes = this.durationForItem(normalizedItem)
      this.resizeState = {
        id: normalizedItem.id,
        handle,
        startY: event.clientY,
        itemSnapshot: normalizedItem,
        workingRange,
        originalStartMinutes: startMinutes,
        originalDurationMinutes: durationMinutes,
        previewStartMinutes: startMinutes,
        previewDurationMinutes: durationMinutes
      }
      this.addResizeListeners()
    },
    addResizeListeners() {
      window.addEventListener('mousemove', this.handleResizeMove)
      window.addEventListener('mouseup', this.handleResizeEnd)
    },
    removeResizeListeners() {
      window.removeEventListener('mousemove', this.handleResizeMove)
      window.removeEventListener('mouseup', this.handleResizeEnd)
    },
    handleResizeMove(event) {
      if (!this.resizeState) return
      const deltaSlots = Math.round((event.clientY - this.resizeState.startY) / this.slotHeight)
      const deltaMinutes = deltaSlots * SLOT_MINUTES
      const originalStartMinutes = this.resizeState.originalStartMinutes
      const originalEndMinutes = originalStartMinutes + this.resizeState.originalDurationMinutes
      const minStartMinutes = this.resizeState.workingRange
        ? Math.max(this.scheduleStartMinutes, this.resizeState.workingRange.startMinutes)
        : this.scheduleStartMinutes
      const maxEndMinutes = this.resizeState.workingRange
        ? Math.min(this.scheduleEndMinutes, this.resizeState.workingRange.endMinutes)
        : this.scheduleEndMinutes
      let previewStartMinutes = originalStartMinutes
      let previewDurationMinutes = this.resizeState.originalDurationMinutes

      if (this.resizeState.handle === 'top') {
        previewStartMinutes = Math.max(minStartMinutes, Math.min(originalEndMinutes - MIN_DURATION_MINUTES, originalStartMinutes + deltaMinutes))
        previewDurationMinutes = originalEndMinutes - previewStartMinutes
      } else {
        const previewEndMinutes = Math.max(originalStartMinutes + MIN_DURATION_MINUTES, Math.min(maxEndMinutes, originalEndMinutes + deltaMinutes))
        previewDurationMinutes = previewEndMinutes - originalStartMinutes
      }

      this.resizeState = {
        ...this.resizeState,
        previewStartMinutes,
        previewDurationMinutes
      }
    },
    handleResizeEnd() {
      if (!this.resizeState) return
      const state = this.resizeState
      this.resizeState = null
      this.removeResizeListeners()
      this.resizeJustEndedAt = Date.now()

      if (state.previewStartMinutes === state.originalStartMinutes && state.previewDurationMinutes === state.originalDurationMinutes) {
        return
      }

      const payload = {
        ...state.itemSnapshot,
        appointment_time: `${this.formatMinutes(state.previewStartMinutes)}:00`,
        duration_minutes: state.previewDurationMinutes
      }
      saveAppointment(payload, {
        isEdit: true,
        notifier: message => this.$message.success(message)
      }).then(result => {
        if (!result.offline) {
          this.$message.success('预约时间已更新')
        }
        this.loadAppointments()
      }).catch(error => {
        this.$message.error((error && error.message) || '调整预约失败')
        this.loadAppointments()
      })
    }
  }
}
</script>

<style scoped>
.page-wrap {
  display: flex;
  flex-direction: column;
  gap: 12px;
  height: 100%;
  min-height: 0;
}

.schedule-layout {
  display: grid;
  grid-template-columns: 320px minmax(0, 1fr);
  gap: 12px;
  flex: 1;
  min-height: 0;
}

.calendar-card,
.schedule-main {
  min-height: 0;
}

.schedule-main {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 0;
}

.page-title-bar {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
}

.page-kicker {
  color: #64748b;
  font-size: 13px;
  margin-bottom: 4px;
}

.page-title-bar h2 {
  margin: 0;
  color: #0f172a;
  font-size: 22px;
}

.page-desc {
  color: #64748b;
  font-size: 12px;
}

.filter-card,
.schedule-card,
.calendar-card {
  border-radius: 18px;
}

/deep/ .filter-card .el-card__body {
  padding: 12px 16px;
}

/deep/ .schedule-card .el-card__body {
  padding: 14px 16px 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

/deep/ .calendar-card .el-card__body {
  padding: 14px 16px 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.schedule-card {
  flex: 1;
  min-height: 0;
}

.calendar-card {
  overflow: hidden;
}

.calendar-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.calendar-title {
  font-size: 15px;
  font-weight: 700;
  color: #0f172a;
}

.calendar-subtitle {
  margin-top: 4px;
  color: #94a3b8;
  font-size: 12px;
  line-height: 1.5;
}

.calendar-date-cell {
  min-height: 50px;
  padding: 6px 8px;
  border-radius: 10px;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: space-between;
  transition: background-color 0.2s ease, box-shadow 0.2s ease;
}

.calendar-date-cell:hover {
  background: #f8fbff;
}

.calendar-date-cell.is-selected {
  background: linear-gradient(180deg, #eff6ff 0%, #dbeafe 100%);
  box-shadow: inset 0 0 0 1px rgba(37, 99, 235, 0.18);
}

.calendar-date-cell.has-appointments .calendar-date-cell__day {
  color: #1d4ed8;
}

.calendar-date-cell__day {
  font-size: 14px;
  font-weight: 700;
  color: #0f172a;
}

.calendar-date-cell__count {
  margin-top: 6px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 36px;
  padding: 0 6px;
  border-radius: 999px;
  background: #eff6ff;
  color: #2563eb;
  font-size: 11px;
  line-height: 20px;
}

/deep/ .calendar-card .el-calendar {
  flex: 1;
}

/deep/ .calendar-card .el-calendar__header {
  padding: 8px 0 12px;
}

/deep/ .calendar-card .el-calendar-table td.is-selected {
  background: transparent;
}

/deep/ .calendar-card .el-calendar-table .el-calendar-day {
  padding: 4px;
  height: auto;
}

.filter-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.pending-patient-alert {
  margin-top: 12px;
}

.date-picker {
  width: 180px;
}

.doctor-filter {
  width: 180px;
}

.schedule-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 12px;
}

.schedule-title {
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
  line-height: 1.1;
}

.schedule-subtitle {
  margin-top: 4px;
  color: #94a3b8;
  font-size: 13px;
}

.schedule-tip {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
  color: #64748b;
  font-size: 12px;
}

.legend {
  width: 10px;
  height: 10px;
  border-radius: 999px;
  display: inline-block;
  margin-left: 8px;
}

.legend.waiting {
  background: #3b82f6;
}

.legend.visited {
  background: #16a34a;
}

.legend.left {
  background: #64748b;
}

.legend.rescheduled {
  background: #f59e0b;
}

.legend.arrears {
  background: #ef4444;
}

.schedule-scroll {
  width: 100%;
  overflow-x: auto;
  overflow-y: hidden;
}

.schedule-board {
  display: grid;
  grid-template-rows: 40px auto;
  border: 1px solid #dbe3ee;
  border-radius: 16px;
  overflow: hidden;
  background: #fff;
}

.schedule-corner,
.doctor-head {
  background: #f8fafc;
  color: #475569;
  font-weight: 700;
  padding: 10px 8px;
  border-bottom: 1px solid #e5e7eb;
  border-right: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
}

.doctor-head {
  justify-content: center;
  font-size: 13px;
  flex-direction: column;
  gap: 4px;
}

.doctor-head__name {
  color: #0f172a;
}

.doctor-head__shift {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 64px;
  padding: 0 8px;
  border-radius: 999px;
  line-height: 20px;
  font-size: 12px;
  font-weight: 700;
}

.doctor-head__shift.is-morning {
  background: #dbeafe;
  color: #1d4ed8;
}

.doctor-head__shift.is-evening {
  background: #ffedd5;
  color: #c2410c;
}

.doctor-head__shift.is-rest {
  background: #e2e8f0;
  color: #334155;
}

.doctor-head__shift.is-empty {
  background: #f1f5f9;
  color: #64748b;
}

.doctor-head__shift.is-custom {
  background: #e0e7ff;
  color: #4338ca;
}

.time-axis,
.schedule-lane {
  position: relative;
  border-right: 1px solid #e5e7eb;
  background-image: repeating-linear-gradient(
    to bottom,
    transparent,
    transparent calc(var(--slot-height) - 1px),
    #edf2f7 calc(var(--slot-height) - 1px),
    #edf2f7 var(--slot-height)
  );
}

.time-axis {
  background-color: #fafcff;
}

.schedule-lane {
  background-color: #fff;
  cursor: pointer;
}

.schedule-lane:hover {
  background-color: #fbfdff;
}

.time-label {
  position: absolute;
  left: 0;
  right: 0;
  transform: translateY(-50%);
  padding: 0 6px;
  font-size: 12px;
  font-weight: 600;
  color: #0f172a;
}

.time-label:first-child {
  transform: translateY(0);
  top: 4px !important;
}

.hour-line {
  position: absolute;
  left: 0;
  right: 0;
  height: 1px;
  background: #dbe7f5;
  z-index: 2;
}

.non-working-mask {
  position: absolute;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 8px;
  z-index: 1;
  pointer-events: none;
}

.non-working-mask.is-off {
  background: rgba(148, 163, 184, 0.16);
}

.non-working-mask.is-rest {
  background: rgba(100, 116, 139, 0.22);
}

.non-working-mask.is-empty {
  background: rgba(203, 213, 225, 0.24);
}

.non-working-mask__label {
  padding: 0 8px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.85);
  color: #64748b;
  font-size: 12px;
  line-height: 22px;
  font-weight: 700;
}

.appointment-block {
  position: absolute;
  border-radius: 10px;
  padding: 5px 9px 6px 11px;
  box-shadow: 0 6px 16px rgba(15, 23, 42, 0.08);
  cursor: pointer;
  overflow: hidden;
  transition: box-shadow 0.2s ease, transform 0.2s ease;
  z-index: 3;
}

.appointment-block:hover {
  box-shadow: 0 12px 24px rgba(15, 23, 42, 0.12);
  transform: translateY(-1px);
}

.appointment-block.is-waiting {
  background: linear-gradient(180deg, #eff6ff 0%, #dbeafe 100%);
  border-left: 4px solid #2563eb;
}

.appointment-block.is-visited {
  background: linear-gradient(180deg, #ecfdf5 0%, #dcfce7 100%);
  border-left: 4px solid #16a34a;
}

.appointment-block.is-left {
  background: linear-gradient(180deg, #f8fafc 0%, #e2e8f0 100%);
  border-left: 4px solid #64748b;
}

.appointment-block.is-rescheduled {
  background: linear-gradient(180deg, #fffbeb 0%, #fef3c7 100%);
  border-left: 4px solid #f59e0b;
}

.appointment-block.is-cancelled {
  background: linear-gradient(180deg, #fef2f2 0%, #fee2e2 100%);
  border-left: 4px solid #ef4444;
}

.appointment-block.is-overlap {
  padding-right: 6px;
  padding-left: 8px;
}

.appointment-block__title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 6px;
}

.appointment-block__name {
  font-size: 13px;
  font-weight: 700;
  color: #0f172a;
  line-height: 1.2;
  flex: 1;
  min-width: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.appointment-block__badges {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}

.appointment-block__status-chip,
.appointment-block__arrears-chip {
  border-radius: 999px;
  padding: 0 5px;
  font-size: 11px;
  line-height: 16px;
  white-space: nowrap;
}

.appointment-block__status-chip {
  background: rgba(15, 23, 42, 0.08);
  color: #334155;
}

.appointment-block__arrears-chip {
  background: rgba(239, 68, 68, 0.14);
  color: #b91c1c;
}

.appointment-block__offline-chip {
  background: rgba(245, 158, 11, 0.16);
  color: #b45309;
  border-radius: 999px;
  padding: 0 5px;
  font-size: 11px;
  line-height: 16px;
  white-space: nowrap;
}

.appointment-block__offline-chip.is-failed {
  background: rgba(239, 68, 68, 0.16);
  color: #b91c1c;
}

.appointment-block__meta,
.appointment-block__project {
  font-size: 11px;
  color: #475569;
  line-height: 1.3;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.appointment-block__project {
  color: #1e293b;
}

.resize-handle {
  position: absolute;
  left: 10px;
  right: 10px;
  height: 6px;
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.1);
  opacity: 0;
  transition: opacity 0.2s ease;
  cursor: ns-resize;
}

.resize-handle--top {
  top: 2px;
}

.resize-handle--bottom {
  bottom: 2px;
}

.appointment-block:hover .resize-handle {
  opacity: 1;
}

.patient-suggest-wrap {
  position: relative;
}

.schedule-form-tip {
  margin: -4px 0 14px 100px;
  padding: 9px 12px;
  border-radius: 10px;
  font-size: 12px;
  line-height: 1.6;
}

.schedule-form-tip.is-morning {
  background: #eff6ff;
  color: #1d4ed8;
}

.schedule-form-tip.is-evening {
  background: #fff7ed;
  color: #c2410c;
}

.schedule-form-tip.is-rest {
  background: #f1f5f9;
  color: #334155;
}

.schedule-form-tip.is-empty {
  background: #f8fafc;
  color: #64748b;
}

.schedule-form-tip.is-custom {
  background: #eef2ff;
  color: #4338ca;
}

.patient-suggestion-panel {
  position: absolute;
  top: calc(100% + 4px);
  left: 0;
  right: 0;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.12);
  z-index: 20;
  max-height: 240px;
  overflow: auto;
}

.patient-suggestion-item {
  padding: 10px 12px;
  cursor: pointer;
  border-bottom: 1px solid #f1f5f9;
}

.patient-suggestion-item:last-child {
  border-bottom: none;
}

.patient-suggestion-item:hover {
  background: #f8fafc;
}

.patient-suggestion-name {
  font-size: 13px;
  color: #0f172a;
  font-weight: 600;
}

.patient-suggestion-meta {
  margin-top: 2px;
  font-size: 12px;
  color: #64748b;
}

.quick-status-box {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.quick-status-patient {
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
}

.quick-status-detail {
  color: #475569;
  font-size: 13px;
  line-height: 1.5;
}

.quick-status-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

@media (max-width: 992px) {
  .schedule-layout {
    grid-template-columns: 1fr;
  }

  .page-title-bar,
  .schedule-header,
  .calendar-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .schedule-form-tip {
    margin-left: 0;
  }
}
</style>
