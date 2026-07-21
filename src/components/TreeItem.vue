<template>
  <div class="tree-item-wrapper">
    <div
      class="tree-item-row"
      :class="{ active: isActive, expanded: isExpanded }"
      :style="{ paddingLeft: `${depth * 16 + 12}px` }"
      @click="handleClick"
    >
      <div class="expand-btn" v-if="item.children && item.children.length > 0" @click.stop="toggleExpand">
        <component :is="isExpanded ? icons.ChevronDown : icons.ChevronRight" :size="14" />
      </div>
      <div class="expand-placeholder" v-else></div>
      
      <component 
        :is="item.icon === 'folder' ? (isExpanded ? icons.FolderOpen : icons.Folder) : icons.FileText" 
        :size="16" 
        :class="item.icon"
      />
      
      <span class="item-label">{{ item.label }}</span>
      
      <div class="item-actions">
        <component :is="icons.MoreHorizontal" :size="14" class="more-btn" />
      </div>
    </div>
    
    <div v-if="isExpanded && item.children && item.children.length > 0" class="tree-children">
      <TreeItem
        v-for="child in item.children"
        :key="child.id"
        :item="child"
        :depth="depth + 1"
        :active-id="activeId"
        :expanded="expanded"
        @select="$emit('select', $event)"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ChevronDown, ChevronRight, Folder, FolderOpen, FileText, MoreHorizontal } from 'lucide-vue-next'

const props = defineProps({
  item: {
    type: Object,
    required: true
  },
  depth: {
    type: Number,
    default: 0
  },
  activeId: {
    type: String,
    default: null
  },
  expanded: {
    type: Boolean,
    default: null
  }
})

const emit = defineEmits(['select'])

const icons = {
  ChevronDown,
  ChevronRight,
  Folder,
  FolderOpen,
  FileText,
  MoreHorizontal
}

const localExpanded = ref(props.item.icon === 'folder')
const isExpanded = computed(() => props.expanded !== null ? props.expanded : localExpanded.value)
const isActive = computed(() => props.activeId === props.item.id)

const toggleExpand = () => {
  if (props.expanded === null) {
    localExpanded.value = !localExpanded.value
  }
}

const handleClick = () => {
  emit('select', props.item)
}
</script>

<style lang="scss" scoped>
.tree-item-wrapper {
  display: flex;
  flex-direction: column;
}

.tree-item-row {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 8px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.15s ease;
  font-size: 13px;
  color: #c9d1d9;

  &:hover {
    background: rgba(255, 255, 255, 0.05);
  }

  &.active {
    background: rgba(59, 130, 246, 0.15);
    color: #58a6ff;
    
    svg.folder, svg.folder-open {
      color: #58a6ff;
    }
  }
}

.expand-btn {
  width: 16px;
  height: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #8b949e;
  
  &:hover {
    color: #c9d1d9;
  }
}

.expand-placeholder {
  width: 16px;
}

svg.folder {
  color: #d29922;
}

svg.folder-open {
  color: #d29922;
}

svg.file {
  color: #8b949e;
}

.item-label {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-actions {
  opacity: 0;
  transition: opacity 0.15s ease;
}

.tree-item-row:hover .item-actions {
  opacity: 1;
}

.more-btn {
  padding: 4px;
  border-radius: 4px;
  color: #8b949e;

  &:hover {
    background: rgba(255, 255, 255, 0.1);
    color: #c9d1d9;
  }
}

.tree-children {
  overflow: hidden;
}
</style>