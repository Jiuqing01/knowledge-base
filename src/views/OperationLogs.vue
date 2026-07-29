<template>
  <div class="operation-logs-page">
    <div class="toolbar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索日志"
        class="search-input"
        clearable
        @input="loadLogs"
      />
      <div class="toolbar-actions">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          @change="loadLogs"
        />
        <el-button icon="Refresh" @click="loadLogs">刷新</el-button>
        <el-button icon="Download" @click="handleExport">导出</el-button>
      </div>
    </div>
    
    <el-table
      :data="logs"
      v-loading="loading"
      stripe
    >
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="userId" label="用户ID" width="100" />
      <el-table-column prop="action" label="操作类型" width="150">
        <template #default="{ row }">
          <el-tag :type="getOperationType(row.action)">
            {{ getOperationLabel(row.action) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="targetType" label="目标类型" width="120" />
      <el-table-column prop="targetId" label="目标ID" width="100" />
      <el-table-column prop="details" label="操作详情" min-width="300" />
      <el-table-column prop="ipAddress" label="IP地址" width="150" />
      <el-table-column prop="createdAt" label="操作时间" width="180">
        <template #default="{ row }">
          {{ formatDate(row.createdAt) }}
        </template>
      </el-table-column>
    </el-table>
    
    <div class="pagination">
      <span class="total-count">共 {{ totalLogs }} 条</span>
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :total="totalLogs"
        :page-sizes="[10, 20, 50, 100]"
        layout="prev, pager, next, jumper"
        @current-change="loadLogs"
        @size-change="loadLogs"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Download } from '@element-plus/icons-vue'
import { adminAPI } from '@/api'
import * as XLSX from 'xlsx'

const searchKeyword = ref('')
const dateRange = ref([])
const loading = ref(false)
const logs = ref([])
const page = ref(1)
const pageSize = ref(10)
const totalLogs = ref(0)

const getOperationType = (action) => {
  const types = {
    'CREATE': 'success',
    'UPDATE': 'warning',
    'DELETE': 'danger',
    'READ': 'info',
    'LOGIN': 'primary',
    'UPLOAD': 'success',
    'DOWNLOAD': 'info'
  }
  return types[action] || 'info'
}

const getOperationLabel = (action) => {
  const labels = {
    'CREATE': '创建',
    'UPDATE': '更新',
    'DELETE': '删除',
    'READ': '查看',
    'LOGIN': '登录',
    'UPLOAD': '上传',
    'DOWNLOAD': '下载'
  }
  return labels[action] || action
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

const loadLogs = async () => {
  loading.value = true
  try {
    const params = {
      page: page.value - 1,
      size: pageSize.value
    }
    
    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }
    
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0].toISOString()
      params.endDate = dateRange.value[1].toISOString()
    }
    
    const res = await adminAPI.getLogs(params)
    if (res.success && res.data) {
      logs.value = res.data.content || []
      totalLogs.value = res.data.total || 0
    }
  } catch (error) {
    console.error('加载日志失败:', error)
    logs.value = []
    totalLogs.value = 0
  } finally {
    loading.value = false
  }
}

const handleExport = async () => {
  if (logs.value.length === 0) {
    ElMessage.warning('没有数据可导出')
    return
  }
  
  try {
    const exportData = logs.value.map(log => ({
      'ID': log.id,
      '用户ID': log.userId,
      '操作类型': getOperationLabel(log.action),
      '目标类型': log.targetType,
      '目标ID': log.targetId,
      '操作详情': log.details,
      'IP地址': log.ipAddress,
      '操作时间': formatDate(log.createdAt)
    }))
    
    const worksheet = XLSX.utils.json_to_sheet(exportData)
    const workbook = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(workbook, worksheet, '操作日志')
    
    const fileName = `操作日志_${new Date().toLocaleDateString('zh-CN').replace(/\//g, '-')}.xlsx`
    XLSX.writeFile(workbook, fileName)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
}

onMounted(() => {
  loadLogs()
})
</script>

<style scoped>
.operation-logs-page {
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