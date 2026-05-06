<template>
  <div :style="{ backgroundImage: `url(${backgroundImage})` }" class="login-container">
    <div class="login-form">
      <h2>系统登录</h2>
      <el-form :model="loginForm" ref="loginForm">
        <el-form-item>
          <el-input v-model="loginForm.username" placeholder="用户名"></el-input>
        </el-form-item>
        <el-form-item>
          <el-input v-model="loginForm.password" placeholder="密码" type="password"></el-input>
        </el-form-item>
        <el-form-item>
          <el-row :gutter="20">
            <el-col :span="16">
              <el-input v-model="loginForm.captcha" placeholder="验证码"></el-input>
            </el-col>
            <el-col :span="8">
              <img :src="captchaUrl" @click="refreshCaptcha" />
            </el-col>
          </el-row>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin">登录</el-button>
        </el-form-item>
        <el-form-item>
          <el-button @click="changeBackground">更换背景图片</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      loginForm: {
        username: '',
        password: '',
        captcha: ''
      },
      captchaUrl: '/captcha',
      backgroundImage: 'url1.jpg',
      backgroundImages: ['url1.jpg', 'url2.jpg', 'url3.jpg']
    };
  },
  methods: {
    handleLogin() {
      axios.post('/login', this.loginForm)
          .then(response => {
            const {success, message, role} = response.data;
            if (success) {
              this.$message.success('登录成功');
              // 根据角色跳转
              if (role === 'admin') {
                this.$router.push('/admin');
              } else if (role === 'doctor') {
                this.$router.push('/doctor');
              } else if (role === 'nurse') {
                this.$router.push('/nurse');
              }
            } else {
              this.$message.error(message);
            }
          })
          .catch(error => {
            console.error('Error:', error);
            this.$message.error('登录失败');
          });
    },
    refreshCaptcha() {
      this.captchaUrl = `/captcha?timestamp=${new Date().getTime()}`;
    },
    changeBackground() {
      const randomIndex = Math.floor(Math.random() * this.backgroundImages.length);
      this.backgroundImage = this.backgroundImages[randomIndex];
    }
  }
};
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-size: cover;
}

.login-form {
  background: rgba(255, 255, 255, 0.8);
  padding: 20px;
  border-radius: 8px;
  text-align: center;
}
</style>
