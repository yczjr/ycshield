<template>
  <div>
    <div class="page-card">
      <div class="head-row">
        <div class="page-title" style="margin:0">用户管理</div>
        <div>
          <el-button plain @click="openSelfPwd">修改我的密码</el-button>
          <el-button type="primary" @click="dlg = true">+ 新增用户</el-button>
        </div>
      </div>
      <div class="tip">管理员可在此新增账号、重置他人密码、启用/停用账号。密码至少 6 位。</div>
    </div>

    <div class="page-card">
      <el-table :data="rows" size="small" v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="username" label="账号" width="140" />
        <el-table-column prop="realName" label="姓名" width="140" />
        <el-table-column label="角色" width="180">
          <template #default="{ row }">
            <el-tag v-for="r in row.roles" :key="r" size="small" style="margin-right:4px">{{ r }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginTime" label="最近登录" width="180" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button link type="primary" @click="openReset(row)">重置密码</el-button>
            <el-button link type="warning" @click="toggle(row)">{{ row.status === 1 ? '停用' : '启用' }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dlg" title="新增用户" width="460px">
      <el-form label-width="90px">
        <el-form-item label="账号"><el-input v-model="form.username" /></el-form-item>
        <el-form-item label="姓名"><el-input v-model="form.realName" /></el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.roleCode" style="width:100%">
            <el-option label="监测员" value="MONITOR" /><el-option label="分析员" value="ANALYST" />
            <el-option label="处置员" value="HANDLER" /><el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="初始密码">
          <el-input v-model="form.password" placeholder="留空默认 Blued@123456" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dlg = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="pwdDlg" :title="`重置密码 - ${target?.username || ''}`" width="440px">
      <el-form label-width="90px">
        <el-form-item label="新密码">
          <el-input v-model="newPwd" show-password placeholder="至少 6 位" />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="newPwd2" show-password placeholder="再输一次" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdDlg = false">取消</el-button>
        <el-button type="primary" @click="doReset">确认重置</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="selfDlg" title="修改我的密码" width="440px">
      <el-form label-width="90px">
        <el-form-item label="原密码"><el-input v-model="selfOld" show-password /></el-form-item>
        <el-form-item label="新密码"><el-input v-model="selfNew" show-password placeholder="至少 6 位" /></el-form-item>
        <el-form-item label="确认密码"><el-input v-model="selfNew2" show-password /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="selfDlg = false">取消</el-button>
        <el-button type="primary" @click="doSelfPwd">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listUsers, createUser, resetUserPassword, changeMyPassword } from '@/api/auth'
import http from '@/api/http'

const rows = ref<any[]>([])
const loading = ref(false)
const dlg = ref(false)
const pwdDlg = ref(false)
const selfDlg = ref(false)
const target = ref<any>(null)
const newPwd = ref('')
const newPwd2 = ref('')
const selfOld = ref('')
const selfNew = ref('')
const selfNew2 = ref('')
const form = reactive<any>({ username: '', realName: '', roleCode: 'MONITOR', password: '' })

const load = async () => {
  loading.value = true
  try { rows.value = await listUsers() } finally { loading.value = false }
}

const save = async () => {
  if (!form.username || !form.realName) return ElMessage.warning('请填写账号与姓名')
  await createUser(form)
  ElMessage.success('已创建')
  dlg.value = false
  form.username = ''; form.realName = ''; form.password = ''
  load()
}

const openReset = (row: any) => {
  target.value = row
  newPwd.value = ''
  newPwd2.value = ''
  pwdDlg.value = true
}
const doReset = async () => {
  const p = newPwd.value.trim()
  if (p.length < 6) return ElMessage.warning('密码至少 6 位')
  if (p !== newPwd2.value.trim()) return ElMessage.warning('两次输入不一致')
  await resetUserPassword(target.value.id, p)
  ElMessage.success(`已重置 ${target.value.username} 的密码`)
  pwdDlg.value = false
}

const openSelfPwd = () => {
  selfOld.value = ''; selfNew.value = ''; selfNew2.value = ''
  selfDlg.value = true
}
const doSelfPwd = async () => {
  const p = selfNew.value.trim()
  if (p.length < 6) return ElMessage.warning('新密码至少 6 位')
  if (p !== selfNew2.value.trim()) return ElMessage.warning('两次输入不一致')
  await changeMyPassword(selfOld.value.trim(), p)
  ElMessage.success('密码已修改，请牢记新密码')
  selfDlg.value = false
}

const toggle = async (row: any) => {
  await http.post(`/admin/users/${row.id}/status?status=${row.status === 1 ? 0 : 1}`)
  ElMessage.success('已更新')
  load()
}

onMounted(load)
</script>

<style scoped>
.head-row { display: flex; justify-content: space-between; align-items: center; }
.tip { font-size: 12px; color: #909399; margin-top: 8px; }
</style>
