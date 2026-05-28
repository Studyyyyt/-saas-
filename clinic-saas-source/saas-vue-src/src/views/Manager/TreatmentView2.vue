<template>
  <div style="height: 100%; width: 100%">
    <!-- 查询框 -->
    <div style="display: flex; justify-content: space-between; align-items: center;">
      <div>
        <el-select v-model="searchType" placeholder="请选择查询条件" style="width: 150px;">
          <el-option label="序号" value="id"></el-option>
          <el-option label="姓名" value="name"></el-option>
        </el-select>
        <el-input v-model="keyword" style="width: 300px; margin-left: 10px; margin-right: 10px" placeholder="请输入关键词"></el-input>
        <el-button type="primary" @click="search">查询</el-button>
        <el-button type="info" @click="reset">重置</el-button>
      </div>
    </div>

    <!-- 表格 -->
    <div style="margin: 10px 0">
      <el-table :data="treatments" stripe :header-cell-style="{ backgroundColor: 'aliceblue', color: '#666' }"
                @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center"></el-table-column>
        <el-table-column prop="id" label="序号" width="70" align="center"></el-table-column>
        <el-table-column prop="patient_name" label="患者姓名"></el-table-column>
        <el-table-column prop="appointment_purpose" label="预约目的"></el-table-column>
        <el-table-column prop="status" label="状态"></el-table-column>
        <el-table-column prop="doctor_name" label="医生姓名"></el-table-column>
        <el-table-column prop="treatment_date" label="治疗日期"></el-table-column>
        <el-table-column prop="treatment_content" label="治疗内容"></el-table-column>
        <el-table-column prop="treatment_fee" label="治疗金额"></el-table-column>
        <el-table-column label="操作" align="center" width="220">
          <template slot-scope="scope">
            <el-button size="mini" type="primary" plain @click="handleEdit(scope.row)">查看</el-button>
            <el-button size="mini" type="danger" plain @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页组件 -->
    <div style="margin-top: 20px; text-align: center;">
      <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="currentPage"
          :page-sizes="[5, 10, 20, 50]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="totalItems">
      </el-pagination>

      <!-- 编辑弹窗 -->
      <el-dialog :title="title" :visible.sync="dialogVisible" width="30%">
        <el-form ref="form" :model="editItem" label-width="80px">
          <el-form-item label="患者姓名" prop="patient_name">
            <el-input v-model="editItem.patient_name"></el-input>
          </el-form-item>
          <el-form-item label="预约目的" prop="appointment_purpose">
            <el-input v-model="editItem.appointment_purpose"></el-input>
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-input v-model="editItem.status"></el-input>
          </el-form-item>
          <el-form-item label="医生姓名" prop="doctor_account_id">
            <el-select v-model="editItem.doctor_account_id" placeholder="请选择医生" style="width: 100%">
              <el-option v-for="doctor in doctors" :key="doctor.id" :label="doctor.name" :value="doctor.id"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="治疗日期" prop="treatment_date">
            <el-input v-model="editItem.treatment_date"></el-input>
          </el-form-item>
          <el-form-item label="治疗方案" prop="treatment_content">
            <el-input type="textarea" v-model="editItem.treatment_content" >
            </el-input>
          </el-form-item>
          <el-form-item label="治疗金额" prop="treatment_fee">
            <el-input v-model="editItem.treatment_fee"></el-input>
          </el-form-item>

        </el-form>
<!--        <span slot="footer" class="dialog-footer">-->
<!--          <el-button @click="closeDialog">取消</el-button>-->
<!--          <el-button type="primary" @click="handleSaveEdit">保存</el-button>-->
<!--        </span>-->
      </el-dialog>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
import { showApiError } from '@/utils/errorMessage'

