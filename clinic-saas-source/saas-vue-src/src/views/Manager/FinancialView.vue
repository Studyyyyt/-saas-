<template>
  <div class="financial-page">
    <!-- 页面标题区 -->
    <div class="hero-card">
      <div>
        <div class="page-kicker">财务管理</div>
        <h2>财务流水</h2>
        <p>管理诊所收入与支出，查看医生业绩统计。</p>
      </div>
      <div class="hero-actions">
        <el-button type="primary" plain @click="showAddDialog">新增</el-button>
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
        <el-input v-model="keyword" class="query-input" placeholder="请输入关键词" @keyup.enter.native="search"></el-input>
        <el-button type="primary" icon="el-icon-search" @click="search">查询</el-button>
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
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center"></el-table-column>
        <el-table-column prop="id" label="编号" width="70" align="center"></el-table-column>
        <el-table-column prop="name" label="名称"></el-table-column>
        <el-table-column prop="amount" label="金额"></el-table-column>
        <el-table-column prop="date" label="日期"></el-table-column>
        <el-table-column prop="type" label="类型"></el-table-column>
        <el-table-column prop="payment_channel_name" label="收款渠道"></el-table-column>
        <el-table-column prop="remark" label="备注"></el-table-column>
        <el-table-column label="操作" align="center" width="220" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" type="primary" plain @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="mini" type="danger" plain @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && !finances.length" description="暂无财务流水"></el-empty>
      <div v-else class="pagination-row">
        <el-pagination
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="currentPage"
            :page-sizes="[5, 10, 20, 50]"
            :page-size="pageSize"
            layout="total, sizes, prev, pager, next, jumper"
            :total="totalItems">
        </el-pagination>
      </div>
    </el-card>

    <!-- 医生业绩统计 -->
    <el-card class="doctor-performance-card" shadow="never">
      <div slot="header" class="doctor-performance-header">
        <div>
          <div class="doctor-performance-title">医生业绩统计</div>
          <div class="doctor-performance-tip">
            统计口径：优先按病历操作业绩分摊快照汇总折后产值；无分摊快照的历史处置仍按治疗记录兜底统计。实收金额 = 收费金额 - 退款金额。
          </div>
        </div>
        <div class="doctor-performance-actions">
          <el-date-picker
              v-model="doctorPerformanceRange"
              type="daterange"
              unlink-panels
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="yyyy-MM-dd">
          </el-date-picker>
          <el-button type="primary" @click="fetchDoctorPerformance">查询统计</el-button>
          <el-button @click="resetDoctorPerformanceRange">重置范围</el-button>
        </div>
      </div>

      <div class="doctor-performance-summary">
        <div class="summary-item">
          <div class="summary-label">医生人数</div>
          <div class="summary-value">{{ doctorPerformanceMeta.doctor_count || 0 }}</div>
        </div>
        <div class="summary-item">
          <div class="summary-label">业绩操作数</div>
          <div class="summary-value">{{ doctorPerformanceSummary.project_count || 0 }}</div>
        </div>
        <div class="summary-item">
          <div class="summary-label">营业额合计</div>
          <div class="summary-value">¥{{ formatMoney(doctorPerformanceSummary.turnover_amount) }}</div>
        </div>
        <div class="summary-item">
          <div class="summary-label">实收合计</div>
          <div class="summary-value">¥{{ formatMoney(doctorPerformanceSummary.received_amount) }}</div>
        </div>
        <div class="summary-item">
          <div class="summary-label">欠费合计</div>
          <div class="summary-value">¥{{ formatMoney(doctorPerformanceSummary.arrears_amount) }}</div>
        </div>
      </div>

      <div class="doctor-performance-range-text">
        当前统计范围：{{ doctorPerformanceMeta.start_date || '--' }} 至 {{ doctorPerformanceMeta.end_date || '--' }}
      </div>

      <el-table
          v-loading="doctorPerformanceLoading"
          :data="doctorPerformanceList"
          stripe
          border
          show-summary
          :summary-method="buildDoctorPerformanceSummary"
          :header-cell-style="{ backgroundColor: '#f8fafc', color: '#475569', fontWeight: '600' }">
        <el-table-column prop="doctor_name" label="医生" min-width="160"></el-table-column>
        <el-table-column prop="project_count" label="业绩操作数" width="110" align="center"></el-table-column>
        <el-table-column prop="turnover_amount" label="营业额" min-width="130" align="right">
          <template slot-scope="scope">¥{{ formatMoney(scope.row.turnover_amount) }}</template>
        </el-table-column>
        <el-table-column prop="received_amount" label="实收金额" min-width="130" align="right">
          <template slot-scope="scope">¥{{ formatMoney(scope.row.received_amount) }}</template>
        </el-table-column>
        <el-table-column prop="refunded_amount" label="退款金额" min-width="130" align="right">
          <template slot-scope="scope">¥{{ formatMoney(scope.row.refunded_amount) }}</template>
        </el-table-column>
        <el-table-column prop="arrears_amount" label="欠费金额" min-width="130" align="right">
          <template slot-scope="scope">¥{{ formatMoney(scope.row.arrears_amount) }}</template>
        </el-table-column>
      </el-table>
    </el-card>

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
            <el-option label="收入" value="收入"></el-option>
            <el-option label="支出" value="支出"></el-option>
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
import moment from 'moment';
import { showApiError } from '@/utils/errorMessage'

