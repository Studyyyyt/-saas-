<template>
  <div style="height: 100%; width: 100%">
    <!-- 查询框 -->
    <div>
      <el-select v-model="searchType" placeholder="请选择查询条件" style="width: 150px;">
        <el-option label="序号" value="id"></el-option>
        <el-option label="产品名称" value="name"></el-option>
        <el-option label="类别" value="category"></el-option>
        <el-option label="品牌" value="brand"></el-option>
        <el-option label="供应商" value="supplier"></el-option>
      </el-select>
      <el-input v-model="keyword" style="width: 300px; margin-left: 10px; margin-right: 10px" placeholder="请输入关键词"></el-input>
      <el-button type="primary" @click="search">查询</el-button>
      <el-button type="info" @click="reset">重置</el-button>
    </div>
    <!-- 操作框 -->
    <div style="margin: 10px 0; display: flex; align-items: center;">
      <el-button type="primary" plain @click="showAddDialog">新增</el-button>
      <el-button type="danger" plain @click="delBatch">批量删除</el-button>
      <!-- 文件上传 -->
        <el-upload
            ref="upload"
            action=""
            :before-upload="handleBeforeUpload"
            :show-file-list="false"
            accept=".xlsx, .xls"
            style="margin-left: 10px;"
        >
          <el-button type="primary" plain>导入Excel文件</el-button>
        </el-upload>
      <el-button style="margin-left: 10px;" type="info" plain @click="exportExcel">导出 Excel</el-button>
    </div>
    <!-- 表格 -->
    <div>
      <el-table :data="inventory" stripe :header-cell-style="{ backgroundColor: 'aliceblue', color: '#666' }"
                @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center"></el-table-column>
        <el-table-column prop="id" label="序号" width="70" align="center"></el-table-column>
        <el-table-column prop="product_name" label="产品名称"></el-table-column>
        <el-table-column prop="category" label="类别"></el-table-column>
        <el-table-column prop="brand" label="品牌"></el-table-column>
        <el-table-column prop="supplier" label="供应商"></el-table-column>
        <el-table-column prop="specification" label="规格"></el-table-column>
        <el-table-column prop="unit" label="单位"></el-table-column>
        <el-table-column prop="quantity" label="数量"></el-table-column>
        <el-table-column prop="price" label="价格"></el-table-column>
        <el-table-column prop="product_batch" label="产品批次"></el-table-column>
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

      <!-- 新增/编辑弹窗 -->
      <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="30%">
        <el-form ref="form" :model="editItem" label-width="80px">
          <el-form-item label="产品名称" prop="product_name">
            <el-input v-model="editItem.product_name"></el-input>
          </el-form-item>
          <el-form-item label="类别" prop="category">
            <el-input v-model="editItem.category"></el-input>
          </el-form-item>
          <el-form-item label="品牌" prop="brand">
            <el-input v-model="editItem.brand"></el-input>
          </el-form-item>
          <el-form-item label="供应商" prop="supplier">
            <el-input v-model="editItem.supplier"></el-input>
          </el-form-item>
          <el-form-item label="规格" prop="specification">
            <el-input v-model="editItem.specification"></el-input>
          </el-form-item>
          <el-form-item label="单位" prop="unit">
            <el-input v-model="editItem.unit"></el-input>
          </el-form-item>
          <el-form-item label="数量" prop="quantity">
            <el-input v-model="editItem.quantity"></el-input>
          </el-form-item>
          <el-form-item label="价格" prop="price">
            <el-input v-model="editItem.price"></el-input>
          </el-form-item>
          <el-form-item label="产品批次" prop="product_batch">
            <el-input v-model="editItem.product_batch"></el-input>
          </el-form-item>
        </el-form>
        <span slot="footer" class="dialog-footer">
          <el-button @click="closeDialog">取消</el-button>
          <el-button type="primary" @click="handleSaveEdit">{{ isEditing ? '保存' : '新增' }}</el-button>
        </span>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
import * as XLSX from 'xlsx';

