<template>
  <div class="schedule-page">
    <div class="page-title-bar">
      <div>
        <div class="page-kicker">排班管理</div>
        <h2>月度排班设置</h2>
      </div>
      <div class="page-desc">横向按当月日期，纵向按医生，在同一界面完成所有医生排班。</div>
    </div>

    <el-card class="toolbar-card" shadow="never">
      <div class="toolbar-row">
        <el-button icon="el-icon-arrow-left" @click="goPrevMonth">上月</el-button>
        <el-date-picker
          v-model="selectedMonth"
          type="month"
          value-format="yyyy-MM"
          format="yyyy-MM"
          class="month-picker"
          placeholder="选择月份"
        ></el-date-picker>
        <el-button icon="el-icon-arrow-right" @click="goNextMonth">下月</el-button>

        <el-button type="primary" :loading="saving" :disabled="!hasPendingChanges" @click="saveSchedules">保存排班</el-button>
        <el-button :disabled="!hasPendingChanges" @click="discardPendingChanges">撤销修改</el-button>
        <el-button icon="el-icon-refresh" :loading="loading" @click="loadSchedules">刷新</el-button>
      </div>

      <div class="toolbar-row toolbar-row--secondary">
        <div class="brush-group">
          <span class="toolbar-label">当前班次</span>
          <el-radio-group v-model="activeShift" size="small">
            <el-radio-button
              v-for="option in brushOptions"
              :key="option.value"
              :label="option.value"
            >{{ option.label }}</el-radio-button>
          </el-radio-group>
        </div>

        <div class="toolbar-hint">
          <span>点击任意单元格即可套用当前班次</span>
          <span>医生数：{{ doctorCount }}</span>
          <span v-if="hasPendingChanges" class="toolbar-hint__warn">待保存 {{ pendingChangeCount }} 项</span>
        </div>
      </div>
    </el-card>

    <div class="summary-grid">
      <div class="summary-card total">
        <div class="summary-label">排班医生</div>
        <div class="summary-value">{{ doctorCount }}</div>
        <div class="summary-meta">{{ monthTitle }}</div>
      </div>
      <div class="summary-card morning">
        <div class="summary-label">早班</div>
        <div class="summary-value">{{ monthSummary.morning }}</div>
        <div class="summary-meta">09:00-18:00</div>
      </div>
      <div class="summary-card evening">
        <div class="summary-label">晚班</div>
        <div class="summary-value">{{ monthSummary.evening }}</div>
        <div class="summary-meta">13:00-21:00</div>
      </div>
      <div class="summary-card rest">
        <div class="summary-label">休息</div>
        <div class="summary-value">{{ monthSummary.rest }}</div>
        <div class="summary-meta">全天休息</div>
      </div>
      <div class="summary-card empty">
        <div class="summary-label">未排班</div>
        <div class="summary-value">{{ monthSummary.empty }}</div>
        <div class="summary-meta">待补排班</div>
      </div>
    </div>

    <el-card class="matrix-card" shadow="never" v-loading="loading || saving">
      <template v-if="allDoctors.length">
        <div class="matrix-head">
          <div>
            <div class="matrix-title">{{ monthTitle }} 全员排班</div>
            <div class="matrix-subtitle">
              早班为 09:00-18:00，晚班为 13:00-21:00，休息为全天不可预约。
              <span v-if="activeBrushText">当前点击将设置为{{ activeBrushText }}</span>
            </div>
          </div>
          <div class="matrix-legend">
            <span class="legend-chip morning">早班</span>
            <span class="legend-chip evening">晚班</span>
            <span class="legend-chip rest">休息</span>
            <span class="legend-chip empty">未排班</span>
          </div>
        </div>

        <div class="matrix-scroll">
          <table class="matrix-table">
            <thead>
              <tr>
                <th class="doctor-header">医生</th>
                <th
                  v-for="date in monthDates"
                  :key="date.dateText"
                  class="date-header"
                  :class="{ 'is-today': date.isToday, 'is-weekend': date.isWeekend }"
                >
                  <div class="date-header__day">{{ date.day }}</div>
                  <div class="date-header__meta">{{ date.weekLabel }}</div>
                </th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in scheduleRows" :key="row.doctor.id">
                <th class="doctor-cell">
                  <div class="doctor-cell__name">{{ row.doctor.name }}</div>
                  <div class="doctor-cell__meta">{{ row.summaryText }}</div>
                </th>

                <td
                  v-for="cell in row.cells"
                  :key="cell.key"
                  class="matrix-td"
                  :class="{ 'is-today': cell.isToday, 'is-weekend': cell.isWeekend }"
                >
                  <button
                    type="button"
                    class="shift-cell"
                    :class="shiftCellClass(cell)"
                    @click="applyShiftToCell(cell)"
                  >
                    <span class="shift-cell__label">{{ cell.displayLabel }}</span>
                    <span class="shift-cell__time">{{ cell.timeText }}</span>
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
  </div>
