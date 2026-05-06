<template>
  <div class="notice-page" v-loading="loading">
    <el-card class="notice-card" shadow="never">
      <div slot="header" class="notice-header">
        <div>
          <div class="notice-kicker">预约提醒</div>
          <h2>预约详情</h2>
        </div>
        <div class="clinic-name">长沙舒澳口腔</div>
      </div>

      <div v-if="error" class="notice-error">{{ error }}</div>

      <div v-else-if="appointment" class="notice-body">
        <div class="notice-tip">您好，{{ appointment.patient_name || '患者' }}，以下是您的预约信息：</div>

        <div class="detail-grid">
          <div class="detail-label">患者姓名</div>
          <div class="detail-value">{{ appointment.patient_name || '—' }}</div>

          <div class="detail-label">预约时间</div>
          <div class="detail-value">{{ formatSchedule(appointment) }}</div>

          <div class="detail-label">预约项目</div>
          <div class="detail-value">{{ appointment.appointment_purpose || '到院面诊' }}</div>

          <div class="detail-label">预约医生</div>
          <div class="detail-value">{{ appointment.doctor_name || '门诊医生待确认' }}</div>

          <div class="detail-label">当前状态</div>
          <div class="detail-value">{{ appointment.status || '待治疗' }}</div>

          <div class="detail-label">门诊名称</div>
          <div class="detail-value">长沙舒澳口腔</div>
        </div>

        <div class="notice-remark">
          请按预约时间提前到院，如需改约请提前联系门诊。
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'AppointmentNoticeView',
  data() {
    return {
      loading: false,
      error: '',
      appointment: null
    }
  },
  mounted() {
    this.loadAppointment()
  },
  methods: {
    loadAppointment() {
      const id = this.$route.query.id
      if (!id) {
        this.error = '缺少预约编号'
        return
      }
      this.loading = true
      axios.get('/appointments/public/detail', {
        params: { id }
      }).then(res => {
        const record = res.data && res.data.data
        if (!record) {
          this.error = '未找到预约信息'
          return
        }
        this.appointment = record
      }).catch(() => {
        this.error = '预约详情加载失败'
      }).finally(() => {
        this.loading = false
      })
    },
    formatSchedule(appointment) {
      const date = appointment && appointment.appointment_date ? appointment.appointment_date : ''
      const time = this.normalizeTime(appointment && appointment.appointment_time ? appointment.appointment_time : '')
      return [date, time].filter(Boolean).join(' ') || '—'
    },
    normalizeTime(time) {
      const text = String(time || '').trim()
      if (text.endsWith(':00:00')) {
        return text.slice(0, -3)
      }
      if (text.length === 8 && text.endsWith(':00')) {
        return text.slice(0, 5)
      }
      return text
    }
  }
}
</script>

<style scoped>
.notice-page {
  min-height: 100vh;
  padding: 24px 16px;
  background: #f5f7fb;
}

.notice-card {
  max-width: 720px;
  margin: 0 auto;
  border-radius: 18px;
}

.notice-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.notice-kicker {
  font-size: 13px;
  color: #909399;
  margin-bottom: 6px;
}

.notice-header h2 {
  margin: 0;
  font-size: 28px;
  color: #1f2d3d;
}

.clinic-name {
  font-size: 14px;
  color: #409eff;
  font-weight: 600;
}

.notice-body {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.notice-tip {
  font-size: 16px;
  color: #303133;
}

.detail-grid {
  display: grid;
  grid-template-columns: 120px 1fr;
  row-gap: 14px;
  column-gap: 12px;
}

.detail-label {
  color: #909399;
}

.detail-value {
  color: #303133;
  font-weight: 500;
  word-break: break-word;
}

.notice-remark {
  padding: 14px 16px;
  background: #f0f7ff;
  border-radius: 12px;
  color: #606266;
  line-height: 1.7;
}

.notice-error {
  color: #f56c6c;
  font-size: 15px;
}

@media (max-width: 640px) {
  .notice-page {
    padding: 16px 12px;
  }

  .notice-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .notice-header h2 {
    font-size: 24px;
  }

  .detail-grid {
    grid-template-columns: 92px 1fr;
  }
}
</style>
