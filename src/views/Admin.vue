<template>
  <div class="admin-page">
    <el-tabs v-model="activeTab" type="border-card" class="admin-tabs" @tab-change="handleTabChange">
      <el-tab-pane label="用户管理" name="users">
        <div class="tab-content">
          <div class="toolbar">
            <el-input
              v-model="userSearch"
              placeholder="搜索用户名"
              class="search-input"
              clearable
              @input="loadUsers"
            />
            <div class="toolbar-actions">
              <el-button type="primary" icon="Plus" @click="handleAddUser">新增用户</el-button>
              <el-button type="danger" icon="Delete" :disabled="selectedUsers.length === 0" @click="handleBatchDelete">批量删除</el-button>
              <el-button icon="Refresh" @click="loadUsers">刷新</el-button>
            </div>
          </div>
          
          <el-table
            :data="users"
            v-loading="usersLoading"
            stripe
            @selection-change="handleSelectionChange"
          >
            <el-table-column type="selection" width="50" />
            <el-table-column prop="username" label="用户账号" min-width="150">
              <template #default="{ row }">
                <div class="user-cell">
                  <div class="user-avatar">
                    {{ row.username?.charAt(0).toUpperCase() || 'U' }}
                  </div>
                  <span>{{ row.username }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="role" label="角色" width="100">
              <template #default="{ row }">
                <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'info'">
                  {{ row.role === 'ADMIN' ? '管理员' : '用户' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="存储配额" width="150">
              <template #default="{ row }">
                {{ formatBytes(row.storageUsed) }} / {{ formatBytes(row.storageQuota) }}
              </template>
            </el-table-column>
            <el-table-column prop="email" label="邮箱" min-width="150">
              <template #default="{ row }">
                {{ row.email || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="phone" label="手机" width="120">
              <template #default="{ row }">
                {{ row.phone || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="注册时间" width="180">
              <template #default="{ row }">
                {{ formatDate(row.createdAt) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="160" fixed="right">
              <template #default="{ row }">
                <div class="action-buttons">
                  <el-button size="small" type="primary" link @click="handleEditUser(row)">修改</el-button>
                  <el-button size="small" type="warning" link @click="showQuotaDialog(row)">配额</el-button>
                  <el-button size="small" type="info" link @click="handleResetPassword(row)">重置密码</el-button>
                  <el-button size="small" type="danger" link @click="handleDeleteUser(row)" :disabled="row.role === 'ADMIN'">删除</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
          
          <div class="pagination">
            <span class="total-count">共 {{ totalUsers }} 条</span>
            <el-pagination
              v-model:current-page="userPage"
              v-model:page-size="userPageSize"
              :total="totalUsers"
              :page-sizes="[10, 20, 50, 100]"
              layout="prev, pager, next, jumper"
              @current-change="loadUsers"
              @size-change="loadUsers"
            />
          </div>
        </div>
      </el-tab-pane>
      
      <el-tab-pane label="文件扩展名白名单" name="whitelist">
        <div class="tab-content">
          <div class="config-section">
            <div class="config-header">
              <h3>文件扩展名白名单配置</h3>
              <p class="config-desc">超级管理员可配置允许上传的文件扩展名（严格模式，仅白名单内的扩展名允许上传）</p>
            </div>
            
            <div class="whitelist-layout">
              <div class="whitelist-left">
                <div class="template-label">允许的扩展名</div>
                <el-input
                  v-model="whitelistForm.extensions"
                  type="textarea"
                  :rows="8"
                  placeholder="请输入允许的文件扩展名，用逗号分隔"
                />
                <div class="form-tip">💡 扩展名校验不区分大小写，统一转为小写后比对</div>
              </div>
              
              <div class="whitelist-right">
                <div class="template-label">示例</div>
                <div class="preview-example">.pdf, .doc, .docx, .xls, .xlsx, .ppt, .pptx, .jpg, .png, .zip, .txt, .md</div>
              </div>
            </div>
            
            <div class="config-actions">
              <el-button type="primary" @click="handleSaveWhitelist">保存配置</el-button>
              <el-button @click="resetWhitelist">重置</el-button>
            </div>
          </div>
        </div>
      </el-tab-pane>
      
      <el-tab-pane label="初始文件夹模板" name="templates">
        <div class="tab-content">
          <div class="config-section">
            <div class="config-header">
              <h3>初始文件夹模板配置</h3>
              <p class="config-desc">超级管理员可配置新用户的初始文件夹模板，支持树形多层嵌套结构</p>
              <p class="config-desc">模板修改仅影响新注册用户，已有用户不受影响</p>
            </div>
            
            <div class="template-layout">
              <div class="template-left">
                <div class="template-label">文件夹模板</div>
                <el-input
                  v-model="templateForm.templates"
                  type="textarea"
                  :rows="12"
                  placeholder="每行一个路径，使用斜杠(/)表示层级关系，例如：&#10;文档/工作/报告&#10;文档/个人/笔记&#10;资源/图片&#10;资源/模板"
                />
                <div class="form-tip">💡 使用斜杠(/)表示层级关系，每行一个完整路径</div>
              </div>
              
              <div class="template-right">
                <div class="template-label">模板预览</div>
                <div class="template-preview-box">
                  <el-tree
                    :data="templateTree"
                    :props="{ label: 'name', children: 'children' }"
                    default-expand-all
                    class="template-tree"
                  >
                    <template #default="{ node, data }">
                      <span class="custom-tree-node">
                        <el-icon><Folder /></el-icon>
                        <span>{{ node.label }}</span>
                      </span>
                    </template>
                  </el-tree>
                </div>
              </div>
            </div>
            
            <div class="config-actions">
              <el-button type="primary" @click="handleSaveTemplates">保存模板</el-button>
              <el-button @click="resetTemplates">重置</el-button>
            </div>
          </div>
        </div>
      </el-tab-pane>
      
    </el-tabs>
    
    <el-dialog v-model="quotaVisible" title="调整存储配额" width="400px" draggable>
      <el-form label-width="100px">
        <el-form-item label="当前配额">
          {{ formatBytes(selectedUser?.storageQuota) }}
        </el-form-item>
        <el-form-item label="新配额">
          <el-input-number
            v-model="newQuotaGb"
            :min="1"
            :max="100"
          />
          <span class="ml-2">GB</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="quotaVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSetQuota">确认</el-button>
      </template>
    </el-dialog>
    
    <el-dialog v-model="addUserVisible" title="新增用户" width="500px" draggable>
      <el-form :model="newUser" label-width="100px">
        <el-form-item label="用户名">
          <el-input v-model="newUser.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="newUser.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="newUser.confirmPassword" type="password" placeholder="请确认密码" show-password />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="newUser.role" placeholder="请选择角色">
            <el-option label="用户" value="USER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="newUser.email" type="email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机">
          <el-input v-model="newUser.phone" placeholder="请输入手机号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addUserVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmAddUser">确认添加</el-button>
      </template>
    </el-dialog>
    
    <el-dialog v-model="editUserVisible" title="修改用户信息" width="500px" draggable>
      <el-form :model="editUser" label-width="100px">
        <el-form-item label="用户名">
          <el-input v-model="editUser.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="editUser.role" placeholder="请选择角色">
            <el-option label="用户" value="USER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editUser.email" type="email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机">
          <el-input v-model="editUser.phone" placeholder="请输入手机号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editUserVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmEditUser">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Plus, Delete, Folder } from '@element-plus/icons-vue'
import { adminAPI } from '@/api'

const activeTab = ref('users')

const selectedUsers = ref([])

const users = ref([])
const usersLoading = ref(false)
const userSearch = ref('')
const userPage = ref(1)
const userPageSize = ref(10)
const totalUsers = ref(0)

const quotaVisible = ref(false)
const selectedUser = ref(null)
const newQuotaGb = ref(1)

const addUserVisible = ref(false)
const newUser = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  role: 'USER',
  email: '',
  phone: ''
})

const editUserVisible = ref(false)
const editingUserId = ref(null)
const editUser = reactive({
  username: '',
  role: 'USER',
  email: '',
  phone: ''
})

const loadUsers = async () => {
  usersLoading.value = true
  
  try {
    const res = await adminAPI.getUsers({
      page: userPage.value - 1,
      size: userPageSize.value,
      keyword: userSearch.value
    })
    
    users.value = res.success ? (res.data?.content || []) : []
    totalUsers.value = res.success ? (res.data?.total || 0) : 0
  } catch (error) {
    console.error('加载用户失败:', error)
  } finally {
    usersLoading.value = false
  }
}

const handleSelectionChange = (val) => {
  selectedUsers.value = val
}

const showQuotaDialog = (user) => {
  selectedUser.value = user
  newQuotaGb.value = Math.ceil(user.storageQuota / 1073741824)
  quotaVisible.value = true
}

const handleSetQuota = async () => {
  try {
    await adminAPI.setQuota(selectedUser.value.id, newQuotaGb.value * 1073741824)
    ElMessage.success('配额调整成功')
    quotaVisible.value = false
    loadUsers()
  } catch (error) {
    ElMessage.error('调整失败')
  }
}

const handleResetPassword = async (user) => {
  try {
    await ElMessageBox.confirm(
      `确定要重置用户 "${user.username}" 的密码吗？重置后密码为 123456`,
      '重置密码',
      { type: 'warning' }
    )
    
    await adminAPI.resetPassword(user.id)
    ElMessage.success('密码已重置为 123456')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('重置失败')
    }
  }
}

const handleDeleteUser = async (user) => {
  if (user.role === 'ADMIN') {
    ElMessage.warning('不能删除管理员账户')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 "${user.username}" 吗？`,
      '删除用户',
      { type: 'error' }
    )
    
    await adminAPI.deleteUser(user.id)
    ElMessage.success('用户已删除')
    loadUsers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedUsers.value.length} 个用户吗？`,
      '批量删除',
      { type: 'error' }
    )
    
    for (const user of selectedUsers.value) {
      if (user.role !== 'ADMIN') {
        await adminAPI.deleteUser(user.id)
      }
    }
    ElMessage.success('批量删除成功')
    selectedUsers.value = []
    loadUsers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleEditUser = (row) => {
  editingUserId.value = row.id
  editUser.username = row.username
  editUser.role = row.role
  editUser.email = row.email || ''
  editUser.phone = row.phone || ''
  editUserVisible.value = true
}

const handleConfirmEditUser = async () => {
  if (!editUser.username.trim()) {
    ElMessage.warning('请输入用户名')
    return
  }
  
  try {
    await adminAPI.updateUser(editingUserId.value, {
      username: editUser.username,
      role: editUser.role,
      email: editUser.email,
      phone: editUser.phone
    })
    ElMessage.success('用户信息修改成功')
    editUserVisible.value = false
    loadUsers()
  } catch (error) {
    ElMessage.error('修改失败')
  }
}

const handleAddUser = () => {
  newUser.username = ''
  newUser.password = ''
  newUser.confirmPassword = ''
  newUser.role = 'USER'
  newUser.email = ''
  newUser.phone = ''
  addUserVisible.value = true
}

const handleConfirmAddUser = async () => {
  if (!newUser.username.trim()) {
    ElMessage.warning('请输入用户名')
    return
  }
  if (!newUser.password.trim()) {
    ElMessage.warning('请输入密码')
    return
  }
  if (newUser.password !== newUser.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }
  
  try {
    await adminAPI.register(newUser.username, newUser.password, newUser.role)
    ElMessage.success('用户添加成功')
    addUserVisible.value = false
    loadUsers()
  } catch (error) {
    ElMessage.error('添加失败')
  }
}

const whitelistForm = reactive({
  extensions: ''
})

const originalWhitelist = ref('')
const originalTemplates = ref('')

const DEFAULT_TEMPLATES = `文档/工作/报告
文档/个人/笔记
资源/图片
资源/模板`

const DEFAULT_WHITELIST = '.pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.jpg,.png,.zip,.txt,.md'

const loadWhitelist = async () => {
  try {
    const res = await adminAPI.getConfig()
    const value = res.success ? (res.data?.allowed_file_extensions?.configValue || DEFAULT_WHITELIST) : DEFAULT_WHITELIST
    whitelistForm.extensions = value
    originalWhitelist.value = value
  } catch (error) {
    console.error('加载配置失败:', error)
    whitelistForm.extensions = DEFAULT_WHITELIST
    originalWhitelist.value = DEFAULT_WHITELIST
  }
}

const resetWhitelist = () => {
  whitelistForm.extensions = originalWhitelist.value
  ElMessage.info('已重置为已保存的配置')
}

const handleSaveWhitelist = async () => {
  if (!whitelistForm.extensions.trim()) {
    ElMessage.warning('请输入允许的扩展名')
    return
  }
  
  try {
    await adminAPI.updateConfig({
      allowed_file_extensions: whitelistForm.extensions.trim()
    })
    originalWhitelist.value = whitelistForm.extensions
    ElMessage.success('配置保存成功')
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const templateForm = reactive({
  templates: ''
})

const templateTree = computed(() => {
  if (!templateForm.templates.trim()) {
    return []
  }
  
  const paths = templateForm.templates.split('\n').filter(p => p.trim())
  const root = { name: '根目录', children: [] }
  
  paths.forEach(path => {
    const parts = path.trim().split('/').filter(p => p)
    let current = root
    
    parts.forEach(part => {
      let child = current.children.find(c => c.name === part)
      if (!child) {
        child = { name: part, children: [] }
        current.children.push(child)
      }
      current = child
    })
  })
  
  return root.children
})

const loadTemplates = async () => {
  try {
    const res = await adminAPI.getFolderTemplates()
    const value = res.success ? (res.data?.configValue || DEFAULT_TEMPLATES) : DEFAULT_TEMPLATES
    templateForm.templates = value
    originalTemplates.value = value
  } catch (error) {
    console.error('加载模板失败:', error)
    templateForm.templates = DEFAULT_TEMPLATES
    originalTemplates.value = DEFAULT_TEMPLATES
  }
}

const resetTemplates = () => {
  templateForm.templates = originalTemplates.value
  ElMessage.info('已重置为已保存的模板')
}

const handleSaveTemplates = async () => {
  if (!templateForm.templates.trim()) {
    ElMessage.warning('请输入文件夹模板')
    return
  }
  
  try {
    await adminAPI.updateFolderTemplates({
      templates: templateForm.templates.trim()
    })
    originalTemplates.value = templateForm.templates
    ElMessage.success('模板保存成功')
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const formatBytes = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const handleTabChange = (tab) => {
  if (tab === 'users') {
    loadUsers()
  } else if (tab === 'whitelist') {
    loadWhitelist()
  } else if (tab === 'templates') {
    loadTemplates()
  }
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.admin-page {
  background: white;
  border-radius: 12px;
  padding: 0;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  height: calc(100vh - 140px);
  overflow: hidden;
}

.admin-tabs {
  height: 100%;
}

.admin-tabs :deep(.el-tabs__content) {
  height: calc(100% - 44px);
  overflow: hidden;
}

.tab-content {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid #e8e8e8;
  background: #fafafa;
}

.search-input {
  width: 280px;
}

.toolbar-actions {
  display: flex;
  gap: 12px;
}

.tab-content :deep(.el-table) {
  flex: 1;
  overflow-y: auto;
}

.tab-content :deep(.el-table th) {
  padding: 14px 16px;
  font-size: 14px;
  font-weight: 600;
  background: #fafafa;
}

.tab-content :deep(.el-table td) {
  padding: 14px 16px;
  font-size: 14px;
}

.tab-content :deep(.el-table--striped .el-table__body tr.el-table__row--striped td) {
  background: #fcfcfc;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #409eff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  font-weight: 600;
}

.action-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 20px 24px;
  border-top: 1px solid #e8e8e8;
  gap: 20px;
}

.total-count {
  font-size: 14px;
  color: #666;
}

.config-section {
  padding: 24px;
}

.config-header {
  margin-bottom: 24px;
}

.config-header h3 {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 12px 0;
  color: #303133;
}

.config-desc {
  font-size: 14px;
  color: #909399;
  margin: 0 0 6px 0;
  line-height: 1.6;
}

.form-tip {
  font-size: 13px;
  color: #909399;
  margin-top: 10px;
  line-height: 1.5;
}

.config-actions {
  margin-top: 24px;
  display: flex;
  gap: 12px;
}

.template-tree {
  overflow-y: auto;
  background: white;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  padding: 12px;
  font-size: 14px;
}

.custom-tree-node {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #303133;
}

.custom-tree-node svg {
  color: #409eff;
}

.template-layout {
  display: flex;
  gap: 24px;
  margin-top: 16px;
}

.template-left {
  flex: 1;
  min-width: 0;
}

.template-right {
  flex: 1;
  min-width: 0;
}

.template-label {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 10px;
}

.template-preview-box {
  background: #fafafa;
  border: 1px solid #e8e8e8;
  border-radius: 10px;
  padding: 16px;
  min-height: 320px;
}

.whitelist-layout {
  display: flex;
  gap: 24px;
  margin-top: 16px;
}

.whitelist-left {
  flex: 1;
  min-width: 0;
}

.whitelist-right {
  flex: 1;
  min-width: 0;
}

.preview-example {
  background: #f5f5f5;
  padding: 20px;
  border-radius: 10px;
  font-family: monospace;
  font-size: 14px;
  color: #606266;
  line-height: 1.8;
  border: 1px solid #e8e8e8;
  min-height: 200px;
}

.ml-2 {
  margin-left: 8px;
}
</style>
