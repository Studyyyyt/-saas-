<template>
  <div class="referral-selector">
    <el-radio-group v-model="form.referrer_type" size="mini" :disabled="disabled" @change="handleTypeChange">
      <el-radio-button label="">未填写</el-radio-button>
      <el-radio-button label="patient">现有客户</el-radio-button>
      <el-radio-button label="external">外部介绍人</el-radio-button>
    </el-radio-group>

    <div v-if="form.referrer_type === 'patient'" class="referral-panel">
      <el-select
        v-model="form.referrer_patient_id"
        style="width:100%"
        filterable
        remote
        reserve-keyword
        clearable
        placeholder="输入患者姓名/手机号搜索介绍客户"
        :remote-method="searchPatients"
        :loading="patientLoading"
        :disabled="disabled"
        @change="handlePatientChange"
      >
        <el-option
          v-for="item in patientOptions"
          :key="item.id"
          :label="formatPatientOption(item)"
          :value="item.id"
        />
      </el-select>
      <div class="field-tip">介绍人为现有客户时，请从下拉搜索结果里选择。</div>
    </div>

    <div v-else-if="form.referrer_type === 'external'" class="referral-panel referral-panel--external">
      <el-input
        v-model="form.external_referrer_name"
        maxlength="50"
        :disabled="disabled"
        placeholder="请输入外部介绍人姓名"
      />
      <el-select
        v-model="form.external_referrer_type"
        style="width:100%"
        clearable
        :disabled="disabled"
        placeholder="可选：介绍人类型"
      >
        <el-option
          v-for="item in externalTypeOptions"
          :key="item"
          :label="item"
          :value="item"
        />
      </el-select>
      <el-input
        v-model="form.external_referrer_contact"
        maxlength="50"
        :disabled="disabled"
        placeholder="可选：联系方式"
      />
    </div>

    <el-input
      v-if="showRemark && form.referrer_type"
      v-model="form.referral_remark"
      class="referral-remark"
      type="textarea"
      :rows="2"
      maxlength="200"
      show-word-limit
      :disabled="disabled"
      placeholder="可选：补充说明介绍场景"
    />
  </div>
</template>

<script>
import axios from 'axios'

function buildEmptyReferral() {
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

function normalizeText(value) {
  return String(value || '').trim()
}

export default {
  name: 'ReferralSelector',
  props: {
    value: {
      type: Object,
      default: () => ({})
    },
    disabled: {
      type: Boolean,
      default: false
    },
    currentPatientId: {
      type: [Number, String],
      default: null
    },
    externalTypeOptions: {
      type: Array,
      default: () => ['家属', '朋友', '同事', '合作方', '其他']
    },
    showRemark: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      syncing: false,
      patientLoading: false,
      patientOptions: [],
      form: buildEmptyReferral()
    }
  },
  watch: {
    value: {
      immediate: true,
      deep: true,
      handler(value) {
        this.syncing = true
        this.form = this.normalizeForm(value)
        this.ensureCurrentPatientOption(this.form)
        this.$nextTick(() => {
          this.syncing = false
        })
      }
    },
    form: {
      deep: true,
      handler(value) {
        if (this.syncing) return
        this.$emit('input', this.normalizeOutput(value))
      }
    }
  },
  methods: {
    normalizeForm(value = {}) {
      const form = Object.assign(buildEmptyReferral(), value || {})
      const referrerType = normalizeText(form.referrer_type)
      if (referrerType === 'patient') {
        form.referrer_type = 'patient'
      } else if (referrerType === 'external') {
        form.referrer_type = 'external'
      } else if (Number(form.referrer_patient_id) > 0 || normalizeText(form.referrer_patient_name)) {
        form.referrer_type = 'patient'
      } else if (normalizeText(form.external_referrer_name) || normalizeText(form.external_referrer_contact) || normalizeText(form.external_referrer_type)) {
        form.referrer_type = 'external'
      } else {
        form.referrer_type = ''
      }
      form.referrer_patient_id = Number(form.referrer_patient_id) > 0 ? Number(form.referrer_patient_id) : null
      form.referrer_patient_name = normalizeText(form.referrer_patient_name)
      form.external_referrer_type = normalizeText(form.external_referrer_type)
      form.external_referrer_name = normalizeText(form.external_referrer_name)
      form.external_referrer_contact = normalizeText(form.external_referrer_contact)
      form.referral_remark = normalizeText(form.referral_remark)
      return form
    },
    normalizeOutput(value = {}) {
      const form = this.normalizeForm(value)
      if (form.referrer_type === 'patient') {
        return {
          referrer_type: 'patient',
          referrer_patient_id: form.referrer_patient_id || null,
          referrer_patient_name: form.referrer_patient_name || '',
          external_referrer_type: '',
          external_referrer_name: '',
          external_referrer_contact: '',
          referral_remark: form.referral_remark || ''
        }
      }
      if (form.referrer_type === 'external') {
        return {
          referrer_type: 'external',
          referrer_patient_id: null,
          referrer_patient_name: '',
          external_referrer_type: form.external_referrer_type || '',
          external_referrer_name: form.external_referrer_name || '',
          external_referrer_contact: form.external_referrer_contact || '',
          referral_remark: form.referral_remark || ''
        }
      }
      return buildEmptyReferral()
    },
    handleTypeChange(value) {
      if (value === 'patient') {
        this.form = Object.assign(buildEmptyReferral(), {
          referrer_type: 'patient',
          referral_remark: this.form.referral_remark || ''
        })
        return
      }
      if (value === 'external') {
        this.form = Object.assign(buildEmptyReferral(), {
          referrer_type: 'external',
          referral_remark: this.form.referral_remark || ''
        })
        return
      }
      this.form = buildEmptyReferral()
      this.patientOptions = []
    },
    formatPatientOption(item) {
      if (!item) return ''
      return [item.name || '', item.phone || '', item.customer_source || ''].filter(Boolean).join('｜')
    },
    ensureCurrentPatientOption(source = {}) {
      const patientId = Number(source && source.referrer_patient_id)
      if (!Number.isFinite(patientId) || patientId <= 0) {
        return
      }
      const exists = (this.patientOptions || []).some(item => Number(item.id) === patientId)
      if (exists) {
        return
      }
      this.patientOptions = [{
        id: patientId,
        name: source.referrer_patient_name || '',
        phone: '',
        customer_source: ''
      }].concat(this.patientOptions || [])
    },
    async searchPatients(query) {
      const keyword = normalizeText(query)
      const current = (this.patientOptions || []).find(item => Number(item.id) === Number(this.form.referrer_patient_id || 0)) || null
      if (!keyword) {
        this.patientOptions = current ? [current] : []
        return
      }
      this.patientLoading = true
      try {
        const response = await axios.get('/patients/search', { params: { keyword, page: 1, size: 20 } })
        const data = response.data && response.data.data ? response.data.data : {}
        const currentPatientId = Number(this.currentPatientId || 0)
        const list = (Array.isArray(data.list) ? data.list : []).filter(item => Number(item.id || 0) !== currentPatientId)
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
      const patient = (this.patientOptions || []).find(item => Number(item.id) === Number(value || 0))
      this.form.referrer_patient_id = patient ? Number(patient.id) : null
      this.form.referrer_patient_name = patient ? normalizeText(patient.name) : ''
    }
  }
}
</script>

<style scoped>
.referral-selector {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.referral-panel {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.field-tip {
  color: #909399;
  font-size: 12px;
  line-height: 1.5;
}

.referral-remark {
  margin-top: 2px;
}
</style>
