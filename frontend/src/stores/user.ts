import { defineStore } from 'pinia'
import { login as loginApi, me } from '@/api/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('blued_token') || '',
    userId: 0,
    username: '',
    realName: '',
    roles: [] as string[],
    roleNames: [] as string[]
  }),
  getters: {
    isLogin: (s) => !!s.token,
    hasRole: (s) => (r: string) => s.roles.includes(r),
    roleText: (s) => s.roleNames.join(' / ')
  },
  actions: {
    async login(username: string, password: string) {
      const res: any = await loginApi({ username, password })
      this.token = res.token
      this.userId = res.userId
      this.username = res.username
      this.realName = res.realName
      this.roles = res.roles || []
      this.roleNames = res.roleNames || []
      localStorage.setItem('blued_token', res.token)
      return res
    },
    async loadMe() {
      if (!this.token) return
      try {
        const res: any = await me()
        this.userId = res.userId
        this.username = res.username
        this.realName = res.realName
        this.roles = res.roles || []
        this.roleNames = res.roleNames || []
      } catch (e) {
        this.logout()
      }
    },
    logout() {
      this.token = ''
      this.roles = []
      localStorage.removeItem('blued_token')
    }
  }
})
