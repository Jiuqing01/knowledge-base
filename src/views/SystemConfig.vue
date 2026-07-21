<template>
  <div class="system-config-page">
    <div class="config-section">
      <div class="section-header">
        <h3 class="section-title">文件上传配置</h3>
      </div>
      
      <el-form :model="uploadConfig" label-width="150px">
        <el-form-item label="允许的文件扩展名">
          <el-input v-model="uploadConfig.allowedExtensions" placeholder="多个扩展名用逗号分隔" />
        </el-form-item>
        
        <el-form-item label="最大文件大小(MB)">
          <el-input-number
            v-model="uploadConfig.maxFileSize"
            :min="1"
            :max="1000"
          />
        </el-form-item>
        
        <el-form-item label="默认存储配额(GB)">
          <el-input-number
            v-model="uploadConfig.defaultStorageQuota"
            :min="1"
            :max="100"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSaveUploadConfig">保存配置</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <div class="config-section">
      <div class="section-header">
        <h3 class="section-title">用户配置</h3>
      </div>
      
      <el-form :model="userConfig" label-width="150px">
        <el-form-item label="登录失败次数限制">
          <el-input-number
            v-model="userConfig.loginAttemptLimit"
            :min="1"
            :max="10"
          />
        </el-form-item>
        
        <el-form-item label="Session超时时间(分钟)">
          <el-input-number
            v-model="userConfig.sessionTimeout"
            :min="5"
            :max="120"
          />
        </el-form-item>
        
        <el-form-item label="是否开启验证码">
          <el-switch v-model="userConfig.enableCaptcha" />
        </el-form-item>
        
        <el-form-item label="是否开启注册">
          <el-switch v-model="userConfig.enableRegistration" />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSaveUserConfig">保存配置</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <div class="config-section">
      <div class="section-header">
        <h3 class="section-title">系统信息</h3>
      </div>
      
      <div class="about-info">
        <div class="info-item">
          <span class="info-label">系统名称</span>
          <span class="info-value">{{ systemInfo.systemName }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">版本号</span>
          <span class="info-value">{{ systemInfo.version }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">后端框架</span>
          <span class="info-value">Spring Boot 2.7.18</span>
        </div>
        <div class="info-item">
          <span class="info-label">前端框架</span>
          <span class="info-value">Vue 3 + Element Plus</span>
        </div>
        <div class="info-item">
          <span class="info-label">数据库</span>
          <span class="info-value">MySQL / H2</span>
        </div>
        <div class="info-item">
          <span class="info-label">缓存</span>
          <span class="info-value">Redis</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { systemConfigAPI } from '@/api'

const uploadConfig = reactive({
  allowedExtensions: '',
  maxFileSize: 500,
  defaultStorageQuota: 1
})

const userConfig = reactive({
  loginAttemptLimit: 5,
  sessionTimeout: 30,
  enableCaptcha: true,
  enableRegistration: true
})

const systemInfo = reactive({
  systemName: '知识库管理系统',
  version: 'v1.0.0'
})

const loadConfigs = async () => {
  try {
    const res = await systemConfigAPI.getAllConfigs()
    if (res.success && res.data) {
      const configMap = {}
      res.data.forEach(config => {
        configMap[config.configKey] = config.configValue
      })
      
      uploadConfig.allowedExtensions = configMap.allowed_file_extensions || 'pdf,doc,docx,xls,xlsx,ppt,pptx,jpg,jpeg,png,gif,bmp,webp,txt,md,zip,rar'
      uploadConfig.maxFileSize = Math.round(parseInt(configMap.max_file_size || 524288000) / 1024 / 1024)
      uploadConfig.defaultStorageQuota = Math.round(parseInt(configMap.default_storage_quota || 1073741824) / 1024 / 1024 / 1024)
      
      userConfig.loginAttemptLimit = parseInt(configMap.login_attempt_limit || 5)
      userConfig.sessionTimeout = parseInt(configMap.session_timeout || 30)
      userConfig.enableCaptcha = configMap.enable_captcha === 'true'
      userConfig.enableRegistration = configMap.enable_registration === 'true'
      
      systemInfo.systemName = configMap.system_name || '知识库管理系统'
      systemInfo.version = configMap.version || 'v1.0.0'
    }
  } catch (error) {
    console.error('加载配置失败:', error)
  }
}

const handleSaveUploadConfig = async () => {
  try {
    await systemConfigAPI.updateConfig('allowed_file_extensions', uploadConfig.allowedExtensions, '允许的文件扩展名')
    await systemConfigAPI.updateConfig('max_file_size', String(uploadConfig.maxFileSize * 1024 * 1024), '最大文件大小(字节)')
    await systemConfigAPI.updateConfig('default_storage_quota', String(uploadConfig.defaultStorageQuota * 1024 * 1024 * 1024), '默认存储配额(字节)')
    ElMessage.success('文件上传配置保存成功')
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const handleSaveUserConfig = async () => {
  try {
    await systemConfigAPI.updateConfig('login_attempt_limit', String(userConfig.loginAttemptLimit), '登录失败次数限制')
    await systemConfigAPI.updateConfig('session_timeout', String(userConfig.sessionTimeout), 'Session超时时间(分钟)')
    await systemConfigAPI.updateConfig('enable_captcha', String(userConfig.enableCaptcha), '是否开启验证码')
    await systemConfigAPI.updateConfig('enable_registration', String(userConfig.enableRegistration), '是否开启注册')
    ElMessage.success('用户配置保存成功')
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

onMounted(() => {
  loadConfigs()
})
</script>

<style scoped>
.system-config-page {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.config-section {
  margin-bottom: 24px;
}

.section-header {
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e8e8e8;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.about-info {
  background: #fafafa;
  border-radius: 8px;
  padding: 16px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px dashed #e8e8e8;
}

.info-item:last-child {
  border-bottom: none;
}

.info-label {
  font-size: 14px;
  color: #666;
}

.info-value {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}
</style>