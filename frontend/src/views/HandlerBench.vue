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
          <el-tab-pane :label="`我的待处置（${counts.HANDLING}）`" name="HANDLING" />
          <el-tab-pane :label="`待复核（${counts.REVIEWING}）`" name="REVIEWING" />
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
        <el-table-column prop="title" label="事件标题" min-width="200" show-overflow-tooltip />
        <el-table-column label="等级" width="76" align="center">
          <template #default="{ row }">
            <span :class="row.level === 1 ? 'lv-high' : row.level === 2 ? 'lv-mid' : 'lv-low'">{{ row.levelName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="attackIps" label="攻击IP" width="140" show-overflow-tooltip />
        <el-table-column prop="victimIps" label="受害IP" width="150" show-overflow-tooltip />
        <el-table-column prop="handlerName" label="处置人" width="90" />
        <el-table-column label="状态" width="96" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 'REVIEWING' ? 'warning' : 'danger'">{{ row.statusName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="assignTime" label="指派时间" width="150" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="go(row)">{{ tab === 'HANDLING' ? '去处置' : '查看' }}</el-button>
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
import { pageEvents, statOverview } from '@/api/event'

const router = useRouter()
const tab = ref('HANDLING')
const rows = ref<any[]>([])
const total = ref(0)
const loading = ref(false)
const counts = reactive<any>({ HANDLING: 0, REVIEWING: 0 })
const cards = ref<any[]>([])
const q = reactive<any>({ pageNum: 1, pageSize: 20 })

const rowClass = ({ row }: any) => (row.isTimeout ? 'warning-row' : '')

const loadStats = async () => {
  const s: any = await statOverview()
  counts.HANDLING = s.byStatus?.HANDLING || 0
  counts.REVIEWING = s.byStatus?.REVIEWING || 0
  cards.value = [
    { label: '待处置', value: counts.HANDLING, color: '#c62828' },
    { label: '待复核', value: counts.REVIEWING, color: '#e07b00' },
    { label: '已处置次数', value: s.disposalCount || 0, color: '#1f5aa8' },
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

onMounted(load)
</script>

<style scoped>
.head-row { display: flex; align-items: center; gap: 12px; }
.head-row :deep(.el-tabs__header) { margin-bottom: 8px; }
.lv-high { color: #c62828; font-weight: 600; }
.lv-mid { color: #e07b00; font-weight: 600; }
.lv-low { color: #2e7d32; }
</style>
