<template>
  <div class="material-page">
    <div class="hero-card">
      <div>
        <div class="page-kicker">耗材采购</div>
        <h2>耗材分类管理</h2>
        <p>维护两级分类树，供耗材档案和采购录入统一引用。</p>
      </div>
      <div class="hero-actions">
        <el-button v-if="canManage" type="primary" plain @click="openDialog()">新增分类</el-button>
        <el-button @click="loadTree">刷新</el-button>
      </div>
    </div>

    <el-alert
      v-if="!canManage"
      title="当前账号仅支持查看分类树，分类管理权限仅 admin 开放。"
      type="warning"
      :closable="false"
      show-icon
    />

    <el-card shadow="never" class="tree-card">
      <el-tree
        :data="treeData"
        node-key="id"
        default-expand-all
        :expand-on-click-node="false"
        :props="{ label: 'name', children: 'children' }"
      >
        <div slot-scope="{ data }" class="tree-node">
          <div class="node-main">
            <span class="node-name">{{ data.name }}</span>
            <el-tag size="mini" :type="data.status === '启用' ? 'success' : 'info'">{{ data.status || '-' }}</el-tag>
          </div>
          <div class="node-actions" v-if="canManage">
            <el-button size="mini" type="text" @click.stop="openDialog(data)">编辑</el-button>
            <el-button size="mini" type="text" @click.stop="openDialog({ parent_id: data.id || 0 })">新增子类</el-button>
            <el-button size="mini" type="text" style="color:#ef4444" @click.stop="deleteCategory(data)">删除</el-button>
          </div>
        </div>
      </el-tree>
      <el-empty v-if="!treeData.length" description="暂无分类数据"></el-empty>
    </el-card>

    <el-dialog :title="form.id ? '编辑分类' : '新增分类'" :visible.sync="dialogVisible" width="460px">
      <el-form label-width="100px">
        <el-form-item label="分类名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="父分类">
          <el-select v-model="form.parent_id" :disabled="Boolean(form.id && isChildCategory)" style="width:100%">
            <el-option label="一级分类" :value="0" />
            <el-option v-for="item in rootOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort_order" :min="0" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width:100%">
            <el-option v-for="item in MATERIAL_CATEGORY_STATUS_OPTIONS" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveCategory">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios'
import { getAdminSession } from '@/utils/adminSession'
import { MATERIAL_CATEGORY_STATUS_OPTIONS, canManageMaterialCategories, normalizeMaterialRole } from '@/utils/materialConstants'
import { showApiError } from '@/utils/errorMessage'

function defaultForm() {
  return {
    id: null,
    name: '',
    parent_id: 0,
    sort_order: 0,
    status: '启用'
  }
}

export default {
  name: 'MaterialCategoryView',
  data() {
    return {
      MATERIAL_CATEGORY_STATUS_OPTIONS,
      currentUser: getAdminSession() || {},
      treeData: [],
      dialogVisible: false,
      saving: false,
      form: defaultForm(),
      isChildCategory: false
    }
  },
  computed: {
    canManage() {
      return canManageMaterialCategories(normalizeMaterialRole(this.currentUser && this.currentUser.role))
    },
    rootOptions() {
      return this.treeData || []
    }
  },
  mounted() {
    this.loadTree()
  },
  methods: {
    async loadTree() {
      try {
        const res = await axios.get('/material-categories/tree', { params: { includeDisabled: true } })
        this.treeData = Array.isArray(res.data.data) ? res.data.data : []
      } catch (error) {
        this.treeData = []
        showApiError(this, '获取材料分类', error)
      }
    },
    openDialog(source) {
      const row = source || {}
      this.isChildCategory = !!(row.parent_id && Number(row.parent_id) > 0)
      this.form = Object.assign(defaultForm(), row, {
        parent_id: row.parent_id == null ? 0 : Number(row.parent_id)
      })
      this.dialogVisible = true
    },
    async saveCategory() {
      if (!String(this.form.name || '').trim()) {
        this.$message.warning('分类名称不能为空')
        return
      }
      this.saving = true
      const payload = Object.assign({}, this.form, {
        parent_id: Number(this.form.parent_id || 0),
        sort_order: Number(this.form.sort_order || 0)
      })
      const request = payload.id
        ? axios.put('/material-categories/edit', payload)
        : axios.post('/material-categories/add', payload)
      try {
        const res = await request
        if (res.data.code === '200') {
          this.$message.success(payload.id ? '分类更新成功' : '分类创建成功')
          this.dialogVisible = false
          this.loadTree()
        } else {
          this.$message.error(res.data.msg || '保存失败')
        }
      } catch (error) {
        this.$message.error('保存失败')
      } finally {
        this.saving = false
      }
    },
    deleteCategory(row) {
      this.$confirm(`确认删除分类“${row.name}”吗？`, '提示', { type: 'warning' }).then(async () => {
        const res = await axios.delete(`/material-categories/delete/${row.id}`)
        if (res.data.code === '200') {
          this.$message.success('删除成功')
          this.loadTree()
        } else {
          this.$message.error(res.data.msg || '删除失败')
        }
      }).catch(() => {})
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
.tree-card { border-radius:18px; }
.tree-node { width:100%; display:flex; justify-content:space-between; align-items:center; gap:16px; padding:8px 0; }
.node-main { display:flex; align-items:center; gap:10px; }
.node-name { color:#0f172a; font-weight:600; }
.node-actions { display:flex; gap:10px; }
@media (max-width: 768px) {
  .hero-card,.tree-node { flex-direction:column; align-items:flex-start; }
}
</style>
