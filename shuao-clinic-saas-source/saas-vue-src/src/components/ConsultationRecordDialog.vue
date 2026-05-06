<template>
  <el-dialog
    :title="dialogTitle"
    :visible.sync="innerVisible"
    width="720px"
    append-to-body
    @close="handleClose"
  >
    <div v-if="showReadonlyMeta" class="system-meta-card">
      <div class="system-meta-item">
        <span>关联患者</span>
        <strong>{{ linkedPatientDisplay || '未关联' }}</strong>
      </div>
      <div class="system-meta-item">
        <span>首次到店</span>
        <strong>{{ form.arrived_at || '-' }}</strong>
      </div>
      <div class="system-meta-item">
        <span>首次成交</span>
        <strong>{{ form.deal_at || '-' }}</strong>
      </div>
    </div>

    <el-alert
      v-if="showPhoneOpenPrompt"
      :type="openConsultationAlertType"
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
        <span>该号码已是诊所客户 <strong>{{ phonePromptFlags.matchedPatientName }}</strong>，是否关联到该客户档案？</span>
        <div v-if="!isReadOnly" class="patient-match-actions">
          <el-button size="mini" type="primary" @click="linkMatchedPatient">关联</el-button>
          <el-button size="mini" plain @click="unlinkMatchedPatient">不关联</el-button>
        </div>
      </div>
    </el-alert>

    <el-form :model="form" label-width="110px" class="consultation-form">
      <el-form-item label="咨询时间">
        <el-date-picker
          v-model="form.consultation_time"
          type="datetime"
          value-format="yyyy-MM-dd HH:mm:ss"
          format="yyyy-MM-dd HH:mm"
          style="width:100%"
          :disabled="isReadOnly"
        />
      </el-form-item>
      <el-form-item label="咨询渠道">
        <el-select v-model="form.consultation_channel" style="width:100%" :disabled="isReadOnly" @change="handleChannelChange">
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
          :disabled="isReadOnly"
          :show-remark="false"
          @input="handleReferralChange"
        />
      </el-form-item>
      <el-form-item label="主诉项目">
        <el-select v-model="form.chief_project" style="width:100%" :disabled="isReadOnly" placeholder="请选择主诉项目">
          <el-option
            v-for="item in chiefProjectOptions"
            :key="item"
            :label="item"
            :value="item"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="意向强度">
        <div class="intent-group">
          <el-button
            v-for="item in intentOptions"
            :key="item.value"
            :type="intentButtonType(item)"
            :plain="form.intent_level !== item.value"
            :disabled="isReadOnly"
            @click="setIntentLevel(item.value)"
          >{{ item.label }}</el-button>
        </div>
        <div class="intent-tips">
          <div v-for="item in intentOptions" :key="`${item.value}-tip`" class="intent-tip-item">
            <span>{{ item.tip }}</span>
          </div>
        </div>
      </el-form-item>
      <el-form-item label="处理结果">
        <div class="result-group">
          <el-button
            v-for="item in handlingResultOptions"
            :key="item"
            :type="form.handling_result === item ? 'primary' : 'default'"
            :plain="form.handling_result !== item"
            :disabled="isReadOnly"
            @click="setHandlingResult(item)"
          >{{ item }}</el-button>
        </div>
      </el-form-item>
      <el-form-item label="关联患者" :required="form.handling_result === '已成交'">
        <el-select
          v-model="form.patient_id"
          style="width:100%"
          filterable
          remote
          reserve-keyword
          clearable
          placeholder="输入患者姓名/手机号搜索并关联"
          :remote-method="searchPatients"
          :loading="patientLoading"
          :disabled="isReadOnly"
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
          {{ form.handling_result === '已成交' ? '已成交必须关联患者档案。' : '可选：用于把咨询转化沉淀到具体患者。' }}
        </div>
      </el-form-item>
      <el-form-item label="咨询人姓名/昵称">
        <el-input v-model="form.contact_name" maxlength="50" :disabled="isReadOnly" />
      </el-form-item>
      <el-form-item label="联系方式">
        <el-input
          v-model="form.contact_phone"
          maxlength="11"
          placeholder="输入11位手机号"
          :disabled="isReadOnly"
          @blur="handlePhoneBlur"
        />
      </el-form-item>
      <el-form-item label="备注">
        <el-input
          v-model="form.remarks"
          type="textarea"
          :rows="4"
          maxlength="200"
          show-word-limit
          placeholder="简要记录客户的具体问题、关注点、特殊情况"
          :disabled="isReadOnly"
        />
      </el-form-item>
    </el-form>

    <span slot="footer" class="dialog-footer">
      <el-button @click="innerVisible = false">{{ isReadOnly ? '关闭' : '取消' }}</el-button>
      <el-button v-if="!isReadOnly" type="primary" :loading="saving" @click="submit">保存</el-button>
    </span>
  </el-dialog>
</template>

