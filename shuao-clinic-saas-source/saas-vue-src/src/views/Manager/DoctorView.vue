<template>
  <div class="schedule-page">
    <!-- 页面头部 -->
    <div class="page-header-shell">
      <div class="page-header-left">
        <div class="page-kicker">排班管理</div>
        <h2 class="page-title">医生排班中心</h2>
        <p class="page-subtitle">智能排班 · 关联预约 · 高效管理</p>
      </div>
      <div class="page-header-right">
        <el-button v-if="isAiEnabled('doctor-schedule')" type="primary" plain icon="el-icon-magic-stick" @click="openAiPanel">
          AI 智能排班
        </el-button>
        <el-button icon="el-icon-download" @click="exportSchedule">导出排班</el-button>
      </div>
    </div>

    <!-- 工具栏 -->
    <el-card class="toolbar-card" shadow="never">
      <div class="toolbar-main">
        <div class="toolbar-group">
          <el-button icon="el-icon-arrow-left" circle size="small" @click="goPrevMonth"></el-button>
          <el-date-picker
            v-model="selectedMonth"
            type="month"
            value-format="yyyy-MM"
            format="yyyy-MM"
            class="month-picker"
            placeholder="选择月份"
            size="small"
          ></el-date-picker>
          <el-button icon="el-icon-arrow-right" circle size="small" @click="goNextMonth"></el-button>
        </div>

        <div class="toolbar-divider"></div>

        <div class="brush-group">
          <span class="toolbar-label">班次画笔</span>
          <el-radio-group v-model="activeShift" size="small">
            <el-radio-button
              v-for="option in brushOptions"
              :key="option.value"
              :label="option.value"
            >{{ option.label }}</el-radio-button>
          </el-radio-group>
        </div>

        <div class="toolbar-divider"></div>

        <div class="batch-group">
          <el-button size="small" icon="el-icon-document-copy" @click="showTemplateDialog">排班模板</el-button>
          <el-button size="small" icon="el-icon-edit-outline" @click="showCustomShiftDialog">自定义班次</el-button>
        </div>

        <div class="toolbar-actions">
          <el-tag v-if="hasPendingChanges" type="warning" effect="dark" size="small">
            待保存 {{ pendingChangeCount }} 项
          </el-tag>
          <el-button type="primary" :loading="saving" :disabled="!hasPendingChanges" size="small" @click="saveSchedules">
            保存排班
          </el-button>
          <el-button :disabled="!hasPendingChanges" size="small" @click="discardPendingChanges">撤销</el-button>
          <el-button icon="el-icon-refresh" :loading="loading" size="small" @click="reloadAll">刷新</el-button>
        </div>
      </div>

      <div v-if="selectionRange" class="toolbar-selection-hint">
        <span>已框选 <b>{{ selectionRange.count }}</b> 个单元格，按当前画笔批量设置？</span>
        <el-button type="primary" size="mini" @click="applyShiftToSelection">确认设置</el-button>
        <el-button size="mini" @click="clearSelection">取消</el-button>
      </div>
    </el-card>

    <!-- 统计摘要 -->
    <div class="summary-grid">
      <div class="summary-card total">
        <div class="summary-icon">
          <i class="el-icon-user-solid"></i>
        </div>
        <div class="summary-body">
          <div class="summary-value">{{ doctorCount }}</div>
          <div class="summary-label">排班医生</div>
        </div>
      </div>
      <div class="summary-card morning">
        <div class="summary-icon">
          <i class="el-icon-sunrise"></i>
        </div>
        <div class="summary-body">
          <div class="summary-value">{{ monthSummary.morning }}</div>
          <div class="summary-label">早班人次</div>
        </div>
      </div>
      <div class="summary-card evening">
        <div class="summary-icon">
          <i class="el-icon-moon"></i>
        </div>
        <div class="summary-body">
          <div class="summary-value">{{ monthSummary.evening }}</div>
          <div class="summary-label">晚班人次</div>
        </div>
      </div>
      <div class="summary-card rest">
        <div class="summary-icon">
          <i class="el-icon-coffee-cup"></i>
        </div>
        <div class="summary-body">
          <div class="summary-value">{{ monthSummary.rest }}</div>
          <div class="summary-label">休息天数</div>
        </div>
      </div>
      <div class="summary-card saturation">
        <div class="summary-icon">
          <i class="el-icon-data-line"></i>
        </div>
        <div class="summary-body">
          <div class="summary-value">{{ saturationRate }}%</div>
          <div class="summary-label">预约饱和度</div>
        </div>
        <el-progress
          :percentage="saturationRate"
          :color="saturationColor"
          :show-text="false"
          class="summary-progress"
        ></el-progress>
      </div>
      <div class="summary-card warning" v-if="overloadDoctors.length">
        <div class="summary-icon">
          <i class="el-icon-warning"></i>
        </div>
        <div class="summary-body">
          <div class="summary-value">{{ overloadDoctors.length }}</div>
          <div class="summary-label">超负荷医生</div>
        </div>
      </div>
    </div>

    <!-- 排班矩阵 -->
    <el-card class="matrix-card" shadow="never" v-loading="loading || saving">
      <template v-if="allDoctors.length">
        <div class="matrix-head">
          <div>
            <div class="matrix-title">{{ monthTitle }} 全员排班矩阵</div>
            <div class="matrix-subtitle">
              点击单元格设置班次 · 拖拽框选批量设置 · 点击医生名整行设置 · 点击日期整列设置
              <span v-if="activeBrushText" class="active-brush-text">当前画笔：{{ activeBrushText }}</span>
            </div>
          </div>
          <div class="matrix-legend">
            <span class="legend-chip morning">早班 09:00-18:00</span>
            <span class="legend-chip evening">晚班 13:00-21:00</span>
            <span class="legend-chip rest">休息</span>
            <span class="legend-chip custom">自定义</span>
            <span class="legend-chip empty">未排班</span>
          </div>
        </div>

        <div class="matrix-scroll" ref="matrixScroll"
          @mousedown="onMatrixMouseDown"
          @mousemove="onMatrixMouseMove"
          @mouseup="onMatrixMouseUp"
          @mouseleave="onMatrixMouseLeave"
        >
          <table class="matrix-table" ref="matrixTable">
            <thead>
              <tr>
                <th class="doctor-header">
                  <div class="doctor-header-content">
                    <span>医生</span>
                    <el-tooltip content="点击医生名可整行设置当前班次">
                      <i class="el-icon-question"></i>
                    </el-tooltip>
                  </div>
                </th>
                <th
                  v-for="date in monthDates"
                  :key="date.dateText"
                  class="date-header"
                  :class="{ 'is-today': date.isToday, 'is-weekend': date.isWeekend }"
                  @click="applyShiftToColumn(date)"
                >
                  <div class="date-header__day">{{ date.day }}</div>
                  <div class="date-header__meta">{{ date.weekLabel }}</div>
                  <div v-if="date.appointmentCount" class="date-header__badge">
                    {{ date.appointmentCount }}约
                  </div>
                </th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in scheduleRows" :key="row.doctor.id">
                <th class="doctor-cell" @click="applyShiftToRow(row)">
                  <div class="doctor-cell__avatar">
                    {{ row.doctor.name.charAt(0) }}
                  </div>
                  <div class="doctor-cell__info">
                    <div class="doctor-cell__name">{{ row.doctor.name }}</div>
                    <div class="doctor-cell__meta">
                      <span>早{{ row.summary.morning }}</span>
                      <span>晚{{ row.summary.evening }}</span>
                      <span>休{{ row.summary.rest }}</span>
                    </div>
                    <div v-if="row.appointmentCount" class="doctor-cell__appointments">
                      本月 {{ row.appointmentCount }} 个预约
                    </div>
                  </div>
                </th>

                <td
                  v-for="cell in row.cells"
                  :key="cell.key"
                  class="matrix-td"
                  :class="{
                    'is-today': cell.isToday,
                    'is-weekend': cell.isWeekend,
                    'is-selected': isCellSelected(cell),
                    'has-conflict': cell.hasConflict
                  }"
                  :data-key="cell.key"
                  @click="applyShiftToCell(cell)"
                >
                  <button
                    type="button"
                    class="shift-cell"
                    :class="shiftCellClass(cell)"
                  >
                    <span class="shift-cell__label">{{ cell.displayLabel }}</span>
                    <span class="shift-cell__time">{{ cell.timeText }}</span>
                    <span v-if="cell.appointmentCount" class="shift-cell__badge">
                      {{ cell.appointmentCount }}
                    </span>
                    <span v-if="cell.isDirty" class="shift-cell__draft">改</span>
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </template>

      <el-empty v-else description="暂无可排班医生"></el-empty>
    </el-card>

    <!-- 自定义班次弹窗 -->
    <el-dialog title="自定义班次" :visible.sync="customShiftVisible" width="400px">
      <el-form label-width="80px">
        <el-form-item label="医生">
          <span>{{ customShiftForm.doctorName }}</span>
        </el-form-item>
        <el-form-item label="日期">
          <span>{{ customShiftForm.scheduleDate }}</span>
        </el-form-item>
        <el-form-item label="开始时间">
          <el-time-picker v-model="customShiftForm.startTime" format="HH:mm" value-format="HH:mm:ss" placeholder="开始时间"></el-time-picker>
        </el-form-item>
        <el-form-item label="结束时间">
          <el-time-picker v-model="customShiftForm.endTime" format="HH:mm" value-format="HH:mm:ss" placeholder="结束时间"></el-time-picker>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="customShiftVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmCustomShift">确认</el-button>
      </div>
    </el-dialog>

    <!-- 排班模板弹窗 -->
    <el-dialog title="排班模板" :visible.sync="templateDialogVisible" width="600px">
      <div class="template-dialog-body">
        <div class="template-section">
          <h4>快速套用模板</h4>
          <div class="template-list">
            <div
              v-for="tpl in shiftTemplates"
              :key="tpl.id"
              class="template-item"
              @click="applyTemplate(tpl)"
            >
              <div class="template-name">{{ tpl.name }}</div>
              <div class="template-pattern">
                <span v-for="(tone, day) in tpl.pattern" :key="day" :class="`pattern-dot tone-${tone}`">
                  {{ ['日','一','二','三','四','五','六'][day-1] }}
                </span>
              </div>
            </div>
          </div>
        </div>
        <el-divider></el-divider>
        <div class="template-section">
          <h4>保存当前为模板</h4>
          <el-input v-model="newTemplateName" placeholder="输入模板名称" style="width: 300px; margin-right: 10px;"></el-input>
          <el-button type="primary" @click="saveAsTemplate" :disabled="!newTemplateName">保存</el-button>
        </div>
      </div>
    </el-dialog>

    <!-- AI 智能排班面板 -->
    <transition name="ai-panel">
      <div v-if="aiPanelVisible" class="ai-panel-overlay" @click.self="aiPanelVisible = false">
        <div class="ai-panel">
          <div class="ai-panel-header">
            <div>
              <i class="el-icon-magic-stick"></i>
              <span>AI 智能排班建议</span>
            </div>
            <el-button type="text" icon="el-icon-close" @click="aiPanelVisible = false"></el-button>
          </div>
          <div class="ai-panel-body" v-loading="aiLoading">
            <div v-if="aiSuggestion" class="ai-suggestion-content">
              <div class="ai-insight-card">
                <div class="ai-insight-title">📊 预约数据分析</div>
                <div class="ai-insight-text">{{ aiSuggestion.analysis }}</div>
              </div>
              <div class="ai-insight-card">
                <div class="ai-insight-title">💡 排班优化建议</div>
                <div class="ai-insight-text">{{ aiSuggestion.advice }}</div>
              </div>
              <div class="ai-insight-card">
                <div class="ai-insight-title">📋 建议调整明细</div>
                <div class="ai-change-list">
                  <div v-for="(change, idx) in aiSuggestion.changes" :key="idx" class="ai-change-item">
                    <span class="ai-change-date">{{ change.date }}</span>
                    <span class="ai-change-doctor">{{ change.doctor }}</span>
                    <span class="ai-change-arrow">→</span>
                    <span :class="`ai-change-shift tone-${change.toTone}`">{{ change.toShift }}</span>
                  </div>
                </div>
              </div>
              <div class="ai-actions">
                <el-button type="primary" @click="applyAiSuggestion">一键应用建议</el-button>
                <el-button @click="aiPanelVisible = false">暂不应用</el-button>
              </div>
            </div>
            <div v-else class="ai-empty">
              <i class="el-icon-magic-stick"></i>
              <p>AI 将根据历史预约量和医生负荷<br>为您生成最优排班建议</p>
              <el-button type="primary" @click="generateAiSuggestion">生成排班建议</el-button>
            </div>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script>
