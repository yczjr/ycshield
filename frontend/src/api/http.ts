import axios from 'axios'
import { ElMessage } from 'element-plus'

const http = axios.create({ baseURL: '/api/v1', timeout: 60000 })

http.interceptors.request.use((cfg: any) => {
  const t = localStorage.getItem('blued_token')
  if (t) cfg.headers.Authorization = 'Bearer ' + t
  return cfg
})

http.interceptors.response.use(
  (res: any) => {
    const d = res.data
    if (d && typeof d.code === 'number') {
      if (d.code === 0) return d.data
      if (d.code === 401) {
        localStorage.removeItem('blued_token')
        if (!location.hash.includes('/login')) location.hash = '#/login'
      }
      ElMessage.error(d.message || '请求失败')
      return Promise.reject(new Error(d.message || 'error'))
    }
    return d
  },
  (err: any) => {
    const msg = err?.response?.data?.message || err.message || '网络错误'
    ElMessage.error(msg)
    return Promise.reject(err)
  }
)

export default http
