<template>
  <div class="system-clinic-manage apple-design-scope">
    <div class="page-header">
      <h2 class="page-title">诊所管理</h2>
      <p class="page-subtitle">管理所有门诊诊所，包括创建、编辑、禁用和删除操作</p>
    </div>

    <div class="action-bar">
      <el-input
        v-model="searchName"
        placeholder="搜索诊所名称"
        prefix-icon="el-icon-search"
        clearable
        style="width: 280px"
        @input="handleSearch"
      />
      <el-button type="primary" icon="el-icon-plus" @click="openAddDialog">新增诊所</el-button>
    </div>

    <div class="content-card">
      <el-table :data="clinicList" v-loading="loading" stripe>
        <el-table-column prop="id" label="诊所ID" width="140" />
        <el-table-column prop="name" label="诊所名称" min-width="160" />
        <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip />
        <el-table-column prop="contactPhone" label="联系电话" width="130" />
        <el-table-column prop="status" label="状态" width="90">
          <template slot-scope="scope">
            <el-switch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              @change="val => toggleStatus(scope.row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="160">
          <template slot-scope="scope">
            {{ formatDate(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="openEditDialog(scope.row)">编辑</el-button>
            <el-button type="text" size="small" style="color: #e74c3c" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 新增/编辑诊所弹窗 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="480px"
      :close-on-click-modal="false"
      :append-to-body="true"
      :modal-append-to-body="true"
    >
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="诊所ID" prop="id">
          <el-input
            v-model="form.id"
            placeholder="支持自定义拼音，如 yikouya，留空则自动生成"
            :disabled="isEdit"
          />
        </el-form-item>
        <el-form-item label="诊所名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入诊所名称" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.address" placeholder="请输入地址" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios'
import { getAdminSession, saveAdminSession } from '@/utils/adminSession'

export default {
  name: 'SystemClinicManageView',
  data() {
    return {
      loading: false,
      clinicList: [],
      searchName: '',
      dialogVisible: false,
      isEdit: false,
      dialogTitle: '新增诊所',
      submitting: false,
      form: {
        id: '',
        name: '',
        address: '',
        contactPhone: ''
      },
      formRules: {
        name: [{ required: true, message: '请输入诊所名称', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.loadClinics()
  },
  methods: {
    loadClinics() {
      this.loading = true
      axios.get('/clinics/manage', { params: { name: this.searchName } })
        .then(res => {
          if (res.data.code === '200') {
            this.clinicList = res.data.data || []
          }
        })
        .catch(err => {
          console.error('加载诊所列表失败:', err)
          this.$message.error('加载诊所列表失败')
        })
        .finally(() => {
          this.loading = false
        })
    },
    handleSearch() {
      this.loadClinics()
    },
    openAddDialog() {
      this.isEdit = false
      this.dialogTitle = '新增诊所'
      this.form = { id: '', name: '', address: '', contactPhone: '' }
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.formRef && this.$refs.formRef.clearValidate()
      })
    },
    openEditDialog(row) {
      this.isEdit = true
      this.dialogTitle = '编辑诊所'
      this.form = {
        id: row.id,
        name: row.name,
        address: row.address || '',
        contactPhone: row.contactPhone || ''
      }
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.formRef && this.$refs.formRef.clearValidate()
      })
    },
    handleSubmit() {
      this.$refs.formRef.validate(valid => {
        if (!valid) return
        this.submitting = true
        const payload = { ...this.form }
        // 创建诊所时传递当前用户ID，后端自动绑定用户到诊所
        if (!this.isEdit) {
          const session = getAdminSession()
          if (session && session.id) {
            payload.creatorUserId = session.id
          }
        }
        const api = this.isEdit
          ? axios.put(`/clinics/manage/${this.form.id}`, payload)
          : axios.post('/clinics/manage', payload)
        api.then(res => {
          if (res.data.code === '200') {
            this.$message.success(this.isEdit ? '编辑成功' : '创建成功')
            this.dialogVisible = false
            this.loadClinics()
            // 创建诊所后刷新 session 中的诊所列表，使顶部切换器同步
            if (!this.isEdit) {
              this.refreshUserClinics()
            }
          } else {
            this.$message.error(res.data.msg || '操作失败')
          }
        }).catch(err => {
          console.error('保存诊所失败:', err)
          this.$message.error('保存失败')
        }).finally(() => {
          this.submitting = false
        })
      })
    },
    toggleStatus(row, status) {
      axios.post(`/clinics/manage/${row.id}/toggle`, { status })
        .then(res => {
          if (res.data.code === '200') {
            this.$message.success(status === 1 ? '诊所已启用' : '诊所已禁用')
          } else {
            this.$message.error(res.data.msg || '操作失败')
            row.status = status === 1 ? 0 : 1
          }
        })
        .catch(err => {
          console.error('切换状态失败:', err)
          this.$message.error('操作失败')
          row.status = status === 1 ? 0 : 1
        })
    },
    handleDelete(row) {
      this.$confirm(
        `确定要删除诊所「${row.name}」吗？删除前请确认该诊所下无关联数据。`,
        '删除确认',
        { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' }
      ).then(() => {
        axios.delete(`/clinics/manage/${row.id}`)
          .then(res => {
            if (res.data.code === '200') {
              this.$message.success('删除成功')
              this.loadClinics()
            } else {
              this.$message.error(res.data.msg || '删除失败')
            }
          })
          .catch(err => {
            console.error('删除诊所失败:', err)
            this.$message.error('删除失败')
          })
      }).catch(() => {})
    },
    refreshUserClinics() {
      const session = getAdminSession()
      if (!session || !session.id) return
      axios.get('/clinics/mine', { params: { userId: session.id } })
        .then(res => {
          if (res.data.code === '200') {
            session.clinics = res.data.data || []
            saveAdminSession(session)
          }
        })
        .catch(err => {
          console.error('刷新诊所列表失败:', err)
        })
    },
    formatDate(dateStr) {
      if (!dateStr) return '-'
      const d = new Date(dateStr)
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
    }
  }
}
</script>

<style scoped>
.system-clinic-manage {
  padding: var(--apple-space-xl) 0;
}
.page-header {
  margin-bottom: var(--apple-space-xl);
}
.page-title {
  font-size: 28px;
  font-weight: 600;
  color: var(--apple-text-primary);
  margin: 0 0 8px;
}
.page-subtitle {
  font-size: 14px;
  color: var(--apple-text-secondary);
  margin: 0;
}
.action-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--apple-space-lg);
}
.content-card {
  background: var(--apple-surface);
  border-radius: var(--apple-radius-lg);
  border: 1px solid var(--apple-border);
  box-shadow: var(--apple-shadow-sm);
  overflow: hidden;
}
</style>
