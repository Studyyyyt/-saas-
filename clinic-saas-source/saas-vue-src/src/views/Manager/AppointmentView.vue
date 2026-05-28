<template>
  <div class="page-wrap">
    <div class="page-title-bar">
      <div>
        <div class="page-kicker">预约管理</div>
        <h2>预约视图</h2>
      </div>
      <div class="page-title-actions">
        <div class="view-tab-bar">
          <div
            class="view-tab-item"
            :class="{ active: viewMode === 'calendar' }"
            @click="viewMode = 'calendar'"
          >
            <i class="el-icon-date"></i> 日历视图
          </div>
          <div
            class="view-tab-item"
            :class="{ active: viewMode === 'list' }"
            @click="viewMode = 'list'"
          >
            <i class="el-icon-tickets"></i> 列表视图
          </div>
        </div>
        <el-button v-if="appointmentAgentVisible" type="primary" plain icon="el-icon-magic-stick" @click="openAiAssist">
          AI 辅助
        </el-button>
        <el-button type="primary" class="btn-primary" @click="showAddModal">
          <i class="el-icon-plus"></i> 新增预约
        </el-button>
      </div>
    </div>

    <query-card>
      <div class="filter-main">
        <el-input v-model="searchName" placeholder="输入预约人姓名" clearable style="width: 200px"></el-input>
        <el-date-picker
          v-model="searchDate"
          type="date"
          value-format="yyyy-MM-dd"
          format="yyyy-MM-dd"
          placeholder="选择预约日期"
          style="width: 150px"
        ></el-date-picker>
        <el-button size="small" class="btn-today" @click="setToday">今天</el-button>
        <el-select v-model="searchDoctor" placeholder="选择医生" clearable style="width: 140px">
          <el-option v-for="doc in doctorOptions" :key="doc" :label="doc" :value="doc"></el-option>
        </el-select>
      </div>
      <div class="filter-status">
        <span class="filter-label">状态</span>
        <el-radio-group v-model="searchStatus" size="small">
          <el-radio-button label="">全部</el-radio-button>
          <el-radio-button label="待治疗">待治疗</el-radio-button>
          <el-radio-button label="治疗中">治疗中</el-radio-button>
          <el-radio-button label="已完成">已完成</el-radio-button>
          <el-radio-button label="已取消">已取消</el-radio-button>
        </el-radio-group>
      </div>
      <template #actions>
        <el-button type="primary" class="btn-primary" @click="searchAppointments">查询</el-button>
        <el-button class="btn-secondary" @click="resetSearch">重置</el-button>
      </template>
    </query-card>

    <!-- 列表视图 -->
    <el-card v-show="viewMode === 'list'" shadow="never" class="list-card">
      <el-table :data="appointments" stripe size="medium">
        <el-table-column label="患者" width="180">
          <template slot-scope="scope">
            <div class="patient-cell">
              <el-avatar :size="32" icon="el-icon-user" class="patient-avatar" />
              <div class="patient-info">
                <div class="patient-name-link" @click="goToPatientDetail(scope.row.patient_id)">{{ scope.row.patient_name }}</div>
                <div v-if="scope.row.phone" class="patient-phone">{{ scope.row.phone }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="预约日期" width="140">
          <template slot-scope="scope">
            <span style="white-space: nowrap">{{ scope.row.appointment_date }}</span>
          </template>
        </el-table-column>
        <el-table-column label="时间" width="100">
          <template slot-scope="scope">
            <span style="white-space: nowrap">{{ scope.row.appointment_time && scope.row.appointment_time.slice(0, 5) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="doctor_name" label="医生" width="100"></el-table-column>
        <el-table-column prop="appointment_purpose" label="预约项目"></el-table-column>
        <el-table-column label="状态" width="130">
          <template slot-scope="scope">
            <el-dropdown trigger="click" @command="(cmd) => handleStatusChange(scope.row, cmd)">
              <span class="status-tag" :class="`status-tag--${statusClass(scope.row.status)}`" style="cursor:pointer">
                {{ scope.row.status || '待治疗' }} <i class="el-icon-arrow-down" style="font-size:10px;margin-left:2px"></i>
              </span>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item v-for="opt in statusOptions" :key="opt.value" :command="opt.value">
                  <span class="status-dot" :class="`status-dot--${opt.class}`"></span> {{ opt.label }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240">
          <template slot-scope="scope">
            <div style="display: flex; gap: 4px; flex-wrap: nowrap; align-items: center">
              <el-button size="mini" type="primary" plain class="btn-mini" @click="startVisit(scope.row)">开始就诊</el-button>
              <el-button size="mini" type="text" class="btn-text" @click="handleEdit(scope.row)">编辑</el-button>
              <el-button size="mini" type="text" class="btn-text-danger" @click="handleDelete(scope.row.id)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 日历视图 -->
    <div v-show="viewMode === 'calendar'" class="calendar-view">
      <el-card shadow="never" class="calendar-card">
        <div class="calendar-header">
          <div class="calendar-nav-group">
            <el-button size="small" class="btn-week-nav" @click="prevWeek">
              <i class="el-icon-arrow-left"></i> 上一周
            </el-button>
            <el-date-picker
              v-model="calendarPickDate"
              type="date"
              value-format="yyyy-MM-dd"
              format="yyyy-MM-dd"
              placeholder="选择日期跳转"
              size="small"
              style="width: 140px"
              @change="onPickDate"
            ></el-date-picker>
            <div class="calendar-week-label">{{ currentWeekLabel }}</div>
            <el-button size="small" class="btn-week-nav" @click="nextWeek">
              下一周 <i class="el-icon-arrow-right"></i>
            </el-button>
            <el-button size="small" class="btn-today" @click="resetWeek">本周</el-button>
          </div>
          <div class="calendar-legend">
            <span class="legend-item"><span class="legend-dot legend-dot--arrived"></span>已到诊</span>
            <span class="legend-item"><span class="legend-dot legend-dot--pending"></span>待就诊</span>
            <span class="legend-item"><span class="legend-dot legend-dot--cancelled"></span>已取消</span>
          </div>
        </div>
        <div class="calendar-body-v2">
          <!-- 表头行 -->
          <div class="cal-header-row">
            <div class="cal-time-header">时间</div>
            <div
              v-for="day in weekDays"
              :key="'h-' + day.key"
              class="cal-day-header"
              :class="{ 'cal-day-header--today': day.isToday }"
            >
              <div class="day-name">{{ day.name }}</div>
              <div class="day-date">{{ day.date }}</div>
            </div>
          </div>
          <!-- 主体行 -->
          <div class="cal-content-row">
            <!-- 左侧时间轴 -->
            <div class="cal-time-axis">
              <div
                v-for="slot in allSlots"
                :key="slot"
                class="cal-time-cell"
                :class="{ 'cal-time-cell--hour': isHourSlot(slot) }"
              >
                {{ isHourSlot(slot) ? slot : '' }}
              </div>
            </div>
            <!-- 每天列 -->
            <div v-for="day in weekDays" :key="'col-' + day.key" class="cal-day-col" :data-date="day.fullDate">
              <!-- 背景格子（接收点击与双击） -->
              <div
                v-for="slot in allSlots"
                :key="slot"
                class="cal-bg-slot"
                @dblclick="handleSlotDblClick(day, slot)"
              ></div>
              <!-- 预约卡片层 -->
              <div class="cal-events-layer">
                <div
                  v-for="appt in getDayAppointments(day)"
                  :key="appt.id"
                  class="calendar-appt-card"
                  :class="`status-${statusClass(appt.status)}`"
                  :style="computeEventStyle(appt)"
                  @mousedown="startMove($event, appt)"
                >
                  <div class="card-header">
                    <span class="appt-patient-name" @click.stop="goToPatientDetail(appt.patient_id)">{{ appt.patient_name }}</span>
                    <div class="card-header-right">
                      <el-dropdown trigger="click" size="mini" @command="(cmd) => handleStatusChange(appt, cmd)">
                        <span class="appt-status-tag" :class="`status-${statusClass(appt.status)}`" @click.stop @mousedown.stop>
                          {{ appt.status || '待治疗' }}
                        </span>
                        <el-dropdown-menu slot="dropdown">
                          <el-dropdown-item v-for="opt in statusOptions" :key="opt.value" :command="opt.value">
                            {{ opt.label }}
                          </el-dropdown-item>
                        </el-dropdown-menu>
                      </el-dropdown>
                      <span class="appt-time">{{ appt.appointment_time && appt.appointment_time.slice(0,5) }}</span>
                    </div>
                  </div>
                  <div class="card-body">
                    <div class="appt-project">{{ appt.appointment_purpose }}</div>
                    <div class="appt-doctor">{{ appt.doctor_name }}</div>
                  </div>
                  <!-- 拖拽改变时长手柄 -->
                  <div class="resize-handle" @mousedown="startResize($event, appt)"></div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <el-dialog :title="isEditing ? '编辑预约' : '新增预约'" :visible.sync="dialogVisible" width="560px" custom-class="appt-dialog" append-to-body :modal-append-to-body="true">
      <el-form :model="editItem" label-width="100px">
        <el-form-item label="患者姓名" class="patient-name-form-item">
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
        <el-form-item label="预约日期">
          <el-date-picker
            v-model="editItem.appointment_date"
            type="date"
            value-format="yyyy-MM-dd"
            format="yyyy-MM-dd"
            placeholder="选择预约日期"
            style="width: 100%"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="预约时间">
          <el-time-picker
            v-model="editItem.appointment_time"
            value-format="HH:mm:ss"
            format="HH:mm"
            placeholder="选择预约时间"
            style="width: 100%"
          ></el-time-picker>
        </el-form-item>
        <el-form-item label="医生">
          <el-select v-model="editItem.doctor_name" placeholder="请选择医生" filterable allow-create default-first-option style="width: 100%">
            <el-option
              v-for="doc in doctorList"
              :key="doc"
              :label="doc"
              :value="doc"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="时长(分钟)">
          <el-input-number v-model="editItem.duration_minutes" :min="30" :max="480" :step="30" step-strictly style="width: 100%"></el-input-number>
        </el-form-item>
        <el-form-item label="预约项目">
          <el-input v-model="editItem.appointment_purpose" type="textarea" :rows="2" placeholder="请输入预约项目，如：种植牙初诊、洁牙等"></el-input>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="editItem.status" placeholder="请选择状态" style="width: 100%">
            <el-option v-for="opt in statusOptions" :key="opt.value" :label="opt.label" :value="opt.value"></el-option>
            <el-option label="已治疗" value="已治疗"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button
          v-if="isEditing && editItem.id"
          type="danger"
          plain
          class="btn-danger-plain"
          @click="handleDelete(editItem.id, { closeDialog: true })"
        >删除预约</el-button>
        <el-button class="btn-secondary" @click="closeDialog">取消</el-button>
        <el-button type="primary" class="btn-primary" @click="handleSave">{{ isEditing ? '保存' : '确认新增' }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios'
import { showApiError } from '@/utils/errorMessage'
import QueryCard from '@/components/design-system/QueryCard.vue'
import { getAdminSession } from '@/utils/adminSession'

const STATUS_OPTIONS = [
  { value: '待治疗', label: '待治疗', class: 'pending' },
  { value: '治疗中', label: '治疗中', class: 'in-progress' },
  { value: '已完成', label: '已完成', class: 'completed' },
  { value: '已取消', label: '已取消', class: 'cancelled' }
]

const createDefaultAppointment = () => ({
  id: null,
  patient_id: null,
  patient_name: '',
  appointment_date: '',
  appointment_time: '',
  doctor_name: '',
  appointment_purpose: '',
  status: '待治疗',
  duration_minutes: 60
})

export default {
  name: 'AppointmentView',
  components: { QueryCard },
  data() {
    return {
      viewMode: 'calendar',
      searchName: '',
      searchDate: '',
      searchDoctor: '',
      searchStatus: '',
      appointments: [],
      dialogVisible: false,
      isEditing: false,
      editItem: createDefaultAppointment(),
      allPatients: [],
      patientSuggestionVisible: false,
      patientSuggestionBlurTimer: null,
      stepActive: 0,
      doctorList: [],
      calendarWeekOffset: 0,
      calendarPickDate: '',
      slotHeight: 36,
      resizingAppt: null,
      resizeStartY: 0,
      resizeStartDuration: 60,
      movingAppt: null,
      moveStartX: 0,
      moveStartY: 0,
      moveStartTime: '',
      moveStartDate: '',
      isDragging: false,
      dragThreshold: 5,
      justDragged: false,
      statusOptions: STATUS_OPTIONS,
      appointmentAgentVisible: false,
      appointmentAgentKey: ''
    }
  },
  computed: {
    filteredPatientSuggestions() {
      return (this.allPatients || []).slice(0, 8)
    },
    allSlots() {
      const slots = []
      for (let h = 8; h <= 19; h++) {
        slots.push(`${String(h).padStart(2, '0')}:00`)
        if (h < 19) slots.push(`${String(h).padStart(2, '0')}:30`)
      }
      return slots
    },
    weekDays() {
      const days = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
      const today = new Date()
      // 基于 calendarWeekOffset 计算周起始日
      const weekStart = new Date(today)
      weekStart.setDate(today.getDate() - today.getDay() + this.calendarWeekOffset * 7)
      const result = []
      for (let i = 0; i < 7; i++) {
        const d = new Date(weekStart)
        d.setDate(weekStart.getDate() + i)
        result.push({
          key: i,
          name: days[i],
          date: `${d.getMonth() + 1}/${d.getDate()}`,
          fullDate: `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`,
          isToday: d.toDateString() === today.toDateString()
        })
      }
      return result
    },
    currentWeekLabel() {
      const days = this.weekDays
      if (!days.length) return ''
      const start = days[0].fullDate
      const end = days[6].fullDate
      const today = new Date()
      const weekStart = new Date(today)
      weekStart.setDate(today.getDate() - today.getDay())
      const isCurrentWeek = this.calendarWeekOffset === 0
      return `${start} ~ ${end}${isCurrentWeek ? '（本周）' : ''}`
    },
    doctorOptions() {
      const doctors = new Set()
      this.appointments.forEach(a => {
        if (a.doctor_name) doctors.add(a.doctor_name)
      })
      return Array.from(doctors)
    }
  },
  mounted() {
    this.loadDoctors()
    this.searchDate = this.getTodayString()
    this.searchAppointments()
    this.loadAppointmentAgentConfig()
  },
  methods: {
    prevWeek() {
      this.calendarWeekOffset--
    },
    nextWeek() {
      this.calendarWeekOffset++
    },
    resetWeek() {
      this.calendarWeekOffset = 0
    },
    onPickDate(dateStr) {
      // 用户手动选择日期，计算该日期所在周相对于本周的偏移
      if (!dateStr) return
      // 安全解析 yyyy-MM-dd 格式，避免 new Date('2026-05-08') 被当作 UTC 导致时区偏差
      const [y, m, d] = dateStr.split('-').map(Number)
      const picked = new Date(y, m - 1, d)

      const today = new Date()
      // 计算本周的周日（周起始）
      const todayWeekStart = new Date(today)
      todayWeekStart.setDate(today.getDate() - today.getDay())
      // 计算所选日期所在周的周日
      const pickedWeekStart = new Date(picked)
      pickedWeekStart.setDate(picked.getDate() - picked.getDay())
      // 计算周数差
      const msPerWeek = 7 * 24 * 60 * 60 * 1000
      const weekDiff = Math.round((pickedWeekStart.getTime() - todayWeekStart.getTime()) / msPerWeek)
      this.calendarWeekOffset = weekDiff
    },
    statusClass(status) {
      const s = String(status || '').trim()
      if (['已就诊', '已治疗', '已完成', '已离开'].includes(s)) return 'completed'
      if (['已取消', '已改约'].includes(s)) return 'cancelled'
      if (['待确认', '待治疗', '待就诊'].includes(s)) return 'pending'
      if (['治疗中', '正在治疗', '进行中'].includes(s)) return 'in-progress'
      if (['已到诊'].includes(s)) return 'arrived'
      return 'confirmed'
    },
    isHourSlot(slot) {
      return slot && slot.endsWith(':00')
    },
    getDayAppointments(day) {
      return this.appointments.filter(a => {
        const apptDate = String(a.appointment_date || '').trim()
        if (apptDate !== day.fullDate) return false
        if (this.searchName) {
          const keyword = String(this.searchName).trim().toLowerCase()
          if (!String(a.patient_name || '').toLowerCase().includes(keyword)) return false
        }
        if (this.searchDoctor && a.doctor_name !== this.searchDoctor) return false
        if (this.searchStatus && a.status !== this.searchStatus) return false
        return true
      })
    },
    computeEventStyle(appt) {
      const time = String(appt.appointment_time || '08:00').trim()
      const [hStr, mStr] = time.split(':')
      const h = parseInt(hStr, 10)
      const m = parseInt(mStr || '0', 10)
      const slotIndex = (h - 8) * 2 + Math.floor(m / 30)
      const top = slotIndex * this.slotHeight
      const duration = parseInt(appt.duration_minutes, 10) || 60
      const height = (duration / 30) * this.slotHeight
      return { top: top + 'px', height: height + 'px' }
    },
    startResize(e, appt) {
      e.stopPropagation()
      e.preventDefault()
      this.resizingAppt = appt
      this.resizeStartY = e.clientY
      this.resizeStartDuration = parseInt(appt.duration_minutes, 10) || 60
      document.addEventListener('mousemove', this.onResizeMove)
      document.addEventListener('mouseup', this.onResizeEnd)
    },
    onResizeMove(e) {
      if (!this.resizingAppt) return
      const deltaY = e.clientY - this.resizeStartY
      const deltaSlots = Math.round(deltaY / this.slotHeight)
      const newDuration = Math.max(30, this.resizeStartDuration + deltaSlots * 30)
      // 步进为30分钟
      const snapped = Math.round(newDuration / 30) * 30
      this.resizingAppt.duration_minutes = snapped
    },
    onResizeEnd() {
      if (!this.resizingAppt) return
      const appt = this.resizingAppt
      axios.put('/appointments/edit', appt).then(response => {
        if (response.data.code === '200') {
          this.$message.success('时长更新成功')
        } else {
          this.$message.error(response.data.msg || '时长更新失败')
        }
      }).catch(error => {
        console.error('Error updating duration:', error)
        this.$message.error('时长更新失败')
      }).finally(() => {
        this.resizingAppt = null
        this.resizeStartY = 0
        this.resizeStartDuration = 60
        document.removeEventListener('mousemove', this.onResizeMove)
        document.removeEventListener('mouseup', this.onResizeEnd)
      })
    },
    startMove(e, appt) {
      if (e.target.classList.contains('resize-handle')) return
      e.stopPropagation()
      e.preventDefault()
      this.movingAppt = appt
      this.moveStartX = e.clientX
      this.moveStartY = e.clientY
      this.moveStartTime = appt.appointment_time
      this.moveStartDate = appt.appointment_date
      this.isDragging = false
      document.addEventListener('mousemove', this.onMove)
      document.addEventListener('mouseup', this.onMoveEnd)
    },
    onMove(e) {
      if (!this.movingAppt) return
      const deltaY = e.clientY - this.moveStartY
      if (Math.abs(deltaY) > this.dragThreshold || Math.abs(e.clientX - this.moveStartX) > this.dragThreshold) {
        this.isDragging = true
      }
      if (!this.isDragging) return
      const deltaSlots = Math.round(deltaY / this.slotHeight)
      const [h, m, s] = String(this.moveStartTime || '08:00').split(':').map(Number)
      const totalMinutes = h * 60 + (m || 0) + deltaSlots * 30
      const newH = Math.floor(totalMinutes / 60)
      const newM = totalMinutes % 60
      if (newH < 8 || newH >= 19) return
      this.movingAppt.appointment_time = `${String(newH).padStart(2, '0')}:${String(newM).padStart(2, '0')}:00`
      // 水平方向：检测目标日期列
      const el = document.elementFromPoint(e.clientX, e.clientY)
      if (el) {
        const dayCol = el.closest('.cal-day-col')
        if (dayCol) {
          const targetDate = dayCol.dataset.date
          if (targetDate && targetDate !== this.movingAppt.appointment_date) {
            this.movingAppt.appointment_date = targetDate
          }
        }
      }
    },
    onMoveEnd() {
      if (!this.movingAppt) {
        document.removeEventListener('mousemove', this.onMove)
        document.removeEventListener('mouseup', this.onMoveEnd)
        return
      }
      const appt = this.movingAppt
      const wasDragging = this.isDragging
      this.movingAppt = null
      this.moveStartX = 0
      this.moveStartY = 0
      this.moveStartDate = ''
      this.isDragging = false
      document.removeEventListener('mousemove', this.onMove)
      document.removeEventListener('mouseup', this.onMoveEnd)
      if (wasDragging) {
        this.justDragged = true
        setTimeout(() => { this.justDragged = false }, 150)
        axios.put('/appointments/edit', appt).then(response => {
          if (response.data.code === '200') {
            this.$message.success('时间更新成功')
          } else {
            this.$message.error(response.data.msg || '时间更新失败')
          }
        }).catch(error => {
          console.error('Error updating time:', error)
          this.$message.error('时间更新失败')
        })
      }
    },
    handleStatusChange(row, newStatus) {
      if (row.status === newStatus) return
      const payload = { ...row, status: newStatus }
      axios.put('/appointments/edit', payload).then(response => {
        if (response.data.code === '200') {
          row.status = newStatus
          this.$message.success('状态更新成功')
        } else {
          this.$message.error(response.data.msg || '状态更新失败')
        }
      }).catch(error => {
        console.error('Error updating status:', error)
        this.$message.error('状态更新失败')
      })
    },
    startVisit(row) {
      this.$router.push({
        path: '/MedicalRecord',
        query: {
          patient_id: row.patient_id,
          patient_name: row.patient_name
        }
      })
    },
    goToPatientDetail(patientId) {
      if (this.isDragging || this.justDragged) return
      if (!patientId) {
        this.$message.warning('该预约未关联患者')
        return
      }
      this.$router.push({
        path: '/PatientDetail',
        query: { id: patientId }
      })
    },
    handleSlotDblClick(day, slot) {
      this.isEditing = false
      this.editItem = {
        ...createDefaultAppointment(),
        appointment_date: day.fullDate,
        appointment_time: slot + ':00'
      }
      this.dialogVisible = true
    },
    loadDoctors() {
      axios.get('/doctors/selectAll', {
        params: { page: 1, size: 1000 }
      }).then(response => {
        const data = response.data.data || {}
        const list = data.list || []
        const doctors = new Set(list.map(d => d.doctor_name).filter(Boolean))
        this.doctorList = Array.from(doctors)
        if (!this.doctorList.length) {
          // 后备方案：从已有预约数据中提取医生
          this.appointments.forEach(a => {
            if (a.doctor_name) doctors.add(a.doctor_name)
          })
          this.doctorList = Array.from(doctors)
        }
      }).catch(() => {
        // 接口不存在时，从已有预约数据中提取医生
        const doctors = new Set()
        this.appointments.forEach(a => {
          if (a.doctor_name) doctors.add(a.doctor_name)
        })
        this.doctorList = Array.from(doctors)
      })
    },
    fetchAppointments() {
      this.searchAppointments()
    },
    loadPatients(keyword = '') {
      axios.get('/patients/search', {
        params: {
          keyword: String(keyword || '').trim(),
          page: 1,
          size: 20
        }
      }).then(response => {
        const data = response.data.data || {}
        const list = data.list || response.data.data || []
        this.allPatients = (Array.isArray(list) ? list : [])
          .map(item => ({
            id: item.id,
            name: item.name || '',
            phone: item.phone || ''
          }))
          .filter(item => item.name)
      }).catch(error => {
        console.error('Error fetching patients:', error)
        this.allPatients = []
      })
    },
    getTodayString() {
      const d = new Date()
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
    },
    setToday() {
      this.searchDate = this.getTodayString()
      this.searchAppointments()
    },
    searchAppointments() {
      // 如果选择了其他条件但日期为空，默认查询当天
      const hasOtherFilter = this.searchName || this.searchDoctor || this.searchStatus
      if (hasOtherFilter && !this.searchDate) {
        this.searchDate = this.getTodayString()
      }
      axios.get('/appointments/selectAll', {
        params: { page: 1, size: 1000 }
      }).then(response => {
        const data = response.data.data || {}
        let list = data.list || []
        if (this.searchName) {
          const keyword = String(this.searchName).trim().toLowerCase()
          list = list.filter(item => String(item.patient_name || '').toLowerCase().includes(keyword))
        }
        if (this.searchDate) {
          list = list.filter(item => item.appointment_date === this.searchDate)
        }
        if (this.searchDoctor) {
          list = list.filter(item => item.doctor_name === this.searchDoctor)
        }
        if (this.searchStatus) {
          list = list.filter(item => item.status === this.searchStatus)
        }
        this.appointments = list
      }).catch(error => {
        console.error('Error searching appointments:', error)
        showApiError(this, '查询预约', error)
      })
    },
    resetSearch() {
      this.searchName = ''
      this.searchDate = ''
      this.searchDoctor = ''
      this.searchStatus = ''
      this.fetchAppointments()
    },
    showAddModal() {
      this.isEditing = false
      this.editItem = createDefaultAppointment()
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.isEditing = true
      this.editItem = Object.assign(createDefaultAppointment(), row)
      this.dialogVisible = true
    },
    closeDialog() {
      this.dialogVisible = false
      this.isEditing = false
      this.editItem = createDefaultAppointment()
      this.patientSuggestionVisible = false
      if (this.patientSuggestionBlurTimer) {
        clearTimeout(this.patientSuggestionBlurTimer)
        this.patientSuggestionBlurTimer = null
      }
    },
    handlePatientNameInput() {
      this.editItem.patient_id = null
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
      this.patientSuggestionVisible = false
    },
    validateAppointment() {
      if (!this.editItem.patient_name || !String(this.editItem.patient_name).trim()) return '患者姓名必填'
      // 如果 patient_id 为空，尝试根据名字匹配
      if (!this.editItem.patient_id) {
        const name = String(this.editItem.patient_name).trim()
        const matched = this.allPatients.find(p => p.name === name)
        if (matched && matched.id) {
          this.editItem.patient_id = matched.id
        } else {
          return '请选择患者'
        }
      }
      if (!this.editItem.appointment_date) return '预约日期必填'
      if (!this.editItem.appointment_time) return '预约时间必填'
      if (!this.editItem.doctor_name || !String(this.editItem.doctor_name).trim()) return '医生必填'
      if (!this.editItem.appointment_purpose || !String(this.editItem.appointment_purpose).trim()) return '预约项目必填'
      if (!this.editItem.status || !String(this.editItem.status).trim()) return '预约状态必填'
      return ''
    },
    handleSave() {
      // 保存前自动根据姓名匹配患者
      if (!this.editItem.patient_id && this.editItem.patient_name) {
        const name = String(this.editItem.patient_name).trim()
        const matched = this.allPatients.find(p => p.name === name)
        if (matched && matched.id) {
          this.editItem.patient_id = matched.id
        }
      }
      const validationMessage = this.validateAppointment()
      if (validationMessage) {
        this.$message.warning(validationMessage)
        return
      }
      const request = this.isEditing
        ? axios.put('/appointments/edit', this.editItem)
        : axios.post('/appointments/add', this.editItem)
      request.then(response => {
        if (response.data.code === '200') {
          this.$message.success(this.isEditing ? '编辑成功' : '新增成功')
          this.closeDialog()
          this.fetchAppointments()
        } else {
          this.$message.error(response.data.msg || (this.isEditing ? '编辑失败' : '新增失败'))
        }
      }).catch(error => {
        console.error('Error saving appointment:', error)
        this.$message.error((error.response && error.response.data && error.response.data.msg) || '保存失败')
      })
    },
    handleDelete(id, options = {}) {
      const appointmentId = Number(id)
      if (!appointmentId) {
        this.$message.warning('当前预约缺少有效ID，无法删除')
        return
      }
      this.$confirm('删除后无法恢复，确认删除这条预约吗？', '删除预约', {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        axios.delete(`/appointments/delete/${appointmentId}`)
          .then(() => {
            this.$message.success('删除成功')
            if (options.closeDialog) {
              this.closeDialog()
            }
            this.fetchAppointments()
          })
          .catch(error => {
            console.error('Error deleting appointment:', error)
            this.$message.error((error.response && error.response.data && error.response.data.msg) || '删除失败')
          })
      }).catch(error => {
        if (error !== 'cancel' && error !== 'close') {
          console.error('Error confirming appointment deletion:', error)
        }
      })
    },
    async loadAppointmentAgentConfig() {
      try {
        const session = getAdminSession() || {}
        const accountId = session.id || ''
        const res = await axios.get('/api/ai/function-mappings/appointment-assist/agent-key', { params: { accountId } })
        if (res.data && res.data.code === '200' && res.data.data) {
          const data = res.data.data
          if (data.agentKey && data.agentKey.trim() !== '') {
            this.appointmentAgentKey = data.agentKey
            this.appointmentAgentVisible = data.isVisibleOnPage === true || data.isVisibleOnPage === 1
          } else {
            this.appointmentAgentKey = ''
            this.appointmentAgentVisible = false
          }
        } else {
          this.appointmentAgentKey = ''
          this.appointmentAgentVisible = false
        }
      } catch (error) {
        console.error('加载预约辅助功能映射失败:', error)
        this.appointmentAgentKey = ''
        this.appointmentAgentVisible = false
      }
    },
    openAiAssist() {
      if (!this.appointmentAgentKey) {
        this.$message.warning('当前「预约辅助」功能未绑定 Agent')
        return
      }
      this.$message.info('AI 辅助功能即将打开，Agent: ' + this.appointmentAgentKey)
      // TODO: 接入具体的 AI 辅助交互弹窗或侧边栏
    }
  }
}
</script>

<style scoped>
/* e看牙 设计系统变量 — 定义在 .page-wrap 上确保 scoped 样式内可继承 */
.page-wrap {
  --primary: #00a6c9;
  --primary-hover: #0095b5;
  --primary-light: rgba(0, 166, 201, 0.08);
  --primary-border: rgba(0, 166, 201, 0.3);
  --text-primary: #1d222a;
  --text-regular: #3e3e3c;
  --text-secondary: #636a74;
  --text-muted: #9397a2;
  --text-white: #ffffff;
  --bg-page: #f5f5f5;
  --bg-card: #ffffff;
  --bg-hover: #f5f7fa;
  --border-color: #d9d9d9;
  --border-dark: #c0c4cc;
  --success: #52c41a;
  --warning: #faad14;
  --danger: #f86359;
  --info: #00a6c9;
  --shadow-card: 0 2px 8px rgba(0, 0, 0, 0.08);
  --shadow-dropdown: 0 4px 12px rgba(0, 0, 0, 0.1);
  --shadow-modal: 0 8px 24px rgba(0, 0, 0, 0.12);
  --radius-sm: 4px;
  --radius-md: 8px;
  --radius-lg: 12px;
  --space-xs: 4px;
  --space-sm: 8px;
  --space-md: 12px;
  --space-lg: 16px;
  --space-xl: 24px;
}

/* 筛选面板 */
.filter-main {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  align-items: center;
}
.filter-status {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
}
.filter-label {
  font-size: 13px;
  color: var(--text-secondary);
  white-space: nowrap;
}
.btn-today {
  padding: 0 12px !important;
  height: 32px !important;
  font-size: 13px !important;
}

/* 页面容器 */
.page-wrap {
  display: flex;
  flex-direction: column;
  gap: 16px;
  background: var(--bg-page);
  min-height: 100vh;
  padding: 16px;
  box-sizing: border-box;
  font-family: -apple-system, BlinkMacSystemFont, "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", "Helvetica Neue", Helvetica, Arial, sans-serif;
}

/* 页面标题栏 */
.page-title-bar {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
}

.page-kicker {
  color: var(--text-secondary);
  font-size: 13px;
  margin-bottom: 4px;
  font-weight: 400;
}

.page-title-bar h2 {
  margin: 0;
  color: var(--text-primary);
  font-size: 18px;
  font-weight: 600;
  line-height: 1.4;
}

/* 视图切换 Tab */
.view-tab-bar {
  display: flex;
  border-bottom: 1px solid var(--border-color);
  height: 40px;
  background: var(--bg-card);
  border-radius: var(--radius-md) var(--radius-md) 0 0;
  padding: 0 8px;
}

.view-tab-item {
  display: inline-flex;
  align-items: center;
  height: 40px;
  padding: 0 16px;
  font-size: 14px;
  color: var(--text-secondary);
  cursor: pointer;
  border-bottom: 3px solid transparent;
  margin-bottom: -1px;
  transition: all 0.3s ease;
}

.view-tab-item:hover {
  color: var(--primary);
}

.view-tab-item.active {
  color: var(--primary);
  border-bottom-color: var(--primary);
  font-weight: 500;
}

/* 操作按钮区 */
.page-title-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

/* 按钮规范 */
.btn-primary {
  background: var(--primary) !important;
  border-color: var(--primary) !important;
  color: var(--text-white) !important;
  border-radius: var(--radius-sm) !important;
  padding: 0 16px !important;
  height: 32px !important;
  font-size: 13px !important;
  font-weight: 500 !important;
  transition: background 0.2s ease;
}

.btn-primary:hover {
  background: var(--primary-hover) !important;
  border-color: var(--primary-hover) !important;
}

.btn-secondary {
  background: var(--bg-card) !important;
  color: var(--text-regular) !important;
  border: 1px solid var(--border-color) !important;
  border-radius: var(--radius-sm) !important;
  padding: 0 16px !important;
  height: 32px !important;
  font-size: 13px !important;
  transition: all 0.2s ease;
}

.btn-secondary:hover {
  border-color: var(--primary) !important;
  color: var(--primary) !important;
}

.btn-mini {
  border-radius: var(--radius-sm) !important;
  padding: 0 12px !important;
  height: 24px !important;
  font-size: 12px !important;
}

.btn-text {
  color: var(--primary) !important;
  padding: 0 8px !important;
  font-size: 13px !important;
}

.btn-text-danger {
  color: var(--danger) !important;
  padding: 0 8px !important;
  font-size: 13px !important;
}

.btn-danger-plain {
  border-radius: var(--radius-sm) !important;
  padding: 0 16px !important;
  height: 32px !important;
  font-size: 13px !important;
}

/* 卡片 */
.list-card,
.calendar-card {
  background: var(--bg-card) !important;
  border-radius: var(--radius-md) !important;
  border: 1px solid var(--border-color) !important;
  box-shadow: var(--shadow-card) !important;
}

.list-card:hover,
.calendar-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12) !important;
}

/* 表格规范 */
.el-table {
  background: transparent !important;
  font-size: 13px;
}

.el-table::before {
  display: none;
}

.el-table th {
  background: #fafafa !important;
  color: var(--text-secondary) !important;
  font-weight: 600 !important;
  font-size: 13px !important;
  height: 40px !important;
  padding: 0 12px !important;
  border-bottom: 1px solid var(--border-color) !important;
}

.el-table td {
  height: 40px !important;
  padding: 0 12px !important;
  color: var(--text-regular);
  font-size: 13px;
  border-bottom: 1px solid var(--border-color) !important;
}

.el-table tr:hover td {
  background: var(--bg-hover) !important;
}

.el-table--striped .el-table__body tr.el-table__row--striped td {
  background: rgba(0, 0, 0, 0.01) !important;
}

.el-table--striped .el-table__body tr.el-table__row--striped:hover td {
  background: var(--bg-hover) !important;
}

/* 患者单元格 */
.patient-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.patient-avatar {
  background: var(--primary-light) !important;
  color: var(--primary) !important;
  border: none !important;
}

.patient-name {
  font-weight: 500;
  color: var(--text-primary);
  font-size: 14px;
}

.patient-phone {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 2px;
}

/* 状态标签 */
.status-tag {
  display: inline-flex;
  align-items: center;
  height: 20px;
  padding: 0 8px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 500;
}

.status-tag--confirmed {
  background: var(--primary-light);
  color: var(--primary);
  border: 1px solid var(--primary-border);
}

.status-tag--completed {
  background: rgba(82, 196, 26, 0.1);
  color: var(--success);
  border: 1px solid rgba(82, 196, 26, 0.3);
}

.status-tag--pending {
  background: rgba(250, 173, 20, 0.1);
  color: var(--warning);
  border: 1px solid rgba(250, 173, 20, 0.3);
}

.status-tag--cancelled {
  background: rgba(248, 99, 89, 0.1);
  color: var(--danger);
  border: 1px solid rgba(248, 99, 89, 0.3);
}

/* 日历视图 */
.calendar-view {
  margin-top: 0;
}

.calendar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 0 12px;
}

.calendar-nav-group {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.calendar-nav-group .el-date-editor {
  margin: 0 4px;
}

.btn-week-nav {
  padding: 0 10px !important;
  height: 30px !important;
  font-size: 13px !important;
  color: var(--text-regular) !important;
  border: 1px solid var(--border-color) !important;
  background: var(--bg-card) !important;
  border-radius: var(--radius-sm) !important;
  transition: all 0.2s ease;
}

.btn-week-nav:hover {
  border-color: var(--primary) !important;
  color: var(--primary) !important;
}

.calendar-week-label {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.4;
  min-width: 200px;
  text-align: center;
}

.calendar-legend {
  display: flex;
  gap: 16px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--text-secondary);
}

.legend-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.legend-dot--arrived {
  background: var(--success);
}

.legend-dot--pending {
  background: var(--warning);
}

.legend-dot--cancelled {
  background: var(--danger);
}

/* 日历主体 — 新布局：Flex + 绝对定位事件层 */
.calendar-body-v2 {
  display: flex;
  flex-direction: column;
  border-radius: var(--radius-md);
  overflow: hidden;
  border: 1px solid var(--border-color);
}

.cal-header-row {
  display: flex;
  flex-shrink: 0;
  border-bottom: 1px solid var(--border-color);
}

.cal-time-header {
  width: 70px;
  flex-shrink: 0;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  color: var(--text-secondary);
  font-weight: 600;
  background: var(--bg-card);
  border-right: 1px solid var(--border-color);
}

.cal-day-header {
  flex: 1;
  min-width: 100px;
  height: 48px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: var(--bg-card);
  border-right: 1px solid var(--border-color);
  padding: 4px 0;
  transition: background 0.2s ease;
}

.cal-day-header:last-child {
  border-right: none;
}

.cal-day-header--today {
  background: var(--primary-light);
}

.cal-day-header--today .day-name {
  color: var(--primary);
  font-weight: 600;
}

.day-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
}

.day-date {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 2px;
}

/* 主体内容行 */
.cal-content-row {
  display: flex;
  overflow-x: auto;
  position: relative;
}

/* 左侧时间轴 */
.cal-time-axis {
  width: 70px;
  flex-shrink: 0;
  background: var(--bg-card);
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
}

.cal-time-cell {
  height: 36px;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  font-size: 11px;
  color: var(--text-secondary);
  padding-top: 2px;
  border-bottom: 1px solid var(--border-color);
  box-sizing: border-box;
}

.cal-time-cell--hour {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-regular);
  border-bottom: 1px solid var(--border-dark);
}

