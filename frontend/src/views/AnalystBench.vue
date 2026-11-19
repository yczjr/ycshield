<template>
  <div>
    <el-row :gutter="12">
      <el-col :span="6" v-for="c in cards" :key="c.label">
        <div class="stat-card">
          <div class="label">{{ c.label }}</div>
          <div class="value" :style="{ color: c.color }">{{ c.value }}</div>
        </div>
      </el-col>
    </el-row>

    <div class="page-card" style="margin-top:12px">
      <div class="head-row">
        <el-tabs v-model="tab" @tab-change="load" style="flex:1">
          <el-tab-pane :label="`待认领（${counts.SUBMITTED}）`" name="SUBMITTED" />
          <el-tab-pane :label="`我的研判中（${counts.ANALYZING}）`" name="ANALYZING" />
        </el-tabs>
        <el-button size="small" plain @click="load">刷新</el-button>
      </div>

      <el-table :data="rows" v-loading="loading" size="small" row-key="id"
                :row-class-name="rowClass" @row-dblclick="go">
        <el-table-column prop="eventNo" label="事件编号" width="168">
          <template #default="{ row }">
            <el-link type="primary" class="mono" @click="go(row)">{{ row.eventNo }}</el-link>
          </template>
        </el-table-column>
        <el-table-column label="攻击类型" width="150">
          <template #default="{ row }">{{ row.attackTypeName }}</template>
        </el-table-column>
        <el-table-column label="等级" width="76" align="center">
          <template #default="{ row }">
            <span :class="row.level === 1 ? 'lv-high' : row.level === 2 ? 'lv-mid' : 'lv-low'">{{ row.levelName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="attackIps" label="攻击IP" width="140" show-overflow-tooltip />
        <el-table-column prop="victimIps" label="受害IP" width="150" show-overflow-tooltip />
        <el-table-column prop="assetName" label="受害资产" width="120" show-overflow-tooltip />
        <el-table-column label="攻击结果" width="96" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.attackResult === 'SUCCESS' ? 'danger' : row.attackResult === 'FAILED' ? 'success' : 'warning'">
              {{ row.attackResultName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="deviceTypeName" label="来源设备" width="110" />
        <el-table-column prop="foundTime" label="发现时间" width="150" />
        <el-table-column prop="reporterName" label="上报人" width="90" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button v-if="tab === 'SUBMITTED'" link type="primary" @click="claim(row)">认领</el-button>
            <el-button link type="primary" @click="go(row)">研判</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination style="margin-top:12px;justify-content:flex-end" background
                     layout="total, prev, pager, next" :total="total"
                     :page-size="q.pageSize" :current-page="q.pageNum"
                     @current-change="onPage" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { pageEvents, claimEvent, statOverview } from '@/api/event'

const router = useRouter()
const tab = ref('SUBMITTED')
const rows = ref<any[]>([])
const total = ref(0)
const loading = ref(false)
const counts = reactive<any>({ SUBMITTED: 0, ANALYZING: 0 })
const cards = ref<any[]>([])
const q = reactive<any>({ pageNum: 1, pageSize: 20 })

const rowClass = ({ row }: any) => (row.isTimeout ? 'warning-row' : '')

const loadStats = async () => {
  const s: any = await statOverview()
  counts.SUBMITTED = s.byStatus?.SUBMITTED || 0
  counts.ANALYZING = s.byStatus?.ANALYZING || 0
  cards.value = [
    { label: '待认领', value: counts.SUBMITTED, color: '#e07b00' },
    { label: '研判中', value: counts.ANALYZING, color: '#1f5aa8' },
    { label: '超时预警', value: s.timeout, color: '#c62828' },
    { label: '已归档', value: s.byStatus?.ARCHIVED || 0, color: '#2e7d32' }
  ]
}

const load = async () => {
  loading.value = true
  try {
    const data: any = await pageEvents({ status: tab.value, pageNum: q.pageNum, pageSize: q.pageSize })
    rows.value = data.records
    total.value = data.total
    await loadStats()
  } finally {
    loading.value = false
  }
}

const onPage = (p: number) => { q.pageNum = p; load() }
const go = (row: any) => router.push('/events/' + row.id)
const claim = async (row: any) => {
  await claimEvent(row.id)
  ElMessage.success('已认领，进入研判')
  router.push('/events/' + row.id)
}

onMounted(load)
</script>

<style scoped>
.head-row { display: flex; align-items: center; gap: 12px; }
.head-row :deep(.el-tabs__header) { margin-bottom: 8px; }
.lv-high { color: #c62828; font-weight: 600; }
.lv-mid { color: #e07b00; font-weight: 600; }
.lv-low { color: #2e7d32; }
</style>
