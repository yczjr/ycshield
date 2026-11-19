<template>
  <div class="screen" ref="screenEl">
    <header class="top">
      <div class="top-left">
        <span class="brand-mark">蓝盾YC</span>
        <span class="brand-sub">BLUE SHIELD</span>
      </div>
      <div class="top-title">网络安全态势感知平台</div>
      <div class="top-right">
        <nav class="quick-nav">
          <a @click="go2('/bench/analyst')">研判台</a>
          <a @click="go2('/bench/handler')">处置台</a>
          <a @click="go2('/events')">事件列表</a>
          <a @click="go2('/system/user')">用户管理</a>
        </nav>
        <span class="clock mono">{{ now }}</span>
        <span class="live"><i class="dot"></i>实时监测</span>
        <el-button size="small" text class="fs-btn" @click="fullscreen">全屏</el-button>
      </div>
    </header>

    <div class="stage">
      <!-- 左列 -->
      <div class="col left">
        <section class="panel">
          <div class="panel-hd"><span>威胁趋势</span><em class="hd-tip">近 14 天</em></div>
          <div ref="trendEl" class="chart h210"></div>
        </section>
        <section class="panel">
          <div class="panel-hd"><span>攻击类型分布</span></div>
          <div ref="categoryEl" class="chart h200"></div>
        </section>
        <section class="panel">
          <div class="panel-hd"><span>受攻击资产 TOP5</span></div>
          <div class="rank">
            <div v-for="(a, i) in topAssets" :key="a.name" class="rank-row">
              <span class="rk" :class="'rk' + (i + 1)">{{ i + 1 }}</span>
              <span class="rank-name">{{ a.name }}</span>
              <span class="rank-bar"><i :style="{ width: pct(a.value, maxAsset) + '%' }"></i></span>
              <span class="rank-val mono">{{ a.value }}</span>
            </div>
            <div v-if="!topAssets.length" class="empty">暂无数据</div>
          </div>
        </section>
      </div>

      <!-- 中间 -->
      <div class="col center">
        <div class="center-kpi">
          <div class="kpi-item">
            <span class="kpi-ico">◈</span>
            <span class="kpi-label">威胁总量</span>
            <span class="kpi-num mono">{{ kpi.total }}</span>
          </div>
          <div class="kpi-item">
            <span class="kpi-ico up">▲</span>
            <span class="kpi-label">今日新增</span>
            <span class="kpi-num mono c-cyan">{{ kpi.today }}</span>
          </div>
          <div class="kpi-item">
            <span class="kpi-ico warn">▲</span>
            <span class="kpi-label">高危</span>
            <span class="kpi-num mono c-red">{{ kpi.high }}</span>
          </div>
          <div class="kpi-item link" @click="go2('/bench/analyst')">
            <span class="kpi-ico">◇</span>
            <span class="kpi-label">处理中</span>
            <span class="kpi-num mono c-yellow">{{ kpi.pending }}</span>
          </div>
          <div class="kpi-item link" @click="go2('/events?tab=ARCHIVED')">
            <span class="kpi-ico">◇</span>
            <span class="kpi-label">已归档</span>
            <span class="kpi-num mono c-green">{{ kpi.archived }}</span>
          </div>
        </div>

        <div class="map-wrap">
          <div ref="mapEl" class="map"></div>
          <div class="map-legend">
            <span><i class="lg lg1"></i>高</span>
            <span><i class="lg lg2"></i>中</span>
            <span><i class="lg lg3"></i>低</span>
          </div>
        </div>

        <section class="panel ticker-panel">
          <div class="panel-hd"><span>实时威胁事件</span><em class="hd-tip">最近 10 条</em></div>
          <div class="ticker-hd">
            <span class="c1">时间</span><span class="c2">事件编号</span><span class="c3">攻击类型</span>
            <span class="c4">受害资产 / 标题</span><span class="c5">等级</span><span class="c6">状态</span>
          </div>
          <div class="ticker">
            <div v-for="(r, i) in recent" :key="r.id" class="ticker-row" :class="{ odd: i % 2 }" @click="go(r)">
              <span class="c1 mono">{{ r.foundTime }}</span>
              <span class="c2 mono">{{ r.eventNo }}</span>
              <span class="c3">{{ r.attackTypeName }}</span>
              <span class="c4">{{ r.title }}</span>
              <span class="c5"><i :class="'lv' + r.level">{{ r.levelName }}</i></span>
              <span class="c6">{{ r.statusName }}</span>
            </div>
            <div v-if="!recent.length" class="empty">暂无事件</div>
          </div>
        </section>
      </div>

      <!-- 右列 -->
      <div class="col right">
        <section class="panel">
          <div class="panel-hd"><span>威胁等级分布</span></div>
          <div ref="levelEl" class="chart h200"></div>
        </section>
        <section class="panel">
          <div class="panel-hd"><span>境外攻击来源 TOP</span></div>
          <div class="rank">
            <div v-for="(c, i) in byCountry" :key="c.name" class="rank-row">
              <span class="rk" :class="'rk' + (i + 1)">{{ i + 1 }}</span>
              <span class="rank-name">{{ c.name }}</span>
              <span class="rank-bar"><i :style="{ width: pct(c.value, maxCountry) + '%' }"></i></span>
              <span class="rank-val mono">{{ c.value }}</span>
            </div>
            <div v-if="!byCountry.length" class="empty">暂无境外攻击</div>
          </div>
        </section>
        <section class="panel">
          <div class="panel-hd"><span>攻击源 IP TOP10</span></div>
          <div class="rank">
            <div v-for="(p, i) in topIps" :key="p.name" class="rank-row">
              <span class="rk" :class="'rk' + (i + 1)">{{ i + 1 }}</span>
              <span class="rank-name mono">{{ p.name }}</span>
              <span class="rank-bar"><i class="red" :style="{ width: pct(p.value, maxIp) + '%' }"></i></span>
              <span class="rank-val mono">{{ p.value }}</span>
            </div>
            <div v-if="!topIps.length" class="empty">暂无数据</div>
          </div>
        </section>
        <section class="panel">
          <div class="panel-hd"><span>攻击方向 / 结果</span></div>
          <div ref="mixEl" class="chart h180"></div>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import chinaJson from '@/assets/china.json'