export default {
  name: 'InventoryView2',
  data() {
    return {
      treatments: [],
      selectedRows: [], // 存储选中的行的 ID
      currentPage: 1, // 当前页数
      pageSize: 5, // 每页显示的条数
      totalItems: 0, // 总条数
      searchType: 'id', // 默认查询类型
      keyword: '', // 查询关键词
      title:'治疗方案',
      dialogVisible: false, // 新增/编辑弹窗可见性
      doctors: [],
      editItem: { // 编辑/新增的数据
        patient_name: '',
        appointment_purpose: '',
        doctor_account_id: null,
        doctor_name:'',
        status: '',
        treatment_date:'',
        treatment_content: '',
        treatment_fee: '',
      },
    };
  },
  computed: {

  },
  mounted() {
    this.search();
    this.loadDoctors();
  },
  methods: {
    loadDoctors() {
      axios.get('/accounts/doctors/active')
          .then(response => {
            this.doctors = Array.isArray(response.data.data) ? response.data.data : [];
          })
          .catch(error => {
            console.error('Error fetching doctors:', error);
            this.doctors = [];
          });
    },
    currentDoctorById(id) {
      return (this.doctors || []).find(item => Number(item.id) === Number(id)) || null;
    },
    search() {
      let url = '/treatments/selectAll';
      let params = {
        page: this.currentPage,
        size: this.pageSize
      };

      if (this.keyword) {
        url = `/treatments/selectBy${this.searchType}`;
        params[this.searchType] = this.keyword;
      }

      axios.get(url, { params })
          .then(response => {
            const result = response.data;
            if (result.code !== '200') {
              this.$message.error((res.data.msg || '获取治疗记录失败') + '，请刷新页面重试。如问题持续，请联系管理员。')
              return;
            }
            const data = result.data || {};
            this.treatments = (data.list || []).map(item => ({
              ...item,
              appointment_purpose: this.normalizeAppointmentPurpose(item.appointment_purpose),
            }));
            this.totalItems = data.total || 0;
          })
          .catch(error => {
            console.error('Error fetching treatment:', error);
            showApiError(this, '获取治疗记录', error)
          });
    },
    reset() {//重置
      this.searchType = 'id';
      this.keyword = '';
      this.currentPage = 1;
      this.pageSize = 5;
      this.search();
    },
    normalizeAppointmentPurpose(value) {
      if (value === null || value === undefined) {
        return '';
      }
      if (typeof value === 'number') {
        return '异常预约目的（待人工核对）';
      }
      const text = String(value).trim();
      if (!text) {
        return '';
      }
      if (/^历史异常值-\d+$/.test(text)) {
        return '历史异常数据（待人工核对）';
      }
      if (/^\d+$/.test(text)) {
        return '异常预约目的（待人工核对）';
      }
      if ((text.startsWith('{') && text.endsWith('}')) || (text.startsWith('[') && text.endsWith(']'))) {
        try {
          const parsed = JSON.parse(text);
          if (Array.isArray(parsed)) {
            return parsed.map(item => this.normalizeAppointmentPurpose(item)).filter(Boolean).join(' / ');
          }
          if (typeof parsed === 'object') {
            return this.normalizeAppointmentPurpose(parsed.appointment_purpose || parsed.预约目的 || parsed.reason || parsed.name || JSON.stringify(parsed));
          }
        } catch (e) {
          return text;
        }
      }
      return text;
    },
    handleSizeChange(size) {
      this.pageSize = size;
      this.search();
    },
    handleCurrentChange(page) {
      this.currentPage = page;
      this.search();
    },
    handleSelectionChange(val) {//选择的行
      this.selectedRows = val.map(row => row.id);
    },
    closeDialog() {
      this.dialogVisible = false;
      this.isEditing = false;
    },
    handleEdit(row) {
      // 将选中行的数据赋值给编辑的数据项
      const matchedDoctor = this.currentDoctorById(row.doctor_account_id) || (this.doctors || []).find(item => item.name === String(row.doctor_name || '').trim());
      this.editItem = Object.assign({}, row, {
        doctor_account_id: matchedDoctor ? matchedDoctor.id : (row.doctor_account_id ? Number(row.doctor_account_id) : null),
        doctor_name: matchedDoctor ? matchedDoctor.name : String(row.doctor_name || '').trim(),
      });
      // 打开新增/编辑弹窗
      this.dialogVisible = true;
    },
    // handleSaveEdit() {
    //     // 编辑状态下发送编辑请求
    //     axios.put("/treatments/edit", this.editItem)
    //         .then(response => {
    //           // 处理编辑成功逻辑
    //           this.$message.success("编辑成功");
    //           this.closeDialog();
    //           this.search(); // 刷新数据
    //         })
    //         .catch(error => {
    //           // 处理编辑失败逻辑
    //           console.error('Error editing item:', error);
    //           this.$message.error("编辑失败");
    //         });
    //
    // },
    handleDelete(id) {
      this.$confirm('此操作将永久删除该条目，是否继续？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        axios.delete(`/treatments/delete/${id}`)
            .then(response => {
              this.$message.success('删除成功');
              this.search(); // 刷新数据
            })
            .catch(error => {
              console.error('Error deleting item:', error);
              this.$message.error('删除失败');
            });
      }).catch(() => {
        this.$message.info('已取消删除');
      });
    }
  }
};
</script>

<style scoped>
.dialog-footer {
  text-align: right;
}

</style>