<script>
import axios from 'axios'
import ReferralSelector from '@/components/ReferralSelector.vue'
import { savePendingAppointmentPatient } from '@/utils/appointmentPrefill'
import { emitConsultationSaved } from '@/utils/consultationDialog'
import {
  CHIEF_PROJECT_OPTIONS,
  CONSULTATION_CHANNEL_OPTIONS,
  HANDLING_RESULT_OPTIONS,
  INTENT_LEVEL_OPTIONS
} from '@/utils/consultationOptions'

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
    remarks: '',
    arrived_at: '',
    deal_at: ''
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
  name: 'ConsultationRecordDialog',
  components: {
    ReferralSelector
  },
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    mode: {
      type: String,
      default: 'create'
    },
    record: {
      type: Object,
      default: () => ({})
    },
    currentUser: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      innerVisible: false,
      form: createEmptyForm(),
      referralForm: createEmptyReferralState(),
      phonePromptFlags: null,
      saving: false,
      patientLoading: false,
      patientOptions: [],
      consultationChannelOptions: CONSULTATION_CHANNEL_OPTIONS,
      chiefProjectOptions: CHIEF_PROJECT_OPTIONS,
      intentOptions: INTENT_LEVEL_OPTIONS,
      handlingResultOptions: HANDLING_RESULT_OPTIONS
    }
  },
  computed: {
    isReadOnly() {
      return this.mode === 'detail'
    },
    dialogTitle() {
      if (this.mode === 'detail') return '咨询记录详情'
      if (this.mode === 'edit') return '编辑咨询记录'
      return '记录咨询'
    },
    showReadonlyMeta() {
      return this.mode !== 'create'
    },
    linkedPatientDisplay() {
      const current = this.currentPatientOption
      if (current && current.name) {
        return current.phone ? `${current.name}｜${current.phone}` : current.name
      }
      if (this.phonePromptFlags && this.form.patient_id && this.phonePromptFlags.matchedPatientName) {
        return this.phonePromptFlags.matchedPatientName
      }
      return ''
    },
    currentPatientOption() {
      return (this.patientOptions || []).find(item => Number(item.id) === Number(this.form.patient_id || 0)) || null
    },
    showPhoneOpenPrompt() {
      return Number(this.phonePromptFlags && this.phonePromptFlags.openConsultationCount) >= 1
    },
    openConsultationAlertType() {
      return Number(this.phonePromptFlags && this.phonePromptFlags.openConsultationCount) >= 1 ? 'error' : 'warning'
    },
    openConsultationAlertTitle() {
      const count = Number(this.phonePromptFlags && this.phonePromptFlags.openConsultationCount) || 0
      return `该手机号已有 ${count} 条未成交咨询，请先确认是否重复录入后再保存。`
    },
    showReferralSelector() {
      return this.form.consultation_channel === '转介绍' || this.hasReferralPayload()
    }
  },
  watch: {
    visible: {
      immediate: true,
      handler(value) {
        this.innerVisible = value
        if (value) {
          this.initForm()
        }
      }
    },
    innerVisible(value) {
      this.$emit('update:visible', value)
    },
    record: {
      deep: true,
      handler() {
        if (this.innerVisible) {
          this.initForm()
        }
      }
    }
  },
  methods: {
    initForm() {
      const base = createEmptyForm()
      if (this.mode === 'create') {
        const now = new Date()
        const format = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')} ${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}:00`
        this.form = Object.assign(base, { consultation_time: format })
        this.referralForm = createEmptyReferralState()
        this.phonePromptFlags = null
        this.patientOptions = []
        return
      }
      const source = this.record || {}
      this.form = Object.assign(base, {
        id: source.id || null,
        patient_id: source.patient_id || null,
        consultation_time: source.consultation_time || '',
        consultation_channel: source.consultation_channel || '微信',
        chief_project: source.chief_project || '',
        intent_level: source.intent_level || '',
        handling_result: source.handling_result || '待跟进',
        contact_name: source.contact_name || '',
        contact_phone: source.contact_phone || '',
        remarks: source.remarks || '',
        arrived_at: source.arrived_at || '',
        deal_at: source.deal_at || ''
      })
      this.referralForm = Object.assign(createEmptyReferralState(), {
        referrer_type: source.referrer_type || '',
        referrer_patient_id: source.referrer_patient_id || null,
        referrer_patient_name: source.referrer_patient_name || '',
        external_referrer_type: source.external_referrer_type || '',
        external_referrer_name: source.external_referrer_name || '',
        external_referrer_contact: source.external_referrer_contact || ''
      })
      this.phonePromptFlags = null
      this.patientOptions = []
      this.ensureCurrentPatientOption(source)
    },
    handleChannelChange(value) {
      if (value !== '转介绍' && !this.isReadOnly) {
        this.referralForm = createEmptyReferralState()
      }
    },
    handleReferralChange(value) {
      this.referralForm = Object.assign(createEmptyReferralState(), value || {})
    },
    intentButtonType(item) {
      if (item.type === 'danger') return 'danger'
      if (item.type === 'warning') return 'warning'
      return 'info'
    },
    setIntentLevel(value) {
      if (this.isReadOnly) return
      this.form.intent_level = value
    },
    setHandlingResult(value) {
      if (this.isReadOnly) return
      this.form.handling_result = value
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
    linkMatchedPatient() {
      if (!this.phonePromptFlags || !this.phonePromptFlags.matchedPatientId) return
      this.form.patient_id = this.phonePromptFlags.matchedPatientId
      this.ensureCurrentPatientOption({
        patient_id: this.phonePromptFlags.matchedPatientId,
        patient_name: this.phonePromptFlags.matchedPatientName,
        patient_phone: this.form.contact_phone
      })
      this.$message.success(`已关联到客户档案：${this.phonePromptFlags.matchedPatientName}`)
    },
    unlinkMatchedPatient() {
      this.form.patient_id = null
      this.$message.info('本次咨询将不关联现有客户档案')
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
        const response = await axios.get('/patients/search', { params: { keyword, page: 1, size: 20 } })
        const data = response.data && response.data.data ? response.data.data : {}
        const list = Array.isArray(data.list) ? data.list : []
        this.patientOptions = list
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
      const selected = (this.patientOptions || []).find(item => Number(item.id) === Number(value || 0))
      this.form.patient_id = selected ? Number(selected.id) : null
      if (!selected) {
        return
      }
      if (!String(this.form.contact_name || '').trim()) {
        this.form.contact_name = selected.name || ''
      }
      if (!String(this.form.contact_phone || '').trim()) {
        this.form.contact_phone = selected.phone || ''
      }
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
      const currentUserId = Number(this.currentUser && this.currentUser.id)
      const currentUserName = String((this.currentUser && this.currentUser.name) || '').trim()
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
      if (this.mode === 'edit') {
        payload.updated_by = Number.isFinite(currentUserId) && currentUserId > 0 ? currentUserId : null
      } else {
        payload.created_by = Number.isFinite(currentUserId) && currentUserId > 0 ? currentUserId : null
        payload.created_by_name = currentUserName
      }
      return payload
    },
    submit() {
      const validationMessage = this.validateForm()
      if (validationMessage) {
        this.$message.warning(validationMessage)
        return
      }
      const request = this.mode === 'edit'
        ? axios.put('/consultations/edit', this.buildPayload())
        : axios.post('/consultations/add', this.buildPayload())
      this.saving = true
      request.then(response => {
        if (!response.data || response.data.code !== '200') {
          this.$message.error((response.data && response.data.msg) || '保存失败')
          return
        }
        const data = response.data.data || {}
        const record = data.record || response.data.data || {}
        const weekCount = Number(data.weekCount || 0)
        emitConsultationSaved({ record, mode: this.mode })
        this.$emit('saved', { record, response: data, mode: this.mode })
        this.innerVisible = false
        if (this.mode === 'create') {
          this.$message.success(`已记录咨询，本周共 ${weekCount} 条`)
          if (this.form.handling_result === '已预约到店') {
            this.promptCreateAppointment(record)
          }
        } else {
          this.$message.success('咨询记录已更新')
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
      const patientName = (record && record.patient_name) || this.phonePromptFlags && this.phonePromptFlags.matchedPatientName || this.form.contact_name
      this.$confirm('咨询记录已保存，是否立即创建预约？', '立即创建预约', {
        type: 'info',
        confirmButtonText: '去创建',
        cancelButtonText: '稍后'
      }).then(() => {
        savePendingAppointmentPatient({
          patient_id: this.form.patient_id || null,
          patient_name: patientName,
          patient_phone: this.form.contact_phone,
          appointment_purpose: this.form.chief_project
        })
        this.$router.push({ path: '/Appointment2', query: { fromConsultation: '1' } }).catch(() => {})
      }).catch(() => {})
    },
    handleClose() {
      this.phonePromptFlags = null
      this.referralForm = createEmptyReferralState()
      this.patientOptions = []
      this.$emit('close')
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
.system-meta-card {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 16px;
  padding: 14px 16px;
  border-radius: 14px;
  background: #f8fafc;
}

.system-meta-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  color: #475569;
}

.system-meta-item strong {
  color: #0f172a;
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

.consultation-form {
  padding-top: 4px;
}

.intent-group,
.result-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.intent-tips {
  margin-top: 10px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.intent-tip-item {
  color: #64748b;
  font-size: 12px;
  line-height: 1.5;
}

.field-tip {
  margin-top: 6px;
  color: #64748b;
  font-size: 12px;
  line-height: 1.5;
}

@media (max-width: 768px) {
  .system-meta-card {
    grid-template-columns: 1fr;
  }
}
</style>
