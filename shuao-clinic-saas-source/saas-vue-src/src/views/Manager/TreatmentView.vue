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
      <el-table :data="appointments" stripe :header-cell-style="{ backgroundColor: 'aliceblue', color: '#666' }"
                @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center"></el-table-column>
        <el-table-column prop="id" label="序号" width="70" align="center"></el-table-column>
        <el-table-column prop="patient_name" label="患者姓名"></el-table-column>
        <el-table-column prop="appointment_date" label="预约日期"></el-table-column>
        <el-table-column prop="appointment_time" label="预约时间"></el-table-column>
        <el-table-column prop="appointment_purpose" label="预约目的"></el-table-column>
        <el-table-column prop="status" label="状态"></el-table-column>
        <el-table-column label="操作" align="center" width="220">
          <template slot-scope="scope">
            <el-button size="mini" type="primary" plain @click="handleEdit(scope.row)">治疗</el-button>
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
        <!--实现分页功能的，通过分页组件可以让用户方便地切换不同的页码和调整每页显示的条目数量，从而更好地浏览大量数据-->
      </el-pagination>
      <!-- 治疗弹窗 -->
      <el-dialog :title="title" :visible.sync="dialogVisible" width="30%">
        <el-steps :active="activeStep">
          <el-step title="预约信息"></el-step>
          <el-step title="治疗计划"></el-step>
          <el-step title="治疗清单"></el-step>
        </el-steps>
        <el-form ref="form" :model="editItem" label-width="80px">
          <!-- 步骤一 -->
          <div v-show="activeStep === 0">
            <el-form-item label="患者姓名" prop="patient_name">
              <el-input v-model="editItem.patient_name"></el-input>
            </el-form-item>
            <el-form-item label="预约目的" prop="appointment_purpose">
              <el-input v-model="editItem.appointment_purpose"></el-input>
            </el-form-item>
            <el-form-item label="状态" prop="status">
              <el-input v-model="editItem.status"></el-input>
            </el-form-item>
            <el-form-item label="默认医生" prop="doctor_account_id">
              <el-select v-model="editItem.doctor_account_id" placeholder="请选择默认医生" style="width: 100%" @change="handleDefaultDoctorChange">
                <el-option v-for="doctor in doctors" :key="doctor.id" :label="doctor.name" :value="doctor.id"></el-option>
              </el-select>
            </el-form-item>
          </div>

          <!-- 步骤二 -->
          <div v-show="activeStep === 1">
            <el-form-item label="治疗日期" prop="treatment_date">
              <el-date-picker v-model="editItem.treatment_date" type="date" placeholder="选择日期" value-format="yyyy-MM-dd"></el-date-picker>
            </el-form-item>
            <el-form-item label="治疗方案" prop="treatment_content">
              <el-checkbox-group v-model="selectedTreatmentPlans">
                <el-checkbox v-for="plan in treatmentPlans" :key="plan.id" :label="plan">
                  {{ plan.treatment_content }}
                </el-checkbox>
              </el-checkbox-group>
            </el-form-item>
            <div v-for="(plan, index) in selectedTreatmentPlans" :key="index" style="margin-top: 10px;">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="处置医生">
                    <el-select v-model="plan.doctor_account_id" placeholder="请选择处置医生" style="width: 100%" @change="handlePlanDoctorChange(plan)">
                      <el-option v-for="doctor in doctors" :key="doctor.id" :label="doctor.name" :value="doctor.id"></el-option>
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="金额">
                    <el-input v-model="plan.treatment_free" readonly></el-input>
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
            <el-form-item label="总金额" prop="treatment_fee">
              <el-input v-model="editItem.treatment_fee" readonly></el-input>
            </el-form-item>
          </div>



          <!-- 步骤三 -->
          <div v-show="activeStep === 2">
            <div>患者姓名：{{ editItem.patient_name }}</div>
            <div>预约目的：{{ editItem.appointment_purpose }}</div>
            <div>状态：{{ editItem.status }}</div>
            <div>医生姓名：{{ editItem.doctor_name }}</div>
            <div>治疗日期：{{ editItem.treatment_date }}</div>
            <div>治疗方案：</div>
            <ul>
              <li v-for="plan in selectedTreatmentPlans" :key="plan.id">
                {{ plan.treatment_content }} -- 医生：{{ plan.doctor_name || '未指定' }} -- 金额：{{ plan.treatment_free }}
              </li>
            </ul>
            <div>总金额：{{ editItem.treatment_fee }}</div>
          </div>
        </el-form>
        <span slot="footer" class="dialog-footer">
          <el-button v-show="activeStep > 0" @click="prevStep">上一步</el-button>
          <el-button v-show="activeStep < 2" type="primary" @click="nextStep">下一步</el-button>
          <el-button v-show="activeStep === 2" type="primary" @click="handleSaveEdit">保存</el-button>
          <el-button @click="closeDialog">取消</el-button>
        </span>
      </el-dialog>
    </div>
  </div>
