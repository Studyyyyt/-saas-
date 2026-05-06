<template>
  <div class="portal-shell" v-loading="loading">
    <div class="portal-mobile">
      <section class="hero-card">
        <div class="hero-top">
          <span class="hero-badge">微信患者中心</span>
          <span class="hero-badge hero-badge--light">已绑定微信</span>
        </div>
        <div class="hero-content">
          <div>
            <div class="hero-title">{{ portalData.patient && portalData.patient.name ? `${portalData.patient.name}，你好` : '患者中心' }}</div>
            <div class="hero-subtitle">
              查看预约、病例、影像和电子知情同意书，重要事项可直接在手机上处理。
            </div>
          </div>
          <div class="hero-clinic">长沙舒澳口腔</div>
        </div>
      </section>

      <section class="quick-grid">
        <div class="quick-card quick-card--action" role="button" tabindex="0" @click="goToSection('appointments')" @keydown.enter.prevent="goToSection('appointments')">
          <div class="quick-card__icon">📅</div>
          <div class="quick-card__title">我的预约</div>
          <div class="quick-card__desc">改期 / 取消</div>
        </div>
        <div class="quick-card quick-card--action" role="button" tabindex="0" @click="goToSection('consents')" @keydown.enter.prevent="goToSection('consents')">
          <div class="quick-card__icon">📄</div>
          <div class="quick-card__title">知情同意书</div>
          <div class="quick-card__desc">阅读 / 签字</div>
        </div>
        <div class="quick-card quick-card--action" role="button" tabindex="0" @click="goToSection('records')" @keydown.enter.prevent="goToSection('records')">
          <div class="quick-card__icon">📝</div>
          <div class="quick-card__title">我的病例</div>
          <div class="quick-card__desc">诊断与治疗</div>
        </div>
        <div class="quick-card quick-card--action" role="button" tabindex="0" @click="goToSection('images')" @keydown.enter.prevent="goToSection('images')">
          <div class="quick-card__icon">🩻</div>
          <div class="quick-card__title">我的影像</div>
          <div class="quick-card__desc">一键打开文件</div>
        </div>
      </section>

      <section class="section-card" v-if="error">
        <div class="portal-error">{{ error }}</div>
      </section>

      <template v-else-if="portalData.patient">
        <section ref="appointmentsSection" class="section-card">
          <div class="section-header">
            <div>
              <div class="section-title">我的预约</div>
              <div class="section-subtitle">支持患者主动取消未就诊预约</div>
            </div>
          </div>
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
                <el-button
                  size="mini"
                  type="danger"
                  plain
                  :disabled="!canCancel(item)"
                  @click="cancelAppointment(item)"
                >取消预约</el-button>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无预约"></el-empty>
        </section>

        <section ref="consentsSection" class="section-card">
          <div class="section-header">
            <div>
              <div class="section-title">我的知情同意书</div>
              <div class="section-subtitle">医生下发后，可在此阅读并手写签字确认</div>
            </div>
          </div>
          <div v-if="consents.length" class="card-list">
            <div v-for="item in consents" :key="item.id" class="info-card">
              <div class="info-card__top">
                <div>
                  <div class="info-card__title">{{ item.title || '电子知情同意书' }}</div>
                  <div class="info-card__meta">医生：{{ item.doctor_name || '门诊医生' }}</div>
                </div>
                <el-tag size="mini" :type="consentStatusType(item.status)">{{ item.status || '待签署' }}</el-tag>
              </div>
              <div class="info-card__meta">下发时间：{{ formatDateTime(item.issued_at) || '—' }}</div>
              <div class="info-card__meta" v-if="item.signed_at">签署时间：{{ formatDateTime(item.signed_at) }}</div>
              <div class="info-card__actions">
                <el-button size="mini" type="primary" plain @click="openConsentDetail(item)">
                  {{ isConsentSigned(item) ? '查看详情' : '阅读并签字' }}
                </el-button>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无知情同意书"></el-empty>
        </section>

        <section ref="recordsSection" class="section-card">
          <div class="section-header">
            <div>
              <div class="section-title">我的病例</div>
              <div class="section-subtitle">就诊记录与医生备注</div>
            </div>
          </div>
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
        </section>

        <section ref="imagesSection" class="section-card">
          <div class="section-header">
            <div>
              <div class="section-title">我的影像</div>
              <div class="section-subtitle">点击即可打开</div>
            </div>
          </div>
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
        </section>
      </template>
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

    <el-dialog
      :title="currentConsent.title || '电子知情同意书'"
      :visible.sync="consentDialogVisible"
      width="92%"
      append-to-body
      @close="resetConsentDialog"
      @opened="handleConsentDialogOpened">
      <div v-if="currentConsent.id" class="consent-dialog-body">
        <div class="consent-meta-grid">
          <div class="consent-meta-item"><span>医生</span><strong>{{ currentConsent.doctor_name || '门诊医生' }}</strong></div>
          <div class="consent-meta-item"><span>状态</span><strong>{{ currentConsent.status || '待签署' }}</strong></div>
          <div class="consent-meta-item"><span>下发时间</span><strong>{{ formatDateTime(currentConsent.issued_at) || '—' }}</strong></div>
          <div class="consent-meta-item"><span>签署时间</span><strong>{{ formatDateTime(currentConsent.signed_at) || '未签署' }}</strong></div>
        </div>

        <div class="consent-content-box">{{ currentConsent.content || '暂无内容' }}</div>

        <div v-if="isConsentSigned(currentConsent)" class="consent-signed-box">
          <div class="consent-signed-title">已完成签署</div>
          <div class="consent-signed-meta">签署人：{{ currentConsent.signature_name || portalData.patient.name || '患者本人' }}</div>
          <img v-if="currentConsent.signature_data" :src="currentConsent.signature_data" alt="signature" class="consent-signature-image" />
          <div v-if="currentConsent.signature_remark" class="consent-signed-meta">备注：{{ currentConsent.signature_remark }}</div>
        </div>

        <div v-else class="consent-sign-box">
          <div class="consent-sign-title">请确认已完整阅读，并手写签字</div>
          <el-form :model="consentSignForm" label-width="88px">
            <el-form-item label="签字姓名">
              <el-input v-model="consentSignForm.signature_name" placeholder="请输入签字姓名"></el-input>
            </el-form-item>
            <el-form-item label="签字备注">
              <el-input v-model="consentSignForm.signature_remark" type="textarea" :rows="2" placeholder="可选"></el-input>
            </el-form-item>
          </el-form>
          <el-checkbox v-model="consentSignForm.agreed" class="consent-checkbox">我已阅读并同意上述内容</el-checkbox>
          <div class="consent-signature-label">手写签名</div>
          <SignaturePad ref="consentSignaturePad" :height="180" />
          <div class="consent-signature-actions">
            <el-button size="mini" @click="clearConsentSignature">清空签名</el-button>
          </div>
        </div>
      </div>
      <span slot="footer">
        <el-button @click="consentDialogVisible = false">关闭</el-button>
        <el-button v-if="currentConsent.id && !isConsentSigned(currentConsent)" type="primary" @click="submitConsentSignature">确认签字</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios'
