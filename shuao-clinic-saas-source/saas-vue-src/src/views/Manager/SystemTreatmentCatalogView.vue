<template>
  <div class="project-page">
    <div class="hero-card">
      <div>
        <div class="page-kicker">系统设置</div>
        <h2>项目库</h2>
        <p>维护标准治疗项目、分类树和项目对应的标准操作流程。</p>
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
        <el-button plain @click="exportProjects">导出 Excel</el-button>
        <el-button @click="reloadAll">刷新</el-button>
      </div>
    </div>

    <div class="project-layout">
      <el-card shadow="never" class="category-card">
        <div slot="header" class="panel-header">
          <span>项目分类</span>
          <div class="panel-actions">
            <el-button size="mini" type="primary" plain @click="openCategoryDialog()">新增</el-button>
          </div>
        </div>
        <el-tree
          :data="categoryTree"
          node-key="id"
          default-expand-all
          :expand-on-click-node="false"
          :props="{ label: 'name', children: 'children' }"
          @node-click="handleCategoryNodeClick"
        >
          <div slot-scope="{ data }" class="tree-node">
            <div class="tree-node__main">
              <span class="tree-node__name">{{ data.name }}</span>
              <el-tag size="mini" :type="data.status === '启用' ? 'success' : 'info'">{{ data.status }}</el-tag>
            </div>
            <div class="tree-node__actions">
              <el-button size="mini" type="text" @click.stop="openCategoryDialog(data)">编辑</el-button>
              <el-button size="mini" type="text" @click.stop="openCategoryDialog({ parent_id: data.id })">子类</el-button>
              <el-button size="mini" type="text" style="color:#ef4444" @click.stop="deleteCategory(data)">删除</el-button>
            </div>
          </div>
        </el-tree>
        <div class="category-footer">
          <el-button size="mini" @click="clearCategoryFilter">查看全部项目</el-button>
          <span v-if="activeCategoryName" class="category-current">当前：{{ activeCategoryName }}</span>
        </div>
      </el-card>

      <div class="project-main">
        <el-row :gutter="14" class="summary-row">
          <el-col :span="8">
            <el-card shadow="never" class="summary-card">
              <div class="summary-label">项目总数</div>
              <div class="summary-value">{{ total }}</div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card shadow="never" class="summary-card">
              <div class="summary-label">在用项目</div>
              <div class="summary-value">{{ enabledCount }}</div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card shadow="never" class="summary-card">
              <div class="summary-label">含标准流程</div>
              <div class="summary-value">{{ relationConfiguredCount }}</div>
            </el-card>
          </el-col>
        </el-row>

        <el-card shadow="never" class="query-card">
          <div class="query-row">
            <el-input v-model="filters.keyword" class="query-input" clearable placeholder="搜索编码 / 名称 / 分类" @keyup.enter.native="loadProjects" />
            <el-select v-model="filters.status" clearable class="query-select" placeholder="状态">
              <el-option label="在用" value="在用" />
              <el-option label="停用" value="停用" />
            </el-select>
            <el-button type="primary" @click="loadProjects">查询</el-button>
            <el-button @click="resetFilters">重置</el-button>
            <el-button type="success" plain @click="openProjectDialog()">新增项目</el-button>
          </div>
        </el-card>

        <el-card shadow="never" class="table-card">
          <el-table :data="rows" stripe :header-cell-style="{ backgroundColor: '#f8fafc', color: '#475569', fontWeight: '600' }">
            <el-table-column prop="project_code" label="项目编码" width="140" />
            <el-table-column prop="project_name" label="项目名称" min-width="160" />
            <el-table-column prop="category_path" label="分类" min-width="140">
              <template slot-scope="scope">{{ scope.row.category_path || '-' }}</template>
            </el-table-column>
            <el-table-column label="默认价格" width="110">
              <template slot-scope="scope">¥{{ moneyText(scope.row.default_price) }}</template>
            </el-table-column>
            <el-table-column prop="estimated_visit_count" label="预计次数" width="90" />
            <el-table-column prop="estimated_cycle_days" label="预计周期" width="100">
              <template slot-scope="scope">{{ scope.row.estimated_cycle_days || 0 }}天</template>
            </el-table-column>
            <el-table-column label="标准流程" width="120">
              <template slot-scope="scope">{{ relationCount(scope.row) }}项</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="90">
              <template slot-scope="scope">
                <el-tag size="mini" :type="scope.row.status === '在用' ? 'success' : 'info'">{{ scope.row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="sort_order" label="排序" width="80" />
            <el-table-column label="操作" fixed="right" width="180">
              <template slot-scope="scope">
                <el-button size="mini" type="primary" plain @click="openProjectDialog(scope.row)">编辑</el-button>
                <el-button size="mini" type="danger" plain @click="deleteProject(scope.row)">删除</el-button>
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
      </div>
    </div>

    <el-dialog :title="projectDialogTitle" :visible.sync="projectDialogVisible" width="880px" append-to-body>
      <el-form :model="editItem" label-width="110px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="项目编码">
              <el-input v-model="editItem.project_code" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="项目名称">
              <el-input v-model="editItem.project_name" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="项目分类">
              <CategoryTreeSelect v-model="editItem.category_id" :options="enabledCategoryTree" title="项目分类" placeholder="可选" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="默认价格">
              <el-input-number v-model="editItem.default_price" :min="0" :precision="2" :step="1" controls-position="right" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="预计治疗次数">
              <el-input-number v-model="editItem.estimated_visit_count" :min="1" controls-position="right" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="预计周期天数">
              <el-input-number v-model="editItem.estimated_cycle_days" :min="0" controls-position="right" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="状态">
              <el-select v-model="editItem.status" style="width:100%">
                <el-option label="在用" value="在用" />
                <el-option label="停用" value="停用" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="排序">
              <el-input-number v-model="editItem.sort_order" :min="0" controls-position="right" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="16">
            <el-form-item label="备注">
              <el-input v-model="editItem.remark" />
            </el-form-item>
          </el-col>
        </el-row>

        <div class="relation-panel">
          <div class="relation-panel__head">
            <div>
              <div class="relation-panel__title">标准操作流程</div>
              <div class="relation-panel__tip">按展示顺序配置；支持设置是否必经与业绩权重。权重为 0 表示该操作不计医生业绩。</div>
            </div>
            <div class="relation-panel__actions">
              <el-select v-model="relationPickerId" filterable clearable placeholder="添加操作" style="width:240px">
                <el-option v-for="item in operationOptions" :key="item.id" :label="relationOptionLabel(item)" :value="item.id" />
              </el-select>
              <el-button type="primary" plain @click="appendRelationByPicker" :disabled="!relationPickerId">添加</el-button>
            </div>
          </div>
          <el-table :data="editItem.operation_relations" size="mini" border>
            <el-table-column type="index" label="#" width="52" />
            <el-table-column prop="operation_name" label="操作名称" min-width="160" />
            <el-table-column prop="operation_category" label="操作大类" width="120" />
            <el-table-column label="触发外加工" width="110">
              <template slot-scope="scope">{{ scope.row.need_lab_processing === 1 ? '是' : '否' }}</template>
            </el-table-column>
            <el-table-column label="是否必经" width="110">
              <template slot-scope="scope">
                <el-switch v-model="scope.row.is_required" :active-value="1" :inactive-value="0" />
              </template>
            </el-table-column>
            <el-table-column label="业绩权重" width="130">
              <template slot-scope="scope">
                <el-input-number v-model="scope.row.performance_weight" :min="0" :precision="2" :step="0.1" controls-position="right" style="width:100%" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180">
              <template slot-scope="scope">
                <el-button size="mini" type="text" @click="moveRelation(scope.$index, -1)">上移</el-button>
                <el-button size="mini" type="text" @click="moveRelation(scope.$index, 1)">下移</el-button>
                <el-button size="mini" type="text" style="color:#ef4444" @click="removeRelation(scope.$index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!editItem.operation_relations.length" description="当前项目尚未配置标准操作"></el-empty>
        </div>
      </el-form>
      <span slot="footer">
        <el-button @click="projectDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveProject">保存</el-button>
      </span>
    </el-dialog>

    <el-dialog :title="categoryForm.id ? '编辑分类' : '新增分类'" :visible.sync="categoryDialogVisible" width="460px" append-to-body>
      <el-form :model="categoryForm" label-width="100px">
        <el-form-item label="分类名称">
          <el-input v-model="categoryForm.name" />
        </el-form-item>
        <el-form-item label="父分类">
          <el-select v-model="categoryForm.parent_id" style="width:100%">
            <el-option label="一级分类" :value="0" />
            <el-option v-for="item in rootCategoryOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="categoryForm.sort_order" :min="0" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="categoryForm.status" style="width:100%">
            <el-option label="启用" value="启用" />
            <el-option label="停用" value="停用" />
          </el-select>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="categoryDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveCategory">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios'
import * as XLSX from 'xlsx'
import CategoryTreeSelect from '@/components/CategoryTreeSelect.vue'
import { getAdminSession } from '@/utils/adminSession'

function defaultProject(currentUser = {}) {
  return {
    id: null,
    project_code: '',
    project_name: '',
    category_id: '',
    category_path: '',
    default_price: 0,
    estimated_visit_count: 1,
    estimated_cycle_days: 0,
    status: '在用',
    sort_order: 0,
    remark: '',
    created_by: currentUser.id || null,
    created_by_name: currentUser.name || '',
    updated_by: currentUser.id || null,
    updated_by_name: currentUser.name || '',
    operation_relations: []
  }
}

function defaultCategory(currentUser = {}) {
  return {
    id: null,
    name: '',
    parent_id: 0,
    sort_order: 0,
    status: '启用',
    created_by: currentUser.id || null,
    created_by_name: currentUser.name || '',
    updated_by: currentUser.id || null,
    updated_by_name: currentUser.name || ''
  }
}

export default {
  name: 'SystemTreatmentCatalogView',
  components: { CategoryTreeSelect },
  data() {
    return {
      currentUser: getAdminSession() || {},
      categoryTree: [],
      activeCategoryId: '',
      activeCategoryName: '',
      filters: {
        keyword: '',
        status: '',
        page: 1,
        size: 10
      },
      rows: [],
      total: 0,
      operationOptions: [],
      relationPickerId: '',
      projectDialogVisible: false,
      categoryDialogVisible: false,
      saving: false,
      editItem: defaultProject(getAdminSession() || {}),
      categoryForm: defaultCategory(getAdminSession() || {})
    }
  },
  computed: {
    projectDialogTitle() {
      return this.editItem && this.editItem.id ? '编辑项目' : '新增项目'
    },
    enabledCount() {
      return this.rows.filter(item => item.status === '在用').length
    },
    relationConfiguredCount() {
      return this.rows.filter(item => this.relationCount(item) > 0).length
    },
    rootCategoryOptions() {
      return (this.categoryTree || []).map(item => ({ id: item.id, name: item.name }))
    },
    enabledCategoryTree() {
      const cloneNode = node => ({
        ...node,
        children: Array.isArray(node.children) ? node.children.filter(child => child.status === '启用').map(cloneNode) : []
      })
      return (this.categoryTree || []).filter(item => item.status === '启用').map(cloneNode)
    }
  },
  mounted() {
    this.reloadAll()
  },
  methods: {
    moneyText(value) {
      const amount = Number(value || 0)
      return Number.isFinite(amount) ? amount.toFixed(2) : '0.00'
    },
    relationCount(row) {
      return Array.isArray(row && row.operation_relations) ? row.operation_relations.length : Number(row && row.operation_count || 0)
    },
    relationOptionLabel(item) {
      const category = item && item.operation_category ? ` / ${item.operation_category}` : ''
      const lab = item && item.need_lab_processing === 1 ? ' / 外加工' : ''
      return `${item.operation_name || '未命名操作'}${category}${lab}`
    },
    async reloadAll() {
      await Promise.all([this.loadCategories(), this.loadOperations()])
      await this.loadProjects()
    },
    async loadCategories() {
      const res = await axios.get('/treatment-project-categories/tree', { params: { includeDisabled: true } })
      this.categoryTree = Array.isArray(res.data.data) ? res.data.data : []
    },
    async loadOperations() {
      const res = await axios.get('/treatment-operations/selectEnabled')
      this.operationOptions = Array.isArray(res.data.data) ? res.data.data : []
    },
    async loadProjects() {
      const res = await axios.get('/treatment-projects/search', {
        params: {
          keyword: this.filters.keyword || undefined,
          categoryId: this.activeCategoryId || undefined,
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
      this.loadProjects()
    },
    handleCategoryNodeClick(node) {
      this.activeCategoryId = node && node.id ? node.id : ''
      this.activeCategoryName = node && node.name ? node.name : ''
      this.filters.page = 1
      this.loadProjects()
    },
    clearCategoryFilter() {
      this.activeCategoryId = ''
      this.activeCategoryName = ''
      this.filters.page = 1
      this.loadProjects()
    },
    resetFilters() {
      this.filters.keyword = ''
      this.filters.status = ''
      this.filters.page = 1
      this.clearCategoryFilter()
    },
    async openProjectDialog(row) {
      this.currentUser = getAdminSession() || {}
      this.relationPickerId = ''
      if (row && row.id) {
        const res = await axios.get('/treatment-projects/selectById', { params: { id: row.id } })
        this.editItem = Object.assign(defaultProject(this.currentUser), res.data.data || {})
      } else {
        this.editItem = defaultProject(this.currentUser)
      }
      this.editItem.operation_relations = Array.isArray(this.editItem.operation_relations)
        ? this.editItem.operation_relations.map((item, index) => ({ ...item, operation_order: index + 1, is_required: item.is_required === 0 ? 0 : 1, performance_weight: item.performance_weight === undefined || item.performance_weight === null ? 1 : Number(item.performance_weight) }))
        : []
      this.projectDialogVisible = true
    },
    appendRelationByPicker() {
      const selected = (this.operationOptions || []).find(item => String(item.id) === String(this.relationPickerId || ''))
      if (!selected) return
      const exists = (this.editItem.operation_relations || []).some(item => String(item.operation_id) === String(selected.id))
      if (exists) {
        this.$message.warning('该操作已添加')
        return
      }
      if (!Array.isArray(this.editItem.operation_relations)) {
        this.$set(this.editItem, 'operation_relations', [])
      }
      this.editItem.operation_relations.push({
        operation_id: selected.id,
        operation_name: selected.operation_name,
        operation_code: selected.operation_code,
        operation_category: selected.operation_category,
        need_lab_processing: selected.need_lab_processing,
        default_processing_days: selected.default_processing_days,
        operation_order: this.editItem.operation_relations.length + 1,
        is_required: 1,
        performance_weight: 1
      })
      this.relationPickerId = ''
    },
    moveRelation(index, offset) {
      const list = this.editItem.operation_relations || []
      const targetIndex = index + offset
      if (targetIndex < 0 || targetIndex >= list.length) return
      const current = list[index]
      this.$set(list, index, list[targetIndex])
      this.$set(list, targetIndex, current)
      list.forEach((item, itemIndex) => {
        item.operation_order = itemIndex + 1
      })
    },
    removeRelation(index) {
      const list = this.editItem.operation_relations || []
      list.splice(index, 1)
      list.forEach((item, itemIndex) => {
        item.operation_order = itemIndex + 1
      })
    },
    async saveProject() {
      if (!String(this.editItem.project_code || '').trim()) {
        this.$message.warning('项目编码不能为空')
        return
      }
      if (!String(this.editItem.project_name || '').trim()) {
        this.$message.warning('项目名称不能为空')
        return
      }
      this.saving = true
      const payload = Object.assign({}, this.editItem, {
        project_code: String(this.editItem.project_code || '').trim(),
        project_name: String(this.editItem.project_name || '').trim(),
        category_id: this.editItem.category_id || null,
        default_price: Number(this.editItem.default_price || 0),
        estimated_visit_count: Number(this.editItem.estimated_visit_count || 1),
        estimated_cycle_days: Number(this.editItem.estimated_cycle_days || 0),
        sort_order: Number(this.editItem.sort_order || 0),
        remark: String(this.editItem.remark || '').trim(),
        created_by: this.editItem.id ? this.editItem.created_by : (this.currentUser.id || null),
        created_by_name: this.editItem.id ? this.editItem.created_by_name : (this.currentUser.name || ''),
        updated_by: this.currentUser.id || null,
        updated_by_name: this.currentUser.name || '',
        operation_relations: (this.editItem.operation_relations || []).map((item, index) => ({
          operation_id: item.operation_id,
          operation_order: index + 1,
          is_required: item.is_required === 0 ? 0 : 1,
          performance_weight: Number(item.performance_weight || 0)
        }))
      })
      const request = payload.id ? axios.put('/treatment-projects/edit', payload) : axios.post('/treatment-projects/add', payload)
      try {
        const res = await request
        if (res.data.code === '200') {
          this.$message.success(payload.id ? '项目更新成功' : '项目创建成功')
          this.projectDialogVisible = false
          this.loadProjects()
        } else {
          this.$message.error(res.data.msg || '保存失败')
        }
      } catch (error) {
        this.$message.error('保存失败')
      } finally {
        this.saving = false
      }
    },
    deleteProject(row) {
      this.$confirm(`确认删除项目“${row.project_name}”吗？`, '提示', { type: 'warning' }).then(async () => {
        const res = await axios.delete(`/treatment-projects/delete/${row.id}`)
        if (res.data.code === '200') {
          this.$message.success('删除成功')
          this.loadProjects()
        } else {
          this.$message.error(res.data.msg || '删除失败')
        }
      }).catch(() => {})
    },
    openCategoryDialog(source) {
      this.currentUser = getAdminSession() || {}
      const row = source || {}
      this.categoryForm = Object.assign(defaultCategory(this.currentUser), row, {
        parent_id: row.parent_id == null ? 0 : Number(row.parent_id)
      })
      this.categoryDialogVisible = true
    },
    async saveCategory() {
      if (!String(this.categoryForm.name || '').trim()) {
        this.$message.warning('分类名称不能为空')
        return
      }
      const payload = Object.assign({}, this.categoryForm, {
        name: String(this.categoryForm.name || '').trim(),
        parent_id: Number(this.categoryForm.parent_id || 0),
        sort_order: Number(this.categoryForm.sort_order || 0),
        created_by: this.categoryForm.id ? this.categoryForm.created_by : (this.currentUser.id || null),
        created_by_name: this.categoryForm.id ? this.categoryForm.created_by_name : (this.currentUser.name || ''),
        updated_by: this.currentUser.id || null,
        updated_by_name: this.currentUser.name || ''
      })
      const request = payload.id
        ? axios.put('/treatment-project-categories/edit', payload)
        : axios.post('/treatment-project-categories/add', payload)
      try {
        const res = await request
        if (res.data.code === '200') {
          this.$message.success(payload.id ? '分类更新成功' : '分类创建成功')
          this.categoryDialogVisible = false
          this.loadCategories()
        } else {
          this.$message.error(res.data.msg || '保存失败')
        }
      } catch (error) {
        this.$message.error('保存失败')
      }
    },
    deleteCategory(row) {
      this.$confirm(`确认删除分类“${row.name}”吗？`, '提示', { type: 'warning' }).then(async () => {
        const res = await axios.delete(`/treatment-project-categories/delete/${row.id}`)
        if (res.data.code === '200') {
          this.$message.success('删除成功')
          this.loadCategories()
          if (String(this.activeCategoryId) === String(row.id)) {
            this.clearCategoryFilter()
          }
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
            project_code: item.project_code || item.项目编码 || '',
            project_name: item.project_name || item.项目名称 || '',
            default_price: Number(item.default_price || item.默认价格 || 0),
            estimated_visit_count: Number(item.estimated_visit_count || item.预计治疗次数 || 1),
            estimated_cycle_days: Number(item.estimated_cycle_days || item.预计周期天数 || 0),
            status: item.status || item.状态 || '在用',
            sort_order: Number(item.sort_order || item.排序 || 0),
            remark: item.remark || item.备注 || '',
            created_by: this.currentUser.id || null,
            created_by_name: this.currentUser.name || '',
            updated_by: this.currentUser.id || null,
            updated_by_name: this.currentUser.name || ''
          })).filter(item => item.project_code && item.project_name)
          if (!payload.length) {
            this.$message.warning('Excel 中没有可导入的项目数据')
            return
          }
          const res = await axios.post('/treatment-projects/importBatch', payload)
          if (res.data.code === '200') {
            this.$message.success(res.data.data || '导入成功')
            this.loadProjects()
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
    exportProjects() {
      const headers = ['项目编码', '项目名称', '分类路径', '默认价格', '预计治疗次数', '预计周期天数', '状态', '排序', '备注']
      const rows = (this.rows || []).map(item => ([
        item.project_code || '',
        item.project_name || '',
        item.category_path || '',
        this.moneyText(item.default_price),
        item.estimated_visit_count || 1,
        item.estimated_cycle_days || 0,
        item.status || '',
        item.sort_order || 0,
        item.remark || ''
      ]))
      const worksheet = XLSX.utils.aoa_to_sheet([headers, ...rows])
      const workbook = XLSX.utils.book_new()
      XLSX.utils.book_append_sheet(workbook, worksheet, '项目库')
      XLSX.writeFile(workbook, 'treatment-projects.xlsx')
    }
  }
}
</script>

<style scoped>
.project-page { display:flex; flex-direction:column; gap:14px; }
.hero-card { display:flex; justify-content:space-between; align-items:center; gap:16px; padding:18px; border-radius:18px; background:#fff; box-shadow:0 8px 24px rgba(31,71,136,.08); }
.page-kicker { color:#64748b; font-size:13px; }
.hero-card h2 { margin:6px 0 8px; color:#0f172a; font-size:24px; }
.hero-card p { margin:0; color:#94a3b8; }
.hero-actions { display:flex; gap:10px; flex-wrap:wrap; }
.project-layout { display:grid; grid-template-columns:320px minmax(0, 1fr); gap:14px; }
.category-card, .query-card, .table-card, .summary-card { border-radius:18px; }
.panel-header { display:flex; justify-content:space-between; align-items:center; }
.tree-node { width:100%; display:flex; justify-content:space-between; gap:12px; align-items:center; padding:8px 0; }
.tree-node__main { display:flex; gap:10px; align-items:center; }
.tree-node__name { color:#0f172a; font-weight:600; }
.tree-node__actions { display:flex; gap:8px; }
.category-footer { display:flex; justify-content:space-between; align-items:center; gap:10px; padding-top:12px; }
.category-current { color:#64748b; font-size:12px; }
.project-main { display:flex; flex-direction:column; gap:14px; min-width:0; }
.summary-row { margin:0 !important; }
.summary-label { color:#94a3b8; font-size:13px; }
.summary-value { margin-top:8px; font-size:28px; font-weight:700; color:#0f172a; }
.query-row { display:flex; flex-wrap:wrap; gap:10px; align-items:center; }
.query-input { width:280px; }
.query-select { width:160px; }
.pagination-row { display:flex; justify-content:flex-end; padding-top:16px; }
.relation-panel { margin-top:8px; padding:14px; border-radius:16px; background:#f8fafc; border:1px solid #e2e8f0; }
.relation-panel__head { display:flex; justify-content:space-between; align-items:center; gap:16px; margin-bottom:12px; }
.relation-panel__title { color:#0f172a; font-size:16px; font-weight:700; }
.relation-panel__tip { color:#64748b; font-size:12px; margin-top:4px; }
.relation-panel__actions { display:flex; gap:10px; align-items:center; flex-wrap:wrap; }
@media (max-width: 1200px) {
  .project-layout { grid-template-columns:1fr; }
}
</style>