import axios from 'axios'
import { showApiError } from '@/utils/errorMessage'
import { isAiEnabled as checkAiEnabled } from '@/utils/aiConfig'
import {
  SHIFT_CODE_EVENING,
  SHIFT_CODE_MORNING,
  SHIFT_CODE_NONE,
  SHIFT_CODE_REST,
  SHIFT_CODE_CUSTOM,
  SHIFT_OPTIONS,
  createShiftPayload,
  createCustomShiftPayload,
  normalizeDateText,
  normalizeScheduleRecord,
  scheduleDisplayLabel,
  scheduleKey,
  scheduleTimeDescription,
  scheduleTone,
  buildTemplateFromCells,
  applyTemplateToMonth
} from '@/utils/doctorSchedule'

const CLEAR_SHIFT = 'CLEAR'
const WEEK_LABELS = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']

export default {
  name: 'DoctorView',
  data() {
    return {
      allDoctors: [],
      selectedMonth: this.formatMonth(new Date()),
      scheduleEntries: [],
      draftScheduleMap: {},
      activeShift: SHIFT_CODE_MORNING,
      loading: false,
      saving: false,
      // 预约数据关联
      appointmentMap: {},
      // 框选
      isMouseDown: false,
      selectionStart: null,
      selectionEnd: null,
      selectionRange: null,
      // 自定义班次
      customShiftVisible: false,
      customShiftForm: {
        doctorName: '',
        scheduleDate: '',
        startTime: '',
        endTime: ''
      },
      // 模板
      templateDialogVisible: false,
      shiftTemplates: [],
      newTemplateName: '',
      // AI
      aiPanelVisible: false,
      aiLoading: false,
      aiSuggestion: null
    }
  },
  computed: {
    brushOptions() {
      return [
        { value: SHIFT_CODE_MORNING, label: '早班' },
        { value: SHIFT_CODE_EVENING, label: '晚班' },
        { value: SHIFT_CODE_REST, label: '休息' },
        { value: SHIFT_CODE_CUSTOM, label: '自定义' },
        { value: CLEAR_SHIFT, label: '清空' }
      ]
    },
    activeBrushText() {
      if (this.activeShift === CLEAR_SHIFT) return '清空排班'
      const matched = SHIFT_OPTIONS.find(item => item.value === this.activeShift)
      return matched ? `${matched.label}（${matched.timeText}）` : ''
    },
    doctorCount() {
      return Array.isArray(this.allDoctors) ? this.allDoctors.length : 0
    },
    pendingChangeCount() {
      return Object.keys(this.draftScheduleMap || {}).length
    },
    hasPendingChanges() {
      return this.pendingChangeCount > 0
    },
    serverScheduleMap() {
      const map = {}
      ;(this.scheduleEntries || []).forEach(item => {
        if (!item) return
        map[scheduleKey(item.doctor_name, item.schedule_date)] = item
      })
      return map
    },
    monthMeta() {
      const matched = String(this.selectedMonth || '').match(/^(\d{4})-(\d{2})$/)
      const baseDate = matched
        ? new Date(Number(matched[1]), Number(matched[2]) - 1, 1)
        : new Date()
      return {
        year: baseDate.getFullYear(),
        month: baseDate.getMonth()
      }
    },
    monthTitle() {
      return `${this.monthMeta.year}年${String(this.monthMeta.month + 1).padStart(2, '0')}月`
    },
    monthDates() {
      const lastDay = new Date(this.monthMeta.year, this.monthMeta.month + 1, 0).getDate()
      const list = []
      for (let day = 1; day <= lastDay; day++) {
        const date = new Date(this.monthMeta.year, this.monthMeta.month, day)
        const weekDay = date.getDay()
        const dateText = normalizeDateText(date)
        list.push({
          day,
          dateText,
          weekLabel: WEEK_LABELS[weekDay],
          isToday: dateText === normalizeDateText(new Date()),
          isWeekend: weekDay === 0 || weekDay === 6,
          appointmentCount: this.getDateAppointmentCount(dateText)
        })
      }
      return list
    },
    scheduleRows() {
      return (this.allDoctors || []).map(doctor => {
        const cells = this.monthDates.map(date => {
          const key = scheduleKey(doctor.name, date.dateText)
          const entry = this.resolveDisplaySchedule(key)
          const displayLabel = entry ? scheduleDisplayLabel(entry) : SHIFT_CODE_NONE
          const tone = entry ? scheduleTone(entry.shiftType || displayLabel) : 'empty'
          const appointmentCount = this.getCellAppointmentCount(doctor.name, date.dateText)
          return {
            key,
            doctor,
            dateText: date.dateText,
            day: date.day,
            isToday: date.isToday,
            isWeekend: date.isWeekend,
            entry,
            displayLabel,
            timeText: entry ? scheduleTimeDescription(entry) : '点击设置',
            tone,
            isDirty: Object.prototype.hasOwnProperty.call(this.draftScheduleMap, key),
            appointmentCount,
            hasConflict: this.checkConflict(doctor.name, date.dateText)
          }
        })
        const summary = this.summarizeCells(cells)
        return {
          doctor,
          cells,
          summary,
          appointmentCount: cells.reduce((sum, c) => sum + (c.appointmentCount || 0), 0)
        }
      })
    },
    monthSummary() {
      const summary = { morning: 0, evening: 0, rest: 0, empty: 0, custom: 0 }
      ;(this.scheduleRows || []).forEach(row => {
        summary.morning += row.summary.morning
        summary.evening += row.summary.evening
        summary.rest += row.summary.rest
        summary.empty += row.summary.empty
        summary.custom += row.summary.custom
      })
      return summary
    },
    saturationRate() {
      // 计算预约饱和度：有预约的排班单元格 / 总排班单元格（排除休息）
      let totalWorkDays = 0
      let hasAppointmentDays = 0
      this.scheduleRows.forEach(row => {
        row.cells.forEach(cell => {
          if (cell.tone !== 'rest' && cell.tone !== 'empty') {
            totalWorkDays++
            if (cell.appointmentCount > 0) hasAppointmentDays++
          }
        })
      })
      if (totalWorkDays === 0) return 0
      return Math.round((hasAppointmentDays / totalWorkDays) * 100)
    },
    saturationColor() {
      if (this.saturationRate < 30) return '#10b981'
      if (this.saturationRate < 70) return '#f59e0b'
      return '#ef4444'
    },
    overloadDoctors() {
      // 工作天数 > 22天视为超负荷
      return this.scheduleRows.filter(row => {
        const workDays = row.summary.morning + row.summary.evening + row.summary.custom
        return workDays > 22
      })
    }
  },
  mounted() {
    this.loadDoctors()
    this.loadSchedules()
    this.loadAppointments()
    this.loadTemplates()
  },
  methods: {
    formatMonth(value) {
      const date = new Date(value)
      if (Number.isNaN(date.getTime())) return ''
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      return `${year}-${month}`
    },
    normalizeDoctor(item) {
      if (!item || !item.id || !item.name) return null
      return { id: Number(item.id), name: String(item.name).trim() }
    },
    summarizeCells(cells) {
      const summary = { morning: 0, evening: 0, rest: 0, empty: 0, custom: 0 }
      ;(cells || []).forEach(cell => {
        if (cell.displayLabel === SHIFT_CODE_MORNING) summary.morning += 1
        else if (cell.displayLabel === SHIFT_CODE_EVENING) summary.evening += 1
        else if (cell.displayLabel === SHIFT_CODE_REST) summary.rest += 1
        else if (cell.displayLabel === SHIFT_CODE_CUSTOM) summary.custom += 1
        else summary.empty += 1
      })
      return summary
    },
    resolveDisplaySchedule(key) {
      if (Object.prototype.hasOwnProperty.call(this.draftScheduleMap, key)) {
        return this.draftScheduleMap[key]
      }
      return this.serverScheduleMap[key] || null
    },
    scheduleEntriesEqual(serverEntry, nextEntry) {
      const left = serverEntry || null
      const right = nextEntry || null
      if (!left && !right) return true
      if (!left || !right) return false
      return String(left.doctor_name || '') === String(right.doctor_name || '')
        && String(left.schedule_date || '') === String(right.schedule_date || '')
        && String(left.start_time || '') === String(right.start_time || '')
        && String(left.end_time || '') === String(right.end_time || '')
        && String(left.status || '') === String(right.status || '')
    },
    cleanupDrafts() {
      Object.keys(this.draftScheduleMap || {}).forEach(key => {
        const serverEntry = this.serverScheduleMap[key] || null
        const draftEntry = this.draftScheduleMap[key]
        if (this.scheduleEntriesEqual(serverEntry, draftEntry)) {
          this.$delete(this.draftScheduleMap, key)
        }
      })
    },
    loadDoctors() {
      axios.get('/accounts/doctors/active').then(response => {
        const list = Array.isArray(response.data.data) ? response.data.data : []
        this.allDoctors = list.map(this.normalizeDoctor).filter(Boolean)
          .sort((a, b) => String(a.name).localeCompare(String(b.name), 'zh-CN'))
      }).catch(() => { this.allDoctors = [] })
    },
    loadSchedules() {
      this.loading = true
      const startDate = `${this.selectedMonth}-01`
      const lastDay = new Date(this.monthMeta.year, this.monthMeta.month + 1, 0).getDate()
      const endDate = `${this.selectedMonth}-${String(lastDay).padStart(2, '0')}`
      axios.get(`/doctors/schedules?startDate=${startDate}&endDate=${endDate}`).then(response => {
        const list = Array.isArray(response.data.data) ? response.data.data : []
        this.scheduleEntries = list.map(normalizeScheduleRecord).filter(Boolean)
          .sort((a, b) => Number(a.id || 0) - Number(b.id || 0))
        this.cleanupDrafts()
      }).catch(error => {
        console.error('Error loading doctor schedules:', error)
        showApiError(this, '加载排班信息', error)
      }).finally(() => { this.loading = false })
    },
    loadAppointments() {
      // 加载预约数据用于关联展示
      axios.get('/appointments/scheduleEntries').then(response => {
        const list = Array.isArray(response.data.data) ? response.data.data : []
        const map = {}
        list.forEach(item => {
          if (!item || !item.appointment_date) return
          const date = normalizeDateText(item.appointment_date)
          const key = `${item.doctor_name || ''}|${date}`
          if (!map[key]) map[key] = 0
          map[key]++
          // 按日期统计
          if (!map[date]) map[date] = 0
          map[date]++
        })
        this.appointmentMap = map
      }).catch(() => { this.appointmentMap = {} })
    },
    loadTemplates() {
      axios.get('/doctors/shiftTemplates').then(response => {
        const list = Array.isArray(response.data.data) ? response.data.data : []
        this.shiftTemplates = list.map(t => ({
          id: t.id,
          name: t.name,
          pattern: typeof t.pattern_json === 'string' ? JSON.parse(t.pattern_json) : t.pattern_json
        }))
      }).catch(() => {
        // Mock 模板数据
        this.shiftTemplates = [
          { id: 1, name: '标准早班周', pattern: { '1': 'morning', '2': 'morning', '3': 'morning', '4': 'morning', '5': 'morning', '6': 'rest', '7': 'rest' } },
          { id: 2, name: '早晚轮班周', pattern: { '1': 'morning', '2': 'evening', '3': 'morning', '4': 'evening', '5': 'morning', '6': 'rest', '7': 'rest' } },
          { id: 3, name: '全晚班周', pattern: { '1': 'evening', '2': 'evening', '3': 'evening', '4': 'evening', '5': 'evening', '6': 'rest', '7': 'rest' } },
          { id: 4, name: '做二休一', pattern: { '1': 'morning', '2': 'morning', '3': 'rest', '4': 'evening', '5': 'evening', '6': 'rest', '7': 'morning' } }
        ]
      })
    },
    getCellAppointmentCount(doctorName, dateText) {
      const key = `${doctorName || ''}|${dateText}`
      return this.appointmentMap[key] || 0
    },
    getDateAppointmentCount(dateText) {
      return this.appointmentMap[dateText] || 0
    },
    checkConflict(doctorName, dateText) {
      // 简单冲突检测：同一医生同一天有多个排班记录（理论上不应发生）
      const key = scheduleKey(doctorName, dateText)
      const entries = (this.scheduleEntries || []).filter(e => scheduleKey(e.doctor_name, e.schedule_date) === key)
      return entries.length > 1
    },
    reloadAll() {
      this.loadSchedules()
      this.loadAppointments()
    },
    goPrevMonth() {
      const baseDate = new Date(this.monthMeta.year, this.monthMeta.month - 1, 1)
      this.selectedMonth = this.formatMonth(baseDate)
      this.loadSchedules()
    },
    goNextMonth() {
      const baseDate = new Date(this.monthMeta.year, this.monthMeta.month + 1, 1)
      this.selectedMonth = this.formatMonth(baseDate)
      this.loadSchedules()
    },
    shiftCellClass(cell) {
      return {
        [`tone-${cell.tone}`]: true,
        'is-today': cell.isToday,
        'is-weekend': cell.isWeekend,
        'is-dirty': cell.isDirty
      }
    },
    applyShiftToCell(cell) {
      // 如果当前是自定义班次，打开弹窗
      if (this.activeShift === SHIFT_CODE_CUSTOM) {
        this.customShiftForm = {
          doctorName: cell.doctor.name,
          scheduleDate: cell.dateText,
          startTime: cell.entry?.start_time || '09:00:00',
          endTime: cell.entry?.end_time || '18:00:00'
        }
        this.customShiftVisible = true
        return
      }

      const serverEntry = this.serverScheduleMap[cell.key] || null
      if (this.activeShift === CLEAR_SHIFT) {
        if (!serverEntry) {
          this.$delete(this.draftScheduleMap, cell.key)
          return
        }
        this.$set(this.draftScheduleMap, cell.key, null)
        return
      }
      const nextEntry = createShiftPayload({
        id: serverEntry ? serverEntry.id : null,
        doctorName: cell.doctor.name,
        scheduleDate: cell.dateText,
        shiftType: this.activeShift
      })
      if (this.scheduleEntriesEqual(serverEntry, nextEntry)) {
        this.$delete(this.draftScheduleMap, cell.key)
        return
      }
      this.$set(this.draftScheduleMap, cell.key, nextEntry)
    },
    applyShiftToRow(row) {
      // 整行设置当前班次
      row.cells.forEach(cell => {
        const serverEntry = this.serverScheduleMap[cell.key] || null
        if (this.activeShift === CLEAR_SHIFT) {
          if (serverEntry) this.$set(this.draftScheduleMap, cell.key, null)
          else this.$delete(this.draftScheduleMap, cell.key)
          return
        }
        const nextEntry = createShiftPayload({
          id: serverEntry ? serverEntry.id : null,
          doctorName: cell.doctor.name,
          scheduleDate: cell.dateText,
          shiftType: this.activeShift
        })
        if (this.scheduleEntriesEqual(serverEntry, nextEntry)) {
          this.$delete(this.draftScheduleMap, cell.key)
        } else {
          this.$set(this.draftScheduleMap, cell.key, nextEntry)
        }
      })
      this.$message.success(`已为 ${row.doctor.name} 设置整月${this.activeBrushText}`)
    },
    applyShiftToColumn(date) {
      // 整列设置当前班次
      this.scheduleRows.forEach(row => {
        const cell = row.cells.find(c => c.dateText === date.dateText)
        if (!cell) return
        const serverEntry = this.serverScheduleMap[cell.key] || null
        if (this.activeShift === CLEAR_SHIFT) {
          if (serverEntry) this.$set(this.draftScheduleMap, cell.key, null)
          else this.$delete(this.draftScheduleMap, cell.key)
          return
        }
        const nextEntry = createShiftPayload({
          id: serverEntry ? serverEntry.id : null,
          doctorName: cell.doctor.name,
          scheduleDate: cell.dateText,
          shiftType: this.activeShift
        })
        if (this.scheduleEntriesEqual(serverEntry, nextEntry)) {
          this.$delete(this.draftScheduleMap, cell.key)
        } else {
          this.$set(this.draftScheduleMap, cell.key, nextEntry)
        }
      })
      this.$message.success(`已为 ${date.day}日 全员设置${this.activeBrushText}`)
    },
    // 框选相关
    onMatrixMouseDown(e) {
      const td = e.target.closest('.matrix-td')
      if (!td) return
      const key = td.getAttribute('data-key')
      if (!key) return
      this.isMouseDown = true
      this.selectionStart = key
      this.selectionEnd = key
      this.updateSelectionRange()
    },
    onMatrixMouseMove(e) {
      if (!this.isMouseDown) return
      const td = e.target.closest('.matrix-td')
      if (!td) return
      const key = td.getAttribute('data-key')
      if (!key) return
      this.selectionEnd = key
      this.updateSelectionRange()
    },
    onMatrixMouseUp() {
      this.isMouseDown = false
    },
    onMatrixMouseLeave() {
      this.isMouseDown = false
    },
    updateSelectionRange() {
      // 根据 start 和 end key 计算框选范围
      const allKeys = []
      this.scheduleRows.forEach(row => {
        row.cells.forEach(cell => allKeys.push(cell.key))
      })
      const startIdx = allKeys.indexOf(this.selectionStart)
      const endIdx = allKeys.indexOf(this.selectionEnd)
      if (startIdx === -1 || endIdx === -1) {
        this.selectionRange = null
        return
      }
      // 计算行列索引
      const colCount = this.monthDates.length
      const startRow = Math.floor(startIdx / colCount)
      const startCol = startIdx % colCount
      const endRow = Math.floor(endIdx / colCount)
      const endCol = endIdx % colCount
      const minRow = Math.min(startRow, endRow)
      const maxRow = Math.max(startRow, endRow)
      const minCol = Math.min(startCol, endCol)
      const maxCol = Math.max(startCol, endCol)
      const selectedKeys = []
      for (let r = minRow; r <= maxRow; r++) {
        for (let c = minCol; c <= maxCol; c++) {
          const idx = r * colCount + c
          if (idx >= 0 && idx < allKeys.length) {
            selectedKeys.push(allKeys[idx])
          }
        }
      }
      this.selectionRange = { keys: selectedKeys, count: selectedKeys.length }
    },
    isCellSelected(cell) {
      return this.selectionRange && this.selectionRange.keys.includes(cell.key)
    },
    clearSelection() {
      this.selectionRange = null
      this.selectionStart = null
      this.selectionEnd = null
    },
    applyShiftToSelection() {
      if (!this.selectionRange || !this.selectionRange.keys.length) return
      this.selectionRange.keys.forEach(key => {
        // 找到对应的 cell
        let targetCell = null
        for (const row of this.scheduleRows) {
          const cell = row.cells.find(c => c.key === key)
          if (cell) { targetCell = cell; break }
        }
        if (!targetCell) return
        const serverEntry = this.serverScheduleMap[key] || null
        if (this.activeShift === CLEAR_SHIFT) {
          if (serverEntry) this.$set(this.draftScheduleMap, key, null)
          else this.$delete(this.draftScheduleMap, key)
          return
        }
        const nextEntry = createShiftPayload({
          id: serverEntry ? serverEntry.id : null,
          doctorName: targetCell.doctor.name,
          scheduleDate: targetCell.dateText,
          shiftType: this.activeShift
        })
        if (this.scheduleEntriesEqual(serverEntry, nextEntry)) {
          this.$delete(this.draftScheduleMap, key)
        } else {
          this.$set(this.draftScheduleMap, key, nextEntry)
        }
      })
      this.$message.success(`已批量设置 ${this.selectionRange.count} 个单元格`)
      this.clearSelection()
    },
    discardPendingChanges() {
      this.draftScheduleMap = {}
      this.clearSelection()
    },
    async saveSchedules() {
      const pendingKeys = Object.keys(this.draftScheduleMap || {})
      if (!pendingKeys.length) {
        this.$message.info('当前没有待保存的排班修改')
        return
      }
      this.saving = true
      const batchList = []
      const deleteIds = []
      pendingKeys.forEach(key => {
        const serverEntry = this.serverScheduleMap[key] || null
        const draftEntry = this.draftScheduleMap[key]
        if (draftEntry === null) {
          if (serverEntry && serverEntry.id) deleteIds.push(serverEntry.id)
        } else {
          batchList.push({ ...draftEntry, id: serverEntry ? serverEntry.id : null })
        }
      })
      try {
        if (deleteIds.length) {
          await axios.delete('/doctors/deleteBatch', { data: deleteIds })
        }
        if (batchList.length) {
          await axios.post('/doctors/batchSave', batchList)
        }
        this.draftScheduleMap = {}
        this.$message.success(`排班已保存，共更新 ${pendingKeys.length} 项`)
        await this.loadSchedules()
      } catch (error) {
        const msg = (error.response && error.response.data && error.response.data.msg) || '排班保存失败'
        this.$message.error(msg)
      } finally {
        this.saving = false
      }
    },
    // 自定义班次
    showCustomShiftDialog() {
      this.$message.info('请先点击矩阵中的单元格设置自定义班次')
    },
    confirmCustomShift() {
      const payload = createCustomShiftPayload({
        doctorName: this.customShiftForm.doctorName,
        scheduleDate: this.customShiftForm.scheduleDate,
        startTime: this.customShiftForm.startTime,
        endTime: this.customShiftForm.endTime
      })
      if (!payload) {
        this.$message.error('请填写完整的时间信息')
        return
      }
      const key = scheduleKey(this.customShiftForm.doctorName, this.customShiftForm.scheduleDate)
      const serverEntry = this.serverScheduleMap[key] || null
      if (this.scheduleEntriesEqual(serverEntry, payload)) {
        this.$delete(this.draftScheduleMap, key)
      } else {
        payload.id = serverEntry ? serverEntry.id : null
        this.$set(this.draftScheduleMap, key, payload)
      }
      this.customShiftVisible = false
      this.$message.success('自定义班次已设置')
    },
    // 模板
    showTemplateDialog() {
      this.templateDialogVisible = true
      this.newTemplateName = ''
    },
    applyTemplate(tpl) {
      this.scheduleRows.forEach(row => {
        const drafts = applyTemplateToMonth(tpl.pattern, this.monthDates, row.doctor)
        Object.keys(drafts).forEach(key => {
          const serverEntry = this.serverScheduleMap[key] || null
          const draftEntry = drafts[key]
          if (this.scheduleEntriesEqual(serverEntry, draftEntry)) {
            this.$delete(this.draftScheduleMap, key)
          } else {
            this.$set(this.draftScheduleMap, key, draftEntry)
          }
        })
      })
      this.templateDialogVisible = false
      this.$message.success(`已套用模板「${tpl.name}」到全部医生`)
    },
    saveAsTemplate() {
      if (!this.newTemplateName) return
      // 以第一个医生为基准生成模板
      if (!this.scheduleRows.length) return
      const pattern = buildTemplateFromCells(this.scheduleRows[0].cells)
      const tpl = { id: Date.now(), name: this.newTemplateName, pattern }
      this.shiftTemplates.push(tpl)
      this.newTemplateName = ''
      this.$message.success('模板已保存（本地）')
    },
    // AI
    openAiPanel() {
      this.aiPanelVisible = true
      this.aiSuggestion = null
    },
    generateAiSuggestion() {
      this.aiLoading = true
      // 模拟 AI 分析
      setTimeout(() => {
        const analysis = `本月共有 ${this.doctorCount} 名医生，${this.monthSummary.morning} 个早班、${this.monthSummary.evening} 个晚班。预约饱和度为 ${this.saturationRate}%。周末预约量偏低，建议减少周末排班或调整为弹性班次。`
        const advice = `1. ${this.overloadDoctors.length ? this.overloadDoctors.map(d => d.doctor.name).join('、') + ' 工作天数超过22天，建议安排调休。' : '目前医生负荷较均衡，建议保持。'}\n2. 周末早班利用率低，建议将部分早班调整为晚班或休息。\n3. 根据历史数据，周三、周四预约量最高，建议确保这两天全员在岗。`
        const changes = []
        // 生成一些随机建议调整
        this.scheduleRows.slice(0, 3).forEach(row => {
          const weekendCells = row.cells.filter(c => c.isWeekend && c.tone !== 'rest')
          if (weekendCells.length) {
            changes.push({
              date: weekendCells[0].dateText,
              doctor: row.doctor.name,
              toShift: '休息',
              toTone: 'rest'
            })
          }
        })
        this.aiSuggestion = { analysis, advice, changes }
        this.aiLoading = false
      }, 1500)
    },
    applyAiSuggestion() {
      if (!this.aiSuggestion || !this.aiSuggestion.changes) return
      this.aiSuggestion.changes.forEach(change => {
        const key = scheduleKey(change.doctor, change.date)
        const serverEntry = this.serverScheduleMap[key] || null
        const shiftType = change.toShift === '休息' ? SHIFT_CODE_REST : SHIFT_CODE_MORNING
        const nextEntry = createShiftPayload({
          id: serverEntry ? serverEntry.id : null,
          doctorName: change.doctor,
          scheduleDate: change.date,
          shiftType
        })
        if (this.scheduleEntriesEqual(serverEntry, nextEntry)) {
          this.$delete(this.draftScheduleMap, key)
        } else {
          this.$set(this.draftScheduleMap, key, nextEntry)
        }
      })
      this.aiPanelVisible = false
      this.$message.success('AI 建议已应用到排班草稿')
    },
    exportSchedule() {
      this.$message.info('导出功能开发中，即将支持 Excel/PDF 导出')
    },
    isAiEnabled(key) {
      return checkAiEnabled(key)
    }
  }
}
</script>

