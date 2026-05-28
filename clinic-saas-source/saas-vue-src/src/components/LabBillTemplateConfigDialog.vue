<template>
  <el-dialog
    :title="form.id ? '编辑账单模板' : '新增账单模板'"
    :visible.sync="dialogVisible"
    width="920px"
    :close-on-click-modal="false"
    append-to-body
  >
    <div class="template-dialog">
      <el-form label-width="110px" class="template-form">
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12">
            <el-form-item label="模板名称">
              <el-input v-model="form.template_name" placeholder="如：A工厂 2026版"></el-input>
            </el-form-item>
          </el-col>
          <el-col :xs="12" :sm="6">
            <el-form-item label="表头行">
              <el-input-number v-model="form.header_row" :min="1" controls-position="right" style="width:100%" @change="refreshSheetMeta" />
            </el-form-item>
          </el-col>
          <el-col :xs="12" :sm="6">
            <el-form-item label="起始行">
              <el-input-number v-model="form.data_start_row" :min="1" controls-position="right" style="width:100%" @change="refreshSheetMeta" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <div class="sample-upload">
        <el-upload
          action=""
          :before-upload="handleBeforeUpload"
          :show-file-list="false"
          accept=".xlsx,.xls"
        >
          <el-button type="primary" plain>上传示例 Excel</el-button>
        </el-upload>
        <span class="upload-tip">上传后可拖拽表头字段到右侧系统字段槽位</span>
      </div>

      <div class="mapping-layout">
        <div class="mapping-panel">
          <div class="panel-title">Excel 表头</div>
          <div class="header-list">
            <div
              v-for="header in displayHeaders"
              :key="header"
              class="header-chip"
              draggable="true"
              @dragstart="handleDragStart(header)"
            >
              {{ header }}
            </div>
          </div>
        </div>

        <div class="mapping-panel">
          <div class="panel-title">系统字段映射</div>
          <div class="field-list">
            <div
              v-for="field in LAB_TEMPLATE_FIELDS"
              :key="field.key"
              class="field-card"
              @dragover.prevent
              @drop="handleDrop(field.key)"
            >
              <div class="field-card__head">
                <span>{{ field.label }}</span>
                <el-tag size="mini" :type="field.required ? 'danger' : 'info'">{{ field.required ? '必填' : '可选' }}</el-tag>
              </div>
              <div class="field-card__body" :class="{ mapped: !!mappedHeader(field.key) }">
                <span v-if="mappedHeader(field.key)">{{ mappedHeader(field.key) }}</span>
                <span v-else>拖拽 Excel 列到这里</span>
              </div>
              <div class="field-card__foot">
                <el-button size="mini" type="text" @click="clearField(field.key)">清空</el-button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="preview-panel">
        <div class="panel-title">解析预览</div>
        <el-table :data="previewRows" size="mini" border max-height="260">
          <el-table-column prop="_rowIndex" label="原始行号" width="90" />
          <el-table-column v-for="field in LAB_TEMPLATE_FIELDS" :key="field.key" :prop="field.key" :label="field.label" min-width="120" />
        </el-table>
        <div v-if="!previewRows.length" class="preview-empty">上传示例文件后可预览映射结果</div>
      </div>
    </div>

    <span slot="footer">
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="saveTemplate">保存模板</el-button>
    </span>
  </el-dialog>
</template>

<script>
import * as XLSX from 'xlsx'
import { LAB_TEMPLATE_FIELDS } from '@/utils/labConstants'
import axios from 'axios'

function defaultForm() {
  return {
    id: null,
    template_name: '',
    header_row: 1,
    data_start_row: 2,
    column_mapping: {}
  }
}

