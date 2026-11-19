<template>
  <div class="login-wrap">
    <div class="login-card">
      <div class="brand">
        <div class="logo">蓝盾YC</div>
        <div class="sub">蓝队防守作战协同平台</div>
      </div>
      <el-form :model="form" @submit.prevent>
        <el-form-item>
          <el-input v-model="form.username" size="large" placeholder="账号" :prefix-icon="User" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" size="large" type="password" show-password
                    placeholder="密码" :prefix-icon="Lock" @keyup.enter="submit" />
        </el-form-item>
        <el-button type="primary" size="large" style="width:100%" :loading="loading" @click="submit">
          登 录
        </el-button>
      </el-form>
      <div class="hint">请使用值班账号登录，账号由管理员统一分配</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { homeOf } from '@/router'

const router = useRouter()
const user = useUserStore()
const loading = ref(false)
const form = reactive({ username: '', password: '' })

const submit = async () => {
  if (!form.username || !form.password) return ElMessage.warning('请输入账号和密码')
  loading.value = true
  try {
    const res: any = await user.login(form.username.trim(), form.password.trim())
    ElMessage.success('登录成功')
    router.push(homeOf(res.roles || []))
  } catch (e) {
    /* 错误已由拦截器提示 */
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-wrap { height: 100vh; display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #0b2647 0%, #123b6b 55%, #1f5aa8 100%); }
.login-card { width: 380px; background: #fff; border-radius: 10px; padding: 36px 34px 26px;
  box-shadow: 0 18px 50px rgba(0, 0, 0, .28); }
.brand { text-align: center; margin-bottom: 26px; }
.brand .logo { font-size: 34px; font-weight: 800; color: #0b3d91; letter-spacing: 8px; }
.brand .sub { font-size: 13px; color: #7b8794; margin-top: 6px; letter-spacing: 1px; }
.hint { margin-top: 16px; font-size: 12px; color: #a0a7b0; text-align: center; }
</style>