import { statDashboard } from '@/api/event'

const router = useRouter()
const screenEl = ref<HTMLElement>()
const trendEl = ref<HTMLElement>()
const categoryEl = ref<HTMLElement>()
const levelEl = ref<HTMLElement>()
const mapEl = ref<HTMLElement>()
const mixEl = ref<HTMLElement>()

const kpi = ref<any>({ total: 0, today: 0, high: 0, pending: 0, archived: 0 })
const recent = ref<any[]>([])
const topAssets = ref<any[]>([])
const topIps = ref<any[]>([])
const byCountry = ref<any[]>([])
const now = ref('')

const charts: Record<string, any> = {}
let timer: any = null
let clock: any = null

const TXT = '#7fb0dd'
const AXIS = 'rgba(64,124,180,.35)'
const PALETTE = ['#2f9cff', '#22d3a6', '#ffc53d', '#ff5f6d', '#a78bfa', '#3ddc97', '#f97316', '#38bdf8']
const tip = { backgroundColor: 'rgba(6,20,38,.94)', borderColor: '#1f4a75', textStyle: { color: '#cfe4ff', fontSize: 12 } }

const shortName = (n: string) =>
  (n || '').replace(/(维吾尔自治区|壮族自治区|回族自治区|特别行政区|自治区|省|市)$/, '')

const maxAsset = computed(() => Math.max(1, ...topAssets.value.map((x) => x.value)))
const maxIp = computed(() => Math.max(1, ...topIps.value.map((x) => x.value)))
const maxCountry = computed(() => Math.max(1, ...byCountry.value.map((x) => x.value)))
const pct = (v: number, m: number) => Math.max(6, Math.round((v / m) * 100))

