<template>
  <div class="staff-h5-page appointment-h5-page">
    <div class="h5-hero-card">
      <div>
        <div class="h5-page-kicker">员工微信 H5</div>
        <h2>今日预约</h2>
        <p>{{ selectedDate }} · 适合微信内快速查看今日排班与预约详情</p>
      </div>
      <div class="hero-actions">
        <el-date-picker
          v-model="selectedDate"
          type="date"
          value-format="yyyy-MM-dd"
          format="yyyy-MM-dd"
          placeholder="选择日期"
          size="small"
          @change="loadAppointments"
        />
        <el-select v-model="selectedDoctor" size="small" placeholder="医生筛选" clearable @change="applyDoctorFilter">
          <el-option label="全部医生" value="ALL"></el-option>
          <el-option v-for="doctor in allDoctors" :key="doctor" :label="doctor" :value="doctor"></el-option>
        </el-select>
      </div>
    </div>

    <div class="h5-summary-row">
      <div class="h5-summary-card">
        <div class="summary-num">{{ visibleAppointments.length }}</div>
        <div class="summary-label">当日预约</div>
      </div>
      <div class="h5-summary-card accent">
        <div class="summary-num">{{ allDoctors.length }}</div>
        <div class="summary-label">排班医生</div>
      </div>
      <div class="h5-summary-card success">
        <div class="summary-num">{{ visibleAppointments.filter(item => waitingStatus(item.status)).length }}</div>
        <div class="summary-label">未就诊</div>
      </div>
    </div>

    <div class="h5-section-card">
      <div class="section-title">预约列表</div>
      <div v-if="visibleAppointments.length" class="appointment-card-list">
        <div v-for="item in visibleAppointments" :key="item.id" class="appointment-card" @click="openDetail(item)">
          <div class="appointment-card__top">
            <div>
              <div class="appointment-card__name">{{ item.patient_name || '未填写患者' }}</div>
              <div class="appointment-card__meta">{{ formatTime(item.appointment_time) }} · {{ item.doctor_name || '未指定医生' }}</div>
            </div>
            <el-tag size="mini" :type="statusType(item.status)">{{ item.status || '未知' }}</el-tag>
          </div>
          <div class="appointment-card__purpose">{{ item.appointment_purpose || '未填写预约目的' }}</div>
        </div>
      </div>
      <el-empty v-else description="当天暂无预约"></el-empty>
    </div>

    <el-drawer :visible.sync="detailVisible" size="92%" direction="btt" custom-class="appointment-detail-drawer">
      <div v-if="detailItem.id" class="detail-sheet">
        <div class="detail-title">预约详情</div>
        <div class="detail-row"><span>患者</span><strong>{{ detailItem.patient_name || '-' }}</strong></div>
        <div class="detail-row"><span>时间</span><strong>{{ selectedDate }} {{ formatTime(detailItem.appointment_time) }}</strong></div>
        <div class="detail-row"><span>医生</span><strong>{{ detailItem.doctor_name || '-' }}</strong></div>
        <div class="detail-row"><span>状态</span><strong>{{ detailItem.status || '-' }}</strong></div>
        <div class="detail-row detail-row--block"><span>预约目的</span><strong>{{ detailItem.appointment_purpose || '未填写' }}</strong></div>
        <div class="detail-actions">
          <el-button type="primary" @click="goPatient360" :disabled="!resolveDetailPatientId()">进入患者360</el-button>
          <el-button type="warning" plain @click="goEditAppointment" :disabled="!resolveDetailPatientId()">编辑预约</el-button>
          <el-button type="primary" @click="goPatientList">查看患者列表</el-button>
          <el-button plain @click="detailVisible = false">关闭</el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import axios from 'axios'
import { getStaffPortalQuery, saveStaffPortalSessionFromQuery } from '@/utils/portalSession'
import { fetchCachedResource } from '@/utils/offline/apiClient'

