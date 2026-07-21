<template>
  <div class="dashboard">
    <!-- 侧边栏 -->
    <aside class="sidebar" :class="{ collapsed: sidebarCollapsed }">
      <div class="sidebar-header">
        <span class="logo-text">知识库</span>
      </div>
      
      <!-- 导航菜单 -->
      <nav class="sidebar-nav">
        <div class="nav-group">
          <div 
            class="nav-group-title"
            @click="toggleKnowledgeBase"
          >
            <el-icon class="nav-icon"><FolderOpened /></el-icon>
            <span class="nav-text" v-show="!sidebarCollapsed">知识库</span>
            <el-icon class="nav-arrow" :class="{ expanded: knowledgeBaseExpanded }" v-show="!sidebarCollapsed">
              <ArrowRight />
            </el-icon>
            <el-tooltip v-if="sidebarCollapsed" content="知识库" placement="right" effect="dark">
              <span class="tooltip-anchor"></span>
            </el-tooltip>
          </div>
          <div v-show="knowledgeBaseExpanded" class="nav-group-items">
            <div 
              class="nav-item root-folder"
              :class="{ active: !folderStore.currentFolder && route.path === '/dashboard' }"
              @click="handleFolderSelect(null); router.push('/dashboard')"
            >
              <el-icon class="nav-icon"><HomeFilled /></el-icon>
              <span class="nav-text" v-show="!sidebarCollapsed">全部文件</span>
              <el-tooltip v-if="sidebarCollapsed" content="全部文件" placement="right" effect="dark">
                <span class="tooltip-anchor"></span>
              </el-tooltip>
              <el-icon class="nav-arrow expanded" v-show="!sidebarCollapsed">
                <ArrowRight />
              </el-icon>
            </div>
            <div class="folder-tree-container">
              <div v-for="folder in rootFolders" :key="folder.id">
                <div 
                  class="nav-item folder-item"
                  :class="{ active: folderStore.currentFolder?.id === folder.id }"
                  @click="handleFolderSelect(folder)"
                  @contextmenu="handleFolderContextMenu($event, folder)"
                >
                  <el-icon class="nav-icon"><FolderOpened /></el-icon>
                  <template v-if="editingFolderId === folder.id">
                    <el-input 
                      v-model="editingFolderName" 
                      class="folder-edit-input"
                      size="small"
                      @blur="handleRenameConfirm(folder)"
                      @keyup.enter="handleRenameConfirm(folder)"
                      @keyup.esc="handleRenameCancel(folder)"
                      ref="renameInputRef"
                    />
                  </template>
                  <template v-else>
                    <span class="nav-text" v-show="!sidebarCollapsed">{{ folder.name }}</span>
                  </template>
                  <el-tooltip v-if="sidebarCollapsed && editingFolderId !== folder.id" :content="folder.name" placement="right" effect="dark">
                    <span class="tooltip-anchor"></span>
                  </el-tooltip>
                  <el-icon v-if="hasChildren(folder)" class="nav-arrow" :class="{ expanded: expandedFolders.includes(folder.id) }" v-show="!sidebarCollapsed" @click.stop="toggleFolderExpand(folder.id)">
                    <ArrowRight />
                  </el-icon>
                </div>
                <div v-if="expandedFolders.includes(folder.id)" class="sub-folders">
                  <template v-for="subFolder in getChildren(folder)" :key="subFolder.id">
                    <div 
                      class="nav-item sub-folder-item"
                      :class="{ active: folderStore.currentFolder?.id === subFolder.id }"
                      @click="handleFolderSelect(subFolder)"
                      @contextmenu="handleFolderContextMenu($event, subFolder)"
                    >
                      <el-icon class="nav-icon"><Folder /></el-icon>
                      <template v-if="editingFolderId === subFolder.id">
                        <el-input 
                          v-model="editingFolderName" 
                          class="folder-edit-input"
                          size="small"
                          @blur="handleRenameConfirm(subFolder)"
                          @keyup.enter="handleRenameConfirm(subFolder)"
                          @keyup.esc="handleRenameCancel(subFolder)"
                        />
                      </template>
                      <template v-else>
                        <span class="nav-text" v-show="!sidebarCollapsed">{{ subFolder.name }}</span>
                      </template>
                      <el-tooltip v-if="sidebarCollapsed && editingFolderId !== subFolder.id" :content="subFolder.name" placement="right" effect="dark">
                        <span class="tooltip-anchor"></span>
                      </el-tooltip>
                      <el-icon v-if="hasChildren(subFolder)" class="nav-arrow" :class="{ expanded: expandedFolders.includes(subFolder.id) }" v-show="!sidebarCollapsed" @click.stop="toggleFolderExpand(subFolder.id)">
                        <ArrowRight />
                      </el-icon>
                    </div>
                    <div v-if="expandedFolders.includes(subFolder.id)" class="sub-folders">
                      <template v-for="deepFolder in getChildren(subFolder)" :key="deepFolder.id">
                        <div 
                          class="nav-item deep-folder-item"
                          :class="{ active: folderStore.currentFolder?.id === deepFolder.id }"
                          @click="handleFolderSelect(deepFolder)"
                          @contextmenu="handleFolderContextMenu($event, deepFolder)"
                        >
                          <el-icon class="nav-icon"><Folder /></el-icon>
                          <template v-if="editingFolderId === deepFolder.id">
                            <el-input 
                              v-model="editingFolderName" 
                              class="folder-edit-input"
                              size="small"
                              @blur="handleRenameConfirm(deepFolder)"
                              @keyup.enter="handleRenameConfirm(deepFolder)"
                              @keyup.esc="handleRenameCancel(deepFolder)"
                            />
                          </template>
                          <template v-else>
                            <span class="nav-text" v-show="!sidebarCollapsed">{{ deepFolder.name }}</span>
                          </template>
                          <el-tooltip v-if="sidebarCollapsed && editingFolderId !== deepFolder.id" :content="deepFolder.name" placement="right" effect="dark">
                            <span class="tooltip-anchor"></span>
                          </el-tooltip>
                        </div>
                      </template>
                    </div>
                  </template>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <div v-if="isAdmin" class="nav-group">
          <div 
            class="nav-group-title"
            @click="toggleAdmin"
          >
            <el-icon class="nav-icon"><Setting /></el-icon>
            <span class="nav-text" v-show="!sidebarCollapsed">系统管理</span>
            <el-icon class="nav-arrow" :class="{ expanded: adminExpanded }" v-show="!sidebarCollapsed">
              <ArrowRight />
            </el-icon>
            <el-tooltip v-if="sidebarCollapsed" content="系统管理" placement="right" effect="dark">
              <span class="tooltip-anchor"></span>
            </el-tooltip>
          </div>
          <div v-show="adminExpanded" class="nav-group-items">
            <div 
              class="nav-item"
              :class="{ active: route.path === '/dashboard/admin' }"
              @click="router.push('/dashboard/admin')"
            >
              <span class="nav-text" v-show="!sidebarCollapsed">用户管理</span>
              <el-tooltip v-if="sidebarCollapsed" content="用户管理" placement="right" effect="dark">
                <span class="tooltip-anchor"></span>
              </el-tooltip>
            </div>
            <div 
              class="nav-item"
              :class="{ active: route.path === '/dashboard/system-config' }"
              @click="router.push('/dashboard/system-config')"
            >
              <span class="nav-text" v-show="!sidebarCollapsed">系统配置</span>
              <el-tooltip v-if="sidebarCollapsed" content="系统配置" placement="right" effect="dark">
                <span class="tooltip-anchor"></span>
              </el-tooltip>
            </div>
            <div 
              class="nav-item"
              :class="{ active: route.path === '/dashboard/folder-templates' }"
              @click="router.push('/dashboard/folder-templates')"
            >
              <span class="nav-text" v-show="!sidebarCollapsed">文件夹模板</span>
              <el-tooltip v-if="sidebarCollapsed" content="文件夹模板" placement="right" effect="dark">
                <span class="tooltip-anchor"></span>
              </el-tooltip>
            </div>
            <div 
              class="nav-item"
              :class="{ active: route.path === '/dashboard/operation-logs' }"
              @click="router.push('/dashboard/operation-logs')"
            >
              <span class="nav-text" v-show="!sidebarCollapsed">操作日志</span>
              <el-tooltip v-if="sidebarCollapsed" content="操作日志" placement="right" effect="dark">
                <span class="tooltip-anchor"></span>
              </el-tooltip>
            </div>
            <div 
              class="nav-item"
              :class="{ active: route.path === '/dashboard/all-files' }"
              @click="router.push('/dashboard/all-files')"
            >
              <span class="nav-text" v-show="!sidebarCollapsed">所有文件</span>
              <el-tooltip v-if="sidebarCollapsed" content="所有文件" placement="right" effect="dark">
                <span class="tooltip-anchor"></span>
              </el-tooltip>
            </div>
            <div 
              class="nav-item"
              :class="{ active: route.path === '/dashboard/tags' }"
              @click="router.push('/dashboard/tags')"
            >
              <span class="nav-text" v-show="!sidebarCollapsed">标签管理</span>
              <el-tooltip v-if="sidebarCollapsed" content="标签管理" placement="right" effect="dark">
                <span class="tooltip-anchor"></span>
              </el-tooltip>
            </div>
          </div>
        </div>
      </nav>
      
    </aside>
    
    <!-- 主内容区 -->
    <main class="main-content">
      <header class="main-header">
        <div class="header-left">
          <el-button class="sidebar-toggle-btn" @click="toggleSidebar">
            <svg class="sidebar-toggle-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <line x1="5" y1="6" x2="19" y2="6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              <line x1="5" y1="12" x2="19" y2="12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              <line x1="5" y1="18" x2="19" y2="18" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              <polygon v-if="!sidebarCollapsed" points="15,10 19,12 15,14" fill="currentColor"/>
              <polygon v-else points="9,10 5,12 9,14" fill="currentColor"/>
            </svg>
          </el-button>
          <div class="breadcrumb">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item @click="handleFolderSelect(null); router.push('/dashboard')">知识库</el-breadcrumb-item>
              <template v-if="breadcrumbItems.length > 0">
                <el-breadcrumb-item 
                  v-for="(item, index) in breadcrumbItems" 
                  :key="item.id"
                  :class="{ active: index === breadcrumbItems.length - 1 }"
                  @click="handleFolderSelect(item)"
                >
                  {{ item.name }}
                </el-breadcrumb-item>
              </template>
              <el-breadcrumb-item v-else>全部文件</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
        </div>
        <div class="header-right">
          <div class="header-search">
            <el-input 
              v-model="quickSearch" 
              placeholder="快速搜索..." 
              clearable
              size="small"
              class="quick-search-input"
              @keyup.enter="handleQuickSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>
          <div class="storage-info">
            <div class="storage-bar-container">
              <div class="storage-bar">
                <div class="storage-fill" :style="{ width: storagePercentage + '%' }"></div>
              </div>
              <span class="storage-text">{{ formatBytes(storageUsed) }} / {{ formatBytes(storageQuota) }}</span>
            </div>
          </div>
          <div class="header-icons">
            <el-tooltip content="搜索" placement="bottom">
              <el-button class="icon-btn" @click="toggleSearch">
                <el-icon><Search /></el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip content="通知" placement="bottom">
              <el-button class="icon-btn" @click="toggleNotification">
                <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notification-badge">
                  <el-icon><Bell /></el-icon>
                </el-badge>
              </el-button>
            </el-tooltip>
            <el-tooltip :content="isFullscreen ? '退出全屏' : '全屏'" placement="bottom">
              <el-button class="icon-btn" @click="toggleFullscreen">
                <el-icon><ZoomIn v-if="!isFullscreen" /><ZoomOut v-else /></el-icon>
              </el-button>
            </el-tooltip>
            <el-dropdown @command="handleLanguageChange" trigger="click">
              <span class="language-btn">
                <svg viewBox="0 0 24 24" width="22" height="22">
                  <text x="10" y="14" font-size="11" font-weight="bold" text-anchor="middle" fill="currentColor">文</text>
                  <text x="14" y="19" font-size="9" font-weight="bold" text-anchor="middle" fill="currentColor">A</text>
                </svg>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item :command="'zh'" :disabled="currentLanguage === '中文'">中文</el-dropdown-item>
                  <el-dropdown-item :command="'en'" :disabled="currentLanguage === 'English'">English</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
          <el-dropdown trigger="click" @command="handleUserCommand" class="user-dropdown">
            <div class="user-info" v-if="userInfo">
              <div class="avatar">
                {{ userInfo.username?.charAt(0).toUpperCase() || 'U' }}
              </div>
              <span class="username">{{ userInfo.username || '未知用户' }}</span>
              <el-icon class="dropdown-arrow"><ArrowRight /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="settings">
                  设置
                </el-dropdown-item>
                <el-dropdown-item command="help">
                  帮助与反馈
                </el-dropdown-item>
                <el-dropdown-item divided command="switch">
                  切换账号
                </el-dropdown-item>
                <el-dropdown-item command="logout">
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>
      

      <div class="content-body">
        <router-view />
      </div>
    </main>
    
    <el-dialog 
      v-model="showSearchModal" 
      title="" 
      width="600px" 
      draggable
      :close-on-click-modal="false"
      class="search-modal"
    >
      <div class="search-modal-body">
        <el-input 
          v-model="searchKeyword" 
          placeholder="请输入关键词搜索" 
          class="search-modal-input"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        
        <div v-if="!searchKeyword || searchResults.length === 0" class="search-empty">
          <el-icon class="empty-icon"><FolderOpened /></el-icon>
          <span class="empty-text">无数据</span>
        </div>
        
        <div v-else class="search-results">
          <div 
            v-for="(result, index) in searchResults" 
            :key="result.id"
            class="search-item"
            :class="{ active: selectedIndex === index }"
            @click="selectResult(result)"
            @mouseenter="selectedIndex = index"
          >
            <el-icon><FolderOpened /></el-icon>
            <span>{{ result.name }}</span>
          </div>
        </div>
      </div>
      
      <template #footer>
        <div class="search-footer">
          <el-button @click="confirmSelection" class="footer-btn">
            <el-icon><ArrowLeft /></el-icon>
            确认
          </el-button>
          <el-button @click="moveUp" class="footer-btn">
            <el-icon><ArrowUp /></el-icon>
          </el-button>
          <el-button @click="moveDown" class="footer-btn">
            <el-icon><ArrowDown /></el-icon>
          </el-button>
          <el-button @click="toggleSelection" class="footer-btn">切换</el-button>
          <el-button @click="showSearchModal = false" class="footer-btn esc-btn">
            <span class="esc-label">ESC</span>关闭
          </el-button>
        </div>
      </template>
    </el-dialog>
    
    <el-dialog 
      v-model="showNotificationModal" 
      title="" 
      width="400px" 
      draggable
      :close-on-click-modal="false"
      class="notification-modal"
    >
      <div class="notification-body">
        <div class="notification-header">
          <span class="notification-title">通知公告</span>
          <el-button class="mark-all-read" size="small" @click="markAllRead">
            <el-icon><Message /></el-icon>
          </el-button>
        </div>
        
        <div class="notification-list">
        <div 
          v-for="notification in notifications" 
          :key="notification.id"
          class="notification-item"
          :class="{ unread: !notification.read, read: notification.read }"
          @click="markAsRead(notification)"
        >
          <div class="notification-content">
            <span class="notification-text">{{ notification.content }}</span>
            <span v-if="!notification.read" class="unread-badge">未读</span>
            <span v-else class="read-badge">已读</span>
          </div>
          <span class="notification-time">{{ notification.time }}</span>
        </div>
        
        <div v-if="notifications.length === 0" class="notification-empty">
          <el-icon class="empty-icon"><Bell /></el-icon>
          <span class="empty-text">暂无通知</span>
        </div>
        </div>
      </div>
    </el-dialog>
    
    <!-- 创建文件夹对话框 -->
    <el-dialog v-model="createFolderVisible" title="创建文件夹" width="400px" draggable>
      <el-form :model="newFolder" label-width="100px">
        <el-form-item label="文件夹名称" required>
          <el-input v-model="newFolder.name" placeholder="请输入文件夹名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createFolderVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreateFolder">创建</el-button>
      </template>
    </el-dialog>
    
    <!-- 修改密码对话框 -->
    <el-dialog v-model="passwordVisible" title="修改密码" width="400px" draggable>
      <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="100px">
        <el-form-item label="当前密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordVisible = false">取消</el-button>
        <el-button type="primary" @click="handleChangePassword">确认修改</el-button>
      </template>
    </el-dialog>
    
    <ContextMenu 
      :visible="contextMenuVisible"
      :position="contextMenuPosition"
      :folder="contextMenuFolder"
      @close="closeContextMenu"
      @rename="handleRenameFolder"
      @delete="handleDeleteFolder"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  FolderOpened, Folder, Plus, Setting, User, MoreFilled, Key, SwitchButton, HomeFilled, ArrowRight,
  Search, Bell, ZoomIn, ZoomOut, Menu, ArrowUp, ArrowDown, Message, ArrowLeft
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useFolderStore } from '@/stores/folder'
import { folderAPI, tagAPI } from '@/api'
import FolderTree from '@/components/FolderTree.vue'
import ContextMenu from '@/components/ContextMenu.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const folderStore = useFolderStore()