const t = () => {
  const d = new Date()
  const p = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`
}

/* ---------- 地图准备：省名归一 + 中心点 ---------- */
let mapCenters: Record<string, [number, number]> = {}
const prepareMap = () => {
  const geo: any = JSON.parse(JSON.stringify(chinaJson))
  mapCenters = {}
  geo.features.forEach((f: any) => {
    const raw = f.properties?.name || ''
    const cp = f.properties?.centroid || f.properties?.center
    if (cp && cp.length === 2) mapCenters[shortName(raw)] = [cp[0], cp[1]]
    f.properties.name = shortName(raw)
  })
  echarts.registerMap('bluedChina', geo)
}

/* ---------- 各图表 ---------- */
const drawTrend = (trend: any[]) => {
  const c = (charts.trend ||= echarts.init(trendEl.value!))
  c.setOption({
    grid: { left: 38, right: 14, top: 18, bottom: 26 },
    tooltip: { ...tip, trigger: 'axis' },
    xAxis: {
      type: 'category', data: trend.map((d) => d.date), boundaryGap: false,
      axisLabel: { color: TXT, fontSize: 10 }, axisLine: { lineStyle: { color: AXIS } }
    },
    yAxis: {
      type: 'value', axisLabel: { color: TXT, fontSize: 10 },
      splitLine: { lineStyle: { color: AXIS } }
    },
    series: [{
      type: 'line', smooth: true, symbol: 'circle', symbolSize: 5,
      data: trend.map((d) => d.count),
      lineStyle: { width: 2, color: '#22d3a6' }, itemStyle: { color: '#22d3a6' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(34,211,166,.42)' }, { offset: 1, color: 'rgba(34,211,166,0)' }
        ])
      }
    }, {
      type: 'bar', barWidth: 8, data: trend.map((d) => d.count),
      itemStyle: {
        borderRadius: [2, 2, 0, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(47,156,255,.75)' }, { offset: 1, color: 'rgba(47,156,255,.05)' }
        ])
      }
    }]
  })
}

const donut = (key: string, el: HTMLElement, data: any[], colors = PALETTE) => {
  const c = (charts[key] ||= echarts.init(el))
  c.setOption({
    color: colors,
    tooltip: { ...tip, trigger: 'item', formatter: '{b}<br/>{c} 起（{d}%）' },
    legend: { bottom: 0, textStyle: { color: TXT, fontSize: 11 }, itemWidth: 8, itemHeight: 8 },
    series: [{
      type: 'pie', radius: ['46%', '68%'], center: ['50%', '43%'],
      itemStyle: { borderColor: '#061424', borderWidth: 2 },
      label: { color: TXT, fontSize: 11, formatter: '{b} {d}%' },
      labelLine: { length: 8, length2: 8, lineStyle: { color: AXIS } },
      data
    }]
  })
}

const drawMix = (dir: any[], result: any[]) => {
  const c = (charts.mix ||= echarts.init(mixEl.value!))
  const names = dir.map((d) => d.name)
  const dirData = dir.map((d) => d.value)
  const resData = names.map((n) => (result.find((r) => r.name === n) || {}).value || 0)
  c.setOption({
    tooltip: { ...tip, trigger: 'axis' },
    legend: { top: 0, textStyle: { color: TXT, fontSize: 10 }, itemWidth: 8, itemHeight: 8 },
    grid: { left: 34, right: 10, top: 28, bottom: 22 },
    xAxis: {
      type: 'category', data: names,
      axisLabel: { color: TXT, fontSize: 10, interval: 0 }, axisLine: { lineStyle: { color: AXIS } }
    },
    yAxis: {
      type: 'value', axisLabel: { color: TXT, fontSize: 10 }, splitLine: { lineStyle: { color: AXIS } }
    },
    series: [
      { name: '事件数', type: 'bar', barWidth: 12, data: dirData,
        itemStyle: { borderRadius: [2, 2, 0, 0], color: '#2f9cff' } },
      { name: '参照', type: 'bar', barWidth: 12, data: resData,
        itemStyle: { borderRadius: [2, 2, 0, 0], color: 'rgba(255,197,61,.65)' } }
    ]
  })
}

const drawMap = (byRegion: any[]) => {
  if (!mapEl.value) return
  const c = (charts.map ||= echarts.init(mapEl.value))
  const values = byRegion.map((r) => r.value)
  const maxV = Math.max(1, ...values)
  const points = byRegion
    .filter((r) => mapCenters[r.name])
    .map((r) => ({ name: r.name, value: [...mapCenters[r.name], r.value] }))

  c.setOption({
    tooltip: {
      ...tip, trigger: 'item',
      formatter: (p: any) => {
        const v = Array.isArray(p.value) ? p.value[2] : p.value
        return `${p.name}<br/>事件 ${v == null || isNaN(v) ? 0 : v} 起`
      }
    },
    visualMap: {
      show: false, min: 0, max: maxV, inRange: { color: ['#0b2c4d', '#12558c', '#c1443a', '#ff5f6d'] }
    },
    geo: {
      map: 'bluedChina', roam: false, zoom: 1.16, top: 24, bottom: 12,
      itemStyle: {
        areaColor: '#0a2338', borderColor: 'rgba(47,156,255,.55)', borderWidth: 1,
        shadowColor: 'rgba(47,156,255,.45)', shadowBlur: 18
      },
      emphasis: {
        itemStyle: { areaColor: '#173f66' },
        label: { show: true, color: '#eaf6ff', fontSize: 11 }
      },
      label: { show: false }
    },
    series: [
      {
        type: 'map', geoIndex: 0, data: byRegion.map((r) => ({ name: r.name, value: r.value }))
      },
      {
        name: '攻击源', type: 'effectScatter', coordinateSystem: 'geo', zlevel: 2,
        symbolSize: (val: any) => 6 + Math.min(18, (val[2] / maxV) * 18),
        rippleEffect: { brushType: 'stroke', scale: 3.6, period: 3.4 },
        itemStyle: { color: '#ff5f6d', shadowBlur: 12, shadowColor: '#ff5f6d' },
        label: {
          show: true, position: 'right', formatter: (p: any) => p.name,
          color: '#ffd7d8', fontSize: 10
        },
        data: points
      }
    ]
  }, true)
}

const load = async () => {
  const s: any = await statDashboard()
  kpi.value = { total: s.total, today: s.today, high: s.high, pending: s.pending, archived: s.archived }
  recent.value = s.recent || []
  topAssets.value = (s.topAssets || []).slice(0, 5)
  topIps.value = (s.topAttackIps || []).slice(0, 10)
  byCountry.value = (s.byCountry || []).slice(0, 5)

  await nextTick()
  drawTrend(s.trend || [])
  donut('category', categoryEl.value!, (s.byCategory || []).slice(0, 6))
  donut('level', levelEl.value!, s.byLevel || [], ['#ff5f6d', '#ffc53d', '#2f9cff'])
  drawMix(s.byDirection || [], s.byResult || [])
  drawMap(s.byRegion || [])
}

const go = (r: any) => router.push('/events/' + r.id)
const go2 = (p: string) => router.push(p)
const fullscreen = () => {
  const el = screenEl.value
  if (!el) return
  if (document.fullscreenElement) document.exitFullscreen()
  else el.requestFullscreen?.()
}
const onResize = () => Object.values(charts).forEach((c) => c.resize())

onMounted(async () => {
  now.value = t()
  clock = setInterval(() => { now.value = t() }, 1000)
  prepareMap()
  await load()
  window.addEventListener('resize', onResize)
  timer = setInterval(load, 20000)
})

onBeforeUnmount(() => {
  clearInterval(clock)
  clearInterval(timer)
  window.removeEventListener('resize', onResize)
  Object.values(charts).forEach((c) => c.dispose())
})
</script>

<style scoped>
.screen {
  position: relative;
  min-height: 100vh;
  padding: 10px 14px 14px;
  background:
    radial-gradient(circle at 50% -10%, rgba(47, 156, 255, .22) 0%, transparent 55%),
    linear-gradient(180deg, #05121f 0%, #04101a 45%, #030c15 100%);
  color: #cfe4ff;
  overflow: hidden;
}
.screen::before {
  content: ''; position: absolute; inset: 0; pointer-events: none;
  background-image: linear-gradient(rgba(47, 156, 255, .045) 1px, transparent 1px),
    linear-gradient(90deg, rgba(47, 156, 255, .045) 1px, transparent 1px);
  background-size: 44px 44px;
}
/* 顶栏 */
.top {
  position: relative; display: flex; align-items: center; justify-content: space-between;
  height: 52px; margin-bottom: 8px; padding: 0 10px;
  background: linear-gradient(180deg, rgba(24, 62, 100, .55), rgba(6, 20, 34, .15));
  border-bottom: 1px solid rgba(47, 156, 255, .35);
}
.top::after {
  content: ''; position: absolute; left: 0; right: 0; bottom: -1px; height: 2px;
  background: linear-gradient(90deg, transparent, #2f9cff, transparent);
  opacity: .8;
}
.top-left { display: flex; align-items: baseline; gap: 8px; }
.brand-mark { font-size: 20px; font-weight: 800; letter-spacing: 3px;
  background: linear-gradient(180deg, #eaf6ff, #4aa8ff); -webkit-background-clip: text;
  background-clip: text; color: transparent; }
.brand-sub { font-size: 9px; color: #3f6f9c; letter-spacing: 2px; }
.top-title {
  position: absolute; left: 50%; transform: translateX(-50%);
  font-size: 24px; font-weight: 800; letter-spacing: 6px;
  background: linear-gradient(180deg, #ffffff, #3f9dff 85%); -webkit-background-clip: text;
  background-clip: text; color: transparent; text-shadow: 0 0 22px rgba(47, 156, 255, .45);
}
.top-right { display: flex; align-items: center; gap: 14px; }
.clock { font-size: 14px; color: #6fa9dc; letter-spacing: 1px; }
.live { font-size: 12px; color: #22d3a6; display: flex; align-items: center; gap: 6px; }
.live .dot { width: 7px; height: 7px; border-radius: 50%; background: #22d3a6;
  box-shadow: 0 0 10px #22d3a6; animation: blink 1.6s infinite; }
@keyframes blink { 0%, 100% { opacity: 1 } 50% { opacity: .25 } }
.fs-btn { color: #7fb0dd !important; }
.quick-nav { display: flex; gap: 6px; margin-right: 4px; }
.quick-nav a {
  font-size: 12px; color: #9dc6ea; padding: 4px 10px; border-radius: 3px; cursor: pointer;
  border: 1px solid rgba(47, 156, 255, .32); background: rgba(47, 156, 255, .08);
  transition: all .18s;
}
.quick-nav a:hover { color: #fff; background: rgba(47, 156, 255, .35); border-color: #2f9cff; }
.kpi-item.link { cursor: pointer; }
.kpi-item.link:hover .kpi-label { color: #eaf6ff; }
/* 三列 */
.stage { display: grid; grid-template-columns: 23% 1fr 23%; gap: 10px; height: calc(100vh - 84px); }
.col { display: flex; flex-direction: column; gap: 10px; min-height: 0; }
.center { min-width: 0; }
/* 面板 */
.panel {
  position: relative; flex: 1 1 0; min-height: 0;
  background: linear-gradient(180deg, rgba(15, 42, 70, .72), rgba(6, 20, 34, .82));
  border: 1px solid rgba(47, 156, 255, .22);
  border-radius: 4px; padding: 6px 10px 4px;
}
.panel::before, .panel::after {
  content: ''; position: absolute; width: 12px; height: 12px; border-color: #2f9cff; border-style: solid;
}
.panel::before { top: -1px; left: -1px; border-width: 2px 0 0 2px; }
.panel::after { bottom: -1px; right: -1px; border-width: 0 2px 2px 0; }
.panel-hd {
  display: flex; align-items: center; justify-content: space-between;
  font-size: 13px; font-weight: 600; color: #a9cdee; letter-spacing: 1px;
  padding-left: 8px; border-left: 3px solid #2f9cff; margin-bottom: 2px;
}
.hd-tip { font-size: 10px; color: #4f7ba6; font-style: normal; font-weight: 400; }
.chart { width: 100%; }
.h210 { height: 210px } .h200 { height: 200px } .h180 { height: 168px }
/* 排行 */
.rank { padding: 4px 2px; }
.rank-row { display: flex; align-items: center; gap: 8px; padding: 4px 2px; font-size: 12px; color: #a9cdee; }
.rk { width: 16px; height: 16px; line-height: 16px; text-align: center; border-radius: 2px;
  font-size: 10px; background: rgba(47, 156, 255, .18); color: #7fb0dd; }
.rk1 { background: rgba(255, 95, 109, .85); color: #fff }
.rk2 { background: rgba(255, 197, 61, .8); color: #2a1c00 }
.rk3 { background: rgba(47, 156, 255, .8); color: #fff }
.rank-name { flex: 0 0 86px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.rank-bar { flex: 1; height: 6px; background: rgba(47, 156, 255, .12); border-radius: 3px; overflow: hidden; }
.rank-bar i { display: block; height: 100%; border-radius: 3px;
  background: linear-gradient(90deg, rgba(47, 156, 255, .3), #2f9cff); }
.rank-bar i.red { background: linear-gradient(90deg, rgba(255, 95, 109, .3), #ff5f6d); }
.rank-val { flex: 0 0 30px; text-align: right; color: #dbeaff; }
/* 中间 */
.center-kpi {
  display: grid; grid-template-columns: repeat(5, 1fr); gap: 8px; flex: 0 0 64px;
  background: linear-gradient(180deg, rgba(15, 42, 70, .55), rgba(6, 20, 34, .35));
  border: 1px solid rgba(47, 156, 255, .18); border-radius: 4px; padding: 8px 6px;
}
.kpi-item { display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 2px; }
.kpi-ico { font-size: 11px; color: #2f9cff; }
.kpi-ico.up { color: #22d3a6 } .kpi-ico.warn { color: #ff5f6d }
.kpi-label { font-size: 11px; color: #6f9ac4; }
.kpi-num { font-size: 22px; font-weight: 700; color: #eaf6ff; line-height: 1.1; letter-spacing: 1px; }
.c-cyan { color: #22d3a6 } .c-red { color: #ff5f6d } .c-yellow { color: #ffc53d } .c-green { color: #3ddc97 }
.map-wrap { position: relative; flex: 1 1 auto; min-height: 0; }
.map { width: 100%; height: 100%; min-height: 320px; }
.map-legend { position: absolute; left: 10px; bottom: 8px; display: flex; gap: 10px; font-size: 11px; color: #7fb0dd; }
.lg { display: inline-block; width: 10px; height: 10px; border-radius: 2px; margin-right: 3px; }
.lg1 { background: #ff5f6d } .lg2 { background: #c1443a } .lg3 { background: #12558c }
/* 实时流 */
.ticker-panel { flex: 0 0 214px; }
.ticker-hd, .ticker-row { display: flex; align-items: center; gap: 8px; font-size: 12px; }
.ticker-hd { color: #4f7ba6; padding: 4px 6px; border-bottom: 1px solid rgba(47, 156, 255, .16); }
.ticker { max-height: 158px; overflow: hidden; }
.ticker-row { padding: 4px 6px; color: #a9cdee; border-radius: 3px; cursor: pointer; }
.ticker-row.odd { background: rgba(47, 156, 255, .06); }
.ticker-row:hover { background: rgba(47, 156, 255, .18); color: #eaf6ff; }
.c1 { width: 110px; color: #5d8bb5 }
.c2 { width: 152px; color: #4aa8ff }
.c3 { width: 108px }
.c4 { flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap }
.c5 { width: 44px; text-align: center }
.c6 { width: 74px; text-align: right; color: #22d3a6 }
.lv1 { color: #ff5f6d } .lv2 { color: #ffc53d } .lv3 { color: #3ddc97 }
.empty { text-align: center; color: #3f6f9c; font-size: 12px; padding: 18px 0; }
</style>
