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

    <el-form :model="form" label-width="130px" class="consultation-form">
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
      <el-form-item label="录入人">
        <el-autocomplete
          v-model="form.created_by_name"
          style="width:100%"
          :fetch-suggestions="searchAccountsForAutocomplete"
          placeholder="可手动输入姓名，或输入关键字从列表选择"
          :disabled="isReadOnly"
          @select="handleAccountSelect"
          value-key="name"
          clearable
        >
          <template slot-scope="{ item }">
            <span>{{ item.name }}</span>
            <span style="color:#999; font-size:12px; margin-left:8px">{{ item.role || '' }}</span>
          </template>
        </el-autocomplete>
        <div class="field-tip">手动输入将只保存姓名；从列表选择可同步关联到系统账号。</div>
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
        <div v-if="patientSearchEmpty && !isReadOnly" class="field-tip field-tip--action">
          未找到匹配患者，
          <el-button type="text" size="mini" @click="goCreatePatient">立即创建患者档案</el-button>
          后返回关联
        </div>
        <div class="field-tip">
          {{ form.handling_result === '已成交' ? '已成交必须关联患者档案，用于统计累计消费金额。' : '关联后该咨询记录会同步到患者档案的「咨询记录」Tab 中。' }}
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
      <el-form-item label="预计消费金额">
        <el-input-number
          v-model="form.estimated_amount"
          :min="0"
          :precision="2"
          :controls="false"
          style="width:100%"
          placeholder="预估客户可能消费的金额"
          :disabled="isReadOnly"
        />
      </el-form-item>
      <el-form-item label="客户顾虑">
        <el-input
          v-model="form.customer_concerns"
          type="textarea"
          :rows="2"
          maxlength="500"
          show-word-limit
          placeholder="记录客户的顾虑点、对比的竞品机构、价格敏感度、决策周期等"
          :disabled="isReadOnly"
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

      <!-- AI 辅助区域 -->
      <div v-if="consultationAgentVisible && consultationAgentKey && !isReadOnly" class="ai-section">
        <el-divider>AI 辅助</el-divider>
        <div class="ai-actions">
          <el-button size="small" type="primary" plain :loading="aiLoading" icon="el-icon-magic-stick" @click="callAiAssist">
            {{ aiLoading ? 'AI 分析中...' : 'AI 辅助分析' }}
          </el-button>
          <span v-if="aiResult" class="ai-result-hint" style="color: #67c23a; font-size: 12px;">
            <i class="el-icon-success" /> 分析完成，结果已填入下方
          </span>
        </div>
        <el-alert
          v-if="aiResult"
          type="success"
          :closable="true"
          class="ai-score-alert"
          style="margin-top: 10px;"
          @close="aiResult = ''"
        >
          <div class="ai-response-block" style="white-space: pre-wrap;">{{ aiResult }}</div>
        </el-alert>
      </div>

      <!-- 跟进历史区域 -->
      <div v-if="mode !== 'create'" class="followup-section">
        <el-divider>跟进历史</el-divider>
        <el-timeline v-if="followups.length">
          <el-timeline-item
            v-for="item in followups"
            :key="item.id"
            :timestamp="item.followup_time"
            placement="top"
          >
            <el-card :body-style="{ padding: '10px 14px' }" shadow="never" class="followup-card">
              <div class="followup-content">{{ item.content }}</div>
              <div v-if="item.next_followup_time" class="followup-meta">
                下次计划：{{ item.next_followup_time }}
              </div>
              <div class="followup-meta">
                跟进人：{{ item.created_by_name || '-' }}
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else-if="!followupLoading" description="暂无跟进记录" />
        <div v-if="!isReadOnly" class="followup-add">
          <el-input
            v-model="newFollowup.content"
            type="textarea"
            :rows="2"
            maxlength="1000"
            show-word-limit
            placeholder="输入本次跟进内容"
          />
          <div class="followup-add-row">
            <el-date-picker
              v-model="newFollowup.next_followup_time"
              type="datetime"
              value-format="yyyy-MM-dd HH:mm:ss"
              format="yyyy-MM-dd HH:mm"
              placeholder="下次计划跟进时间"
              size="small"
              style="flex:1"
            />
            <el-button size="small" type="primary" :loading="followupLoading" @click="addFollowup">
              添加跟进
            </el-button>
          </div>
        </div>
      </div>

      <!-- 快速录入时的跟进内容（创建模式） -->
      <div v-if="mode === 'create' && !isReadOnly" class="followup-section">
        <el-divider>首次跟进</el-divider>
        <el-input
          v-model="newFollowup.content"
          type="textarea"
          :rows="2"
          maxlength="1000"
          show-word-limit
          placeholder="保存时自动创建一条跟进记录（可选）"
        />
        <div class="followup-add-row" style="margin-top:8px">
          <el-date-picker
            v-model="newFollowup.next_followup_time"
            type="datetime"
            value-format="yyyy-MM-dd HH:mm:ss"
            format="yyyy-MM-dd HH:mm"
            placeholder="下次计划跟进时间"
            size="small"
            style="flex:1"
          />
        </div>
      </div>

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
import { getAdminSession } from '@/utils/adminSession'
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
    estimated_amount: null,
    customer_concerns: '',
    ai_analysis_summary: '',
    ai_analysis_score: null,
    arrived_at: '',
    deal_at: '',
    created_by: null,
    created_by_name: ''
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
      patientSearchEmpty: false,
      accountLoading: false,
      accountOptions: [],
      lastSelectedAccount: null,
      consultationChannelOptions: CONSULTATION_CHANNEL_OPTIONS,
      chiefProjectOptions: CHIEF_PROJECT_OPTIONS,
      intentOptions: INTENT_LEVEL_OPTIONS,
      handlingResultOptions: HANDLING_RESULT_OPTIONS,
      followups: [],
      followupLoading: false,
      newFollowup: { content: '', next_followup_time: '' },
      consultationAgentKey: '',
      consultationAgentVisible: false,
      aiLoading: false,
      aiResult: ''
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
    },
    'form.created_by_name'(newVal) {
      if (this.form.created_by && this.lastSelectedAccount) {
        const selectedName = String(this.lastSelectedAccount.name || '').trim()
        if (String(newVal || '').trim() !== selectedName) {
          this.form.created_by = null
          this.lastSelectedAccount = null
        }
      }
    }
  },
  methods: {
    /**
     * 将对象递归格式化为可读文本（支持嵌套对象和数组）
     * @param {any} obj - 任意对象/数组/值
     * @param {number} indent - 当前缩进层级
     * @return {string} 格式化后的文本
     */
    formatObjectToText(obj, indent = 0) {
      if (obj == null) return ''
      if (typeof obj === 'string') return obj
      if (typeof obj === 'number' || typeof obj === 'boolean') return String(obj)
      if (Array.isArray(obj)) {
        if (obj.length === 0) return ''
        return obj
          .map(item => this.formatObjectToText(item, indent))
          .filter(item => item && item.trim())
          .join('\n')
      }
      if (typeof obj === 'object') {
        const prefix = '  '.repeat(indent)
        const lines = []
        for (const [key, value] of Object.entries(obj)) {
          const formattedValue = this.formatObjectToText(value, indent + 1)
          if (!formattedValue || !formattedValue.trim()) continue
          if (formattedValue.includes('\n') && indent === 0) {
            lines.push(`${prefix}${key}：`)
            lines.push(formattedValue)
          } else {
            lines.push(`${prefix}${key}：${formattedValue}`)
          }
        }
        return lines.join('\n')
      }
      return String(obj)
    },
    initForm() {
      const base = createEmptyForm()
      if (this.mode === 'create') {
        const now = new Date()
        const format = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')} ${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}:00`
        const preselectedId = this.record && this.record.patient_id ? Number(this.record.patient_id) : null
        const rawUser = this.currentUser || {}
        const currentUserId = Number(rawUser.id || rawUser.accountId || rawUser.userId || null)
        const currentUserName = String(rawUser.name || rawUser.username || '').trim()
        const createInit = { consultation_time: format, patient_id: preselectedId, created_by_name: currentUserName }
        this.lastSelectedAccount = null
        if (Number.isFinite(currentUserId) && currentUserId > 0) {
          createInit.created_by = currentUserId
          this.lastSelectedAccount = { id: currentUserId, name: currentUserName }
        }
        this.form = Object.assign(base, createInit)
        this.referralForm = createEmptyReferralState()
        this.phonePromptFlags = null
        this.patientOptions = []
        this.patientSearchEmpty = false
        this.accountOptions = []
        this.followups = []
        this.newFollowup = { content: '', next_followup_time: '' }
        this.aiResult = ''
        this.loadAiFunctionMapping()
        if (preselectedId && preselectedId > 0) {
          this.ensureCurrentPatientOption(this.record)
        }
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
        estimated_amount: source.estimated_amount || null,
        customer_concerns: source.customer_concerns || '',
        ai_analysis_summary: source.ai_analysis_summary || '',
        ai_analysis_score: source.ai_analysis_score || null,
        arrived_at: source.arrived_at || '',
        deal_at: source.deal_at || '',
        created_by: source.created_by || null,
        created_by_name: source.created_by_name || ''
      })
      this.accountOptions = []
      this.lastSelectedAccount = null
      if (source.created_by) {
        const accountItem = { id: source.created_by, name: source.created_by_name || '' }
        this.accountOptions = [accountItem]
        this.lastSelectedAccount = { ...accountItem }
      }
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
      this.patientSearchEmpty = false
      this.newFollowup = { content: '', next_followup_time: '' }
      this.aiResult = ''
      this.loadAiFunctionMapping()
      this.ensureCurrentPatientOption(source)
      if (this.mode !== 'create' && this.form.id) {
        this.loadFollowups()
      }
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
      this.patientSearchEmpty = false
      try {
        const response = await axios.get('/patients/search', { params: { keyword, page: 1, size: 20 } })
        const data = response.data && response.data.data ? response.data.data : {}
        const list = Array.isArray(data.list) ? data.list : []
        this.patientOptions = list
        this.patientSearchEmpty = keyword.length > 0 && list.length === 0
        if (current) {
          this.ensureCurrentPatientOption(current)
        }
      } catch (error) {
        this.patientOptions = current ? [current] : []
        this.patientSearchEmpty = false
      } finally {
        this.patientLoading = false
      }
    },
    goCreatePatient() {
      const name = String(this.form.contact_name || '').trim()
      const phone = String(this.form.contact_phone || '').trim()
      const query = {}
      if (name) query.name = name
      if (phone && /^\d{11}$/.test(phone)) query.phone = phone
      this.$router.push({ path: '/Patient', query })
      this.innerVisible = false
    },
    searchAccountsForAutocomplete(queryString, cb) {
      if (!queryString) { cb([]); return }
      axios.get('/accounts/selectByname', { params: { name: queryString, page: 1, size: 20 } })
        .then(response => {
          const data = response.data && response.data.data ? response.data.data : {}
          const list = Array.isArray(data.list) ? data.list : []
          cb(list.map(item => ({ ...item, value: item.name })))
        })
        .catch(() => cb([]))
    },
    handleAccountSelect(item) {
      if (item && item.id) {
        this.form.created_by = Number(item.id)
        this.form.created_by_name = String(item.name || '').trim()
        this.lastSelectedAccount = { ...item }
      } else {
        this.form.created_by = null
        this.lastSelectedAccount = null
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
      const rawUser = this.currentUser || {}
      const currentUserId = Number(
        rawUser.id || rawUser.accountId || rawUser.userId || null
      )
      const currentUserName = String(rawUser.name || rawUser.username || '').trim()
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
        remarks: String(this.form.remarks || '').trim(),
        estimated_amount: this.form.estimated_amount || null,
        customer_concerns: String(this.form.customer_concerns || '').trim(),
        ai_analysis_summary: String(this.form.ai_analysis_summary || '').trim(),
        ai_analysis_score: this.form.ai_analysis_score || null
      }
      if (this.mode === 'edit') {
        payload.updated_by = Number.isFinite(currentUserId) && currentUserId > 0 ? currentUserId : null
      }
      const selectedCreatedBy = Number(this.form.created_by || 0)
      if (selectedCreatedBy > 0) {
        payload.created_by = selectedCreatedBy
      } else {
        payload.created_by = null
      }
      payload.created_by_name = String(this.form.created_by_name || '').trim() || currentUserName
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

        // 如果有新增跟进内容，自动创建跟进记录
        if (this.mode === 'create' && this.newFollowup.content) {
          this.autoCreateFollowup(record.id)
        }

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
        const data = error && error.response && error.response.data ? error.response.data : null
        const status = error && error.response ? error.response.status : ''
        let message = data && data.msg ? data.msg : null
        if (!message && data) {
          try {
            message = status + ': ' + JSON.stringify(data)
          } catch (e) {
            message = status + ': ' + String(data)
          }
        }
        this.$message.error(message || '保存失败')
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
        this.$router.push({ path: '/Appointment', query: { fromConsultation: '1' } }).catch(() => {})
      }).catch(() => {})
    },
    async loadAiFunctionMapping() {
      try {
        const session = getAdminSession() || {}
        const accountId = session.id || ''
        const res = await axios.get('/api/ai/function-mappings/consultation-assist/agent-key', { params: { accountId } })
        if (res.data && res.data.code === '200' && res.data.data) {
          const data = res.data.data
          if (data.agentKey && data.agentKey.trim() !== '') {
            this.consultationAgentKey = data.agentKey
            this.consultationAgentVisible = data.isVisibleOnPage === true || data.isVisibleOnPage === 1
          } else {
            this.consultationAgentKey = ''
            this.consultationAgentVisible = false
          }
        } else {
          this.consultationAgentKey = ''
          this.consultationAgentVisible = false
        }
      } catch (error) {
        console.error('加载咨询辅助功能映射失败:', error)
        this.consultationAgentKey = ''
        this.consultationAgentVisible = false
      }
    },
    async callAiAssist() {
      if (!this.consultationAgentKey) {
        this.$message.warning('未绑定 AI Agent，请在 AI 总览页配置')
        return
      }
      const message = [
        '【咨询信息】',
        `主诉项目：${this.form.chief_project || '-'}`,
        `意向强度：${this.form.intent_level || '-'}`,
        `处理结果：${this.form.handling_result || '-'}`,
        `客户顾虑：${this.form.customer_concerns || '-'}`,
        `备注：${this.form.remarks || '-'}`,
        `预计金额：${this.form.estimated_amount || '-'}`,
        `咨询渠道：${this.form.consultation_channel || '-'}`,
        `联系方式：${this.form.contact_name || '-'} ${this.form.contact_phone || ''}`
      ].join('\n')
      this.aiLoading = true
      try {
        const session = getAdminSession() || {}
        const accountId = session.id || ''
        const res = await axios.post(`/api/ai/proxy/${this.consultationAgentKey}`, { message, account_id: accountId })
        if (res.data && res.data.code === '200') {
          const resultData = res.data.data
          let text = ''
          if (typeof resultData === 'string') {
            text = resultData
          } else if (resultData && typeof resultData === 'object') {
            const candidateKeys = ['content', 'reply', 'message', 'data']
            for (const k of candidateKeys) {
              if (resultData[k] != null) {
                const val = resultData[k]
                if (typeof val === 'string') {
                  text = val
                } else if (typeof val === 'object') {
                  text = this.formatObjectToText(val)
                } else {
                  text = String(val)
                }
                break
              }
            }
            if (!text) {
              text = JSON.stringify(resultData, null, 2)
            }
          }
          this.aiResult = text
          this.$message.success('AI 分析完成')
        } else {
          this.$message.error(res.data?.msg || 'AI 调用失败')
        }
      } catch (error) {
        console.error('AI 辅助调用失败:', error)
        this.$message.error('AI 调用失败，请检查 Agent 配置')
      } finally {
        this.aiLoading = false
      }
    },
    handleClose() {
      this.phonePromptFlags = null
      this.referralForm = createEmptyReferralState()
      this.patientOptions = []
      this.patientSearchEmpty = false
      this.accountOptions = []
      this.lastSelectedAccount = null
      this.followups = []
      this.newFollowup = { content: '', next_followup_time: '' }
      this.aiResult = ''
      this.consultationAgentKey = ''
      this.consultationAgentVisible = false
      this.$emit('close')
    },
    async loadFollowups() {
      if (!this.form.id) return
      this.followupLoading = true
      try {
        const response = await axios.get(`/consultations/${this.form.id}/followups`)
        const data = response.data && response.data.data ? response.data.data : []
        this.followups = Array.isArray(data) ? data : []
      } catch (error) {
        this.followups = []
      } finally {
        this.followupLoading = false
      }
    },
    async addFollowup() {
      const content = String(this.newFollowup.content || '').trim()
      if (!content) {
        this.$message.warning('请输入跟进内容')
        return
      }
      const rawUser = this.currentUser || {}
      const currentUserId = Number(rawUser.id || rawUser.accountId || rawUser.userId || null)
      const currentUserName = String(rawUser.name || rawUser.username || '').trim()
      this.followupLoading = true
      try {
        const response = await axios.post('/consultations/followups/add', {
          consultation_id: this.form.id,
          content: content,
          next_followup_time: this.newFollowup.next_followup_time || null,
          created_by: currentUserId,
          created_by_name: currentUserName
        })
        if (response.data && response.data.code === '200') {
          this.$message.success('跟进记录已添加')
          this.newFollowup = { content: '', next_followup_time: '' }
          this.loadFollowups()
        } else {
          this.$message.error((response.data && response.data.msg) || '添加失败')
        }
      } catch (error) {
        const msg = error && error.response && error.response.data && error.response.data.msg
        this.$message.error(msg || '添加跟进失败')
      } finally {
        this.followupLoading = false
      }
    },
    async autoCreateFollowup(consultationId) {
      const content = String(this.newFollowup.content || '').trim()
      if (!content || !consultationId) return
      const rawUser = this.currentUser || {}
      const currentUserId = Number(rawUser.id || rawUser.accountId || rawUser.userId || null)
      const currentUserName = String(rawUser.name || rawUser.username || '').trim()
      try {
        await axios.post('/consultations/followups/add', {
          consultation_id: consultationId,
          content: content,
          next_followup_time: this.newFollowup.next_followup_time || null,
          created_by: currentUserId,
          created_by_name: currentUserName
        })
      } catch (error) {
        console.error('自动创建跟进记录失败', error)
      }
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

.field-tip--action {
  color: #e6a23c;
}

.followup-section {
  margin-top: 8px;
}

.followup-card {
  background: #f8fafc;
  border-radius: 10px;
}

.followup-content {
  color: #0f172a;
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
}

.followup-meta {
  margin-top: 6px;
  color: #64748b;
  font-size: 12px;
}

.followup-add {
  margin-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.followup-add-row {
  display: flex;
  gap: 8px;
  align-items: center;
}

.ai-section {
  margin-top: 8px;
}

.ai-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.ai-result-text {
  color: #475569;
  font-size: 13px;
}

.ai-score-alert {
  margin-top: 10px;
}

.ai-response-block {
  margin-top: 12px;
  padding: 12px 14px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
}


@media (max-width: 768px) {
  .system-meta-card {
    grid-template-columns: 1fr;
  }

  .followup-add-row {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
