<template>
  <div>
    <div class="page-card">
      <el-tabs v-model="tab" @tab-change="reload">
        <el-tab-pane label="全部" name="ALL" />
        <el-tab-pane label="我的上报" name="MINE" />
        <el-tab-pane label="重要" name="IMPORTANT" />
        <el-tab-pane label="超时" name="TIMEOUT" />
        <el-tab-pane label="已归档" name="ARCHIVED" />
      </el-tabs>
      <div class="filter-row">
        <el-input v-model="q.keyword" placeholder="编号 / 标题 / IP" clearable style="width:190px" />
        <el-cascader v-model="typePath" :options="typeTree" :props="{ checkStrictly: true }"
                     placeholder="攻击类型" clearable style="width:200px" @change="onType" />
        <el-select v-model="q.attackDirection" placeholder="攻击方向" clearable style="width:130px">
          <el-option v-for="d in dict.attack_direction || []" :key="d.code" :label="d.name" :value="d.code" />
        </el-select>
        <el-select v-model="q.attackResult" placeholder="攻击结果" clearable style="width:140px">
          <el-option v-for="d in dict.attack_result || []" :key="d.code" :label="d.name" :value="d.code" />
        </el-select>
        <el-select v-model="q.level" placeholder="危害等级" clearable style="width:120px">
          <el-option label="高危" :value="1" />
          <el-option label="中危" :value="2" />
          <el-option label="低危" :value="3" />
        </el-select>
        <el-select v-model="q.status" placeholder="当前阶段" clearable style="width:130px">
          <el-option v-for="d in dict.event_status || []" :key="d.code" :label="d.name" :value="d.code" />
        </el-select>
        <el-select v-model="q.deviceType" placeholder="来源设备" clearable style="width:140px">
          <el-option v-for="d in dict.device_type || []" :key="d.code" :label="d.name" :value="d.code" />
        </el-select>
        <el-date-picker v-model="range" type="daterange" value-format="YYYY-MM-DD"
                        start-placeholder="发现起" end-placeholder="发现止" style="width:250px" />
        <el-button type="primary" @click="reload">查询</el-button>
        <el-button @click="reset">重置</el-button>
        <el-button type="success" @click="$router.push('/events/create')">+ 上报事件</el-button>
      </div>
    </div>

    <div class="page-card">
      <div class="page-title">监测事件上报 <span style="font-weight:400;color:#909399">共 {{ total }} 条</span></div>
      <el-table :data="rows" v-loading="loading" size="small" row-key="id"
                :row-class-name="rowClass" @row-dblclick="go">
        <el-table-column prop="eventNo" label="事件编号" width="168">
          <template #default="{ row }">
            <el-link type="primary" class="mono" @click="go(row)">{{ row.eventNo }}</el-link>
          </template>
        </el-table-column>
        <el-table-column label="攻击类型" width="150">
          <template #default="{ row }">
            <div>{{ row.attackTypeName }}</div>
            <el-tag size="small" type="info" effect="plain">{{ row.attackTypeCategoryName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="危害等级" width="86" align="center">
          <template #default="{ row }">
            <span :class="row.level === 1 ? 'tag-high' : row.level === 2 ? 'tag-mid' : 'tag-low'">
              {{ row.levelName }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="攻击方向" width="92" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.attackDirection === 'IN2IN' ? 'warning' : 'info'" effect="plain">
              {{ row.attackDirectionName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="attackIps" label="攻击IP" width="140" show-overflow-tooltip />
        <el-table-column label="受害IP" width="150">
          <template #default="{ row }">
            <div v-for="(ip, i) in splitIps(row.victimIps)" :key="i" class="mono" style="font-size:12px">{{ ip }}</div>
          </template>
        </el-table-column>
        <el-table-column label="来源设备" width="120">
          <template #default="{ row }">
            {{ row.deviceTypeName }}<span v-if="row.deviceName"> · {{ row.deviceName }}</span>
            <el-tag v-if="row.alarmCount > 1" size="small" type="success" effect="plain">+{{ row.alarmCount - 1 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="assetName" label="受害资产" width="130" show-overflow-tooltip />
        <el-table-column label="攻击结果" width="96" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="resultTag(row.attackResult)">{{ row.attackResultName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="当前阶段" width="96" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="statusTag(row.status)">{{ row.statusName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="foundTime" label="发现时间" width="150" />
        <el-table-column prop="reporterName" label="上报人" width="90" />
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="go(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top:12px;justify-content:flex-end" background
                     layout="total, sizes, prev, pager, next, jumper"
                     :total="total" :page-size="q.pageSize" :current-page="q.pageNum"
                     @current-change="onPage" @size-change="onSize" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { pageEvents } from '@/api/event'
import { getAllDict, getAttackTypeTree } from '@/api/dict'

const router = useRouter()
const route = useRoute()
const dict = ref<any>({})
const typeTree = ref<any[]>([])
const typePath = ref<any>([])
const rows = ref<any[]>([])
const total = ref(0)
const loading = ref(false)
const range = ref<any>(null)
const tab = ref<string>((route.query.tab as string) || 'ALL')

const q = reactive<any>({
  keyword: '', attackTypeCategory: '', attackType: '', attackDirection: '', attackResult: '',
  level: null, status: (route.query.status as string) || '', deviceType: '',
  pageNum: 1, pageSize: 20
})

const splitIps = (s: string) => (s ? s.split(',').slice(0, 3) : [])

const resultTag = (v: string) =>
  v === 'SUCCESS' ? 'danger' : v === 'PARTIAL' ? 'warning' : v === 'FAILED' ? 'success' : 'info'
const statusTag = (v: string) =>
  v === 'SUBMITTED' ? 'warning' : v === 'ANALYZING' ? 'primary'
    : v === 'HANDLING' ? 'danger' : v === 'REVIEWING' ? 'warning'
      : v === 'ARCHIVED' ? 'success' : 'info'
const rowClass = ({ row }: any) => (row.isTimeout ? 'warning-row' : '')

const load = async () => {
  loading.value = true
  try {
    const data: any = await pageEvents({
      ...q,
      tab: tab.value === 'ALL' ? '' : tab.value,
      beginTime: range.value?.[0] || '',
      endTime: range.value?.[1] || ''
    })
    rows.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const reload = () => { q.pageNum = 1; load() }
const reset = () => {
  Object.assign(q, { keyword: '', attackTypeCategory: '', attackType: '', attackDirection: '',
    attackResult: '', level: null, status: '', deviceType: '', pageNum: 1 })
  typePath.value = []
  range.value = null
  load()
}
const onType = (val: any) => {
  q.attackTypeCategory = val?.[0] || ''
  q.attackType = val?.[1] || ''
}
const onPage = (p: number) => { q.pageNum = p; load() }
const onSize = (s: number) => { q.pageSize = s; q.pageNum = 1; load() }
const go = (row: any) => router.push('/events/' + row.id)

onMounted(async () => {
  dict.value = await getAllDict()
  const tree: any = await getAttackTypeTree()
  typeTree.value = tree.map((c: any) => ({
    value: c.code, label: c.name,
    children: (c.children || []).map((s: any) => ({ value: s.code, label: s.name }))
  }))
  load()
})
</script>
