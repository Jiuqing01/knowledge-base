<template>
  <div class="editor">
    <div class="editor-toolbar">
      <div class="toolbar-left">
        <div class="tool-btn">
          <component :is="icons.Bold" :size="16" />
        </div>
        <div class="tool-btn">
          <component :is="icons.Italic" :size="16" />
        </div>
        <div class="tool-btn">
          <component :is="icons.Underline" :size="16" />
        </div>
        <div class="divider"></div>
        <div class="tool-btn">
          <component :is="icons.List" :size="16" />
        </div>
        <div class="tool-btn">
          <component :is="icons.ListOrdered" :size="16" />
        </div>
        <div class="tool-btn">
          <component :is="icons.Quote" :size="16" />
        </div>
        <div class="divider"></div>
        <div class="tool-btn">
          <component :is="icons.Link" :size="16" />
        </div>
        <div class="tool-btn">
          <component :is="icons.Image" :size="16" />
        </div>
        <div class="tool-btn">
          <component :is="icons.Table" :size="16" />
        </div>
        <div class="divider"></div>
        <div class="tool-btn">
          <component :is="icons.Code" :size="16" />
        </div>
        <div class="tool-btn">
          <component :is="icons.CodeSquare" :size="16" />
        </div>
      </div>
      
      <div class="toolbar-center">
        <div class="format-select">
          <select class="format-option">
            <option>正文</option>
            <option>标题 1</option>
            <option>标题 2</option>
            <option>标题 3</option>
            <option>引用</option>
          </select>
        </div>
        <div class="font-select">
          <select class="font-option">
            <option>微软雅黑</option>
            <option>宋体</option>
            <option>黑体</option>
            <option>Arial</option>
          </select>
        </div>
        <div class="size-select">
          <select class="size-option">
            <option>12px</option>
            <option>14px</option>
            <option>16px</option>
            <option>18px</option>
            <option>24px</option>
          </select>
        </div>
      </div>
      
      <div class="toolbar-right">
        <div class="tool-btn">
          <component :is="icons.AlignLeft" :size="16" />
        </div>
        <div class="tool-btn">
          <component :is="icons.AlignCenter" :size="16" />
        </div>
        <div class="tool-btn">
          <component :is="icons.AlignRight" :size="16" />
        </div>
        <div class="divider"></div>
        <div class="tool-btn">
          <component :is="icons.Undo" :size="16" />
        </div>
        <div class="tool-btn">
          <component :is="icons.Redo" :size="16" />
        </div>
        <div class="divider"></div>
        <div class="tool-btn">
          <component :is="icons.Plus" :size="16" class="plus-btn" />
        </div>
      </div>
    </div>
    
    <div class="editor-content">
      <div class="doc-header">
        <span class="doc-title">知识库编辑器</span>
        <div class="doc-meta">
          <div class="meta-item">
            <component :is="icons.User" :size="12" />
            <span>李江 最后保存于 · 6 天前</span>
          </div>
        </div>
      </div>
      
      <div class="editor-body">
        <div class="markdown-content">
          <h1>强大的知识库编辑器</h1>
          <p>在这里撰写知识内容，知识库是一个多组织多层级的文档中心，是连接资源库和应用库的内容中枢，你可以...</p>
          
          <h2>丰富的编辑器工具栏</h2>
          <ul>
            <li>可以调入/唤起常用菜单</li>
            <li>选择某一个绿色"+"接入资源库资源</li>
            <li>选择右上角"..."查看草稿、历史版本和更多功能</li>
          </ul>
          
          <h2>功能模块</h2>
          
          <div class="feature-cards">
            <div class="feature-card">
              <div class="card-icon digital-asset">
                <component :is="icons.BookOpen" :size="28" />
              </div>
              <h3>数字资产</h3>
              <p>统一管理企业各类数字化内容和媒体资产。</p>
            </div>
            
            <div class="feature-card">
              <div class="card-icon knowledge">
                <component :is="icons.Search" :size="28" />
              </div>
              <h3>知识管理</h3>
              <p>构建知识库，沉淀和复用企业知识。</p>
            </div>
            
            <div class="feature-card">
              <div class="card-icon portal">
                <component :is="icons.Globe" :size="28" />
              </div>
              <h3>数字门户</h3>
              <p>统一的企业级入口，提供个性化体验。</p>
            </div>
            
            <div class="feature-card">
              <div class="card-icon support">
                <component :is="icons.Heart" :size="28" />
              </div>
              <h3>客户支持</h3>
              <p>构建自助式服务中心，提升客户体验。</p>
            </div>
            
            <div class="feature-card">
              <div class="card-icon community">
                <component :is="icons.Users" :size="28" />
              </div>
              <h3>知识社区</h3>
              <p>在社区交流互动，促进知识共享、学习和协作。</p>
            </div>
          </div>
        </div>
      </div>
      
      <div class="editor-footer">
        <div class="footer-left">
          <span class="status">已自动保存</span>
        </div>
        <div class="footer-right">
          <button class="btn-save">
            <component :is="icons.Save" :size="14" />
            <span>保存</span>
          </button>
          <button class="btn-more">
            <component :is="icons.MoreHorizontal" :size="14" />
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { 
  Bold, Italic, Underline, List, ListOrdered, Quote, Link, Image, Table, 
  Code, CodeSquare, AlignLeft, AlignCenter, AlignRight, Undo, Redo, Plus,
  User, BookOpen, Search, Globe, Heart, Users, Save, MoreHorizontal
} from 'lucide-vue-next'

