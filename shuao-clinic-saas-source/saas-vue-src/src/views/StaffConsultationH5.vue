<template>
  <div class="staff-h5-page consultation-h5-page" v-loading="loading">
    <div class="h5-hero-card">
      <div>
        <div class="h5-page-kicker">员工微信 H5</div>
        <h2>咨询记录</h2>
        <p>{{ summary.displayName || '员工账号' }} · 手机查看历史咨询，护士和管理员可直接录入。</p>
      </div>
      <div class="hero-actions" :class="{ 'hero-actions--single': !canCreate }">
        <el-input v-model="keyword" placeholder="搜索姓名/手机号/项目" clearable @input="applyFilter"></el-input>
        <el-button v-if="canCreate" type="primary" @click="openCreateDialog">记录咨询</el-button>
      </div>
    </div>

    <div v-if="error" class="h5-section-card">
      <div class="error-text">{{ error }}</div>
    </div>

    <template v-else>
      <div class="h5-summary-row h5-summary-row--triple">
        <div class="h5-summary-card">
          <div class="summary-num">{{ todayCount }}</div>
          <div class="summary-label">今日录入</div>
        </div>
        <div class="h5-summary-card accent">
          <div class="summary-num">{{ highIntentPendingCount }}</div>
          <div class="summary-label">高意向待跟进</div>
        </div>
        <div class="h5-summary-card success">
          <div class="summary-num">{{ linkedPatientCount }}</div>
          <div class="summary-label">已关联患者</div>
        </div>
      </div>

      <div class="h5-section-card">
        <div class="mobile-filter-head">
          <div class="section-title section-title--compact">状态筛选</div>
          <div class="mobile-filter-chips">
            <button
              v-for="item in consultationStageOptions"
              :key="item.value"
              type="button"
              class="mobile-filter-chip"
              :class="{ 'is-active': quickStage === item.value }"
              @click="toggleQuickStage(item.value)"
            >
              {{ item.label }}
            </button>
          </div>
        </div>
      </div>

      <div class="h5-section-card">
        <div class="section-title">历史咨询记录</div>
        <div v-if="filteredRows.length" class="consultation-card-list">
          <div v-for="item in filteredRows" :key="item.id" class="consultation-card">
            <div class="consultation-card__top">
              <div>
                <div class="consultation-card__name">{{ item.contact_name || item.patient_name || '未填写咨询人' }}</div>
                <div class="consultation-card__meta">
                  {{ formatDateTime(item.consultation_time) || '未记录时间' }} · {{ item.consultation_channel || '未知渠道' }} · {{ item.created_by_name || '未标记录入人' }}
                </div>
              </div>
              <div class="consultation-card__tags">
                <el-tag size="mini" :type="intentTagType(item.intent_level)">{{ item.intent_level || '未评估' }}</el-tag>
                <el-tag size="mini" :type="resultTagType(item.handling_result)">{{ item.handling_result || '未处理' }}</el-tag>
              </div>
            </div>
            <div class="consultation-card__desc">主诉：{{ item.chief_project || '未填写' }}</div>
            <div class="consultation-card__desc">手机号：{{ item.contact_phone || '未留手机号' }}</div>
            <div class="consultation-card__desc">关联患者：{{ item.patient_name || '未关联' }}</div>
            <div class="consultation-card__actions">
              <el-button v-if="canEditRecord(item)" size="mini" type="text" @click="openEditDialog(item)">编辑</el-button>
              <el-button
                v-if="item.patient_id && item.handling_result === '已预约到店'"
                size="mini"
                type="text"
                @click="goPatient360ForAppointment(item)"
              >去建预约</el-button>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无咨询记录"></el-empty>
      </div>
    </template>

    <el-dialog
      :title="dialogMode === 'edit' ? '编辑咨询' : '记录咨询'"
      :visible.sync="dialogVisible"
      width="92%"
      append-to-body
      @close="handleDialogClose"
    >
      <el-alert
        v-if="showPhoneOpenPrompt"
        type="error"
        show-icon
        :closable="false"
        class="prompt-alert prompt-alert--strong"
        :title="openConsultationAlertTitle"
      />

      <el-alert
        v-if="phonePromptFlags && phonePromptFlags.phoneMatchedPatient"
        type="info"
        show-icon
        :closable="false"
        class="prompt-alert"
      >
        <div class="patient-match-box">
          <span>该号码已匹配患者 <strong>{{ phonePromptFlags.matchedPatientName }}</strong>。</span>
          <div class="patient-match-actions">
            <el-button size="mini" type="primary" @click="linkMatchedPatient">关联患者</el-button>
            <el-button size="mini" plain @click="unlinkMatchedPatient">暂不关联</el-button>
          </div>
        </div>
      </el-alert>

      <el-form :model="form" label-width="92px" class="consultation-form">
        <el-form-item label="咨询时间">
          <el-date-picker
            v-model="form.consultation_time"
            type="datetime"
            value-format="yyyy-MM-dd HH:mm:ss"
            format="yyyy-MM-dd HH:mm"
            style="width:100%"
          />
        </el-form-item>
        <el-form-item label="咨询渠道">
          <el-select v-model="form.consultation_channel" style="width:100%" @change="handleChannelChange">
            <el-option
              v-for="item in consultationChannelOptions"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="showReferralSelector" label="介绍人">
          <ReferralSelector
            :value="referralForm"
            :current-patient-id="form.patient_id || null"
            :show-remark="false"
            @input="handleReferralChange"
          />
        </el-form-item>
        <el-form-item label="主诉项目">
          <el-select v-model="form.chief_project" style="width:100%" placeholder="请选择主诉项目">
            <el-option
              v-for="item in chiefProjectOptions"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="意向强度">
          <el-select v-model="form.intent_level" style="width:100%" placeholder="请选择意向强度">
            <el-option
              v-for="item in intentOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="处理结果">
          <el-select v-model="form.handling_result" style="width:100%">
            <el-option
              v-for="item in handlingResultOptions"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="关联患者" :required="form.handling_result === '已成交'">
          <el-select
            v-model="form.patient_id"
            style="width:100%"
            filterable
            remote
            reserve-keyword
            clearable
            placeholder="输入患者姓名/手机号搜索"
            :remote-method="searchPatients"
            :loading="patientLoading"
            @change="handlePatientChange"
          >
            <el-option
              v-for="item in patientOptions"
              :key="item.id"
              :label="formatPatientOption(item)"
              :value="item.id"
            />
          </el-select>
          <div class="field-tip">
            {{ form.handling_result === '已成交' ? '已成交必须关联患者档案。' : '可选：把咨询沉淀到具体患者。' }}
          </div>
        </el-form-item>
        <el-form-item label="咨询人">
          <el-input v-model="form.contact_name" maxlength="50" placeholder="姓名或昵称"></el-input>
        </el-form-item>
        <el-form-item label="联系方式">
          <el-input
            v-model="form.contact_phone"
            maxlength="11"
            placeholder="输入11位手机号"
            @blur="handlePhoneBlur"
          ></el-input>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="form.remarks"
            type="textarea"
            :rows="4"
            maxlength="200"
            show-word-limit
            placeholder="记录客户诉求、关注点和特殊情况"
          ></el-input>
        </el-form-item>
      </el-form>

      <span slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">{{ dialogMode === 'edit' ? '保存' : '确定' }}</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios'
