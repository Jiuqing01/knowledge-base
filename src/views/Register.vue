<template>
  <div class="register-container">
    <div class="register-left">
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
    
    <div class="register-right">
      <div class="register-content">
        <div class="register-header">
          <h2 class="register-title">注册新账户</h2>
          <p class="register-desc">欢迎注册！请输入您的账户信息</p>
        </div>
        
        <el-form ref="formRef" :model="form" :rules="rules" class="register-form" @keyup.enter="handleRegister">
          <el-form-item prop="username">
            <el-input
              v-model="form.username"
              type="text"
              placeholder="请输入用户名"
              class="register-input"
            />
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              show-password
              class="register-input"
            />
          </el-form-item>
          
          <el-form-item prop="confirmPassword">
            <el-input
              v-model="form.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              show-password
              class="register-input"
            />
          </el-form-item>
          
          <el-form-item prop="role">
            <el-radio-group v-model="form.role" class="role-group">
              <el-radio label="USER">用户</el-radio>
              <el-radio label="ADMIN">管理员</el-radio>
            </el-radio-group>
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
          
          <el-form-item>
            <el-button
              type="primary"
              size="large"
              class="register-btn"
              :loading="loading"
              @click="handleRegister"
            >
              注册
            </el-button>
          </el-form-item>
        </el-form>
        
        <div class="register-footer">
          <span>您已有账户？</span>
          <router-link to="/login" class="login-link">登录</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
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
  confirmPassword: '',
  role: 'USER',
  captcha: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度为3-50个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
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

const handleRegister = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  if (form.captcha.toUpperCase() !== captchaText.value.toUpperCase()) {
    ElMessage.error('验证码错误')
    refreshCaptcha()
    return
  }
  
  loading.value = true
  const result = await userStore.register(form.username, form.password, form.role)
  loading.value = false
  
  if (result.success) {
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } else {
    ElMessage.error(result.message)
    refreshCaptcha()
  }
}

refreshCaptcha()
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
}

.register-left {
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

.register-right {
  width: 450px;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.register-content {
  width: 100%;
}

.register-header {
  margin-bottom: 32px;
}

.register-title {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin-bottom: 8px;
}

.register-desc {
  font-size: 14px;
  color: #999;
}

.register-form {
  margin-bottom: 24px;
}

.register-input {
  width: 100%;
  margin-bottom: 16px;
}

.role-group {
  display: flex;
  gap: 30px;
  margin-bottom: 16px;
}

:deep(.register-input .el-input__wrapper) {
  height: 44px;
  background: #f5f7fa;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  padding: 0 16px;
  box-shadow: none;
}

:deep(.register-input .el-input__wrapper:hover) {
  border-color: #dcdfe6;
}

:deep(.register-input .el-input__wrapper.is-focus) {
  border-color: #409eff;
  box-shadow: none;
}

:deep(.register-input .el-input__inner) {
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

.register-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: bold;
  background: #1a73e8;
  border: none;
  border-radius: 8px;
}

.register-btn:hover {
  background: #1557b0;
}

.register-footer {
  text-align: center;
  color: #999;
  font-size: 14px;
}

.login-link {
  color: #409eff;
  margin-left: 4px;
  text-decoration: none;
}

.login-link:hover {
  text-decoration: underline;
}
</style>