</template>

<script>
import axios from 'axios'
import { showApiError } from '@/utils/errorMessage'
import {
  SHIFT_CODE_EVENING,
  SHIFT_CODE_MORNING,
  SHIFT_CODE_NONE,
  SHIFT_CODE_REST,
  SHIFT_OPTIONS,
  createShiftPayload,
  normalizeDateText,
  normalizeScheduleRecord,
  scheduleDisplayLabel,
  scheduleKey,
  scheduleTimeDescription,
  scheduleTone
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
      saving: false
    }
  },
  computed: {
    brushOptions() {
      return [
        { value: SHIFT_CODE_MORNING, label: '早班 09:00-18:00' },
        { value: SHIFT_CODE_EVENING, label: '晚班 13:00-21:00' },
        { value: SHIFT_CODE_REST, label: '休息' },
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
        list.push({
          day,
          dateText: normalizeDateText(date),
          weekLabel: WEEK_LABELS[weekDay],
          isToday: normalizeDateText(date) === normalizeDateText(new Date()),
          isWeekend: weekDay === 0 || weekDay === 6
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
          return {
            key,
            doctor,
            dateText: date.dateText,
            day: date.day,
            isToday: date.isToday,
            isWeekend: date.isWeekend,
            entry,
            displayLabel,
            timeText: entry ? scheduleTimeDescription(entry) : '点击设置班次',
            tone,
            isDirty: Object.prototype.hasOwnProperty.call(this.draftScheduleMap, key)
          }
        })
        const summary = this.summarizeCells(cells)
        return {
          doctor,
          cells,
          summary,
          summaryText: `早${summary.morning} / 晚${summary.evening} / 休${summary.rest} / 未${summary.empty}`
        }
      })
    },
    monthSummary() {
      const summary = {
        morning: 0,
        evening: 0,
        rest: 0,
        empty: 0
      }
      ;(this.scheduleRows || []).forEach(row => {
        summary.morning += row.summary.morning
        summary.evening += row.summary.evening
        summary.rest += row.summary.rest
        summary.empty += row.summary.empty
      })
      return summary
    }
  },
  mounted() {
    this.loadDoctors()
    this.loadSchedules()
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
      return {
        id: Number(item.id),
        name: String(item.name).trim()
      }
    },
    summarizeCells(cells) {
      const summary = {
        morning: 0,
        evening: 0,
        rest: 0,
        empty: 0
      }
      ;(cells || []).forEach(cell => {
        if (cell.displayLabel === SHIFT_CODE_MORNING) {
          summary.morning += 1
        } else if (cell.displayLabel === SHIFT_CODE_EVENING) {
          summary.evening += 1
        } else if (cell.displayLabel === SHIFT_CODE_REST) {
          summary.rest += 1
        } else {
          summary.empty += 1
        }
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
        this.allDoctors = list
          .map(this.normalizeDoctor)
          .filter(Boolean)
          .sort((a, b) => String(a.name).localeCompare(String(b.name), 'zh-CN'))
      }).catch(() => {
        this.allDoctors = []
      })
    },
    loadSchedules() {
      this.loading = true
      axios.get('/doctors/scheduleEntries').then(response => {
        const list = Array.isArray(response.data.data) ? response.data.data : []
        this.scheduleEntries = list
          .map(normalizeScheduleRecord)
          .filter(Boolean)
          .sort((a, b) => Number(a.id || 0) - Number(b.id || 0))
        this.cleanupDrafts()
      }).catch(error => {
        console.error('Error loading doctor schedules:', error)
        showApiError(this, '加载排班信息', error)
      }).finally(() => {
        this.loading = false
      })
    },
    goPrevMonth() {
      const baseDate = new Date(this.monthMeta.year, this.monthMeta.month - 1, 1)
      this.selectedMonth = this.formatMonth(baseDate)
    },
    goNextMonth() {
      const baseDate = new Date(this.monthMeta.year, this.monthMeta.month + 1, 1)
      this.selectedMonth = this.formatMonth(baseDate)
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
    discardPendingChanges() {
      this.draftScheduleMap = {}
    },
    async saveSchedules() {
      const pendingKeys = Object.keys(this.draftScheduleMap || {})
      if (!pendingKeys.length) {
        this.$message.info('当前没有待保存的排班修改')
        return
      }
      this.saving = true
      let failedMessage = ''
      for (const key of pendingKeys) {
        const serverEntry = this.serverScheduleMap[key] || null
        const draftEntry = this.draftScheduleMap[key]
        try {
          if (draftEntry === null) {
            if (serverEntry && serverEntry.id) {
              await axios.delete(`/doctors/delete/${serverEntry.id}`)
            }
            continue
          }
          if (serverEntry && serverEntry.id) {
            await axios.put('/doctors/edit', {
              ...draftEntry,
              id: serverEntry.id
            })
          } else {
            await axios.post('/doctors/add', draftEntry)
          }
        } catch (error) {
          failedMessage = (error.response && error.response.data && error.response.data.msg) || '排班保存失败'
          break
        }
      }

      await this.loadSchedules()
      if (!failedMessage) {
        this.draftScheduleMap = {}
        this.$message.success(`排班已保存，共更新 ${pendingKeys.length} 项`)
      } else {
        this.$message.error(failedMessage)
      }
      this.saving = false
    }
  }
}
</script>

