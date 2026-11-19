<template>
  <div>
    <div class="page-card">
      <div class="page-title">字典管理（来源设备等字典由管理员自定义，修改后即时生效）</div>
      <div class="filter-row">
        <el-select v-model="dictType" style="width:260px" @change="load">
          <el-option v-for="t in types" :key="t.value" :label="t.label" :value="t.value" />
        </el-select>
        <el-button type="primary" @click="openAdd">+ 新增字典项</el-button>
        <span style="color:#909399;font-size:12px">两级字典（如攻击类型）请先选大类作为父项</span>
      </div>
    </div>

    <div class="page-card">
      <el-table :data="rows" size="small" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="大类" width="180">
          <template #default="{ row }">{{ parentName(row.parentId) }}</template>
        </el-table-column>
        <el-table-column prop="code" label="编码" width="200" />
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column label="操作" width="90">
          <template #default="{ row }">
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dlg" title="新增字典项" width="460px">
      <el-form label-width="90px">
        <el-form-item v-if="isTwoLevel" label="所属大类">
          <el-select v-model="form.parentId" style="width:100%" clearable placeholder="顶级（不选）">
            <el-option v-for="p in parents" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="编码"><el-input v-model="form.code" placeholder="大写英文，如 HONEYPOT" /></el-form-item>
        <el-form-item label="名称"><el-input v-model="form.name" placeholder="中文名称" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="1" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dlg = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDict, saveDictItem, deleteDictItem, clearDictCache } from '@/api/dict'

const types = [
  { label: '来源设备（device_type）', value: 'device_type' },
  { label: '攻击类型大类（attack_type_category）', value: 'attack_type_category' },
  { label: '攻击类型子类（attack_type）', value: 'attack_type' },
  { label: '攻击阶段（attack_phase）', value: 'attack_phase' },
  { label: '攻击方向（attack_direction）', value: 'attack_direction' },
  { label: '攻击成功与否（attack_result）', value: 'attack_result' },
  { label: '处置动作（disposal_action）', value: 'disposal_action' },
  { label: '执行设备（exec_device）', value: 'exec_device' },
  { label: '验证结果（verify_result）', value: 'verify_result' }
]

const dictType = ref('device_type')
const rows = ref<any[]>([])
const loading = ref(false)
const dlg = ref(false)
const form = reactive<any>({ parentId: null, code: '', name: '', sort: 99 })
const categories = ref<any[]>([])

const isTwoLevel = computed(() => dictType.value === 'attack_type')
const parents = computed(() => categories.value)
const parentName = (pid: number) => {
  if (!pid) return '-'
  const hit = categories.value.find((x: any) => x.id === pid)
  return hit ? hit.name : '-'
}

const load = async () => {
  loading.value = true
  try {
    rows.value = await getDict(dictType.value)
    categories.value = await getDict('attack_type_category')
  } finally {
    loading.value = false
  }
}

const openAdd = () => {
  form.parentId = null
  form.code = ''
  form.name = ''
  form.sort = 99
  dlg.value = true
}

const save = async () => {
  if (!form.code || !form.name) return ElMessage.warning('请填写编码与名称')
  await saveDictItem({ dictType: dictType.value, ...form })
  clearDictCache()
  ElMessage.success('已保存')
  dlg.value = false
  load()
}

const remove = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确认删除「${row.name}」？`, '提示', { type: 'warning' })
  } catch { return }
  await deleteDictItem(row.id)
  clearDictCache()
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>
