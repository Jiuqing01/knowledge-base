<template>
  <div>
    <div 
      class="nav-item folder-item"
      :class="{ active: folderStore.currentFolder?.id === folder.id }"
      @click="handleFolderSelect(folder)"
      @contextmenu="handleFolderContextMenu($event, folder)"
    >
      <el-icon class="nav-icon"><FolderOpened /></el-icon>
      <template v-if="editingFolderId === folder.id">
        <el-input 
          v-model="localEditingName"
          class="folder-edit-input"
          size="small"
          @blur="handleRenameConfirm"
          @keyup.enter="handleRenameConfirm"
          @keyup.esc="handleRenameCancel"
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
      <RecursiveFolderItem
        v-for="child in getChildren(folder)"
        :key="child.id"
        :folder="child"
        :depth="depth + 1"
        :sidebar-collapsed="sidebarCollapsed"
        :editing-folder-id="editingFolderId"
        :expanded-folders="expandedFolders"
        :set-editing-folder-id="props.setEditingFolderId"
        :on-rename-success="props.onRenameSuccess"
        @folder-select="handleFolderSelect"
        @context-menu="handleFolderContextMenu"
        @toggle-expand="toggleFolderExpand"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, watch } from 'vue'
import { FolderOpened, Folder, ArrowRight } from '@element-plus/icons-vue'
import { useFolderStore } from '@/stores/folder'
import { folderAPI } from '@/api'
import { ElMessage } from 'element-plus'

const props = defineProps({
  folder: {
    type: Object,
    required: true
  },
  depth: {
    type: Number,
    default: 1
  },
  sidebarCollapsed: {
    type: Boolean,
    default: false
  },
  editingFolderId: {
    type: Number,
    default: null
  },
  expandedFolders: {
    type: Array,
    default: () => []
  },
  setEditingFolderId: {
    type: Function,
    required: true
  },
  onRenameSuccess: {
    type: Function,
    default: () => {}
  }
})

const emit = defineEmits(['folder-select', 'context-menu', 'toggle-expand'])

const folderStore = useFolderStore()
const renameInputRef = ref(null)
const localEditingName = ref('')
const isRenaming = ref(false)

watch(() => props.editingFolderId, (newId) => {
  if (newId === props.folder.id) {
    localEditingName.value = props.folder.name
    nextTick(() => {
      if (renameInputRef.value) {
        renameInputRef.value.focus()
        renameInputRef.value.input?.select()
      }
    })
  }
})

const hasChildren = (folder) => {
  return folderStore.folders.some(f => f.parentId === folder.id)
}

const getChildren = (folder) => {
  return folderStore.folders.filter(f => f.parentId === folder.id)
}

const handleFolderSelect = (folder) => {
  emit('folder-select', folder)
}

const handleFolderContextMenu = (event, folder) => {
  emit('context-menu', event, folder)
}

const toggleFolderExpand = (folderId) => {
  emit('toggle-expand', folderId)
}

const handleRenameConfirm = async () => {
  if (isRenaming.value) return
  isRenaming.value = true
  
  try {
    if (!localEditingName.value.trim()) {
      ElMessage.warning('请输入文件夹名称')
      return
    }
    
    const newName = localEditingName.value.trim()
    
    if (newName === props.folder.name) {
      props.setEditingFolderId(null)
      return
    }
    
    const siblings = folderStore.folders.filter(f => f.parentId === props.folder.parentId && f.id !== props.folder.id)
    if (siblings.some(f => f.name === newName)) {
      ElMessage.warning('该名称已存在')
      return
    }
    
    await folderAPI.renameFolder(props.folder.id, newName)
    ElMessage.success('重命名成功')
    props.setEditingFolderId(null)
    props.onRenameSuccess()
  } catch (error) {
    ElMessage.error('重命名失败')
  } finally {
    isRenaming.value = false
  }
}

const handleRenameCancel = () => {
  props.setEditingFolderId(null)
}

const focusRenameInput = () => {
  nextTick(() => {
    if (renameInputRef.value) {
      renameInputRef.value.focus()
    }
  })
}

defineExpose({ focusRenameInput })
</script>

<style scoped>
.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 20px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 14px;
  color: #666;
  margin: 0;
}

.nav-item:hover {
  background: #f0f5ff;
  color: #409eff;
}

.nav-item.active {
  background: #ecf5ff;
  color: #409eff;
  font-weight: 500;
}

.nav-icon {
  font-size: 18px;
  flex-shrink: 0;
  color: #666;
}

.nav-item:hover .nav-icon,
.nav-item.active .nav-icon {
  color: #409eff;
}

.nav-text {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tooltip-anchor {
  display: inline-block;
  width: 100%;
  height: 100%;
}

.nav-arrow {
  font-size: 14px;
  color: #999;
  transition: transform 0.2s ease;
  flex-shrink: 0;
}

.nav-arrow.expanded {
  transform: rotate(90deg);
}

.folder-edit-input {
  flex: 1;
  margin: 0;
  height: 28px;
}

.sub-folders {
  padding-left: 20px;
  background: #fafafa;
}
</style>