export default {
  name: 'StaffAppointmentH5',
  data() {
    return {
      selectedDate: '',
      selectedDoctor: 'ALL',
      allDoctors: [],
      appointments: [],
      visibleAppointments: [],
      detailVisible: false,
      detailItem: {}
    }
  },
  mounted() {
    saveStaffPortalSessionFromQuery(this.$route.query)
    this.selectedDate = this.formatDate(new Date())
    this.loadAppointments()
  },
  methods: {
    formatDate(date) {
      const d = new Date(date)
      if (Number.isNaN(d.getTime())) return ''
      const y = d.getFullYear()
      const m = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      return `${y}-${m}-${day}`
    },
    formatTime(value) {
      const text = String(value || '').trim()
      if (!text) return '--:--'
      return text.length >= 5 ? text.slice(0, 5) : text
    },
    statusType(status) {
      if (status === '已取消') return 'danger'
      if (status === '已治疗' || status === '已完成' || status === '已就诊') return 'success'
      if (status === '已离开') return 'info'
      if (status === '已改约') return 'warning'
      return 'primary'
    },
    waitingStatus(status) {
      return ['待治疗', '已预约', '待就诊'].includes(String(status || '').trim())
    },
    loadAppointments() {
      const query = getStaffPortalQuery(this.$route.query)
      Promise.all([
        fetchCachedResource({
          cacheKey: 'page:staff:overview',
          scope: '',
          url: '/staff-portal/overview',
          params: {
            accountId: query.accountId,
            staffToken: query.staffToken
          },
          loader: () => axios.get('/staff-portal/overview', {
            params: {
              accountId: query.accountId,
              staffToken: query.staffToken
            }
          })
        }),
        fetchCachedResource({
          cacheKey: 'page:staff:appointments:all',
          scope: 'appointmentsH5',
          url: '/appointments/selectAll',
          params: { page: 1, size: 1000 },
          loader: () => axios.get('/appointments/selectAll', { params: { page: 1, size: 1000 } }),
          notifier: message => this.$message.warning(message)
        }),
        fetchCachedResource({
          cacheKey: 'ref:doctors-active',
          scope: '',
          url: '/accounts/doctors/active',
          loader: () => axios.get('/accounts/doctors/active')
        }).catch(() => ({ data: [] }))
      ]).then(([staffRes, response, doctorsRes]) => {
        const overview = staffRes && staffRes.data ? staffRes.data : {}
        const doctorName = overview && overview.summary && overview.summary.doctorName
          ? overview.summary.doctorName
          : ''
        const data = response && response.data ? response.data : {}
        const list = data.list || []
        const activeDoctorNames = (Array.isArray(doctorsRes && doctorsRes.data) ? doctorsRes.data : [])
          .map(item => String((item && item.name) || '').trim())
          .filter(Boolean)
          const filteredByDate = (list || []).filter(item => item.appointment_date === this.selectedDate)
        this.appointments = doctorName
            ? filteredByDate.filter(item => (item.doctor_name || '') === doctorName)
            : filteredByDate
        this.allDoctors = doctorName
          ? [doctorName]
          : (activeDoctorNames.length ? activeDoctorNames : Array.from(new Set(this.appointments.map(item => item.doctor_name || '未指定医生'))))
        this.applyDoctorFilter()
      }).catch(error => {
        console.error('Error fetching appointments:', error)
      })
    },
    applyDoctorFilter() {
      if (!this.selectedDoctor || this.selectedDoctor === 'ALL') {
        this.visibleAppointments = this.appointments.slice().sort((a, b) => String(a.appointment_time || '').localeCompare(String(b.appointment_time || '')))
        return
      }
      this.visibleAppointments = this.appointments
        .filter(item => (item.doctor_name || '未指定医生') === this.selectedDoctor)
        .sort((a, b) => String(a.appointment_time || '').localeCompare(String(b.appointment_time || '')))
    },
    openDetail(item) {
      this.detailItem = Object.assign({}, item)
      this.detailVisible = true
    },
    resolveDetailPatientId(item = this.detailItem) {
      const patientId = item && (
        item.patient_id ||
        item.patientId ||
        item.related_patient_id ||
        item.relatedPatientId
      )
      return patientId === undefined || patientId === null ? '' : String(patientId).trim()
    },
    goPatient360() {
      const patientId = this.resolveDetailPatientId()
      if (!patientId) {
        this.$message.warning('该预约未关联患者档案')
        return
      }
      this.detailVisible = false
      this.$router.push({
        path: '/staff-h5/patient360',
        query: Object.assign({}, getStaffPortalQuery(this.$route.query), {
          id: patientId,
          name: this.detailItem.patient_name || ''
        })
      })
    },
    goEditAppointment() {
      const patientId = this.resolveDetailPatientId()
      if (!patientId) {
        this.$message.warning('该预约未关联患者档案')
        return
      }
      const query = Object.assign({}, getStaffPortalQuery(this.$route.query), {
        id: patientId,
        name: this.detailItem.patient_name || '',
        openAppointment: '1',
        appointmentPurpose: this.detailItem.appointment_purpose || ''
      })
      if (this.detailItem && this.detailItem.id !== undefined && this.detailItem.id !== null && String(this.detailItem.id).trim()) {
        query.editAppointmentId = String(this.detailItem.id).trim()
      }
      this.detailVisible = false
      this.$router.push({
        path: '/staff-h5/patient360',
        query
      })
    },
    goPatientList() {
      this.detailVisible = false
      this.$router.push({ path: '/staff-h5/patients', query: getStaffPortalQuery(this.$route.query) })
    }
  }
}
</script>