const userInfo = computed(() => userStore.userInfo)
const isAdmin = computed(() => userStore.isAdmin)

const knowledgeBaseExpanded = ref(true)
const adminExpanded = ref(false)
const sidebarCollapsed = ref(false)

const showSearchModal = ref(false)
const searchKeyword = ref('')
const searchResults = ref([])
const selectedIndex = ref(0)

const showNotificationModal = ref(false)
const isFullscreen = ref(false)
const currentLanguage = ref('中文')
const notifications = ref([
  {
    id: 1,
    content: '欢迎登录 KMatrix后台管理系统',
    time: '2026/7/11 15:38:16',
    read: false
  }
])

const unreadCount = computed(() => notifications.value.filter(n => !n.read).length)

const toggleSearch = () => {
  showSearchModal.value = !showSearchModal.value
  if (showSearchModal.value) {
    searchKeyword.value = ''
    searchResults.value = []
    selectedIndex.value = 0
  }
}

const toggleNotification = () => {
  showNotificationModal.value = !showNotificationModal.value
}

const markAsRead = (notification) => {
  notification.read = true
}

const markAllRead = () => {
  notifications.value.forEach(n => n.read = true)
}

const handleSearch = () => {
  if (!searchKeyword.value.trim()) {
    searchResults.value = []
    return
  }
  
  searchResults.value = folders.value.filter(folder => 
    folder.name.includes(searchKeyword.value.trim())
  )
  selectedIndex.value = 0
}

