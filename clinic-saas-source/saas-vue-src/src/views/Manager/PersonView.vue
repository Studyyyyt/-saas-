<template>
  <div class="user-profile">
    <el-card class="user-card">
      <div slot="header" class="clearfix">
        <span>个人信息</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="editProfile">编辑</el-button>
      </div>

      <div class="user-info" style="">
        <el-avatar icon="el-icon-user-solid"></el-avatar>
        <el-form :model="user">
          <el-form-item label="姓名">
            <span>{{ user.name }}</span>
          </el-form-item>
          <el-form-item label="年龄">
            <span>{{ user.age }}</span>
          </el-form-item>
          <el-form-item label="邮箱">
            <span>{{ user.email }}</span>
          </el-form-item>
          <el-form-item label="地址">
            <span>{{ user.address }}</span>
          </el-form-item>
          <el-form-item label="联系方式">
            <span>{{ user.phone }}</span>
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <!-- 编辑个人信息的对话框 -->
    <el-dialog :visible.sync="dialogVisible" title="编辑个人信息">
      <el-form :model="editUser" label-width="80px">
        <el-form-item label="姓名">
          <el-input v-model="editUser.name"></el-input>
        </el-form-item>
        <el-form-item label="年龄">
          <el-input v-model="editUser.age"></el-input>
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editUser.email"></el-input>
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="editUser.address"></el-input>
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="editUser.phone"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveProfile">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  data() {
    return {
      user: {
        name: '张三',
        age: 28,
        email: 'zhangsan@example.com',
        address: '北京市海淀区',
        phone:'12203456771'
      },
      dialogVisible: false,
      editUser: {}
    };
  },
  methods: {
    editProfile() {
      // 深拷贝用户信息到编辑表单
      this.editUser = { ...this.user };
      this.dialogVisible = true;
    },
    saveProfile() {
      // 保存编辑后的个人信息
      this.user = { ...this.editUser };
      this.dialogVisible = false;
      // 你可以在这里添加保存到后端的代码，例如使用 axios 发送 POST 请求
    }
  }
};
</script>

<style scoped>
.user-card {
  width: 500px;
  margin: 20px auto;
}
.user-info {
  margin-top: 20px;
}
</style>
