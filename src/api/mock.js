import { ElMessage } from 'element-plus'

// Mock数据存储
const mockData = {
  users: [
    {
      id: 1,
      username: 'admin',
      password: 'admin123',
      role: 'ADMIN',
      storage_used: 107374182,
      storage_quota: 1073741824,
      created_at: '2024-01-01 10:00:00'
    },
    {
      id: 2,
      username: 'user',
      password: 'user123',
      role: 'USER',
      storage_used: 52428800,
      storage_quota: 1073741824,
      created_at: '2024-02-01 10:00:00'
    }
  ],
  folders: [
    { id: 1, name: '文档资料', parent_id: null, owner_id: 1 },
    { id: 2, name: '项目文档', parent_id: 1, owner_id: 1 },
    { id: 3, name: '会议记录', parent_id: 1, owner_id: 1 },
    { id: 4, name: '技术文档', parent_id: null, owner_id: 1 },
    { id: 5, name: 'API文档', parent_id: 4, owner_id: 1 },
    { id: 6, name: '设计文档', parent_id: null, owner_id: 2 },
    { id: 7, name: '个人资料', parent_id: null, owner_id: 2 }
  ],
  files: [
    {
      id: 1,
      original_name: '项目需求文档.pdf',
      file_size: 1048576,
      folder_id: 2,
      owner_id: 1,
      tags: [{ id: 1, name: '项目' }, { id: 2, name: '重要' }],
      created_at: '2024-01-15 14:30:00',
      updated_at: '2024-01-15 14:30:00'
    },
    {
      id: 2,
      original_name: '技术方案.docx',
      file_size: 524288,
      folder_id: 5,
      owner_id: 1,
      tags: [{ id: 3, name: '技术' }],
      created_at: '2024-01-20 09:15:00',
      updated_at: '2024-01-20 09:15:00'
    },
    {
      id: 3,
      original_name: '会议纪要.md',
      file_size: 10240,
      folder_id: 3,
      owner_id: 1,
      tags: [],
      created_at: '2024-01-25 16:00:00',
      updated_at: '2024-01-25 16:00:00'
    },
    {
      id: 4,
      original_name: '设计规范.png',
      file_size: 2097152,
      folder_id: 6,
      owner_id: 2,
      tags: [{ id: 4, name: '设计' }],
      created_at: '2024-02-01 11:00:00',
      updated_at: '2024-02-01 11:00:00'
    }
  ],
  tags: [
    { id: 1, name: '项目', file_count: 1 },
    { id: 2, name: '重要', file_count: 1 },
    { id: 3, name: '技术', file_count: 1 },
    { id: 4, name: '设计', file_count: 1 },
    { id: 5, name: '文档', file_count: 0 }
  ],
  logs: [
    { id: 1, user_id: 1, action: 'LOGIN', target_type: 'USER', detail: '用户登录成功', ip_address: '127.0.0.1', created_at: '2024-01-25 10:00:00' },
    { id: 2, user_id: 1, action: 'UPLOAD', target_type: 'FILE', detail: '上传文件: 会议纪要.md', ip_address: '127.0.0.1', created_at: '2024-01-25 16:00:00' },
    { id: 3, user_id: 2, action: 'DOWNLOAD', target_type: 'FILE', detail: '下载文件: 设计规范.png', ip_address: '127.0.0.1', created_at: '2024-02-02 09:30:00' }
  ]
}

// 生成Token
const generateToken = () => {
  return Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15)
}

// 存储当前用户Token
let currentUser = null
let accessToken = null
let refreshToken = null

// Mock API
export const authAPI = {
  login: async (username, password) => {
    const user = mockData.users.find(u => u.username === username && u.password === password)
    
    if (user) {
      accessToken = generateToken()
      refreshToken = generateToken()
      currentUser = user
      
      return {
        success: true,
        data: {
          accessToken,
          refreshToken,
          user: {
            id: user.id,
            username: user.username,
            role: user.role,
            storage_used: user.storage_used,
            storage_quota: user.storage_quota
          }
        }
      }
    } else {
      return { success: false, message: '用户名或密码错误' }
    }
  },
  
  register: async (username, password, role = 'USER') => {
    const exists = mockData.users.find(u => u.username === username)
    
    if (exists) {
      return { success: false, message: '用户名已存在' }
    }
    
    const newUser = {
      id: mockData.users.length + 1,
      username,
      password,
      role,
      storage_used: 0,
      storage_quota: role === 'ADMIN' ? 10737418240 : 1073741824,
      created_at: new Date().toLocaleString('zh-CN')
    }
    
    mockData.users.push(newUser)
    
    // 普通用户创建初始文件夹模板
    if (role === 'USER') {
      const baseFolders = ['文档资料', '项目文档', '个人资料']
      baseFolders.forEach(name => {
        mockData.folders.push({
          id: mockData.folders.length + 1,
          name,
          parent_id: null,
          owner_id: newUser.id
        })
      })
    }
    
    return { success: true, message: '注册成功' }
  },
  
  logout: async () => {
    currentUser = null
    accessToken = null
    refreshToken = null
    return { success: true }
  },
  
  refreshToken: async () => {
    if (currentUser && refreshToken) {
      accessToken = generateToken()
      return { success: true, data: { accessToken, refreshToken } }
    }
    return { success: false, message: '登录已过期' }
  },
  
  changePassword: async (oldPassword, newPassword) => {
    if (!currentUser) {
      return { success: false, message: '请先登录' }
    }
    
    if (currentUser.password !== oldPassword) {
      return { success: false, message: '当前密码错误' }
    }
    
    currentUser.password = newPassword
    return { success: true }
  }
}

