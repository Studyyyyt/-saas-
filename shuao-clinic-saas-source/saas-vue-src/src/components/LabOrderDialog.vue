<template>
  <el-dialog
    :title="dialogTitle"
    :visible.sync="dialogVisible"
    width="760px"
    :close-on-click-modal="false"
  >
    <div class="lab-order-dialog">
      <div class="dialog-topbar">
        <el-tag size="small" :type="statusTagType(form.status)">{{ form.status || '已下单' }}</el-tag>
        <div class="topbar-actions">
          <el-button v-if="form.patient_id" size="mini" type="primary" plain @click="goPatient360">患者档案</el-button>
          <el-button v-if="form.patient_id && form.medical_record_id" size="mini" plain @click="goMedicalRecord">关联病历</el-button>
          <el-button v-if="form.patient_id && form.treatment_id" size="mini" plain @click="goTreatmentRecord">关联治疗</el-button>
        </div>
      </div>

      <el-form label-width="110px" class="order-form">
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12">
            <el-form-item label="加工厂">
              <el-select
                v-model="form.factory_id"
                :disabled="factoryFieldDisabled"
                filterable
                style="width:100%"
                placeholder="请选择加工厂"
                @change="handleFactoryChange"
              >
                <el-option v-for="item in factories" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="患者">
              <el-select
                v-model="selectedPatientId"
                :disabled="patientFieldDisabled"
                remote
                filterable
                clearable
                reserve-keyword
                style="width:100%"
                placeholder="输入姓名或手机号搜索患者"
                :remote-method="searchPatients"
                :loading="patientLoading"
                @change="handlePatientChange"
              >
                <el-option
                  v-for="item in patientOptions"
                  :key="item.id"
                  :label="formatPatientLabel(item)"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :xs="24" :sm="12">
            <el-form-item label="关联病历操作">
              <el-select
                v-model="form.medical_record_operation_id"
                :disabled="treatmentFieldDisabled || !form.patient_id"
                clearable
                filterable
                style="width:100%"
                placeholder="可选：待登记加工操作"
                @change="handleMedicalRecordOperationChange"
              >
                <el-option
                  v-for="item in visiblePendingOperationOptions"
                  :key="item.id"
                  :label="formatPendingOperationLabel(item)"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="关联治疗">
              <el-select
                v-model="form.treatment_id"
                :disabled="treatmentFieldDisabled || !form.patient_id"
                clearable
                filterable
                style="width:100%"
                placeholder="可选关联治疗记录"
              >
                <el-option
                  v-for="item in treatmentOptions"
                  :key="item.id"
                  :label="formatTreatmentLabel(item)"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="订单状态">
              <el-select
                v-model="form.status"
                :disabled="statusFieldDisabled"
                style="width:100%"
                placeholder="请选择状态"
              >
                <el-option v-for="item in availableStatusOptions" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :xs="24" :sm="12">
            <el-form-item label="产品">
              <el-select
                v-model="selectedProductId"
                :disabled="productFieldDisabled"
                clearable
                filterable
                style="width:100%"
                placeholder="选择价格表产品可自动带出单价"
                @change="handleProductChange"
              >
                <el-option
                  v-for="item in productOptions"
                  :key="item.id"
                  :label="formatProductLabel(item)"
                  :value="item.id"
                />
              </el-select>
              <div v-if="canManageFactoryProducts" class="field-helper">
                <el-button type="text" size="mini" @click="goFactoryProductLibrary">
                  {{ form.factory_id ? '维护当前加工厂产品库' : '先选择加工厂，再维护产品库' }}
                </el-button>
              </div>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="产品规格">
              <el-input v-model="form.product_spec" :disabled="productFieldDisabled"></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :xs="24" :sm="12">
            <el-form-item label="产品名称">
              <el-input v-model="form.product_name" :disabled="productFieldDisabled"></el-input>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="备注">
              <el-input v-model="form.remark" :disabled="remarkFieldDisabled"></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :xs="24" :sm="8">
            <el-form-item label="单价">
              <el-input-number
                v-model="form.unit_price"
                :disabled="priceFieldDisabled"
                :min="0"
                :precision="2"
                :step="1"
                controls-position="right"
                style="width:100%"
                @change="recalculateTotal"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="8">
            <el-form-item label="数量">
              <el-input-number
                v-model="form.quantity"
                :disabled="quantityFieldDisabled"
                :min="1"
                controls-position="right"
                style="width:100%"
                @change="recalculateTotal"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="8">
            <el-form-item label="总金额">
              <el-input :value="moneyText(totalAmount)" disabled></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :xs="24" :sm="8">
            <el-form-item label="下单日期">
              <el-date-picker
                v-model="form.order_date"
                :disabled="dateFieldDisabled"
                type="date"
                value-format="yyyy-MM-dd"
                style="width:100%"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="8">
            <el-form-item label="预计完成日期">
              <el-date-picker
                v-model="form.expected_delivery_date"
                :disabled="expectedDateFieldDisabled"
                type="date"
                value-format="yyyy-MM-dd"
                style="width:100%"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="8">
            <el-form-item label="实际收货日期">
              <el-date-picker
                v-model="form.actual_delivery_date"
                :disabled="actualDateFieldDisabled"
                type="date"
                value-format="yyyy-MM-dd"
                style="width:100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <div class="dialog-tip">
          <span>订单默认创建为“已下单”。</span>
          <span v-if="isLockedStatusOrder">当前订单已不处于“已下单”，仅允许修改状态和实际收货日期。</span>
        </div>
      </el-form>

      <div v-if="form.medical_record_operation_id" class="operation-summary-box">
        <div class="operation-summary-box__title">已关联病历操作</div>
        <div class="operation-summary-box__desc">
          {{ [form.project_name, form.operation_name, form.tooth_positions ? `牙位:${form.tooth_positions}` : ''].filter(Boolean).join('｜') || '已关联病历操作' }}
        </div>
      </div>
    </div>

    <span slot="footer">
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button v-if="!isDetail" type="primary" :loading="saving" @click="submit">保存</el-button>
    </span>
  </el-dialog>