/* 每天列 */
.cal-day-col {
  flex: 1;
  min-width: 100px;
  position: relative;
  display: flex;
  flex-direction: column;
  background: var(--bg-card);
  border-right: 1px solid var(--border-color);
}

.cal-day-col:last-child {
  border-right: none;
}

.cal-bg-slot {
  height: 36px;
  border-bottom: 1px solid var(--border-color);
  box-sizing: border-box;
  transition: background 0.15s ease;
}

.cal-bg-slot:hover {
  background: var(--bg-hover);
}

/* 预约卡片绝对定位层 */
.cal-events-layer {
  position: absolute;
  top: 0;
  left: 0;
  right: 4px;
  bottom: 0;
  pointer-events: none;
}

.cal-events-layer .calendar-appt-card {
  pointer-events: auto;
}

/* 日历预约卡片 — e看牙风格（绝对定位） */
.calendar-appt-card {
  position: absolute;
  left: 2px;
  right: 2px;
  border-radius: 8px 8px 0 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.24);
  overflow: hidden;
  cursor: grab;
  transition: box-shadow 0.2s ease, height 0.1s ease;
  z-index: 2;
}

.calendar-appt-card:active {
  cursor: grabbing;
}

.calendar-appt-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.32);
}

.calendar-appt-card .card-header {
  padding: 0 8px;
  line-height: 20px;
  font-size: 13px;
  font-weight: 400;
  border-top-left-radius: 8px;
  border-top-right-radius: 8px;
  color: var(--text-white);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.calendar-appt-card .appt-patient-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-white);
}

