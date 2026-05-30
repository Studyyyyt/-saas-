<template>
  <div class="inventory-page">
    <!-- 页面标题区 -->
    <div class="hero-card">
      <div>
        <div class="page-kicker">库存管理</div>
        <h2>库存预警</h2>
        <p>监控库存不足的产品，及时生成采购单补货。</p>
      </div>
      <div class="hero-actions">
        <el-button type="primary" plain @click="showAddDialog">新增</el-button>
        <el-button type="danger" plain @click="delBatch">批量删除</el-button>
        <el-button type="info" plain @click="exportExcel">导出 Excel</el-button>
      </div>
    </div>

    <!-- 查询区 -->
    <el-card shadow="never" class="query-card">
      <div class="query-row">
        <el-select v-model="searchType" placeholder="请选择查询条件" class="query-select">
          <el-option label="序号" value="id"></el-option>
          <el-option label="产品名称" value="name"></el-option>
          <el-option label="类别" value="category"></el-option>
          <el-option label="品牌" value="brand"></el-option>
          <el-option label="供应商" value="supplier"></el-option>
        </el-select>
        <el-input v-model="keyword" class="query-input" placeholder="请输入关键词" @keyup.enter.native="search"></el-input>
        <el-button type="primary" icon="el-icon-search" @click="search">查询</el-button>
        <el-button icon="el-icon-refresh" @click="reset">重置</el-button>
      </div>
    </el-card>

    <!-- 表格区 -->
    <el-card shadow="never" class="table-card">
      <el-table
        :data="inventory"
        stripe
        v-loading="loading"
        :header-cell-style="{ backgroundColor: '#f8fafc', color: '#475569', fontWeight: '600' }"
        @selection-change="handleSelectionChange"
      >
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
        <el-table-column label="操作" align="center" width="180" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" type="primary" plain @click="handleEdit(scope.row)">采购</el-button>
            <el-button size="mini" type="danger" plain @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && !inventory.length" description="暂无低库存数据"></el-empty>
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="480px">
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
        <el-form-item label="创建日期">
          <el-date-picker v-model="editItem.createdate" type="date" value-format="yyyy-MM-dd" placeholder="选择创建日期"></el-date-picker>
        </el-form-item>
        <el-form-item label="购买日期">
          <el-date-picker v-model="editItem.purchasedate" type="date" value-format="yyyy-MM-dd" placeholder="选择购买日期"></el-date-picker>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="closeDialog">取消</el-button>
        <el-button type="primary" @click="handleSaveEdit">{{ isEditing ? '保存' : '生成采购单' }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios';
import * as XLSX from 'xlsx';

export default {
  name: 'InventoryView2',
  data() {
    return {
      loading: false,
      inventory: [],
      selectedRows: [],
      currentPage: 1,
      pageSize: 5,
      totalItems: 0,
      searchType: 'id',
      keyword: '',
      dialogVisible: false,
      editItem: {
        product_name: '',
        category: '',
        brand: '',
        supplier: '',
        specification: '',
        unit: '',
        quantity: '',
        price: '',
        createdate:'',
        purchasedate:'',
        status:'待采购'
      },
      isEditing: false
    };
  },
  computed: {
    dialogTitle() {
      return this.isEditing ? '编辑信息' : '新增信息';
    }
  },
  mounted() {
    this.search();
  },
  methods: {
    search() {
      this.loading = true;
      let url = '/inventory/selectLowStock';
      let params = {
        page: this.currentPage,
        size: this.pageSize
      };

      if (this.keyword) {
        url = `/inventory/selectBy${this.searchType.charAt(0).toUpperCase() + this.searchType.slice(1)}?${this.searchType}=${this.keyword}`;
      }

      axios.get(url, {params})
          .then(response => {
            const data = response.data.data;
            this.inventory = data.list || [];
            this.totalItems = data.total || 0;
          })
          .catch(error => {
            console.error('Error fetching inventory:', error);
          })
          .finally(() => {
            this.loading = false;
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
      this.selectedRows = val.map(row => row.id);
    },
    showAddDialog() {
      this.isEditing = false;
      this.editItem = {
        product_name: '',
        category: '',
        brand: '',
        supplier: '',
        specification: '',
        unit: '',
        quantity: '',
        price: '',
        createdate:'',
        purchasedate:'',
        status:'待采购'
      };
      this.dialogVisible = true;
    },
    closeDialog() {
      this.dialogVisible = false;
      this.isEditing = false;
    },
    handleAdd() {
      axios.post("/purchases/add", this.editItem)
          .then(response => {
            this.$message.success("新增成功");
            this.dialogVisible = false;
          })
          .catch(error => {
            console.error('Error adding new item:', error);
            this.$message.error("新增失败");
          });
    },
    handleEdit(row) {
      this.editItem = Object.assign({}, row);
      this.isEditing = true;
      this.dialogVisible = true;
    },
    handleSaveEdit() {
      if (this.isEditing) {
        axios.post("/purchases/add", this.editItem)
            .then(response => {
              this.$message.success("生成采购单成功");
              this.closeDialog();
            })
            .catch(error => {
              console.error('Error editing item:', error);
              this.$message.error("生成采购单失败");
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
        axios.delete(`/inventory/delete/${id}`)
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
    delBatch() {
      this.$confirm('此操作将永久删除所选条目，是否继续？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        axios.delete(`/inventory/deleteBatch`, {data: this.selectedRows})
            .then(response => {
              this.$message.success('批量删除成功');
              this.search();
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

    exportExcel() {
      const headers = ['编号', '产品名称', '类别', '品牌', '供应商', '规格', '单位', '数量', '价格', '产品批次'];
      const rows = this.inventory.map(item => [
        item.id,
        item.product_name,
        item.category,
        item.brand,
        item.supplier,
        item.specification,
        item.unit,
        item.quantity,
        item.brand,
        item.price,
        item.product_batch,
      ]);

      const worksheet = XLSX.utils.aoa_to_sheet([headers, ...rows]);
      const workbook = XLSX.utils.book_new();
      XLSX.utils.book_append_sheet(workbook, worksheet, "库存数据");
      XLSX.writeFile(workbook, "inventory.xlsx");
    }
  }
};
</script>

<style scoped>
.inventory-page {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.dialog-footer {
  text-align: right;
}
</style>
