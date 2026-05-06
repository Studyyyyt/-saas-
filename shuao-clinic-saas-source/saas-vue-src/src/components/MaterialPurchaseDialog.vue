<template>
  <el-dialog
    :title="dialogTitle"
    :visible.sync="dialogVisible"
    :width="dialogWidth"
    :close-on-click-modal="false"
  >
    <div class="purchase-dialog">
      <el-form label-width="100px">
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12">
            <el-form-item label="供应商">
              <el-input v-model="form.supplier_name" :disabled="readonly || invoiceLocked" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="采购日期">
              <el-date-picker
                v-model="form.purchase_date"
                :disabled="readonly || invoiceLocked"
                type="date"
                value-format="yyyy-MM-dd"
                style="width:100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :xs="24" :sm="12">
            <el-form-item label="付款方式">
              <el-select v-model="form.payment_method" :disabled="readonly || invoiceLocked" style="width:100%">
                <el-option v-for="item in MATERIAL_PAYMENT_METHOD_OPTIONS" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="发票图片">
              <div class="invoice-line">
                <el-upload
                  v-if="!readonly && !invoiceLocked"
                  action=""
                  :show-file-list="false"
                  :before-upload="handleInvoiceUpload"
                  accept="image/*,.pdf"
                >
                  <el-button type="primary" plain>上传发票</el-button>
                </el-upload>
                <el-button v-if="form.invoice_image_url" type="text" @click="openInvoicePreview">查看附件</el-button>
                <span v-if="!form.invoice_image_url" class="invoice-tip">可选，支持图片/PDF</span>
              </div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :xs="24" :sm="12">
            <el-form-item label="分类筛选">
              <CategoryTreeSelect
                v-model="materialFilter.categoryId"
                :options="categories"
                :disabled="readonly || invoiceLocked"
                placeholder="可选分类筛选"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="备注">
              <el-input v-model="form.remark" :disabled="readonly || invoiceLocked" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <div class="items-head">
        <div>
          <div class="items-title">采购明细</div>
          <div class="items-subtitle">选择耗材后自动带出名称和规格，录入单价与数量即可。</div>
        </div>
        <el-button v-if="!readonly && !invoiceLocked" type="primary" plain size="mini" @click="addItem">新增明细</el-button>
      </div>

      <el-table :data="form.items" size="small" border class="items-table">
        <el-table-column label="耗材" min-width="220">
          <template slot-scope="scope">
            <el-select
              v-model="scope.row.material_id"
              :disabled="readonly || invoiceLocked"
              remote
              filterable
              clearable
              reserve-keyword
              style="width:100%"
              placeholder="搜索耗材"
              :remote-method="query => searchMaterials(query, scope.$index)"
              :loading="materialLoadingIndex === scope.$index"
              @change="value => handleMaterialChange(value, scope.$index)"
            >
              <el-option
                v-for="item in materialOptionsMap[scope.$index] || []"
                :key="item.id"
                :label="formatMaterialLabel(item)"
                :value="item.id"
              />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column prop="material_spec" label="规格" min-width="120">
          <template slot-scope="scope">{{ scope.row.material_spec || '-' }}</template>
        </el-table-column>
        <el-table-column label="单价" width="120">
          <template slot-scope="scope">
            <el-input-number
              v-model="scope.row.unit_price"
              :disabled="readonly || invoiceLocked"
              :min="0"
              :precision="2"
              controls-position="right"
              style="width:100%"
              @change="recalculateRow(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="数量" width="110">
          <template slot-scope="scope">
            <el-input-number
              v-model="scope.row.quantity"
              :disabled="readonly || invoiceLocked"
              :min="1"
              controls-position="right"
              style="width:100%"
              @change="recalculateRow(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="小计" width="120">
          <template slot-scope="scope">¥{{ formatMaterialMoney(scope.row.subtotal) }}</template>
        </el-table-column>
        <el-table-column v-if="!readonly && !invoiceLocked" label="操作" width="80" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" size="mini" style="color:#ef4444" @click="removeItem(scope.$index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="total-bar">
        <span>总金额</span>
        <strong>¥{{ formatMaterialMoney(totalAmount) }}</strong>
      </div>
    </div>

    <span slot="footer">
      <el-button @click="dialogVisible = false">关闭</el-button>
      <el-button v-if="!readonly" type="primary" :loading="saving" @click="submit">保存采购单</el-button>
    </span>
  </el-dialog>
