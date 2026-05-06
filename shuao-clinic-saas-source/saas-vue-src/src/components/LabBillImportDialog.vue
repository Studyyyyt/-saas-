<template>
  <el-dialog
    title="导入月度账单"
    :visible.sync="dialogVisible"
    width="880px"
    :close-on-click-modal="false"
  >
    <div class="bill-import-dialog">
      <el-form label-width="110px">
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12">
            <el-form-item label="加工厂">
              <el-select v-model="form.factoryId" filterable style="width:100%" placeholder="请选择加工厂" @change="handleFactoryChange">
                <el-option v-for="item in factories" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="账单月份">
              <el-date-picker
                v-model="form.billMonth"
                type="month"
                value-format="yyyy-MM"
                style="width:100%"
                placeholder="请选择账单月份"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12">
            <el-form-item label="模板">
              <el-select v-model="form.templateId" style="width:100%" placeholder="请选择模板" @change="handleTemplateChange">
                <el-option v-for="item in templates" :key="item.id" :label="item.template_name" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="原始文件">
              <el-upload
                action=""
                :before-upload="handleBeforeUpload"
                :show-file-list="false"
                accept=".xlsx,.xls"
              >
                <el-button type="primary" plain>选择 Excel</el-button>
              </el-upload>
              <div class="file-name">{{ fileName || '尚未选择文件' }}</div>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <div class="import-summary">
        <div class="summary-item">
          <span class="summary-label">解析条数</span>
          <strong class="summary-value">{{ parsedItems.length }}</strong>
        </div>
        <div class="summary-item">
          <span class="summary-label">账单金额</span>
          <strong class="summary-value money">¥{{ moneyText(totalAmount) }}</strong>
        </div>
      </div>

      <el-alert
        v-if="parseError"
        :title="parseError"
        type="error"
        :closable="false"
        show-icon
      />

      <div class="preview-table">
        <div class="preview-head">
          <div class="panel-title">解析预览</div>
          <el-button size="mini" @click="parseSelectedFile">重新解析</el-button>
        </div>
        <el-table :data="previewRows" size="mini" border max-height="320">
          <el-table-column prop="raw_row_number" label="行号" width="80" />
          <el-table-column prop="patient_name" label="患者" min-width="120" />
          <el-table-column prop="product_name" label="产品" min-width="140" />
          <el-table-column prop="product_spec" label="规格" min-width="120" />
          <el-table-column prop="quantity" label="数量" width="90" />
          <el-table-column prop="unit_price" label="单价" width="90">
            <template slot-scope="scope">¥{{ moneyText(scope.row.unit_price) }}</template>
          </el-table-column>
          <el-table-column prop="total_amount" label="金额" width="100">
            <template slot-scope="scope">¥{{ moneyText(scope.row.total_amount) }}</template>
          </el-table-column>
          <el-table-column prop="delivery_date" label="送货日期" min-width="120" />
        </el-table>
        <div v-if="!previewRows.length" class="preview-empty">选择模板并上传 Excel 后可在这里预览解析结果</div>
      </div>
    </div>

    <span slot="footer">
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="uploading" @click="submitImport">开始导入</el-button>
    </span>
  </el-dialog>
</template>

<script>
import axios from 'axios'
import * as XLSX from 'xlsx'
import { formatMoney, LAB_TEMPLATE_FIELDS } from '@/utils/labConstants'

function buildDefaultForm() {
  const today = new Date()
  const y = today.getFullYear()
  const m = String(today.getMonth() + 1).padStart(2, '0')
  return {
    factoryId: '',
    billMonth: `${y}-${m}`,
    templateId: ''
  }
}