<style scoped>
.schedule-page {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding-bottom: 24px;
}

/* 页面头部 */
.page-header-shell {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 16px;
}

.page-header-left {
  flex: 1;
}

.page-kicker {
  color: #64748b;
  font-size: 13px;
  margin-bottom: 4px;
}

.page-title {
  margin: 0;
  color: #0f172a;
  font-size: 26px;
  font-weight: 800;
  line-height: 1.2;
}

.page-subtitle {
  margin: 6px 0 0;
  color: #94a3b8;
  font-size: 13px;
}

.page-header-right {
  display: flex;
  gap: 10px;
}

/* 工具栏 */
.toolbar-card {
  border-radius: 18px;
}

/deep/ .toolbar-card .el-card__body {
  padding: 16px 18px;
}

.toolbar-main {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.toolbar-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.month-picker {
  width: 160px;
}

.toolbar-divider {
  width: 1px;
  height: 28px;
  background: #e2e8f0;
}

.brush-group {
  display: flex;
  align-items: center;
  gap: 10px;
}

.toolbar-label {
  color: #475569;
  font-size: 13px;
  font-weight: 600;
  white-space: nowrap;
}

.batch-group {
  display: flex;
  gap: 8px;
}

.toolbar-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: auto;
}

.toolbar-selection-hint {
  margin-top: 12px;
  padding: 10px 14px;
  background: #eff6ff;
  border-radius: 12px;
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 13px;
  color: #1e40af;
}

