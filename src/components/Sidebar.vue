<template>
  <aside class="sidebar">
    <div class="sidebar-header">
      <div class="logo">
        <div class="logo-icon">
          <component :is="icons.Grid3X3" :size="24" />
        </div>
        <span class="logo-text">知识库文档</span>
      </div>
    </div>
    
    <div class="sidebar-nav">
      <div class="nav-item active">
        <component :is="icons.FolderOpen" :size="20" />
        <span>知识库</span>
      </div>
      <div class="nav-item">
        <component :is="icons.Database" :size="20" />
        <span>资源库</span>
      </div>
      <div class="nav-item">
        <component :is="icons.Box" :size="20" />
        <span>应用库</span>
      </div>
      <div class="nav-item">
        <component :is="icons.Tag" :size="20" />
        <span>标签</span>
      </div>
      <div class="nav-item">
        <component :is="icons.Settings" :size="20" />
        <span>设置</span>
      </div>
    </div>
    
    <div class="tree-container">
      <div class="tree-header">
        <component :is="expandAll ? icons.ChevronDown : icons.ChevronRight" :size="16" class="expand-icon" @click="expandAll = !expandAll" />
        <span>公司管理制度</span>
        <component :is="icons.Search" :size="16" class="search-icon" />
      </div>
      
      <div class="tree-content">
        <div
          v-for="item in treeData"
          :key="item.id"
          class="tree-item"
        >
          <TreeItem 
            :item="item" 
            :depth="0" 
            :expanded="expandAll"
            @select="handleSelect" 
          />
        </div>
      </div>
    </div>
    
    <div class="sidebar-footer">
      <div class="user-info">
        <div class="avatar">
          <component :is="icons.User" :size="20" />
        </div>
        <div class="user-details">
          <span class="user-name">数据概览</span>
          <span class="user-role">管理员</span>
        </div>
      </div>
      <div class="footer-actions">
        <div class="footer-action-item">
          <component :is="icons.Settings" :size="18" />
          <span>设置</span>
        </div>
        <div class="footer-action-item">
          <component :is="icons.Upload" :size="18" />
          <span>发布</span>
        </div>
        <div class="footer-action-item">
          <component :is="icons.Archive" :size="18" />
          <span>回收站</span>
        </div>
      </div>
    </div>
  </aside>
</template>

<script setup>
import { ref } from 'vue'
import { Grid3X3, FolderOpen, Database, Box, Tag, Settings, Search, ChevronDown, ChevronRight, User, Upload, Archive } from 'lucide-vue-next'
import TreeItem from './TreeItem.vue'

defineProps({
  treeData: {
    type: Array,
    required: true
  },
  activeNode: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['select'])

const expandAll = ref(true)

const icons = {
  Grid3X3,
  FolderOpen,
  Database,
  Box,
  Tag,
  Settings,
  Search,
  ChevronDown,
  ChevronRight,
  User,
  Upload,
  Archive
}

const handleSelect = (node) => {
  emit('select', node)
}
</script>

<style lang="scss" scoped>
.sidebar {
  width: 240px;
  background: linear-gradient(180deg, #1a1d23 0%, #0d1117 100%);
  color: #c9d1d9;
  display: flex;
  flex-direction: column;
  height: 100vh;
  padding: 16px;
  position: fixed;
  left: 0;
  top: 0;
  z-index: 100;
}

.sidebar-header {
  padding-bottom: 16px;
  border-bottom: 1px solid #30363d;
  margin-bottom: 16px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo-icon {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-text {
  font-size: 14px;
  font-weight: 600;
  color: #f0f6fc;
}

.sidebar-nav {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 16px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 14px;

  &:hover {
    background: rgba(59, 130, 246, 0.1);
    color: #f0f6fc;
  }

  &.active {
    background: rgba(59, 130, 246, 0.15);
    color: #58a6ff;
  }
}

.tree-container {
  flex: 1;
  overflow-y: auto;
}

.tree-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  font-size: 12px;
  color: #8b949e;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.expand-icon {
  cursor: pointer;
  
  &:hover {
    color: #f0f6fc;
  }
}

.search-icon {
  margin-left: auto;
  opacity: 0.5;
  cursor: pointer;
  
  &:hover {
    opacity: 1;
  }
}

.tree-content {
  padding: 4px 0;
}

.sidebar-footer {
  border-top: 1px solid #30363d;
  padding-top: 12px;
  margin-top: auto;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s ease;

  &:hover {
    background: rgba(255, 255, 255, 0.05);
  }
}

.avatar {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.user-details {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: 13px;
  color: #f0f6fc;
  font-weight: 500;
}

.user-role {
  font-size: 11px;
  color: #8b949e;
}

.footer-actions {
  display: flex;
  justify-content: space-around;
  padding: 12px 8px;
  margin-top: 8px;
  border-top: 1px solid #30363d;
}

.footer-action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 11px;
  color: #8b949e;

  &:hover {
    background: rgba(255, 255, 255, 0.05);
    color: #f0f6fc;
  }
}
</style>