<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-illustration">
        <img src="@/assets/login.png" alt="" class="login-image">
      </div>
      <div class="login-form-wrap">
        <el-form :model="user" class="login-form" :rules="rules" ref="loginRef">
          <div class="login-title">欢迎登录口腔诊所管理系统</div>
          <el-form-item prop="username">
            <el-input prefix-icon="el-icon-user" size="medium" placeholder="请输入用户名" v-model="user.username"></el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input prefix-icon="el-icon-lock" size="medium" show-password placeholder="请输入密码" v-model="user.password"></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" style="width: 100%" @click="login">登 录</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
    <div class="beian-footer">
      备案号：
      <a href="https://beian.miit.gov.cn" target="_blank" rel="noopener noreferrer">湘ICP备2026011054号</a>
      长沙市天心区舒澳口腔诊所
    </div>
  </div>
</template>

<script>
import ValidCode from "@/views/ValidCode.vue";
import axios from "axios";
import { saveAdminSession } from "@/utils/adminSession";

export default {
  name: "Login",
  components: {
    ValidCode
  },
  data() {
    return {
      user: {
        username: '',
        password: ''
      },
      rules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
        ]
      }
    }
  },
  created() {

  },
  methods: {
    login() {
      this.$refs['loginRef'].validate((valid) => {
        if (valid) {
          const payload = {
            username: String(this.user.username || '').trim(),
            password: String(this.user.password || '').trim()
          }
          // 验证通过
          axios.post('/loginController/login', payload)
              .then(response => {
                  if(response.data.code==200) {
                      saveAdminSession(response.data.data);
                      this.$message.success("登录成功");
                      if (this.$route.path !== '/home') {
                        this.$router.push("/home").catch(() => {});
                      }
                  }
                  else {
                      this.$message.error(response.data.msg || "登录失败");
                  }
              })
              .catch(error => {
                  console.error('Error adding new item:', error);
                  const msg = error && error.response && error.response.data && error.response.data.msg
                    ? error.response.data.msg
                    : "登录失败";
                  this.$message.error(msg);
              });
        }
      })
    }
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: #0f9fff;
  padding: 24px;
  box-sizing: border-box;
}

.login-card {
  display: flex;
  background-color: #fff;
  width: 50%;
  border-radius: 5px;
  overflow: hidden;
}

.login-illustration,
.login-form-wrap {
  flex: 1;
}

.login-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.login-form-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-form {
  width: 80%;
}

.login-title {
  font-size: 20px;
  font-weight: bold;
  text-align: center;
  margin-bottom: 20px;
}

.beian-footer {
  margin-top: 18px;
  color: #fff;
  font-size: 14px;
}

.beian-footer a {
  color: #fff;
  text-decoration: underline;
}

@media (max-width: 1200px) {
  .login-card {
    width: 80%;
  }
}

@media (max-width: 768px) {
  .login-page {
    padding: 16px;
  }

  .login-card {
    width: 100%;
    flex-direction: column;
  }
}
</style>
