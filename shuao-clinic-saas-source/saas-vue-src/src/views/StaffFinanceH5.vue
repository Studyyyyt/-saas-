<template>
  <div class="staff-h5-page finance-h5-page">
    <div class="h5-hero-card">
      <div>
        <div class="h5-page-kicker">员工微信 H5</div>
        <h2>财务流水</h2>
        <p>手机查看最近收支，适合店长/管理员外出巡店时快速确认。</p>
      </div>
      <div class="hero-actions">
        <el-select v-model="typeFilter" size="small" placeholder="类型" @change="loadFinances">
          <el-option label="全部类型" value="ALL"></el-option>
          <el-option label="收入" value="收入"></el-option>
          <el-option label="支出" value="支出"></el-option>
        </el-select>
        <el-input v-model="keyword" placeholder="搜索名称/备注" clearable @input="loadFinances"></el-input>
      </div>
    </div>

    <div class="h5-summary-row h5-summary-row--triple">
      <div class="h5-summary-card">
        <div class="summary-num">{{ incomeTotal }}</div>
        <div class="summary-label">收入合计</div>
      </div>
      <div class="h5-summary-card accent">
        <div class="summary-num">{{ expenseTotal }}</div>
        <div class="summary-label">支出合计</div>
      </div>
      <div class="h5-summary-card success">
        <div class="summary-num">{{ balanceTotal }}</div>
        <div class="summary-label">净额</div>
      </div>
    </div>

    <div class="h5-section-card">
      <div class="section-title">最近流水</div>
      <div v-if="finances.length" class="finance-card-list">
        <div v-for="item in finances" :key="item.id" class="finance-card">
          <div class="finance-card__top">
            <div>
              <div class="finance-card__name">{{ item.name || '未命名流水' }}</div>
              <div class="finance-card__meta">{{ item.date || '未记录日期' }} · {{ item.type || '未知类型' }}</div>
            </div>
            <div class="finance-card__amount" :class="{ income: item.type === '收入', expense: item.type === '支出' }">
              {{ item.type === '支出' ? '-' : '+' }}{{ formatAmount(item.amount) }}
            </div>
          </div>
          <div class="finance-card__remark">{{ item.remark || '无备注' }}</div>
        </div>
      </div>
      <el-empty v-else description="暂无财务数据"></el-empty>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'StaffFinanceH5',
  data() {
    return {
      typeFilter: 'ALL',
      keyword: '',
      finances: []
    }
  },
  computed: {
    incomeTotal() {
      return this.finances
        .filter(item => item.type === '收入')
        .reduce((sum, item) => sum + this.toNumber(item.amount), 0)
        .toFixed(2)
    },
    expenseTotal() {
      return this.finances
        .filter(item => item.type === '支出')
        .reduce((sum, item) => sum + this.toNumber(item.amount), 0)
        .toFixed(2)
    },
    balanceTotal() {
      return (Number(this.incomeTotal) - Number(this.expenseTotal)).toFixed(2)
    }
  },
  mounted() {
    this.loadFinances()
  },
  methods: {
    toNumber(value) {
      const num = Number(value || 0)
      return Number.isNaN(num) ? 0 : num
    },
    formatAmount(value) {
      return this.toNumber(value).toFixed(2)
    },
    loadFinances() {
      axios.get('/finances/all', { params: { page: 1, size: 200 } }).then(res => {
        const data = res.data.data || {}
        const list = data.list || []
        const text = String(this.keyword || '').trim().toLowerCase()
        this.finances = (list || [])
          .filter(item => {
            const matchedType = this.typeFilter === 'ALL' || (item.type || '') === this.typeFilter
            if (!matchedType) return false
            if (!text) return true
            const name = String(item.name || '').toLowerCase()
            const remark = String(item.remark || '').toLowerCase()
            return name.includes(text) || remark.includes(text)
          })
          .sort((a, b) => String(b.date || '').localeCompare(String(a.date || '')))
      }).catch(error => {
        console.error('Error fetching finances:', error)
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
  font-size: 18px;
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
.finance-card-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}
.finance-card {
  border-radius: 16px;
  background: #f8fbff;
  padding: 14px;
  min-width: 0;
  height: 100%;
}
.finance-card__top {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}
.finance-card__name {
  font-size: 16px;
  font-weight: 700;
  color: #303133;
  line-height: 1.5;
  word-break: break-word;
}
.finance-card__meta,
.finance-card__remark {
  color: #606266;
  font-size: 13px;
  line-height: 1.7;
  margin-top: 6px;
  word-break: break-word;
}
.finance-card__amount {
  font-size: 16px;
  font-weight: 700;
  white-space: nowrap;
  flex-shrink: 0;
}
.finance-card__amount.income {
  color: #16a34a;
}
.finance-card__amount.expense {
  color: #ef4444;
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
  .finance-card__top {
    flex-direction: column;
    align-items: flex-start;
  }
}

@media (max-width: 359px) {
  .hero-actions,
  .h5-summary-row--triple,
  .finance-card-list {
    grid-template-columns: 1fr;
  }
}
</style>