defineProps({
  content: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update'])

const icons = {
  Bold,
  Italic,
  Underline,
  List,
  ListOrdered,
  Quote,
  Link,
  Image,
  Table,
  Code,
  CodeSquare,
  AlignLeft,
  AlignCenter,
  AlignRight,
  Undo,
  Redo,
  Plus,
  User,
  BookOpen,
  Search,
  Globe,
  Heart,
  Users,
  Save,
  MoreHorizontal
}
</script>

<style lang="scss" scoped>
.editor {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.editor-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 12px;
  background: #fafbfc;
  border-bottom: 1px solid #e5e7eb;
}

.toolbar-left,
.toolbar-center,
.toolbar-right {
  display: flex;
  align-items: center;
  gap: 4px;
}

.tool-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 6px;
  color: #24292f;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    background: rgba(59, 130, 246, 0.1);
    color: #58a6ff;
  }
}

.plus-btn {
  color: #238636;
  background: rgba(35, 134, 54, 0.1);

  &:hover {
    background: rgba(35, 134, 54, 0.15);
    color: #238636;
  }
}

.divider {
  width: 1px;
  height: 20px;
  background: #d0d7de;
  margin: 0 4px;
}

.format-select,
.font-select,
.size-select {
  select {
    padding: 4px 8px;
    border: 1px solid #d0d7de;
    border-radius: 4px;
    font-size: 12px;
    background: white;
    cursor: pointer;
    outline: none;

    &:hover {
      border-color: #8b949e;
    }
  }
}

.editor-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  padding: 24px;
}

.doc-header {
  margin-bottom: 24px;
}

.doc-title {
  font-size: 24px;
  font-weight: 600;
  color: #24292f;
}

.doc-meta {
  display: flex;
  gap: 16px;
  margin-top: 8px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #8b949e;
}

.editor-body {
  flex: 1;
}

.markdown-content {
  line-height: 1.8;
  color: #24292f;
}

.markdown-content h1 {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 16px;
  color: #1a1a1a;
}

.markdown-content h2 {
  font-size: 20px;
  font-weight: 600;
  margin-top: 24px;
  margin-bottom: 12px;
  color: #1a1a1a;
}

.markdown-content h3 {
  font-size: 16px;
  font-weight: 600;
  margin-top: 16px;
  margin-bottom: 8px;
}

.markdown-content p {
  margin-bottom: 12px;
  color: #374151;
}

.markdown-content ul {
  margin-left: 24px;
  margin-bottom: 16px;
}

.markdown-content li {
  margin-bottom: 8px;
  color: #374151;
}

.feature-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-top: 24px;
}

.feature-card {
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-radius: 12px;
  padding: 20px;
  transition: all 0.3s ease;
  border: 1px solid #e2e8f0;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  }
}

.card-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12px;
  color: white;

  &.digital-asset {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  }

  &.knowledge {
    background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  }

  &.portal {
    background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  }

  &.support {
    background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  }

  &.community {
    background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
  }
}

.feature-card h3 {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 8px;
  color: #1a1a1a;
}

.feature-card p {
  font-size: 13px;
  color: #64748b;
  line-height: 1.6;
}

.editor-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 24px;
  border-top: 1px solid #e5e7eb;
  background: #fafbfc;
}

.footer-left {
  .status {
    font-size: 12px;
    color: #6e7781;
  }
}

.footer-right {
  display: flex;
  gap: 8px;
}

.btn-save {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: #238636;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    background: #2ea043;
    box-shadow: 0 2px 8px rgba(35, 134, 54, 0.3);
  }
}

.btn-more {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: 1px solid #d0d7de;
  border-radius: 6px;
  color: #24292f;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    background: #f6f8fa;
    border-color: #8b949e;
  }
}
</style>