const moveUp = () => {
  if (selectedIndex.value > 0) {
    selectedIndex.value--
  }
}

const moveDown = () => {
  if (selectedIndex.value < searchResults.value.length - 1) {
    selectedIndex.value++
  }
}

const toggleSelection = () => {
  if (searchResults.value.length > 0) {
    selectedIndex.value = (selectedIndex.value + 1) % searchResults.value.length
  }
}

const selectResult = (result) => {
  const index = searchResults.value.findIndex(r => r.id === result.id)
  selectedIndex.value = index
}

const confirmSelection = () => {
  if (searchResults.value.length > 0 && searchResults.value[selectedIndex.value]) {
    handleFolderSelect(searchResults.value[selectedIndex.value])
    showSearchModal.value = false
  }
}

const toggleKnowledgeBase = () => {
  knowledgeBaseExpanded.value = !knowledgeBaseExpanded.value
}

const toggleAdmin = () => {
  adminExpanded.value = !adminExpanded.value
}

const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

// 文件夹数据
const expandedFolders = ref([])

const rootFolders = computed(() => {
  return folderStore.folders.filter(f => !f.parentId)
})

const hasChildren = (folder) => {
  return folderStore.folders.some(f => f.parentId === folder.id)
}

const getChildren = (folder) => {
  return folderStore.folders.filter(f => f.parentId === folder.id)
}