.calendar-appt-card .appt-time {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.85);
}

.calendar-appt-card .card-body {
  background: var(--bg-card);
  color: var(--text-regular);
  font-size: 12px;
  padding: 4px 8px;
  border-bottom-left-radius: 8px;
}

.calendar-appt-card .appt-project {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: var(--text-regular);
}

.calendar-appt-card .appt-doctor {
  font-size: 11px;
  color: var(--text-secondary);
  margin-top: 2px;
}

/* 各状态标题色 */
.calendar-appt-card.status-confirmed .card-header { background: var(--primary); }
.calendar-appt-card.status-completed .card-header { background: var(--success); }
.calendar-appt-card.status-cancelled .card-header { background: var(--danger); }
.calendar-appt-card.status-pending .card-header { background: var(--warning); }

/* 拖拽调整时长手柄 */
.resize-handle {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 6px;
  cursor: ns-resize;
  background: transparent;
  transition: background 0.15s ease;
  z-index: 5;
}
.resize-handle::after {
  content: '';
  position: absolute;
  left: 30%;
  right: 30%;
  top: 2px;
  height: 2px;
  border-radius: 1px;
  background: rgba(255, 255, 255, 0.6);
}
.resize-handle:hover,
.resize-handle:active {
  background: rgba(0, 0, 0, 0.12);
}