<style scoped>
.staff-h5-page {
  min-height: 100vh;
  background: #f5f7fb;
  padding: 14px;
  box-sizing: border-box;
}
.h5-hero-card,
.h5-section-card {
  background: #fff;
  border-radius: 18px;
  padding: 16px;
  box-shadow: 0 8px 24px rgba(31, 71, 136, 0.08);
  margin-bottom: 14px;
}
.h5-page-kicker {
  color: #409eff;
  font-size: 12px;
  margin-bottom: 8px;
}
.h5-hero-card h2 {
  margin: 0 0 6px;
  font-size: 24px;
  color: #1f2d3d;
}
.h5-hero-card p {
  margin: 0;
  color: #606266;
  font-size: 13px;
}
.hero-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-top: 14px;
}
.h5-summary-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  margin-bottom: 14px;
}
.h5-summary-card {
  background: #fff;
  border-radius: 16px;
  padding: 14px 12px;
  text-align: center;
  box-shadow: 0 8px 20px rgba(31, 71, 136, 0.06);
}
.h5-summary-card.accent { background: #eef6ff; }
.h5-summary-card.success { background: #eefbf3; }
.summary-num {
  font-size: 22px;
  font-weight: 700;
  color: #303133;
}
.summary-label {
  margin-top: 6px;
  font-size: 12px;
  color: #8b95a7;
}
.section-title {
  font-size: 16px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 12px;
}
.appointment-card-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}
.appointment-card {
  border-radius: 16px;
  background: #f8fbff;
  padding: 14px;
  height: 100%;
}
.appointment-card__top {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}
.appointment-card__name {
  font-size: 16px;
  font-weight: 700;
  color: #303133;
}
.appointment-card__meta,
.appointment-card__purpose {
  color: #606266;
  font-size: 13px;
  line-height: 1.7;
  margin-top: 6px;
}
.detail-sheet {
  padding: 18px;
}
.detail-title {
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 16px;
}
.detail-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #f0f2f5;
}
.detail-row--block {
  flex-direction: column;
}
.detail-row span {
  color: #8b95a7;
  font-size: 13px;
}
.detail-row strong {
  color: #303133;
  font-size: 14px;
}
.detail-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: 18px;
}

@media (max-width: 420px) {
  .hero-actions,
  .h5-summary-row {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .detail-actions,
  .appointment-card__top,
  .detail-row {
    flex-direction: column;
    align-items: flex-start;
  }
}

@media (max-width: 359px) {
  .hero-actions,
  .h5-summary-row,
  .appointment-card-list {
    grid-template-columns: 1fr;
  }
}
</style>
