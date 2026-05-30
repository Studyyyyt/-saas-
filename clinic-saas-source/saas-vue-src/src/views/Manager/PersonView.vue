<template>
  <div class="person-page">
    <div class="hero-card">
      <div>
        <div class="page-kicker">个人中心</div>
        <h2>个人信息</h2>
        <p>查看并管理您的个人资料。</p>
      </div>
      <div class="hero-actions">
        <el-button type="primary" plain @click="editProfile">编辑资料</el-button>
      </div>
    </div>

    <el-card shadow="never" class="info-card">
      <div class="profile-header">
        <el-avatar :size="80" icon="el-icon-user-solid" class="profile-avatar"></el-avatar>
        <div class="profile-name">{{ user.name }}</div>
      </div>
      <el-divider></el-divider>
      <el-form label-width="100px" class="profile-form">
        <el-form-item label="姓名">
          <span class="form-value">{{ user.name }}</span>
        </el-form-item>
        <el-form-item label="年龄">
          <span class="form-value">{{ user.age }}</span>
        </el-form-item>
        <el-form-item label="邮箱">
          <span class="form-value">{{ user.email }}</span>
        </el-form-item>
        <el-form-item label="地址">
          <span class="form-value">{{ user.address }}</span>
        </el-form-item>
        <el-form-item label="联系方式">
          <span class="form-value">{{ user.phone }}</span>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 编辑个人信息的对话框 -->
    <el-dialog :visible.sync="dialogVisible" title="编辑个人信息" width="480px">
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
        <el-form-item label="联系方式">
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
.person-page {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.info-card {
  max-width: 600px;
  border-radius: 18px;
}
.profile-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px 0 8px;
}
.profile-avatar {
  font-size: 36px;
  background: linear-gradient(135deg, #5A8F7B 0%, #7AAF9B 100%);
  color: #fff;
  transition: transform 0.3s ease;
}
.profile-avatar:hover {
  transform: scale(1.05);
}
.profile-name {
  margin-top: 12px;
  font-size: 20px;
  font-weight: 600;
  color: #2C3E35;
}
.profile-form .el-form-item {
  margin-bottom: 14px;
}
.form-value {
  color: #475569;
  font-size: 15px;
}
.dialog-footer {
  text-align: right;
}
</style>
