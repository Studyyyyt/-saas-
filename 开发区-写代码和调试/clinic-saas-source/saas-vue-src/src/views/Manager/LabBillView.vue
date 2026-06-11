<template>
  <div class="lab-page">
    <div class="hero-card">
      <div>
        <div class="page-kicker">义齿加工</div>
        <h2>月度账单</h2>
        <p>按加工厂模板导入 Excel 账单，自动完成订单匹配、异常归类与对账确认。</p>
      </div>
      <div class="hero-actions">
        <el-button v-if="canOperate" type="primary" plain @click="importDialogVisible = true">导入账单</el-button>
        <el-button @click="loadList">刷新</el-button>
      </div>
    </div>

    <el-card shadow="never" class="query-card">
      <div class="query-row">
        <el-select v-model="filters.factoryId" clearable filterable class="query-select" placeholder="加工厂">
          <el-option v-for="item in factories" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
        <el-select v-model="filters.status" clearable class="query-select" placeholder="账单状态">
          <el-option v-for="item in LAB_BILL_STATUS_OPTIONS" :key="item" :label="item" :value="item" />
        </el-select>
        <el-date-picker
          v-model="filters.billMonth"
          type="month"
          value-format="yyyy-MM"
          class="query-select"
          placeholder="账单月份"
        />
        <el-button type="primary" icon="el-icon-search" @click="loadList">查询</el-button>
        <el-button icon="el-icon-refresh" @click="resetFilters">重置</el-button>
      </div>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table
        :data="rows"
        stripe
        v-loading="loading"
        :header-cell-style="{ backgroundColor: '#f8fafc', color: '#475569', fontWeight: '600' }"
      >
        <el-table-column prop="factory_name" label="加工厂" min-width="160" />
        <el-table-column prop="bill_month" label="账单月份" width="110" />
        <el-table-column label="总金额" width="110">
          <template slot-scope="scope">¥{{ formatMoney(scope.row.total_amount) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="120" align="center">
          <template slot-scope="scope">
            <el-tag size="mini" :type="billStatusTagType(scope.row.status)">{{ scope.row.status || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="matched_count" label="完全匹配" width="110" align="right" />
        <el-table-column prop="mismatched_count" label="不符" width="90" align="right" />
        <el-table-column prop="only_in_bill_count" label="仅账单有" width="100" align="right" />
        <el-table-column prop="only_in_system_count" label="仅系统有" width="100" align="right" />
        <el-table-column prop="imported_by_name" label="导入人" width="110" />
        <el-table-column prop="imported_at" label="导入时间" min-width="160">
          <template slot-scope="scope">{{ formatDateTime(scope.row.imported_at) }}</template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="210">
          <template slot-scope="scope">
            <el-button type="text" size="mini" @click="goDetail(scope.row)">详情</el-button>
            <el-button type="text" size="mini" @click="downloadBill(scope.row)">下载原件</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && !rows.length" description="暂无月度账单"></el-empty>

      <div v-else class="pagination-row">
        <el-pagination
          :current-page="currentPage"
          :page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="totalItems"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <LabBillImportDialog
      :visible.sync="importDialogVisible"
      :factories="factories"
      :current-user="currentUser"
      @saved="handleImportSaved"
    />
  </div>
</template>

<script>
import axios from 'axios'
import { getAdminSession } from '@/utils/adminSession'
import LabBillImportDialog from '@/components/LabBillImportDialog.vue'
import { showApiError } from '@/utils/errorMessage'
import {
  LAB_BILL_STATUS_OPTIONS,
  billStatusTagType,
  canOperateLabBills,
  formatMoney,
  normalizeLabRole
} from '@/utils/labConstants'

export default {
  name: 'LabBillView',
  components: { LabBillImportDialog },
  data() {
    return {
      LAB_BILL_STATUS_OPTIONS,
      currentUser: getAdminSession() || {},
      loading: false,
      rows: [],
      totalItems: 0,
      currentPage: 1,
      pageSize: 10,
      factories: [],
      filters: {
        factoryId: '',
        status: '',
        billMonth: ''
      },
      importDialogVisible: false
    }
  },
  computed: {
    canOperate() {
      return canOperateLabBills(normalizeLabRole(this.currentUser && this.currentUser.role))
    }
  },
  mounted() {
    this.loadFactories()
    this.loadList()
  },
  methods: {
    formatMoney,
    billStatusTagType,
    formatDateTime(value) {
      return value ? String(value).slice(0, 19).replace('T', ' ') : ''
    },
    async loadFactories() {
      try {
        const res = await axios.get('/lab-factories/selectEnabled')
        this.factories = Array.isArray(res.data.data) ? res.data.data : []
      } catch (error) {
        this.factories = []
      }
    },
    async loadList() {
      this.loading = true
      try {
        const res = await axios.get('/lab-bills/search', {
          params: {
            factoryId: this.filters.factoryId || undefined,
            status: this.filters.status || undefined,
            billMonth: this.filters.billMonth || undefined,
            page: this.currentPage,
            size: this.pageSize
          }
        })
        const data = res.data && res.data.data ? res.data.data : {}
        this.rows = Array.isArray(data.list) ? data.list : []
        this.totalItems = Number(data.total || 0)
      } catch (error) {
        this.rows = []
        this.totalItems = 0
        showApiError(this, '获取技工账单', error)
      } finally {
        this.loading = false
      }
    },
    resetFilters() {
      this.filters = { factoryId: '', status: '', billMonth: '' }
      this.currentPage = 1
      this.loadList()
    },
    handleSizeChange(size) {
      this.pageSize = size
      this.currentPage = 1
      this.loadList()
    },
    handleCurrentChange(page) {
      this.currentPage = page
      this.loadList()
    },
    handleImportSaved() {
      this.loadList()
    },
    goDetail(row) {
      this.$router.push({ path: `/lab-bills/${row.id}` }).catch(() => {})
    },
    downloadBill(row) {
      window.open(`/lab-bills/file/${row.id}`)
    }
  }
}
</script>

<style scoped>
.lab-page { display:flex; flex-direction:column; gap:14px; }
.hero-card { display:flex; justify-content:space-between; align-items:center; gap:16px; padding:18px; border-radius:18px; background:#fff; box-shadow:0 8px 24px rgba(31,71,136,.08); }
.page-kicker { color:#64748b; font-size:13px; }
.hero-card h2 { margin:6px 0 8px; color:#0f172a; font-size:24px; }
.hero-card p { margin:0; color:#94a3b8; }
.hero-actions { display:flex; gap:10px; flex-wrap:wrap; }
.query-card,.table-card { border-radius:18px; }
.query-row { display:flex; gap:12px; flex-wrap:wrap; }
.query-select { width:180px; }
.pagination-row { display:flex; justify-content:flex-end; padding-top:16px; }
@media (max-width: 768px) {
  .hero-card { flex-direction:column; align-items:flex-start; }
  .query-select { width:100%; }
  .pagination-row { justify-content:center; }
}
</style>
