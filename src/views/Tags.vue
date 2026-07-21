<template>
  <div class="tags-page">
    <div class="page-header">
      <h2>标签管理</h2>
      <el-button type="primary" @click="showCreateDialog">
        <el-icon><Plus /></el-icon>
        创建标签
      </el-button>
    </div>
    
    <div class="tags-content">
      <el-table :data="tags" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        
        <el-table-column prop="name" label="标签名称" min-width="200">
          <template #default="{ row }">
            <el-tag :type="getTagType(row.id)">{{ row.name }}</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        
        <el-table-column label="关联文件数" width="120">
          <template #default="{ row }">
            {{ row.fileCount || 0 }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              @click="handleViewFiles(row)"
            >
              查看文件
            </el-button>
            <el-button
              type="danger"
              link
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="totalTags"
          :page-sizes="[20, 50, 100]"
          layout="total, sizes, prev, pager, next"
          @size-change="loadTags"
          @current-change="loadTags"
        />
      </div>
    </div>
    
    <!-- 创建标签对话框 -->
    <el-dialog v-model="createVisible" title="创建标签" width="400px">
      <el-form :model="newTag" label-width="80px">
        <el-form-item label="标签名称">
          <el-input v-model="newTag.name" placeholder="请输入标签名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreate">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { tagAPI } from '@/api'

const router = useRouter()

const tags = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const totalTags = ref(0)

const createVisible = ref(false)
const newTag = reactive({
  name: ''
})

const tagTypes = ['', 'success', 'warning', 'danger', 'info']

const getTagType = (id) => {
  return tagTypes[id % tagTypes.length]
}

const formatDate = (dateStr) => {
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

const loadTags = async () => {
  loading.value = true
  
  try {
    const res = await tagAPI.getTags()
    tags.value = res.data || []
    totalTags.value = tags.value.length || 0
  } catch (error) {
    console.error('加载标签失败:', error)
  } finally {
    loading.value = false
  }
}

const showCreateDialog = () => {
  newTag.name = ''
  createVisible.value = true
}

const handleCreate = async () => {
  if (!newTag.name.trim()) {
    ElMessage.warning('请输入标签名称')
    return
  }
  
  try {
    await tagAPI.createTag(newTag.name)
    ElMessage.success('创建成功')
    createVisible.value = false
    loadTags()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '创建失败')
  }
}

const handleViewFiles = (tag) => {
  router.push({ path: '/dashboard/files', query: { tagId: tag.id } })
}

const handleDelete = async (tag) => {
  try {
    await ElMessageBox.confirm(
      '删除标签将同时移除所有文件的关联，确定要删除吗？',
      '删除确认',
      { type: 'warning' }
    )
    
    await tagAPI.deleteTag(tag.id)
    ElMessage.success('删除成功')
    loadTags()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadTags()
})
</script>

<style scoped>
.tags-page {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  background: #ffffff;
  padding: 16px 20px;
  border-bottom: 1px solid #e8e8e8;
  border-radius: 8px 8px 0 0;
}

.page-header h2 {
  font-size: 20px;
  color: #333;
  margin: 0;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 24px;
}
</style>