import ReferralSelector from '@/components/ReferralSelector.vue'
import {
  CHIEF_PROJECT_OPTIONS,
  CONSULTATION_STAGE_FILTER_OPTIONS,
  CONSULTATION_CHANNEL_OPTIONS,
  HANDLING_RESULT_OPTIONS,
  INTENT_LEVEL_OPTIONS,
  matchConsultationStage
} from '@/utils/consultationOptions'
import { getStaffPortalQuery, saveStaffPortalSessionFromQuery } from '@/utils/portalSession'

function createEmptyForm() {
  return {
    id: null,
    patient_id: null,
    consultation_time: '',
    consultation_channel: '微信',
    chief_project: '',
    intent_level: '',
    handling_result: '待跟进',
    contact_name: '',
    contact_phone: '',
    remarks: ''
  }
}

function createEmptyReferralState() {
  return {
    referrer_type: '',
    referrer_patient_id: null,
    referrer_patient_name: '',
    external_referrer_type: '',
    external_referrer_name: '',
    external_referrer_contact: '',
    referral_remark: ''
  }
}

export default {
  name: 'StaffConsultationH5',
  components: {
    ReferralSelector
  },
  data() {
    return {
      loading: false,
      saving: false,
      error: '',
      account: {},
      summary: {},
      keyword: '',
      quickStage: 'all',
      rows: [],
      filteredRows: [],
      dialogVisible: false,
      dialogMode: 'create',
      form: createEmptyForm(),
      referralForm: createEmptyReferralState(),
      phonePromptFlags: null,
      patientLoading: false,
      patientOptions: [],
      consultationStageOptions: CONSULTATION_STAGE_FILTER_OPTIONS,
      consultationChannelOptions: CONSULTATION_CHANNEL_OPTIONS,
      chiefProjectOptions: CHIEF_PROJECT_OPTIONS,
      intentOptions: INTENT_LEVEL_OPTIONS,
      handlingResultOptions: HANDLING_RESULT_OPTIONS
    }
  },
  computed: {
    normalizedRole() {
      const role = String((this.account && this.account.role) || '').trim()
      if (role === '管理员' || role === 'admin') return 'admin'
      if (role === '医生' || role === 'doctor') return 'doctor'
      if (role === '护士' || role === 'nurse') return 'nurse'
      return role
    },
    canCreate() {
      return this.normalizedRole === 'admin' || this.normalizedRole === 'nurse'
    },
    todayText() {
      const now = new Date()
      const year = now.getFullYear()
      const month = String(now.getMonth() + 1).padStart(2, '0')
      const day = String(now.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    },
    todayCount() {
      return this.rows.filter(item => String(item.consultation_time || '').slice(0, 10) === this.todayText).length
    },
    highIntentPendingCount() {
      return this.rows.filter(item => item.intent_level === '高' && item.handling_result === '待跟进').length
    },
    linkedPatientCount() {
      return this.rows.filter(item => Number(item.patient_id) > 0).length
    },
    showPhoneOpenPrompt() {
      return Number(this.phonePromptFlags && this.phonePromptFlags.openConsultationCount) >= 1
    },
    openConsultationAlertTitle() {
      const count = Number(this.phonePromptFlags && this.phonePromptFlags.openConsultationCount) || 0
      return `该手机号已有 ${count} 条未成交咨询，请先确认是否重复录入。`
    },
    showReferralSelector() {
      return this.form.consultation_channel === '转介绍' || this.hasReferralPayload()
    }
  },
  mounted() {
    saveStaffPortalSessionFromQuery(this.$route.query)
    this.loadContext()
  },
  methods: {
    buildPortalQuery(extra = {}) {
      return Object.assign({}, getStaffPortalQuery(this.$route.query), extra)
    },
    formatDateTime(value) {
      if (!value) return ''
      return String(value).slice(0, 19).replace('T', ' ')
    },
    formatNow() {
      const now = new Date()
      const year = now.getFullYear()
      const month = String(now.getMonth() + 1).padStart(2, '0')
      const day = String(now.getDate()).padStart(2, '0')
      const hour = String(now.getHours()).padStart(2, '0')
      const minute = String(now.getMinutes()).padStart(2, '0')
      return `${year}-${month}-${day} ${hour}:${minute}:00`
    },
    intentTagType(value) {
      if (value === '高') return 'danger'
      if (value === '中') return 'warning'
      return 'info'
    },
    resultTagType(value) {
      if (value === '已成交') return 'success'
      if (value === '已预约到店') return 'success'
      if (value === '不再跟进') return 'info'
      return 'warning'
    },
    formatPatientOption(item) {
      if (!item) return ''
      return [item.name || '', item.phone || '', item.customer_source || ''].filter(Boolean).join('｜')
    },
    ensureCurrentPatientOption(source = {}) {
      const patientId = Number(source && source.patient_id)
      if (!Number.isFinite(patientId) || patientId <= 0) {
        return
      }
      const exists = (this.patientOptions || []).some(item => Number(item.id) === patientId)
      if (exists) {
        return
      }
      this.patientOptions = [{
        id: patientId,
        name: source.patient_name || '',
        phone: source.patient_phone || '',
        customer_source: source.patient_customer_source || ''
      }].concat(this.patientOptions || [])
    },
    loadContext() {
      const query = getStaffPortalQuery(this.$route.query)
      if (!query.accountId || !query.staffToken) {
        this.error = '缺少员工身份信息'
        return
      }
      this.loading = true
      axios.get('/staff-portal/overview', {
        params: {
          accountId: query.accountId,
          staffToken: query.staffToken
        }
      }).then(res => {
        if (res.data.code !== '200') {
          this.error = res.data.msg || '加载失败'
          return
        }
        const data = res.data.data || {}
        this.account = data.account || {}
        this.summary = data.summary || {}
        this.error = ''
        this.loadList()
      }).catch(() => {
        this.error = '员工身份校验失败'
      }).finally(() => {
        this.loading = false
      })
    },
    loadList() {
      const accountId = Number(this.account && this.account.id)
      if (!Number.isFinite(accountId) || accountId <= 0) {
        this.rows = []
        this.filteredRows = []
        return
      }
      this.loading = true
      axios.get('/consultations/search', {
        params: {
          page: 1,
          size: 500
        }
      }).then(res => {
        const data = (res.data && res.data.data) || {}
        const list = Array.isArray(data.list) ? data.list : []
        this.rows = list.slice().sort((a, b) => String(b.consultation_time || '').localeCompare(String(a.consultation_time || '')))
        this.applyFilter()
      }).catch(() => {
        this.error = '加载咨询记录失败'
        this.rows = []
        this.filteredRows = []
      }).finally(() => {
        this.loading = false
      })
    },
    applyFilter() {
      const text = String(this.keyword || '').trim().toLowerCase()
      this.filteredRows = this.rows.filter(item => {
        if (!matchConsultationStage(item, this.quickStage)) {
          return false
        }
        if (!text) {
          return true
        }
        const values = [
          item.contact_name,
          item.contact_phone,
          item.patient_name,
          item.chief_project
        ].map(value => String(value || '').toLowerCase())
        return values.some(value => value.includes(text))
      })
    },
    toggleQuickStage(stage) {
      this.quickStage = this.quickStage === stage && stage !== 'all' ? 'all' : stage
      this.applyFilter()
    },
    openCreateDialog() {
      if (!this.canCreate) {
        this.$message.warning('当前账号仅可查看历史咨询记录')
        return
      }
      this.dialogMode = 'create'
      this.form = Object.assign(createEmptyForm(), {
        consultation_time: this.formatNow()
      })
      this.referralForm = createEmptyReferralState()
      this.phonePromptFlags = null
      this.patientOptions = []
      this.dialogVisible = true
    },
    canEditRecord(item) {
      if (!item) return false
      if (this.normalizedRole === 'admin') return true
      return this.normalizedRole === 'nurse' && Number(item.created_by) === Number(this.account && this.account.id)
    },
    openEditDialog(item) {
      if (!this.canEditRecord(item)) {
        this.$message.warning('当前账号无权编辑该咨询记录')
        return
      }
      this.dialogMode = 'edit'
      this.form = Object.assign(createEmptyForm(), {
        id: item.id || null,
        patient_id: item.patient_id || null,
        consultation_time: item.consultation_time || '',
        consultation_channel: item.consultation_channel || '微信',
        chief_project: item.chief_project || '',
        intent_level: item.intent_level || '',
        handling_result: item.handling_result || '待跟进',
        contact_name: item.contact_name || '',
        contact_phone: item.contact_phone || '',
        remarks: item.remarks || ''
      })
      this.referralForm = Object.assign(createEmptyReferralState(), {
        referrer_type: item.referrer_type || '',
        referrer_patient_id: item.referrer_patient_id || null,
        referrer_patient_name: item.referrer_patient_name || '',
        external_referrer_type: item.external_referrer_type || '',
        external_referrer_name: item.external_referrer_name || '',
        external_referrer_contact: item.external_referrer_contact || ''
      })
      this.phonePromptFlags = null
      this.patientOptions = []
      this.ensureCurrentPatientOption(item)
      this.dialogVisible = true
    },
    handleDialogClose() {
      this.phonePromptFlags = null
      this.referralForm = createEmptyReferralState()
      this.patientOptions = []
    },
    handleChannelChange(value) {
      if (value !== '转介绍') {
        this.referralForm = createEmptyReferralState()
      }
    },
    handleReferralChange(value) {
      this.referralForm = Object.assign(createEmptyReferralState(), value || {})
    },
    handlePhoneBlur() {
      const phone = String(this.form.contact_phone || '').trim()
      if (!phone || !/^\d{11}$/.test(phone)) {
        this.phonePromptFlags = null
        return
      }
      axios.get('/consultations/matchPatientByPhone', { params: { phone } }).then(response => {
        this.phonePromptFlags = response.data && response.data.data ? response.data.data : null
      }).catch(() => {
        this.phonePromptFlags = null
      })
    },
    linkMatchedPatient() {
      if (!this.phonePromptFlags || !this.phonePromptFlags.matchedPatientId) return
      this.form.patient_id = this.phonePromptFlags.matchedPatientId
      this.ensureCurrentPatientOption({
        patient_id: this.phonePromptFlags.matchedPatientId,
        patient_name: this.phonePromptFlags.matchedPatientName,
        patient_phone: this.form.contact_phone
      })
      this.$message.success(`已关联患者：${this.phonePromptFlags.matchedPatientName}`)
    },
    unlinkMatchedPatient() {
      this.form.patient_id = null
      this.$message.info('本次咨询暂不关联现有患者')
    },
    async searchPatients(query) {
      const keyword = String(query || '').trim()
      const current = (this.patientOptions || []).find(item => Number(item.id) === Number(this.form.patient_id || 0)) || null
      if (!keyword) {
        this.patientOptions = current ? [current] : []
        return
      }
      this.patientLoading = true
      try {
        const res = await axios.get('/patients/search', { params: { keyword, page: 1, size: 20 } })
        const data = res.data && res.data.data ? res.data.data : {}
        this.patientOptions = Array.isArray(data.list) ? data.list : []
        if (current) {
          this.ensureCurrentPatientOption(current)
        }
      } catch (error) {
        this.patientOptions = current ? [current] : []
      } finally {
        this.patientLoading = false
      }
    },
    handlePatientChange(value) {
      const patient = (this.patientOptions || []).find(item => Number(item.id) === Number(value || 0))
      this.form.patient_id = patient ? Number(patient.id) : null
      if (!patient) {
        return
      }
      if (!String(this.form.contact_name || '').trim()) {
        this.form.contact_name = patient.name || ''
      }
      if (!String(this.form.contact_phone || '').trim()) {
        this.form.contact_phone = patient.phone || ''
      }
    },
    validateForm() {
      if (!this.form.consultation_time) return '咨询时间必填'
      if (!this.form.consultation_channel) return '咨询渠道必填'
      if (!this.form.chief_project) return '主诉项目必填'
      if (!this.form.intent_level) return '请选择意向强度'
      if (!this.form.handling_result) return '处理结果必填'
      if (this.form.handling_result === '已成交' && !this.form.patient_id) return '已成交必须关联患者信息'
      const phone = String(this.form.contact_phone || '').trim()
      if (phone && !/^\d{11}$/.test(phone)) return '联系方式需为11位手机号'
      if (this.form.consultation_channel === '转介绍' && this.referralForm.referrer_type === 'patient' && !this.referralForm.referrer_patient_id) return '请选择有效的介绍患者'
      if (this.form.consultation_channel === '转介绍' && this.referralForm.referrer_type === 'external' && !String(this.referralForm.external_referrer_name || '').trim()) return '请输入外部介绍人姓名'
      return ''
    },
    buildPayload() {
      const currentUserId = Number(this.account && this.account.id)
      const currentUserName = String((this.account && this.account.name) || '').trim()
      const referralPayload = this.form.consultation_channel === '转介绍' ? this.referralForm : createEmptyReferralState()
      const payload = {
        id: this.form.id,
        patient_id: this.form.patient_id || null,
        consultation_time: this.form.consultation_time,
        consultation_channel: this.form.consultation_channel,
        referrer_type: referralPayload.referrer_type || '',
        referrer_patient_id: referralPayload.referrer_patient_id || null,
        referrer_patient_name: referralPayload.referrer_patient_name || '',
        external_referrer_type: referralPayload.external_referrer_type || '',
        external_referrer_name: referralPayload.external_referrer_name || '',
        external_referrer_contact: referralPayload.external_referrer_contact || '',
        chief_project: this.form.chief_project,
        intent_level: this.form.intent_level,
        handling_result: this.form.handling_result,
        contact_name: String(this.form.contact_name || '').trim(),
        contact_phone: String(this.form.contact_phone || '').trim(),
        remarks: String(this.form.remarks || '').trim()
      }
      if (this.dialogMode === 'edit') {
        payload.updated_by = Number.isFinite(currentUserId) && currentUserId > 0 ? currentUserId : null
      } else {
        payload.created_by = Number.isFinite(currentUserId) && currentUserId > 0 ? currentUserId : null
        payload.created_by_name = currentUserName
      }
      return payload
    },
    submit() {
      const message = this.validateForm()
      if (message) {
        this.$message.warning(message)
        return
      }
      const request = this.dialogMode === 'edit'
        ? axios.put('/consultations/edit', this.buildPayload())
        : axios.post('/consultations/add', this.buildPayload())
      this.saving = true
      request.then(response => {
        if (!response.data || response.data.code !== '200') {
          this.$message.error((response.data && response.data.msg) || '保存失败')
          return
        }
        const data = response.data.data || {}
        const record = data.record || data || {}
        const weekCount = Number(data.weekCount || 0)
        this.dialogVisible = false
        this.loadList()
        if (this.dialogMode === 'edit') {
          this.$message.success('咨询记录已更新')
          return
        }
        this.$message.success(`已记录咨询，本周共 ${weekCount} 条`)
        if (this.form.handling_result === '已预约到店') {
          if (Number(record.patient_id) > 0) {
            this.promptCreateAppointment(record)
          } else {
            this.$message.info('当前咨询未关联患者档案，暂无法直接跳转建预约。')
          }
        }
      }).catch(error => {
        const message = error && error.response && error.response.data && error.response.data.msg
          ? error.response.data.msg
          : '保存失败'
        this.$message.error(message)
      }).finally(() => {
        this.saving = false
      })
    },
    promptCreateAppointment(record) {
      this.$confirm('咨询已保存，是否继续为该患者新增预约？', '继续建预约', {
        type: 'info',
        confirmButtonText: '去新增',
        cancelButtonText: '稍后'
      }).then(() => {
        this.goPatient360ForAppointment(record)
      }).catch(() => {})
    },
    goPatient360ForAppointment(record) {
      const patientId = Number(record && record.patient_id)
      if (!Number.isFinite(patientId) || patientId <= 0) return
      const patientName = String((record && record.patient_name) || (this.phonePromptFlags && this.phonePromptFlags.matchedPatientName) || this.form.contact_name || '').trim()
      const appointmentPurpose = String((record && record.chief_project) || this.form.chief_project || '').trim()
      this.$router.push({
        path: '/staff-h5/patient360',
        query: this.buildPortalQuery({
          id: patientId,
          name: patientName,
          openAppointment: '1',
          appointmentPurpose
        })
      })
    },
    hasReferralPayload() {
      return !!(
        Number(this.referralForm.referrer_patient_id || 0) > 0
        || String(this.referralForm.referrer_patient_name || '').trim()
        || String(this.referralForm.external_referrer_type || '').trim()
        || String(this.referralForm.external_referrer_name || '').trim()
        || String(this.referralForm.external_referrer_contact || '').trim()
        || String(this.referralForm.referrer_type || '').trim()
      )
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
  line-height: 1.6;
}
.hero-actions {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 120px;
  gap: 10px;
  margin-top: 14px;
}
.hero-actions--single {
  grid-template-columns: 1fr;
}
.h5-summary-row {
  display: grid;
  gap: 10px;
  margin-bottom: 14px;
}
.h5-summary-row--triple {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}
.h5-summary-card {
  background: #fff;
  border-radius: 16px;
  padding: 14px 12px;
  text-align: center;
  box-shadow: 0 8px 20px rgba(31, 71, 136, 0.06);
}
.h5-summary-card.accent { background: #fff7ec; }
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
.section-title--compact {
  margin-bottom: 0;
}
.mobile-filter-head {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.mobile-filter-chips {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}
.mobile-filter-chip {
  border: 1px solid #d8e3f0;
  background: #f8fbff;
  color: #52606d;
  border-radius: 999px;
  padding: 8px 14px;
  font-size: 13px;
  line-height: 1;
  cursor: pointer;
  transition: all 0.2s ease;
}
.mobile-filter-chip.is-active {
  background: #409eff;
  border-color: #409eff;
  color: #fff;
  box-shadow: 0 8px 18px rgba(64, 158, 255, 0.18);
}
.consultation-card-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}
.consultation-card {
  border-radius: 16px;
  background: #f8fbff;
  padding: 14px;
  height: 100%;
}
.consultation-card__top {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}
.consultation-card__name {
  font-size: 16px;
  font-weight: 700;
  color: #303133;
}
.consultation-card__meta,
.consultation-card__desc {
  color: #606266;
  font-size: 13px;
  line-height: 1.7;
  margin-top: 6px;
}
.consultation-card__tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  justify-content: flex-end;
}
.consultation-card__actions {
  margin-top: 8px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
.consultation-form {
  padding-top: 4px;
}
.prompt-alert {
  margin-bottom: 14px;
}
.prompt-alert--strong {
  border: 1px solid rgba(220, 38, 38, 0.16);
  box-shadow: 0 10px 24px rgba(220, 38, 38, 0.08);
}
.patient-match-box {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}
.patient-match-actions {
  display: flex;
  gap: 8px;
}
.field-tip {
  margin-top: 6px;
  color: #64748b;
  font-size: 12px;
  line-height: 1.5;
}
.error-text {
  color: #f56c6c;
  font-size: 14px;
  line-height: 1.7;
}

@media (max-width: 420px) {
  .hero-actions {
    grid-template-columns: minmax(0, 1fr) 108px;
  }

  .hero-actions--single {
    grid-template-columns: 1fr;
  }

  .h5-summary-row--triple {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .consultation-card__top {
    flex-direction: column;
    align-items: flex-start;
  }

  .consultation-card__tags,
  .consultation-card__actions {
    justify-content: flex-start;
  }
}

@media (max-width: 359px) {
  .hero-actions,
  .h5-summary-row--triple,
  .consultation-card-list {
    grid-template-columns: 1fr;
  }
}
</style>