export const fileAPI = {
  getFiles: async (params) => {
    let files = mockData.files.filter(f => {
      if (!currentUser) return false
      if (currentUser.role !== 'ADMIN' && f.owner_id !== currentUser.id) return false
      if (params.folderId && f.folder_id !== parseInt(params.folderId)) return false
      if (params.tagId && !f.tags.find(t => t.id === parseInt(params.tagId))) return false
      if (params.keyword && !f.original_name.includes(params.keyword)) return false
      return true
    })
    
    const page = parseInt(params.page) || 1
    const size = parseInt(params.size) || 20
    const start = (page - 1) * size
    const end = start + size
    
    return {
      success: true,
      data: {
        content: files.slice(start, end),
        total: files.length
      }
    }
  },
  
  getFile: async (id) => {
    const file = mockData.files.find(f => f.id === parseInt(id))
    if (!file) return { success: false, message: '文件不存在' }
    return { success: true, data: file }
  },
  
  uploadFile: async (file, folderId) => {
    if (!currentUser) return { success: false, message: '请先登录' }
    
    const newFile = {
      id: mockData.files.length + 1,
      original_name: file.name,
      file_size: file.size,
      folder_id: folderId ? parseInt(folderId) : null,
      owner_id: currentUser.id,
      tags: [],
      created_at: new Date().toLocaleString('zh-CN'),
      updated_at: new Date().toLocaleString('zh-CN')
    }
    
    mockData.files.push(newFile)
    currentUser.storage_used += file.size
    
    return { success: true, data: newFile }
  },
  
  downloadFile: async (id) => {
    const file = mockData.files.find(f => f.id === parseInt(id))
    if (!file) throw new Error('文件不存在')
    
    const content = `这是模拟文件内容: ${file.original_name}`
    return new Blob([content], { type: 'application/octet-stream' })
  },
  
  downloadBatch: async (ids) => {
    const content = ids.map(id => {
      const file = mockData.files.find(f => f.id === parseInt(id))
      return file ? `文件: ${file.original_name}\n内容: 模拟内容\n\n` : ''
    }).join('')
    
    return new Blob([content], { type: 'application/zip' })
  },
  
  updateMetadata: async (id, data) => {
    const file = mockData.files.find(f => f.id === parseInt(id))
    if (!file) return { success: false, message: '文件不存在' }
    
    if (data.description) file.description = data.description
    file.updated_at = new Date().toLocaleString('zh-CN')
    
    return { success: true, data: file }
  },
  
  replaceContent: async (id, file) => {
    const f = mockData.files.find(f => f.id === parseInt(id))
    if (!f) return { success: false, message: '文件不存在' }
    
    f.original_name = file.name
    f.file_size = file.size
    f.updated_at = new Date().toLocaleString('zh-CN')
    
    return { success: true, data: f }
  },
  
  renameFile: async (id, name) => {
    const file = mockData.files.find(f => f.id === parseInt(id))
    if (!file) return { success: false, message: '文件不存在' }
    
    file.original_name = name
    file.updated_at = new Date().toLocaleString('zh-CN')
    
    return { success: true, data: file }
  },
  
  moveFile: async (id, folderId) => {
    const file = mockData.files.find(f => f.id === parseInt(id))
    if (!file) return { success: false, message: '文件不存在' }
    
    file.folder_id = folderId ? parseInt(folderId) : null
    file.updated_at = new Date().toLocaleString('zh-CN')
    
    return { success: true, data: file }
  },
  
  deleteFile: async (id) => {
    const index = mockData.files.findIndex(f => f.id === parseInt(id))
    if (index === -1) return { success: false, message: '文件不存在' }
    
    const file = mockData.files[index]
    if (currentUser) {
      currentUser.storage_used -= file.file_size
    }
    
    mockData.files.splice(index, 1)
    return { success: true }
  },
  
  addTag: async (fileId, tagId) => {
    const file = mockData.files.find(f => f.id === parseInt(fileId))
    if (!file) return { success: false, message: '文件不存在' }
    
    const tag = mockData.tags.find(t => t.id === parseInt(tagId))
    if (!tag) return { success: false, message: '标签不存在' }
    
    if (!file.tags.find(t => t.id === parseInt(tagId))) {
      file.tags.push(tag)
      tag.file_count++
    }
    
    return { success: true }
  },
  
  removeTag: async (fileId, tagId) => {
    const file = mockData.files.find(f => f.id === parseInt(fileId))
    if (!file) return { success: false, message: '文件不存在' }
    
    const tagIndex = file.tags.findIndex(t => t.id === parseInt(tagId))
    if (tagIndex !== -1) {
      const tag = file.tags[tagIndex]
      file.tags.splice(tagIndex, 1)
      if (tag.file_count > 0) tag.file_count--
    }
    
    return { success: true }
  }
}