</template>

<script>
import axios from 'axios'
import CategoryTreeSelect from '@/components/CategoryTreeSelect.vue'
import { MATERIAL_PAYMENT_METHOD_OPTIONS, formatMaterialMoney } from '@/utils/materialConstants'

function emptyItem() {
  return {
    material_id: '',
    material_name: '',
    material_spec: '',
    unit_price: 0,
    quantity: 1,
    subtotal: 0
  }
}

function defaultForm(currentUser) {
  const today = new Date()
  const y = today.getFullYear()
  const m = String(today.getMonth() + 1).padStart(2, '0')
  const d = String(today.getDate()).padStart(2, '0')
  return {
    id: null,
    supplier_name: '',
    purchase_date: `${y}-${m}-${d}`,
    total_amount: 0,
    payment_method: '现金',
    invoice_image_url: '',
    remark: '',
    created_by: currentUser && currentUser.id ? Number(currentUser.id) : null,
    created_by_name: currentUser && currentUser.name ? currentUser.name : '',
    status: '有效',
    items: [emptyItem()]
  }
}

export default {
  name: 'MaterialPurchaseDialog',
  components: { CategoryTreeSelect },
  props: {
    visible: { type: Boolean, default: false },
    purchase: { type: Object, default: () => ({}) },
    categories: { type: Array, default: () => [] },
    currentUser: { type: Object, default: () => ({}) },
    readonly: { type: Boolean, default: false },
    invoiceLocked: { type: Boolean, default: false },
    dialogWidth: { type: String, default: '980px' }
  },
  data() {
    return {
      MATERIAL_PAYMENT_METHOD_OPTIONS,
      form: defaultForm(this.currentUser),
      materialFilter: {
        categoryId: ''
      },
      materialOptionsMap: {},
      materialLoadingIndex: -1,
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
    dialogTitle() {
      if (this.readonly) return '采购单详情'
      return this.form.id ? '编辑采购单' : '新建采购单'
    },
    totalAmount() {
      return (this.form.items || []).reduce((sum, item) => sum + Number(item.subtotal || 0), 0)
    }
  },
  watch: {
    visible(value) {
      if (value) {
        this.syncFromProps()
      }
    },
    purchase: {
      deep: true,
      handler() {
        if (this.visible) {
          this.syncFromProps()
        }
      }
    }
  },
  methods: {
    formatMaterialMoney,
    syncFromProps() {
      const source = this.purchase || {}
      const base = defaultForm(this.currentUser)
      const items = Array.isArray(source.items) && source.items.length
        ? source.items.map(item => Object.assign(emptyItem(), item, {
          material_id: item.material_id || '',
          unit_price: Number(item.unit_price || 0),
          quantity: Number(item.quantity || 1),
          subtotal: Number(item.subtotal || 0)
        }))
        : [emptyItem()]
      this.form = Object.assign(base, source, {
        purchase_date: source.purchase_date ? String(source.purchase_date).slice(0, 10) : base.purchase_date,
        items
      })
      this.materialFilter.categoryId = ''
      this.materialOptionsMap = {}
      this.form.items.forEach((item, index) => {
        this.materialOptionsMap[index] = item.material_id
          ? [{
            id: item.material_id,
            name: item.material_name,
            spec: item.material_spec
          }]
          : []
      })
      this.recalculateAll()
    },
    formatMaterialLabel(item) {
      const spec = item && item.spec ? ` / ${item.spec}` : ''
      const stock = item && item.current_stock != null ? ` / 库存 ${item.current_stock}` : ''
      return `${item.name || '未命名耗材'}${spec}${stock}`
    },
    async searchMaterials(query, index) {
      const keyword = String(query || '').trim()
      this.materialLoadingIndex = index
      try {
        const res = await axios.get('/materials/searchLite', {
          params: {
            keyword: keyword || undefined,
            categoryId: this.materialFilter.categoryId || undefined,
            limit: 30
          }
        })
        this.$set(this.materialOptionsMap, index, Array.isArray(res.data.data) ? res.data.data : [])
      } catch (error) {
        this.$set(this.materialOptionsMap, index, [])
      } finally {
        this.materialLoadingIndex = -1
      }
    },
    async handleMaterialChange(value, index) {
      if (!value) {
        this.form.items[index] = emptyItem()
        return
      }
      let material = (this.materialOptionsMap[index] || []).find(item => Number(item.id) === Number(value))
      if (!material) {
        const res = await axios.get(`/materials/${value}`)
        material = res.data && res.data.data ? res.data.data : null
      }
      if (!material) return
      const row = this.form.items[index]
      row.material_id = Number(material.id)
      row.material_name = material.name || ''
      row.material_spec = material.spec || ''
      if (!Number(row.unit_price)) {
        row.unit_price = 0
      }
      if (!Number(row.quantity)) {
        row.quantity = 1
      }
      this.recalculateRow(row)
    },
    recalculateRow(row) {
      const price = Number(row.unit_price || 0)
      const quantity = Number(row.quantity || 0)
      row.subtotal = Math.round(price * quantity * 100) / 100
      this.recalculateAll()
    },
    recalculateAll() {
      this.form.total_amount = Math.round(this.totalAmount * 100) / 100
    },
    addItem() {
      this.form.items.push(emptyItem())
    },
    removeItem(index) {
      this.form.items.splice(index, 1)
      if (!this.form.items.length) {
        this.form.items.push(emptyItem())
      }
      this.recalculateAll()
    },
    async handleInvoiceUpload(file) {
      const formData = new FormData()
      formData.append('file', file)
      try {
        const res = await axios.post('/material-purchases/uploadInvoice', formData, {
          headers: { 'Content-Type': 'multipart/form-data' }
        })
        if (res.data.code === '200') {
          this.form.invoice_image_url = res.data.data || ''
          this.$message.success('发票上传成功')
        } else {
          this.$message.error(res.data.msg || '发票上传失败')
        }
      } catch (error) {
        this.$message.error('发票上传失败')
      }
      return false
    },
    openInvoicePreview() {
      if (!this.form.id) {
        this.$message.info('采购单保存后可预览发票')
        return
      }
      window.open(`/material-purchases/invoice/${this.form.id}`)
    },
    validateForm() {
      if (!this.form.purchase_date) return '采购日期不能为空'
      if (!this.form.payment_method) return '付款方式不能为空'
      const validItems = (this.form.items || []).filter(item => item.material_id)
      if (!validItems.length) return '请至少添加一条采购明细'
      for (const item of validItems) {
        if (!item.material_id) return '请选择耗材'
        if (Number(item.quantity) <= 0) return '采购数量必须大于0'
        if (Number(item.unit_price) < 0) return '采购单价不能小于0'
      }
      return ''
    },
    async submit() {
      const validation = this.validateForm()
      if (validation) {
        this.$message.warning(validation)
        return
      }
      const payload = Object.assign({}, this.form, {
        items: (this.form.items || [])
          .filter(item => item.material_id)
          .map(item => ({
            id: item.id || null,
            material_id: Number(item.material_id),
            material_name: item.material_name || '',
            material_spec: item.material_spec || '',
            unit_price: Number(item.unit_price || 0),
            quantity: Number(item.quantity || 0),
            subtotal: Number(item.subtotal || 0)
          })),
        created_by: this.form.created_by || null,
        created_by_name: this.form.created_by_name || ''
      })
      this.saving = true
      const request = payload.id ? axios.put('/material-purchases/edit', payload) : axios.post('/material-purchases/add', payload)
      try {
        const res = await request
        if (res.data.code === '200') {
          this.$message.success(payload.id ? '采购单更新成功' : '采购单创建成功')
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
.purchase-dialog {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.invoice-line {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}

.invoice-tip {
  color: #94a3b8;
  font-size: 12px;
}

.items-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.items-title {
  color: #0f172a;
  font-size: 15px;
  font-weight: 700;
}

.items-subtitle {
  margin-top: 4px;
  color: #64748b;
  font-size: 12px;
}

.total-bar {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 10px;
  padding: 14px 16px;
  border-radius: 14px;
  background: #f8fafc;
  color: #0f172a;
}

.total-bar strong {
  color: #0f766e;
  font-size: 20px;
}
</style>
