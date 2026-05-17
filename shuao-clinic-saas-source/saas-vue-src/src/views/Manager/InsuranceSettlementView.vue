<template>
  <div class="insurance-page">
    <el-card class="page-card" shadow="never">
      <div class="page-head">
        <div>
          <div class="page-kicker">医保管理</div>
          <h2>医保结算</h2>
          <p>管理医保结算草稿与结算记录。当前阶段用于准备 mock 报文与梳理门诊收费拆分结构。</p>
        </div>
        <el-button type="primary" @click="openCreateDialog">新增结算草稿</el-button>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <div class="query-row">
        <el-input-number v-model="queryPatientId" :min="1" controls-position="right" placeholder="患者ID"></el-input-number>
        <el-button type="primary" icon="el-icon-search" @click="loadSettlements">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </div>
      <el-table :data="settlements" stripe style="margin-top: 16px" :header-cell-style="{ backgroundColor: '#f8fafc', color: '#475569', fontWeight: '600' }">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="patient_id" label="患者ID" width="100"></el-table-column>
        <el-table-column prop="visit_no" label="就诊流水号" min-width="160"></el-table-column>
        <el-table-column prop="settlement_no" label="医保结算单号" min-width="160"></el-table-column>
        <el-table-column prop="biz_type" label="业务类型" width="160"></el-table-column>
        <el-table-column prop="settlement_status" label="结算状态" width="120"></el-table-column>
        <el-table-column prop="upload_status" label="上传状态" width="130"></el-table-column>
        <el-table-column prop="total_amount" label="总金额" width="110"></el-table-column>
        <el-table-column prop="insurance_amount" label="医保支付" width="110"></el-table-column>
        <el-table-column prop="personal_amount" label="个人金额" width="110"></el-table-column>
        <el-table-column prop="cash_amount" label="现金金额" width="110"></el-table-column>
      </el-table>
      <el-empty v-if="!settlements.length" description="暂无医保结算记录"></el-empty>
    </el-card>

    <el-dialog title="新增医保结算草稿" :visible.sync="dialogVisible" width="720px">
      <el-form :model="form" label-width="120px">
        <el-form-item label="患者选择">
          <el-select
            v-model="form.patient_id"
            filterable
            remote
            reserve-keyword
            clearable
            placeholder="请输入患者姓名搜索并选择患者"
            :remote-method="searchPatientOptions"
            :loading="patientLoading"
            style="width:100%"
            @focus="ensurePatientOptionsLoaded"
            @change="handlePatientChange"
          >
            <el-option
              v-for="item in patientOptions"
              :key="item.id"
              :label="formatPatientOption(item)"
              :value="item.id"
            ></el-option>
          </el-select>
          <div v-if="selectedPatient" class="selected-patient-card">
            <div><strong>已选患者：</strong>{{ selectedPatient.name }}</div>
            <div class="selected-patient-meta">
              ID：{{ selectedPatient.id }}
              <span v-if="selectedPatient.gender">｜性别：{{ selectedPatient.gender }}</span>
              <span v-if="selectedPatient.phone">｜电话：{{ selectedPatient.phone }}</span>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="最近收费记录">
          <el-select
            v-model="selectedFinanceId"
            clearable
            filterable
            placeholder="选择收费记录后可自动回填金额"
            :loading="financeLoading"
            style="width:100%"
            @change="handleFinanceRecordChange"
          >
            <el-option
              v-for="item in financeOptions"
              :key="item.id"
              :label="formatFinanceOption(item)"
              :value="item.id"
            ></el-option>
          </el-select>
          <div class="field-helper">当前按 patient_id 精确加载最近收费记录；选中后自动带出财务记录ID、总金额与现金金额。</div>
        </el-form-item>

        <el-form-item label="最近治疗记录">
          <el-select
            v-model="selectedTreatmentId"
            clearable
            filterable
            placeholder="选择治疗记录后可自动回填治疗信息"
            :loading="treatmentLoading"
            style="width:100%"
            @change="handleTreatmentRecordChange"
          >
            <el-option
              v-for="item in treatmentOptions"
              :key="item.id"
              :label="formatTreatmentOption(item)"
              :value="item.id"
            ></el-option>
          </el-select>
          <div class="field-helper">当前按 patient_id 精确加载最近治疗记录；选中后自动带出治疗记录ID、业务类型建议与备注。</div>
        </el-form-item>

        <el-form-item label="财务记录ID"><el-input-number v-model="form.finance_id" :min="0" controls-position="right" style="width:100%" /></el-form-item>
        <el-form-item label="治疗记录ID"><el-input-number v-model="form.treatment_id" :min="0" controls-position="right" style="width:100%" /></el-form-item>
        <el-form-item label="业务类型"><el-input v-model="form.biz_type" placeholder="OUTPATIENT_SETTLEMENT" /></el-form-item>
        <el-form-item label="总金额"><el-input-number v-model="form.total_amount" :min="0" :precision="2" controls-position="right" style="width:100%" @change="handleTotalAmountChange" /></el-form-item>
        <el-form-item label="医保支付"><el-input-number v-model="form.insurance_amount" :min="0" :precision="2" controls-position="right" style="width:100%" @change="handleSplitAmountChange" /></el-form-item>
        <el-form-item label="个人金额"><el-input-number v-model="form.personal_amount" :min="0" :precision="2" controls-position="right" style="width:100%" @change="handleSplitAmountChange" /></el-form-item>
        <el-form-item label="现金金额"><el-input-number v-model="form.cash_amount" :min="0" :precision="2" controls-position="right" style="width:100%" @change="markCashManuallyEdited" /></el-form-item>
        <el-form-item label="金额校验">
          <div class="amount-summary">
            <div class="amount-line">
              <span>拆分合计：</span>
              <strong>{{ splitTotalDisplay }}</strong>
              <span class="amount-gap">差额：{{ amountDifferenceDisplay }}</span>
            </div>
            <el-alert
              :title="amountBalanceMessage"
              :type="isAmountBalanced ? 'success' : 'warning'"
              :closable="false"
              show-icon
            ></el-alert>
            <div class="amount-helper">默认会自动把“现金金额”补成差额；如果你手动改过现金金额，就按你填写的值校验。</div>
          </div>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="createDraft">保存草稿</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios'
