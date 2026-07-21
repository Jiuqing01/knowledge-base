<template>
  <div class="files-page">
    <div class="page-header">
      <div class="header-row">
        <div class="header-title">
          <h2>{{ folderStore.currentFolder?.name || '全部文件' }}</h2>
          <span class="file-count">共 {{ totalFiles }} 个文件</span>
        </div>
        <div class="header-tools">
          <el-select 
            v-model="selectedTag" 
            placeholder="按标签筛选" 
            clearable
            class="tag-filter"
            @change="handleTagFilter"
          >
            <el-option
              v-for="tag in availableTags"
              :key="tag.id"
              :label="tag.name"
              :value="tag.id"
            />
          </el-select>
          <el-button icon="FolderAdd" @click="showCreateFolderDialog">新建文件夹</el-button>
          <el-button type="primary" icon="Plus" @click="showUploadDialog">上传文件</el-button>
        </div>
      </div>
      
      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="输入文件名/关键词查找"
          clearable
          class="search-input"
          @input="handleFileSearch"
          @keyup.enter="handleFileSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
    </div>
    
    <div class="file-list">
      <div v-if="selectedFiles.length > 0" class="batch-actions">
        <span class="selected-count">已选择 {{ selectedFiles.length }} 个文件</span>
        <el-button type="success" icon="Download" @click="handleBatchDownload">批量下载</el-button>
        <el-button type="danger" icon="Delete" @click="handleBatchDelete">批量删除</el-button>
      </div>
      
      <el-table
        :data="files"
        v-loading="loading"
        @sort-change="handleSortChange"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="40" />
        <el-table-column prop="originalName" label="文件名" min-width="100" sortable align="left" header-align="left">
          <template #default="{ row }">
            <div class="file-name-cell">
              <el-icon :class="getFileIconClass(row)"><component :is="getFileIcon(row)" /></el-icon>
              <span>{{ row.originalName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="size" label="大小" width="100" align="center">
          <template #default="{ row }">
            {{ formatFileSize(row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column prop="fileType" label="类型" width="90" align="center">
          <template #default="{ row }">
            {{ getFileTypeLabel(row.mimeType) }}
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" sortable align="center">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="tags" label="标签" width="80" align="center">
          <template #default="{ row }">
            <div class="tags-cell">
              <el-tag
                v-for="tag in row.tags"
                :key="tag.id"
                size="small"
                class="tag-item"
              >
                {{ tag.name }}
              </el-tag>
              <span v-if="!row.tags || row.tags.length === 0" class="no-tags">无</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center">
          <template #default="{ row }">
            <div style="display: flex; justify-content: center; gap: 8px;">
              <span style="display: flex; align-items: center; justify-content: center; width: 32px; height: 32px; cursor: pointer; color: #606266; font-size: 16px; border-radius: 4px; background: #f5f7fa;" 
                    @click="handleDownload(row)" title="下载"
                    @mouseenter="($event.target.style.background='#409eff', $event.target.style.color='#fff')"
                    @mouseleave="($event.target.style.background='#f5f7fa', $event.target.style.color='#606266')">
                <el-icon><Download /></el-icon>
              </span>
              <span style="display: flex; align-items: center; justify-content: center; width: 32px; height: 32px; cursor: pointer; color: #606266; font-size: 16px; border-radius: 4px; background: #f5f7fa;" 
                    @click="showAddTagDialog(row)" title="标签"
                    @mouseenter="($event.target.style.background='#409eff', $event.target.style.color='#fff')"
                    @mouseleave="($event.target.style.background='#f5f7fa', $event.target.style.color='#606266')">
                <el-icon><Document /></el-icon>
              </span>
              <span style="display: flex; align-items: center; justify-content: center; width: 32px; height: 32px; cursor: pointer; color: #606266; font-size: 16px; border-radius: 4px; background: #f5f7fa;" 
                    @click="handleFileCommand('rename', row)" title="重命名"
                    @mouseenter="($event.target.style.background='#409eff', $event.target.style.color='#fff')"
                    @mouseleave="($event.target.style.background='#f5f7fa', $event.target.style.color='#606266')">
                <el-icon><Edit /></el-icon>
              </span>
              <span style="display: flex; align-items: center; justify-content: center; width: 32px; height: 32px; cursor: pointer; color: #f56c6c; font-size: 16px; border-radius: 4px; background: #f5f7fa;" 
                    @click="handleDelete(row)" title="删除"
                    @mouseenter="($event.target.style.background='#f56c6c', $event.target.style.color='#fff')"
                    @mouseleave="($event.target.style.background='#f5f7fa', $event.target.style.color='#f56c6c')">
                <el-icon><Delete /></el-icon>
              </span>
            </div>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination" v-if="totalFiles > pageSize">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="totalFiles"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="loadFiles"
          @current-change="loadFiles"
        />
      </div>
    </div>
    
    <el-dialog v-model="createFolderVisible" title="新建文件夹" width="400px" draggable>
      <el-form :model="newFolder" label-width="100px">
        <el-form-item label="父文件夹">
          <el-input :value="folderStore.currentFolder?.name || '全部文件'" disabled />
        </el-form-item>
        <el-form-item label="文件夹名称" required>
          <el-input v-model="newFolder.name" placeholder="请输入文件夹名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createFolderVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreateFolder">创建</el-button>
      </template>
    </el-dialog>
    
    <el-dialog v-model="uploadVisible" title="上传文件" width="500px" draggable>
      <el-upload
        ref="uploadRef"
        :auto-upload="false"
        :file-list="uploadFileList"
        drag
        multiple
        :on-change="handleFileChange"
        :on-remove="handleFileRemove"
      >
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">
          将文件拖到此处，或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            支持的文件类型：pdf, doc, docx, xls, xlsx, ppt, pptx, jpg, png, zip, txt, md 等
          </div>
        </template>
      </el-upload>
      
      <template #footer>
        <el-button @click="uploadVisible = false">取消</el-button>
        <el-button type="primary" :loading="uploading" @click="handleUpload">
          开始上传
        </el-button>
      </template>
    </el-dialog>
    
    <el-dialog v-model="renameVisible" title="重命名文件" width="300px">
      <el-input v-model="newFileName" placeholder="请输入新文件名" />
      <template #footer>
        <el-button @click="renameVisible = false">取消</el-button>
        <el-button type="primary" @click="handleRename">确认</el-button>
      </template>
    </el-dialog>
    
    <el-dialog v-model="addTagVisible" title="添加标签" width="400px">
      <el-select
        v-model="selectedTagIds"
        multiple
        placeholder="选择标签"
        class="w-full"
      >
        <el-option
          v-for="tag in availableTags"
          :key="tag.id"
          :label="tag.name"
          :value="tag.id"
        />
      </el-select>
      <template #footer>
        <el-button @click="addTagVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAddTags">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Upload, Download, Search,
  Document, Picture, Edit, Delete, UploadFilled, FolderAdd
} from '@element-plus/icons-vue'
import { fileAPI, tagAPI, folderAPI } from '@/api'
import { useFolderStore } from '@/stores/folder'

const route = useRoute()

const files = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(12)
const totalFiles = ref(0)
const searchKeyword = ref('')
const selectedFiles = ref([])
const sortOrder = ref(null)

const folderStore = useFolderStore()

const loadFiles = async () => {
  loading.value = true
  
  try {
    const tagId = route.query.tagId
    
    let res
    if (tagId) {
      res = await fileAPI.getFilesByTag(tagId)
    } else if (folderStore.currentFolder) {
      res = await fileAPI.getFilesInFolder(folderStore.currentFolder.id, searchKeyword.value)
    } else {
      const params = {
        page: currentPage.value - 1,
        size: pageSize.value,
        keyword: searchKeyword.value
      }
      res = await fileAPI.getFiles(params)
    }
    
    let data = res.data || []
    
    if (searchKeyword.value && !tagId) {
      data = data.filter(f => f.originalName && f.originalName.toLowerCase().includes(searchKeyword.value.toLowerCase()))
    }
    
    if (sortOrder.value) {
      data.sort((a, b) => {
        const timeA = new Date(a.updatedAt || a.createdAt).getTime()
        const timeB = new Date(b.updatedAt || b.createdAt).getTime()
        return sortOrder.value === 'ascending' ? timeA - timeB : timeB - timeA
      })
    }
    
    files.value = data
    totalFiles.value = data.length || 0
  } catch (error) {
    console.error('加载文件失败:', error)
  } finally {
    loading.value = false
  }
}

const handleFileSearch = () => {
  currentPage.value = 1
  loadFiles()
}

const handleSortChange = ({ prop, order }) => {
  sortOrder.value = order
  currentPage.value = 1
  loadFiles()
}

const handleSelectionChange = (selection) => {
  selectedFiles.value = selection
}

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
    loadFiles()
    loadFolders()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '创建失败')
  }
}

const loadFolders = async () => {
  try {
    const res = await folderAPI.getFolders()
    folderStore.setFolders(res.data || [])
  } catch (error) {
    console.error('加载文件夹失败:', error)
  }
}

const getFileIcon = (file) => {
  const ext = file.originalName.split('.').pop().toLowerCase()
  
  if (['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp'].includes(ext)) {
    return Picture
  }
  if (['mp4', 'avi', 'mov', 'mkv', 'wmv'].includes(ext)) {
    return Document
  }
  return Document
}

const getFileIconClass = (file) => {
  const ext = file.originalName.split('.').pop().toLowerCase()
  
  if (['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp'].includes(ext)) {
    return 'icon-image'
  }
  if (['pdf'].includes(ext)) {
    return 'icon-pdf'
  }
  if (['doc', 'docx'].includes(ext)) {
    return 'icon-word'
  }
  if (['xls', 'xlsx'].includes(ext)) {
    return 'icon-excel'
  }
  return ''
}

const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const getFileTypeLabel = (fileType) => {
  const typeMap = {
    'application/pdf': 'PDF',
    'application/msword': 'Word',
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document': 'Word',
    'application/vnd.ms-excel': 'Excel',
    'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet': 'Excel',
    'application/vnd.ms-powerpoint': 'PPT',
    'application/vnd.openxmlformats-officedocument.presentationml.presentation': 'PPT',
    'image/jpeg': '图片',
    'image/png': '图片',
    'image/gif': '图片',
    'text/plain': '文本',
    'text/markdown': 'Markdown',
    'application/zip': '压缩包',
    'application/x-rar-compressed': '压缩包'
  }
  return typeMap[fileType] || (fileType || '-').split('/').pop().toUpperCase()
}

const formatDate = (dateStr) => {
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const uploadVisible = ref(false)
const uploadRef = ref()
const uploadFileList = ref([])
const uploading = ref(false)
const selectedTag = ref(null)

const showUploadDialog = () => {
  uploadFileList.value = []
  uploadVisible.value = true
}

const handleFileChange = (file, fileList) => {
  uploadFileList.value = fileList
}

const handleFileRemove = (file, fileList) => {
  uploadFileList.value = fileList
}

const handleUpload = async () => {
  if (uploadFileList.value.length === 0) {
    ElMessage.warning('请选择要上传的文件')
    return
  }
  
  uploading.value = true
  
  try {
    const folderId = folderStore.currentFolder?.id
    for (const file of uploadFileList.value) {
      await fileAPI.uploadFile(file.raw, folderId)
    }
    
    ElMessage.success('上传成功')
    uploadVisible.value = false
    uploadFileList.value = []
    loadFiles()
  } catch (error) {
    console.error('上传错误:', error)
    const message = error.response?.data?.message || 
                    error.response?.statusText || 
                    '上传失败'
    ElMessage.error(message)
  } finally {
    uploading.value = false
  }
}

const handleDownload = async (file) => {
  try {
    const res = await fileAPI.downloadFile(file.id)
    const blob = new Blob([res.data])
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = file.originalName
    a.click()
    window.URL.revokeObjectURL(url)
  } catch (error) {
    const message = error.response?.data?.message || 
                    error.response?.statusText || 
                    '下载失败'
    ElMessage.error(message)
  }
}

const handleBatchDownload = async () => {
  if (selectedFiles.value.length === 0) return
  
  try {
    const ids = selectedFiles.value.map(f => f.id)
    const res = await fileAPI.downloadFiles(ids)
    const blob = new Blob([res.data])
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `files_${Date.now()}.zip`
    a.click()
    window.URL.revokeObjectURL(url)
  } catch (error) {
    const message = error.response?.data?.message || 
                    error.response?.statusText || 
                    '下载失败'
    ElMessage.error(message)
  }
}

const handleBatchDelete = async () => {
  if (selectedFiles.value.length === 0) return
  
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedFiles.value.length} 个文件吗？`, '批量删除确认', { type: 'warning' })
    
    for (const file of selectedFiles.value) {
      await fileAPI.deleteFile(file.id)
    }
    
    ElMessage.success('批量删除成功')
    loadFiles()
    selectedFiles.value = []
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleTagFilter = async () => {
  currentPage.value = 1
  
  if (selectedTag.value) {
    try {
      const res = await fileAPI.getFilesByTag(selectedTag.value)
      files.value = res.data || []
      totalFiles.value = files.value.length
    } catch (error) {
      console.error('按标签筛选失败:', error)
      files.value = []
      totalFiles.value = 0
    }
  } else {
    loadFiles()
  }
}

const renameVisible = ref(false)
const newFileName = ref('')
const addTagVisible = ref(false)
const selectedTagIds = ref([])
const availableTags = ref([])
const currentEditFile = ref(null)

const handleFileCommand = (command, file) => {
  currentEditFile.value = file
  
  if (command === 'rename') {
    newFileName.value = file.originalName
    renameVisible.value = true
  } else if (command === 'delete') {
    handleDelete(file)
  }
}

const handleRename = async () => {
  if (!newFileName.value.trim()) {
    ElMessage.warning('请输入文件名')
    return
  }
  
  try {
    await fileAPI.renameFile(currentEditFile.value.id, newFileName.value)
    ElMessage.success('重命名成功')
    renameVisible.value = false
    loadFiles()
  } catch (error) {
    ElMessage.error('重命名失败')
  }
}

const handleDelete = async (file) => {
  try {
    await ElMessageBox.confirm('确定要删除此文件吗？', '删除确认', { type: 'warning' })
    await fileAPI.deleteFile(file.id)
    ElMessage.success('删除成功')
    loadFiles()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const showAddTagDialog = async (file) => {
  currentEditFile.value = file
  selectedTagIds.value = file.tags?.map(t => t.id) || []
  
  try {
    const res = await tagAPI.getTags()
    availableTags.value = res.data || []
  } catch (error) {
    console.error('加载标签失败:', error)
  }
  
  addTagVisible.value = true
}

const handleAddTags = async () => {
  try {
    const existingTags = currentEditFile.value.tags || []
    for (const tag of existingTags) {
      if (!selectedTagIds.value.includes(tag.id)) {
        await fileAPI.removeTag(currentEditFile.value.id, tag.id)
      }
    }
    
    for (const tagId of selectedTagIds.value) {
      if (!existingTags.find(t => t.id === tagId)) {
        await fileAPI.addTag(currentEditFile.value.id, tagId)
      }
    }
    
    ElMessage.success('标签更新成功')
    addTagVisible.value = false
    loadFiles()
  } catch (error) {
    ElMessage.error('更新标签失败')
  }
}

const loadTags = async () => {
  try {
    const res = await tagAPI.getTags()
    availableTags.value = res.data || []
  } catch (error) {
    console.error('加载标签失败:', error)
    availableTags.value = []
  }
}

watch(() => folderStore.currentFolder, () => {
  currentPage.value = 1
  loadFiles()
}, { deep: true })

watch(() => route.query.tagId, () => {
  currentPage.value = 1
  loadFiles()
})

onMounted(() => {
  loadFiles()
  loadTags()
})
</script>

<style>
.files-page {
  min-height: 100%;
  padding: 0;
}

.page-header {
  background: #fff;
  border-radius: 12px;
  padding: 20px 24px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-title h2 {
  font-size: 18px;
  font-weight: 600;
  color: #1f2329;
  margin: 0;
}

.file-count {
  font-size: 14px;
  color: #8f959e;
  padding: 2px 8px;
  background: #f2f3f5;
  border-radius: 4px;
}

.header-tools {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-tools .el-button {
  font-size: 14px;
  padding: 8px 16px;
}

.search-bar {
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.search-input {
  width: 100%;
  max-width: 400px;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 8px;
}

.tag-filter {
  width: 140px;
}

.tag-filter :deep(.el-input__wrapper) {
  border-radius: 8px;
}

.file-list {
  background: #fff;
  border-radius: 12px;
  padding: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

.batch-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  background: #f7f8fa;
  border-bottom: 1px solid #f0f0f0;
}

.selected-count {
  font-size: 14px;
  color: #4e5969;
  font-weight: 500;
}

.batch-actions .el-button {
  font-size: 13px;
  padding: 6px 14px;
}

.file-list :deep(.el-table) {
  border: none;
}

.file-list :deep(.el-table th) {
  background: #fafafa;
  color: #8f959e;
  font-weight: 500;
  font-size: 13px;
  padding: 14px 20px;
  border-bottom: 1px solid #f0f0f0;
}



.file-list :deep(.el-table tr:hover > td) {
  background: #fafafa;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 16px 20px;
  border-top: 1px solid #f0f0f0;
  background: #fafafa;
}

.pagination :deep(.el-pagination) {
  font-size: 13px;
}

.file-name-cell {
  display: flex;
  align-items: center;
  gap: 10px;
  padding-left: 10px;
}

.file-name-cell .el-icon {
  font-size: 20px;
}

.file-name-cell .icon-pdf {
  color: #dc3545;
}

.file-name-cell .icon-word {
  color: #1890ff;
}

.file-name-cell .icon-excel {
  color: #52c41a;
}

.action-container {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 4px;
  width: 88px;
  height: auto;
}

.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 32px;
  cursor: pointer;
  color: #606266;
  font-size: 16px;
  border-radius: 4px;
  transition: all 0.2s;
  background: #f5f7fa;
  box-sizing: border-box;
}

.action-btn:hover {
  background: #409eff;
  color: #fff;
}

.action-btn.danger {
  color: #f56c6c;
}

.action-btn.danger:hover {
  background: #f56c6c;
  color: #fff;
}

.file-name-cell .icon-image {
  color: #722ed1;
}

.tags-cell {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  align-items: center;
  gap: 4px;
}

.tag-item {
  margin-right: 0;
}

.no-tags {
  color: #999;
  font-size: 12px;
}
</style>
