<template>
  <div>
    <el-row :gutter="12">
      <el-col :span="4" v-for="c in cards" :key="c.key">
        <div class="stat-card" @click="go(c)">
          <div class="label">{{ c.label }}</div>
          <div class="value" :style="{ color: c.color }">{{ c.value }}</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="12" style="margin-top:12px">
      <el-col :span="12">
        <div class="page-card">
          <div class="page-title">攻击类型大类分布</div>
          <div ref="chart1" style="height:280px"></div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="page-card">
          <div class="page-title">攻击方向与攻击结果</div>
          <div ref="chart2" style="height:280px"></div>
        </div>
      </el-col>
    </el-row>

    <div class="page-card">
      <div class="page-title">超时事件</div>
      <el-table :data="timeoutRows" size="small" empty-text="暂无超时事件">
        <el-table-column prop="eventNo" label="事件编号" width="180" />
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="levelName" label="等级" width="80" />
        <el-table-column prop="statusName" label="当前阶段" width="100" />
        <el-table-column prop="foundTime" label="发现时间" width="170" />
        <el-table-column label="操作" width="90">
          <template #default="{ row }">
            <el-button link type="primary" @click="$router.push('/events/' + row.id)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { statOverview, pageEvents } from '@/api/event'
import { getAllDict, nameOf } from '@/api/dict'

const router = useRouter()
const cards = ref<any[]>([])
const timeoutRows = ref<any[]>([])
const chart1 = ref<HTMLElement>()
const chart2 = ref<HTMLElement>()
let dict: any = {}

onMounted(async () => {
  dict = await getAllDict()
  const s: any = await statOverview()
  cards.value = [
    { key: 'today', label: '今日新增', value: s.today, color: '#1f5aa8', to: '/events' },
    { key: 'submitted', label: '待受理', value: s.byStatus?.SUBMITTED || 0, color: '#e07b00', tab: 'ALL', status: 'SUBMITTED' },
    { key: 'handling', label: '处置中', value: s.byStatus?.HANDLING || 0, color: '#1f5aa8', status: 'HANDLING' },
    { key: 'timeout', label: '超时', value: s.timeout, color: '#c62828', tab: 'TIMEOUT' },
    { key: 'archived', label: '已归档', value: s.byStatus?.ARCHIVED || 0, color: '#2e7d32', tab: 'ARCHIVED' },
    { key: 'total', label: '事件总数', value: s.total, color: '#455a64', to: '/events' }
  ]
  timeoutRows.value = (await pageEvents({ tab: 'TIMEOUT', pageNum: 1, pageSize: 8 })).records

  await nextTick()
  draw(s)
})

const go = (c: any) => {
  if (c.to) return router.push(c.to)
  router.push({ path: '/events', query: { tab: c.tab || 'ALL', status: c.status || '' } })
}

const draw = (s: any) => {
  if (chart1.value) {
    const data = (s.byCategory || []).map((x: any) => ({
      name: nameOf(dict, 'attack_type_category', x.name), value: x.value
    }))
    const c1 = echarts.init(chart1.value)
    c1.setOption({
      tooltip: { trigger: 'item' },
      legend: { bottom: 0, type: 'scroll' },
      series: [{ type: 'pie', radius: ['38%', '62%'], data, label: { formatter: '{b}\n{c}' } }]
    })
  }
  if (chart2.value) {
    const dir = (s.byDirection || []).map((x: any) => nameOf(dict, 'attack_direction', x.name))
    const dirV = (s.byDirection || []).map((x: any) => x.value)
    const res = (s.byResult || []).map((x: any) => nameOf(dict, 'attack_result', x.name))
    const resV = (s.byResult || []).map((x: any) => x.value)
    const c2 = echarts.init(chart2.value)
    c2.setOption({
      tooltip: { trigger: 'axis' },
      legend: { bottom: 0 },
      grid: { left: 40, right: 20, top: 30, bottom: 50 },
      xAxis: { type: 'category', data: ['攻击方向', ''] },
      yAxis: { type: 'value' },
      series: [
        { name: '攻击方向', type: 'bar', stack: 'a', data: dirV, itemStyle: { color: '#1f5aa8' } },
        { name: '攻击结果', type: 'bar', data: resV, itemStyle: { color: '#e07b00' } }
      ]
    })
  }
}
</script>

<style scoped>
.stat-card { background: #fff; border-radius: 6px; padding: 16px; cursor: pointer;
  transition: box-shadow .2s; }
.stat-card:hover { box-shadow: 0 6px 18px rgba(11, 61, 145, .12); }
.stat-card .label { font-size: 13px; color: #7b8794; }
.stat-card .value { font-size: 26px; font-weight: 700; margin-top: 6px; }
</style>