import { showApiError } from '@/utils/errorMessage'

const emptyForm = () => ({
  patient_id: null,
  finance_id: null,
  treatment_id: null,
  biz_type: 'OUTPATIENT_SETTLEMENT',
  total_amount: 0,
  insurance_amount: 0,
  personal_amount: 0,
  cash_amount: 0,
  remark: ''
})

export default {
  name: 'InsuranceSettlementView',
  data() {
    return {
      queryPatientId: null,
      settlements: [],
      dialogVisible: false,
      form: emptyForm(),
      cashManuallyEdited: false,
      patientLoading: false,
      patientOptions: [],
      selectedPatient: null,
      financeLoading: false,
      financeOptions: [],
      selectedFinanceId: null,
      treatmentLoading: false,
      treatmentOptions: [],
      selectedTreatmentId: null
    }
  },
  computed: {
    splitTotal() {
      return this.toAmount(this.form.insurance_amount) + this.toAmount(this.form.personal_amount) + this.toAmount(this.form.cash_amount)
    },
    amountDifference() {
      return this.roundAmount(this.toAmount(this.form.total_amount) - this.splitTotal)
    },
    isAmountBalanced() {
      return Math.abs(this.amountDifference) <= 0.01
    },
    amountBalanceMessage() {
      if (this.isAmountBalanced) {
        return '金额平衡，可提交创建医保结算草稿'
      }
      return '金额未平衡，请检查医保支付、个人金额、现金金额之和是否等于总金额'
    },
    splitTotalDisplay() {
      return this.formatAmount(this.splitTotal)
    },
    amountDifferenceDisplay() {
      return this.formatAmount(this.amountDifference)
    }
  },
  mounted() {
    this.loadSettlements()
  },
  methods: {
    loadSettlements() {
      const params = {}
      if (this.queryPatientId) params.patientId = this.queryPatientId
      axios.get('/insurances/settlements', { params }).then(res => {
        if (res.data.code === '200') {
          this.settlements = res.data.data || []
        } else {
          this.$message.error(res.data.msg || '加载医保结算失败')
        }
      }).catch(() => {
        showApiError(this, '加载医保结算', error)
      })
    },
    resetQuery() {
      this.queryPatientId = null
      this.loadSettlements()
    },
    openCreateDialog() {
      this.dialogVisible = true
      this.form = emptyForm()
      this.cashManuallyEdited = false
      this.selectedPatient = null
      this.selectedFinanceId = null
      this.selectedTreatmentId = null
      this.financeOptions = []
      this.treatmentOptions = []
      this.autoFillCashAmount()
      this.ensurePatientOptionsLoaded()
    },
    ensurePatientOptionsLoaded() {
      if (this.patientOptions.length) return
      this.fetchPatientOptions('')
    },
    searchPatientOptions(keyword) {
      this.fetchPatientOptions(keyword)
    },
    fetchPatientOptions(keyword) {
      this.patientLoading = true
      const params = { page: 1, size: 100 }
      let url = '/patients/selectAll'
      if (keyword && keyword.trim()) {
        url = `/patients/selectByname?name=${encodeURIComponent(keyword.trim())}`
      }
      axios.get(url, { params }).then(res => {
        const data = (res.data && res.data.data) || {}
        const list = Array.isArray(data.list) ? data.list : []
        this.patientOptions = list
        this.syncSelectedPatient()
      }).catch(() => {
        showApiError(this, '加载患者列表', error)
      }).finally(() => {
        this.patientLoading = false
      })
    },
    handlePatientChange(value) {
      if (!value) {
        this.selectedPatient = null
        this.selectedFinanceId = null
        this.selectedTreatmentId = null
        this.financeOptions = []
        this.treatmentOptions = []
        this.form.finance_id = null
        this.form.treatment_id = null
        return
      }
      this.syncSelectedPatient()
      this.loadPatientRelatedOptions()
    },
    syncSelectedPatient() {
      const matched = this.patientOptions.find(item => String(item.id) === String(this.form.patient_id))
      this.selectedPatient = matched || this.selectedPatient
    },
    loadPatientRelatedOptions() {
      if (!this.form.patient_id) return
      this.loadFinanceOptionsByPatientId(this.form.patient_id)
      this.loadTreatmentOptionsByPatientId(this.form.patient_id)
    },
    loadFinanceOptionsByPatientId(patientId) {
      this.financeLoading = true
      axios.get('/finances/recentByPatientId', { params: { patientId, limit: 20 } }).then(res => {
        this.financeOptions = Array.isArray(res.data && res.data.data) ? res.data.data : []
      }).catch(() => {
        this.financeOptions = []
        showApiError(this, '加载患者收费记录', error)
      }).finally(() => {
        this.financeLoading = false
      })
    },
    loadTreatmentOptionsByPatientId(patientId) {
      this.treatmentLoading = true
      axios.get('/treatments/recentByPatientId', { params: { patientId, limit: 20 } }).then(res => {
        this.treatmentOptions = Array.isArray(res.data && res.data.data) ? res.data.data : []
      }).catch(() => {
        this.treatmentOptions = []
        showApiError(this, '加载患者治疗记录', error)
      }).finally(() => {
        this.treatmentLoading = false
      })
    },
    handleFinanceRecordChange(value) {
      const selected = this.financeOptions.find(item => String(item.id) === String(value))
      if (!selected) {
        this.form.finance_id = null
        return
      }
      this.form.finance_id = selected.id
      this.form.total_amount = this.toAmount(selected.amount)
      this.form.insurance_amount = 0
      this.form.personal_amount = 0
      this.cashManuallyEdited = false
      this.autoFillCashAmount()
      if (!this.form.remark) {
        this.form.remark = selected.remark || ''
      }
    },
    handleTreatmentRecordChange(value) {
      const selected = this.treatmentOptions.find(item => String(item.id) === String(value))
      if (!selected) {
        this.form.treatment_id = null
        return
      }
      this.form.treatment_id = selected.id
      if (!this.form.biz_type || this.form.biz_type === 'OUTPATIENT_SETTLEMENT') {
        this.form.biz_type = selected.appointment_purpose ? `OUTPATIENT_SETTLEMENT-${selected.appointment_purpose}` : 'OUTPATIENT_SETTLEMENT'
      }
      if (!this.form.remark) {
        const parts = [selected.appointment_purpose, selected.treatment_content, selected.treatment_product].filter(Boolean)
        this.form.remark = parts.join('｜')
      }
      const fee = this.toAmount(selected.treatment_fee)
      if (fee > 0 && this.toAmount(this.form.total_amount) === 0) {
        this.form.total_amount = fee
        this.cashManuallyEdited = false
        this.autoFillCashAmount()
      }
    },
    formatPatientOption(item) {
      if (!item) return ''
      const phone = item.phone ? `｜${item.phone}` : ''
      return `${item.name || '未命名患者'}（ID:${item.id}）${phone}`
    },
    formatFinanceOption(item) {
      if (!item) return ''
      return `收费#${item.id}｜${item.type || '未分类'}｜金额:${this.formatAmount(item.amount)}｜日期:${item.date || '-'}${item.remark ? `｜${item.remark}` : ''}`
    },
    formatTreatmentOption(item) {
      if (!item) return ''
      return `治疗#${item.id}｜${item.appointment_purpose || '未填写项目'}｜费用:${this.formatAmount(item.treatment_fee)}｜日期:${item.treatment_date || '-'}${item.doctor_name ? `｜医生:${item.doctor_name}` : ''}`
    },
    handleTotalAmountChange() {
      this.autoFillCashAmount()
    },
    handleSplitAmountChange() {
      this.autoFillCashAmount()
    },
    markCashManuallyEdited() {
      this.cashManuallyEdited = true
    },
    autoFillCashAmount() {
      if (this.cashManuallyEdited) return
      const total = this.toAmount(this.form.total_amount)
      const insurance = this.toAmount(this.form.insurance_amount)
      const personal = this.toAmount(this.form.personal_amount)
      const cash = this.roundAmount(total - insurance - personal)
      this.form.cash_amount = cash >= 0 ? cash : 0
    },
    createDraft() {
      if (!this.form.patient_id) {
        this.$message.warning('请选择患者')
        return
      }
      if (!this.isAmountBalanced) {
        this.$message.warning('金额未平衡，暂不能创建草稿')
        return
      }
      const payload = Object.assign({}, this.form, {
        total_amount: this.toAmount(this.form.total_amount),
        insurance_amount: this.toAmount(this.form.insurance_amount),
        personal_amount: this.toAmount(this.form.personal_amount),
        cash_amount: this.toAmount(this.form.cash_amount)
      })
      axios.post('/insurances/settlements/draft', payload).then(res => {
        if (res.data.code === '200') {
          const draft = res.data.data || {}
          this.$alert(
            `医保结算草稿创建成功\n\n医保结算单号：${draft.settlement_no || '-'}\n就诊流水号：${draft.visit_no || '-'}`,
            '创建成功',
            { confirmButtonText: '知道了' }
          )
          this.dialogVisible = false
          this.form = emptyForm()
          this.cashManuallyEdited = false
          this.selectedPatient = null
          this.selectedFinanceId = null
          this.selectedTreatmentId = null
          this.loadSettlements()
        } else {
          this.$message.error(res.data.msg || '创建医保结算草稿失败')
        }
      }).catch(() => {
        this.$message.error('创建医保结算草稿失败')
      })
    },
    toAmount(value) {
      const num = Number(value)
      return Number.isFinite(num) ? num : 0
    },
    roundAmount(value) {
      return Math.round(this.toAmount(value) * 100) / 100
    },
    formatAmount(value) {
      return this.roundAmount(value).toFixed(2)
    }
  }
}
</script>

<style scoped>
.insurance-page { display:flex; flex-direction:column; gap:18px; }
.page-card, .table-card { border-radius:18px; }
.page-head { display:flex; justify-content:space-between; align-items:flex-start; gap:20px; }
.page-kicker { color:#2563eb; font-size:13px; font-weight:600; margin-bottom:8px; }
.page-head h2 { margin:0; font-size:28px; color:#0f172a; }
.page-head p { margin:10px 0 0; color:#64748b; line-height:1.7; max-width:760px; }
.query-row { display:flex; gap:10px; align-items:center; }
.amount-summary { display:flex; flex-direction:column; gap:10px; }
.amount-line { color:#475569; font-size:13px; }
.amount-gap { margin-left:14px; color:#2563eb; }
.amount-helper, .field-helper { color:#94a3b8; font-size:12px; line-height:1.6; margin-top:6px; }
.selected-patient-card { margin-top:10px; padding:10px 12px; border-radius:12px; background:#f8fafc; color:#334155; }
.selected-patient-meta { margin-top:4px; font-size:12px; color:#64748b; }
@media (max-width: 1200px) {
  .page-head { flex-direction:column; }
}
</style>
