<template>
  <div class="login-container">
    <div class="login-left">
      <div class="illustration-section">
        <div class="illustration">
          <div class="person person-1">
            <div class="head"></div>
            <div class="body"></div>
            <div class="arm arm-left"></div>
            <div class="arm arm-right"></div>
            <div class="leg leg-left"></div>
            <div class="leg leg-right"></div>
          </div>
          <div class="person person-2">
            <div class="head"></div>
            <div class="body"></div>
            <div class="arm arm-left"></div>
            <div class="arm arm-right"></div>
            <div class="leg leg-left"></div>
            <div class="leg leg-right"></div>
          </div>
          <div class="panel">
            <div class="panel-header"></div>
            <div class="panel-body">
              <div class="panel-item"></div>
              <div class="panel-item"></div>
              <div class="panel-item"></div>
              <div class="panel-item"></div>
            </div>
          </div>
        </div>
      </div>
      <div class="slogan-section">
        <h3 class="slogan-title">企业级AI开发平台+知识库</h3>
        <p class="slogan-desc">为开发者提供了完整的企业级AI开发+本地知识库解决方案</p>
      </div>
      <div class="decorations">
        <div class="deco-circle deco-circle-1"></div>
        <div class="deco-circle deco-circle-2"></div>
        <div class="deco-circle deco-circle-3"></div>
        <div class="deco-square deco-square-1"></div>
        <div class="deco-square deco-square-2"></div>
        <div class="deco-square deco-square-3"></div>
      </div>
    </div>
    
    <div class="login-right">
      <div class="login-content">
        <div class="login-header">
          <h2 class="login-title">登录到您的账户</h2>
          <p class="login-desc">欢迎回来！请输入您的账户信息</p>
        </div>
        
        <el-form ref="formRef" :model="form" :rules="rules" class="login-form" @keyup.enter="handleLogin">
          <el-form-item prop="username">
            <el-input
              v-model="form.username"
              type="text"
              placeholder="请输入用户名"
              class="login-input"
            />
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              show-password
              class="login-input"
            />
          </el-form-item>
          
          <el-form-item prop="captcha">
            <div class="captcha-wrapper">
              <el-input
                v-model="form.captcha"
                type="text"
                placeholder="请输入验证码"
                class="captcha-input"
              />
              <div class="captcha-image" @click="refreshCaptcha">
                <span class="captcha-text">{{ captchaText }}</span>
              </div>
            </div>
          </el-form-item>
          
          <div class="form-options">
            <el-checkbox v-model="form.rememberMe" class="remember-checkbox">记住密码</el-checkbox>
            <router-link to="/forgot-password" class="forgot-link">忘记密码？</router-link>
          </div>
          
          <el-form-item>
            <el-button
              type="primary"
              size="large"
              class="login-btn"
              :loading="loading"
              @click="handleLogin"
            >
              登录
            </el-button>
          </el-form-item>
        </el-form>
        
        <div class="social-login">
          <div class="social-divider">
            <span class="divider-line"></span>
            <span class="divider-text">其他账号登录</span>
            <span class="divider-line"></span>
          </div>
          <div class="social-buttons">
            <el-button class="social-btn social-btn-wecom" @click="handleWecomLogin">
              <svg class="social-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M8.5 2H15.5C16.8807 2 18 3.11929 18 4.5V19.5C18 20.8807 16.8807 22 15.5 22H8.5C7.11929 22 6 20.8807 6 19.5V4.5C6 3.11929 7.11929 2 8.5 2Z" fill="#07C160"/>
                <path d="M10 9.5C9.17157 9.5 8.5 10.1716 8.5 11V13C8.5 13.8284 9.17157 14.5 10 14.5C10.8284 14.5 11.5 13.8284 11.5 13V11C11.5 10.1716 10.8284 9.5 10 9.5Z" fill="white"/>
                <path d="M13.5 9.5C12.6716 9.5 12 10.1716 12 11V13C12 13.8284 12.6716 14.5 13.5 14.5C14.3284 14.5 15 13.8284 15 13V11C15 10.1716 14.3284 9.5 13.5 9.5Z" fill="white"/>
                <path d="M17 9.5C16.1716 9.5 15.5 10.1716 15.5 11V13C15.5 13.8284 16.1716 14.5 17 14.5C17.8284 14.5 18.5 13.8284 18.5 13V11C18.5 10.1716 17.8284 9.5 17 9.5Z" fill="white"/>
              </svg>
              <span>企业微信</span>
            </el-button>
            <el-button class="social-btn social-btn-qq" @click="handleQqLogin">
              <svg class="social-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2Z" fill="#12B7F5"/>
                <path d="M8.5 15.5C8.5 15.5 6.5 14 6.5 11.5C6.5 9 8.5 7.5 8.5 7.5C8.5 7.5 10.5 9 10.5 11.5C10.5 14 8.5 15.5 8.5 15.5Z" fill="white"/>
                <path d="M15.5 15.5C15.5 15.5 13.5 14 13.5 11.5C13.5 9 15.5 7.5 15.5 7.5C15.5 7.5 17.5 9 17.5 11.5C17.5 14 15.5 15.5 15.5 15.5Z" fill="white"/>
              </svg>
              <span>QQ</span>
            </el-button>
          </div>
        </div>
        
        <div class="login-footer">
          <span>您还没有账户？</span>
          <router-link to="/register" class="register-link">注册</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)
