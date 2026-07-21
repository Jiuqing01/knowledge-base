<template>
  <div class="all-files-page">
    <div class="toolbar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索文件名"
        class="search-input"
        clearable
        @input="loadFiles"
      />
      <div class="toolbar-actions">
        <el-select v-model="fileType" placeholder="文件类型" @change="loadFiles">
          <el-option label="全部" value="" />
          <el-option label="文档" value="doc" />
          <el-option label="图片" value="image" />
          <el-option label="视频" value="video" />
          <el-option label="其他" value="other" />
        </el-select>
        <el-button icon="Refresh" @click="loadFiles">刷新</el-button>
        <el-button icon="Download" @click="handleExport">导出</el-button>
        <el-button type="danger" icon="Delete" :disabled="selectedFiles.length === 0" @click="handleBatchDelete">批量删除</el-button>
      </div>
    </div>
    
    <el-table
      :data="files"
      v-loading="loading"
      stripe
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="50" />
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="文件名" min-width="200">
        <template #default="{ row }">
          <div class="file-cell">
            <span class="file-icon">{{ getFileIcon(row) }}</span>
            <span>{{ row.originalName }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="type" label="类型" width="100">
        <template #default="{ row }">
          <el-tag type="info">{{ getFileTypeLabel(row) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="size" label="大小" width="120">
        <template #default="{ row }">
          {{ formatSize(row.fileSize) }}
        </template>
      </el-table-column>
      <el-table-column prop="owner" label="所有者" width="120">
        <template #default="{ row }">
          <div class="user-cell">
            <div class="user-avatar">
              {{ row.ownerUsername?.charAt(0).toUpperCase() || 'U' }}
            </div>
            <span>{{ row.ownerUsername }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="folder" label="所属文件夹" min-width="150">
        <template #default="{ row }">
          {{ row.folderName || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="180">
        <template #default="{ row }">
          {{ formatDate(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button size="small" text @click="handlePreview(row)">预览</el-button>
          <el-button size="small" text type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <div class="pagination">
      <span class="total-count">共 {{ totalFiles }} 条</span>
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :total="totalFiles"
        :page-sizes="[10, 20, 50, 100]"
        layout="prev, pager, next, jumper"
        @current-change="loadFiles"
        @size-change="loadFiles"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Download, Delete } from '@element-plus/icons-vue'
import { adminAPI } from '@/api'

const searchKeyword = ref('')
const fileType = ref('')
const loading = ref(false)
const files = ref([])
const selectedFiles = ref([])
const page = ref(1)
const pageSize = ref(10)
const totalFiles = ref(0)

const getFileIcon = (row) => {
  const type = getFileType(row)
  const icons = {
    'doc': '📄',
    'image': '🖼️',
    'video': '🎬',
    'other': '📁'
  }
  return icons[type] || '📄'
}

const getFileType = (row) => {
  const fileName = row.originalName || ''
  const ext = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase()
  
  const docExts = ['doc', 'docx', 'pdf', 'txt', 'xls', 'xlsx', 'ppt', 'pptx', 'csv', 'md', 'rtf']
  const imageExts = ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'svg', 'webp', 'ico']
  const videoExts = ['mp4', 'avi', 'mov', 'wmv', 'flv', 'mkv', 'webm']
  
  if (docExts.includes(ext)) return 'doc'
  if (imageExts.includes(ext)) return 'image'
  if (videoExts.includes(ext)) return 'video'
  return 'other'
}

const getFileTypeLabel = (row) => {
  const type = getFileType(row)
  const labels = {
    'doc': '文档',
    'image': '图片',
    'video': '视频',
    'other': '其他'
  }
  return labels[type] || '未知'
}

const formatSize = (bytes) => {
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

const loadFiles = async () => {
  loading.value = true
  try {
    const res = await adminAPI.getAllFiles(page.value - 1, pageSize.value, searchKeyword.value)
    if (res.success && res.data) {
      files.value = res.data.content || []
      totalFiles.value = res.data.totalElements || 0
    } else {
      files.value = []
      totalFiles.value = 0
    }
  } catch (error) {
    console.error('加载文件失败:', error)
    files.value = []
    totalFiles.value = 0
  } finally {
    loading.value = false
  }
}

const handleSelectionChange = (val) => {
  selectedFiles.value = val
}

const handlePreview = (row) => {
  ElMessage.info(`预览文件: ${row.originalName}`)
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除文件 "${row.originalName}" 吗？`, '删除文件', { type: 'error' })
    await adminAPI.deleteFile(row.id)
    ElMessage.success('文件已删除')
    loadFiles()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedFiles.value.length} 个文件吗？`, '批量删除', { type: 'error' })
    for (const file of selectedFiles.value) {
      await adminAPI.deleteFile(file.id)
    }
    ElMessage.success('批量删除成功')
    selectedFiles.value = []
    loadFiles()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleExport = () => {
  ElMessage.info('导出功能开发中')
}

onMounted(() => {
  loadFiles()
})
</script>

<style scoped>
.all-files-page {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.search-input {
  width: 250px;
}

.toolbar-actions {
  display: flex;
  gap: 12px;
}

.file-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.file-icon {
  font-size: 18px;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #409eff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  margin-top: 20px;
  gap: 16px;
}

.total-count {
  font-size: 14px;
  color: #666;
}
</style>