const toggleFolderExpand = (folderId) => {
  const index = expandedFolders.value.indexOf(folderId)
  if (index === -1) {
    expandedFolders.value.push(folderId)
  } else {
    expandedFolders.value.splice(index, 1)
  }
}

const breadcrumbItems = computed(() => {
  if (!folderStore.currentFolder) return []
  
  const items = []
  let current = folderStore.currentFolder
  
  while (current) {
    items.unshift(current)
    current = folderStore.folders.find(f => f.id === current.parentId)
  }
  
  return items
})

// 标签数据
const tags = ref([])

// 导航指示条位置
const indicatorTop = computed(() => {
  const path = route.path
  if (path === '/') return 0
  if (path === '/dashboard') return 48
  if (path === '/tags') return 96
  if (path === '/admin') return 144
  return 0
})

// 存储空间
const storageUsed = ref(0)
const storageQuota = ref(1073741824) // 1GB
const storagePercentage = computed(() => {
  return Math.min(100, Math.round((storageUsed.value / storageQuota.value) * 100))
})

const formatStorage = (percentage) => {
  const used = formatBytes(storageUsed.value)
  const total = formatBytes(storageQuota.value)
  return `${used} / ${total}`
}

const formatBytes = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 快速搜索
const quickSearch = ref('')
const handleQuickSearch = () => {
  if (quickSearch.value.trim()) {
    ElMessage.info(`搜索: ${quickSearch.value}`)
    quickSearch.value = ''
  }
}