const captchaText = ref('')

const form = reactive({
  username: '',
  password: '',
  captcha: '',
  rememberMe: false
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度为3-50个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6个字符', trigger: 'blur' }
  ],
  captcha: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ]
}

const generateCaptcha = () => {
  const chars = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz'
  let result = ''
  for (let i = 0; i < 4; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length))
  }
  return result
}

const refreshCaptcha = () => {
  captchaText.value = generateCaptcha()
}

const handleLogin = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  if (form.captcha.toUpperCase() !== captchaText.value.toUpperCase()) {
    ElMessage.error('验证码错误')
    refreshCaptcha()
    return
  }
  
  loading.value = true
  const result = await userStore.login(form.username, form.password)
  loading.value = false
  
  if (result.success) {
    if (form.rememberMe) {
      localStorage.setItem('rememberUsername', form.username)
      localStorage.setItem('rememberPassword', btoa(form.password))
      localStorage.setItem('rememberMe', 'true')
    } else {
      localStorage.removeItem('rememberUsername')
      localStorage.removeItem('rememberPassword')
      localStorage.removeItem('rememberMe')
    }
    
    const userInfo = userStore.userInfo
    if (userInfo.role === 'ADMIN') {
      ElMessage({
        message: '欢迎登录后台管理系统',
        type: 'success',
        showClose: true
      })
    } else {
      ElMessage.success('登录成功')
    }
    router.push('/')
  } else {
    ElMessage.error(result.message)
    refreshCaptcha()
  }
}

const handleWecomLogin = () => {
  ElMessage.info('企业微信登录功能开发中')
}

const handleQqLogin = () => {
  ElMessage.info('QQ登录功能开发中')
}

onMounted(() => {
  refreshCaptcha()
  
  const rememberMe = localStorage.getItem('rememberMe')
  if (rememberMe === 'true') {
    const username = localStorage.getItem('rememberUsername')
    const password = localStorage.getItem('rememberPassword')
    if (username) {
      form.username = username
    }
    if (password) {
      form.password = atob(password)
    }
    form.rememberMe = true
  }
})
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
}

