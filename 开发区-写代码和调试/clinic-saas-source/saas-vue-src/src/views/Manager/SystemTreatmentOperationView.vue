<template>
  <div class="operation-page">
    <div class="hero-card">
      <div>
        <div class="page-kicker">系统设置</div>
        <h2>操作字典</h2>
        <p>维护临床操作名称、外加工触发标记和默认加工天数。</p>
      </div>
      <div class="hero-actions">
        <el-upload
          action=""
          :before-upload="handleImportBeforeUpload"
          :show-file-list="false"
          accept=".xlsx,.xls"
        >
          <el-button type="primary" plain>导入 Excel</el-button>
        </el-upload>
        <el-button plain @click="exportOperations">导出 Excel</el-button>
        <el-button @click="loadList">刷新</el-button>
      </div>
    </div>

    <el-row :gutter="14" class="summary-row">
      <el-col :span="8">
        <el-card shadow="never" class="summary-card">
          <div class="summary-label">操作总数</div>
          <div class="summary-value">{{ total }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="summary-card">
          <div class="summary-label">在用操作</div>
          <div class="summary-value">{{ enabledCount }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="summary-card">
          <div class="summary-label">触发外加工</div>
          <div class="summary-value">{{ needLabCount }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="query-card">
      <div class="query-row">
        <el-input v-model="filters.keyword" class="query-input" clearable placeholder="搜索编码 / 名称 / 大类" @keyup.enter.native="loadList" />
        <el-input v-model="filters.category" class="query-input" clearable placeholder="操作大类" @keyup.enter.native="loadList" />
        <el-select v-model="filters.needLabProcessing" clearable class="query-select" placeholder="外加工">
          <el-option label="触发外加工" :value="1" />
          <el-option label="不触发外加工" :value="0" />
        </el-select>
        <el-select v-model="filters.status" clearable class="query-select" placeholder="状态">
          <el-option label="在用" value="在用" />
          <el-option label="停用" value="停用" />
        </el-select>
        <el-button type="primary" @click="loadList">查询</el-button>
        <el-button @click="resetFilters">重置</el-button>
        <el-button type="success" plain @click="openDialog()">新增操作</el-button>
      </div>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table :data="rows" stripe :header-cell-style="{ backgroundColor: '#f8fafc', color: '#475569', fontWeight: '600' }">
        <el-table-column prop="operation_code" label="操作编码" width="140" />
        <el-table-column prop="operation_name" label="操作名称" min-width="160" />
        <el-table-column prop="operation_category" label="操作大类" width="140">
          <template slot-scope="scope">{{ scope.row.operation_category || '-' }}</template>
        </el-table-column>
        <el-table-column label="触发外加工" width="110">
          <template slot-scope="scope">
            <el-tag size="mini" :type="scope.row.need_lab_processing === 1 ? 'danger' : 'info'">{{ scope.row.need_lab_processing === 1 ? '是' : '否' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="default_processing_days" label="默认加工天数" width="130" />
        <el-table-column prop="status" label="状态" width="90">
          <template slot-scope="scope">
            <el-tag size="mini" :type="scope.row.status === '在用' ? 'success' : 'info'">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort_order" label="排序" width="80" />
        <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
        <el-table-column label="操作" fixed="right" width="160">
          <template slot-scope="scope">
            <el-button size="mini" type="primary" plain @click="openDialog(scope.row)">编辑</el-button>
            <el-button size="mini" type="danger" plain @click="deleteItem(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-row">
        <el-pagination
          background
          layout="total, prev, pager, next"
          :total="total"
          :page-size="filters.size"
          :current-page="filters.page"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="560px" append-to-body>
      <el-form :model="editItem" label-width="110px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="操作编码">
              <el-input v-model="editItem.operation_code" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="操作名称">
              <el-input v-model="editItem.operation_name" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="操作大类">
              <el-input v-model="editItem.operation_category" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="默认加工天数">
              <el-input-number v-model="editItem.default_processing_days" :min="0" controls-position="right" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="触发外加工">
              <el-switch v-model="editItem.need_lab_processing" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="editItem.status" style="width:100%">
                <el-option label="在用" value="在用" />
                <el-option label="停用" value="停用" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="排序">
              <el-input-number v-model="editItem.sort_order" :min="0" controls-position="right" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="备注">
              <el-input v-model="editItem.remark" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveItem">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios'
import * as XLSX from 'xlsx'
import { getAdminSession } from '@/utils/adminSession'

function defaultItem(currentUser = {}) {
  return {
    id: null,
    operation_code: '',
    operation_name: '',
    operation_category: '',
    need_lab_processing: 0,
    default_processing_days: 0,
    status: '在用',
    sort_order: 0,
    remark: '',
    created_by: currentUser.id || null,
    created_by_name: currentUser.name || '',
    updated_by: currentUser.id || null,
    updated_by_name: currentUser.name || ''
  }
}

export default {
  name: 'SystemTreatmentOperationView',
  data() {
    return {
      currentUser: getAdminSession() || {},
      rows: [],
      total: 0,
      saving: false,
      dialogVisible: false,
      editItem: defaultItem(getAdminSession() || {}),
      filters: {
        keyword: '',
        category: '',
        needLabProcessing: '',
        status: '',
        page: 1,
        size: 10
      }
    }
  },
  computed: {
    dialogTitle() {
      return this.editItem && this.editItem.id ? '编辑操作' : '新增操作'
    },
    enabledCount() {
      return this.rows.filter(item => item.status === '在用').length
    },
    needLabCount() {
      return this.rows.filter(item => item.need_lab_processing === 1).length
    }
  },
  mounted() {
    this.loadList()
  },
  methods: {
    async loadList() {
      const res = await axios.get('/treatment-operations/search', {
        params: {
          keyword: this.filters.keyword || undefined,
          category: this.filters.category || undefined,
          needLabProcessing: this.filters.needLabProcessing === '' ? undefined : this.filters.needLabProcessing,
          status: this.filters.status || undefined,
          page: this.filters.page,
          size: this.filters.size
        }
      })
      const data = res.data.data || {}
      this.rows = Array.isArray(data.list) ? data.list : []
      this.total = Number(data.total || 0)
    },
    handlePageChange(page) {
      this.filters.page = page
      this.loadList()
    },
    resetFilters() {
      this.filters.keyword = ''
      this.filters.category = ''
      this.filters.needLabProcessing = ''
      this.filters.status = ''
      this.filters.page = 1
      this.loadList()
    },
    openDialog(row) {
      this.currentUser = getAdminSession() || {}
      this.editItem = Object.assign(defaultItem(this.currentUser), row || {})
      this.dialogVisible = true
    },
    async saveItem() {
      if (!String(this.editItem.operation_code || '').trim()) {
        this.$message.warning('操作编码不能为空')
        return
      }
      if (!String(this.editItem.operation_name || '').trim()) {
        this.$message.warning('操作名称不能为空')
        return
      }
      this.saving = true
      const payload = Object.assign({}, this.editItem, {
        operation_code: String(this.editItem.operation_code || '').trim(),
        operation_name: String(this.editItem.operation_name || '').trim(),
        operation_category: String(this.editItem.operation_category || '').trim(),
        need_lab_processing: this.editItem.need_lab_processing === 1 ? 1 : 0,
        default_processing_days: Number(this.editItem.default_processing_days || 0),
        sort_order: Number(this.editItem.sort_order || 0),
        remark: String(this.editItem.remark || '').trim(),
        created_by: this.editItem.id ? this.editItem.created_by : (this.currentUser.id || null),
        created_by_name: this.editItem.id ? this.editItem.created_by_name : (this.currentUser.name || ''),
        updated_by: this.currentUser.id || null,
        updated_by_name: this.currentUser.name || ''
      })
      const request = payload.id ? axios.put('/treatment-operations/edit', payload) : axios.post('/treatment-operations/add', payload)
      try {
        const res = await request
        if (res.data.code === '200') {
          this.$message.success(payload.id ? '操作更新成功' : '操作创建成功')
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
    deleteItem(row) {
      this.$confirm(`确认删除操作“${row.operation_name}”吗？`, '提示', { type: 'warning' }).then(async () => {
        const res = await axios.delete(`/treatment-operations/delete/${row.id}`)
        if (res.data.code === '200') {
          this.$message.success('删除成功')
          this.loadList()
        } else {
          this.$message.error(res.data.msg || '删除失败')
        }
      }).catch(() => {})
    },
    handleImportBeforeUpload(file) {
      const reader = new FileReader()
      reader.onload = async event => {
        try {
          const workbook = XLSX.read(event.target.result, { type: 'binary' })
          const sheet = workbook.Sheets[workbook.SheetNames[0]]
          const rows = XLSX.utils.sheet_to_json(sheet, { defval: '' })
          const payload = rows.map(item => ({
            operation_code: item.operation_code || item.操作编码 || '',
            operation_name: item.operation_name || item.操作名称 || '',
            operation_category: item.operation_category || item.操作大类 || '',
            need_lab_processing: Number(item.need_lab_processing || item.是否触发外加工 || 0) === 1 ? 1 : 0,
            default_processing_days: Number(item.default_processing_days || item.默认加工天数 || 0),
            status: item.status || item.状态 || '在用',
            sort_order: Number(item.sort_order || item.排序 || 0),
            remark: item.remark || item.备注 || '',
            created_by: this.currentUser.id || null,
            created_by_name: this.currentUser.name || '',
            updated_by: this.currentUser.id || null,
            updated_by_name: this.currentUser.name || ''
          })).filter(item => item.operation_code && item.operation_name)
          if (!payload.length) {
            this.$message.warning('Excel 中没有可导入的操作数据')
            return
          }
          const res = await axios.post('/treatment-operations/importBatch', payload)
          if (res.data.code === '200') {
            this.$message.success(res.data.data || '导入成功')
            this.loadList()
          } else {
            this.$message.error(res.data.msg || '导入失败')
          }
        } catch (error) {
          this.$message.error('导入解析失败')
        }
      }
      reader.readAsBinaryString(file)
      return false
    },
    exportOperations() {
      const headers = ['操作编码', '操作名称', '操作大类', '是否触发外加工', '默认加工天数', '状态', '排序', '备注']
      const rows = (this.rows || []).map(item => ([
        item.operation_code || '',
        item.operation_name || '',
        item.operation_category || '',
        item.need_lab_processing === 1 ? 1 : 0,
        item.default_processing_days || 0,
        item.status || '',
        item.sort_order || 0,
        item.remark || ''
      ]))
      const worksheet = XLSX.utils.aoa_to_sheet([headers, ...rows])
      const workbook = XLSX.utils.book_new()
      XLSX.utils.book_append_sheet(workbook, worksheet, '操作字典')
      XLSX.writeFile(workbook, 'treatment-operations.xlsx')
    }
  }
}
</script>

<style scoped>
.operation-page { display:flex; flex-direction:column; gap:14px; }
.hero-card { display:flex; justify-content:space-between; align-items:center; gap:16px; padding:18px; border-radius:18px; background:#fff; box-shadow:0 8px 24px rgba(31,71,136,.08); }
.page-kicker { color:#64748b; font-size:13px; }
.hero-card h2 { margin:6px 0 8px; color:#0f172a; font-size:24px; }
.hero-card p { margin:0; color:#94a3b8; }
.hero-actions { display:flex; gap:10px; flex-wrap:wrap; }
.summary-row { margin:0 !important; }
.summary-card, .query-card, .table-card { border-radius:18px; }
.summary-label { color:#94a3b8; font-size:13px; }
.summary-value { margin-top:8px; font-size:28px; font-weight:700; color:#0f172a; }
.query-row { display:flex; flex-wrap:wrap; gap:10px; align-items:center; }
.query-input { width:220px; }
.query-select { width:160px; }
.pagination-row { display:flex; justify-content:flex-end; padding-top:16px; }
</style>
