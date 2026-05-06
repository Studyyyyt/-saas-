<template>
  <div class="material-page">
    <div class="hero-card" v-loading="loading">
      <div>
        <div class="page-kicker">耗材采购</div>
        <h2>采购单详情</h2>
        <p>{{ purchase.supplier_name || '未填写供应商' }} / {{ formatDate(purchase.purchase_date) || '-' }}</p>
        <div class="hero-meta">
          <el-tag size="mini" :type="purchaseStatusTagType(purchase.status)">{{ purchase.status || '-' }}</el-tag>
          <span>付款方式：{{ purchase.payment_method || '-' }}</span>
          <span>录入人：{{ purchase.created_by_name || '-' }}</span>
        </div>
      </div>
      <div class="hero-actions">
        <el-button @click="$router.back()">返回</el-button>
        <el-button v-if="purchase.invoice_image_url" @click="openInvoice">查看发票</el-button>
      </div>
    </div>

    <el-row :gutter="14" class="summary-row">
      <el-col :xs="24" :sm="8">
        <el-card shadow="never" class="summary-card">
          <div class="summary-label">总金额</div>
          <div class="summary-value money">¥{{ formatMaterialMoney(purchase.total_amount) }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card shadow="never" class="summary-card">
          <div class="summary-label">明细数量</div>
          <div class="summary-value">{{ Array.isArray(purchase.items) ? purchase.items.length : 0 }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card shadow="never" class="summary-card">
          <div class="summary-label">状态</div>
          <div class="summary-value">{{ purchase.status || '-' }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="detail-card">
      <div class="detail-grid">
        <div class="detail-item"><span>采购日期</span><strong>{{ formatDate(purchase.purchase_date) || '-' }}</strong></div>
        <div class="detail-item"><span>供应商</span><strong>{{ purchase.supplier_name || '-' }}</strong></div>
        <div class="detail-item"><span>付款方式</span><strong>{{ purchase.payment_method || '-' }}</strong></div>
        <div class="detail-item"><span>创建时间</span><strong>{{ formatDateTime(purchase.created_at) || '-' }}</strong></div>
        <div class="detail-item"><span>作废人</span><strong>{{ purchase.voided_by_name || '-' }}</strong></div>
        <div class="detail-item"><span>作废时间</span><strong>{{ formatDateTime(purchase.voided_at) || '-' }}</strong></div>
      </div>
      <div class="detail-remark">
        <div class="remark-label">备注</div>
        <div class="remark-value">{{ purchase.remark || '无' }}</div>
      </div>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table :data="purchase.items || []" border stripe size="small">
        <el-table-column prop="material_name" label="耗材名称" min-width="160" />
        <el-table-column prop="material_spec" label="规格" min-width="140" />
        <el-table-column label="单价" width="110">
          <template slot-scope="scope">¥{{ formatMaterialMoney(scope.row.unit_price) }}</template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="90" align="right" />
        <el-table-column label="小计" width="110">
          <template slot-scope="scope">¥{{ formatMaterialMoney(scope.row.subtotal) }}</template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!(purchase.items || []).length" description="暂无采购明细"></el-empty>
    </el-card>
  </div>
</template>

<script>
import axios from 'axios'
import { formatMaterialMoney, purchaseStatusTagType } from '@/utils/materialConstants'
import { showApiError } from '@/utils/errorMessage'

export default {
  name: 'MaterialPurchaseDetailView',
  data() {
    return {
      purchaseId: this.$route.params.id || this.$route.query.id || '',
      loading: false,
      purchase: {}
    }
  },
  mounted() {
    this.loadDetail()
  },
  methods: {
    formatMaterialMoney,
    purchaseStatusTagType,
    formatDate(value) {
      return value ? String(value).slice(0, 10) : ''
    },
    formatDateTime(value) {
      return value ? String(value).slice(0, 19).replace('T', ' ') : ''
    },
    async loadDetail() {
      if (!this.purchaseId) {
        this.$message.warning('缺少采购单ID')
        return
      }
      this.loading = true
      try {
        const res = await axios.get(`/material-purchases/${this.purchaseId}`)
        if (res.data.code === '200') {
          this.purchase = res.data.data || {}
        } else {
          this.$message.error(res.data.msg || '获取采购单失败')
        }
      } catch (error) {
        showApiError(this, '获取采购单详情', error)
      } finally {
        this.loading = false
      }
    },
    openInvoice() {
      if (!this.purchase.id) return
      window.open(`/material-purchases/invoice/${this.purchase.id}`)
    }
  }
}
</script>

<style scoped>
.material-page { display:flex; flex-direction:column; gap:14px; }
.hero-card,.summary-card,.detail-card,.table-card { border-radius:18px; }
.hero-card { display:flex; justify-content:space-between; align-items:flex-start; gap:16px; padding:18px; background:#fff; box-shadow:0 8px 24px rgba(31,71,136,.08); }
.page-kicker { color:#64748b; font-size:13px; }
.hero-card h2 { margin:6px 0 8px; color:#0f172a; font-size:24px; }
.hero-card p { margin:0; color:#94a3b8; }
.hero-meta { display:flex; gap:10px; flex-wrap:wrap; margin-top:10px; color:#64748b; font-size:12px; }
.hero-actions { display:flex; gap:10px; flex-wrap:wrap; }
.summary-row { margin:0 !important; }
.summary-label { color:#94a3b8; font-size:13px; }
.summary-value { margin-top:8px; font-size:26px; font-weight:700; color:#0f172a; }
.summary-value.money { color:#0f766e; }
.detail-card { padding:18px; background:#fff; }
.detail-grid { display:grid; grid-template-columns:repeat(auto-fill, minmax(220px, 1fr)); gap:12px; }
.detail-item { padding:14px; border-radius:14px; background:#f8fafc; display:flex; flex-direction:column; gap:6px; }
.detail-item span { color:#64748b; font-size:12px; }
.detail-item strong { color:#0f172a; }
.detail-remark { margin-top:14px; padding:14px; border-radius:14px; background:#f8fafc; }
.remark-label { color:#64748b; font-size:12px; }
.remark-value { margin-top:6px; color:#0f172a; line-height:1.7; }
</style>
