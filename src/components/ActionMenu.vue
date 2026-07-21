<template>
  <div v-if="visible" class="action-menu-overlay" @click="$emit('close')">
    <div class="action-menu" @click.stop>
      <div class="menu-header">
        <span>更多操作</span>
        <component :is="icons.X" :size="16" class="close-btn" @click="$emit('close')" />
      </div>
      
      <div class="menu-section">
        <div class="menu-item" @click="handleAction('draft')">
          <component :is="icons.FileText" :size="18" />
          <span>历史版本</span>
        </div>
        <div class="menu-item" @click="handleAction('history')">
          <component :is="icons.History" :size="18" />
          <span>历史版本</span>
        </div>
        <div class="menu-item" @click="handleAction('recycle')">
          <component :is="icons.Trash2" :size="18" />
          <span>版本回收站</span>
        </div>
      </div>
      
      <div class="menu-divider"></div>
      
      <div class="menu-section">
        <div class="menu-item" @click="handleAction('move')">
          <component :is="icons.FolderMove" :size="18" />
          <span>移动</span>
        </div>
        <div class="menu-item" @click="handleAction('copy')">
          <component :is="icons.Copy" :size="18" />
          <span>复制</span>
        </div>
        <div class="menu-item" @click="handleAction('shortcut')">
          <component :is="icons.Star" :size="18" />
          <span>添加快捷方式</span>
        </div>
      </div>
      
      <div class="menu-divider"></div>
      
      <div class="menu-section">
        <div class="menu-item" @click="handleAction('export')">
          <component :is="icons.Download" :size="18" />
          <span>导出</span>
        </div>
        <div class="menu-item" @click="handleAction('import')">
          <component :is="icons.Upload" :size="18" />
          <span>添加快捷方式</span>
        </div>
      </div>
      
      <div class="menu-divider"></div>
      
      <div class="menu-section">
        <div class="menu-item" @click="handleAction('distribute')">
          <component :is="icons.Send" :size="18" />
          <span>内容分发途径</span>
        </div>
      </div>
      
      <div class="menu-divider"></div>
      
      <div class="menu-section">
        <div class="menu-item danger" @click="handleAction('delete')">
          <component :is="icons.Trash" :size="18" />
          <span>删除</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { FileText, History, Trash2, FolderMove, Copy, Star, Download, Upload, Send, Trash, X } from 'lucide-vue-next'

defineProps({
  visible: {
    type: Boolean,
    default: false
  }
})

defineEmits(['close'])

const icons = {
  FileText,
  History,
  Trash2,
  FolderMove,
  Copy,
  Star,
  Download,
  Upload,
  Send,
  Trash,
  X
}

const handleAction = (action) => {
  console.log('Action:', action)
}
</script>

<style lang="scss" scoped>
.action-menu-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: flex-start;
  justify-content: flex-end;
  padding: 120px 24px;
  z-index: 1000;
}

.action-menu {
  width: 240px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.15);
  overflow: hidden;
}

.menu-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #f6f8fa;
  border-bottom: 1px solid #e5e7eb;
  font-size: 14px;
  font-weight: 600;
  color: #24292f;
}

.close-btn {
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  color: #8b949e;
  transition: all 0.2s ease;

  &:hover {
    background: rgba(0, 0, 0, 0.05);
    color: #24292f;
  }
}

.menu-section {
  padding: 4px 0;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  cursor: pointer;
  transition: background 0.2s ease;
  font-size: 13px;
  color: #24292f;

  &:hover {
    background: #f6f8fa;
  }

  &.danger {
    color: #cf222e;

    &:hover {
      background: rgba(207, 34, 46, 0.1);
    }
  }
}

.menu-divider {
  height: 1px;
  background: #e5e7eb;
  margin: 4px 0;
}
</style>
