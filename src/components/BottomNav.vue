<template>
  <div class="bottom-nav">
    <div class="nav-items">
      <div class="nav-item active">
        <component :is="icons.Home" :size="20" />
        <span>首页</span>
      </div>
      <div class="nav-item">
        <component :is="icons.Search" :size="20" />
        <span>发现</span>
      </div>
      <div class="nav-item center">
        <div class="center-btn">
          <component :is="icons.Plus" :size="24" />
        </div>
      </div>
      <div class="nav-item">
        <component :is="icons.Bell" :size="20" />
        <span>通知</span>
      </div>
      <div class="nav-item">
        <component :is="icons.User" :size="20" />
        <span>我的</span>
      </div>
    </div>
    <div class="brand-badge">
      <span class="brand-text">baklib.cn</span>
    </div>
  </div>
</template>

<script setup>
import { Home, Search, Plus, Bell, User } from 'lucide-vue-next'

const icons = {
  Home,
  Search,
  Plus,
  Bell,
  User
}
</script>

<style lang="scss" scoped>
.bottom-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  width: 100%;
}

.nav-items {
  display: flex;
  align-items: center;
  gap: 32px;
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 8px 16px;
  cursor: pointer;
  transition: all 0.2s ease;
  color: #6e7781;

  &:hover {
    color: #238636;
  }

  &.active {
    color: #238636;
  }

  &.center {
    position: relative;
    top: -12px;
  }

  span {
    font-size: 12px;
  }
}

.center-btn {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #238636 0%, #2ea043 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 4px 12px rgba(35, 134, 54, 0.3);
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    transform: scale(1.05);
    box-shadow: 0 6px 16px rgba(35, 134, 54, 0.4);
  }
}

.brand-badge {
  padding: 6px 12px;
  background: rgba(175, 184, 193, 0.2);
  border-radius: 20px;
}

.brand-text {
  font-size: 12px;
  color: #6e7781;
  font-weight: 500;
}
</style>