// 创建文件夹
const createFolderVisible = ref(false)
const newFolder = reactive({
  name: ''
})

const showCreateFolderDialog = () => {
  newFolder.name = ''
  createFolderVisible.value = true
}

const handleCreateFolder = async () => {
  if (!newFolder.name.trim()) {
    ElMessage.warning('请输入文件夹名称')
    return
  }
  
  try {
    const parentId = folderStore.currentFolder?.id || null
    await folderAPI.createFolder(newFolder.name, parentId)
    ElMessage.success('文件夹创建成功')
    createFolderVisible.value = false
    loadFolders()
  } catch (error) {
    ElMessage.error('创建失败')
  }
}

// 右键菜单
const contextMenuVisible = ref(false)
const contextMenuPosition = reactive({ x: 0, y: 0 })
const contextMenuFolder = ref(null)

const editingFolderId = ref(null)
const editingFolderName = ref('')
const renameInputRef = ref(null)

const handleFolderContextMenu = (event, folder) => {
  event.preventDefault()
  contextMenuPosition.x = event.clientX
  contextMenuPosition.y = event.clientY
  contextMenuFolder.value = folder
  contextMenuVisible.value = true
}

const closeContextMenu = () => {
  contextMenuVisible.value = false
  contextMenuFolder.value = null
}

