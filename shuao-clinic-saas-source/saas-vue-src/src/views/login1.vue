<template>
  <div class="login-page apple-design-scope">
    <div class="login-card">
      <div class="login-brand">
        <div class="brand-mark">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
            <path d="M12 2C7.5 2 4 6.5 4 10c0 3.5 3 7 8 12 5-5 8-8.5 8-12 0-3.5-3.5-8-8-8z"/>
            <circle cx="12" cy="10" r="3"/>
          </svg>
        </div>
        <div class="brand-title">舒澳口腔</div>
        <div class="brand-subtitle">诊所管理系统</div>
      </div>

      <el-form :model="user" class="login-form" :rules="rules" ref="loginRef">
        <el-form-item prop="username">
          <el-input
            prefix-icon="el-icon-user"
            size="medium"
            placeholder="用户名"
            v-model="user.username"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            prefix-icon="el-icon-lock"
            size="medium"
            show-password
            placeholder="密码"
            v-model="user.password"
            @keyup.enter.native="login"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            class="login-btn"
            :loading="isLoading"
            @click="login"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="login-footer">
      备案号：
      <a href="https://beian.miit.gov.cn" target="_blank" rel="noopener noreferrer">
        湘ICP备2026011054号
      </a>
      · 长沙市天心区舒澳口腔诊所
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
  background:
    radial-gradient(ellipse 60% 40% at 80% 20%, rgba(37, 99, 235, 0.06), transparent),
    radial-gradient(ellipse 50% 50% at 20% 80%, rgba(124, 58, 237, 0.04), transparent),
    var(--apple-bg-primary);
  padding: 24px;
  box-sizing: border-box;
}

.login-card {
  width: 100%;
  max-width: 420px;
  background: var(--apple-bg-secondary);
  border-radius: 24px;
  box-shadow: var(--apple-shadow-xl), 0 0 0 1px rgba(255, 255, 255, 0.5) inset;
  padding: 56px 48px;
  box-sizing: border-box;
}

.login-brand {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 36px;
}

.brand-mark {
  width: 64px;
  height: 64px;
  border-radius: 18px;
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
  box-shadow: 0 8px 24px rgba(37, 99, 235, 0.25);
}

.brand-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--apple-text-primary);
  letter-spacing: -0.02em;
}

.brand-subtitle {
  font-size: 14px;
  color: var(--apple-text-secondary);
  margin-top: 6px;
  font-weight: 400;
}

.login-form >>> .el-input__inner {
  border-radius: var(--apple-radius-md) !important;
  height: 48px !important;
  font-size: 14px !important;
  padding-left: 42px !important;
  background: #f3f4f6 !important;
  border: 1px solid transparent !important;
}

.login-form >>> .el-input__inner:focus {
  background: #ffffff !important;
  border-color: rgba(37, 99, 235, 0.4) !important;
  box-shadow: 0 0 0 4px rgba(37, 99, 235, 0.1), 0 0 20px rgba(37, 99, 235, 0.08) !important;
}

.login-form >>> .el-input__prefix {
  left: 14px;
  color: var(--apple-text-tertiary);
}

.login-btn {
  width: 100%;
  height: 48px;
  font-size: 15px !important;
  font-weight: 600 !important;
  border-radius: var(--apple-radius-md) !important;
  margin-top: 8px;
  background: linear-gradient(135deg, #2563eb, #1d4ed8) !important;
  border: none !important;
  box-shadow: 0 4px 14px rgba(37, 99, 235, 0.25) !important;
  transition: all 0.2s ease !important;
  position: relative;
  overflow: hidden;
}

.login-btn::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.4) 0%, transparent 70%);
  border-radius: 50%;
  transform: translate(-50%, -50%) scale(0);
  opacity: 0;
  pointer-events: none;
}

.login-btn:hover {
  box-shadow: 0 6px 20px rgba(37, 99, 235, 0.35) !important;
  transform: translateY(-1px);
}

.login-btn:active {
  transform: scale(0.98);
}

.login-btn:active::after {
  animation: login-ripple 0.5s ease-out forwards;
}

@keyframes login-ripple {
  0% { transform: translate(-50%, -50%) scale(0); opacity: 0.5; }
  100% { transform: translate(-50%, -50%) scale(2.5); opacity: 0; }
}

.login-footer {
  margin-top: 32px;
  font-size: 12px;
  color: var(--apple-text-tertiary);
  text-align: center;
}

.login-footer a {
  color: var(--apple-text-secondary);
  text-decoration: none;
  transition: color var(--apple-transition-fast);
}

.login-footer a:hover {
  color: var(--apple-accent);
}

/* 移动端适配 */
@media (max-width: 480px) {
  .login-card {
    padding: 36px 24px;
  }

  .brand-mark {
    width: 56px;
    height: 56px;
  }
}
</style>
