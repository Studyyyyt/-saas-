<template>
  <div class="financial-page">
    <!-- 页面标题区 -->
    <div class="hero-card">
      <div>
        <div class="page-kicker">财务管理</div>
        <h2>月度财务</h2>
        <p>按月份查看财务收支明细与统计图表。</p>
      </div>
      <div class="hero-actions">
        <el-button type="primary" plain @click="showAddDialog">新增</el-button>
        <el-button type="info" plain @click="exportData">导出 CSV</el-button>
        <el-button type="info" plain @click="exportExcel">导出 Excel</el-button>
      </div>
    </div>

    <!-- 查询区 -->
    <el-card shadow="never" class="query-card">
      <div class="query-row">
        <el-select v-model="searchType" placeholder="请选择查询条件" class="query-select">
          <el-option label="编号" value="id"></el-option>
          <el-option label="名称" value="name"></el-option>
          <el-option label="金额" value="amount"></el-option>
          <el-option label="日期" value="date"></el-option>
          <el-option label="类型" value="type"></el-option>
        </el-select>
        <el-input v-model="keyword" class="query-input" placeholder="请输入关键词" @keyup.enter.native="fetchDataByMonth"></el-input>
        <el-date-picker v-model="selectedMonth" type="month" placeholder="选择月份" format="yyyy-MM" value-format="yyyy-MM" @change="fetchDataByMonth"></el-date-picker>
        <el-button type="primary" icon="el-icon-search" @click="fetchDataByMonth">查询</el-button>
        <el-button icon="el-icon-refresh" @click="reset">重置</el-button>
      </div>
    </el-card>

    <!-- 表格区 -->
    <el-card shadow="never" class="table-card">
      <el-table
        :data="finances"
        stripe
        v-loading="loading"
        :header-cell-style="{ backgroundColor: '#f8fafc', color: '#475569', fontWeight: '600' }"
        height="340"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center"></el-table-column>
        <el-table-column prop="id" label="编号" width="70" align="center"></el-table-column>
        <el-table-column prop="name" label="名称"></el-table-column>
        <el-table-column prop="amount" label="金额" align="right">
          <template slot-scope="scope">
            <span :class="parseFloat(scope.row.amount) >= 0 ? 'amount-positive' : 'amount-negative'">
              {{ scope.row.amount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="date" label="日期"></el-table-column>
        <el-table-column prop="type" label="类型">
          <template slot-scope="scope">
            {{ scope.row.type === 'income' ? '收入' : scope.row.type === 'expense' ? '支出' : scope.row.type }}
          </template>
        </el-table-column>
        <el-table-column prop="payment_channel_name" label="收款渠道"></el-table-column>
        <el-table-column prop="remark" label="备注"></el-table-column>
        <el-table-column label="操作" align="center" width="220" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" type="primary" plain @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="mini" type="danger" plain @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && !finances.length" description="暂无财务数据"></el-empty>
    </el-card>

    <!-- 统计图表 -->
    <el-card shadow="never" class="chart-card">
      <div class="chart-summary">
        <div class="chart-summary-item">
          <div class="chart-summary-label">当月收入总额</div>
          <div class="chart-summary-value amount-positive">{{ totalIncome }}</div>
        </div>
        <div class="chart-summary-divider"></div>
        <div class="chart-summary-item">
          <div class="chart-summary-label">当月支出总额</div>
          <div class="chart-summary-value amount-negative">{{ totalExpense }}</div>
        </div>
        <div class="chart-summary-divider"></div>
        <div class="chart-summary-item">
          <div class="chart-summary-label">利润总额</div>
          <div class="chart-summary-value">{{ netTotal }}</div>
        </div>
      </div>
      <div ref="chart" class="chart-container"></div>
    </el-card>

    <!-- 财务记录弹窗 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="480px">
      <el-form ref="form" :model="editItem" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="editItem.name"></el-input>
        </el-form-item>
        <el-form-item label="金额" prop="amount">
          <el-input v-model="editItem.amount"></el-input>
        </el-form-item>
        <el-form-item label="日期" prop="date">
          <el-date-picker v-model="editItem.date" type="date" placeholder="选择日期" value-format="yyyy-MM-dd"></el-date-picker>
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="editItem.type" placeholder="请选择类型">
            <el-option label="收入" value="income"></el-option>
            <el-option label="支出" value="expense"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="editItem.remark"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="closeDialog">取消</el-button>
        <el-button type="primary" @click="handleSaveEdit">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios';
import * as echarts from 'echarts';
import moment from 'moment';
import * as XLSX from 'xlsx';

export default {
  name: 'FinancialMonthlyView',
  data() {
    return {
      loading: false,
      finances: [],
      selectedRows: [],
      searchType: 'id',
      keyword: '',
      dialogVisible: false,
      editItem: {
        id: null,
        name: '',
        amount: '',
        date: '',
        type: '',
        remark: '',
        patient_id: null,
        treatment_id: null,
        payment_channel_id: null,
        payment_channel_name: ''
      },
      isEditing: false,
      chart: null,
      selectedMonth: moment().format('YYYY-MM'),
      totalIncome: 0,
      totalExpense: 0,
      netTotal: 0
    };
  },
  computed: {
    dialogTitle() {
      return this.isEditing ? '编辑信息' : '新增信息';
    }
  },
  mounted() {
    this.fetchDataByMonth();
    this.chart = echarts.init(this.$refs.chart);
  },
  methods: {
    fetchDataByMonth() {
      this.loading = true;
      let url = `/finances/selectByMonth`;
      if (this.keyword) {
        url = `/finances/selectBy${this.searchType.charAt(0).toUpperCase() + this.searchType.slice(1)}`;
      }

      const normalizedMonth = typeof this.selectedMonth === 'string' && /^\d{4}-\d{2}$/.test(this.selectedMonth)
        ? this.selectedMonth
        : moment().format('YYYY-MM');
      if (this.selectedMonth !== normalizedMonth) {
        this.selectedMonth = normalizedMonth;
      }

      const [year, month] = normalizedMonth.split('-');
      const params = { year, month };
      if (this.keyword) {
        params[this.searchType] = this.keyword;
      }

      axios.get(url, { params })
          .then(response => {
            this.finances = response.data.data || [];
            this.updateChart();
          })
          .catch(error => {
            console.error('Error fetching data by month:', error);
            this.finances = [];
            this.totalIncome = 0;
            this.totalExpense = 0;
            this.netTotal = 0;
            this.$message.error('月份参数异常，已自动切回当前月份');
          })
          .finally(() => {
            this.loading = false;
          });
    },
    updateChart() {
      const dateMap = {};
      let income = 0;
      let expense = 0;

      if (!Array.isArray(this.finances) || this.finances.length === 0) {
        this.totalIncome = 0;
        this.totalExpense = 0;
        this.netTotal = 0;
        this.chart.setOption({
          title: { text: '财务金额图' },
          tooltip: {},
          xAxis: { type: 'category', data: [] },
          yAxis: {},
          series: [{ name: '金额', type: 'bar', data: [] }]
        });
        return;
      }

      this.finances.forEach(finance => {
        const date = moment(finance.date).format('YYYY-MM-DD');
        if (!dateMap[date]) {
          dateMap[date] = 0;
        }
        dateMap[date] += parseFloat(finance.amount);

        const t = finance.type;
        if (t === 'income' || t === '收入') {
          income += parseFloat(finance.amount);
        } else if (t === 'expense' || t === '支出') {
          expense += parseFloat(finance.amount);
        }
      });

      const dates = Object.keys(dateMap).sort();
      const amounts = dates.map(date => dateMap[date]);

      this.totalIncome = income.toFixed(2);
      this.totalExpense = expense.toFixed(2);
      this.netTotal = (income - expense).toFixed(2);

      const option = {
        title: { text: '财务金额图' },
        tooltip: {},
        xAxis: { type: 'category', data: dates },
        yAxis: {},
        series: [{ name: '金额', type: 'bar', data: amounts }]
      };
      this.chart.setOption(option, true);
    },
    reset() {
      this.searchType = 'id';
      this.keyword = '';
      this.selectedMonth = moment().format('YYYY-MM');
      this.fetchDataByMonth();
    },
    handleSelectionChange(val) {
      this.selectedRows = val.map(row => row.id);
    },
    showAddDialog() {
      this.isEditing = false;
      this.editItem = {
        id: null,
        name: '',
        amount: '',
        date: '',
        type: '',
        remark: '',
        patient_id: null,
        treatment_id: null,
        payment_channel_id: null,
        payment_channel_name: ''
      };
      this.dialogVisible = true;
    },
    closeDialog() {
      this.dialogVisible = false;
      this.isEditing = false;
    },
    handleAdd() {
      axios.post("/finances/add", this.editItem)
          .then(response => {
            this.$message.success("新增成功");
            this.dialogVisible = false;
            this.fetchDataByMonth();
          })
          .catch(error => {
            console.error('Error adding new item:', error);
            this.$message.error("新增失败");
          });
    },
    handleEdit(row) {
      this.isEditing = true;
      const typeMap = { '收入': 'income', '支出': 'expense' };
      this.editItem = {
        id: row.id,
        name: row.name || '',
        amount: row.amount !== undefined ? String(row.amount) : '',
        date: row.date || '',
        type: typeMap[row.type] || row.type || '',
        remark: row.remark || '',
        patient_id: row.patient_id,
        treatment_id: row.treatment_id,
        payment_channel_id: row.payment_channel_id,
        payment_channel_name: row.payment_channel_name || ''
      };
      this.dialogVisible = true;
    },
    handleSaveEdit() {
      if (this.isEditing) {
        axios.put("/finances/edit", this.editItem)
            .then(response => {
              this.$message.success("编辑成功");
              this.closeDialog();
              this.fetchDataByMonth();
            })
            .catch(error => {
              console.error('Error editing item:', error);
              this.$message.error("编辑失败");
            });
      } else {
        this.handleAdd();
      }
    },
    handleDelete(id) {
      this.$confirm('此操作将永久删除该条目，是否继续？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        axios.delete(`/finances/delete/${id}`)
            .then(response => {
              this.$message.success('删除成功');
              this.fetchDataByMonth();
            })
            .catch(error => {
              console.error('删除失败', error);
              this.$message.error('删除失败');
            });
      }).catch(() => {
        this.$message.info('已取消删除');
      });
    },
    exportData() {
      const headers = ['编号', '名称', '金额', '日期', '类型', '收款渠道', '备注'];
      const rows = this.finances.map(finance => [
        finance.id,
        finance.name,
        finance.amount,
        finance.date,
        finance.type,
        finance.payment_channel_name || '',
        finance.remark
      ]);

      let csvContent = "data:text/csv;charset=utf-8,"
          + headers.join(",") + "\n"
          + rows.map(row => row.join(",")).join("\n");

      const encodedUri = encodeURI(csvContent);
      const link = document.createElement("a");
      link.setAttribute("href", encodedUri);
      link.setAttribute("download", "finances.csv");
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    },
    exportExcel() {
      const headers = ['编号', '名称', '金额', '日期', '类型', '收款渠道', '备注'];
      const rows = this.finances.map(finance => [
        finance.id,
        finance.name,
        finance.amount,
        finance.date,
        finance.type,
        finance.payment_channel_name || '',
        finance.remark
      ]);

      const worksheet = XLSX.utils.aoa_to_sheet([headers, ...rows]);
      const workbook = XLSX.utils.book_new();
      XLSX.utils.book_append_sheet(workbook, worksheet, "财务数据");
      XLSX.writeFile(workbook, "finances.xlsx");
    }
  }
};
</script>

<style scoped>
.financial-page {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.amount-positive {
  color: #67C23A;
  font-weight: 600;
}
.amount-negative {
  color: #F56C6C;
  font-weight: 600;
}
.chart-card {
  border-radius: 18px;
}
.chart-summary {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 24px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}
.chart-summary-item {
  text-align: center;
}
.chart-summary-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 6px;
}
.chart-summary-value {
  font-size: 22px;
  font-weight: 700;
  color: #303133;
}
.chart-summary-divider {
  width: 1px;
  height: 36px;
  background: #e2e8f0;
}
.chart-container {
  width: 100%;
  height: 400px;
}
.dialog-footer {
  text-align: right;
}
</style>
