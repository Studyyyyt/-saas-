<template>
  <div class="material-page">
    <div class="hero-card">
      <div>
        <div class="page-kicker">耗材采购</div>
        <h2>耗材档案</h2>
        <p>支持分类树筛选、低库存预警和采购录入前的统一耗材主数据维护。</p>
      </div>
      <div class="hero-actions">
        <el-button v-if="canEdit" type="primary" plain @click="openDialog()">新增耗材</el-button>
        <el-button @click="loadList">刷新</el-button>
      </div>
    </div>

    <el-card shadow="never" class="query-card">
      <div class="query-row">
        <CategoryTreeSelect
          v-model="filters.categoryId"
          :options="categories"
          placeholder="分类筛选"
          title="分类筛选"
        />
        <el-input
          v-model="filters.keyword"
          class="query-input"
          clearable
          placeholder="搜索耗材名称 / 规格 / 品牌"
          @keyup.enter.native="loadList"
        />
        <el-select v-model="filters.status" clearable class="query-select" placeholder="状态">
          <el-option v-for="item in MATERIAL_STATUS_OPTIONS" :key="item" :label="item" :value="item" />
        </el-select>
        <el-switch
          v-model="filters.lowStockOnly"
          active-text="仅低库存"
          inactive-text="全部"
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
        :row-class-name="rowClassName"
        :header-cell-style="{ backgroundColor: '#f8fafc', color: '#475569', fontWeight: '600' }"
      >
        <el-table-column prop="name" label="耗材名称" min-width="150" />
        <el-table-column prop="spec" label="规格" min-width="140" />
        <el-table-column prop="brand" label="品牌" min-width="120" />
        <el-table-column prop="category_name" label="分类" min-width="120" />
        <el-table-column prop="unit" label="单位" width="80" />
        <el-table-column prop="current_stock" label="当前库存" width="100" align="right" />
        <el-table-column prop="min_stock_alert" label="预警阈值" width="100" align="right" />
        <el-table-column label="状态" width="90" align="center">
          <template slot-scope="scope">
            <el-tag size="mini" :type="materialStatusTagType(scope.row.status)">{{ scope.row.status || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="预警差额" width="100" align="right">
          <template slot-scope="scope">{{ scope.row.alert_gap || 0 }}</template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="180">
          <template slot-scope="scope">
            <el-button type="text" size="mini" @click="openDialog(scope.row, !canEdit)">详情</el-button>
            <el-button v-if="canEdit" type="text" size="mini" @click="openDialog(scope.row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && !rows.length" description="暂无耗材档案"></el-empty>
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

    <MaterialDialog
      :visible.sync="dialogVisible"
      :material="activeMaterial"
      :categories="categories"
      :readonly="dialogReadonly"
      @saved="loadList"
    />
  </div>
</template>

<script>
import axios from 'axios'
import { getAdminSession } from '@/utils/adminSession'
import CategoryTreeSelect from '@/components/CategoryTreeSelect.vue'
import MaterialDialog from '@/components/MaterialDialog.vue'
import { MATERIAL_STATUS_OPTIONS, canEditMaterials, materialStatusTagType, normalizeMaterialRole } from '@/utils/materialConstants'
import { showApiError } from '@/utils/errorMessage'

export default {
  name: 'MaterialView',
  components: { CategoryTreeSelect, MaterialDialog },
  data() {
    return {
      MATERIAL_STATUS_OPTIONS,
      currentUser: getAdminSession() || {},
      categories: [],
      rows: [],
      loading: false,
      totalItems: 0,
      currentPage: 1,
      pageSize: 10,
      filters: {
        categoryId: '',
        keyword: '',
        status: '',
        lowStockOnly: false
      },
      dialogVisible: false,
      dialogReadonly: false,
      activeMaterial: {}
    }
  },
  computed: {
    canEdit() {
      return canEditMaterials(normalizeMaterialRole(this.currentUser && this.currentUser.role))
    }
  },
  mounted() {
    this.loadCategories()
    this.loadList()
  },
  methods: {
    materialStatusTagType,
    async loadCategories() {
      try {
        const res = await axios.get('/material-categories/tree', { params: { includeDisabled: true } })
        this.categories = Array.isArray(res.data.data) ? res.data.data : []
      } catch (error) {
        this.categories = []
      }
    },
    async loadList() {
      this.loading = true
      try {
        const res = await axios.get('/materials/search', {
          params: {
            categoryId: this.filters.categoryId || undefined,
            keyword: this.filters.keyword || undefined,
            status: this.filters.status || undefined,
            lowStockOnly: this.filters.lowStockOnly || undefined,
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
        showApiError(this, '获取耗材档案', error)
      } finally {
        this.loading = false
      }
    },
    resetFilters() {
      this.filters = { categoryId: '', keyword: '', status: '', lowStockOnly: false }
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
    rowClassName({ row }) {
      return Number(row && row.alert_gap) > 0 ? 'material-row--warning' : ''
    },
    openDialog(row, readonly = false) {
      this.dialogReadonly = readonly
      this.activeMaterial = row ? Object.assign({}, row) : {}
      this.dialogVisible = true
    }
  }
}
</script>

<style scoped>
.material-page { display:flex; flex-direction:column; gap:14px; }
.hero-card { display:flex; justify-content:space-between; align-items:flex-start; gap:16px; padding:18px; border-radius:18px; background:#fff; box-shadow:0 8px 24px rgba(31,71,136,.08); }
.page-kicker { color:#64748b; font-size:13px; }
.hero-card h2 { margin:6px 0 8px; color:#0f172a; font-size:24px; }
.hero-card p { margin:0; color:#94a3b8; }
.hero-actions { display:flex; gap:10px; flex-wrap:wrap; }
.query-card,.table-card { border-radius:18px; }
.query-row { display:flex; gap:12px; flex-wrap:wrap; align-items:center; }
.query-input { width:260px; }
.query-select { width:160px; }
.pagination-row { display:flex; justify-content:flex-end; padding-top:16px; }
::v-deep .material-row--warning td { background: #fff7ed !important; }
@media (max-width: 768px) {
  .hero-card { flex-direction:column; }
  .query-input,.query-select,.category-tree-select { width:100%; }
  .pagination-row { justify-content:center; }
}
</style>
