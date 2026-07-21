<template>
  <Transition name="fade">
    <div 
      v-if="visible" 
      class="context-menu"
      :style="{ left: position.x + 'px', top: position.y + 'px' }"
      @click.stop
    >
      <div 
        v-for="item in menuItems" 
        :key="item.key"
        class="menu-item"
        :class="{ disabled: item.disabled }"
        @click="handleMenuClick(item)"
      >
        <el-icon v-if="item.icon" class="menu-icon"><component :is="item.icon" /></el-icon>
        <span class="menu-text">{{ item.label }}</span>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { Edit, Delete } from '@element-plus/icons-vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  position: {
    type: Object,
    default: () => ({ x: 0, y: 0 })
  },
  folder: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['close', 'rename', 'delete'])

const menuItems = reactive([
  { key: 'rename', label: '重命名', icon: Edit },
  { key: 'delete', label: '删除', icon: Delete }
])

const handleMenuClick = (item) => {
  emit('close')
  if (item.key === 'rename') {
    emit('rename', props.folder)
  } else if (item.key === 'delete') {
    emit('delete', props.folder)
  }
}
</script>

<style scoped>
.context-menu {
  position: fixed;
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  padding: 4px 0;
  min-width: 140px;
  z-index: 9999;
  border: 1px solid #e8e8e8;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 8px 16px;
  font-size: 14px;
  color: #303133;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.menu-item:hover {
  background-color: #f5f7fa;
}

.menu-item.disabled {
  color: #c0c4cc;
  cursor: not-allowed;
}

.menu-item.disabled:hover {
  background-color: transparent;
}

.menu-icon {
  margin-right: 8px;
  font-size: 14px;
  color: #606266;
}

.menu-text {
  flex: 1;
}

.menu-divider {
  height: 1px;
  background-color: #e8e8e8;
  margin: 4px 0;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.15s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>