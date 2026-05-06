<template>
  <div class="system-page">
    <div class="hero-card">
      <div>
        <div class="page-kicker">系统设置</div>
        <h2>收款渠道</h2>
        <p>维护收费时可选的收款渠道，支持收费拆分到多个渠道。</p>
      </div>
      <div class="hero-actions">
        <el-button type="primary" plain @click="showAddDialog">新增渠道</el-button>
        <el-button @click="fetchChannels">刷新</el-button>
      </div>
    </div>

    <el-row :gutter="14" class="summary-row">
      <el-col :span="8">
        <el-card shadow="never" class="summary-card">
          <div class="summary-label">渠道总数</div>
          <div class="summary-value">{{ channels.length }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="summary-card">
          <div class="summary-label">启用渠道</div>
          <div class="summary-value">{{ enabledCount }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="summary-card">
          <div class="summary-label">停用渠道</div>
          <div class="summary-value">{{ disabledCount }}</div>
        </el-card>
      </el-col>
    </el-row>

    <div class="catalog-grid">
      <div v-for="item in channels" :key="item.id" class="catalog-card">
        <div class="catalog-card__top">
          <div>
            <div class="catalog-card__title">{{ item.channel_name }}</div>
            <div class="catalog-card__meta">排序 {{ item.sort_order || 0 }}</div>
          </div>
          <el-tag size="mini" :type="item.status === 1 ? 'success' : 'info'">{{ item.status === 1 ? '启用' : '停用' }}</el-tag>
        </div>
        <div class="catalog-card__footer">
          <span>更新时间：{{ formatDateTime(item.updated_at) || '-' }}</span>
          <div class="footer-actions">
            <el-button size="mini" type="primary" plain @click="handleEdit(item)">编辑</el-button>
            <el-button size="mini" type="danger" plain @click="handleDelete(item.id)">删除</el-button>
          </div>
        </div>
      </div>
    </div>

    <el-dialog :title="isEditing ? '编辑渠道' : '新增渠道'" :visible.sync="dialogVisible" width="420px">
      <el-form :model="editItem" label-width="100px">
        <el-form-item label="渠道名称"><el-input v-model="editItem.channel_name"></el-input></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="editItem.status" style="width:100%">
            <el-option label="启用" :value="1"></el-option>
            <el-option label="停用" :value="0"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="排序"><el-input v-model="editItem.sort_order"></el-input></el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveChannel">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios'
import { showApiError } from '@/utils/errorMessage'

const defaultItem = () => ({
  channel_name: '',
  status: 1,
  sort_order: 0
})

export default {
  name: 'SystemPaymentChannelView',
  data() {
    return {
      channels: [],
      dialogVisible: false,
      isEditing: false,
      editItem: defaultItem()
    }
  },
  computed: {
    enabledCount() {
      return this.channels.filter(item => item.status === 1).length
    },
    disabledCount() {
      return this.channels.filter(item => item.status !== 1).length
    }
  },
  mounted() {
    this.fetchChannels()
  },
  methods: {
    fetchChannels() {
      axios.get('/payment-channels/selectAll').then(res => {
        this.channels = res.data.data || []
      }).catch(() => {
        showApiError(this, '获取收款渠道', error)
      })
    },
    showAddDialog() {
      this.isEditing = false
      this.editItem = defaultItem()
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.isEditing = true
      this.editItem = Object.assign(defaultItem(), row)
      this.dialogVisible = true
    },
    saveChannel() {
      const request = this.isEditing ? axios.put('/payment-channels/edit', this.editItem) : axios.post('/payment-channels/add', this.editItem)
      request.then(res => {
        if (res.data.code === '200') {
          this.$message.success(this.isEditing ? '编辑成功' : '新增成功')
          this.dialogVisible = false
          this.fetchChannels()
        } else {
          this.$message.error(res.data.msg || '保存失败')
        }
      }).catch(() => {
        this.$message.error('保存失败')
      })
    },
    handleDelete(id) {
      this.$confirm('确认删除该渠道？', '提示', { type: 'warning' }).then(() => {
        axios.delete(`/payment-channels/delete/${id}`).then(() => {
          this.$message.success('删除成功')
          this.fetchChannels()
        })
      })
    },
    formatDateTime(value) {
      if (!value) return ''
      return String(value).slice(0, 19).replace('T', ' ')
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
.hero-actions { display:flex; gap:10px; }
.summary-row { margin:0 !important; }
.summary-card { border-radius:18px; }
.summary-label { color:#94a3b8; font-size:13px; }
.summary-value { margin-top:8px; font-size:28px; font-weight:700; color:#0f172a; }
.catalog-grid { display:grid; grid-template-columns:repeat(auto-fill,minmax(280px,1fr)); gap:14px; }
.catalog-card { background:#fff; border-radius:18px; padding:16px; box-shadow:0 8px 20px rgba(31,71,136,.06); }
.catalog-card__top { display:flex; justify-content:space-between; gap:12px; }
.catalog-card__title { font-size:18px; font-weight:700; color:#303133; }
.catalog-card__meta { margin-top:6px; color:#409eff; font-size:13px; }
.catalog-card__footer { margin-top:16px; display:flex; justify-content:space-between; align-items:center; color:#8b95a7; font-size:12px; gap:12px; }
.footer-actions { display:flex; gap:8px; }
</style>