const handleRenameFolder = (folder) => {
  editingFolderId.value = folder.id
  editingFolderName.value = folder.name
  nextTick(() => {
    renameInputRef.value?.focus()
  })
}

const handleRenameConfirm = async (folder) => {
  const newName = editingFolderName.value.trim()
  if (!newName) {
    ElMessage.warning('请输入文件夹名称')
    editingFolderName.value = folder.name
    return
  }
  
  if (newName === folder.name) {
    editingFolderId.value = null
    return
  }
  
  const siblings = folderStore.folders.filter(f => f.parentId === folder.parentId && f.id !== folder.id)
  if (siblings.some(f => f.name === newName)) {
    ElMessage.warning('该名称已存在')
    editingFolderName.value = folder.name
    return
  }
  
  try {
    await folderAPI.renameFolder(folder.id, newName)
    ElMessage.success('重命名成功')
    editingFolderId.value = null
    loadFolders()
  } catch (error) {
    ElMessage.error('重命名失败')
    editingFolderName.value = folder.name
  }
}

const handleRenameCancel = (folder) => {
  editingFolderId.value = null
  editingFolderName.value = folder.name
}

const handleDeleteFolder = async (folder) => {
  await ElMessageBox.confirm(
    '确定要删除该文件夹吗？删除后文件夹内的所有文件也将被删除！',
    '删除确认',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await folderAPI.deleteFolder(folder.id)
      ElMessage.success('删除成功')
      if (folderStore.currentFolder?.id === folder.id) {
        folderStore.setCurrentFolder(null)
      }
      loadFolders()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {
    ElMessage.info('已取消删除')
  })
}

// 修改密码
const passwordVisible = ref(false)
const passwordFormRef = ref()
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少6个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const showPasswordDialog = () => {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordVisible.value = true
}

const handleChangePassword = async () => {
  const valid = await passwordFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  const result = await userStore.changePassword(passwordForm.oldPassword, passwordForm.newPassword)
  if (result.success) {
    ElMessage.success('密码修改成功，请重新登录')
    passwordVisible.value = false
    handleLogout()
  } else {
    ElMessage.error(result.message)
  }
}

// 文件夹选择
const handleFolderSelect = (folder) => {
  if (folder) {
    folderStore.setCurrentFolder(folder)
  } else {
    folderStore.clearCurrentFolder()
  }
  router.push('/dashboard')
}

// 加载标签
const loadTags = async () => {
  try {
    const res = await tagAPI.getTags()
    tags.value = res.data?.content || res.data || []
  } catch (error) {
    console.error('加载标签失败:', error)
  }
}

// 用户命令处理
const handleUserCommand = (command) => {
  switch (command) {
    case 'settings':
      showPasswordDialog()
      break
    case 'help':
      ElMessage.info('帮助与反馈功能开发中')
      break
    case 'switch':
      localStorage.clear()
      router.push('/login')
      break
    case 'logout':
      handleLogout()
      break
  }
}

const handleLanguageChange = (lang) => {
  if (lang === 'zh') {
    currentLanguage.value = '中文'
  } else if (lang === 'en') {
    currentLanguage.value = 'English'
  }
}

const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
    isFullscreen.value = true
  } else {
    document.exitFullscreen()
    isFullscreen.value = false
  }
}

// 登出
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm(
      '确认退出登录吗？',
      isAdmin.value ? '消息' : '提示',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: isAdmin.value ? 'success' : 'info'
      }
    )
    await userStore.logout()
    router.push('/login')
  } catch {
    ElMessage.info('已取消退出登录')
  }
}

