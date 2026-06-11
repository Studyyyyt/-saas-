<template>
  <div class="lab-page" v-loading="loading">
    <div class="hero-card">
      <div>
        <div class="page-kicker">义齿加工</div>
        <h2>{{ bill.factory_name || '账单详情' }}</h2>
        <p>{{ bill.bill_month || '-' }} 月度账单，对账状态：{{ bill.status || '-' }}</p>
        <div class="hero-meta">
          <el-tag size="mini" :type="billStatusTagType(bill.status)">{{ bill.status || '-' }}</el-tag>
          <span>导入人：{{ bill.imported_by_name || '-' }}</span>
          <span>导入时间：{{ formatDateTime(bill.imported_at) || '-' }}</span>
        </div>
      </div>
      <div class="hero-actions">
        <el-button @click="$router.back()">返回</el-button>
        <el-button @click="downloadBill">下载原件</el-button>
        <el-button
          v-if="canOperate && bill.id && bill.status !== '已完成对账'"
          type="primary"
          :disabled="!detail.allResolved"
          :loading="confirming"
          @click="confirmBill"
        >完成对账</el-button>
      </div>
    </div>

    <el-row :gutter="14" class="summary-row">
      <el-col :xs="12" :sm="6">
        <el-card shadow="never" class="summary-card">
          <div class="summary-label">账单金额</div>
          <div class="summary-value money">¥{{ formatMoney(bill.total_amount) }}</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="never" class="summary-card">
          <div class="summary-label">完全匹配</div>
          <div class="summary-value">{{ bill.matched_count || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="never" class="summary-card">
          <div class="summary-label">异常待处理</div>
          <div class="summary-value warn">{{ detail.pendingIssueCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="never" class="summary-card">
          <div class="summary-label">对账确认</div>
          <div class="summary-value">{{ detail.allResolved ? '可确认' : '未完成' }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-alert
      v-if="bill.status !== '已完成对账'"
      :title="detail.allResolved ? '所有异常已处理，可点击“完成对账”写入义齿加工支出。' : '仍有异常待处理，请先完成人工确认或忽略。'"
      :type="detail.allResolved ? 'success' : 'warning'"
      :closable="false"
      show-icon
    />

    <el-collapse v-model="activePanels" class="result-collapse">
      <el-collapse-item :title="`完全匹配（${matchedItems.length}）`" name="matched">
        <el-table :data="matchedItems" size="small" border>
          <el-table-column prop="raw_row_number" label="行号" width="80" />
          <el-table-column prop="patient_name" label="患者" min-width="120" />
          <el-table-column prop="product_name" label="产品" min-width="140" />
          <el-table-column prop="product_spec" label="规格" min-width="120" />
          <el-table-column prop="quantity" label="数量" width="90" />
          <el-table-column label="金额" width="100">
            <template slot-scope="scope">¥{{ formatMoney(scope.row.total_amount) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="110">
            <template slot-scope="scope">
              <el-tag size="mini" type="success">{{ scope.row.match_status }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-collapse-item>

      <el-collapse-item :title="`数量/金额不符（${mismatchItems.length}）`" name="mismatch">
        <el-table :data="mismatchItems" size="small" border>
          <el-table-column prop="raw_row_number" label="行号" width="80" />
          <el-table-column prop="patient_name" label="患者" min-width="120" />
          <el-table-column prop="product_name" label="产品" min-width="140" />
          <el-table-column prop="quantity" label="数量" width="90" />
          <el-table-column label="金额" width="100">
            <template slot-scope="scope">¥{{ formatMoney(scope.row.total_amount) }}</template>
          </el-table-column>
          <el-table-column prop="matched_lab_order_id" label="系统订单ID" width="110" />
          <el-table-column label="匹配状态" width="110">
            <template slot-scope="scope">
              <el-tag size="mini" :type="matchStatusTagType(scope.row.match_status)">{{ scope.row.match_status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="处理状态" width="110">
            <template slot-scope="scope">
              <el-tag size="mini" :type="resolutionStatusTagType(scope.row.resolution_status)">{{ scope.row.resolution_status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="处理备注" min-width="220">
            <template slot-scope="scope">
              <div class="resolve-box">
                <el-select
                  v-model="scope.row.resolution_status"
                  size="mini"
                  :disabled="!canOperate"
                  style="width:100px"
                >
                  <el-option label="待处理" value="待处理" />
                  <el-option label="已处理" value="已处理" />
                  <el-option label="已忽略" value="已忽略" />
                </el-select>
                <el-input
                  v-model="scope.row.resolution_remark"
                  size="mini"
                  :disabled="!canOperate"
                  placeholder="填写处理说明"
                />
                <el-button v-if="canOperate" size="mini" type="primary" plain @click="saveBillItem(scope.row)">保存</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </el-collapse-item>

      <el-collapse-item :title="`仅账单有（${onlyInBillItems.length}）`" name="billOnly">
        <el-table :data="onlyInBillItems" size="small" border>
          <el-table-column prop="raw_row_number" label="行号" width="80" />
          <el-table-column prop="patient_name" label="患者" min-width="120" />
          <el-table-column prop="product_name" label="产品" min-width="140" />
          <el-table-column prop="product_spec" label="规格" min-width="120" />
          <el-table-column prop="quantity" label="数量" width="90" />
          <el-table-column label="金额" width="100">
            <template slot-scope="scope">¥{{ formatMoney(scope.row.total_amount) }}</template>
          </el-table-column>
          <el-table-column label="处理状态" width="110">
            <template slot-scope="scope">
              <el-tag size="mini" :type="resolutionStatusTagType(scope.row.resolution_status)">{{ scope.row.resolution_status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="处理备注" min-width="240">
            <template slot-scope="scope">
              <div class="resolve-box">
                <el-select
                  v-model="scope.row.resolution_status"
                  size="mini"
                  :disabled="!canOperate"
                  style="width:100px"
                >
                  <el-option label="待处理" value="待处理" />
                  <el-option label="已处理" value="已处理" />
                  <el-option label="已忽略" value="已忽略" />
                </el-select>
                <el-input
                  v-model="scope.row.resolution_remark"
                  size="mini"
                  :disabled="!canOperate"
                  placeholder="记录漏录原因或处理结果"
                />
                <el-button v-if="canOperate" size="mini" type="primary" plain @click="saveBillItem(scope.row)">保存</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </el-collapse-item>

      <el-collapse-item :title="`仅系统有（${onlyInSystemOrders.length}）`" name="systemOnly">
        <el-table :data="onlyInSystemOrders" size="small" border>
          <el-table-column prop="lab_order_id" label="系统订单ID" width="110" />
          <el-table-column label="患者" min-width="120">
            <template slot-scope="scope">{{ scope.row.order && scope.row.order.patient_name ? scope.row.order.patient_name : '-' }}</template>
          </el-table-column>
          <el-table-column label="产品" min-width="140">
            <template slot-scope="scope">{{ scope.row.order && scope.row.order.product_name ? scope.row.order.product_name : '-' }}</template>
          </el-table-column>
          <el-table-column label="订单金额" width="110">
            <template slot-scope="scope">¥{{ formatMoney(scope.row.order && scope.row.order.total_amount) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template slot-scope="scope">{{ scope.row.order && scope.row.order.status ? scope.row.order.status : '-' }}</template>
          </el-table-column>
          <el-table-column label="处理状态" width="110">
            <template slot-scope="scope">
              <el-tag size="mini" :type="resolutionStatusTagType(scope.row.resolution_status)">{{ scope.row.resolution_status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="处理备注" min-width="260">
            <template slot-scope="scope">
              <div class="resolve-box">
                <el-select
                  v-model="scope.row.resolution_status"
                  size="mini"
                  :disabled="!canOperate"
                  style="width:100px"
                >
                  <el-option label="待处理" value="待处理" />
                  <el-option label="已处理" value="已处理" />
                  <el-option label="已忽略" value="已忽略" />
                </el-select>
                <el-input
                  v-model="scope.row.resolution_remark"
                  size="mini"
                  :disabled="!canOperate"
                  placeholder="记录工厂漏算或系统无需结算原因"
                />
                <el-button v-if="canOperate" size="mini" type="primary" plain @click="saveSystemOnly(scope.row)">保存</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </el-collapse-item>
    </el-collapse>
  </div>
</template>

<script>
import axios from 'axios'
import { getAdminSession } from '@/utils/adminSession'
import { showApiError } from '@/utils/errorMessage'
import {
  billStatusTagType,
  canOperateLabBills,
  formatMoney,
  matchStatusTagType,
  normalizeLabRole,
  resolutionStatusTagType
} from '@/utils/labConstants'

export default {
  name: 'LabBillDetailView',
  data() {
    return {
      currentUser: getAdminSession() || {},
      billId: this.$route.params.id || this.$route.query.id || '',
      loading: false,
      confirming: false,
      activePanels: ['matched', 'mismatch', 'billOnly', 'systemOnly'],
      bill: {},
      detail: {
        matchedItems: [],
        mismatchItems: [],
        onlyInBillItems: [],
        onlyInSystemOrders: [],
        allResolved: false,
        pendingIssueCount: 0
      }
    }
  },
  computed: {
    canOperate() {
      return canOperateLabBills(normalizeLabRole(this.currentUser && this.currentUser.role))
    },
    matchedItems() {
      return this.detail.matchedItems || []
    },
    mismatchItems() {
      return this.detail.mismatchItems || []
    },
    onlyInBillItems() {
      return this.detail.onlyInBillItems || []
    },
    onlyInSystemOrders() {
      return this.detail.onlyInSystemOrders || []
    }
  },
  mounted() {
    this.loadDetail()
  },
  methods: {
    formatMoney,
    billStatusTagType,
    matchStatusTagType,
    resolutionStatusTagType,
    formatDateTime(value) {
      return value ? String(value).slice(0, 19).replace('T', ' ') : ''
    },
    async loadDetail() {
      if (!this.billId) {
        this.$message.warning('缺少账单ID')
        return
      }
      this.loading = true
      try {
        const res = await axios.get(`/lab-bills/${this.billId}`)
        if (res.data.code === '200') {
          const data = res.data.data || {}
          this.bill = data.bill || {}
          this.detail = Object.assign({
            matchedItems: [],
            mismatchItems: [],
            onlyInBillItems: [],
            onlyInSystemOrders: [],
            allResolved: false,
            pendingIssueCount: 0
          }, data)
        } else {
          this.$message.error(res.data.msg || '获取账单详情失败')
        }
      } catch (error) {
        showApiError(this, '获取账单详情', error)
      } finally {
        this.loading = false
      }
    },
    async saveBillItem(row) {
      const res = await axios.put(`/lab-bills/items/${row.id}/resolution`, {
        resolution_status: row.resolution_status,
        resolution_remark: row.resolution_remark,
        resolved_by: this.currentUser && this.currentUser.id ? Number(this.currentUser.id) : null,
        resolved_by_name: this.currentUser && this.currentUser.name ? this.currentUser.name : ''
      })
      if (res.data.code === '200') {
        this.$message.success('处理结果已保存')
        this.loadDetail()
      } else {
        this.$message.error(res.data.msg || '保存失败')
      }
    },
    async saveSystemOnly(row) {
      const res = await axios.put(`/lab-bills/unmatched-orders/${row.id}/resolution`, {
        resolution_status: row.resolution_status,
        resolution_remark: row.resolution_remark,
        resolved_by: this.currentUser && this.currentUser.id ? Number(this.currentUser.id) : null,
        resolved_by_name: this.currentUser && this.currentUser.name ? this.currentUser.name : ''
      })
      if (res.data.code === '200') {
        this.$message.success('处理结果已保存')
        this.loadDetail()
      } else {
        this.$message.error(res.data.msg || '保存失败')
      }
    },
    async confirmBill() {
      if (!this.bill.id) return
      this.confirming = true
      try {
        const res = await axios.post(`/lab-bills/confirm/${this.bill.id}`, {
          confirmed_by: this.currentUser && this.currentUser.id ? Number(this.currentUser.id) : null,
          confirmed_by_name: this.currentUser && this.currentUser.name ? this.currentUser.name : ''
        })
        if (res.data.code === '200') {
          this.$message.success('对账完成，义齿加工支出已写入财务')
          this.loadDetail()
        } else {
          this.$message.error(res.data.msg || '对账确认失败')
        }
      } catch (error) {
        this.$message.error('对账确认失败')
      } finally {
        this.confirming = false
      }
    },
    downloadBill() {
      if (!this.bill.id) return
      window.open(`/lab-bills/file/${this.bill.id}`)
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
.summary-card { border-radius:18px; }
.summary-label { color:#94a3b8; font-size:13px; }
.summary-value { margin-top:8px; font-size:26px; font-weight:700; color:#0f172a; }
.summary-value.money { color:#0f766e; }
.summary-value.warn { color:#b45309; }
.result-collapse { border-radius:18px; overflow:hidden; }
.resolve-box { display:flex; gap:8px; align-items:center; }
@media (max-width: 900px) {
  .hero-card { flex-direction:column; }
  .resolve-box { flex-direction:column; align-items:stretch; }
}
</style>