import SignaturePad from '@/components/SignaturePad.vue'
import { getPatientPortalQuery, savePatientPortalSessionFromQuery } from '@/utils/portalSession'

export default {
  name: 'PatientPortalHome',
  components: { SignaturePad },
  data() {
    return {
      loading: false,
      error: '',
      portalData: {},
      editDialogVisible: false,
      consentDialogVisible: false,
      currentConsent: {},
      consentDetailLoading: false,
      editForm: {
        id: null,
        patient_name: '',
        appointment_date: '',
        appointment_time: '',
        doctor_name: '',
        appointment_purpose: '',
        cancel_reason: '',
        status: ''
      },
      consentSignForm: {
        signature_name: '',
        signature_remark: '',
        agreed: false
      }
    }
  },
  computed: {
    appointments() {
      return this.portalData.appointments || []
    },
    images() {
      return this.portalData.images || []
    },
    records() {
      return this.portalData.records || []
    },
    consents() {
      return this.portalData.consents || []
    },
  },
  mounted() {
    savePatientPortalSessionFromQuery(this.$route.query)
    this.loadPortalData()
  },
  methods: {
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
    formatDateTime(value) {
      if (!value) return ''
      return String(value).slice(0, 19).replace('T', ' ')
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
    consentStatusType(status) {
      return this.isConsentSigned({ status }) ? 'success' : 'warning'
    },
    isConsentSigned(item) {
      return String((item && item.status) || '').trim() === '已签署'
    },
    canCancel(item) {
      const status = String(item.status || '').trim()
      return ['待治疗', '已预约', '待就诊', '已改约'].includes(status)
    },
    goToSection(section) {
      const refMap = {
        appointments: 'appointmentsSection',
        consents: 'consentsSection',
        records: 'recordsSection',
        images: 'imagesSection'
      }
      const refName = refMap[section]
      if (!refName) return
      this.$nextTick(() => {
        const target = this.$refs[refName]
        if (target && target.scrollIntoView) {
          target.scrollIntoView({ behavior: 'smooth', block: 'start' })
        }
      })
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
        this.$message.success('预约已更新')
        this.editDialogVisible = false
        this.loadPortalData()
      }).catch(() => {
        this.$message.error('修改失败')
      })
    },
    cancelAppointment(item) {
      if (!this.canCancel(item)) {
        this.$message.warning('当前预约状态不可取消')
        return
      }
      this.$prompt('请输入取消原因（可选）', '取消预约', {
        confirmButtonText: '确认取消',
        cancelButtonText: '返回',
        inputPlaceholder: '例如：临时有事，改天再约',
        inputValue: '患者计划变更'
      }).then(({ value }) => {
        const query = getPatientPortalQuery(this.$route.query)
        axios.post(`/patient-portal/appointments/${item.id}/cancel`, {
          reason: value
        }, {
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
    openConsentDetail(item) {
      const query = getPatientPortalQuery(this.$route.query)
      this.consentDetailLoading = true
      axios.get(`/patient-portal/consents/${item.id}`, {
        params: {
          portalToken: query.portalToken
        }
      }).then(res => {
        if (res.data.code !== '200') {
          this.$message.error(res.data.msg || '加载知情同意书失败')
          return
        }
        this.currentConsent = res.data.data || {}
        this.consentSignForm = {
          signature_name: (this.portalData.patient && this.portalData.patient.name) || '',
          signature_remark: '',
          agreed: false
        }
        this.consentDialogVisible = true
      }).catch(() => {
        this.$message.error('加载知情同意书失败')
      }).finally(() => {
        this.consentDetailLoading = false
      })
    },
    handleConsentDialogOpened() {
      if (this.isConsentSigned(this.currentConsent)) {
        return
      }
      this.$nextTick(() => {
        if (this.$refs.consentSignaturePad && this.$refs.consentSignaturePad.resetCanvas) {
          this.$refs.consentSignaturePad.resetCanvas()
        }
      })
    },
    clearConsentSignature() {
      if (this.$refs.consentSignaturePad && this.$refs.consentSignaturePad.clear) {
        this.$refs.consentSignaturePad.clear()
      }
    },
    resetConsentDialog() {
      this.currentConsent = {}
      this.consentSignForm = {
        signature_name: (this.portalData.patient && this.portalData.patient.name) || '',
        signature_remark: '',
        agreed: false
      }
      this.clearConsentSignature()
    },
    submitConsentSignature() {
      if (!this.currentConsent.id) {
        this.$message.warning('知情同意书不存在')
        return
      }
      if (!this.consentSignForm.agreed) {
        this.$message.warning('请先确认已阅读并同意上述内容')
        return
      }
      const signaturePad = this.$refs.consentSignaturePad
      if (!signaturePad || !signaturePad.hasStroke || !signaturePad.hasStroke()) {
        this.$message.warning('请先手写签名')
        return
      }
      const query = getPatientPortalQuery(this.$route.query)
      axios.post(`/patient-portal/consents/${this.currentConsent.id}/sign`, {
        signature_name: this.consentSignForm.signature_name,
        signature_data: signaturePad.exportImage(),
        signature_remark: this.consentSignForm.signature_remark
      }, {
        params: {
          portalToken: query.portalToken
        }
      }).then(res => {
        if (res.data.code !== '200') {
          this.$message.error(res.data.msg || '签署失败')
          return
        }
        this.currentConsent = res.data.data || {}
        this.$message.success('签署成功')
        this.loadPortalData()
      }).catch(() => {
        this.$message.error('签署失败')
      })
    },
    viewImage(img) {
      const url = img && img.id ? `/patient-images/file/${img.id}` : ''
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
.portal-shell {
  min-height: 100vh;
  background: linear-gradient(180deg, #eff6ff 0%, #f6f9fc 24%, #f5f7fb 100%);
  padding: 18px 12px 36px;
  box-sizing: border-box;
  overflow-x: hidden;
}

.portal-mobile {
  max-width: 430px;
  margin: 0 auto;
  min-width: 0;
}

.hero-card {
  background: linear-gradient(135deg, #2f80ed 0%, #56ccf2 100%);
  border-radius: 24px;
  padding: 18px;
  color: #fff;
  box-shadow: 0 20px 40px rgba(47, 128, 237, 0.22);
  min-width: 0;
}

.hero-top,
.hero-content,
.info-card__top,
.info-card__actions,
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.18);
  font-size: 12px;
  line-height: 1.4;
  word-break: break-word;
}

.hero-badge--light {
  background: rgba(255, 255, 255, 0.28);
}

.hero-content {
  align-items: flex-start;
  margin-top: 16px;
}

.hero-title {
  font-size: 26px;
  font-weight: 700;
  line-height: 1.2;
  word-break: break-word;
}

.hero-subtitle,
.hero-clinic {
  margin-top: 8px;
  font-size: 13px;
  line-height: 1.7;
  color: rgba(255, 255, 255, 0.88);
  word-break: break-word;
}

.hero-stats {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
  margin-top: 18px;
}

.hero-stat {
  padding: 12px 10px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.14);
  min-width: 0;
}

.hero-stat__value {
  font-size: 18px;
  font-weight: 700;
  word-break: break-word;
}

.hero-stat__label {
  margin-top: 4px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.84);
}

.quick-grid {
  margin-top: 14px;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}

.quick-card,
.section-card {
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 14px 34px rgba(31, 45, 61, 0.08);
}

.quick-card {
  padding: 16px 10px;
  text-align: center;
  min-width: 0;
}

.quick-card--action {
  cursor: pointer;
  transition: transform .18s ease, box-shadow .18s ease;
}

.quick-card--action:active {
  transform: scale(0.98);
}

.quick-card__icon {
  width: 38px;
  height: 38px;
  margin: 0 auto 8px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #eef5ff;
  font-size: 20px;
}

.quick-card__title {
  color: #303133;
  font-weight: 600;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;
}

.quick-card__desc,
.section-subtitle,
.info-card__meta,
.portal-error,
.info-card__reason-text {
  color: #909399;
  font-size: 12px;
  line-height: 1.7;
  word-break: break-word;
}

.section-card {
  margin-top: 16px;
  padding: 16px;
  min-width: 0;
  scroll-margin-top: 12px;
}

.section-title,
.info-card__title,
.info-card__doctor {
  color: #303133;
}

.section-title {
  font-size: 17px;
  font-weight: 700;
  line-height: 1.4;
  word-break: break-word;
}

.card-list {
  display: grid;
  gap: 12px;
  margin-top: 12px;
}

.info-card {
  border: 1px solid #edf1f7;
  border-radius: 18px;
  padding: 14px;
  background: #fff;
  min-width: 0;
}

.info-card__top {
  align-items: flex-start;
  margin-bottom: 10px;
}

.info-card__title {
  font-size: 16px;
  font-weight: 700;
  line-height: 1.4;
  word-break: break-word;
}

.info-card__doctor {
  font-size: 13px;
  font-weight: 600;
  line-height: 1.5;
  word-break: break-word;
}

.info-card__reason {
  margin-top: 10px;
  padding: 10px 12px;
  border-radius: 12px;
  background: #fff6f5;
  border: 1px solid #ffd9d5;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-card__reason-label {
  font-size: 11px;
  color: #f56c6c;
  font-weight: 700;
  letter-spacing: 0.4px;
}

.info-card__actions {
  justify-content: flex-start;
  margin-top: 12px;
  flex-wrap: wrap;
}

.portal-error {
  color: #f56c6c;
}

.consent-dialog-body {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.consent-meta-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.consent-meta-item {
  padding: 12px;
  border-radius: 14px;
  background: #f8fafc;
}

.consent-meta-item span {
  display: block;
  color: #94a3b8;
  font-size: 12px;
}

.consent-meta-item strong {
  display: block;
  margin-top: 6px;
  color: #1f2937;
  font-size: 13px;
  line-height: 1.7;
  word-break: break-word;
}

.consent-content-box {
  max-height: 280px;
  overflow-y: auto;
  padding: 14px;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  background: #fff;
  color: #334155;
  line-height: 1.9;
  white-space: pre-wrap;
}

.consent-sign-box,
.consent-signed-box {
  padding: 14px;
  border-radius: 16px;
  background: #f8fbff;
  border: 1px solid #dbeafe;
}

.consent-sign-title,
.consent-signed-title {
  font-size: 14px;
  font-weight: 700;
  color: #1f2937;
}

.consent-checkbox {
  margin: 8px 0 12px;
}

.consent-signature-label,
.consent-signed-meta {
  color: #64748b;
  font-size: 12px;
  line-height: 1.7;
}

.consent-signature-actions {
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
}

.consent-signature-image {
  width: 100%;
  max-width: 320px;
  margin-top: 10px;
  border-radius: 12px;
  border: 1px solid #dbeafe;
  background: #fff;
}

@media (max-width: 768px) {
  .hero-top,
  .hero-content,
  .section-header,
  .info-card__top {
    align-items: flex-start;
  }

  .info-card__actions {
    width: 100%;
  }
}

@media (max-width: 420px) {
  .portal-shell {
    padding: 12px 8px 28px;
  }

  .hero-card,
  .section-card,
  .quick-card {
    border-radius: 18px;
  }

  .hero-top,
  .hero-content,
  .info-card__top,
  .section-header,
  .info-card__actions {
    flex-direction: column;
    align-items: flex-start;
  }

  .hero-stats,
  .quick-grid,
  .consent-meta-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .hero-title {
    font-size: 22px;
  }

  .hero-stat__value {
    font-size: 17px;
  }

  .info-card__actions .el-button {
    width: 100%;
    margin-left: 0 !important;
  }
}

@media (max-width: 359px) {
  .hero-stats,
  .quick-grid,
  .consent-meta-grid {
    grid-template-columns: 1fr;
  }
}
</style>
