<template>
  <div class="lab-page">
    <!-- 页面头部 -->
    <el-card class="hero-card" shadow="never">
      <div class="hero-head">
        <div>
          <div class="page-kicker">义齿加工</div>
          <h2>加工厂档案</h2>
          <p>维护合作加工厂；点击“产品库维护”可直接进入外加工产品价格库和账单模板配置。</p>
        </div>
        <div class="hero-actions">
          <el-button v-if="canManage" type="primary" icon="el-icon-plus" @click="openCreateDialog">
            新增加工厂
          </el-button>
          <el-button icon="el-icon-refresh" @click="loadList">刷新</el-button>
        </div>
      </div>
    </el-card>

    <!-- 指标卡片 -->
    <div class="summary-grid">
      <div class="summary-card total">
        <div class="summary-icon"><i class="el-icon-office-building"></i></div>
        <div class="summary-body">
          <div class="summary-value">{{ overview.total_count || 0 }}</div>
          <div class="summary-label">档案总数</div>
        </div>
      </div>
      <div class="summary-card active">
        <div class="summary-icon"><i class="el-icon-circle-check"></i></div>
        <div class="summary-body">
          <div class="summary-value">{{ overview.active_count || 0 }}</div>
          <div class="summary-label">合作中</div>
        </div>
      </div>
      <div class="summary-card inactive">
        <div class="summary-icon"><i class="el-icon-circle-close"></i></div>
        <div class="summary-body">
          <div class="summary-value">{{ overview.inactive_count || 0 }}</div>
          <div class="summary-label">已停止合作</div>
        </div>
      </div>
      <div class="summary-card product">
        <div class="summary-icon"><i class="el-icon-goods"></i></div>
        <div class="summary-body">
          <div class="summary-value">{{ overview.total_products || 0 }}</div>
          <div class="summary-label">价格项总数</div>
        </div>
      </div>
      <div class="summary-card template">
        <div class="summary-icon"><i class="el-icon-document-copy"></i></div>
        <div class="summary-body">
          <div class="summary-value">{{ overview.total_templates || 0 }}</div>
          <div class="summary-label">账单模板数</div>
        </div>
      </div>
    </div>

    <!-- 查询筛选 -->
    <el-card class="query-card" shadow="never">
      <div class="query-row">
        <el-input
          v-model="filters.keyword"
          class="query-input"
          clearable
          placeholder="搜索加工厂名称 / 联系人 / 电话"
          @keyup.enter.native="loadList"
        />
        <el-select v-model="filters.status" clearable class="query-select" placeholder="合作状态">
          <el-option v-for="item in LAB_FACTORY_STATUS_OPTIONS" :key="item" :label="item" :value="item" />
        </el-select>
        <el-button type="primary" icon="el-icon-search" @click="loadList">查询</el-button>
        <el-button icon="el-icon-refresh-left" @click="resetFilters">重置</el-button>
      </div>
    </el-card>

    <!-- 表格 -->
    <el-card class="table-card" shadow="never">
      <el-table
        :data="rows"
        stripe
        v-loading="loading"
        size="small"
        :header-cell-style="tableHeaderStyle"
      >
        <el-table-column prop="name" label="加工厂" min-width="180" show-overflow-tooltip />
        <el-table-column prop="contact_name" label="联系人" min-width="100" />
        <el-table-column prop="contact_phone" label="联系电话" min-width="130" />
        <el-table-column prop="cooperation_start_date" label="合作开始" min-width="110">
          <template slot-scope="scope">{{ formatDate(scope.row.cooperation_start_date) || '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template slot-scope="scope">
            <el-tag
              size="mini"
              :type="scope.row.status === '合作中' ? 'success' : 'info'"
              effect="light"
            >
              {{ scope.row.status || '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" fixed="right" width="300">
          <template slot-scope="scope">
            <el-button type="text" size="mini" class="primary-link" @click="goDetail(scope.row)">
              <i class="el-icon-s-operation"></i> 产品库维护
            </el-button>
            <el-button v-if="canManage" type="text" size="mini" @click="openEditDialog(scope.row)">编辑</el-button>
            <el-button
              v-if="canManage"
              type="text"
              size="mini"
              class="danger-link"
              @click="handleDelete(scope.row)"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && !rows.length" description="暂无加工厂档案"></el-empty>

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

    <!-- 新增/编辑弹窗 -->
    <el-dialog :title="editItem.id ? '编辑加工厂' : '新增加工厂'" :visible.sync="dialogVisible" width="520px" append-to-body>
      <el-form :model="editItem" label-width="110px">
        <el-form-item label="加工厂名称"><el-input v-model="editItem.name" maxlength="100" /></el-form-item>
        <el-form-item label="联系人"><el-input v-model="editItem.contact_name" maxlength="50" /></el-form-item>
        <el-form-item label="联系电话"><el-input v-model="editItem.contact_phone" maxlength="30" /></el-form-item>
        <el-form-item label="地址"><el-input v-model="editItem.address" type="textarea" :rows="2" maxlength="200" /></el-form-item>
        <el-form-item label="合作开始日期">
          <el-date-picker v-model="editItem.cooperation_start_date" type="date" value-format="yyyy-MM-dd" style="width:100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="editItem.status" style="width:100%">
            <el-option v-for="item in LAB_FACTORY_STATUS_OPTIONS" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveFactory">保存</el-button>
      </span>
    </el-dialog>

  </div>
</template>

<script>
import axios from 'axios'
import { getAdminSession } from '@/utils/adminSession'
import { LAB_FACTORY_STATUS_OPTIONS, canManageLabFactory, normalizeLabRole } from '@/utils/labConstants'
import { showApiError } from '@/utils/errorMessage'
function defaultItem() {
  return {
    id: null,
    name: '',
    contact_name: '',
    contact_phone: '',
    address: '',
    cooperation_start_date: '',
    status: '合作中'
  }
}

export default {
  name: 'LabFactoryView',
  components: {},
  data() {
    return {
      LAB_FACTORY_STATUS_OPTIONS,
      currentUser: getAdminSession() || {},
      loading: false,
      saving: false,
      rows: [],
      totalItems: 0,
      currentPage: 1,
      pageSize: 10,
      filters: {
        keyword: '',
        status: ''
      },
      dialogVisible: false,
      editItem: defaultItem(),
      overview: {}
    }
  },
  computed: {
    canManage() {
      return canManageLabFactory(normalizeLabRole(this.currentUser && this.currentUser.role))
    }
  },
  mounted() {
    this.loadList()
    this.loadOverview()
  },
  methods: {
    tableHeaderStyle() {
      return { backgroundColor: '#f8fafc', color: '#475569', fontWeight: '600' }
    },
    formatDate(value) {
      return value ? String(value).slice(0, 10) : ''
    },
    async loadList() {
      this.loading = true
      try {
        const res = await axios.get('/lab-factories/search', {
          params: {
            keyword: this.filters.keyword || undefined,
            status: this.filters.status || undefined,
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
        showApiError(this, '获取加工厂档案', error)
      } finally {
        this.loading = false
      }
    },
    async loadOverview() {
      try {
        const res = await axios.get('/lab-factories/dashboard/overview')
        this.overview = (res.data && res.data.data) || {}
      } catch (error) {
        this.overview = {}
      }
    },
    resetFilters() {
      this.filters = { keyword: '', status: '' }
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
    openCreateDialog() {
      this.editItem = defaultItem()
      this.dialogVisible = true
    },
    openEditDialog(row) {
      this.editItem = Object.assign(defaultItem(), row, {
        cooperation_start_date: this.formatDate(row.cooperation_start_date)
      })
      this.dialogVisible = true
    },
    async saveFactory() {
      if (!String(this.editItem.name || '').trim()) {
        this.$message.warning('加工厂名称不能为空')
        return
      }
      this.saving = true
      const request = this.editItem.id
        ? axios.put('/lab-factories/edit', this.editItem)
        : axios.post('/lab-factories/add', this.editItem)
      try {
        const res = await request
        if (res.data.code === '200') {
          this.$message.success(this.editItem.id ? '更新成功' : '新增成功')
          this.dialogVisible = false
          this.loadList()
          this.loadOverview()
        } else {
          this.$message.error(res.data.msg || '保存失败')
        }
      } catch (error) {
        this.$message.error('保存失败')
      } finally {
        this.saving = false
      }
    },
    handleDelete(row) {
      this.$confirm(`确认删除加工厂“${row.name}”吗？`, '提示', { type: 'warning' }).then(async () => {
        const res = await axios.delete(`/lab-factories/delete/${row.id}`)
        if (res.data.code === '200') {
          this.$message.success('删除成功')
          this.loadList()
          this.loadOverview()
        } else {
          this.$message.error(res.data.msg || '删除失败')
        }
      }).catch(() => {})
    },
    goDetail(row) {
      this.$router.push({ path: `/lab-factories/${row.id}` }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.lab-page {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.hero-card {
  border-radius: 20px;
}

.hero-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.hero-actions {
  display: flex;
  gap: 10px;
}

.page-kicker {
  color: #2563eb;
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.hero-head h2 {
  margin: 8px 0 6px;
  font-size: 28px;
  color: #0f172a;
}

.hero-head p {
  margin: 0;
  color: #64748b;
}

.query-card,
.table-card {
  border-radius: 20px;
}

.query-row {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}

.query-input {
  width: 280px;
}

.query-select {
  width: 180px;
}

/* 指标卡片 */
.summary-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 12px;
}

.summary-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px;
  border-radius: 18px;
  background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
  border: 1px solid rgba(148, 163, 184, 0.18);
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.summary-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.06);
}

.summary-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.summary-card.total .summary-icon { background: #eff6ff; color: #2563eb; }
.summary-card.active .summary-icon { background: #f0fdf4; color: #16a34a; }
.summary-card.inactive .summary-icon { background: #f8fafc; color: #64748b; }
.summary-card.product .summary-icon { background: #fff7ed; color: #ea580c; }
.summary-card.template .summary-icon { background: #f5f3ff; color: #7c3aed; }

.summary-body {
  min-width: 0;
}

.summary-value {
  font-size: 22px;
  font-weight: 700;
  color: #0f172a;
  line-height: 1.2;
}

.summary-label {
  font-size: 13px;
  color: #64748b;
  margin-top: 4px;
}

.pagination-row {
  display: flex;
  justify-content: flex-end;
  padding-top: 16px;
}

.primary-link {
  font-weight: 600;
  color: #2563eb;
}

.danger-link {
  color: #ef4444;
}

@media (max-width: 768px) {
  .summary-grid {
    grid-template-columns: 1fr;
  }

  .hero-head {
    flex-direction: column;
    align-items: flex-start;
  }

  .query-input,
  .query-select {
    width: 100%;
  }

  .pagination-row {
    justify-content: center;
  }
}
</style>
