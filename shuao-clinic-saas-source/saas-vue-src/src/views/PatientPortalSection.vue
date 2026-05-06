<template>
  <div class="patient-portal-section-page" v-loading="loading">
    <div class="portal-mobile">
      <section class="section-card section-card--hero">
        <div class="section-header">
          <div>
            <div class="section-title">{{ pageTitle }}</div>
            <div class="section-subtitle">{{ pageSubtitle }}</div>
          </div>
          <el-button size="mini" plain @click="goHome">返回首页</el-button>
        </div>
      </section>

      <section class="section-card" v-if="error">
        <div class="portal-error">{{ error }}</div>
      </section>

      <section class="section-card" v-else>
        <template v-if="section === 'appointments'">
          <div v-if="appointments.length" class="card-list">
            <div v-for="item in appointments" :key="item.id" class="info-card">
              <div class="info-card__top">
                <div>
                  <div class="info-card__title">{{ formatSchedule(item) }}</div>
                  <div class="info-card__meta">医生：{{ item.doctor_name || '门诊医生待确认' }}</div>
                </div>
                <el-tag size="mini" :type="statusTagType(item.status)">{{ item.status || '待治疗' }}</el-tag>
              </div>
              <div class="info-card__meta">项目：{{ item.appointment_purpose || '到院面诊' }}</div>
              <div class="info-card__reason" v-if="item.cancel_reason">
                <span class="info-card__reason-label">取消原因</span>
                <span class="info-card__reason-text">{{ item.cancel_reason }}</span>
              </div>
              <div class="info-card__actions">
                <el-button size="mini" type="primary" plain @click="startEdit(item)">修改预约</el-button>
                <el-button size="mini" type="danger" plain :disabled="!canCancel(item)" @click="cancelAppointment(item)">取消预约</el-button>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无预约"></el-empty>
        </template>

        <template v-else-if="section === 'records'">
          <div v-if="records.length" class="card-list">
            <div v-for="record in records" :key="record.id" class="info-card">
              <div class="info-card__top">
                <div class="info-card__title">{{ formatDate(record.visit_date) || '未记录日期' }}</div>
                <span class="info-card__doctor">{{ record.doctor_name || '门诊医生' }}</span>
              </div>
              <div class="info-card__meta">主诉：{{ record.chief_complaint || '无' }}</div>
              <div class="info-card__meta">诊断：{{ record.diagnosis || '无' }}</div>
              <div class="info-card__meta">治疗：{{ record.treatment || '无' }}</div>
              <div class="info-card__meta">备注：{{ record.notes || '无' }}</div>
            </div>
          </div>
          <el-empty v-else description="暂无病例"></el-empty>
        </template>

        <template v-else>
          <div v-if="images.length" class="card-list">
            <div v-for="img in images" :key="img.id" class="info-card">
              <div class="info-card__top">
                <div>
                  <div class="info-card__title">{{ img.image_name || '影像资料' }}</div>
                  <div class="info-card__meta">日期：{{ img.image_date || '—' }}</div>
                </div>
                <el-tag size="mini">{{ img.image_type || '其他' }}</el-tag>
              </div>
              <div class="info-card__meta">备注：{{ img.notes || '无' }}</div>
              <div class="info-card__actions">
                <el-button size="mini" type="primary" plain @click="viewImage(img)">查看影像</el-button>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无影像"></el-empty>
        </template>
      </section>
    </div>

    <el-dialog title="修改预约" :visible.sync="editDialogVisible" width="92%" append-to-body>
      <el-form :model="editForm" label-width="90px">
        <el-form-item label="预约日期">
          <el-date-picker v-model="editForm.appointment_date" type="date" value-format="yyyy-MM-dd" format="yyyy-MM-dd" style="width:100%"></el-date-picker>
        </el-form-item>
        <el-form-item label="预约时间">
          <el-time-picker v-model="editForm.appointment_time" value-format="HH:mm:ss" format="HH:mm" style="width:100%"></el-time-picker>
        </el-form-item>
        <el-form-item label="预约项目">
          <el-input v-model="editForm.appointment_purpose" type="textarea" :rows="2"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios'
import { getPatientPortalQuery, savePatientPortalSessionFromQuery } from '@/utils/portalSession'