/* 弹窗 / 模态框 */
.appt-dialog {
  border-radius: var(--radius-lg) !important;
  box-shadow: var(--shadow-modal) !important;
  overflow: hidden;
}

.appt-dialog .el-dialog__header {
  padding: 16px 20px 12px !important;
  border-bottom: 1px solid var(--border-color);
}

.appt-dialog .el-dialog__title {
  font-size: 16px !important;
  font-weight: 600 !important;
  color: var(--text-primary) !important;
  line-height: 1.4;
}

.appt-dialog .el-dialog__body {
  padding: 16px 20px !important;
}

.appt-dialog .el-dialog__footer {
  padding: 12px 20px 16px !important;
  border-top: 1px solid var(--border-color);
}

.dialog-footer {
  text-align: right;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

/* 表单输入框 */
.appt-dialog .el-input__inner,
.appt-dialog .el-textarea__inner,
.appt-dialog .el-date-editor .el-input__inner,
.appt-dialog .el-select .el-input__inner {
  height: 32px !important;
  line-height: 32px !important;
  border-radius: var(--radius-sm) !important;
  border: 1px solid var(--border-color) !important;
  background: var(--bg-card) !important;
  color: var(--text-regular) !important;
  font-size: 13px !important;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
  padding: 0 12px !important;
}

.appt-dialog .el-input__inner:focus,
.appt-dialog .el-textarea__inner:focus,
.appt-dialog .el-date-editor .el-input__inner:focus,
.appt-dialog .el-select .el-input__inner:focus {
  border-color: var(--primary) !important;
  box-shadow: 0 0 0 2px var(--primary-light) !important;
  outline: none !important;
}

.appt-dialog .el-textarea__inner {
  padding: 8px 12px !important;
  height: auto !important;
  line-height: 1.5 !important;
}

.appt-dialog .el-form-item__label {
  color: var(--text-secondary) !important;
  font-weight: 500 !important;
  font-size: 13px !important;
  line-height: 32px !important;
}

/* 患者建议下拉面板 */
.patient-suggest-wrap {
  position: relative;
}

.patient-suggestion-panel {
  position: absolute;
  top: calc(100% + 4px);
  left: 0;
  right: 0;
  max-height: 240px;
  overflow-y: auto;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-dropdown);
  z-index: 20;
  padding: 4px 0;
}

