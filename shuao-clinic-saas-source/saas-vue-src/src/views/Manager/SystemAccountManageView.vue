<template>
  <div class="system-page">
    <div class="hero-card">
      <div>
        <div class="page-kicker">系统设置</div>
        <h2>账号管理</h2>
        <p>维护员工账号、姓名、密码、角色与微信绑定信息。</p>
      </div>
      <el-button type="primary" plain @click="showAddDialog">新增账号</el-button>
    </div>

    <el-card shadow="never" class="table-card">
      <el-table :data="accounts" stripe :header-cell-style="{ backgroundColor: 'aliceblue', color: '#666' }">
        <el-table-column prop="id" label="序号" width="70" align="center"></el-table-column>
        <el-table-column prop="username" label="账号名称"></el-table-column>
        <el-table-column prop="name" label="姓名"></el-table-column>
        <el-table-column prop="roleLabel" label="角色"></el-table-column>
        <el-table-column label="微信绑定" width="220">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.wechat_openid" size="mini" type="success">已绑定</el-tag>
            <el-tag v-else size="mini" type="info">未绑定</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template slot-scope="scope">
            <el-button size="mini" type="primary" plain @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="mini" type="danger" plain @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog :title="isEditing ? '编辑员工账号' : '新增员工账号'" :visible.sync="dialogVisible" width="420px">
      <el-form :model="editItem" label-width="100px">
        <el-form-item label="账号名称"><el-input v-model="editItem.username"></el-input></el-form-item>
        <el-form-item label="姓名"><el-input v-model="editItem.name"></el-input></el-form-item>
        <el-form-item label="密码"><el-input type="password" v-model="editItem.password"></el-input></el-form-item>
        <el-form-item label="角色">
          <el-select v-model="editItem.role" placeholder="请选择角色" style="width:100%">
            <el-option label="管理员" value="admin"></el-option>
            <el-option label="医生" value="doctor"></el-option>
            <el-option label="护士" value="nurse"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="微信OpenID"><el-input v-model="editItem.wechat_openid"></el-input></el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveAccount">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios'
import { showApiError } from '@/utils/errorMessage'

const defaultAccount = () => ({
  username: '',
  password: '',
  role: '',
  name: '',
  wechat_openid: ''
})

export default {
  name: 'SystemAccountManageView',
  data() {
    return {
      accounts: [],
      dialogVisible: false,
      isEditing: false,
      editItem: defaultAccount()
    }
  },
  mounted() {
    this.fetchAccounts()
  },
  methods: {
    formatRole(role) {
      if (role === 'admin') return '管理员'
      if (role === 'doctor') return '医生'
      if (role === 'nurse') return '护士'
      return role || '未设置'
    },
    fetchAccounts() {
      axios.get('/accounts/search', { params: { page: 1, size: 1000 } }).then(response => {
        const data = response.data.data || {}
        this.accounts = (data.list || []).map(item => ({
          ...item,
          roleLabel: this.formatRole(item.role)
        }))
      }).catch(() => {
        showApiError(this, '获取账号列表', error)
      })
    },
    showAddDialog() {
      this.isEditing = false
      this.editItem = defaultAccount()
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.isEditing = true
      this.editItem = Object.assign(defaultAccount(), row)
      this.dialogVisible = true
    },
    saveAccount() {
      const request = this.isEditing ? axios.put('/accounts/edit', this.editItem) : axios.post('/accounts/add', this.editItem)
      request.then(response => {
        if (response.data.code === '200') {
          this.$message.success(this.isEditing ? '编辑成功' : '新增成功')
          this.dialogVisible = false
          this.fetchAccounts()
        } else {
          this.$message.error(response.data.msg || '保存失败')
        }
      }).catch(() => {
        this.$message.error('保存失败')
      })
    },
    handleDelete(id) {
      this.$confirm('确认删除该员工账号？', '提示', { type: 'warning' }).then(() => {
        axios.delete(`/accounts/delete/${id}`).then(() => {
          this.$message.success('删除成功')
          this.fetchAccounts()
        })
      })
    }
  }
}
</script>

<style scoped>
.system-page { display:flex; flex-direction:column; gap:14px; }
.hero-card { display:flex; justify-content:space-between; align-items:center; gap:16px; padding:18px; border-radius:18px; background:#fff; box-shadow:0 8px 24px rgba(31,71,136,.08); }
.page-kicker { color:#64748b; font-size:13px; }
.hero-card h2 { margin:6px 0 8px; color:#0f172a; font-size:24px; }
.hero-card p { margin:0; color:#94a3b8; }
.table-card { border-radius:18px; }
</style>