<style scoped>
.schedule-page {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.page-title-bar {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
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
  font-size: 24px;
}

.page-desc {
  color: #64748b;
  font-size: 13px;
}

.toolbar-card,
.matrix-card {
  border-radius: 18px;
}

/deep/ .toolbar-card .el-card__body,
/deep/ .matrix-card .el-card__body {
  padding: 16px 18px;
}

.toolbar-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}

.toolbar-row--secondary {
  margin-top: 14px;
  justify-content: space-between;
}

.month-picker {
  width: 180px;
}

.brush-group {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
}

.toolbar-label {
  color: #475569;
  font-size: 13px;
  font-weight: 600;
}

.toolbar-hint {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  color: #64748b;
  font-size: 12px;
}

.toolbar-hint__warn {
  color: #c2410c;
  font-weight: 700;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 12px;
}

.summary-card {
  padding: 16px 18px;
  border-radius: 18px;
  background: #fff;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.06);
  border: 1px solid #e5e7eb;
}

.summary-card.total {
  background: linear-gradient(180deg, #f8fbff 0%, #eef4ff 100%);
}

.summary-card.morning {
  background: linear-gradient(180deg, #eff6ff 0%, #dbeafe 100%);
}

.summary-card.evening {
  background: linear-gradient(180deg, #fff7ed 0%, #fed7aa 100%);
}

.summary-card.rest {
  background: linear-gradient(180deg, #f8fafc 0%, #e2e8f0 100%);
}

.summary-card.empty {
  background: linear-gradient(180deg, #fafafa 0%, #f1f5f9 100%);
}

.summary-label {
  color: #475569;
  font-size: 13px;
}

.summary-value {
  margin-top: 10px;
  color: #0f172a;
  font-size: 30px;
  font-weight: 800;
  line-height: 1;
}

.summary-meta {
  margin-top: 10px;
  color: #64748b;
  font-size: 12px;
}

.matrix-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 16px;
}

.matrix-title {
  font-size: 18px;
  color: #0f172a;
  font-weight: 800;
}

.matrix-subtitle {
  margin-top: 6px;
  color: #64748b;
  font-size: 13px;
  line-height: 1.7;
}

.matrix-legend {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.legend-chip {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 72px;
  padding: 0 10px;
  line-height: 28px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.legend-chip.morning {
  background: #dbeafe;
  color: #1d4ed8;
}

.legend-chip.evening {
  background: #ffedd5;
  color: #c2410c;
}

.legend-chip.rest {
  background: #e2e8f0;
  color: #334155;
}

.legend-chip.empty {
  background: #f1f5f9;
  color: #64748b;
}

.matrix-scroll {
  overflow: auto;
  border: 1px solid #dbe3ee;
  border-radius: 18px;
  background: #fff;
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
  border-right: 1px solid #e5e7eb;
  border-bottom: 1px solid #e5e7eb;
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
  color: #0f172a;
  font-size: 14px;
}

.date-header {
  min-width: 88px;
  padding: 10px 6px;
  text-align: center;
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

.doctor-cell {
  position: sticky;
  left: 0;
  z-index: 4;
  min-width: 200px;
  padding: 14px 16px;
  background: #fff;
  text-align: left;
}

.doctor-cell__name {
  color: #0f172a;
  font-size: 14px;
  font-weight: 800;
  line-height: 1.2;
}

.doctor-cell__meta {
  margin-top: 8px;
  color: #64748b;
  font-size: 12px;
  line-height: 1.5;
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
}

.matrix-td.is-weekend {
  background: #fcfcfd;
}

.matrix-td.is-today {
  background: #f8fbff;
}

.shift-cell {
  position: relative;
  width: 100%;
  min-height: 74px;
  padding: 10px 8px;
  border: none;
  background: transparent;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  transition: transform 0.18s ease, box-shadow 0.18s ease, background-color 0.18s ease;
}

.shift-cell:hover {
  transform: translateY(-1px);
  box-shadow: inset 0 0 0 999px rgba(15, 23, 42, 0.02);
}

.shift-cell.is-dirty {
  box-shadow: inset 0 0 0 2px rgba(245, 158, 11, 0.35);
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

.shift-cell__draft {
  position: absolute;
  top: 6px;
  right: 6px;
  width: 20px;
  height: 20px;
  border-radius: 999px;
  background: #fff7ed;
  color: #c2410c;
  font-size: 11px;
  font-weight: 800;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.shift-cell.tone-morning {
  background: linear-gradient(180deg, #f8fbff 0%, #eff6ff 100%);
}

.shift-cell.tone-morning .shift-cell__label {
  color: #1d4ed8;
}

.shift-cell.tone-evening {
  background: linear-gradient(180deg, #fffaf5 0%, #fff7ed 100%);
}

.shift-cell.tone-evening .shift-cell__label {
  color: #c2410c;
}

.shift-cell.tone-rest {
  background: linear-gradient(180deg, #f8fafc 0%, #eef2f7 100%);
}

.shift-cell.tone-rest .shift-cell__label {
  color: #334155;
}

.shift-cell.tone-empty {
  background: #fff;
}

.shift-cell.tone-empty .shift-cell__label {
  color: #64748b;
}

.shift-cell.tone-custom {
  background: linear-gradient(180deg, #f8fafc 0%, #eef2ff 100%);
}

.shift-cell.tone-custom .shift-cell__label {
  color: #4338ca;
}

@media (max-width: 1280px) {
  .summary-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 992px) {
  .page-title-bar,
  .matrix-head,
  .toolbar-row--secondary {
    flex-direction: column;
    align-items: flex-start;
  }

  .summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .summary-grid {
    grid-template-columns: 1fr;
  }

  .doctor-header,
  .doctor-cell {
    min-width: 148px;
  }

  .date-header {
    min-width: 76px;
  }

  .shift-cell {
    min-height: 68px;
    padding: 8px 6px;
  }
}
</style>