.patient-suggestion-item {
  padding: 8px 12px;
  cursor: pointer;
  transition: background 0.2s ease;
}

.patient-suggestion-item:hover {
  background: var(--bg-hover);
}

.patient-suggestion-name {
  color: var(--text-primary);
  font-size: 13px;
  line-height: 1.4;
  font-weight: 500;
}

.patient-suggestion-meta {
  margin-top: 2px;
  color: var(--text-secondary);
  font-size: 12px;
}

/* 响应式 */
@media (max-width: 768px) {
  .page-wrap {
    padding: 12px;
    gap: 12px;
  }

  .page-title-bar {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .page-title-bar h2 {
    font-size: 16px;
  }

  .page-title-actions {
    width: 100%;
    flex-wrap: wrap;
  }

  .calendar-body {
    overflow-x: scroll;
    -webkit-overflow-scrolling: touch;
  }

  .calendar-day-col {
    min-width: 100px;
  }

  .appt-dialog {
    width: 90% !important;
    margin-top: 10vh !important;
  }
}

@media (max-width: 480px) {
  .view-tab-bar {
    width: 100%;
  }

  .calendar-time-col {
    width: 56px;
  }

  .calendar-day-col {
    min-width: 80px;
  }
}

/* 患者姓名可点击链接 */
.patient-name-link {
  color: var(--primary);
  cursor: pointer;
  font-weight: 500;
  font-size: 14px;
}
.patient-name-link:hover {
  text-decoration: underline;
}

/* 状态标签 — 治疗中 */
.status-tag--in-progress {
  background: rgba(0, 166, 201, 0.1);
  color: var(--primary);
  border: 1px solid rgba(0, 166, 201, 0.3);
}
.status-tag--arrived {
  background: rgba(82, 196, 26, 0.1);
  color: var(--success);
  border: 1px solid rgba(82, 196, 26, 0.3);
}

/* 日历卡片头部右侧 */
.card-header-right {
  display: flex;
  align-items: center;
  gap: 6px;
}

/* 日历卡片内状态小标签 */
.appt-status-tag {
  display: inline-flex;
  align-items: center;
  height: 16px;
  padding: 0 6px;
  border-radius: 8px;
  font-size: 10px;
  font-weight: 500;
  cursor: pointer;
  line-height: 1;
  white-space: nowrap;
}
.appt-status-tag.status-pending {
  background: rgba(250, 173, 20, 0.9);
  color: #fff;
}
.appt-status-tag.status-in-progress,
.appt-status-tag.status-confirmed {
  background: var(--primary);
  color: #fff;
}
.appt-status-tag.status-completed {
  background: var(--success);
  color: #fff;
}
.appt-status-tag.status-cancelled {
  background: var(--danger);
  color: #fff;
}
.appt-status-tag.status-arrived {
  background: var(--success);
  color: #fff;
}

/* 状态圆点（下拉菜单用） */
.status-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 6px;
}
.status-dot--pending { background: var(--warning); }
.status-dot--in-progress { background: var(--primary); }
.status-dot--completed { background: var(--success); }
.status-dot--cancelled { background: var(--danger); }
.status-dot--confirmed { background: var(--primary); }
.status-dot--arrived { background: var(--success); }

/* 治疗中卡片头部色 */
.calendar-appt-card.status-in-progress .card-header { background: var(--primary); }
.calendar-appt-card.status-arrived .card-header { background: var(--success); }

/* 弹窗层级修复 */
.appt-dialog {
  z-index: 2000 !important;
}
.appt-dialog .el-dialog__wrapper {
  z-index: 2000 !important;
}
</style>
