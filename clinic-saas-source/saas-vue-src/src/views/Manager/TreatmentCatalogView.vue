<template>
  <div class="catalog-page">
    <div class="catalog-header-card">
      <div>
        <div class="page-kicker">处置收费</div>
        <h2>项目库</h2>
        <p>维护常用处置收费项目，供开处置时直接选择并自动带出默认值。</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" plain @click="showAddDialog">新增项目</el-button>
        <el-button @click="fetchCatalog">刷新</el-button>
      </div>
    </div>

    <div class="catalog-grid">
      <div v-for="item in catalogs" :key="item.id" class="catalog-card">
        <div class="catalog-card__top">
          <div>
            <div class="catalog-card__title">{{ item.item_name }}</div>
            <div class="catalog-card__meta">默认收费 ¥{{ item.default_fee || '0' }}</div>
          </div>
          <el-tag size="mini" :type="item.status === 1 ? 'success' : 'info'">{{ item.status === 1 ? '启用' : '停用' }}</el-tag>
        </div>
        <div class="catalog-card__section">
          <div class="label">默认治疗内容</div>
          <div class="value">{{ item.default_content || '未设置' }}</div>
        </div>
        <div class="catalog-card__section">
          <div class="label">默认使用材料</div>
          <div class="value">{{ item.default_product || '未设置' }}</div>
        </div>
        <div class="catalog-card__footer">
          <span>排序：{{ item.sort_order || 0 }}</span>
          <div class="footer-actions">
            <el-button size="mini" type="primary" plain @click="handleEdit(item)">编辑</el-button>
            <el-button size="mini" type="danger" plain @click="handleDelete(item.id)">删除</el-button>
          </div>
        </div>
      </div>
    </div>

    <el-dialog :title="isEditing ? '编辑项目' : '新增项目'" :visible.sync="dialogVisible" width="420px">
      <el-form :model="editItem" label-width="110px">
        <el-form-item label="项目名称"><el-input v-model="editItem.item_name"></el-input></el-form-item>
        <el-form-item label="默认收费"><el-input v-model="editItem.default_fee"></el-input></el-form-item>
        <el-form-item label="默认治疗内容"><el-input v-model="editItem.default_content" type="textarea" :rows="2"></el-input></el-form-item>
        <el-form-item label="默认使用材料"><el-input v-model="editItem.default_product"></el-input></el-form-item>
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
        <el-button type="primary" @click="saveCatalog">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'TreatmentCatalogView',
  data() {
    return {
      catalogs: [],
      dialogVisible: false,
      isEditing: false,
      editItem: {
        item_name: '',
        default_fee: '',
        default_content: '',
        default_product: '',
        status: 1,
        sort_order: 0
      }
    }
  },
  mounted() {
    this.fetchCatalog()
  },
  methods: {
    fetchCatalog() {
      axios.get('/treatment-catalog/selectAll').then(res => {
        this.catalogs = res.data.data || []
      })
    },
    showAddDialog() {
      this.isEditing = false
      this.editItem = {
        item_name: '',
        default_fee: '',
        default_content: '',
        default_product: '',
        status: 1,
        sort_order: 0
      }
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.isEditing = true
      this.editItem = Object.assign({}, row)
      this.dialogVisible = true
    },
    saveCatalog() {
      const request = this.isEditing ? axios.put('/treatment-catalog/edit', this.editItem) : axios.post('/treatment-catalog/add', this.editItem)
      request.then(res => {
        if (res.data.code === '200') {
          this.$message.success(this.isEditing ? '编辑成功' : '新增成功')
          this.dialogVisible = false
          this.fetchCatalog()
        } else {
          this.$message.error(res.data.msg || '保存失败')
        }
      })
    },
    handleDelete(id) {
      this.$confirm('确认删除该项目？', '提示', { type: 'warning' }).then(() => {
        axios.delete(`/treatment-catalog/delete/${id}`).then(() => {
          this.$message.success('删除成功')
          this.fetchCatalog()
        })
      })
    }
  }
}
</script>

<style scoped>
.catalog-page {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.catalog-header-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  padding: 18px;
  border-radius: 18px;
  background: #fff;
  box-shadow: 0 8px 24px rgba(31, 71, 136, 0.08);
}
.page-kicker {
  color: #64748b;
  font-size: 13px;
}
.catalog-header-card h2 {
  margin: 6px 0 8px;
  color: #0f172a;
  font-size: 24px;
}
.catalog-header-card p {
  margin: 0;
  color: #94a3b8;
}
.header-actions {
  display: flex;
  gap: 10px;
}
.catalog-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 14px;
}
.catalog-card {
  background: #fff;
  border-radius: 18px;
  padding: 16px;
  box-shadow: 0 8px 20px rgba(31, 71, 136, 0.06);
}
.catalog-card__top {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}
.catalog-card__title {
  font-size: 18px;
  font-weight: 700;
  color: #303133;
}
.catalog-card__meta {
  margin-top: 6px;
  color: #5A8F7B;
  font-size: 13px;
}
.catalog-card__section {
  margin-top: 14px;
}
.catalog-card__section .label {
  font-size: 12px;
  color: #8b95a7;
}
.catalog-card__section .value {
  margin-top: 6px;
  color: #303133;
  line-height: 1.7;
  font-size: 14px;
}
.catalog-card__footer {
  margin-top: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #8b95a7;
  font-size: 12px;
}
.footer-actions {
  display: flex;
  gap: 8px;
}
</style>