export default {
  name: 'LabBillImportDialog',
  props: {
    visible: { type: Boolean, default: false },
    factories: { type: Array, default: () => [] },
    currentUser: { type: Object, default: () => ({}) },
    defaultFactoryId: { type: [Number, String], default: '' }
  },
  data() {
    return {
      form: buildDefaultForm(),
      templates: [],
      fileObject: null,
      fileName: '',
      parsedItems: [],
      previewRows: [],
      parseError: '',
      uploading: false
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
    totalAmount() {
      return this.parsedItems.reduce((sum, item) => sum + Number(item.total_amount || 0), 0)
    }
  },
  watch: {
    visible(value) {
      if (value) {
        this.resetState()
      }
    }
  },
  methods: {
    moneyText(value) {
      return formatMoney(value)
    },
    resetState() {
      this.form = Object.assign(buildDefaultForm(), {
        factoryId: this.defaultFactoryId || ''
      })
      this.templates = []
      this.fileObject = null
      this.fileName = ''
      this.parsedItems = []
      this.previewRows = []
      this.parseError = ''
      if (this.form.factoryId) {
        this.handleFactoryChange(this.form.factoryId)
      }
    },
    async handleFactoryChange(value) {
      this.form.templateId = ''
      this.templates = []
      this.parsedItems = []
      this.previewRows = []
      this.parseError = ''
      if (!value) return
      try {
        const res = await axios.get(`/lab-factories/${value}/templates`)
        this.templates = Array.isArray(res.data.data) ? res.data.data : []
        if (this.templates.length) {
          this.form.templateId = this.templates[0].id
        }
      } catch (error) {
        this.templates = []
      }
    },
    handleTemplateChange() {
      if (this.fileObject) {
        this.parseSelectedFile()
      }
    },
    handleBeforeUpload(file) {
      this.fileObject = file
      this.fileName = file && file.name ? file.name : ''
      this.parseSelectedFile()
      return false
    },
    parseTemplateMapping() {
      const template = (this.templates || []).find(item => Number(item.id) === Number(this.form.templateId))
      if (!template || !template.column_mapping) return null
      try {
        return typeof template.column_mapping === 'string'
          ? JSON.parse(template.column_mapping)
          : Object.assign({}, template.column_mapping)
      } catch (error) {
        return null
      }
    },
    parseSelectedFile() {
      this.parseError = ''
      this.parsedItems = []
      this.previewRows = []
      if (!this.fileObject) {
        this.parseError = '请先选择账单文件'
        return
      }
      const template = (this.templates || []).find(item => Number(item.id) === Number(this.form.templateId))
      if (!template) {
        this.parseError = '请先选择账单模板'
        return
      }
      const mapping = this.parseTemplateMapping()
      if (!mapping) {
        this.parseError = '模板映射无效，请重新配置'
        return
      }
      const reader = new FileReader()
      reader.onload = event => {
        try {
          const workbook = XLSX.read(event.target.result, { type: 'binary' })
          const firstSheet = workbook.Sheets[workbook.SheetNames[0]]
          const rows = XLSX.utils.sheet_to_json(firstSheet, { header: 1, defval: '', raw: false })
          const headerRowIndex = Math.max(Number(template.header_row || 1) - 1, 0)
          const startRowIndex = Math.max(Number(template.data_start_row || 2) - 1, 0)
          const headers = (rows[headerRowIndex] || []).map(item => String(item || '').trim())
          const fieldIndexMap = {}
          headers.forEach((header, index) => {
            const fieldKey = mapping[header]
            if (fieldKey) {
              fieldIndexMap[fieldKey] = index
            }
          })
          const missingRequiredField = LAB_TEMPLATE_FIELDS.find(field => field.required && fieldIndexMap[field.key] === undefined)
          if (missingRequiredField) {
            this.parseError = `模板缺少必填映射：${missingRequiredField.label}`
            return
          }
          const parsed = rows
            .slice(startRowIndex)
            .map((row, index) => this.buildItemFromRow(row, fieldIndexMap, startRowIndex + index + 1))
            .filter(item => item && (item.product_name || item.patient_name || item.total_amount))
          this.parsedItems = parsed
          this.previewRows = parsed.slice(0, 30)
        } catch (error) {
          this.parseError = 'Excel 解析失败，请检查模板和文件格式'
        }
      }
      reader.readAsBinaryString(this.fileObject)
    },
    buildItemFromRow(row, fieldIndexMap, rowNumber) {
      const getCell = fieldKey => {
        const index = fieldIndexMap[fieldKey]
        return index === undefined ? '' : row[index]
      }
      const quantity = this.toInteger(getCell('quantity'))
      const unitPrice = this.toMoney(getCell('unit_price'))
      const totalAmountRaw = this.toMoney(getCell('total_amount'))
      const totalAmount = totalAmountRaw > 0 ? totalAmountRaw : Math.round(unitPrice * quantity * 100) / 100
      return {
        raw_row_number: rowNumber,
        product_name: String(getCell('product_name') || '').trim(),
        product_spec: String(getCell('product_spec') || '').trim(),
        quantity,
        unit_price: unitPrice,
        total_amount: totalAmount,
        delivery_date: this.normalizeDateText(getCell('delivery_date')),
        patient_name: String(getCell('patient_name') || '').trim()
      }
    },
    toInteger(value) {
      const number = Number(String(value || '').replace(/,/g, '').trim())
      if (!Number.isFinite(number)) return 0
      return Math.round(number)
    },
    toMoney(value) {
      const normalized = String(value || '').replace(/[￥¥,\s]/g, '').trim()
      const number = Number(normalized)
      if (!Number.isFinite(number)) return 0
      return Math.round(number * 100) / 100
    },
    normalizeDateText(value) {
      const text = String(value || '').trim()
      if (!text) return ''
      if (/^\d{4}-\d{2}-\d{2}$/.test(text)) return text
      if (/^\d{4}\/\d{1,2}\/\d{1,2}$/.test(text)) {
        const date = new Date(text)
        return Number.isNaN(date.getTime()) ? text : this.formatDate(date)
      }
      return text
    },
    formatDate(date) {
      const d = new Date(date)
      if (Number.isNaN(d.getTime())) return ''
      const y = d.getFullYear()
      const m = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      return `${y}-${m}-${day}`
    },
    async submitImport() {
      if (!this.form.factoryId) {
        this.$message.warning('请选择加工厂')
        return
      }
      if (!this.form.billMonth) {
        this.$message.warning('请选择账单月份')
        return
      }
      if (!this.form.templateId) {
        this.$message.warning('请选择账单模板')
        return
      }
      if (!this.fileObject) {
        this.$message.warning('请上传账单文件')
        return
      }
      if (!this.parsedItems.length) {
        this.$message.warning(this.parseError || '没有可导入的账单数据')
        return
      }
      const formData = new FormData()
      formData.append('file', this.fileObject)
      formData.append('factoryId', this.form.factoryId)
      formData.append('billMonth', this.form.billMonth)
      formData.append('templateId', this.form.templateId)
      formData.append('importedBy', this.currentUser && this.currentUser.id ? this.currentUser.id : '')
      formData.append('importedByName', this.currentUser && this.currentUser.name ? this.currentUser.name : '')
      formData.append('parsedItemsJson', JSON.stringify(this.parsedItems))

      this.uploading = true
      try {
        const res = await axios.post('/lab-bills/import', formData, {
          headers: { 'Content-Type': 'multipart/form-data' }
        })
        if (res.data.code === '200') {
          this.$message.success('账单导入成功')
          this.$emit('saved')
          this.dialogVisible = false
        } else {
          this.$message.error(res.data.msg || '导入失败')
        }
      } catch (error) {
        this.$message.error('导入失败')
      } finally {
        this.uploading = false
      }
    }
  }
}
</script>

<style scoped>
.bill-import-dialog {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.file-name {
  margin-top: 8px;
  color: #64748b;
  font-size: 12px;
}

.import-summary {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.summary-item {
  padding: 14px 16px;
  border-radius: 16px;
  background: #f8fafc;
}

.summary-label {
  display: block;
  color: #64748b;
  font-size: 12px;
}

.summary-value {
  display: block;
  margin-top: 8px;
  color: #0f172a;
  font-size: 22px;
  font-weight: 700;
}

.summary-value.money {
  color: #0f766e;
}

.preview-table {
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  padding: 16px;
  background: #fff;
}

.preview-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.panel-title {
  color: #0f172a;
  font-size: 15px;
  font-weight: 700;
}

.preview-empty {
  padding-top: 16px;
  color: #94a3b8;
  text-align: center;
  font-size: 13px;
}

@media (max-width: 768px) {
  .import-summary {
    grid-template-columns: 1fr;
  }
}
</style>
