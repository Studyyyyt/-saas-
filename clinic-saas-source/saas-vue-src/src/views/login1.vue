<template>
  <div class="login-page apple-design-scope">
    <!-- 背景纹理层 -->
    <div class="bg-texture"></div>
    <div class="ink-wash ink-1"></div>
    <div class="ink-wash ink-2"></div>

    <div class="page">
      <div class="card">
        <div class="brand">
          <div class="logo-ring">
            <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M12 2C7.5 2 4 6.5 4 10c0 3.5 3 7 8 12 5-5 8-8.5 8-12 0-3.5-3.5-8-8-8z"/>
              <circle cx="12" cy="10" r="3"/>
            </svg>
          </div>
          <h1 class="brand-title">一隐口腔</h1>
          <div class="divider-line"></div>
          <p class="brand-subtitle">诊所管理系统</p>
        </div>

        <el-form :model="user" class="login-form" :rules="rules" ref="loginRef">
          <el-form-item prop="username" class="input-animate">
            <el-input
              prefix-icon="el-icon-user"
              size="medium"
              placeholder="用户名"
              v-model="user.username"
            />
          </el-form-item>

          <el-form-item prop="password" class="input-animate">
            <el-input
              prefix-icon="el-icon-lock"
              size="medium"
              show-password
              placeholder="密码"
              v-model="user.password"
              @keyup.enter.native="login"
            />
          </el-form-item>

          <el-form-item class="btn-animate">
            <el-button
              type="primary"
              class="login-btn"
              :loading="isLoading"
              @click="login"
            >
              登 录
            </el-button>
          </el-form-item>
        </el-form>

        <div class="login-footer">
          湘ICP备2026011054号 · 一隐口腔诊所
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from "axios";
import { saveAdminSession } from "@/utils/adminSession";

