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
        <el-select v-model="selectedUserId" placeholder="按用户筛选" clearable @change="loadFiles">
          <el-option label="全部用户" value="" />
          <el-option v-for="user in users" :key="user.id" :label="user.username" :value="user.id" />
        </el-select>
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
          <div class="file-cell" @click="handlePreview(row)">
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
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button size="small" text @click="handlePreview(row)">预览</el-button>
          <el-button size="small" text type="primary" @click="handleDownload(row)">下载</el-button>
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

  <el-dialog v-model="previewVisible" :title="'预览: ' + (previewFile?.originalName || '')" width="800px" top="5vh">
    <div v-if="previewLoading" class="preview-loading">
      <el-icon :size="48"><Document /></el-icon>
      <p>正在加载...</p>
    </div>
    <div v-else-if="previewError" class="preview-error">
      <el-icon :size="48"><Document /></el-icon>
      <p>{{ previewError }}</p>
      <el-button type="primary" @click="handlePreview(previewFile)">重新加载</el-button>
    </div>
    <div v-else-if="isImageFile(previewFile)" class="preview-image">
      <img :src="previewUrl" alt="图片预览" />
    </div>
    <div v-else-if="isTextFile(previewFile)" class="preview-text">
      <pre>{{ previewText }}</pre>
    </div>
    <div v-else-if="isWordFile(previewFile)" class="preview-word">
      <pre>{{ previewText }}</pre>
    </div>
    <div v-else class="preview-other">
      <el-icon :size="64"><Document /></el-icon>
      <p>无法预览此类型文件</p>
      <p class="file-info">文件类型: {{ previewFile?.mimeType }}</p>
      <p class="file-info">文件大小: {{ formatSize(previewFile?.fileSize) }}</p>
      <el-button type="primary" @click="handleDownload(previewFile)">下载文件</el-button>
    </div>
    <template #footer>
      <el-button @click="previewVisible = false">关闭</el-button>
      <el-button type="primary" @click="handleDownload(previewFile)">下载文件</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Download, Delete, Document } from '@element-plus/icons-vue'
import { adminAPI, fileAPI } from '@/api'
import * as XLSX from 'xlsx'

const searchKeyword = ref('')
const fileType = ref('')
const selectedUserId = ref('')
const loading = ref(false)
const files = ref([])
const users = ref([])
const selectedFiles = ref([])
const page = ref(1)
const pageSize = ref(10)
const totalFiles = ref(0)

const previewVisible = ref(false)
const previewLoading = ref(false)
const previewError = ref('')
const previewUrl = ref('')
const previewText = ref('')
const previewFile = ref(null)

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
    const params = {
      page: page.value - 1,
      size: pageSize.value,
      keyword: searchKeyword.value
    }
    if (selectedUserId.value) {
      params.userId = selectedUserId.value
    }
    const res = await adminAPI.getAllFiles(params)
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

const loadUsers = async () => {
  try {
    const res = await adminAPI.getUsers({ page: 0, size: 100 })
    if (res.success && res.data) {
      users.value = res.data.content || []
    }
  } catch (error) {
    console.error('加载用户失败:', error)
  }
}

const handleSelectionChange = (val) => {
  selectedFiles.value = val
}

const handlePreview = async (file) => {
  previewFile.value = file
  previewVisible.value = true
  previewLoading.value = true
  previewError.value = ''
  previewUrl.value = ''
  previewText.value = ''

  try {
    const token = localStorage.getItem('accessToken')
    
    if (isPdfFile(file)) {
      const url = `/api/files/preview/${file.id}?token=${encodeURIComponent(token)}`
      const a = document.createElement('a')
      a.href = url
      a.target = '_blank'
      a.rel = 'noopener noreferrer'
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
      previewVisible.value = false
      return
    }
    
    const response = await fetch(`/api/files/preview/${file.id}`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    
    if (!response.ok) {
      throw new Error('预览失败')
    }
    
    const blob = await response.blob()
    
    if (isImageFile(file)) {
      previewUrl.value = window.URL.createObjectURL(blob)
    } else if (isTextFile(file)) {
      const reader = new FileReader()
      reader.onload = (e) => {
        previewText.value = e.target.result
      }
      reader.readAsText(blob)
    } else if (isWordFile(file)) {
      try {
        const mammoth = await import('mammoth')
        const arrayBuffer = await blob.arrayBuffer()
        const result = await mammoth.extractRawText({ arrayBuffer: arrayBuffer })
        previewText.value = result.value
      } catch (wordError) {
        console.error('Word预览失败:', wordError)
        previewError.value = 'Word文件预览失败，请下载查看'
      }
    }
  } catch (error) {
    console.error('预览失败:', error)
    previewError.value = error.message || '预览失败'
  } finally {
    previewLoading.value = false
  }
}

const isImageFile = (file) => {
  if (!file || !file.mimeType) return false
  return file.mimeType.startsWith('image/')
}

const isTextFile = (file) => {
  if (!file || !file.mimeType) return false
  const textTypes = [
    'text/plain',
    'text/html',
    'text/css',
    'text/javascript',
    'application/json',
    'application/xml',
    'text/markdown',
    'text/x-markdown'
  ]
  return textTypes.includes(file.mimeType)
}

const isPdfFile = (file) => {
  if (!file || !file.mimeType) return false
  return file.mimeType === 'application/pdf'
}

const isWordFile = (file) => {
  if (!file || !file.mimeType) return false
  return file.mimeType.includes('word') || file.mimeType.includes('docx') || 
         file.mimeType.includes('application/vnd.openxmlformats-officedocument.wordprocessingml.document')
}

const handleDownload = async (row) => {
  try {
    const token = localStorage.getItem('accessToken')
    const url = `/api/files/download/${row.id}?token=${encodeURIComponent(token)}`
    const a = document.createElement('a')
    a.href = url
    a.download = row.originalName
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
  } catch (error) {
    ElMessage.error('下载失败')
  }
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

const handleExport = async () => {
  if (files.value.length === 0) {
    ElMessage.warning('没有数据可导出')
    return
  }
  
  try {
    const exportData = files.value.map(file => ({
      'ID': file.id,
      '文件名': file.originalName,
      '类型': getFileTypeLabel(file),
      '大小': formatSize(file.fileSize),
      '所有者': file.ownerUsername || '-',
      '所属文件夹': file.folderName || '-',
      '创建时间': formatDate(file.createdAt)
    }))
    
    const worksheet = XLSX.utils.json_to_sheet(exportData)
    const workbook = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(workbook, worksheet, '所有文件')
    
    const fileName = `所有文件_${new Date().toLocaleDateString('zh-CN').replace(/\//g, '-')}.xlsx`
    XLSX.writeFile(workbook, fileName)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
}

onMounted(() => {
  loadFiles()
  loadUsers()
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
  cursor: pointer;
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

.preview-loading,
.preview-error,
.preview-other {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  color: #666;
}

.preview-image {
  text-align: center;
  padding: 20px;
}

.preview-image img {
  max-width: 100%;
  max-height: 500px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.preview-text,
.preview-word {
  max-height: 500px;
  overflow-y: auto;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.preview-text pre,
.preview-word pre {
  margin: 0;
  font-family: 'Microsoft YaHei', 'SimHei', sans-serif;
  font-size: 14px;
  line-height: 1.8;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.preview-other .file-info {
  font-size: 13px;
  color: #999;
  margin-top: 8px;
}
</style>
