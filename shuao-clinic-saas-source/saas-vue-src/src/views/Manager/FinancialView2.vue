<template>
  <div style="height: 100%; width: 100%">
    <!-- 查询框 -->
    <div>
      <el-select v-model="searchType" placeholder="请选择查询条件" style="width: 150px;">
        <el-option label="编号" value="id"></el-option>
        <el-option label="名称" value="name"></el-option>
        <el-option label="金额" value="amount"></el-option>
        <el-option label="日期" value="date"></el-option>
        <el-option label="类型" value="type"></el-option>
      </el-select>
      <el-input v-model="keyword" style="width: 300px; margin-left: 10px; margin-right: 10px" placeholder="请输入关键词"></el-input>
      <el-button type="primary" @click="fetchDataByMonth">查询</el-button>
      <el-button type="info" @click="reset">重置</el-button>
    </div>

    <!-- 月份选择器 -->
    <div style="margin: 10px 0">
      <el-date-picker v-model="selectedMonth" type="month" placeholder="选择月份" format="yyyy-MM" value-format="yyyy-MM" @change="fetchDataByMonth"></el-date-picker>
    </div>

    <!-- 操作框 -->
    <div style="margin: 10px 0">
      <el-button type="primary" plain @click="showAddDialog">新增</el-button>
      <el-button type="info" plain @click="exportData">导出 CSV</el-button>
      <el-button type="info" plain @click="exportExcel">导出 Excel</el-button>
    </div>

    <!-- 表格 -->
    <div style="margin: 10px 0">
      <el-table :data="finances" stripe :header-cell-style="{ backgroundColor: 'aliceblue', color: '#666' }"
                height="340" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center"></el-table-column>
        <el-table-column prop="id" label="编号" width="70" align="center"></el-table-column>
        <el-table-column prop="name" label="名称"></el-table-column>
        <el-table-column prop="amount" label="金额"></el-table-column>
        <el-table-column prop="date" label="日期"></el-table-column>
        <el-table-column prop="type" label="类型"></el-table-column>
        <el-table-column prop="payment_channel_name" label="收款渠道"></el-table-column>
        <el-table-column prop="remark" label="备注"></el-table-column>
        <el-table-column label="操作" align="center" width="180">
          <template slot-scope="scope">
            <el-button size="mini" type="primary" plain @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="mini" type="danger" plain @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- ECharts 图表 -->
    <div>
      <div style="text-align: center; margin-top: 20px;">
        <h3>当月收入总额: {{ totalIncome }} | 当月支出总额: {{ totalExpense }} | 利润总额: {{ netTotal }}</h3>
      </div>
      <div ref="chart" style="width: 100%; height: 400px; margin-top: 20px;"></div>
    </div>

    <!-- 财务记录弹窗 -->
    <div>
      <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="30%">
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
              <el-option label="收入" value="收入"></el-option>
              <el-option label="支出" value="支出"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="备注" prop="remark">
            <el-input v-model="editItem.remark"></el-input>
          </el-form-item>
        </el-form>
        <span slot="footer" class="dialog-footer">
          <el-button @click="closeDialog">取消</el-button>
          <el-button type="primary" @click="handleSaveEdit">保存</el-button>
        </span>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
import * as echarts from 'echarts';
import moment from 'moment'; // Import moment.js for date manipulation
import * as XLSX from 'xlsx'; // Import xlsx

export default {
  name: 'FinancialManagement',
  data() {
    return {
      finances: [],
      selectedRows: [],
      searchType: 'id',
      keyword: '',
      dialogVisible: false,
      editItem: {
        name: '',
        amount: '',
        date: '',
        type: '',
        remark: ''
      },
      isEditing: false,
      chart: null,
      selectedMonth: moment().format('YYYY-MM'), // 默认当前月份
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
      let url = `/finances/selectByMonth`;
      if (this.keyword) {
        url = `/finances/select1By${this.searchType}?${this.searchType}=${this.keyword}`;
      }

      const normalizedMonth = typeof this.selectedMonth === 'string' && /^\d{4}-\d{2}$/.test(this.selectedMonth)
        ? this.selectedMonth
        : moment().format('YYYY-MM');
      if (this.selectedMonth !== normalizedMonth) {
        this.selectedMonth = normalizedMonth;
      }

      const [year, month] = normalizedMonth.split('-');
      const params = {
        year,
        month
      };

      axios.get(url, { params })
          .then(response => {
            this.finances = response.data.data;
            console.log(this.finances)
            if (this.finances) {
              this.updateChart();
            }
          })
          .catch(error => {
            console.error('Error fetching data by month:', error);
            this.$message.error('月份参数异常，已自动切回当前月份');
          });
    },
    updateChart() {
      const dateMap = {};
      let income = 0;
      let expense = 0;

      // Aggregate data by date and calculate total income and expense
      this.finances.forEach(finance => {
        const date = moment(finance.date).format('YYYY-MM-DD');
        if (!dateMap[date]) {
          dateMap[date] = 0;
        }
        dateMap[date] += parseFloat(finance.amount);

        if (finance.type === '收入') {
          income += parseFloat(finance.amount);
        } else if (finance.type === '支出') {
          expense += parseFloat(finance.amount);
        }
      });

      // Prepare data for the chart
      const dates = Object.keys(dateMap).sort();
      const amounts = dates.map(date => dateMap[date]);

      // Update totals
      this.totalIncome = income;
      this.totalExpense = expense;
      this.netTotal = (income + expense).toFixed(2);

      const option = {
        title: {
          text: '财务金额图'
        },
        tooltip: {},
        xAxis: {
          type: 'category',
          data: dates
        },
        yAxis: {},
        series: [{
          name: '金额',
          type: 'bar',
          data: amounts
        }]
      };
      this.chart.setOption(option);
    },
    // search() {
    //   let url = '/finances/all1';
    //   if (this.keyword) {
    //     url = `/finances/select1By${this.searchType}?${this.searchType}=${this.keyword}`;
    //   }
    //
    //   axios.get(url)
    //       .then(response => {
    //         this.finances = response.data.data;
    //         // this.updateChart();
    //       })
    //       .catch(error => {
    //         console.error('Error fetching finances:', error);
    //       });
    // },
    reset() {
      this.searchType = 'id';
      this.keyword = '';
      this.fetchDataByMonth();
    },
    handleSelectionChange(val) {
      this.selectedRows = val.map(row => row.id);
    },
    showAddDialog() {
      this.isEditing = false;
      this.editItem = {
        name: '',
        amount: '',
        date: '',
        type: '',
        remark: ''
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
      console.log(this.editItem);
    },
    handleEdit(row) {
      this.editItem = Object.assign({}, row);
      this.isEditing = true;
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
      // Convert table data to CSV format
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

      // Create a link and trigger the download
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

      // Generate Excel file and trigger download
      XLSX.writeFile(workbook, "finances.xlsx");
    },

  }
};
</script>

<style scoped>
.dialog-footer {
  text-align: right;
}
</style>
