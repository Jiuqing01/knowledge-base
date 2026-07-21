<template>
  <header class="header">
    <div class="header-left">
      <component :is="icons.Menu" :size="16" />
      <span class="header-title">Dashboard</span>
    </div>
    
    <div class="header-right">
      <div class="user-avatar">
        <component :is="icons.User" :size="16" />
      </div>
    </div>
  </header>
</template>

<script setup>
import { Menu, User } from 'lucide-vue-next'

const icons = {
  Menu,
  User
}
</script>

<style lang="scss" scoped>
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 16px;
  background: #ffffff;
  border-bottom: 1px solid #e0e0e0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-title {
  font-size: 14px;
  font-weight: 500;
  color: #333333;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 2px;
}

.user-avatar {
  width: 36px;
  height: 36px;
  background: #3498db;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  cursor: pointer;
}
</style>