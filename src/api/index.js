import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const instance = axios.create({
  baseURL: '/api',
  timeout: 10000
})

instance.interceptors.request.use(
  config => {
    const token = localStorage.getItem('accessToken')
    if (token) {
      if (!config.headers) {
        config.headers = {}
      }
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

let isRefreshing = false
let pendingRequests = []

const processPendingRequests = (token) => {
  pendingRequests.forEach(callback => callback(token))
  pendingRequests = []
}

instance.interceptors.response.use(
  response => {
    if (response.config.responseType === 'blob') {
      return response
    }
    return response.data
  },
  async error => {
    const originalRequest = error.config
    const status = error.response?.status
    
    if (status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        return new Promise((resolve) => {
          pendingRequests.push((token) => {
            originalRequest.headers.Authorization = `Bearer ${token}`
            resolve(instance(originalRequest))
          })
        })
      }
      
      originalRequest._retry = true
      isRefreshing = true
      
      try {
        const refreshToken = localStorage.getItem('refreshToken')
        if (!refreshToken) {
          throw new Error('没有刷新令牌')
        }
        
        const response = await instance.post('/auth/refresh-token', { refreshToken })
        const { accessToken, refreshToken: newRefreshToken } = response.data
        
        localStorage.setItem('accessToken', accessToken)
        if (newRefreshToken) {
          localStorage.setItem('refreshToken', newRefreshToken)
        }
        
        instance.defaults.headers.common.Authorization = `Bearer ${accessToken}`
        originalRequest.headers.Authorization = `Bearer ${accessToken}`
        
        processPendingRequests(accessToken)
        return instance(originalRequest)
      } catch (refreshError) {
        localStorage.removeItem('accessToken')
        localStorage.removeItem('refreshToken')
        localStorage.removeItem('userInfo')
        localStorage.removeItem('userRole')
        ElMessage.error('登录已过期，请重新登录')
        router.push('/login')
        return Promise.reject(refreshError)
      } finally {
        isRefreshing = false
      }
    }
    
    let message = error.message || '请求失败'
    
    if (error.response?.data) {
      if (typeof error.response.data === 'string') {
        message = error.response.data
      } else if (error.response.data.message) {
        message = error.response.data.message
      } else if (error.response.data.error) {
        message = error.response.data.error
      } else if (error.config?.responseType === 'blob') {
        try {
          const text = await error.response.data.text()
          try {
            const json = JSON.parse(text)
            if (json.message) {
              message = json.message
            }
          } catch (e) {
            message = text || '下载失败'
          }
        } catch (e) {
          message = '下载失败'
        }
      }
    }
    
    if (status === 500 && message === 'Internal Server Error') {
      message = '服务器内部错误，请稍后重试'
    }
    
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export const authAPI = {
  login: async (username, password) => {
    const response = await instance.post('/auth/login', { username, password })
    if (response.success) {
      localStorage.setItem('accessToken', response.data.accessToken)
      localStorage.setItem('refreshToken', response.data.refreshToken)
      localStorage.setItem('userInfo', JSON.stringify(response.data.user))
      localStorage.setItem('userRole', response.data.user.role)
    }
    return response
  },

  register: async (username, password, role = 'USER') => {
    try {
      const response = await instance.post('/auth/register', { username, password, role })
      return response
    } catch (error) {
      console.error('Register error:', error)
      return { success: false, message: error.response?.data?.message || '注册失败' }
    }
  },

  logout: async () => {
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('userRole')
    return instance.post('/auth/logout')
  },

  refreshToken: async () => {
    const refreshToken = localStorage.getItem('refreshToken')
    const response = await instance.post('/auth/refresh-token', { refreshToken })
    if (response.success) {
      localStorage.setItem('accessToken', response.data.accessToken)
    }
    return response
  },

  changePassword: async (oldPassword, newPassword) => {
    return instance.post('/auth/change-password', { oldPassword, newPassword })
  },

  sendResetCode: async (phone) => {
    return instance.post('/auth/send-reset-code', { phone })
  },

  resetPassword: async (phone, code, password) => {
    return instance.post('/auth/reset-password', { phone, code, password })
  },

  getCurrentUser: async (userId) => {
    return instance.get('/auth/me', { params: { userId } })
  }
}

export const fileAPI = {
  getFiles: async (params) => {
    return instance.get('/files', { params })
  },

  getFilesInFolder: async (folderId, keyword) => {
    const params = keyword ? { keyword } : {}
    return instance.get(`/files/folder/${folderId}`, { params })
  },

  getFile: async (id) => {
    return instance.get(`/files/${id}`)
  },

  uploadFile: async (file, folderId) => {
    const formData = new FormData()
    formData.append('file', file)
    if (folderId) {
      formData.append('folderId', folderId)
    }
    return instance.post('/files/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  downloadFile: async (id) => {
    const token = localStorage.getItem('accessToken')
    console.log('Downloading file:', id, 'Token:', token ? token.substring(0, 20) + '...' : 'null')
    return instance.get(`/files/download/${id}`, { responseType: 'blob' })
  },

  previewFile: async (id) => {
    const token = localStorage.getItem('accessToken')
    console.log('Previewing file:', id, 'Token:', token ? token.substring(0, 20) + '...' : 'null')
    return instance.get(`/files/preview/${id}`, { responseType: 'blob' })
  },

  downloadBatch: async (ids) => {
    return instance.post('/files/download/batch', ids, { responseType: 'blob' })
  },

  updateMetadata: async (id, data) => {
    return instance.put(`/files/${id}`, data)
  },

  replaceContent: async (id, file) => {
    const formData = new FormData()
    formData.append('file', file)
    return instance.post(`/files/${id}/replace`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  renameFile: async (id, name) => {
    return instance.put(`/files/${id}/rename`, { name })
  },

  moveFile: async (id, folderId) => {
    return instance.put(`/files/${id}/move`, { folderId })
  },

  deleteFile: async (id) => {
    return instance.delete(`/files/${id}`)
  },

  downloadFiles: async (fileIds) => {
    return instance.post('/files/download/batch', fileIds, { 
      responseType: 'blob'
    })
  },

  getFilesByTag: async (tagId) => {
    return instance.get(`/files/tag/${tagId}`)
  },

  addTag: async (fileId, tagId) => {
    return instance.post(`/files/${fileId}/tags`, { tagId })
  },

  removeTag: async (fileId, tagId) => {
    return instance.delete(`/files/${fileId}/tags/${tagId}`)
  }
}

export const folderAPI = {
  getFolders: async () => {
    return instance.get('/folders')
  },

  getContents: async (id, params) => {
    return instance.get(`/folders/${id}/contents`, { params })
  },

  createFolder: async (name, parentId = null) => {
    return instance.post('/folders', { name, parentId })
  },

  renameFolder: async (id, name) => {
    return instance.put(`/folders/${id}`, { name })
  },

  moveFolder: async (id, parentId) => {
    return instance.put(`/folders/${id}/move`, { parentId })
  },

  deleteFolder: async (id) => {
    return instance.delete(`/folders/${id}`)
  }
}

export const tagAPI = {
  getTags: async (params = {}) => {
    return instance.get('/tags', { params })
  },

  createTag: async (name, color) => {
    return instance.post('/tags', { name, color })
  },

  deleteTag: async (id) => {
    return instance.delete(`/tags/${id}`)
  }
}

export const adminAPI = {
  getUsers: async (params) => {
    return instance.get('/admin/users', { params })
  },

  deleteUser: async (id) => {
    return instance.delete(`/admin/users/${id}`)
  },

  resetPassword: async (id) => {
    return instance.post(`/admin/users/${id}/reset-password`)
  },

  setQuota: async (id, quota) => {
    return instance.put(`/admin/users/${id}/quota`, { quota })
  },

  register: async (username, password, role = 'USER') => {
    return instance.post('/admin/users', { username, password, role })
  },

  updateUser: async (id, data) => {
    return instance.put(`/admin/users/${id}`, data)
  },

  getAllFiles: async (params) => {
    return instance.get('/admin/files', { params })
  },

  getUserFiles: async (userId, params) => {
    return instance.get(`/admin/users/${userId}/files`, { params })
  },

  getConfig: async () => {
    return instance.get('/admin/config')
  },

  updateConfig: async (config) => {
    return instance.put('/admin/config', config)
  },

  getFolderTemplates: async () => {
    return instance.get('/admin/folder-templates')
  },

  updateFolderTemplates: async (templates) => {
    return instance.put('/admin/folder-templates', templates)
  },

  getAllFiles: async (page, size, keyword) => {
    return instance.get('/admin/files', { params: { page, size, keyword } })
  },

  deleteFile: async (id) => {
    return instance.delete(`/admin/files/${id}`)
  },

  getLogs: async (params) => {
    return instance.get('/operation-logs', { params })
  }
}

export const folderTemplateAPI = {
  getTemplates: async () => {
    return instance.get('/folder-templates')
  },

  getFlatTemplates: async () => {
    return instance.get('/folder-templates/flat')
  },

  createTemplate: async (name, parentId = 0) => {
    return instance.post('/folder-templates', { name, parentId })
  },

  updateTemplate: async (id, data) => {
    return instance.put(`/folder-templates/${id}`, data)
  },

  deleteTemplate: async (id) => {
    return instance.delete(`/folder-templates/${id}`)
  }
}

export const systemConfigAPI = {
  getAllConfigs: async () => {
    return instance.get('/system-configs')
  },

  getConfigByKey: async (key) => {
    return instance.get(`/system-configs/${key}`)
  },

  createConfig: async (configKey, configValue, description) => {
    return instance.post('/system-configs', { configKey, configValue, description })
  },

  updateConfig: async (key, configValue, description) => {
    return instance.put(`/system-configs/${key}`, { configValue, description })
  },

  deleteConfig: async (key) => {
    return instance.delete(`/system-configs/${key}`)
  }
}

export default { authAPI, fileAPI, folderAPI, tagAPI, adminAPI, systemConfigAPI, folderTemplateAPI }
