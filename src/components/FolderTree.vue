<template>
  <div class="folder-tree">
    <div class="tree-root">
      <div
        class="tree-item root-item"
        :class="{ active: !activeFolder }"
        @click="handleSelect(null)"
      >
        <el-icon><HomeFilled /></el-icon>
        <span>全部文件</span>
      </div>
      
      <div v-for="folder in rootFolders" :key="folder.id">
        <FolderItem
          :folder="folder"
          :all-folders="folders"
          :active-folder="activeFolder"
          :depth="1"
          @select="handleSelect"
          @refresh="$emit('refresh')"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { HomeFilled } from '@element-plus/icons-vue'
import FolderItem from './FolderItem.vue'

const props = defineProps({
  folders: {
    type: Array,
    default: () => []
  },
  activeFolder: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['select', 'refresh'])

const rootFolders = computed(() => {
  return props.folders.filter(f => !f.parentId)
})

const handleSelect = (folder) => {
  emit('select', folder)
}
</script>

<style scoped>
.folder-tree {
  font-size: 14px;
}

.tree-root {
  display: flex;
  flex-direction: column;
}

.tree-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
  color: #c9d1d9;
}

.tree-item:hover {
  background: rgba(255, 255, 255, 0.05);
}

.tree-item.active {
  background: rgba(59, 130, 246, 0.15);
  color: #58a6ff;
}

.root-item {
  margin-bottom: 8px;
}
</style>