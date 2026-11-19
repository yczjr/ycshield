import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  { path: '/login', component: () => import('@/views/Login.vue') },
  {
    path: '/',
    component: () => import('@/layout/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: '态势大屏', component: () => import('@/views/Dashboard.vue') },
      { path: 'overview', name: '总览', component: () => import('@/views/Overview.vue') },
      { path: 'bench/analyst', name: '研判台', component: () => import('@/views/AnalystBench.vue') },
      { path: 'bench/handler', name: '处置台', component: () => import('@/views/HandlerBench.vue') },
      { path: 'events', name: '事件列表', component: () => import('@/views/EventList.vue') },
      { path: 'events/create', name: '事件上报', component: () => import('@/views/EventCreate.vue') },
      { path: 'events/:id', name: '事件详情', component: () => import('@/views/EventDetail.vue') },
      { path: 'system/dict', name: '字典管理', component: () => import('@/views/SystemDict.vue') },
      { path: 'system/user', name: '用户管理', component: () => import('@/views/SystemUser.vue') }
    ]
  }
]

const router = createRouter({ history: createWebHashHistory(), routes })

export const homeOf = (roles: string[]) => {
  if (roles.includes('ADMIN')) return '/dashboard'
  if (roles.includes('ANALYST')) return '/bench/analyst'
  if (roles.includes('HANDLER')) return '/bench/handler'
  return '/events/create'
}

router.beforeEach((to: any, _from: any, next: any) => {
  const token = localStorage.getItem('blued_token')
  if (to.path !== '/login' && !token) return next('/login')
  if (to.path === '/login' && token) return next('/')
  next()
})

export default router