</template>
<script>
import axios from 'axios';
import { showApiError } from '@/utils/errorMessage'

export default {
  name: 'TreatmentPlan',
  data() {
    return {
      appointments: [],
      treatments: [],
      treatmentPlans: [],
      doctors: [],
      selectedTreatmentPlans: [],
      selectedRows: [],
      currentPage: 1,//当前页数
      pageSize: 5,
      totalItems: 0,//总条数
      searchType: 'id',
      keyword: '',
      title: '治疗方案',
      dialogVisible: false,
      editItem: {
        patient_id: null,
        patient_name: '',
        appointment_purpose: '',
        status: '',
        doctor_account_id: null,
        doctor_name: '',
        treatment_date: '',
        treatment_content: '',
        treatment_fee: '',
      },
      activeStep: 0,//步骤条当前步骤
    };
  },

  mounted() {
    this.search();//按条件查询
    this.loadTreatmentPlans()//加载治疗方案
    this.loadDoctors()
  },
  methods: {
    loadDoctors() {
      axios.get('/accounts/doctors/active')
          .then(response => {
            this.doctors = Array.isArray(response.data.data) ? response.data.data : []
          })
          .catch(error => {
            console.error('Error fetching doctors:', error)
            this.doctors = []
          })
    },
    loadTreatmentPlans() {
      axios.get('/treatment-plans/selectAll')
          .then(response => {
            this.treatmentPlans = response.data.data;
          })
          .catch(error => {
            console.error('Error fetching treatment plans:', error);
          });
    },
    normalizeAppointmentPurpose(value) {
      if (value === null || value === undefined) return ''
      const text = String(value).trim()
      if (!text) return ''
      if (/^历史异常值-\d+$/.test(text)) return '历史异常数据（待人工核对）'
      if (/^\d+$/.test(text)) return '异常预约目的（待人工核对）'
      return text
    },
    search() {
      let url = '/appointments/selectAll';
      let params = {
        page: this.currentPage,
        size: this.pageSize,
        status: '待治疗'
      };

      if (this.keyword) {
        url = `/appointments/selectBy${this.searchType}`;
        params[this.searchType] = this.keyword;
      }

      axios.get(url, {params})
          .then(response => {
            const result = response.data;
            if (result.code !== '200') {
              this.$message.error((res.data.msg || '获取待治疗预约失败') + '，请刷新页面重试。如问题持续，请联系管理员。')
              return;
            }
            const data = result.data || {};
            this.appointments = (data.list || []).filter(appointment => appointment.status === '待治疗').map(appointment => ({
              ...appointment,
              appointment_purpose: this.normalizeAppointmentPurpose(appointment.appointment_purpose),
            }));
            this.totalItems = this.appointments.length;
          })
          .catch(error => {
            console.error('Error fetching appointments:', error);
            showApiError(this, '获取待治疗预约', error)
          });
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
      this.selectedRows = val.map(row => row.id);//提取选中的id
    },
    closeDialog() {
      this.dialogVisible = false;
      this.isEditing = false;
      this.activeStep = 0;
      this.selectedTreatmentPlans = [];
    },
    handleEdit(row) {
      this.selectedTreatmentPlans = [];
      this.activeStep = 0;
      this.editItem = Object.assign({ patient_id: null, doctor_account_id: row.doctor_account_id || null }, row);
      this.dialogVisible = true;
    },
    resolveDoctorName(doctorAccountId) {
      const matchedDoctor = (this.doctors || []).find(item => Number(item.id) === Number(doctorAccountId));
      if (matchedDoctor && matchedDoctor.name) {
        return matchedDoctor.name;
      }
      return this.editItem.doctor_name || '';
    },
    handleDefaultDoctorChange() {
      this.editItem.doctor_name = this.resolveDoctorName(this.editItem.doctor_account_id);
    },
    handlePlanDoctorChange(plan) {
      if (!plan) return;
      plan.doctor_name = this.resolveDoctorName(plan.doctor_account_id);
    },
    syncSelectedTreatmentPlanDefaults() {
      this.selectedTreatmentPlans.forEach(plan => {
        if (!plan.doctor_account_id) {
          plan.doctor_account_id = this.editItem.doctor_account_id || null;
        }
        if (!plan.doctor_name || !String(plan.doctor_name).trim()) {
          plan.doctor_name = this.resolveDoctorName(plan.doctor_account_id);
        }
      });
    },
    handleSaveEdit() {
      if (!this.editItem.patient_id) {
        this.$message.warning('当前预约缺少患者ID，请先在预约管理中重新选择患者后再治疗');
        return;
      }
      if (!this.selectedTreatmentPlans.length) {
        this.$message.warning('请至少选择一条治疗方案');
        return;
      }
      if (!this.editItem.treatment_date) {
        this.$message.warning('请选择治疗日期');
        return;
      }
      this.syncSelectedTreatmentPlanDefaults();
      for (let index = 0; index < this.selectedTreatmentPlans.length; index += 1) {
        const plan = this.selectedTreatmentPlans[index];
        if (!plan.doctor_account_id) {
          this.$message.warning(`第${index + 1}条处置缺少处置医生`);
          return;
        }
      }
      const treatmentData = {
        patient_id: this.editItem.patient_id,
        patient_name: this.editItem.patient_name,
        doctor_account_id: this.editItem.doctor_account_id || null,
        doctor_name: this.editItem.doctor_name,
        treatment_date: this.editItem.treatment_date,
        status: "已治疗",
        discounted_total_fee: Number(this.editItem.treatment_fee || 0),
        items: this.selectedTreatmentPlans.map(plan => ({
          doctor_account_id: plan.doctor_account_id || null,
          doctor_name: plan.doctor_name || '',
          appointment_purpose: plan.treatment_content || this.editItem.appointment_purpose,
          treatment_content: plan.treatment_content || '',
          tooth_positions: '',
          treatment_fee: Number(plan.treatment_free || 0),
        })),
      };

      axios.post('/treatments/batchAdd', treatmentData)
          .then(response => {
            const result = response.data;
            if (result.code !== '200') {
              this.$message.error(result.msg || '治疗信息保存失败');
              return;
            }
            this.$message.success("治疗信息保存成功");
            this.closeDialog();

            const createdTreatments = Array.isArray(result.data) ? result.data : [];
            Promise.all(createdTreatments
                .filter(item => item && item.id)
                .map(item => axios.post(`/treatments/charge/${item.id}`, {
                  date: this.editItem.treatment_date,
                  remark: '来源：治疗计划页自动收费'
                })))
                .catch(error => {
                  console.error('Error charging treatments:', error);
                  this.$message.error('治疗已保存，但自动收费有失败记录');
                });

            axios.put(`/appointments/updateStatus/${this.editItem.id}`, {status: '已治疗'})
                .then(response => {
                  this.$message.success("预约记录更新成功");
                  this.search();
                })
                .catch(error => {
                  console.error('Error updating appointment status:', error);
                  this.$message.error("预约记录更新失败");
                });
          })
          .catch(error => {
            console.error('Error saving treatment:', error);
            this.$message.error("治疗信息保存失败");
          });
    },
    handleDelete(id) {
      this.$confirm('此操作将永久删除该条目，是否继续？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        axios.delete(`/appointments/delete/${id}`)
            .then(response => {
              this.$message.success('删除成功');
              this.search();
            })
            .catch(error => {
              console.error('Error deleting item:', error);
              this.$message.error('删除失败');
            });
      }).catch(() => {
        this.$message.info('已取消删除');
      });
    },
    nextStep() {
      if (this.activeStep === 0) {
        this.handleDefaultDoctorChange();
      }
      if (this.activeStep === 1) {
        this.syncSelectedTreatmentPlanDefaults();
      }
      this.activeStep += 1;
    },
    prevStep() {
      this.activeStep -= 1;
    },
    calculateFee() {
      let totalFee = 0;
      this.selectedTreatmentPlans.forEach(plan => {
        totalFee += parseFloat(plan.treatment_free);// 确保金额是浮点数
      });
      this.editItem.treatment_fee = totalFee.toFixed(2);
      this.syncSelectedTreatmentPlanDefaults();
    }
  },

    watch: {//被调用来重新计算费用
    'selectedTreatmentPlans': {
      handler: 'calculateFee',
      deep: true
    }
  }
};
</script>
<style scoped>
.dialog-footer {
  text-align: right;
}
</style>
