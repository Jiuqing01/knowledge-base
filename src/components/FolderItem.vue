<template>
  <div class="folder-item">
    <div
      class="item-row"
      :class="{ active: activeFolder?.id === folder.id }"
      :style="{ paddingLeft: `${depth * 16}px` }"
      @click="handleSelect(folder)"
    >
      <div class="expand-btn" @click.stop="toggleExpand">
        <el-icon v-if="hasChildren" :class="{ expanded: isExpanded }">
          <ArrowRight />
        </el-icon>
        <span v-else class="expand-placeholder"></span>
      </div>
      
      <el-icon class="folder-icon"><Folder /></el-icon>
      
      <span class="folder-name">{{ folder.name }}</span>
      
      <el-dropdown trigger="click" @command="handleCommand">
        <el-icon class="more-btn"><MoreFilled /></el-icon>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="create">
              <el-icon><Plus /></el-icon>
              新建子文件夹
            </el-dropdown-item>
            <el-dropdown-item command="rename">
              <el-icon><Edit /></el-icon>
              重命名
            </el-dropdown-item>
            <el-dropdown-item command="delete" divided>
              <el-icon><Delete /></el-icon>
              删除
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
    
    <div v-if="isExpanded && childFolders.length > 0" class="children">
      <FolderItem
        v-for="child in childFolders"
        :key="child.id"
        :folder="child"
        :all-folders="allFolders"
        :active-folder="activeFolder"
        :depth="depth + 1"
        @select="$emit('select', $event)"
        @refresh="$emit('refresh')"
      />
    </div>
    
    <!-- 重命名对话框 -->
    <el-dialog v-model="renameVisible" title="重命名文件夹" width="300px">
      <el-input v-model="newName" placeholder="请输入新名称" />
      <template #footer>
        <el-button @click="renameVisible = false">取消</el-button>
        <el-button type="primary" @click="handleRename">确认</el-button>
      </template>
    </el-dialog>
    
    <!-- 新建子文件夹对话框 -->
    <el-dialog v-model="createVisible" title="新建子文件夹" width="300px">
      <el-input v-model="childName" placeholder="请输入文件夹名称" />
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreate">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Folder, ArrowRight, MoreFilled, Plus, Edit, Delete } from '@element-plus/icons-vue'
import { folderAPI } from '@/api'

const props = defineProps({
  folder: {
    type: Object,
    required: true
  },
  allFolders: {
    type: Array,
    default: () => []
  },
  activeFolder: {
    type: Object,
    default: null
  },
  depth: {
    type: Number,
    default: 1
  }
})

const emit = defineEmits(['select', 'refresh'])

const isExpanded = ref(true)

const childFolders = computed(() => {
  return props.allFolders.filter(f => f.parentId === props.folder.id)
})

const hasChildren = computed(() => childFolders.value.length > 0)

const toggleExpand = () => {
  isExpanded.value = !isExpanded.value
}

const handleSelect = (folder) => {
  emit('select', folder)
}

// 重命名
const renameVisible = ref(false)
const newName = ref('')

const handleCommand = (command) => {
  if (command === 'rename') {
    newName.value = props.folder.name
    renameVisible.value = true
  } else if (command === 'create') {
    childName.value = ''
    createVisible.value = true
  } else if (command === 'delete') {
    handleDelete()
  }
}

const handleRename = async () => {
  if (!newName.value.trim()) {
    ElMessage.warning('请输入名称')
    return
  }
  
  try {
    await folderAPI.renameFolder(props.folder.id, newName.value)
    ElMessage.success('重命名成功')
    renameVisible.value = false
    emit('refresh')
  } catch (error) {
    ElMessage.error('重命名失败')
  }
}

// 创建子文件夹
const createVisible = ref(false)
const childName = ref('')

const handleCreate = async () => {
  if (!childName.value.trim()) {
    ElMessage.warning('请输入名称')
    return
  }
  
  try {
    await folderAPI.createFolder(childName.value, props.folder.id)
    ElMessage.success('创建成功')
    createVisible.value = false
    emit('refresh')
  } catch (error) {
    ElMessage.error('创建失败')
  }
}

// 删除
const handleDelete = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要删除此文件夹吗？非空文件夹无法删除。',
      '删除确认',
      { type: 'warning' }
    )
    
    await folderAPI.deleteFolder(props.folder.id)
    ElMessage.success('删除成功')
    emit('refresh')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
}
</script>

<style scoped>
.folder-item {
  display: flex;
  flex-direction: column;
}

.item-row {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 8px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s ease;
  color: #c9d1d9;
}

.item-row:hover {
  background: rgba(255, 255, 255, 0.05);
}

.item-row.active {
  background: rgba(59, 130, 246, 0.15);
  color: #58a6ff;
}

.expand-btn {
  width: 16px;
  height: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.expand-btn .el-icon {
  transition: transform 0.2s ease;
}

.expand-btn .el-icon.expanded {
  transform: rotate(90deg);
}

.expand-placeholder {
  width: 16px;
}

.folder-icon {
  color: #d29922;
}

.folder-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.more-btn {
  opacity: 0;
  padding: 4px;
  transition: opacity 0.2s ease;
}

.item-row:hover .more-btn {
  opacity: 1;
}

.children {
  overflow: hidden;
}
</style>