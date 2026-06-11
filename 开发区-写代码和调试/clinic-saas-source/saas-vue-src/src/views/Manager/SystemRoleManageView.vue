<template>
  <div class="system-page role-page">
    <div class="hero-card">
      <div>
        <div class="page-kicker">系统设置</div>
        <h2>角色管理</h2>
        <p>维护系统角色定义，支持增删改查。删除前需确保该角色下无账号使用。</p>
      </div>
      <el-button type="primary" plain @click="showAddDialog">新增角色</el-button>
    </div>

    <el-card shadow="never" class="table-card">
      <el-table :data="roles" stripe :header-cell-style="{ backgroundColor: '#f8fafc', color: '#475569', fontWeight: '600' }">
        <el-table-column prop="id" label="ID" width="70" align="center"></el-table-column>
        <el-table-column prop="code" label="角色码" width="120">
          <template slot-scope="scope">
            <code class="role-code">{{ scope.row.code }}</code>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="角色名称" width="120"></el-table-column>
        <el-table-column prop="description" label="描述" min-width="200">
          <template slot-scope="scope">
            <span class="text-muted">{{ scope.row.description || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" align="center"></el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template slot-scope="scope">
            <el-tag size="mini" :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" type="primary" plain @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="mini" type="danger" plain @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!roles.length" description="暂无角色数据"></el-empty>
    </el-card>

    <el-dialog
      :title="isEditing ? '编辑角色' : '新增角色'"
      :visible.sync="dialogVisible"
      width="520px"
      :modal-append-to-body="false"
      :append-to-body="true"
      :close-on-click-modal="false"
      custom-class="role-dialog"
    >
      <el-form :model="editItem" label-width="90px">
        <el-form-item label="角色码">
          <el-input v-model="editItem.code" placeholder="英文码，如 receptionist" :disabled="isEditing"></el-input>
          <div class="form-tip">以字母开头，只能包含小写字母、数字和下划线</div>
        </el-form-item>
        <el-form-item label="角色名称">
          <el-input v-model="editItem.name" placeholder="如 前台"></el-input>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="editItem.description" type="textarea" :rows="2" placeholder="可选"></el-input>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="editItem.sortOrder" :min="0" :max="999" style="width:120px"></el-input-number>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="editItem.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用"></el-switch>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRole">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios'
import { showApiError } from '@/utils/errorMessage'

const defaultRole = () => ({
  code: '',
  name: '',
  description: '',
  sortOrder: 0,
  status: 1
})

export default {
  name: 'SystemRoleManageView',
  data() {
    return {
      roles: [],
      dialogVisible: false,
      isEditing: false,
      editItem: defaultRole()
    }
  },
  mounted() {
    this.fetchRoles()
  },
  methods: {
    fetchRoles() {
      axios.get('/roles').then(response => {
        if (response.data.code === '200') {
          this.roles = response.data.data || []
        } else {
          this.$message.error(response.data.msg || '获取角色列表失败')
        }
      }).catch(error => {
        showApiError(this, '获取角色列表', error)
      })
    },
    showAddDialog() {
      this.isEditing = false
      this.editItem = defaultRole()
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.isEditing = true
      this.editItem = { ...row }
      this.dialogVisible = true
    },
    saveRole() {
      const payload = { ...this.editItem }
      const request = this.isEditing
        ? axios.put(`/roles/${this.editItem.id}`, payload)
        : axios.post('/roles', payload)
      request.then(response => {
        if (response.data.code === '200') {
          this.$message.success(this.isEditing ? '编辑成功' : '新增成功')
          this.dialogVisible = false
          this.fetchRoles()
        } else {
          this.$message.error(response.data.msg || '保存失败')
        }
      }).catch(() => {
        this.$message.error('保存失败')
      })
    },
    handleDelete(row) {
      this.$confirm(`确认删除角色「${row.name}（${row.code}）」？`, '提示', { type: 'warning' }).then(() => {
        axios.delete(`/roles/${row.id}`).then(response => {
          if (response.data.code === '200') {
            this.$message.success('删除成功')
            this.fetchRoles()
          } else {
            this.$message.error(response.data.msg || '删除失败')
          }
        }).catch(() => {
          this.$message.error('删除失败')
        })
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.system-page { display:flex; flex-direction:column; gap:14px; }
.dialog-footer { text-align: right; }

.role-code {
  display: inline-block;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 13px;
  font-weight: 600;
  color: #0f172a;
  background: #f1f5f9;
  padding: 2px 8px;
  border-radius: 6px;
}

.form-tip {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 4px;
}

.text-muted {
  color: #94a3b8;
  font-size: 13px;
}
</style>
