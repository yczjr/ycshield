<template>
  <router-view v-if="isScreen" />
  <el-container v-else class="layout">
    <el-aside width="212px" class="aside">
      <div class="brand">
        <div class="logo">蓝盾YC</div>
        <div class="sub">BLUE SHIELD</div>
      </div>
      <el-menu :default-active="active" router background-color="#0b2647"
               text-color="#c8d6e5" active-text-color="#ffffff">
        <template v-for="m in menus" :key="m.index">
          <el-sub-menu v-if="m.children" :index="m.index">
            <template #title><el-icon><component :is="m.icon" /></el-icon><span>{{ m.label }}</span></template>
            <el-menu-item v-for="c in m.children" :key="c.index" :index="c.index">{{ c.label }}</el-menu-item>
          </el-sub-menu>
          <el-menu-item v-else :index="m.index">
            <el-icon><component :is="m.icon" /></el-icon><span>{{ m.label }}</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="title">蓝盾YC · 蓝队防守作战协同平台</div>
        <div class="right">
          <el-tag size="small" effect="plain">{{ user.roleText || '未分配角色' }}</el-tag>
          <span class="name">{{ user.realName || user.username }}</span>
          <el-button link type="primary" @click="logout">退出</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const user = useUserStore()
const route = useRoute()
const router = useRouter()

const isScreen = computed(() => route.path === '/dashboard')

const active = computed(() => {
  if (route.path.startsWith('/events/') && route.path !== '/events/create') return '/events'
  return route.path
})

const menus = computed<any[]>(() => {
  const r = user.roles
  const admin = r.includes('ADMIN')
  const list: any[] = []
  if (admin) {
    list.push({ index: '/dashboard', label: '态势大屏', icon: 'DataBoard' })
    list.push({ index: '/bench/analyst', label: '研判台', icon: 'Search' })
    list.push({ index: '/bench/handler', label: '处置台', icon: 'Tools' })
    list.push({ index: '/events', label: '全部事件', icon: 'List' })
    list.push({ index: '/events/create', label: '事件上报', icon: 'Plus' })
    list.push({
      index: 'sys', label: '系统管理', icon: 'Setting',
      children: [
        { index: '/system/dict', label: '字典管理' },
        { index: '/system/user', label: '用户管理' }
      ]
    })
    return list
  }
  // 监测员：只有上报和我的上报
  if (r.includes('MONITOR')) {
    list.push({ index: '/events/create', label: '事件上报', icon: 'Plus' })
    list.push({ index: '/events?tab=MINE', label: '我的上报', icon: 'Tickets' })
  }
  if (r.includes('ANALYST')) {
    list.push({ index: '/bench/analyst', label: '研判台', icon: 'Search' })
    list.push({ index: '/events', label: '事件列表', icon: 'List' })
  }
  if (r.includes('HANDLER')) {
    list.push({ index: '/bench/handler', label: '处置台', icon: 'Tools' })
    list.push({ index: '/events', label: '事件列表', icon: 'List' })
  }
  if (!list.length) {
    list.push({ index: '/events', label: '事件列表', icon: 'List' })
  }
  return list
})

onMounted(() => { user.loadMe() })

const logout = () => {
  user.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout { height: 100vh; }
.aside { background: #0b2647; overflow: hidden; }
.brand { padding: 18px 20px 12px; border-bottom: 1px solid #123257; }
.brand .logo { color: #fff; font-size: 24px; font-weight: 700; letter-spacing: 6px; }
.brand .sub { color: #5b7fa6; font-size: 10px; letter-spacing: 2px; margin-top: 2px; }
.aside :deep(.el-menu) { border-right: none; }
.header { background: #fff; display: flex; align-items: center; justify-content: space-between;
  border-bottom: 1px solid #e6ebf2; height: 56px; }
.header .title { font-size: 15px; font-weight: 600; color: #0b3d91; }
.header .right { display: flex; align-items: center; gap: 12px; }
.header .name { font-size: 13px; color: #55606e; }
.main { background: #f5f7fa; padding: 14px; }
</style>