watch(() => folderStore.folders, () => {
  const currentFolder = folderStore.currentFolder
  if (currentFolder && currentFolder.parentId) {
    const parentId = currentFolder.parentId
    if (!expandedFolders.value.includes(parentId)) {
      expandedFolders.value.push(parentId)
    }
    const grandparent = folderStore.folders.find(f => f.id === parentId)
    if (grandparent && grandparent.parentId && !expandedFolders.value.includes(grandparent.parentId)) {
      expandedFolders.value.push(grandparent.parentId)
    }
  }
}, { deep: true })

onMounted(() => {
  loadFolders()
  loadTags()
  storageUsed.value = userInfo.value.storage_used || 0
  storageQuota.value = userInfo.value.storage_quota || 1073741824
  document.addEventListener('click', closeContextMenu)
})

onUnmounted(() => {
  document.removeEventListener('click', closeContextMenu)
})

const loadFolders = async () => {
  try {
    const res = await folderAPI.getFolders()
    folderStore.setFolders(res.data || [])
  } catch (error) {
    console.error('加载文件夹失败:', error)
  }
}
</script>

<style scoped>
.dashboard {
  display: flex;
  min-height: 100vh;
  background: #f5f7fa;
}

.sidebar {
  width: 240px;
  background: #ffffff;
  color: #333;
  display: flex;
  flex-direction: column;
  border-right: 1px solid #e8e8e8;
  transition: width 0.3s ease;
}

.sidebar.collapsed {
  width: 60px;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #e8e8e8;
}



.sidebar.collapsed .sidebar-header {
  padding: 16px 12px;
}

.sidebar.collapsed .nav-group-title {
  padding: 12px;
  justify-content: center;
}

.sidebar.collapsed .nav-item {
  padding: 10px;
  justify-content: center;
}

.sidebar.collapsed .nav-icon {
  font-size: 20px;
}

.tooltip-anchor {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
}

.logo {
  width: 36px;
  height: 36px;
  background: #409eff;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 18px;
}

.logo-text {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.sidebar-nav {
  margin-top: 0;
  position: relative;
}

.nav-group {
  margin-bottom: 4px;
}

.nav-group-title {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  cursor: pointer;
  color: #666;
  transition: all 0.2s ease;
  margin: 0;
}

.nav-group-title:hover {
  background: #f5f7fa;
  color: #409eff;
}

.nav-group-title.active {
  color: #409eff;
}

.nav-group-items {
  background: #f8f9fa;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 20px 10px 52px;
  cursor: pointer;
  color: #666;
  transition: all 0.2s ease;
  margin: 0;
  position: relative;
}

.nav-item:hover {
  background: #eef2f7;
  color: #409eff;
}

.nav-item.active {
  background: #eef2f7;
  color: #409eff;
  font-weight: 500;
}

.nav-icon {
  font-size: 18px;
  color: #666;
}

.nav-group-title:hover .nav-icon {
  color: #409eff;
}

.nav-group-title.active .nav-icon {
  color: #409eff;
}

.nav-text {
  font-size: 14px;
  flex: 1;
}

.nav-arrow {
  font-size: 14px;
  color: #999;
  transition: transform 0.2s ease;
}

.nav-arrow.expanded {
  transform: rotate(90deg);
}

.folder-tree-container {
  margin-top: 4px;
  padding-left: 32px;
}

.folder-item {
  padding-left: 20px;
}

.sub-folders {
  background: #ffffff;
  padding-left: 20px;
}

.sub-folder-item {
  padding-left: 20px;
}

.deep-folder-item {
  padding-left: 20px;
}

.folder-edit-input {
  flex: 1;
  margin-left: 8px;
  margin-right: 8px;
  height: 28px;
}

.root-folder {
  background: rgba(59, 130, 246, 0.08);
  font-weight: 600;
  
  &:hover {
    background: rgba(59, 130, 246, 0.12);
  }
}

.main-content {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

.main-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.breadcrumb {
  font-size: 14px;
}

.sidebar-toggle-btn {
  width: 24px;
  height: 24px;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  color: #666;
  cursor: pointer;
  transition: all 0.2s ease;
}

.sidebar-toggle-btn:hover {
  background: #f5f7fa;
  color: #409eff;
}

.sidebar-toggle-icon {
  width: 18px;
  height: 18px;
  transition: all 0.2s ease;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 24px;
}

.header-search {
  border-right: 1px solid #e8e8e8;
  padding-right: 20px;
}

.quick-search-input {
  width: 220px;
}

.storage-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-right: 20px;
  border-right: 1px solid #e8e8e8;
}

.storage-bar-container {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.storage-bar {
  width: 100px;
  height: 4px;
  background: #ebeef5;
  border-radius: 2px;
  overflow: hidden;
}

.storage-fill {
  height: 100%;
  background: linear-gradient(90deg, #409eff 0%, #667eea 100%);
  border-radius: 2px;
  transition: width 0.3s ease;
}

.storage-text {
  font-size: 12px;
  color: #666;
  margin-top: 4px;
}

.header-icons {
  display: flex;
  align-items: center;
  gap: 4px;
}

.icon-btn {
  width: 36px;
  height: 36px;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  color: #666;
  font-size: 18px;

  &.logout-btn {
    color: #f56c6c;
    
    &:hover {
      background: #fff3f0;
      color: #f56c6c;
    }
  }
}

.icon-btn:hover {
  background: #f5f7fa;
  color: #409eff;
}

.language-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  cursor: pointer;
  color: #606266;
  font-size: 14px;
  border-radius: 4px;
  transition: all 0.2s;
}

.language-btn:hover {
  background: #f5f7fa;
  color: #409eff;
}

.language-text {
  font-weight: 500;
}

.notification-badge {
  --el-badge-margin-left: -4px;
}

.user-dropdown {
  display: flex;
  align-items: center;
}

.user-dropdown .user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
}

.user-dropdown .user-info:hover {
  background: #f5f7fa;
}

.user-dropdown .avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #409eff;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 14px;
  font-weight: 600;
}