.login-left {
  flex: 1;
  background: linear-gradient(135deg, #e8f4fc 0%, #f0f8ff 50%, #e0f2fe 100%);
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 40px;
}

.illustration-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.illustration {
  position: relative;
  width: 300px;
  height: 250px;
}

.person {
  position: absolute;
}

.person-1 {
  left: 20px;
  bottom: 30px;
}

.person-2 {
  right: 20px;
  bottom: 30px;
}

.person .head {
  width: 40px;
  height: 40px;
  background: #e8c4b8;
  border-radius: 50%;
  position: relative;
}

.person .head::before {
  content: '';
  position: absolute;
  top: 10px;
  left: 8px;
  width: 8px;
  height: 8px;
  background: #333;
  border-radius: 50%;
}

.person .head::after {
  content: '';
  position: absolute;
  top: 10px;
  right: 8px;
  width: 8px;
  height: 8px;
  background: #333;
  border-radius: 50%;
}

.person-1 .body {
  width: 40px;
  height: 50px;
  background: #4A90E2;
  border-radius: 20px 20px 5px 5px;
  margin-top: 5px;
}

.person-2 .body {
  width: 40px;
  height: 50px;
  background: #67B86B;
  border-radius: 20px 20px 5px 5px;
  margin-top: 5px;
}

.person .arm {
  width: 15px;
  height: 35px;
  background: inherit;
  border-radius: 10px;
  position: absolute;
  top: 50px;
}

.person .arm-left {
  left: -10px;
  transform: rotate(-30deg);
}

.person .arm-right {
  right: -10px;
  transform: rotate(30deg);
}

.person-1 .arm {
  background: #4A90E2;
}

.person-2 .arm {
  background: #67B86B;
}

.person .leg {
  width: 12px;
  height: 30px;
  background: #333;
  border-radius: 6px;
  position: absolute;
  top: 90px;
}

.person .leg-left {
  left: 5px;
}

.person .leg-right {
  right: 5px;
}

.panel {
  position: absolute;
  left: 50%;
  top: 30px;
  transform: translateX(-50%);
  width: 120px;
  height: 100px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.panel-header {
  height: 25px;
  background: #f5f7fa;
  border-radius: 12px 12px 0 0;
  position: relative;
}

.panel-header::before {
  content: '';
  position: absolute;
  top: 10px;
  left: 10px;
  width: 8px;
  height: 8px;
  background: #ff5f56;
  border-radius: 50%;
}

.panel-header::after {
  content: '';
  position: absolute;
  top: 10px;
  left: 25px;
  width: 8px;
  height: 8px;
  background: #ffbd2e;
  border-radius: 50%;
}

.panel-body {
  padding: 10px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.panel-item {
  height: 12px;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  border-radius: 4px;
}

.panel-item:nth-child(1) { width: 100%; }
.panel-item:nth-child(2) { width: 80%; }
.panel-item:nth-child(3) { width: 90%; }
.panel-item:nth-child(4) { width: 70%; }

.slogan-section {
  text-align: center;
  margin-bottom: 40px;
}

.slogan-title {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin-bottom: 12px;
}

.slogan-desc {
  font-size: 14px;
  color: #666;
}

.decorations {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
}

.deco-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(102, 126, 234, 0.1);
}

.deco-circle-1 {
  width: 200px;
  height: 200px;
  top: 10%;
  right: 10%;
}

.deco-circle-2 {
  width: 100px;
  height: 100px;
  top: 60%;
  left: 15%;
  background: rgba(118, 75, 162, 0.08);
}

.deco-circle-3 {
  width: 60px;
  height: 60px;
  bottom: 20%;
  right: 25%;
  background: rgba(74, 144, 226, 0.1);
}

.deco-square {
  position: absolute;
  background: rgba(102, 126, 234, 0.08);
  transform: rotate(45deg);
}

.deco-square-1 {
  width: 80px;
  height: 80px;
  top: 25%;
  left: 5%;
}

.deco-square-2 {
  width: 60px;
  height: 60px;
  bottom: 15%;
  left: 10%;
  background: rgba(118, 75, 162, 0.06);
}

.deco-square-3 {
  width: 40px;
  height: 40px;
  top: 70%;
  right: 8%;
  background: rgba(74, 144, 226, 0.08);
}

.login-right {
  width: 450px;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.login-content {
  width: 100%;
}

.login-header {
  margin-bottom: 32px;
}

.login-title {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin-bottom: 8px;
}

.login-desc {
  font-size: 14px;
  color: #999;
}

.login-form {
  margin-bottom: 24px;
}

.login-input {
  width: 100%;
  margin-bottom: 16px;
}

:deep(.login-input .el-input__wrapper) {
  height: 44px;
  background: #f5f7fa;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  padding: 0 16px;
  box-shadow: none;
}

:deep(.login-input .el-input__wrapper:hover) {
  border-color: #dcdfe6;
}

:deep(.login-input .el-input__wrapper.is-focus) {
  border-color: #409eff;
  box-shadow: none;
}

:deep(.login-input .el-input__inner) {
  color: #333;
  font-size: 14px;
}

.captcha-wrapper {
  display: flex;
  gap: 12px;
}

.captcha-input {
  flex: 1;
}

:deep(.captcha-input .el-input__wrapper) {
  height: 44px;
  background: #f5f7fa;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  padding: 0 16px;
}

.captcha-image {
  width: 120px;
  height: 44px;
  background: #f5f7fa;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
}

.captcha-image:hover {
  border-color: #dcdfe6;
}

.captcha-text {
  font-size: 18px;
  font-weight: bold;
  letter-spacing: 4px;
  color: #666;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

:deep(.remember-checkbox .el-checkbox__label) {
  color: #666;
  font-size: 14px;
}

.forgot-link {
  color: #409eff;
  font-size: 14px;
  cursor: pointer;
}

.forgot-link:hover {
  text-decoration: underline;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: bold;
  background: #1a73e8;
  border: none;
  border-radius: 8px;
}

.login-btn:hover {
  background: #1557b0;
}

.social-login {
  margin-bottom: 24px;
}

.social-divider {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 16px;
}

.divider-line {
  flex: 1;
  height: 1px;
  background: #e8e8e8;
}

.divider-text {
  font-size: 13px;
  color: #999;
}

.social-buttons {
  display: flex;
  gap: 12px;
}

.social-btn {
  flex: 1;
  height: 40px;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 14px;
  color: #666;
  transition: all 0.3s;
}

.social-btn:hover {
  border-color: #dcdfe6;
  background: #f5f7fa;
}

.social-icon {
  width: 18px;
  height: 18px;
}

.login-footer {
  text-align: center;
  color: #999;
  font-size: 14px;
}

.register-link {
  color: #409eff;
  margin-left: 4px;
  text-decoration: none;
}

.register-link:hover {
  text-decoration: underline;
}
</style>