export const folderAPI = {
  getFolders: async () => {
    let folders = mockData.folders
    if (currentUser && currentUser.role !== 'ADMIN') {
      folders = folders.filter(f => f.owner_id === currentUser.id)
    }
    return { success: true, data: folders }
  },
  
  getContents: async (id, params) => {
    const folder = mockData.folders.find(f => f.id === parseInt(id))
    if (!folder) return { success: false, message: '文件夹不存在' }
    
    let files = mockData.files.filter(f => f.folder_id === parseInt(id))
    const page = parseInt(params.page) || 1
    const size = parseInt(params.size) || 20
    const start = (page - 1) * size
    const end = start + size
    
    return {
      success: true,
      data: {
        content: files.slice(start, end),
        total: files.length
      }
    }
  },
  
  createFolder: async (name, parentId) => {
    if (!currentUser) return { success: false, message: '请先登录' }
    
    const newFolder = {
      id: mockData.folders.length + 1,
      name,
      parent_id: parentId ? parseInt(parentId) : null,
      owner_id: currentUser.id
    }
    
    mockData.folders.push(newFolder)
    return { success: true, data: newFolder }
  },
  
  renameFolder: async (id, name) => {
    const folder = mockData.folders.find(f => f.id === parseInt(id))
    if (!folder) return { success: false, message: '文件夹不存在' }
    
    folder.name = name
    return { success: true, data: folder }
  },
  
  moveFolder: async (id, parentId) => {
    const folder = mockData.folders.find(f => f.id === parseInt(id))
    if (!folder) return { success: false, message: '文件夹不存在' }
    
    folder.parent_id = parentId ? parseInt(parentId) : null
    return { success: true, data: folder }
  },
  
  deleteFolder: async (id) => {
    const folder = mockData.folders.find(f => f.id === parseInt(id))
    if (!folder) return { success: false, message: '文件夹不存在' }
    
    const hasChildren = mockData.folders.some(f => f.parent_id === parseInt(id))
    if (hasChildren) {
      return { success: false, message: '非空文件夹无法删除' }
    }
    
    const hasFiles = mockData.files.some(f => f.folder_id === parseInt(id))
    if (hasFiles) {
      return { success: false, message: '非空文件夹无法删除' }
    }
    
    const index = mockData.folders.findIndex(f => f.id === parseInt(id))
    mockData.folders.splice(index, 1)
    
    return { success: true }
  }
}

export const tagAPI = {
  getTags: async (params) => {
    let tags = [...mockData.tags]
    
    if (params.keyword) {
      tags = tags.filter(t => t.name.includes(params.keyword))
    }
    
    const page = parseInt(params.page) || 1
    const size = parseInt(params.size) || 20
    const start = (page - 1) * size
    const end = start + size
    
    return {
      success: true,
      data: {
        content: tags.slice(start, end),
        total: tags.length
      }
    }
  },
  
  createTag: async (name) => {
    const exists = mockData.tags.find(t => t.name === name)
    if (exists) return { success: false, message: '标签已存在' }
    
    const newTag = {
      id: mockData.tags.length + 1,
      name,
      file_count: 0
    }
    
    mockData.tags.push(newTag)
    return { success: true, data: newTag }
  },
  
  deleteTag: async (id) => {
    const index = mockData.tags.findIndex(t => t.id === parseInt(id))
    if (index === -1) return { success: false, message: '标签不存在' }
    
    const tag = mockData.tags[index]
    
    // 从所有文件中移除该标签
    mockData.files.forEach(file => {
      const tagIndex = file.tags.findIndex(t => t.id === parseInt(id))
      if (tagIndex !== -1) {
        file.tags.splice(tagIndex, 1)
      }
    })
    
    mockData.tags.splice(index, 1)
    return { success: true }
  }
}