/* 统计摘要 */
.summary-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 12px;
}

.summary-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 18px;
  border-radius: 18px;
  background: #fff;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.06);
  border: 1px solid #e5e7eb;
  position: relative;
  overflow: hidden;
}

.summary-icon {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.summary-card.total .summary-icon {
  background: linear-gradient(135deg, #dbeafe, #bfdbfe);
  color: #2563eb;
}

.summary-card.morning .summary-icon {
  background: linear-gradient(135deg, #fef3c7, #fde68a);
  color: #d97706;
}

.summary-card.evening .summary-icon {
  background: linear-gradient(135deg, #e0e7ff, #c7d2fe);
  color: #4f46e5;
}

.summary-card.rest .summary-icon {
  background: linear-gradient(135deg, #e2e8f0, #cbd5e1);
  color: #475569;
}

.summary-card.saturation .summary-icon {
  background: linear-gradient(135deg, #d1fae5, #a7f3d0);
  color: #059669;
}

.summary-card.warning .summary-icon {
  background: linear-gradient(135deg, #fee2e2, #fecaca);
  color: #dc2626;
}

.summary-body {
  flex: 1;
  min-width: 0;
}

.summary-value {
  color: #0f172a;
  font-size: 24px;
  font-weight: 800;
  line-height: 1;
}

.summary-label {
  margin-top: 6px;
  color: #64748b;
  font-size: 12px;
}

.summary-progress {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
}

.summary-progress /deep/ .el-progress-bar {
  padding-right: 0;
}

.summary-progress /deep/ .el-progress-bar__outer {
  border-radius: 0;
  background: transparent;
}

.summary-progress /deep/ .el-progress-bar__inner {
  border-radius: 0;
  height: 3px !important;
}

/* 矩阵卡片 */
.matrix-card {
  border-radius: 18px;
}

/deep/ .matrix-card .el-card__body {
  padding: 20px;
}

.matrix-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 20px;
}

.matrix-title {
  font-size: 18px;
  color: #0f172a;
  font-weight: 800;
}

.matrix-subtitle {
  margin-top: 8px;
  color: #64748b;
  font-size: 13px;
  line-height: 1.6;
}

.active-brush-text {
  margin-left: 8px;
  color: #2563eb;
  font-weight: 600;
}

.matrix-legend {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  flex-shrink: 0;
}

.legend-chip {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 72px;
  padding: 0 12px;
  line-height: 30px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.legend-chip.morning {
  background: #fef3c7;
  color: #b45309;
}

.legend-chip.evening {
  background: #e0e7ff;
  color: #4338ca;
}

.legend-chip.rest {
  background: #e2e8f0;
  color: #475569;
}

.legend-chip.custom {
  background: #f3e8ff;
  color: #7c3aed;
}

.legend-chip.empty {
  background: #f1f5f9;
  color: #94a3b8;
}

/* 矩阵表格 */
.matrix-scroll {
  overflow: auto;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  background: #fff;
  user-select: none;
}

.matrix-table {
  width: max-content;
  min-width: 100%;
  border-collapse: separate;
  border-spacing: 0;
}

.doctor-header,
.date-header,
.doctor-cell,
.matrix-td {
  border-right: 1px solid #f1f5f9;
  border-bottom: 1px solid #f1f5f9;
}

.doctor-header,
.date-header {
  position: sticky;
  top: 0;
  background: #f8fafc;
  z-index: 3;
}

.doctor-header {
  left: 0;
  z-index: 5;
  min-width: 200px;
  padding: 14px 16px;
  text-align: left;
}

.doctor-header-content {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #0f172a;
  font-size: 14px;
  font-weight: 700;
}

.date-header {
  min-width: 90px;
  padding: 10px 6px;
  text-align: center;
  cursor: pointer;
  transition: background-color 0.2s;
}

.date-header:hover {
  background: #eff6ff;
}

.date-header.is-weekend {
  background: #f8fafc;
}

.date-header.is-today {
  background: #eff6ff;
}

.date-header__day {
  color: #0f172a;
  font-size: 16px;
  font-weight: 800;
  line-height: 1;
}

.date-header__meta {
  margin-top: 6px;
  color: #64748b;
  font-size: 11px;
}

.date-header__badge {
  margin-top: 4px;
  font-size: 10px;
  color: #2563eb;
  background: #dbeafe;
  border-radius: 999px;
  padding: 1px 6px;
  display: inline-block;
}

.doctor-cell {
  position: sticky;
  left: 0;
  z-index: 4;
  min-width: 200px;
  padding: 12px 14px;
  background: #fff;
  text-align: left;
  cursor: pointer;
  transition: background-color 0.2s;
  display: flex;
  align-items: center;
  gap: 12px;
}

.doctor-cell:hover {
  background: #f8fafc;
}

.doctor-cell__avatar {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: linear-gradient(135deg, #2563eb, #3b82f6);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 700;
  flex-shrink: 0;
}

.doctor-cell__info {
  flex: 1;
  min-width: 0;
}

.doctor-cell__name {
  color: #0f172a;
  font-size: 14px;
  font-weight: 700;
  line-height: 1.2;
}

.doctor-cell__meta {
  margin-top: 6px;
  display: flex;
  gap: 8px;
  color: #64748b;
  font-size: 11px;
}

.doctor-cell__meta span {
  background: #f1f5f9;
  padding: 2px 6px;
  border-radius: 4px;
}

.doctor-cell__appointments {
  margin-top: 4px;
  color: #2563eb;
  font-size: 11px;
}

.matrix-table tr:last-child .doctor-cell,
.matrix-table tr:last-child .matrix-td {
  border-bottom: none;
}

.matrix-table tr > *:last-child {
  border-right: none;
}

.matrix-td {
  padding: 0;
  background: #fff;
  transition: background-color 0.15s;
}

.matrix-td.is-weekend {
  background: #fcfcfd;
}

.matrix-td.is-today {
  background: #f8fbff;
}

.matrix-td.is-selected {
  background: #dbeafe !important;
  box-shadow: inset 0 0 0 2px #2563eb;
}

.matrix-td.has-conflict {
  box-shadow: inset 0 0 0 2px #ef4444;
}

.shift-cell {
  position: relative;
  width: 100%;
  min-height: 72px;
  padding: 10px 8px;
  border: none;
  background: transparent;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  transition: transform 0.18s ease, box-shadow 0.18s ease, background-color 0.18s ease;
}

.shift-cell:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  z-index: 2;
}

.shift-cell.is-dirty {
  box-shadow: inset 0 0 0 2px #f59e0b;
}

.shift-cell__label {
  font-size: 13px;
  font-weight: 800;
  line-height: 1.2;
}

.shift-cell__time {
  font-size: 11px;
  line-height: 1.4;
  color: #64748b;
  text-align: center;
}

.shift-cell__badge {
  position: absolute;
  top: 4px;
  right: 4px;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 999px;
  background: #ef4444;
  color: #fff;
  font-size: 10px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.shift-cell__draft {
  position: absolute;
  top: 4px;
  left: 4px;
  width: 18px;
  height: 18px;
  border-radius: 999px;
  background: #f59e0b;
  color: #fff;
  font-size: 10px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

/* 班次色调 */
.shift-cell.tone-morning {
  background: linear-gradient(180deg, #fffbeb 0%, #fef3c7 100%);
}

.shift-cell.tone-morning .shift-cell__label {
  color: #b45309;
}

.shift-cell.tone-evening {
  background: linear-gradient(180deg, #eef2ff 0%, #e0e7ff 100%);
}

.shift-cell.tone-evening .shift-cell__label {
  color: #4338ca;
}

.shift-cell.tone-rest {
  background: linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%);
}

.shift-cell.tone-rest .shift-cell__label {
  color: #64748b;
}

.shift-cell.tone-empty {
  background: #fff;
}

.shift-cell.tone-empty .shift-cell__label {
  color: #94a3b8;
}

.shift-cell.tone-empty:hover {
  background: #f8fafc;
}

.shift-cell.tone-custom {
  background: linear-gradient(180deg, #faf5ff 0%, #f3e8ff 100%);
}

.shift-cell.tone-custom .shift-cell__label {
  color: #7c3aed;
}

/* 模板弹窗 */
.template-dialog-body {
  padding: 10px 0;
}

.template-section h4 {
  margin: 0 0 14px;
  color: #0f172a;
  font-size: 15px;
}

.template-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.template-item {
  padding: 14px 16px;
  border-radius: 14px;
  border: 1px solid #e2e8f0;
  cursor: pointer;
  transition: all 0.2s;
}

.template-item:hover {
  border-color: #2563eb;
  background: #f8fbff;
  transform: translateX(4px);
}

.template-name {
  font-size: 14px;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 8px;
}

.template-pattern {
  display: flex;
  gap: 6px;
}

.pattern-dot {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 600;
}

.pattern-dot.tone-morning {
  background: #fef3c7;
  color: #b45309;
}

.pattern-dot.tone-evening {
  background: #e0e7ff;
  color: #4338ca;
}

.pattern-dot.tone-rest {
  background: #e2e8f0;
  color: #647569;
}

/* AI 面板 */
.ai-panel-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(15, 23, 42, 0.4);
  z-index: 2000;
  display: flex;
  justify-content: flex-end;
}

.ai-panel {
  width: 480px;
  height: 100%;
  background: #fff;
  box-shadow: -8px 0 32px rgba(15, 23, 42, 0.12);
  display: flex;
  flex-direction: column;
}

.ai-panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #e2e8f0;
  font-size: 18px;
  font-weight: 800;
  color: #0f172a;
}

.ai-panel-header i {
  color: #7c3aed;
  margin-right: 8px;
  font-size: 22px;
}

.ai-panel-body {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

.ai-empty {
  text-align: center;
  padding: 60px 20px;
  color: #64748b;
}

.ai-empty i {
  font-size: 48px;
  color: #c4b5fd;
  margin-bottom: 16px;
}

.ai-insight-card {
  padding: 16px;
  border-radius: 14px;
  background: #f8fafc;
  margin-bottom: 14px;
}

.ai-insight-title {
  font-size: 14px;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 8px;
}

.ai-insight-text {
  font-size: 13px;
  color: #475569;
  line-height: 1.7;
  white-space: pre-line;
}

.ai-change-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.ai-change-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #fff;
  border-radius: 10px;
  font-size: 13px;
}

.ai-change-date {
  color: #64748b;
  font-weight: 600;
  min-width: 90px;
}

.ai-change-doctor {
  color: #0f172a;
  font-weight: 700;
  min-width: 80px;
}

.ai-change-arrow {
  color: #94a3b8;
}

.ai-change-shift {
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.ai-change-shift.tone-rest {
  background: #e2e8f0;
  color: #475569;
}

.ai-actions {
  display: flex;
  gap: 10px;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #e2e8f0;
}

/* 过渡动画 */
.ai-panel-enter-active,
.ai-panel-leave-active {
  transition: opacity 0.3s;
}

.ai-panel-enter-active .ai-panel,
.ai-panel-leave-active .ai-panel {
  transition: transform 0.3s ease;
}

.ai-panel-enter .ai-panel,
.ai-panel-leave-to .ai-panel {
  transform: translateX(100%);
}

.ai-panel-enter-to .ai-panel,
.ai-panel-leave .ai-panel {
  transform: translateX(0);
}

/* 响应式 */
@media (max-width: 1280px) {
  .summary-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 992px) {
  .page-header-shell,
  .matrix-head,
  .toolbar-main {
    flex-direction: column;
    align-items: flex-start;
  }

  .toolbar-actions {
    margin-left: 0;
    width: 100%;
    justify-content: flex-end;
  }

  .summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .ai-panel {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .summary-grid {
    grid-template-columns: 1fr;
  }

  .doctor-header,
  .doctor-cell {
    min-width: 160px;
  }

  .date-header {
    min-width: 68px;
  }

  .shift-cell {
    min-height: 60px;
    padding: 6px 4px;
  }
}
</style>