</template>

<script>
import axios from 'axios'
import {
  LAB_MANUAL_ORDER_STATUS_OPTIONS,
  canManageLabFactory,
  canViewLabOnly,
  formatMoney,
  normalizeLabRole,
  orderStatusRank,
  orderStatusTagType
} from '@/utils/labConstants'

function defaultForm(currentUser) {
  const today = new Date()
  const y = today.getFullYear()
  const m = String(today.getMonth() + 1).padStart(2, '0')
  const d = String(today.getDate()).padStart(2, '0')
  return {
    id: null,
    factory_id: null,
    factory_name: '',
    patient_id: null,
    patient_name: '',
    treatment_id: null,
    medical_record_operation_id: null,
    medical_record_id: null,
    project_id: null,
    project_name: '',
    operation_id: null,
    operation_name: '',
    tooth_positions: '',
    product_name: '',
    product_spec: '',
    unit_price: 0,
    quantity: 1,
    total_amount: 0,
    order_date: `${y}-${m}-${d}`,
    expected_delivery_date: '',
    actual_delivery_date: '',
    status: '已下单',
    remark: '',
    created_by: currentUser && currentUser.id ? Number(currentUser.id) : null,
    created_by_name: currentUser && currentUser.name ? currentUser.name : ''
  }
}

