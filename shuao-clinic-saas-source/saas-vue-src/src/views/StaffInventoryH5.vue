<template>
  <div class="staff-h5-page inventory-h5-page">
    <div class="h5-hero-card">
      <div>
        <div class="h5-page-kicker">员工微信 H5</div>
        <h2>耗材库存速览</h2>
        <p>手机快速看耗材库存、低库存项目和预警阈值，口径与后台耗材管理保持一致。</p>
      </div>
      <div class="hero-actions">
        <el-select v-model="viewMode" size="small" placeholder="视图" @change="loadInventory">
          <el-option label="全部耗材" value="ALL"></el-option>
          <el-option label="低库存" value="LOW"></el-option>
        </el-select>
        <el-input v-model="keyword" placeholder="搜索耗材/规格/品牌" clearable @input="loadInventory"></el-input>
      </div>
    </div>

    <div class="h5-summary-row h5-summary-row--triple">
      <div class="h5-summary-card">
        <div class="summary-num">{{ items.length }}</div>
        <div class="summary-label">耗材档案</div>
      </div>
      <div class="h5-summary-card accent">
        <div class="summary-num">{{ lowStockCount }}</div>
        <div class="summary-label">低库存</div>
      </div>
      <div class="h5-summary-card success">
        <div class="summary-num">{{ activeCount }}</div>
        <div class="summary-label">在用档案</div>
      </div>
    </div>

    <div class="h5-section-card">
      <div class="section-title">库存列表</div>
      <div v-if="items.length" class="inventory-card-list">
        <div v-for="item in items" :key="item.id" class="inventory-card" :class="{ low: isLowStock(item) }">
          <div class="inventory-card__top">
            <div>
              <div class="inventory-card__name">{{ item.name || '未命名耗材' }}</div>
              <div class="inventory-card__meta">{{ item.category_name || '未分类' }} · {{ item.brand || '无品牌' }}</div>
            </div>
            <el-tag size="mini" :type="isLowStock(item) ? 'danger' : 'success'">{{ isLowStock(item) ? '低库存' : '正常' }}</el-tag>
          </div>
          <div class="inventory-card__desc">库存：{{ item.current_stock || 0 }} {{ item.unit || '' }}</div>
          <div class="inventory-card__desc">预警阈值：{{ item.min_stock_alert || 0 }}</div>
          <div class="inventory-card__desc">规格：{{ item.spec || '未填写' }}</div>
        </div>
      </div>
      <el-empty v-else description="暂无库存数据"></el-empty>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'StaffInventoryH5',
  data() {
    return {
      viewMode: 'ALL',
      keyword: '',
      items: []
    }
  },
  computed: {
    lowStockCount() {
      return this.items.filter(item => this.isLowStock(item)).length
    },
    activeCount() {
      return this.items.filter(item => String(item.status || '').trim() === '在用').length
    }
  },
  mounted() {
    this.loadInventory()
  },
  methods: {
    isLowStock(item) {
      const stock = Number(item.current_stock || 0)
      const alert = Number(item.min_stock_alert || 0)
      return !Number.isNaN(stock) && !Number.isNaN(alert) && alert > 0 && stock <= alert
    },
    loadInventory() {
      axios.get('/materials/search', {
        params: {
          page: 1,
          size: 200,
          lowStockOnly: this.viewMode === 'LOW' ? true : undefined
        }
      }).then(res => {
        const data = res.data.data || {}
        const list = data.list || []
        const text = String(this.keyword || '').trim().toLowerCase()
        this.items = (list || [])
          .filter(item => {
            if (!text) return true
            const name = String(item.name || '').toLowerCase()
            const spec = String(item.spec || '').toLowerCase()
            const brand = String(item.brand || '').toLowerCase()
            return name.includes(text) || spec.includes(text) || brand.includes(text)
          })
      }).catch(error => {
        console.error('Error fetching inventory:', error)
      })
    }
  }
}
</script>

<style scoped>
.staff-h5-page {
  min-height: 100vh;
  background: #f5f7fb;
  padding: 12px;
  box-sizing: border-box;
  overflow-x: hidden;
}
.h5-hero-card,
.h5-section-card {
  background: #fff;
  border-radius: 18px;
  padding: 14px;
  box-shadow: 0 8px 24px rgba(31, 71, 136, 0.08);
  margin-bottom: 12px;
}
.h5-page-kicker {
  color: #409eff;
  font-size: 12px;
  margin-bottom: 8px;
}
.h5-hero-card h2 {
  margin: 0 0 6px;
  font-size: 22px;
  line-height: 1.25;
  color: #1f2d3d;
}
.h5-hero-card p {
  margin: 0;
  color: #606266;
  font-size: 13px;
  line-height: 1.6;
  word-break: break-word;
}
.hero-actions {
  display: grid;
  grid-template-columns: 120px minmax(0, 1fr);
  gap: 10px;
  margin-top: 14px;
}
.h5-summary-row {
  display: grid;
  gap: 10px;
  margin-bottom: 12px;
}
.h5-summary-row--triple {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}
.h5-summary-card {
  background: #fff;
  border-radius: 16px;
  padding: 12px 10px;
  text-align: center;
  box-shadow: 0 8px 20px rgba(31, 71, 136, 0.06);
  min-width: 0;
}
.h5-summary-card.accent { background: #fff7ec; }
.h5-summary-card.success { background: #eefbf3; }
.summary-num {
  font-size: 19px;
  font-weight: 700;
  color: #303133;
  word-break: break-word;
}
.summary-label {
  margin-top: 6px;
  font-size: 12px;
  color: #8b95a7;
}
.section-title {
  font-size: 15px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 12px;
}
.inventory-card-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}
.inventory-card {
  border-radius: 16px;
  background: #f8fbff;
  padding: 14px;
  border: 1px solid transparent;
  min-width: 0;
  height: 100%;
}
.inventory-card.low {
  background: #fff7f7;
  border-color: #fecaca;
}
.inventory-card__top {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}
.inventory-card__name {
  font-size: 16px;
  font-weight: 700;
  color: #303133;
  line-height: 1.5;
  word-break: break-word;
}
.inventory-card__meta,
.inventory-card__desc {
  color: #606266;
  font-size: 13px;
  line-height: 1.7;
  margin-top: 6px;
  word-break: break-word;
}
@media (max-width: 768px) {
  .h5-summary-row--triple {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
@media (max-width: 420px) {
  .staff-h5-page {
    padding: 10px;
  }
  .hero-actions {
    grid-template-columns: 108px minmax(0, 1fr);
  }
  .h5-hero-card h2 {
    font-size: 20px;
  }
  .summary-num {
    font-size: 17px;
  }
  .inventory-card__top {
    flex-direction: column;
    align-items: flex-start;
  }
}

@media (max-width: 359px) {
  .hero-actions,
  .h5-summary-row--triple,
  .inventory-card-list {
    grid-template-columns: 1fr;
  }
}
</style>