.user-dropdown .username {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.user-dropdown .dropdown-arrow {
  font-size: 12px;
  color: #999;
  transition: transform 0.2s ease;
}

.user-dropdown:hover .dropdown-arrow {
  transform: rotate(180deg);
}

.search-modal {
  .el-dialog__header {
    display: none;
  }
  
  .el-dialog__body {
    padding: 16px;
  }
  
  .el-dialog__footer {
    padding: 8px 16px;
    border-top: 1px solid #e8e8e8;
  }
}

.notification-modal :deep(.el-dialog) {
  height: 600px !important;
}

.notification-modal :deep(.el-dialog__header) {
  display: none;
}

.notification-modal :deep(.el-dialog__body) {
  padding: 16px;
  height: calc(100% - 20px) !important;
  overflow: hidden;
}

.notification-modal :deep(.el-dialog__footer) {
  display: none;
}

.search-modal-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.search-modal-input {
  width: 100%;
}

.search-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  color: #999;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 8px;
}

.empty-text {
  font-size: 14px;
}

.search-results {
  max-height: 200px;
  overflow-y: auto;
}

.search-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  cursor: pointer;
  border-radius: 4px;
  transition: background 0.2s ease;
  
  &:hover, &.active {
    background: #eef2f7;
  }
  
  span {
    font-size: 14px;
    color: #333;
  }
  
  .el-icon {
    color: #409eff;
  }
}

.search-footer {
  display: flex;
  align-items: center;
  gap: 8px;
  justify-content: flex-start;
}

.footer-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  font-size: 12px;
  
  &.esc-btn {
    margin-left: auto;
    
    .esc-label {
      background: #f5f7fa;
      padding: 2px 6px;
      border-radius: 4px;
      font-family: monospace;
      font-size: 11px;
      margin-right: 4px;
    }
  }
}

.notification-body {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px solid #e8e8e8;
  flex-shrink: 0;
}

.notification-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.mark-all-read {
  padding: 4px 8px;
  font-size: 12px;
}

.notification-list {
  flex: 1;
  overflow-y: auto;
  margin-top: 12px;
}

.notification-item {
  padding: 12px;
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.2s ease;
  margin-bottom: 8px;
  
  &:hover {
    background: #f5f7fa;
  }
  
  &.unread {
    background: #fff3f0;
  }
  
  &.read {
    background: #f0f9eb;
  }
}

.notification-content {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.notification-text {
  font-size: 14px;
  color: #333;
  flex: 1;
}

.unread-badge {
  background: #f56c6c;
  color: #fff;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 4px;
}

.read-badge {
  background: #67c23a;
  color: #fff;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 4px;
}

.notification-time {
  font-size: 12px;
  color: #999;
}

.notification-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  color: #999;
}

.content-body {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}
</style>