export default {
  name: 'LabOrderDialog',
  props: {
    visible: { type: Boolean, default: false },
    mode: { type: String, default: 'create' },
    order: { type: Object, default: () => ({}) },
    factories: { type: Array, default: () => [] },
    currentUser: { type: Object, default: () => ({}) }
  },
  data() {
    return {
      form: defaultForm(this.currentUser),
      selectedPatientId: null,
      selectedProductId: null,
      patientOptions: [],
      patientLoading: false,
      productOptions: [],
      pendingOperationOptions: [],
      treatmentOptions: [],
      saving: false,
      preferredMedicalRecordId: null
    }
  },
  computed: {
    dialogVisible: {
      get() {
        return this.visible
      },
      set(value) {
        this.$emit('update:visible', value)
      }
    },
    roleCode() {
      return normalizeLabRole(this.currentUser && this.currentUser.role)
    },
    isDetail() {
      return this.mode === 'detail'
    },
    isLockedStatusOrder() {
      return !!this.form.id && this.form.status && this.form.status !== '已下单'
    },
    dialogTitle() {
      if (this.isDetail) return '加工订单详情'
      return this.form.id ? '编辑加工订单' : '新增加工订单'
    },
    totalAmount() {
      const unitPrice = Number(this.form.unit_price || 0)
      const quantity = Number(this.form.quantity || 0)
      if (!Number.isFinite(unitPrice) || !Number.isFinite(quantity)) return 0
      return Math.round(unitPrice * quantity * 100) / 100
    },
    availableStatusOptions() {
      const currentRank = orderStatusRank(this.form.status)
      return LAB_MANUAL_ORDER_STATUS_OPTIONS.filter(item => orderStatusRank(item) >= currentRank && item !== '已对账')
    },
    baseReadOnly() {
      return this.isDetail || canViewLabOnly(this.roleCode)
    },
    factoryFieldDisabled() {
      return this.baseReadOnly || this.isLockedStatusOrder
    },
    patientFieldDisabled() {
      return this.baseReadOnly || this.isLockedStatusOrder
    },
    treatmentFieldDisabled() {
      return this.baseReadOnly || this.isLockedStatusOrder
    },
    productFieldDisabled() {
      return this.baseReadOnly || this.isLockedStatusOrder
    },
    priceFieldDisabled() {
      return this.baseReadOnly || this.isLockedStatusOrder
    },
    quantityFieldDisabled() {
      return this.baseReadOnly || this.isLockedStatusOrder
    },
    dateFieldDisabled() {
      return this.baseReadOnly || this.isLockedStatusOrder
    },
    expectedDateFieldDisabled() {
      return this.baseReadOnly || this.isLockedStatusOrder
    },
    actualDateFieldDisabled() {
      return this.baseReadOnly
    },
    statusFieldDisabled() {
      return this.baseReadOnly
    },
    remarkFieldDisabled() {
      return this.baseReadOnly || this.isLockedStatusOrder
    },
    visiblePendingOperationOptions() {
      const preferredId = this.preferredMedicalRecordId ? Number(this.preferredMedicalRecordId) : null
      if (!preferredId) {
        return this.pendingOperationOptions
      }
      const filtered = (this.pendingOperationOptions || []).filter(item => Number(item && item.medical_record_id) === preferredId)
      return filtered.length ? filtered : this.pendingOperationOptions
    },
    canManageFactoryProducts() {
      return canManageLabFactory(this.roleCode)
    }
  },
  watch: {
    visible(value) {
      if (value) {
        this.syncFromProps()
      }
    },
    order: {
      deep: true,
      handler() {
        if (this.visible) {
          this.syncFromProps()
        }
      }
    }
  },
  methods: {
    moneyText(value) {
      return `¥${formatMoney(value)}`
    },
    statusTagType(status) {
      return orderStatusTagType(status)
    },
    syncFromProps() {
      const base = defaultForm(this.currentUser)
      const source = this.order || {}
      this.preferredMedicalRecordId = source.medical_record_id ? Number(source.medical_record_id) : null
      this.form = Object.assign(base, source, {
        unit_price: Number(source.unit_price || 0),
        quantity: Number(source.quantity || 1),
        order_date: this.normalizeDateText(source.order_date) || base.order_date,
        expected_delivery_date: this.normalizeDateText(source.expected_delivery_date),
        actual_delivery_date: this.normalizeDateText(source.actual_delivery_date),
        status: source.status || base.status
      })
      this.selectedPatientId = this.form.patient_id ? Number(this.form.patient_id) : null
      this.selectedProductId = null
      this.patientOptions = []
      if (this.form.patient_id) {
        this.patientOptions = [{
          id: Number(this.form.patient_id),
          name: this.form.patient_name,
          phone: ''
        }]
      }
      this.loadProducts(this.form.factory_id).then(() => {
        this.selectedProductId = this.matchProductId()
      })
      this.loadTreatments(this.form.patient_id)
      this.loadPendingOperations(this.form.patient_id)
      this.recalculateTotal()
    },
    normalizeDateText(value) {
      if (!value) return ''
      return String(value).slice(0, 10)
    },
    formatPatientLabel(item) {
      const phone = item && item.phone ? ` / ${item.phone}` : ''
      return `${item.name || '未命名患者'}${phone}`
    },
    formatProductLabel(item) {
      const spec = item && item.product_spec ? ` / ${item.product_spec}` : ''
      return `${item.product_name || '未命名产品'}${spec} / ¥${formatMoney(item.unit_price)}`
    },
    formatTreatmentLabel(item) {
      const date = this.normalizeDateText(item && item.treatment_date)
      const project = item && item.appointment_purpose ? item.appointment_purpose : '未命名项目'
      const status = item && item.status ? item.status : '-'
      return `${date || '未排期'} / ${project} / ${status}`
    },
    formatPendingOperationLabel(item) {
      const visitDate = this.normalizeDateText(item && item.visit_date)
      const parts = [
        visitDate || '未记录日期',
        item && item.factory_name ? `加工厂:${item.factory_name}` : '',
        item && item.project_name ? item.project_name : '',
        item && item.operation_name ? item.operation_name : '',
        item && item.tooth_positions ? `牙位:${item.tooth_positions}` : ''
      ].filter(Boolean)
      return parts.join(' / ')
    },
    matchProductId() {
      const matched = this.productOptions.find(item =>
        String(item.product_name || '').trim() === String(this.form.product_name || '').trim()
        && String(item.product_spec || '').trim() === String(this.form.product_spec || '').trim()
      )
      return matched ? matched.id : null
    },
    async loadProducts(factoryId) {
      this.productOptions = []
      if (!factoryId) return
      try {
        const res = await axios.get(`/lab-factories/${factoryId}/products`, { params: { enabledOnly: true } })
        this.productOptions = Array.isArray(res.data.data) ? res.data.data : []
      } catch (error) {
        this.productOptions = []
      }
    },
    async loadTreatments(patientId) {
      this.treatmentOptions = []
      if (!patientId) return
      try {
        const res = await axios.get('/treatments/recentByPatientId', { params: { patientId, limit: 20 } })
        this.treatmentOptions = Array.isArray(res.data.data) ? res.data.data : []
      } catch (error) {
        this.treatmentOptions = []
      }
    },
    async loadPendingOperations(patientId) {
      this.pendingOperationOptions = []
      if (!patientId) return
      try {
        const res = await axios.get('/medical-record-operations/pendingLabList', { params: { patientId, page: 1, size: 100 } })
        const data = res.data && res.data.data ? res.data.data : {}
        this.pendingOperationOptions = Array.isArray(data.list) ? data.list : []
        if (this.form.medical_record_operation_id && !this.pendingOperationOptions.some(item => Number(item.id) === Number(this.form.medical_record_operation_id))) {
          this.pendingOperationOptions.unshift({
            id: this.form.medical_record_operation_id,
            medical_record_id: this.form.medical_record_id,
            project_id: this.form.project_id,
            project_name: this.form.project_name,
            operation_id: this.form.operation_id,
            operation_name: this.form.operation_name,
            factory_id: this.form.factory_id,
            factory_name: this.form.factory_name,
            tooth_positions: this.form.tooth_positions,
            visit_date: this.form.order_date
          })
        }
        if (this.form.medical_record_operation_id) {
          this.handleMedicalRecordOperationChange(this.form.medical_record_operation_id)
        } else {
          this.autoSelectPreferredOperation()
        }
      } catch (error) {
        this.pendingOperationOptions = []
      }
    },
    autoSelectPreferredOperation() {
      if (!this.preferredMedicalRecordId || this.form.medical_record_operation_id) {
        return
      }
      const candidates = this.visiblePendingOperationOptions
      if (candidates.length === 1) {
        this.form.medical_record_operation_id = candidates[0].id
        this.handleMedicalRecordOperationChange(candidates[0].id)
      }
    },
    async searchPatients(query) {
      const keyword = String(query || '').trim()
      if (!keyword) {
        this.patientOptions = []
        return
      }
      this.patientLoading = true
      try {
        const res = await axios.get('/patients/search', { params: { keyword, page: 1, size: 20 } })
        const data = res.data && res.data.data ? res.data.data : {}
        this.patientOptions = Array.isArray(data.list) ? data.list : []
      } catch (error) {
        this.patientOptions = []
      } finally {
        this.patientLoading = false
      }
    },
    async handleFactoryChange(value) {
      if (!value) {
        this.selectedProductId = null
        this.form.factory_name = ''
        return
      }
      const factory = (this.factories || []).find(item => Number(item.id) === Number(value))
      this.form.factory_name = factory ? factory.name : ''
      this.selectedProductId = null
      this.form.product_name = ''
      this.form.product_spec = ''
      this.form.unit_price = 0
      this.recalculateTotal()
      await this.loadProducts(value)
    },
    async handlePatientChange(value) {
      const patient = (this.patientOptions || []).find(item => Number(item.id) === Number(value))
      this.form.patient_id = patient ? Number(patient.id) : null
      this.form.patient_name = patient ? patient.name : ''
      this.preferredMedicalRecordId = null
      this.form.medical_record_operation_id = null
      this.form.medical_record_id = null
      this.form.project_id = null
      this.form.project_name = ''
      this.form.operation_id = null
      this.form.operation_name = ''
      this.form.tooth_positions = ''
      this.form.treatment_id = null
      await this.loadTreatments(this.form.patient_id)
      await this.loadPendingOperations(this.form.patient_id)
    },
    async handleMedicalRecordOperationChange(value) {
      const selected = (this.pendingOperationOptions || []).find(item => Number(item.id) === Number(value))
      if (!selected) {
        this.form.medical_record_id = null
        this.form.project_id = null
        this.form.project_name = ''
        this.form.operation_id = null
        this.form.operation_name = ''
        this.form.tooth_positions = ''
        return
      }
      this.form.medical_record_id = selected.medical_record_id || null
      this.form.project_id = selected.project_id || null
      this.form.project_name = selected.project_name || ''
      this.form.operation_id = selected.operation_id || null
      this.form.operation_name = selected.operation_name || ''
      this.form.tooth_positions = selected.tooth_positions || ''
      const factoryId = selected.factory_id ? Number(selected.factory_id) : null
      if (factoryId) {
        const factory = (this.factories || []).find(item => Number(item.id) === factoryId)
        const changed = Number(this.form.factory_id || 0) !== factoryId
        this.form.factory_id = factoryId
        this.form.factory_name = (selected.factory_name || (factory && factory.name) || '').trim()
        if (changed) {
          this.selectedProductId = null
          this.form.product_name = ''
          this.form.product_spec = ''
          this.form.unit_price = 0
          this.recalculateTotal()
          await this.loadProducts(factoryId)
        }
      }
    },
    goFactoryProductLibrary() {
      if (!this.form.factory_id) {
        this.$message.warning('请先选择加工厂')
        return
      }
      const target = this.$router.resolve({ path: `/lab-factories/${this.form.factory_id}` })
      window.open(target.href, '_blank')
    },
    handleProductChange(value) {
      const product = (this.productOptions || []).find(item => Number(item.id) === Number(value))
      if (!product) return
      this.form.product_name = product.product_name || ''
      this.form.product_spec = product.product_spec || ''
      this.form.unit_price = Number(product.unit_price || 0)
      this.recalculateTotal()
    },
    recalculateTotal() {
      this.form.total_amount = this.totalAmount
    },
    validateForm() {
      if (!this.form.factory_id) return '请选择加工厂'
      if (!this.form.patient_id) return '请选择患者'
      if (!String(this.form.product_name || '').trim()) return '产品名称不能为空'
      if (!this.form.order_date) return '下单日期不能为空'
      if (!Number(this.form.quantity) || Number(this.form.quantity) <= 0) return '数量必须大于0'
      if (Number(this.form.unit_price) < 0) return '单价不能小于0'
      if (!this.form.status) return '订单状态不能为空'
      if (this.form.status === '已对账') return '已对账状态只能由系统自动设置'
      return ''
    },
    async submit() {
      const validation = this.validateForm()
      if (validation) {
        this.$message.warning(validation)
        return
      }
      const payload = Object.assign({}, this.form, {
        patient_id: this.form.patient_id ? Number(this.form.patient_id) : null,
        treatment_id: this.form.treatment_id ? Number(this.form.treatment_id) : null,
        medical_record_operation_id: this.form.medical_record_operation_id ? Number(this.form.medical_record_operation_id) : null,
        medical_record_id: this.form.medical_record_id ? Number(this.form.medical_record_id) : null,
        factory_id: this.form.factory_id ? Number(this.form.factory_id) : null,
        project_id: this.form.project_id ? Number(this.form.project_id) : null,
        operation_id: this.form.operation_id ? Number(this.form.operation_id) : null,
        unit_price: Number(this.form.unit_price || 0),
        quantity: Number(this.form.quantity || 0),
        total_amount: Number(this.totalAmount || 0),
        created_by: this.form.created_by ? Number(this.form.created_by) : null,
        created_by_name: this.form.created_by_name || ''
      })
      this.saving = true
      const request = payload.id ? axios.put('/lab-orders/edit', payload) : axios.post('/lab-orders/add', payload)
      try {
        const res = await request
        if (res.data.code === '200') {
          this.$message.success(payload.id ? '订单更新成功' : '订单创建成功')
          this.$emit('saved')
          this.dialogVisible = false
        } else {
          this.$message.error(res.data.msg || '保存失败')
        }
      } catch (error) {
        this.$message.error('保存失败')
      } finally {
        this.saving = false
      }
    },
    goPatient360() {
      if (!this.form.patient_id) return
      this.$router.push({ path: '/Patient360', query: { id: this.form.patient_id } }).catch(() => {})
    },
    goMedicalRecord() {
      if (!this.form.patient_id) return
      this.$router.push({ path: '/Patient360', query: { id: this.form.patient_id, tab: 'records' } }).catch(() => {})
    },
    goTreatmentRecord() {
      if (!this.form.patient_id || !this.form.treatment_id) return
      this.$router.push({
        path: '/Patient360',
        query: { id: this.form.patient_id, tab: 'billing', treatmentId: this.form.treatment_id }
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.lab-order-dialog {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.dialog-topbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 14px;
  background: #f8fafc;
}

.topbar-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.field-helper {
  margin-top: 4px;
  line-height: 1;
}

.dialog-tip {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  color: #64748b;
  font-size: 12px;
  line-height: 1.6;
}

.operation-summary-box {
  padding: 12px 14px;
  border-radius: 14px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
}

.operation-summary-box__title {
  color: #0f172a;
  font-size: 13px;
  font-weight: 700;
}

.operation-summary-box__desc {
  margin-top: 6px;
  color: #475569;
  font-size: 12px;
  line-height: 1.7;
}

@media (max-width: 768px) {
  .dialog-topbar {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
