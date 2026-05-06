<template>
  <div style="width: 100%; height: 100%">
    <!-- 查询框 -->
    <div>
      <el-select v-model="searchType" placeholder="请选择查询条件" style="width: 150px;">
        <el-option label="序号" value="id"></el-option>
        <el-option label="姓名" value="name"></el-option>
      </el-select>
      <el-input v-model="keyword" style="width: 300px; margin-left: 10px; margin-right: 10px" placeholder="请输入关键词"></el-input>
      <el-button type="primary" @click="fetchAccounts">查询</el-button>
      <el-button type="info" @click="reset">重置</el-button>
    </div>

    <!-- 操作框 -->
    <div style="margin: 10px 0">
      <el-button type="primary" plain @click="showAddDialog">新增</el-button>
      <el-button type="danger" plain @click="delBatch">批量删除</el-button>
    </div>

    <!-- 账号列表 -->
    <!-- 表格 -->
    <div style="margin: 10px 0">
      <el-table :data="accounts" stripe :header-cell-style="{ backgroundColor: 'aliceblue', color: '#666' }"
                @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center"></el-table-column>
        <el-table-column prop="id" label="序号" width="70" align="center"></el-table-column>
        <el-table-column prop="username" label="账号名称"></el-table-column>
        <el-table-column prop="name" label="姓名"></el-table-column>
        <el-table-column prop="roleLabel" label="角色"></el-table-column>
        <el-table-column label="微信绑定" width="240">
          <template slot-scope="scope">
            <div v-if="scope.row.wechat_openid">
              <el-tag size="mini" type="success">已绑定</el-tag>
              <div style="margin-top: 6px; color: #606266; font-size: 12px; word-break: break-all;">{{ scope.row.wechat_openid }}</div>
            </div>
            <el-tag v-else size="mini" type="info">未绑定</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="180">
          <template slot-scope="scope">
            <el-button size="mini" type="primary" plain @click="handleEdit(scope.row)">编辑</el-button>
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
      </div>

    <!-- 新增/编辑账号模态框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="30%">
      <el-form :model="editItem" label-width="100px">
        <el-form-item label="账号名称">
          <el-input v-model="editItem.username"></el-input>
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="editItem.name"></el-input>
        </el-form-item>
        <el-form-item label="密码">
          <el-input type="password" v-model="editItem.password"></el-input>
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="editItem.role" placeholder="请选择角色">
            <el-option label="管理员" value="admin"></el-option>
            <el-option label="医生" value="doctor"></el-option>
            <el-option label="护士" value="nurse"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="微信OpenID">
          <el-input v-model="editItem.wechat_openid" placeholder="可选，用于员工微信绑定"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="closeDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSaveEdit">{{ isEditing ? '保存' : '新增' }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import axios from "axios";

export default {
  data() {
    return {
      searchAccount: '',
      accounts: [], // 账号信息列表
      currentPage: 1, // 当前页数
      pageSize: 5, // 每页显示的条数
      totalItems: 0, // 总条数
      searchType: 'id', // 默认查询类型
      keyword: '', // 查询关键词
      dialogVisible: false, // 账号信息模态框可见性
      editItem: { // 编辑/新增的数据
        // id: '',
        username: '',
        password: '',
        role: '',
        name:'',
        wechat_openid: ''
      },
      isEditing: false, // 是否为新增模式
    };
  },
  computed: {
    dialogTitle() {
      return this.isEditing ? '编辑信息' : '新增信息'; //false为新增，true为编辑
    }
  },
  mounted() {
    this.fetchAccounts();
  },
  methods: {
    // 获取账号信息
    fetchAccounts() {

      let url = '/accounts/search';
      let params = {
        page: this.currentPage,
        size: this.pageSize
      };

      if (this.keyword) {
        url = `/accounts/selectBy${this.searchType}?${this.searchType}=${this.keyword}`;
      }

      axios.get(url,{ params } )
          .then(response => {
            const data = response.data.data;
            this.accounts = (data.list || []).map(item => ({
              ...item,
              roleLabel: this.formatRole(item.role)
            }));
            this.totalItems = data.total;
            this.normalizePagination();
          })
          .catch(error => {
            console.error('Error fetching accounts:', error);
          });
    },
    reset() {//重置
      this.searchType = 'id';
      this.keyword = '';
      this.currentPage = 1;
      this.pageSize = 5;
      this.fetchAccounts();
    },
    handleSizeChange(size) {
      this.pageSize = size;
      this.fetchAccounts();
    },
    handleCurrentChange(page) {
      this.currentPage = page;
      this.fetchAccounts();
    },
    handleSelectionChange(val) {//选择的行
      this.selectedRows = val.map(row => row.id);
    },
    showAddDialog() {
      this.isEditing = false;
      this.editItem = { // 新增表单置空
        // id: '',
        username: '',
        password: '',
        role: '',
        name:'',
        wechat_openid: ''
      };
      this.dialogVisible = true;
    },
    closeDialog() {
      this.dialogVisible = false;
      this.isEditing = false;
    },
    handleAdd() {
      const validationMessage = this.validateAccountForm();
      if (validationMessage) {
        this.$message.warning(validationMessage);
        return;
      }
      axios.post("/accounts/add", this.editItem)

          .then(response => {
            if (response.data.code !== '200') {
              this.$message.error(response.data.msg || "新增失败");
              return;
            }
            this.$message.success("新增成功");
            this.dialogVisible = false;
            this.fetchAccounts();
          })
          .catch(error => {
            console.error('Error adding new item:', error);
            this.$message.error("新增失败");
          });
    },
    handleEdit(row) {
      // 将选中行的数据赋值给编辑的数据项
      this.editItem = Object.assign({}, row);
      // 设置编辑状态为true
      this.isEditing = true; // 设置编辑状态为true，表示编辑
      // 打开新增/编辑弹窗
      this.dialogVisible = true;
    },
    handleSaveEdit() {
      const validationMessage = this.validateAccountForm(this.isEditing);
      if (validationMessage) {
        this.$message.warning(validationMessage);
        return;
      }
      if (this.isEditing) {
        axios.put("/accounts/edit", this.editItem)
            .then(response => {
              if (response.data.code !== '200') {
                this.$message.error(response.data.msg || "编辑失败");
                return;
              }
              this.$message.success("编辑成功");
              this.closeDialog();
              this.fetchAccounts();
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
        axios.delete(`/accounts/delete/${id}`)
            .then(response => {
              this.$message.success('删除成功');
              this.afterDeleteAdjustPage();
              this.fetchAccounts();
            })
            .catch(error => {
              console.error('Error deleting item:', error);
              this.$message.error('删除失败');
            });
      }).catch(() => {
        this.$message.info('已取消删除');
      });
    },
    delBatch() {
      // 确认是否删除选中的行
      this.$confirm('此操作将永久删除所选条目，是否继续？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 批量删除操作
        axios.delete(`/accounts/deleteBatch`, {  data: this.selectedRows })
            .then(response => {
              this.$message.success('批量删除成功');
              this.afterDeleteAdjustPage(this.selectedRows.length);
              this.fetchAccounts();
              this.selectedRows = [];
            })
            .catch(error => {
              console.error('Error deleting batch:', error);
              this.$message.error('批量删除失败');
            });
      }).catch(() => {
        this.$message.info('已取消批量删除');
      });
    },
    validateAccountForm(isEditing = false) {
      if (!this.editItem.username || !String(this.editItem.username).trim()) return '账号名称必填';
      if (!this.editItem.name || !String(this.editItem.name).trim()) return '姓名必填';
      if (!isEditing && (!this.editItem.password || !String(this.editItem.password).trim())) return '密码必填';
      if (!this.editItem.role || !String(this.editItem.role).trim()) return '角色必填';
      return '';
    },
    formatRole(role) {
      const roleMap = {
        admin: '管理员',
        doctor: '医生',
        nurse: '护士'
      };
      return roleMap[role] || role || '-';
    },
    normalizePagination() {
      const maxPage = Math.max(1, Math.ceil((this.totalItems || 0) / this.pageSize));
      if (this.currentPage > maxPage) {
        this.currentPage = maxPage;
        this.fetchAccounts();
      }
    },
    afterDeleteAdjustPage(deletedCount = 1) {
      const remaining = Math.max(0, this.totalItems - deletedCount);
      const maxPage = Math.max(1, Math.ceil(remaining / this.pageSize));
      if (this.currentPage > maxPage) {
        this.currentPage = maxPage;
      }
    }
    // // 显示新增账号模态框
    // showAddModal() {
    //   this.currentAccount = {id: null, username: '', password: '', role: ''};
    //   this.isAdding = true;
    //   this.accountModalVisible = true;
    // },
    // // 编辑账号信息
    // editAccount(account) {
    //   this.currentAccount = {...account};
    //   this.isAdding = false;
    //   this.accountModalVisible = true;
    // },
    // // 保存或新增账号信息
    // saveAccount() {
    //   if (this.isAdding) {
    //     axios.post('/accounts/add', this.currentAccount)
    //         .then(() => {
    //           this.accountModalVisible = false;
    //           this.fetchAccounts();
    //         })
    //         .catch(error => {
    //           console.error('Error adding account:', error);
    //         });
    //   } else {
    //     axios.put(`/accounts/update/${this.currentAccount.id}`, this.currentAccount)
    //         .then(() => {
    //           this.accountModalVisible = false;
    //           this.fetchAccounts();
    //         })
    //         .catch(error => {
    //           console.error('Error updating account:', error);
    //         });
    //   }
    // },
    // // 删除账号
    // deleteAccount(id) {
    //   axios.delete(`/accounts/delete/${id}`)
    //       .then(() => {
    //         this.fetchAccounts();
    //       })
    //       .catch(error => {
    //         console.error('Error deleting account:', error);
    //       });
    // }
  }
};
</script>

<style scoped>
.table-container {
  margin-top: 20px;
}
</style>