export default {
  name: 'PatientPortalSection',
  data() {
    return {
      loading: false,
      error: '',
      portalData: {},
      editDialogVisible: false,
      editForm: {
        id: null,
        patient_name: '',
        appointment_date: '',
        appointment_time: '',
        doctor_name: '',
        appointment_purpose: '',
        cancel_reason: '',
        status: ''
      }
    }
  },
  computed: {
    section() {
      return this.$route.query.section || 'appointments'
    },
    pageTitle() {
      if (this.section === 'records') return '我的病例'
      if (this.section === 'images') return '我的影像'
      return '我的预约'
    },
    pageSubtitle() {
      if (this.section === 'records') return '查看历史病例与医生备注'
      if (this.section === 'images') return '查看影像资料与备注'
      return '支持患者在手机上修改或取消预约'
    },
    appointments() {
      return this.portalData.appointments || []
    },
    images() {
      return this.portalData.images || []
    },
    records() {
      return this.portalData.records || []
    }
  },
  mounted() {
    savePatientPortalSessionFromQuery(this.$route.query)
    this.loadPortalData()
  },
  methods: {
    goHome() {
      this.$router.push({ path: '/patient-portal-home', query: getPatientPortalQuery(this.$route.query) })
    },
    loadPortalData() {
      const query = getPatientPortalQuery(this.$route.query)
      if (!query.patientId || !query.portalToken) {
        this.error = '缺少患者身份信息'
        return
      }
      this.loading = true
      axios.get('/patient-portal/overview', {
        params: {
          patientId: query.patientId,
          portalToken: query.portalToken
        }
      }).then(res => {
        if (res.data.code !== '200') {
          this.error = res.data.msg || '加载失败'
          return
        }
        this.error = ''
        this.portalData = res.data.data || {}
      }).catch(() => {
        this.error = '患者中心加载失败'
      }).finally(() => {
        this.loading = false
      })
    },
    formatDate(value) {
      if (!value) return ''
      return String(value).slice(0, 10)
    },
    normalizeTime(value) {
      const text = String(value || '').trim()
      if (text.endsWith(':00:00')) return text.slice(0, -3)
      if (text.length === 8 && text.endsWith(':00')) return text.slice(0, 5)
      return text
    },
    formatSchedule(item) {
      return [this.formatDate(item.appointment_date), this.normalizeTime(item.appointment_time)].filter(Boolean).join(' ') || '未安排'
    },
    statusTagType(status) {
      if (status === '已取消') return 'danger'
      if (status === '已完成' || status === '已治疗' || status === '已就诊') return 'success'
      if (status === '已离开') return 'info'
      if (status === '已改约') return 'warning'
      return 'primary'
    },
    canCancel(item) {
      const status = String(item.status || '').trim()
      return ['待治疗', '已预约', '待就诊', '已改约'].includes(status)
    },
    startEdit(item) {
      this.editForm = {
        id: item.id,
        patient_name: item.patient_name,
        appointment_date: this.formatDate(item.appointment_date),
        appointment_time: item.appointment_time,
        doctor_name: item.doctor_name,
        appointment_purpose: item.appointment_purpose,
        cancel_reason: item.cancel_reason,
        status: item.status
      }
      this.editDialogVisible = true
    },
    submitEdit() {
      const query = getPatientPortalQuery(this.$route.query)
      axios.put(`/patient-portal/appointments/${this.editForm.id}/edit`, this.editForm, {
        params: { portalToken: query.portalToken }
      }).then(res => {
        if (res.data.code !== '200') {
          this.$message.error(res.data.msg || '修改失败')
          return
        }
        this.$message.success('修改成功')
        this.editDialogVisible = false
        this.loadPortalData()
      }).catch(() => {
        this.$message.error('修改失败')
      })
    },
    cancelAppointment(item) {
      this.$prompt('请输入取消原因', '取消预约', {
        confirmButtonText: '确认取消',
        cancelButtonText: '暂不取消',
        inputPlaceholder: '如：临时有事，改期就诊'
      }).then(({ value }) => {
        const query = getPatientPortalQuery(this.$route.query)
        axios.post(`/patient-portal/appointments/${item.id}/cancel`, { reason: value || '' }, {
          params: { portalToken: query.portalToken }
        }).then(res => {
          if (res.data.code !== '200') {
            this.$message.error(res.data.msg || '取消失败')
            return
          }
          this.$message.success('预约已取消')
          this.loadPortalData()
        }).catch(() => {
          this.$message.error('取消失败')
        })
      }).catch(() => {})
    },
    viewImage(img) {
      const url = img.image_url || (img.id ? `/patient-images/file/${img.id}` : '')
      if (!url) {
        this.$message.warning('暂无影像地址')
        return
      }
      window.location.href = url
    }
  }
}
</script>

<style scoped>
.patient-portal-section-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #eef4ff 0%, #f8fbff 100%);
}
.portal-mobile {
  max-width: 520px;
  margin: 0 auto;
  padding: 18px 14px 28px;
  box-sizing: border-box;
}
.section-card {
  background: #fff;
  border-radius: 20px;
  padding: 18px;
  box-shadow: 0 10px 28px rgba(31, 71, 136, 0.08);
  margin-bottom: 16px;
}
.section-card--hero {
  padding-bottom: 14px;
}
.section-header,
.info-card__top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}
.section-title {
  font-size: 20px;
  font-weight: 700;
  color: #1f2d3d;
}
.section-subtitle,
.info-card__meta,
.portal-error,
.info-card__reason-text {
  color: #606266;
  line-height: 1.7;
  font-size: 14px;
}
.card-list {
  display: grid;
  gap: 12px;
}
.info-card {
  border-radius: 16px;
  background: #f8fbff;
  padding: 14px;
}
.info-card__title,
.info-card__doctor {
  font-weight: 700;
  color: #303133;
}
.info-card__actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 12px;
}
.info-card__reason {
  margin-top: 8px;
  padding: 10px 12px;
  border-radius: 12px;
  background: #fff7f7;
}
.info-card__reason-label {
  display: block;
  font-size: 12px;
  color: #ef4444;
  margin-bottom: 4px;
}
</style>