const createEmptyDoctorPerformanceSummary = () => ({
  doctor_name: '合计',
  project_count: 0,
  turnover_amount: 0,
  received_amount: 0,
  refunded_amount: 0,
  arrears_amount: 0
});

const createDefaultDoctorPerformanceRange = () => ([
  moment().startOf('month').format('YYYY-MM-DD'),
  moment().format('YYYY-MM-DD')
]);

export default {
  name: 'FinancialManagement',
  data() {
    return {
      loading: false,
      finances: [],
      selectedRows: [],
      currentPage: 1,
      pageSize: 5,
      totalItems: 0,
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
      doctorPerformanceRange: createDefaultDoctorPerformanceRange(),
      doctorPerformanceLoading: false,
      doctorPerformanceList: [],
      doctorPerformanceSummary: createEmptyDoctorPerformanceSummary(),
      doctorPerformanceMeta: {
        start_date: '',
        end_date: '',
        doctor_count: 0
      }
    };
  },
  computed: {
    dialogTitle() {
      return this.isEditing ? '编辑信息' : '新增信息';
    }
  },
  mounted() {
    this.search();
    this.fetchDoctorPerformance();
  },
  methods: {
    search() {
      this.loading = true;
      let url = '/finances/selectAll';
      const params = {
        page: this.currentPage,
        size: this.pageSize
      };

      if (this.keyword) {
        url = `/finances/selectBy${this.searchType.charAt(0).toUpperCase() + this.searchType.slice(1)}`;
        params[this.searchType] = this.keyword;
      }

      axios.get(url, { params })
          .then(response => {
            const data = response.data.data || {};
            this.finances = data.list || [];
            this.totalItems = data.total || 0;
          })
          .catch(error => {
            console.error('Error fetching finances:', error);
            showApiError(this, '获取财务流水', error)
          })
          .finally(() => {
            this.loading = false;
          });
    },
    fetchDoctorPerformance() {
      const range = this.normalizeDoctorPerformanceRange();
      if (!range) {
        return;
      }
      const [startDate, endDate] = range;
      this.doctorPerformanceLoading = true;

      axios.get('/finances/doctorPerformance', {
        params: {
          startDate,
          endDate
        }
      }).then(response => {
        const result = response.data || {};
        if (result.code !== '200') {
          this.$message.error((result.data.msg || '获取医生业绩统计失败') + '，请刷新页面重试。如问题持续，请联系管理员。')
          return;
        }
        const data = result.data || {};
        this.doctorPerformanceList = data.list || [];
        this.doctorPerformanceSummary = Object.assign(createEmptyDoctorPerformanceSummary(), data.summary || {});
        this.doctorPerformanceMeta = {
          start_date: data.start_date || startDate,
          end_date: data.end_date || endDate,
          doctor_count: data.doctor_count || 0
        };
      }).catch(error => {
        console.error('Error fetching doctor performance:', error);
        showApiError(this, '获取医生业绩统计', error)
      }).finally(() => {
        this.doctorPerformanceLoading = false;
      });
    },
    normalizeDoctorPerformanceRange() {
      const fallbackRange = createDefaultDoctorPerformanceRange();
      const range = Array.isArray(this.doctorPerformanceRange) ? this.doctorPerformanceRange : [];
      const startDate = moment(range[0], 'YYYY-MM-DD', true).isValid() ? range[0] : fallbackRange[0];
      const endDate = moment(range[1], 'YYYY-MM-DD', true).isValid() ? range[1] : fallbackRange[1];
      if (moment(startDate).isAfter(endDate)) {
        this.$message.warning('结束日期不能早于开始日期');
        return null;
      }
      this.doctorPerformanceRange = [startDate, endDate];
      return this.doctorPerformanceRange;
    },
    resetDoctorPerformanceRange() {
      this.doctorPerformanceRange = createDefaultDoctorPerformanceRange();
      this.fetchDoctorPerformance();
    },
    reset() {
      this.searchType = 'id';
      this.keyword = '';
      this.currentPage = 1;
      this.pageSize = 5;
      this.search();
    },
    handleSizeChange(size) {
      this.pageSize = size;
      this.search();
    },
    handleCurrentChange(page) {
      this.currentPage = page;
      this.search();
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
      axios.post('/finances/add', this.editItem)
          .then(() => {
            this.$message.success('新增成功');
            this.dialogVisible = false;
            this.search();
            this.fetchDoctorPerformance();
          })
          .catch(error => {
            console.error('Error adding new item:', error);
            this.$message.error('新增失败');
          });
    },
    handleEdit(row) {
      this.editItem = Object.assign({}, row);
      this.isEditing = true;
      this.dialogVisible = true;
    },
    handleSaveEdit() {
      if (this.isEditing) {
        axios.put('/finances/edit', this.editItem)
            .then(() => {
              this.$message.success('编辑成功');
              this.closeDialog();
              this.search();
              this.fetchDoctorPerformance();
            })
            .catch(error => {
              console.error('Error editing item:', error);
              this.$message.error('编辑失败');
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
            .then(() => {
              this.$message.success('删除成功');
              this.search();
              this.fetchDoctorPerformance();
            })
            .catch(error => {
              console.error('删除失败', error);
              this.$message.error('删除失败');
            });
      }).catch(() => {
        this.$message.info('已取消删除');
      });
    },
    formatMoney(value) {
      const amount = Number(value || 0);
      return Number.isFinite(amount) ? amount.toFixed(2) : '0.00';
    },
    buildDoctorPerformanceSummary({ columns }) {
      const summary = this.doctorPerformanceSummary || createEmptyDoctorPerformanceSummary();
      return columns.map((column, index) => {
        if (index === 0) {
          return '合计';
        }
        switch (column.property) {
          case 'project_count':
            return summary.project_count || 0;
          case 'turnover_amount':
            return `¥${this.formatMoney(summary.turnover_amount)}`;
          case 'received_amount':
            return `¥${this.formatMoney(summary.received_amount)}`;
          case 'refunded_amount':
            return `¥${this.formatMoney(summary.refunded_amount)}`;
          case 'arrears_amount':
            return `¥${this.formatMoney(summary.arrears_amount)}`;
          default:
            return '';
        }
      });
    },
  }
};
</script>

<style scoped>
.financial-page {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.doctor-performance-card {
  margin-top: 6px;
}

.doctor-performance-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.doctor-performance-title {
  font-size: 16px;
  font-weight: 700;
  color: #303133;
}

.doctor-performance-tip {
  margin-top: 8px;
  color: #909399;
  line-height: 1.6;
  max-width: 720px;
}

.doctor-performance-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.doctor-performance-summary {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.summary-item {
  padding: 14px 16px;
  border-radius: 12px;
  background: #f8fafc;
  border: 1px solid #e5eaf3;
}

.summary-label {
  color: #909399;
  font-size: 12px;
}

.summary-value {
  margin-top: 8px;
  color: #303133;
  font-size: 22px;
  font-weight: 700;
}

.doctor-performance-range-text {
  margin-bottom: 12px;
  color: #606266;
  font-size: 13px;
}

.dialog-footer {
  text-align: right;
}

@media (max-width: 1200px) {
  .doctor-performance-header {
    flex-direction: column;
  }

  .doctor-performance-actions {
    justify-content: flex-start;
  }
}
</style>