export default {
  name: 'LabBillTemplateConfigDialog',
  props: {
    visible: { type: Boolean, default: false },
    factoryId: { type: [Number, String], default: '' },
    template: { type: Object, default: () => ({}) }
  },
  data() {
    return {
      LAB_TEMPLATE_FIELDS,
      form: defaultForm(),
      sheetRows: [],
      draggedHeader: '',
      previewRows: [],
      saving: false
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
    displayHeaders() {
      const headersFromSheet = this.currentHeaders
      if (headersFromSheet.length) return headersFromSheet
      return Object.keys(this.form.column_mapping || {})
    },
    currentHeaders() {
      if (!Array.isArray(this.sheetRows) || !this.sheetRows.length) return []
      const rowIndex = Math.max(Number(this.form.header_row || 1) - 1, 0)
      const row = this.sheetRows[rowIndex] || []
      return row
        .map(item => String(item || '').trim())
        .filter(Boolean)
    }
  },
  watch: {
    visible(value) {
      if (value) {
        this.syncFromProps()
      }
    },
    template: {
      deep: true,
      handler() {
        if (this.visible) {
          this.syncFromProps()
        }
      }
    }
  },
  methods: {
    syncFromProps() {
      const source = this.template || {}
      let mapping = {}
      try {
        if (source.column_mapping) {
          mapping = typeof source.column_mapping === 'string'
            ? JSON.parse(source.column_mapping)
            : Object.assign({}, source.column_mapping)
        }
      } catch (error) {
        mapping = {}
      }
      this.form = Object.assign(defaultForm(), source, {
        header_row: Number(source.header_row || 1),
        data_start_row: Number(source.data_start_row || 2),
        column_mapping: mapping
      })
      this.previewRows = []
      this.draggedHeader = ''
      this.refreshSheetMeta()
    },
    handleBeforeUpload(file) {
      const reader = new FileReader()
      reader.onload = event => {
        const workbook = XLSX.read(event.target.result, { type: 'binary' })
        const firstSheet = workbook.Sheets[workbook.SheetNames[0]]
        this.sheetRows = XLSX.utils.sheet_to_json(firstSheet, { header: 1, defval: '', raw: false })
        this.refreshSheetMeta()
      }
      reader.readAsBinaryString(file)
      return false
    },
    handleDragStart(header) {
      this.draggedHeader = header
    },
    handleDrop(fieldKey) {
      if (!this.draggedHeader) return
      const next = Object.assign({}, this.form.column_mapping || {})
      Object.keys(next).forEach(header => {
        if (header === this.draggedHeader || next[header] === fieldKey) {
          delete next[header]
        }
      })
      next[this.draggedHeader] = fieldKey
      this.form.column_mapping = next
      this.draggedHeader = ''
      this.refreshSheetMeta()
    },
    mappedHeader(fieldKey) {
      const mapping = this.form.column_mapping || {}
      return Object.keys(mapping).find(header => mapping[header] === fieldKey) || ''
    },
    clearField(fieldKey) {
      const next = Object.assign({}, this.form.column_mapping || {})
      Object.keys(next).forEach(header => {
        if (next[header] === fieldKey) {
          delete next[header]
        }
      })
      this.form.column_mapping = next
      this.refreshSheetMeta()
    },
    refreshSheetMeta() {
      if (!Array.isArray(this.sheetRows) || !this.sheetRows.length) {
        this.previewRows = []
        return
      }
      const mapping = this.form.column_mapping || {}
      const headerRowIndex = Math.max(Number(this.form.header_row || 1) - 1, 0)
      const startRowIndex = Math.max(Number(this.form.data_start_row || 2) - 1, 0)
      const headers = (this.sheetRows[headerRowIndex] || []).map(item => String(item || '').trim())
      const fieldIndexMap = {}
      headers.forEach((header, index) => {
        const fieldKey = mapping[header]
        if (fieldKey) {
          fieldIndexMap[fieldKey] = index
        }
      })
      this.previewRows = this.sheetRows
        .slice(startRowIndex, startRowIndex + 8)
        .filter(row => Array.isArray(row) && row.some(cell => String(cell || '').trim()))
        .map((row, index) => {
          const current = { _rowIndex: startRowIndex + index + 1 }
          LAB_TEMPLATE_FIELDS.forEach(field => {
            const cellIndex = fieldIndexMap[field.key]
            current[field.key] = cellIndex === undefined ? '' : row[cellIndex]
          })
          return current
        })
    },
    validateTemplate() {
      if (!this.factoryId) return '缺少加工厂信息'
      if (!String(this.form.template_name || '').trim()) return '模板名称不能为空'
      const requiredFields = LAB_TEMPLATE_FIELDS.filter(item => item.required)
      for (const field of requiredFields) {
        if (!this.mappedHeader(field.key)) {
          return `${field.label} 未配置映射`
        }
      }
      return ''
    },
    async saveTemplate() {
      const validation = this.validateTemplate()
      if (validation) {
        this.$message.warning(validation)
        return
      }
      const payload = {
        id: this.form.id || null,
        template_name: String(this.form.template_name || '').trim(),
        header_row: Number(this.form.header_row || 1),
        data_start_row: Number(this.form.data_start_row || 2),
        column_mapping: JSON.stringify(this.form.column_mapping || {})
      }
      this.saving = true
      const request = payload.id
        ? axios.put(`/lab-factories/${this.factoryId}/templates/edit`, payload)
        : axios.post(`/lab-factories/${this.factoryId}/templates/add`, payload)
      try {
        const res = await request
        if (res.data.code === '200') {
          this.$message.success(payload.id ? '模板更新成功' : '模板创建成功')
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
    }
  }
}
</script>

<style scoped>
.template-dialog {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.sample-upload {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.upload-tip {
  color: #64748b;
  font-size: 12px;
}

.mapping-layout {
  display: grid;
  grid-template-columns: 1fr 1.2fr;
  gap: 16px;
}

.mapping-panel,
.preview-panel {
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  background: #fff;
  padding: 16px;
}

.panel-title {
  margin-bottom: 12px;
  color: #0f172a;
  font-size: 15px;
  font-weight: 700;
}

.header-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.header-chip {
  padding: 8px 12px;
  border-radius: 999px;
  background: #eff6ff;
  color: #4A7F6B;
  cursor: grab;
  user-select: none;
  font-size: 13px;
}

.field-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 12px;
}

.field-card {
  border: 1px dashed #cbd5e1;
  border-radius: 14px;
  padding: 12px;
  background: #f8fafc;
}

.field-card__head,
.field-card__foot {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.field-card__body {
  min-height: 56px;
  margin-top: 10px;
  padding: 10px 12px;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  background: #fff;
  color: #94a3b8;
  display: flex;
  align-items: center;
}

.field-card__body.mapped {
  color: #0f172a;
  border-color: #93c5fd;
  background: #eff6ff;
}

.preview-empty {
  padding: 18px 0 4px;
  text-align: center;
  color: #94a3b8;
  font-size: 13px;
}

@media (max-width: 900px) {
  .mapping-layout {
    grid-template-columns: 1fr;
  }
}
</style>
