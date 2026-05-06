<template>
  <div class="staff-h5-page purchase-h5-page" v-loading="loading">
    <div class="h5-hero-card">
      <div>
        <div class="h5-page-kicker">员工微信 H5</div>
        <h2>采购记录</h2>
        <p>护士和管理员可在手机端查看采购单并新建采购单。</p>
      </div>
      <el-button type="primary" size="small" @click="openDialog()">新建采购单</el-button>
    </div>

    <div class="h5-section-card">
      <div class="section-title">采购单列表</div>
      <div v-if="rows.length" class="list-card-stack">
        <div v-for="item in rows" :key="item.id" class="list-card">
          <div class="list-card__title">{{ item.supplier_name || '未填写供应商' }}</div>
          <div class="list-card__meta">{{ formatDate(item.purchase_date) || '-' }} · {{ item.status || '-' }}</div>
          <div class="list-card__desc">总金额：¥{{ formatMoney(item.total_amount) }}</div>
          <div class="list-card__desc">明细数：{{ Array.isArray(item.items) ? item.items.length : 0 }}</div>
          <div class="list-card__actions">
            <el-button size="mini" type="primary" plain @click="openDialog(item, true)">查看</el-button>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无采购单"></el-empty>
    </div>

    <MaterialPurchaseDialog
      :visible.sync="dialogVisible"
      :purchase="activePurchase"
      :categories="categories"
      :current-user="currentUser"
      :readonly="dialogReadonly"
      dialog-width="94%"
      @saved="handleSaved"
    />
  </div>
</template>

<script>
import axios from 'axios'
import MaterialPurchaseDialog from '@/components/MaterialPurchaseDialog.vue'
import { fetchCachedResource } from '@/utils/offline/apiClient'
import { getStaffPortalQuery, saveStaffPortalSessionFromQuery } from '@/utils/portalSession'

export default {
  name: 'StaffMaterialPurchaseH5',
  components: { MaterialPurchaseDialog },
  data() {
    return {
      loading: false,
      rows: [],
      categories: [],
      dialogVisible: false,
      dialogReadonly: false,
      activePurchase: {},
      currentUser: {
        id: null,
        name: ''
      }
    }
  },
  mounted() {
    saveStaffPortalSessionFromQuery(this.$route.query)
    this.loadCurrentUser()
    this.loadCategories()
    this.loadRows()
  },
  methods: {
    formatDate(value) {
      return value ? String(value).slice(0, 10) : ''
    },
    formatMoney(value) {
      const amount = Number(value || 0)
      return amount.toFixed(2)
    },
    loadCurrentUser() {
      const query = getStaffPortalQuery(this.$route.query)
      fetchCachedResource({
        cacheKey: 'page:staff:overview',
        scope: '',
        url: '/staff-portal/overview',
        params: {
          accountId: query.accountId,
          staffToken: query.staffToken
        },
        loader: () => axios.get('/staff-portal/overview', {
          params: {
            accountId: query.accountId,
            staffToken: query.staffToken
          }
        })
      }).then(result => {
        const payload = result && result.data ? result.data : {}
        const account = payload.account || {}
        this.currentUser = {
          id: account.id || Number(query.accountId || 0) || null,
          name: account.name || ''
        }
      }).catch(() => {
        this.currentUser = {
          id: Number(query.accountId || 0) || null,
          name: ''
        }
      })
    },
    loadCategories() {
      fetchCachedResource({
        cacheKey: 'ref:material-categories',
        scope: '',
        url: '/material-categories/tree',
        params: { includeDisabled: false },
        loader: () => axios.get('/material-categories/tree', { params: { includeDisabled: false } })
      }).then(result => {
        this.categories = Array.isArray(result && result.data) ? result.data : []
      }).catch(() => {
        this.categories = []
      })
    },
    loadRows() {
      this.loading = true
      fetchCachedResource({
        cacheKey: 'page:staff:material-purchases',
        scope: '',
        url: '/material-purchases/search',
        params: { page: 1, size: 100 },
        loader: () => axios.get('/material-purchases/search', { params: { page: 1, size: 100 } }),
        notifier: message => this.$message.warning(message)
      }).then(result => {
        const data = result && result.data ? result.data : {}
        this.rows = Array.isArray(data.list) ? data.list : []
      }).catch(() => {
        this.rows = []
      }).finally(() => {
        this.loading = false
      })
    },
    openDialog(row = null, readonly = false) {
      this.dialogReadonly = readonly
      this.activePurchase = row ? Object.assign({}, row) : {}
      this.dialogVisible = true
    },
    handleSaved() {
      this.loadRows()
    }
  }
}
</script>

<style scoped>
.staff-h5-page {
  min-height: 100vh;
  background: #f5f7fb;
  padding: 14px;
  box-sizing: border-box;
}
.h5-hero-card,
.h5-section-card {
  background: #fff;
  border-radius: 18px;
  padding: 16px;
  box-shadow: 0 8px 24px rgba(31, 71, 136, 0.08);
  margin-bottom: 14px;
}
.h5-page-kicker {
  color: #409eff;
  font-size: 12px;
  margin-bottom: 8px;
}
.h5-hero-card h2 {
  margin: 0 0 6px;
  font-size: 24px;
  color: #1f2d3d;
}
.h5-hero-card p {
  margin: 0;
  color: #606266;
  font-size: 13px;
}
.section-title {
  font-size: 16px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 12px;
}
.list-card-stack {
  display: grid;
  gap: 12px;
}
.list-card {
  border-radius: 16px;
  background: #f8fbff;
  padding: 14px;
}
.list-card__title {
  color: #0f172a;
  font-size: 15px;
  font-weight: 700;
}
.list-card__meta,
.list-card__desc {
  margin-top: 6px;
  color: #64748b;
  font-size: 13px;
  line-height: 1.7;
}
.list-card__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 12px;
}
</style>
