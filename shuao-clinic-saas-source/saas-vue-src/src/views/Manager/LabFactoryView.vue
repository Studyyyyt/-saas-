<template>
  <div class="lab-page">
    <div class="hero-card">
      <div>
        <div class="page-kicker">义齿加工</div>
        <h2>加工厂档案</h2>
          <p>维护合作加工厂；点击“产品库维护”可直接进入外加工产品价格库和账单模板配置。</p>
      </div>
      <div class="hero-actions">
        <el-button v-if="canManage" type="primary" plain @click="openCreateDialog">新增加工厂</el-button>
        <el-button @click="loadList">刷新</el-button>
      </div>
    </div>

    <el-row :gutter="14" class="summary-row">
      <el-col :xs="24" :sm="8">
        <el-card shadow="never" class="summary-card">
          <div class="summary-label">档案总数</div>
          <div class="summary-value">{{ totalItems }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card shadow="never" class="summary-card">
          <div class="summary-label">合作中</div>
          <div class="summary-value">{{ activeCount }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card shadow="never" class="summary-card">
          <div class="summary-label">已停止合作</div>
          <div class="summary-value">{{ inactiveCount }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="query-card">
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
        <el-table-column prop="name" label="加工厂" min-width="180" />
        <el-table-column prop="contact_name" label="联系人" min-width="120" />
        <el-table-column prop="contact_phone" label="联系电话" min-width="130" />
        <el-table-column prop="cooperation_start_date" label="合作开始" min-width="120">
          <template slot-scope="scope">{{ formatDate(scope.row.cooperation_start_date) || '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="120" align="center">
          <template slot-scope="scope">
            <el-tag size="mini" :type="scope.row.status === '合作中' ? 'success' : 'info'">{{ scope.row.status || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="address" label="地址" min-width="220" show-overflow-tooltip />
        <el-table-column label="操作" fixed="right" width="280">
          <template slot-scope="scope">
            <el-button type="text" size="mini" @click="goDetail(scope.row)">产品库维护</el-button>
            <el-button v-if="canManage" type="text" size="mini" @click="openEditDialog(scope.row)">编辑</el-button>
            <el-button v-if="canManage" type="text" size="mini" style="color:#ef4444" @click="handleDelete(scope.row)">删除</el-button>
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

    <el-dialog :title="editItem.id ? '编辑加工厂' : '新增加工厂'" :visible.sync="dialogVisible" width="520px">
      <el-form :model="editItem" label-width="110px">
        <el-form-item label="加工厂名称"><el-input v-model="editItem.name" /></el-form-item>
        <el-form-item label="联系人"><el-input v-model="editItem.contact_name" /></el-form-item>
        <el-form-item label="联系电话"><el-input v-model="editItem.contact_phone" /></el-form-item>
        <el-form-item label="地址"><el-input v-model="editItem.address" type="textarea" :rows="2" /></el-form-item>
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
      editItem: defaultItem()
    }
  },
  computed: {
    canManage() {
      return canManageLabFactory(normalizeLabRole(this.currentUser && this.currentUser.role))
    },
    activeCount() {
      return this.rows.filter(item => item.status === '合作中').length
    },
    inactiveCount() {
      return this.rows.filter(item => item.status === '已停止合作').length
    }
  },
  mounted() {
    this.loadList()
  },
  methods: {
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
.lab-page { display:flex; flex-direction:column; gap:14px; }
.hero-card { display:flex; justify-content:space-between; align-items:center; gap:16px; padding:18px; border-radius:18px; background:#fff; box-shadow:0 8px 24px rgba(31,71,136,.08); }
.page-kicker { color:#64748b; font-size:13px; }
.hero-card h2 { margin:6px 0 8px; color:#0f172a; font-size:24px; }
.hero-card p { margin:0; color:#94a3b8; }
.hero-actions { display:flex; gap:10px; flex-wrap:wrap; }
.summary-row { margin:0 !important; }
.summary-card { border-radius:18px; }
.summary-label { color:#94a3b8; font-size:13px; }
.summary-value { margin-top:8px; font-size:28px; font-weight:700; color:#0f172a; }
.query-card,.table-card { border-radius:18px; }
.query-row { display:flex; gap:12px; flex-wrap:wrap; }
.query-input { width:280px; }
.query-select { width:180px; }
.pagination-row { display:flex; justify-content:flex-end; padding-top:16px; }
@media (max-width: 768px) {
  .hero-card { flex-direction:column; align-items:flex-start; }
  .query-input,.query-select { width:100%; }
  .pagination-row { justify-content:center; }
}
</style>