export default {
  name: "Login",
  data() {
    return {
      isLoading: false,
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
  methods: {
    login() {
      this.$refs['loginRef'].validate((valid) => {
        if (!valid) return;
        this.isLoading = true;
        const payload = {
          username: String(this.user.username || '').trim(),
          password: String(this.user.password || '').trim()
        };
        axios.post('/auth/login', payload)
          .then(response => {
            this.isLoading = false;
            if (response.data.code == 200) {
              saveAdminSession(response.data.data);
              this.$message.success("登录成功");
              if (this.$route.path !== '/home') {
                this.$router.push("/home").catch(() => {});
              }
            } else {
              this.$message.error(response.data.msg || "登录失败");
            }
          })
          .catch(error => {
            this.isLoading = false;
            console.error('登录错误:', error);
            const msg = error && error.response && error.response.data && error.response.data.msg
              ? error.response.data.msg
              : "登录失败";
            this.$message.error(msg);
          });
      });
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
  background: var(--apple-bg-primary);
  padding: 24px;
  box-sizing: border-box;
  position: relative;
  overflow: hidden;
}

/* === 宣纸纹理背景 === */
.bg-texture {
  position: fixed;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  background:
    radial-gradient(ellipse 60% 40% at 30% 20%, rgba(90, 143, 123, 0.06), transparent),
    radial-gradient(ellipse 50% 50% at 70% 80%, rgba(90, 143, 123, 0.04), transparent);
}
.bg-texture::after {
  content: '';
  position: absolute;
  inset: 0;
  background: url("data:image/svg+xml,%3Csvg width='100' height='100' viewBox='0 0 100 100' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noise'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.8' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noise)' opacity='0.03'/%3E%3C/svg%3E");
  opacity: 0.6;
}

/* === 水墨晕染装饰 === */
.ink-wash {
  position: fixed;
  pointer-events: none;
  z-index: 0;
  border-radius: 50%;
  filter: blur(60px);
}
.ink-1 {
  width: 350px;
  height: 350px;
  background: rgba(90, 143, 123, 0.10);
  top: -5%;
  right: -8%;
  animation: inkFloat 18s ease-in-out infinite;
}
.ink-2 {
  width: 280px;
  height: 280px;
  background: rgba(120, 100, 80, 0.06);
  bottom: 5%;
  left: -5%;
  animation: inkFloat 22s ease-in-out infinite reverse;
}

@keyframes inkFloat {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(-15px, 10px) scale(1.08); }
}

/* === 页面容器 === */
.page {
  position: relative;
  z-index: 1;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
}

/* === 卡片 === */
.card {
  width: 100%;
  max-width: 400px;
  background: rgba(255, 255, 255, 0.92);
  border-radius: 16px;
  border: 1px solid rgba(90, 143, 123, 0.15);
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.04), 0 2px 8px rgba(0, 0, 0, 0.02);
  padding: 52px 44px 36px;
  animation: cardEnter 0.9s cubic-bezier(0.22, 1, 0.36, 1) forwards;
  opacity: 0;
  transform: translateY(20px);
}

@keyframes cardEnter {
  to { opacity: 1; transform: translateY(0); }
}

/* === 品牌区 === */
.brand {
  text-align: center;
  margin-bottom: 36px;
  animation: fadeInUp 0.7s cubic-bezier(0.22, 1, 0.36, 1) 0.2s forwards;
  opacity: 0;
  transform: translateY(20px);
}

.logo-ring {
  width: 72px;
  height: 72px;
  margin: 0 auto 18px;
  border-radius: 50%;
  border: 2px solid rgba(90, 143, 123, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  color: #5A8F7B;
}
.logo-ring::before {
  content: '';
  position: absolute;
  inset: 6px;
  border-radius: 50%;
  border: 1px solid rgba(90, 143, 123, 0.15);
}

.brand-title {
  font-family: var(--apple-font-serif);
  font-size: 26px;
  font-weight: 600;
  color: #2C3E35;
  letter-spacing: 0.12em;
}

.divider-line {
  width: 32px;
  height: 2px;
  background: #5A8F7B;
  margin: 14px auto 10px;
  border-radius: 1px;
  animation: lineGrow 0.6s cubic-bezier(0.22, 1, 0.36, 1) 0.5s forwards;
  transform: scaleX(0);
}

@keyframes lineGrow {
  to { transform: scaleX(1); }
}

.brand-subtitle {
  font-size: 12px;
  color: #8A9A8E;
  letter-spacing: 0.15em;
}

@keyframes fadeInUp {
  to { opacity: 1; transform: translateY(0); }
}

/* === 表单 === */
.login-form {
  animation: fadeIn 0.4s ease 0.3s forwards;
  opacity: 0;
}

@keyframes fadeIn {
  to { opacity: 1; }
}

/* 细线输入框（东方风格） */
.login-form >>> .el-input__inner {
  width: 100%;
  height: 44px;
  padding: 8px 4px 8px 36px;
  font-size: 15px;
  color: #2C3E35;
  background: transparent;
  border: none;
  border-bottom: 1.5px solid rgba(90, 143, 123, 0.2);
  border-radius: 0;
  outline: none;
  font-family: var(--apple-font-sans);
  transition: border-color 0.3s ease;
}

.login-form >>> .el-input__inner:focus {
  background: transparent;
  border-bottom-color: #5A8F7B;
  box-shadow: none;
}

.login-form >>> .el-input__prefix {
  left: 2px;
  color: #A0A0A0;
  transition: color 0.3s ease;
}

.login-form >>> .el-input__inner:focus + .el-input__prefix,
.login-form >>> .el-input__inner:focus ~ .el-input__prefix {
  color: #5A8F7B;
}

/* === 输入框入场动画 === */
.input-animate:nth-child(1) {
  animation: fadeInUp 0.5s cubic-bezier(0.22, 1, 0.36, 1) 0.4s forwards;
  opacity: 0;
  transform: translateY(12px);
}
.input-animate:nth-child(2) {
  animation: fadeInUp 0.5s cubic-bezier(0.22, 1, 0.36, 1) 0.5s forwards;
  opacity: 0;
  transform: translateY(12px);
}

/* === 登录按钮 === */
.login-btn {
  width: 100%;
  height: 46px;
  background: #5A8F7B;
  color: #FFFFFF;
  border: none;
  border-radius: 6px;
  font-size: 15px;
  font-weight: 500;
  font-family: var(--apple-font-sans);
  cursor: pointer;
  letter-spacing: 0.1em;
  transition: all 0.25s cubic-bezier(0.22, 1, 0.36, 1);
  position: relative;
  overflow: hidden;
  margin-top: 8px;
}

.login-btn::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.15), transparent);
  transform: translateX(-100%);
  transition: transform 0.5s ease;
}

.login-btn:hover {
  background: #4A7F6B;
  box-shadow: 0 4px 16px rgba(90, 143, 123, 0.25);
}

.login-btn:hover::after {
  transform: translateX(100%);
}

.login-btn:active {
  transform: scale(0.98);
}

/* === 按钮入场动画 === */
.btn-animate {
  animation: fadeInUp 0.5s cubic-bezier(0.22, 1, 0.36, 1) 0.6s forwards;
  opacity: 0;
  transform: translateY(12px);
}

/* === Footer === */
.login-footer {
  margin-top: 28px;
  text-align: center;
  font-size: 11px;
  color: #A0A0A0;
  letter-spacing: 0.05em;
  animation: fadeIn 0.4s ease 0.9s forwards;
  opacity: 0;
}

/* === 移动端适配 === */
@media (max-width: 480px) {
  .card {
    padding: 40px 28px 28px;
  }
  .brand-title {
    font-size: 22px;
  }
}
</style>
