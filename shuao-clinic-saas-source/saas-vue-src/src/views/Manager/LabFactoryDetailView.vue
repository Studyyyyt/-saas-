<template>
  <div class="lab-page">
    <div class="hero-card">
      <div>
        <div class="page-kicker">义齿加工</div>
        <h2>{{ factory.name || '加工厂详情' }}</h2>
        <p>{{ factory.contact_name || '未填写联系人' }} / {{ factory.contact_phone || '未填写电话' }}</p>
        <div class="hero-meta">
          <el-tag size="mini" :type="factory.status === '合作中' ? 'success' : 'info'">{{ factory.status || '-' }}</el-tag>
          <span>合作开始：{{ formatDate(factory.cooperation_start_date) || '-' }}</span>
          <span>{{ factory.address || '未填写地址' }}</span>
        </div>
      </div>
      <div class="hero-actions">
        <el-button @click="$router.back()">返回</el-button>
        <el-button v-if="canManage" type="primary" plain @click="openProductDialog()">新增价格项</el-button>
        <el-button v-if="canManage" @click="batchDialogVisible = true">批量录入价格表</el-button>
        <el-button v-if="canManage" @click="openTemplateDialog()">配置账单模板</el-button>
      </div>
    </div>

    <el-row :gutter="14" class="summary-row">
      <el-col :xs="24" :sm="8">
        <el-card shadow="never" class="summary-card">
          <div class="summary-label">价格项数量</div>
          <div class="summary-value">{{ products.length }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card shadow="never" class="summary-card">
          <div class="summary-label">启用价格项</div>
          <div class="summary-value">{{ enabledProductCount }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card shadow="never" class="summary-card">
          <div class="summary-label">模板数量</div>
          <div class="summary-value">{{ templates.length }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="table-card">
      <div class="section-head">
        <div>
          <h3>产品价格表</h3>
          <p>订单创建时可直接带出单价与规格。</p>
        </div>
      </div>
      <el-table
        :data="products"
        stripe
        v-loading="loadingProducts"
        :header-cell-style="{ backgroundColor: '#f8fafc', color: '#475569', fontWeight: '600' }"
      >
        <el-table-column prop="product_name" label="产品名称" min-width="160" />
        <el-table-column prop="product_spec" label="规格" min-width="120" />
        <el-table-column label="单价" width="100">
          <template slot-scope="scope">¥{{ formatMoney(scope.row.unit_price) }}</template>
        </el-table-column>
        <el-table-column prop="unit" label="单位" width="80" />
        <el-table-column label="状态" width="100">
          <template slot-scope="scope">
            <el-tag size="mini" :type="scope.row.status === '启用' ? 'success' : 'info'">{{ scope.row.status || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updated_at" label="更新时间" min-width="160">
          <template slot-scope="scope">{{ formatDateTime(scope.row.updated_at) }}</template>
        </el-table-column>
        <el-table-column v-if="canManage" label="操作" width="160" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" size="mini" @click="openProductDialog(scope.row)">编辑</el-button>
            <el-button type="text" size="mini" style="color:#ef4444" @click="deleteProduct(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card shadow="never" class="table-card">
      <div class="section-head">
        <div>
          <h3>账单模板</h3>
          <p>每个加工厂可配置多个模板，导入账单时选择对应模板即可复用列映射。</p>
        </div>
      </div>
      <div class="template-grid">
        <div v-for="item in templates" :key="item.id" class="template-card">
          <div class="template-card__title">{{ item.template_name }}</div>
          <div class="template-card__meta">表头行 {{ item.header_row }} / 数据起始行 {{ item.data_start_row }}</div>
          <div class="template-card__mapping">{{ describeTemplate(item.column_mapping) }}</div>
          <div class="template-card__foot">
            <span>{{ formatDateTime(item.updated_at) || '-' }}</span>
            <div class="footer-actions" v-if="canManage">
              <el-button size="mini" type="primary" plain @click="openTemplateDialog(item)">编辑</el-button>
              <el-button size="mini" type="danger" plain @click="deleteTemplate(item)">删除</el-button>
            </div>
          </div>
        </div>
      </div>
      <el-empty v-if="!templates.length" description="尚未配置账单模板"></el-empty>
    </el-card>

    <el-dialog :title="productForm.id ? '编辑价格项' : '新增价格项'" :visible.sync="productDialogVisible" width="460px">
      <el-form :model="productForm" label-width="100px">
        <el-form-item label="产品名称"><el-input v-model="productForm.product_name" /></el-form-item>
        <el-form-item label="规格"><el-input v-model="productForm.product_spec" /></el-form-item>
        <el-form-item label="单价">
          <el-input-number v-model="productForm.unit_price" :min="0" :precision="2" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="单位"><el-input v-model="productForm.unit" placeholder="如：颗 / 套" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="productForm.status" style="width:100%">
            <el-option label="启用" value="启用" />
            <el-option label="停用" value="停用" />
          </el-select>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="productDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingProduct" @click="saveProduct">保存</el-button>
      </span>
    </el-dialog>

    <el-dialog title="批量录入价格表" :visible.sync="batchDialogVisible" width="560px">
      <div class="batch-tip">每行一条，格式：产品名称,规格,单价,单位</div>
      <el-input
        v-model="batchText"
        type="textarea"
        :rows="10"
        placeholder="全瓷冠,常规,680,颗"
      />
      <span slot="footer">
        <el-button @click="batchDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingBatch" @click="saveBatchProducts">导入价格项</el-button>
      </span>
    </el-dialog>

    <LabBillTemplateConfigDialog
      :visible.sync="templateDialogVisible"
      :factory-id="factoryId"
      :template="activeTemplate"
      @saved="handleTemplateSaved"
    />
  </div>
</template>

<script>
import axios from 'axios'
import { getAdminSession } from '@/utils/adminSession'
import { canManageLabFactory, formatMoney, normalizeLabRole } from '@/utils/labConstants'
import LabBillTemplateConfigDialog from '@/components/LabBillTemplateConfigDialog.vue'
import { showApiError } from '@/utils/errorMessage'

function defaultProduct() {
  return {
    id: null,
    product_name: '',
    product_spec: '',
    unit_price: 0,
    unit: '',
    status: '启用'
  }
}

export default {
  name: 'LabFactoryDetailView',
  components: { LabBillTemplateConfigDialog },
  data() {
    return {
      currentUser: getAdminSession() || {},
      factoryId: this.$route.params.id || this.$route.query.id || '',
      factory: {},
      products: [],
      templates: [],
      loadingProducts: false,
      productDialogVisible: false,
      productForm: defaultProduct(),
      savingProduct: false,
      batchDialogVisible: false,
      batchText: '',
      savingBatch: false,
      templateDialogVisible: false,
      activeTemplate: {}
    }
  },
  computed: {
    canManage() {
      return canManageLabFactory(normalizeLabRole(this.currentUser && this.currentUser.role))
    },
    enabledProductCount() {
      return this.products.filter(item => item.status === '启用').length
    }
  },
  mounted() {
    this.loadAll()
  },
  methods: {
    formatMoney,
    formatDate(value) {
      return value ? String(value).slice(0, 10) : ''
    },
    formatDateTime(value) {
      return value ? String(value).slice(0, 19).replace('T', ' ') : ''
    },
    describeTemplate(raw) {
      try {
        const mapping = typeof raw === 'string' ? JSON.parse(raw) : raw
        return Object.keys(mapping || {}).map(key => `${key} → ${mapping[key]}`).join('；')
      } catch (error) {
        return '映射异常'
      }
    },
    async loadAll() {
      if (!this.factoryId) {
        this.$message.warning('缺少加工厂ID')
        return
      }
      await Promise.all([
        this.loadFactory(),
        this.loadProducts(),
        this.loadTemplates()
      ])
    },
    async loadFactory() {
      const res = await axios.get(`/lab-factories/${this.factoryId}`)
      if (res.data.code === '200') {
        this.factory = res.data.data || {}
      } else {
        this.$message.error(res.data.msg || '获取加工厂失败')
      }
    },
    async loadProducts() {
      this.loadingProducts = true
      try {
        const res = await axios.get(`/lab-factories/${this.factoryId}/products`)
        this.products = Array.isArray(res.data.data) ? res.data.data : []
      } catch (error) {
        this.products = []
      } finally {
        this.loadingProducts = false
      }
    },
    async loadTemplates() {
      try {
        const res = await axios.get(`/lab-factories/${this.factoryId}/templates`)
        this.templates = Array.isArray(res.data.data) ? res.data.data : []
      } catch (error) {
        this.templates = []
      }
    },
    openProductDialog(row) {
      this.productForm = row ? Object.assign(defaultProduct(), row, { unit_price: Number(row.unit_price || 0) }) : defaultProduct()
      this.productDialogVisible = true
    },
    async saveProduct() {
      if (!String(this.productForm.product_name || '').trim()) {
        this.$message.warning('产品名称不能为空')
        return
      }
      this.savingProduct = true
      const request = this.productForm.id
        ? axios.put(`/lab-factories/${this.factoryId}/products/edit`, this.productForm)
        : axios.post(`/lab-factories/${this.factoryId}/products/add`, this.productForm)
      try {
        const res = await request
        if (res.data.code === '200') {
          this.$message.success(this.productForm.id ? '更新成功' : '新增成功')
          this.productDialogVisible = false
          this.loadProducts()
        } else {
          this.$message.error(res.data.msg || '保存失败')
        }
      } catch (error) {
        this.$message.error('保存失败')
      } finally {
        this.savingProduct = false
      }
    },
    deleteProduct(row) {
      this.$confirm(`确认删除“${row.product_name}”吗？`, '提示', { type: 'warning' }).then(async () => {
        const res = await axios.delete(`/lab-factories/${this.factoryId}/products/delete/${row.id}`)
        if (res.data.code === '200') {
          this.$message.success('删除成功')
          this.loadProducts()
        } else {
          this.$message.error(res.data.msg || '删除失败')
        }
      }).catch(() => {})
    },
    saveBatchProducts() {
      const lines = String(this.batchText || '').split('\n').map(item => item.trim()).filter(Boolean)
      if (!lines.length) {
        this.$message.warning('请先输入批量数据')
        return
      }
      const items = []
      for (const line of lines) {
        const parts = line.split(',').map(item => item.trim())
        if (!parts[0]) {
          this.$message.warning(`存在空产品名称：${line}`)
          return
        }
        items.push({
          product_name: parts[0],
          product_spec: parts[1] || '',
          unit_price: Number(parts[2] || 0),
          unit: parts[3] || '',
          status: '启用'
        })
      }
      this.savingBatch = true
      axios.post(`/lab-factories/${this.factoryId}/products/batchSave`, items).then(res => {
        if (res.data.code === '200') {
          this.$message.success('批量录入成功')
          this.batchDialogVisible = false
          this.batchText = ''
          this.loadProducts()
        } else {
          this.$message.error(res.data.msg || '批量录入失败')
        }
      }).catch(() => {
        this.$message.error('批量录入失败')
      }).finally(() => {
        this.savingBatch = false
      })
    },
    openTemplateDialog(row) {
      this.activeTemplate = row ? Object.assign({}, row) : {}
      this.templateDialogVisible = true
    },
    handleTemplateSaved() {
      this.loadTemplates()
    },
    deleteTemplate(row) {
      this.$confirm(`确认删除模板“${row.template_name}”吗？`, '提示', { type: 'warning' }).then(async () => {
        const res = await axios.delete(`/lab-factories/${this.factoryId}/templates/delete/${row.id}`)
        if (res.data.code === '200') {
          this.$message.success('删除成功')
          this.loadTemplates()
        } else {
          this.$message.error(res.data.msg || '删除失败')
        }
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.lab-page { display:flex; flex-direction:column; gap:14px; }
.hero-card { display:flex; justify-content:space-between; align-items:flex-start; gap:16px; padding:18px; border-radius:18px; background:#fff; box-shadow:0 8px 24px rgba(31,71,136,.08); }
.page-kicker { color:#64748b; font-size:13px; }
.hero-card h2 { margin:6px 0 8px; color:#0f172a; font-size:24px; }
.hero-card p { margin:0; color:#94a3b8; }
.hero-meta { display:flex; gap:10px; flex-wrap:wrap; margin-top:10px; color:#64748b; font-size:12px; }
.hero-actions { display:flex; gap:10px; flex-wrap:wrap; }
.summary-row { margin:0 !important; }
.summary-card,.table-card { border-radius:18px; }
.summary-label { color:#94a3b8; font-size:13px; }
.summary-value { margin-top:8px; font-size:28px; font-weight:700; color:#0f172a; }
.section-head { display:flex; justify-content:space-between; align-items:flex-start; gap:12px; margin-bottom:12px; }
.section-head h3 { margin:0 0 6px; color:#0f172a; }
.section-head p { margin:0; color:#94a3b8; font-size:13px; }
.template-grid { display:grid; grid-template-columns:repeat(auto-fill, minmax(280px, 1fr)); gap:14px; }
.template-card { border-radius:16px; padding:16px; background:#fff; border:1px solid #e2e8f0; }
.template-card__title { color:#0f172a; font-size:16px; font-weight:700; }
.template-card__meta { margin-top:6px; color:#2563eb; font-size:12px; }
.template-card__mapping { margin-top:10px; color:#475569; font-size:13px; line-height:1.6; min-height:48px; }
.template-card__foot { display:flex; justify-content:space-between; align-items:flex-start; gap:10px; margin-top:14px; color:#94a3b8; font-size:12px; }
.footer-actions { display:flex; gap:8px; }
.batch-tip { margin-bottom:10px; color:#64748b; font-size:12px; }
@media (max-width: 768px) {
  .hero-card { flex-direction:column; }
}
</style>