export default {
  name: 'InventoryView2',
  data() {
    return {
      inventory: [], // 确保初始化为空数组
      selectedRows: [], // 存储选中的行的 ID
      currentPage: 1, // 当前页数
      pageSize: 5, // 每页显示的条数
      totalItems: 0, // 总条数
      searchType: 'id', // 默认查询类型
      keyword: '', // 查询关键词
      dialogVisible: false, // 新增/编辑弹窗可见性
      editItem: { // 编辑/新增的数据
        product_name: '',
        category: '',
        brand: '',
        supplier: '',
        specification: '',
        unit: '',
        quantity: '',
        price: '',
        product_batch: '',
      },
      isEditing: false // 是否处于编辑状态 // 设置编辑状态为false表示新增,true表示编辑
    };
  },
  computed: {
    dialogTitle() {
      return this.isEditing ? '编辑信息' : '新增信息'; //false为新增，true为编辑
    }
  },
  mounted() {
    this.search();
  },
  methods: {
    search() {
      let url = '/inventory/selectAll';
      let params = {
        page: this.currentPage,
        size: this.pageSize
      };

      if (this.keyword) {
        url = `/inventory/selectBy${this.searchType.charAt(0).toUpperCase() + this.searchType.slice(1)}`;
        params[this.searchType] = this.keyword;
      }

      axios.get(url, { params })
          .then(response => {
            // console.log('后端返回的数据:', response.data);
            const data = response.data.data;
            this.inventory = data.list;
            this.totalItems = data.total;
          })
          .catch(error => {
            console.error('Error fetching inventory:', error);
          });
    },
    reset() {//重置
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
    handleSelectionChange(val) {//选择的行
      this.selectedRows = val.map(row => row.id);
    },
    showAddDialog() {
      this.isEditing = false;
      this.editItem = { // 新增表单置空
        product_name: '',
        category: '',
        brand: '',
        supplier: '',
        specification: '',
        unit: '',
        quantity: '',
        price: '',
        product_batch: ''
      };
      this.dialogVisible = true;
    },
    closeDialog() {
      this.dialogVisible = false;
      this.isEditing = false;
    },
    handleAdd() {
      // 实现新增功能的逻辑
      axios.post("/inventory/add", this.editItem)
          .then(response => {
            // 处理新增成功逻辑
            this.$message.success("新增成功");
            this.dialogVisible = false;
            this.search(); // 刷新数据
          })
          .catch(error => {
            // 处理新增失败逻辑
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
      // 发送编辑/新增请求到后端
      if (this.isEditing) {
        // 编辑状态下发送编辑请求
        axios.put("/inventory/edit", this.editItem)
            .then(response => {
              // 处理编辑成功逻辑
              this.$message.success("编辑成功");
              this.closeDialog();
              this.search(); // 刷新数据
            })
            .catch(error => {
              // 处理编辑失败逻辑
              console.error('Error editing item:', error);
              this.$message.error("编辑失败");
            });
      } else {
        // 新增状态下发送新增请求
        this.handleAdd();
      }
    },
    handleDelete(id) {
      this.$confirm('此操作将永久删除该条目，是否继续？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        axios.delete(`/inventory/delete/${id}`)
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
    },
    delBatch() {
      // 确认是否删除选中的行
      this.$confirm('此操作将永久删除所选条目，是否继续？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 批量删除操作
        axios.delete(`/inventory/deleteBatch`, {  data: this.selectedRows })
            .then(response => {
              this.$message.success('批量删除成功');
              this.search(); // 刷新数据
              this.selectedRows = []; // 清空选中的行
            })
            .catch(error => {
              console.error('Error deleting batch:', error);
              this.$message.error('批量删除失败');
            });
      }).catch(() => {
        this.$message.info('已取消批量删除');
      });
    },

    handleBeforeUpload(file) {
      // 解析Excel文件
      const reader = new FileReader();
      reader.onload = (e) => {
        const data = e.target.result;
        const workbook = XLSX.read(data, { type: 'binary' });
        const firstSheetName = workbook.SheetNames[0];
        const worksheet = workbook.Sheets[firstSheetName];
        const jsonData = XLSX.utils.sheet_to_json(worksheet);
        this.handleBatchImport(jsonData);
      };
      reader.readAsBinaryString(file);
      return false; // 阻止文件自动上传
    },

    handleBatchImport(data) {
      // 过滤掉空行
      const filteredData = data.filter(item => item.product_name && item.quantity);
      console.log(filteredData)

      // 发送批量新增请求
      axios.post('/inventory/addBatch', filteredData)
          .then(response => {
            this.$message.success('批量导入成功');
            this.search(); // 刷新数据
          })
          .catch(error => {
            console.error('Error importing batch:', error);
            this.$message.error('批量导入失败');
          });
    },

    exportExcel() {
      const headers = ['编号', '产品名称', '类别', '品牌', '供应商', '规格', '单位', '数量', '价格', '产品批次'];
      const rows = this.inventory.map(inventory => [
        inventory.id,
        inventory.product_name,
        inventory.category,
        inventory.brand,
        inventory.supplier,
        inventory.specification,
        inventory.unit,
        inventory.quantity,
        inventory.brand,
        inventory.price,
        inventory.product_batch,
      ]);

      const worksheet = XLSX.utils.aoa_to_sheet([headers, ...rows]);
      const workbook = XLSX.utils.book_new();
      XLSX.utils.book_append_sheet(workbook, worksheet, "财务数据");

      // Generate Excel file and trigger download
      XLSX.writeFile(workbook, "inventory.xlsx");
    },

  }
};
</script>

<style scoped>
.dialog-footer {
  text-align: right;
}
</style>
