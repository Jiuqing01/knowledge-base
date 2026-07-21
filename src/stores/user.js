import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authAPI } from '@/api'

export const useUserStore = defineStore('user', () => {
  const accessToken = ref(localStorage.getItem('accessToken') || '')
  const refreshToken = ref(localStorage.getItem('refreshToken') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))
  
  const isLoggedIn = computed(() => !!accessToken.value)
  const isAdmin = computed(() => userInfo.value.role === 'ADMIN')
  
  // 登录
  async function login(username, password) {
    try {
      const res = await authAPI.login(username, password)
      
      if (res.success) {
        accessToken.value = res.data.accessToken
        refreshToken.value = res.data.refreshToken
        userInfo.value = res.data.user
        
        localStorage.setItem('accessToken', res.data.accessToken)
        localStorage.setItem('refreshToken', res.data.refreshToken)
        localStorage.setItem('userInfo', JSON.stringify(res.data.user))
        localStorage.setItem('userRole', res.data.user.role)
        
        return { success: true }
      } else {
        return { success: false, message: res.message || '登录失败' }
      }
    } catch (error) {
      return { success: false, message: error.response?.data?.message || '登录失败' }
    }
  }
  
  // 注册
  async function register(username, password, role = 'USER') {
    try {
      const res = await authAPI.register(username, password, role)
      
      if (res.success) {
        return { success: true, message: '注册成功' }
      } else {
        return { success: false, message: res.message || '注册失败' }
      }
    } catch (error) {
      return { success: false, message: error.response?.data?.message || '注册失败' }
    }
  }
  
  // 登出
  async function logout() {
    try {
      await authAPI.logout()
    } catch (error) {
      console.error('Logout error:', error)
    }
    
    accessToken.value = ''
    refreshToken.value = ''
    userInfo.value = {}
    
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('userRole')
  }
  
  // 修改密码
  async function changePassword(oldPassword, newPassword) {
    try {
      const res = await authAPI.changePassword(oldPassword, newPassword)
      
      if (res.success) {
        return { success: true }
      } else {
        return { success: false, message: res.message || '修改密码失败' }
      }
    } catch (error) {
      return { success: false, message: error.response?.data?.message || '修改密码失败' }
    }
  }
  
  return {
    accessToken,
    refreshToken,
    userInfo,
    isLoggedIn,
    isAdmin,
    login,
    register,
    logout,
    changePassword
  }
})