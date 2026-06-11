<template>
  <div class="system-page">
    <div class="hero-card">
      <div>
        <div class="page-kicker">系统设置</div>
        <h2>知情同意书库</h2>
        <p>维护常用知情同意书模板，供医生下发电子同意书时直接套用。</p>
      </div>
      <div class="hero-actions">
        <el-button type="primary" plain @click="showAddDialog">新增模板</el-button>
        <el-button @click="fetchTemplates">刷新</el-button>
      </div>
    </div>

    <el-row :gutter="14" class="summary-row">
      <el-col :span="8">
        <el-card shadow="never" class="summary-card">
          <div class="summary-label">模板总数</div>
          <div class="summary-value">{{ templates.length }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="summary-card">
          <div class="summary-label">启用模板</div>
          <div class="summary-value">{{ enabledCount }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="summary-card">
          <div class="summary-label">停用模板</div>
          <div class="summary-value">{{ disabledCount }}</div>
        </el-card>
      </el-col>
    </el-row>

    <div class="catalog-grid">
      <div v-for="item in templates" :key="item.id" class="catalog-card">
        <div class="catalog-card__top">
          <div>
            <div class="catalog-card__title">{{ item.title }}</div>
            <div class="catalog-card__meta">排序 {{ item.sort_order || 0 }}</div>
          </div>
          <el-tag size="mini" :type="item.status === 1 ? 'success' : 'info'">{{ item.status === 1 ? '启用' : '停用' }}</el-tag>
        </div>
        <div class="catalog-card__section">
          <div class="label">模板内容</div>
          <div class="value value--multiline">{{ item.content || '未设置' }}</div>
        </div>
        <div class="catalog-card__section">
          <div class="label">备注</div>
          <div class="value">{{ item.remark || '未设置' }}</div>
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

    <el-dialog :title="isEditing ? '编辑模板' : '新增模板'" :visible.sync="dialogVisible" width="560px">
      <el-form :model="editItem" label-width="100px">
        <el-form-item label="模板标题"><el-input v-model="editItem.title"></el-input></el-form-item>
        <el-form-item label="模板内容"><el-input v-model="editItem.content" type="textarea" :rows="10"></el-input></el-form-item>
        <el-form-item label="备注"><el-input v-model="editItem.remark"></el-input></el-form-item>
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
        <el-button type="primary" @click="saveTemplate">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios'
import { showApiError } from '@/utils/errorMessage'

const defaultItem = () => ({
  title: '',
  content: '',
  remark: '',
  status: 1,
  sort_order: 0
})

export default {
  name: 'SystemConsentTemplateView',
  data() {
    return {
      templates: [],
      dialogVisible: false,
      isEditing: false,
      editItem: defaultItem()
    }
  },
  computed: {
    enabledCount() {
      return this.templates.filter(item => item.status === 1).length
    },
    disabledCount() {
      return this.templates.filter(item => item.status !== 1).length
    }
  },
  mounted() {
    this.fetchTemplates()
  },
  methods: {
    fetchTemplates() {
      axios.get('/consent-template/selectAll').then(res => {
        this.templates = res.data.data || []
      }).catch(() => {
        showApiError(this, '获取知情同意书模板', error)
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
    saveTemplate() {
      const request = this.isEditing ? axios.put('/consent-template/edit', this.editItem) : axios.post('/consent-template/add', this.editItem)
      request.then(res => {
        if (res.data.code === '200') {
          this.$message.success(this.isEditing ? '编辑成功' : '新增成功')
          this.dialogVisible = false
          this.fetchTemplates()
        } else {
          this.$message.error(res.data.msg || '保存失败')
        }
      }).catch(() => {
        this.$message.error('保存失败')
      })
    },
    handleDelete(id) {
      this.$confirm('确认删除该模板？', '提示', { type: 'warning' }).then(() => {
        axios.delete(`/consent-template/delete/${id}`).then(() => {
          this.$message.success('删除成功')
          this.fetchTemplates()
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
.catalog-grid { display:grid; grid-template-columns:repeat(auto-fill,minmax(320px,1fr)); gap:14px; }
.catalog-card { background:#fff; border-radius:18px; padding:16px; box-shadow:0 8px 20px rgba(31,71,136,.06); }
.catalog-card__top { display:flex; justify-content:space-between; gap:12px; }
.catalog-card__title { font-size:18px; font-weight:700; color:#303133; }
.catalog-card__meta { margin-top:6px; color:#5A8F7B; font-size:13px; }
.catalog-card__section { margin-top:14px; }
.catalog-card__section .label { font-size:12px; color:#8b95a7; }
.catalog-card__section .value { margin-top:6px; color:#303133; line-height:1.7; font-size:14px; }
.value--multiline { white-space:pre-wrap; max-height:220px; overflow-y:auto; }
.catalog-card__footer { margin-top:16px; display:flex; justify-content:space-between; align-items:center; color:#8b95a7; font-size:12px; gap:12px; }
.footer-actions { display:flex; gap:8px; }
</style>