export const adminAPI = {
  getUsers: async (params) => {
    if (!currentUser || currentUser.role !== 'ADMIN') {
      return { success: false, message: '无权限' }
    }
    
    let users = [...mockData.users]
    
    if (params.keyword) {
      users = users.filter(u => u.username.includes(params.keyword))
    }
    
    const page = parseInt(params.page) || 1
    const size = parseInt(params.size) || 20
    const start = (page - 1) * size
    const end = start + size
    
    return {
      success: true,
      data: {
        content: users.slice(start, end),
        total: users.length
      }
    }
  },
  
  deleteUser: async (id) => {
    if (!currentUser || currentUser.role !== 'ADMIN') {
      return { success: false, message: '无权限' }
    }
    
    const index = mockData.users.findIndex(u => u.id === parseInt(id))
    if (index === -1) return { success: false, message: '用户不存在' }
    
    mockData.users.splice(index, 1)
    mockData.folders = mockData.folders.filter(f => f.owner_id !== parseInt(id))
    mockData.files = mockData.files.filter(f => f.owner_id !== parseInt(id))
    
    return { success: true }
  },
  
  resetPassword: async (id) => {
    if (!currentUser || currentUser.role !== 'ADMIN') {
      return { success: false, message: '无权限' }
    }
    
    const user = mockData.users.find(u => u.id === parseInt(id))
    if (!user) return { success: false, message: '用户不存在' }
    
    user.password = '123456'
    return { success: true, message: '密码已重置为123456' }
  },
  
  setQuota: async (id, quota) => {
    if (!currentUser || currentUser.role !== 'ADMIN') {
      return { success: false, message: '无权限' }
    }
    
    const user = mockData.users.find(u => u.id === parseInt(id))
    if (!user) return { success: false, message: '用户不存在' }
    
    user.storage_quota = quota
    return { success: true }
  },
  
  getAllFiles: async (params) => {
    if (!currentUser || currentUser.role !== 'ADMIN') {
      return { success: false, message: '无权限' }
    }
    
    let files = [...mockData.files]
    
    if (params.userId) {
      files = files.filter(f => f.owner_id === parseInt(params.userId))
    }
    
    const page = parseInt(params.page) || 1
    const size = parseInt(params.size) || 20
    const start = (page - 1) * size
    const end = start + size
    
    return {
      success: true,
      data: {
        content: files.slice(start, end),
        total: files.length
      }
    }
  },
  
  getUserFiles: async (userId, params) => {
    if (!currentUser || currentUser.role !== 'ADMIN') {
      return { success: false, message: '无权限' }
    }
    
    let files = mockData.files.filter(f => f.owner_id === parseInt(userId))
    
    const page = parseInt(params.page) || 1
    const size = parseInt(params.size) || 20
    const start = (page - 1) * size
    const end = start + size
    
    return {
      success: true,
      data: {
        content: files.slice(start, end),
        total: files.length
      }
    }
  },
  
  getConfig: async () => {
    if (!currentUser || currentUser.role !== 'ADMIN') {
      return { success: false, message: '无权限' }
    }
    
    return {
      success: true,
      data: {
        allowed_extensions: '.pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.jpg,.png,.zip,.txt,.md',
        max_file_size: 524288000,
        default_storage_quota: 1073741824
      }
    }
  },
  
  updateConfig: async (config) => {
    if (!currentUser || currentUser.role !== 'ADMIN') {
      return { success: false, message: '无权限' }
    }
    
    return { success: true, data: config }
  },
  
  getFolderTemplates: async () => {
    if (!currentUser || currentUser.role !== 'ADMIN') {
      return { success: false, message: '无权限' }
    }
    
    return {
      success: true,
      data: [
        { id: 1, name: '文档资料', parent_id: null },
        { id: 2, name: '项目文档', parent_id: 1 },
        { id: 3, name: '个人资料', parent_id: null }
      ]
    }
  },
  
  updateFolderTemplates: async (templates) => {
    if (!currentUser || currentUser.role !== 'ADMIN') {
      return { success: false, message: '无权限' }
    }
    
    return { success: true, data: templates }
  },
  
  getLogs: async (params) => {
    if (!currentUser || currentUser.role !== 'ADMIN') {
      return { success: false, message: '无权限' }
    }
    
    let logs = [...mockData.logs]
    
    if (params.action) {
      logs = logs.filter(l => l.action === params.action)
    }
    
    const page = parseInt(params.page) || 1
    const size = parseInt(params.size) || 20
    const start = (page - 1) * size
    const end = start + size
    
    return {
      success: true,
      data: {
        content: logs.slice(start, end),
        total: logs.length
      }
    }
  }
}

export default { authAPI, fileAPI, folderAPI, tagAPI, adminAPI }