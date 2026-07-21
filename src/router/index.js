import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: () => import('@/views/ForgotPassword.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/Dashboard.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Files',
        component: () => import('@/views/Files.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'files',
        name: 'FilesList',
        component: () => import('@/views/Files.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'tags',
        name: 'Tags',
        component: () => import('@/views/Tags.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'admin',
        name: 'Admin',
        component: () => import('@/views/Admin.vue'),
        meta: { requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'system-config',
        name: 'SystemConfig',
        component: () => import('@/views/SystemConfig.vue'),
        meta: { requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'folder-templates',
        name: 'FolderTemplates',
        component: () => import('@/views/FolderTemplates.vue'),
        meta: { requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'operation-logs',
        name: 'OperationLogs',
        component: () => import('@/views/OperationLogs.vue'),
        meta: { requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'all-files',
        name: 'AllFiles',
        component: () => import('@/views/AllFiles.vue'),
        meta: { requiresAuth: true, requiresAdmin: true }
      }
    ]
  },
  {
    path: '/',
    redirect: '/dashboard'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  console.log('Navigating from', from.path, 'to', to.path)
  const token = localStorage.getItem('accessToken')
  console.log('Token exists:', !!token)
  
  if (to.meta.requiresAuth && !token) {
    console.log('Requires auth but no token, redirecting to login')
    next('/login')
  } else if (to.meta.requiresAdmin) {
    const role = localStorage.getItem('userRole')
    console.log('User role:', role)
    if (role !== 'ADMIN') {
      console.log('Requires admin but user is not admin')
      next('/')
    } else {
      next()
    }
  } else if ((to.path === '/login' || to.path === '/register' || to.path === '/forgot-password') && token) {
    console.log('Already logged in, redirecting to home')
    next('/')
  } else {
    next()
  }
})

export default router