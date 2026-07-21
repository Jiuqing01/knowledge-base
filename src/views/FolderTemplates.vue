<template>
  <div class="folder-templates-page">
    <div class="toolbar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索模板名称"
        class="search-input"
        clearable
        @input="loadTemplates"
      />
      <div class="toolbar-actions">
        <el-button type="primary" icon="Plus" @click="handleAdd">新增模板</el-button>
        <el-button icon="Refresh" @click="loadTemplates">刷新</el-button>
      </div>
    </div>
    
    <div class="template-tree">
      <el-tree
        ref="treeRef"
        :data="templates"
        :props="defaultProps"
        default-expand-all
        highlight-current
      >
        <template #default="{ node, data }">
          <div class="tree-node">
            <span>{{ data.label }}</span>
            <div class="node-actions">
              <el-button type="primary" link size="small" @click.stop="handleEdit(data)">编辑</el-button>
              <el-button type="danger" link size="small" @click.stop="handleDelete(data)">删除</el-button>
            </div>
          </div>
        </template>
      </el-tree>
    </div>
    
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑模板' : '新增模板'" width="400px" draggable>
      <el-form :model="form" label-width="100px">
        <el-form-item label="模板名称">
          <el-input v-model="form.name" placeholder="请输入模板名称" />
        </el-form-item>
        <el-form-item label="上级模板">
          <el-select v-model="form.parentId" placeholder="请选择上级模板（可选）">
            <el-option label="（无）" :value="0" />
            <el-option
              v-for="template in flatTemplates"
              :key="template.id"
              :label="template.name"
              :value="template.id"
              :disabled="isEdit && template.id === editingId"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import { folderTemplateAPI } from '@/api'

const treeRef = ref(null)
const searchKeyword = ref('')
const templates = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref(null)

const defaultProps = {
  children: 'children',
  label: 'label'
}

const flatTemplates = ref([])

const form = reactive({
  name: '',
  parentId: 0
})

const loadTemplates = async () => {
  try {
    const res = await folderTemplateAPI.getTemplates()
    templates.value = res.data || []
    
    const flatRes = await folderTemplateAPI.getFlatTemplates()
    flatTemplates.value = flatRes.data || []
  } catch (error) {
    console.error('加载模板失败:', error)
    templates.value = []
    flatTemplates.value = []
  }
}

const handleAdd = () => {
  isEdit.value = false
  editingId.value = null
  form.name = ''
  form.parentId = 0
  dialogVisible.value = true
}

const handleEdit = (data) => {
  isEdit.value = true
  editingId.value = data.id
  form.name = data.label
  form.parentId = data.parentId || 0
  dialogVisible.value = true
}

const handleDelete = async (data) => {
  if (data.children && data.children.length > 0) {
    ElMessage.warning('请先删除子模板')
    return
  }
  
  try {
    await ElMessageBox.confirm(`确定要删除模板 "${data.label}" 吗？`, '删除模板', { type: 'error' })
    await folderTemplateAPI.deleteTemplate(data.id)
    ElMessage.success('模板已删除')
    loadTemplates()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleSave = async () => {
  if (!form.name.trim()) {
    ElMessage.warning('请输入模板名称')
    return
  }
  
  try {
    if (isEdit.value) {
      await folderTemplateAPI.updateTemplate(editingId.value, {
        name: form.name,
        parentId: form.parentId
      })
      ElMessage.success('模板编辑成功')
    } else {
      await folderTemplateAPI.createTemplate(form.name, form.parentId)
      ElMessage.success('模板添加成功')
    }
    dialogVisible.value = false
    loadTemplates()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  loadTemplates()
})
</script>

<style scoped>
.folder-templates-page {
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

.template-tree {
  background: #fafafa;
  border-radius: 8px;
  padding: 16px;
  max-height: calc(100vh - 200px);
  overflow-y: auto;
}

.template-tree :deep(.el-tree) {
  background: transparent;
}

.tree-node {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.tree-node span {
  font-size: 14px;
  color: #333;
  flex: 1;
}

.node-actions {
  display: flex;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.2s;
}

.tree-node:hover .node-actions {
  opacity: 1;
}
</style>
