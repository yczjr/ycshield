<template>
  <div v-if="d">
    <div class="page-card">
      <div class="head">
        <div>
          <div class="no-row">
            <span class="mono no">{{ d.main.eventNo }}</span>
            <el-tag :type="levelTag" size="small">{{ levelName }}</el-tag>
            <el-tag :type="statusTag(d.main.status)" size="small">{{ d.statusNames[d.main.status] }}</el-tag>
            <el-tag v-if="d.main.isTimeout === 1" type="danger" size="small" effect="dark">超时</el-tag>
            <el-tag v-if="d.main.isImportant === 1" type="warning" size="small">重要</el-tag>
          </div>
          <div class="title">{{ d.main.title }}</div>
        </div>
        <div class="actions">
          <el-button v-if="canClaim" type="primary" @click="claim">认领研判</el-button>
          <el-button v-if="canAnalyze" type="primary" plain @click="openAnalyze">研判结论</el-button>
          <el-button v-if="canBlock" type="danger" plain @click="openBlock">直接封禁IP结案</el-button>
          <el-button v-if="canFinish" type="success" @click="finishDlg = true">研判办结归档</el-button>
          <el-button v-if="canAssign" type="warning" plain @click="openAssign">转应急处置</el-button>
          <el-button v-if="canDispose" type="warning" @click="disposeDlg = true">登记处置</el-button>
          <el-button v-if="canSubmitReview" type="success" plain @click="submitReview">提交复核</el-button>
          <el-button v-if="canReview" type="success" plain @click="reviewDlg = true">复核</el-button>
          <el-button v-if="canClose" type="info" plain @click="closeEventDlg = true">误报/重复关闭</el-button>
          <el-button @click="$router.push('/events')">返回</el-button>
        </div>
      </div>
      <div class="stage-bar">
        <div v-for="s in stages" :key="s.name" class="stage"
             :class="{ done: stageIndex > s.idx, now: stageIndex === s.idx, skip: s.skip }">
          <span class="dot"></span><span>{{ s.label }}</span>
        </div>
      </div>
    </div>

    <div class="page-card">
      <div class="page-title">事件信息</div>
      <el-descriptions :column="3" border size="small">
        <el-descriptions-item label="攻击类型">
          {{ d.typeNames[d.main.attackType] }}
          <span class="muted">（{{ d.categoryNames[d.main.attackTypeCategory] }}）</span>
        </el-descriptions-item>
        <el-descriptions-item label="攻击方向">{{ d.directionNames[d.main.attackDirection] }}</el-descriptions-item>
        <el-descriptions-item label="攻击阶段">{{ d.main.attackPhase ? d.dictOf('attack_phase', d.main.attackPhase) : '-' }}</el-descriptions-item>
        <el-descriptions-item label="攻击 IP"><span class="mono">{{ d.main.attackIps || '-' }}</span></el-descriptions-item>
        <el-descriptions-item label="受害 IP"><span class="mono">{{ d.main.victimIps || '-' }}</span></el-descriptions-item>
        <el-descriptions-item label="受害资产">{{ d.main.assetName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="攻击成功与否">{{ d.resultNames[d.main.attackResult] }}</el-descriptions-item>
        <el-descriptions-item label="影响状态">{{ d.main.impactStatus ? d.dictOf('impact_status', d.main.impactStatus) : '-' }}</el-descriptions-item>
        <el-descriptions-item label="事件来源">{{ d.main.eventSource }}</el-descriptions-item>
        <el-descriptions-item label="发现时间">{{ d.main.foundTime }}</el-descriptions-item>
        <el-descriptions-item label="上报人">{{ d.main.reporterName }}</el-descriptions-item>
        <el-descriptions-item label="研判 / 处置人">{{ d.main.ownerName || '-' }} / {{ d.main.handlerName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="业务影响" :span="3">{{ d.main.businessImpact || '-' }}</el-descriptions-item>
      </el-descriptions>
    </div>

    <el-row :gutter="12">
      <el-col :span="12">
        <div class="page-card">
          <div class="page-title">监测初判（监测员）</div>
          <div class="concl">
            <div class="row"><b>攻击成功与否：</b>{{ d.resultNames[d.main.attackResult] }}</div>
            <div class="row" v-if="d.main.resultModified === 1">
              <el-tag size="small" type="warning">分析员已修正</el-tag>
              <span class="reason">{{ d.main.modifyReason }}</span>
            </div>
            <div class="body">{{ d.main.preliminaryConclusion }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="page-card">
          <div class="page-title">分析研判（分析员）</div>
          <div class="concl">
            <div class="body">{{ d.main.analysisConclusion || '尚未研判' }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <div class="page-card">
      <div class="page-title">告警记录（{{ d.alarms.length }} 条）</div>
      <el-table :data="d.alarms" size="small" border>
        <el-table-column prop="seq" label="#" width="50" align="center" />
        <el-table-column label="来源设备" width="160">
          <template #default="{ row }">
            {{ d.deviceNames[row.deviceType] }}<span v-if="row.deviceName"> · {{ row.deviceName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="alarmTime" label="告警时间" width="160" />
        <el-table-column prop="alarmName" label="告警名称" width="170" show-overflow-tooltip />
        <el-table-column label="方向" width="100">
          <template #default="{ row }">{{ d.directionNames[row.direction] || '-' }}</template>
        </el-table-column>
        <el-table-column prop="srcIps" label="源IP" width="130" show-overflow-tooltip />
        <el-table-column prop="victimIps" label="受害IP" width="150" show-overflow-tooltip />
        <el-table-column label="原始报文">
          <template #default="{ row }">
            <el-popover v-if="row.rawText" width="520" trigger="click">
              <template #reference><el-button link type="primary">查看</el-button></template>
              <pre class="raw">{{ row.rawText }}</pre>
            </el-popover>
            <span v-else>-</span>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="page-card">
      <div class="page-title">处置记录（{{ d.disposals.length }} 次）</div>
      <el-table :data="d.disposals" size="small" border empty-text="暂无处置记录">
        <el-table-column label="处置动作" width="200">
          <template #default="{ row }">
            <el-tag v-for="a in row.actions.split(',')" :key="a" size="small" style="margin-right:4px">
              {{ d.disposalActionNames[a] || a }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="targets" label="处置对象" width="180" show-overflow-tooltip />
        <el-table-column label="执行设备" width="150">
          <template #default="{ row }">{{ d.execDeviceNames[row.execDevice] || row.execDevice }}</template>
        </el-table-column>
        <el-table-column prop="execTime" label="执行时间" width="160" />
        <el-table-column label="验证" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="row.verifyResult === 'VERIFIED' ? 'success' : row.verifyResult === 'FAILED' ? 'danger' : 'warning'">
              {{ d.verifyResultNames[row.verifyResult] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="说明" show-overflow-tooltip />
        <el-table-column prop="executorName" label="执行人" width="90" />
      </el-table>
    </div>

    <el-row :gutter="12">
      <el-col :span="14">
        <div class="page-card">
          <div class="page-title">流转时间线</div>
          <el-timeline style="padding-left:6px">
            <el-timeline-item v-for="f in d.flows" :key="f.id" :timestamp="f.operateTime" placement="top"
                              :type="f.action === 'MODIFY' ? 'warning' : 'primary'">
              <div>
                <b>{{ actionName(f.action) }}</b>
                <span class="muted"> · {{ f.operatorName }}</span>
                <span v-if="f.fromStatus && f.toStatus && f.fromStatus !== f.toStatus" class="muted">
                  （{{ d.statusNames[f.fromStatus] }} → {{ d.statusNames[f.toStatus] }}）
                </span>
              </div>
              <div v-if="f.fieldName" class="field-change">
                字段 {{ f.fieldName }}：<span class="old">{{ f.oldValue }}</span> → <span class="new">{{ f.newValue }}</span>
              </div>
              <div v-if="f.opinion" class="opinion">{{ f.opinion }}</div>
            </el-timeline-item>
          </el-timeline>
        </div>
      </el-col>
      <el-col :span="10">
        <div class="page-card">
          <div class="page-title">证据附件（{{ d.evidences.length }}）</div>
          <div v-if="!d.evidences.length" class="muted">暂无附件</div>
          <div v-for="e in d.evidences" :key="e.id" class="evi">
            <a :href="'/files/' + e.filePath" target="_blank">{{ e.fileName }}</a>
            <div class="evi-meta">{{ (e.fileSize / 1024).toFixed(0) }} KB · {{ e.md5 }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-dialog v-model="analyzeDlg" title="研判结论" width="560px">
      <el-form label-width="100px">
        <el-form-item label="攻击成功与否">
          <el-select v-model="analyzeForm.attackResult" style="width:100%">
            <el-option v-for="x in dict.attack_result || []" :key="x.code" :label="x.name" :value="x.code" />
          </el-select>
        </el-form-item>
        <el-form-item label="危害等级">
          <el-select v-model="analyzeForm.level" style="width:100%">
            <el-option label="高危" :value="1" /><el-option label="中危" :value="2" /><el-option label="低危" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="攻击方向">
          <el-select v-model="analyzeForm.attackDirection" style="width:100%">
            <el-option v-for="x in dict.attack_direction || []" :key="x.code" :label="x.name" :value="x.code" />
          </el-select>
        </el-form-item>
        <el-form-item label="修正理由">
          <el-input v-model="analyzeForm.modifyReason" placeholder="修改判定时说明依据" />
        </el-form-item>
        <el-form-item label="研判结论">
          <el-input v-model="analyzeForm.analysisConclusion" type="textarea" :rows="4"
                    placeholder="定性、影响范围、处置建议（可多次补充，不限字数）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="analyzeDlg = false">取消</el-button>
        <el-button type="primary" @click="doAnalyze">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="blockDlg" title="直接封禁 IP 结案" width="520px">
      <el-alert type="warning" :closable="false" show-icon style="margin-bottom:14px"
                title="本操作不接入设备，仅记录研判人的封禁决定，事件直接归档。" />
      <el-form label-width="90px">
        <el-form-item label="封禁对象" required>
          <el-input v-model="blockForm.targets" type="textarea" :rows="2" placeholder="IP，多个用逗号分隔" />
        </el-form-item>
        <el-form-item label="封禁说明">
          <el-input v-model="blockForm.opinion" type="textarea" :rows="3"
                    placeholder="如：确认为恶意扫描源，已提交防火墙人工封禁" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="blockDlg = false">取消</el-button>
        <el-button type="danger" @click="doBlock">确认封禁并结案</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="finishDlg" title="研判办结归档" width="480px">
      <el-alert type="info" :closable="false" show-icon style="margin-bottom:14px"
                title="研判后认为无需处置（如攻击未成功、一般扫描），直接归档结束。" />
      <el-input v-model="finishForm.opinion" type="textarea" :rows="3" placeholder="办结说明（可选）" />
      <template #footer>
        <el-button @click="finishDlg = false">取消</el-button>
        <el-button type="primary" @click="doFinish">确认办结</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="assignDlg" title="转应急处置（指派处置人）" width="460px">
      <el-form label-width="90px">
        <el-form-item label="处置人">
          <el-select v-model="assignForm.handlerId" style="width:100%">
            <el-option v-for="h in handlers" :key="h.id" :label="h.realName || h.username" :value="h.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="处置建议">
          <el-input v-model="assignForm.opinion" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignDlg = false">取消</el-button>
        <el-button type="primary" @click="doAssign">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="disposeDlg" title="登记处置（结构化）" width="620px">
      <el-form label-width="100px">
        <el-form-item label="处置动作">
          <el-select v-model="disposeForm.actions" multiple style="width:100%">
            <el-option v-for="x in dict.disposal_action || []" :key="x.code" :label="x.name" :value="x.code" />
          </el-select>
        </el-form-item>
        <el-form-item label="处置对象">
          <el-input v-model="disposeForm.targets" placeholder="IP / 域名 / URL / 账号 / 主机名，多个用逗号" />
        </el-form-item>
        <el-form-item label="执行设备">
          <el-select v-model="disposeForm.execDevice" style="width:100%">
            <el-option v-for="x in dict.exec_device || []" :key="x.code" :label="x.name" :value="x.code" />
          </el-select>
        </el-form-item>
        <el-form-item label="执行时间">
          <el-date-picker v-model="disposeForm.execTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" />
        </el-form-item>
        <el-form-item label="生效时间">
          <el-date-picker v-model="disposeForm.effectTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" />
        </el-form-item>
        <el-form-item label="验证结果">
          <el-select v-model="disposeForm.verifyResult" style="width:100%">
            <el-option v-for="x in dict.verify_result || []" :key="x.code" :label="x.name" :value="x.code" />
          </el-select>
        </el-form-item>
        <el-form-item label="结果说明">
          <el-input v-model="disposeForm.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="disposeDlg = false">取消</el-button>
        <el-button type="primary" @click="doDispose">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="reviewDlg" title="复核" width="480px">
      <el-form label-width="90px">
        <el-form-item label="复核结果">
          <el-radio-group v-model="reviewForm.pass">
            <el-radio :value="true">通过并归档</el-radio>
            <el-radio :value="false">驳回退回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="意见">
          <el-input v-model="reviewForm.opinion" type="textarea" :rows="3"
                    :placeholder="reviewForm.pass ? '复核意见（可选）' : '驳回原因（必填）'" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewDlg = false">取消</el-button>
        <el-button type="primary" @click="doReview">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="closeEventDlg" title="关闭事件（误报/重复）" width="460px">
      <el-input v-model="closeForm.reason" type="textarea" :rows="3" placeholder="关闭原因（必填）" />
      <template #footer>
        <el-button @click="closeEventDlg = false">取消</el-button>
        <el-button type="danger" @click="doClose">确认关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getEvent, claimEvent, analyzeEvent, assignEvent, disposeEvent, submitReview as submitReviewApi,
  reviewEvent, closeEvent, finishEvent, blockIpEvent } from '@/api/event'
import { getAllDict } from '@/api/dict'
import { listHandlers } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const user = useUserStore()
const dict = ref<any>({})
const raw = ref<any>(null)
const handlers = ref<any[]>([])
const analyzeDlg = ref(false)
const assignDlg = ref(false)
const disposeDlg = ref(false)
const reviewDlg = ref(false)
const closeEventDlg = ref(false)
const blockDlg = ref(false)
const finishDlg = ref(false)

const id = Number(route.params.id)

const analyzeForm = reactive<any>({ attackResult: '', level: 2, attackDirection: '', modifyReason: '', analysisConclusion: '' })
const assignForm = reactive<any>({ handlerId: null, opinion: '' })
const disposeForm = reactive<any>({ actions: [], targets: '', execDevice: '', execTime: '', effectTime: '', verifyResult: 'PENDING', remark: '' })
const reviewForm = reactive<any>({ pass: true, opinion: '' })
const closeForm = reactive<any>({ reason: '' })
const blockForm = reactive<any>({ targets: '', opinion: '' })
const finishForm = reactive<any>({ opinion: '' })

const d = computed<any>(() => {
  if (!raw.value) return null
  const x: any = { ...raw.value }
  x.dictOf = (type: string, code: string) => {
    const list = dict.value?.[type] || []
    const hit = list.find((i: any) => i.code === code)
    return hit ? hit.name : code
  }
  return x
})

const status = computed(() => raw.value?.main?.status)
const isAdmin = computed(() => user.roles.includes('ADMIN'))
const isOwner = computed(() => raw.value?.main?.ownerId === user.userId)
const isHandler = computed(() => raw.value?.main?.handlerId === user.userId)

const canClaim = computed(() => status.value === 'SUBMITTED' && (user.roles.includes('ANALYST') || isAdmin.value))
const canAnalyze = computed(() => ['ANALYZING', 'HANDLING'].includes(status.value || '') && (isOwner.value || isAdmin.value))
const canBlock = computed(() => canAnalyze.value && !!raw.value?.main?.analysisConclusion)
const canFinish = computed(() => canAnalyze.value && !!raw.value?.main?.analysisConclusion)
const canAssign = computed(() => canAnalyze.value && !!raw.value?.main?.analysisConclusion)
const canDispose = computed(() => status.value === 'HANDLING' && (isHandler.value || isAdmin.value))
const canSubmitReview = computed(() => canDispose.value && (raw.value?.disposals?.length > 0))
const canReview = computed(() => status.value === 'REVIEWING' && (user.roles.includes('ANALYST') || isAdmin.value))
const canClose = computed(() => ['SUBMITTED', 'ANALYZING'].includes(status.value || '')
  && (user.roles.includes('ANALYST') || isAdmin.value))

const levelName = computed(() => (raw.value?.levelNames?.[String(raw.value?.main?.level)] || ''))
const levelTag = computed(() => (raw.value?.main?.level === 1 ? 'danger' : raw.value?.main?.level === 2 ? 'warning' : 'primary'))
const statusTag = (v: string) => v === 'SUBMITTED' ? 'warning' : v === 'ANALYZING' ? 'primary'
  : v === 'HANDLING' ? 'danger' : v === 'REVIEWING' ? 'warning' : v === 'ARCHIVED' ? 'success' : 'info'

const stages = [
  { name: 'SUBMITTED', label: '监测上报', idx: 0 },
  { name: 'ANALYZING', label: '研判', idx: 1 },
  { name: 'HANDLING', label: '应急处置', idx: 2, skip: true },
  { name: 'REVIEWING', label: '复核', idx: 3, skip: true },
  { name: 'ARCHIVED', label: '归档', idx: 4 }
]
const stageIndex = computed(() => {
  const m: any = { SUBMITTED: 0, ANALYZING: 1, HANDLING: 2, REVIEWING: 3, ARCHIVED: 4, CLOSED: 4 }
  return m[status.value || ''] ?? 0
})

const actionName = (a: string) => ({
  CREATE: '提交上报', CLAIM: '认领研判', ANALYZE: '研判', ASSIGN: '转应急处置',
  DISPOSE: '登记处置', SUBMIT_REVIEW: '提交复核', ARCHIVE: '复核归档', REJECT: '驳回',
  CLOSE: '关闭事件', MODIFY: '字段修正', FINISH: '研判办结归档', BLOCK_CLOSE: '直接封禁结案'
} as any)[a] || a

const load = async () => {
  raw.value = await getEvent(id)
  if (raw.value) {
    analyzeForm.attackResult = raw.value.main.attackResult
    analyzeForm.level = raw.value.main.level
    analyzeForm.attackDirection = raw.value.main.attackDirection
    analyzeForm.analysisConclusion = raw.value.main.analysisConclusion || ''
    blockForm.targets = raw.value.main.attackIps || ''
  }
}

const claim = async () => { await claimEvent(id); ElMessage.success('已认领'); load() }
const openAnalyze = () => { analyzeDlg.value = true }
const doAnalyze = async () => {
  if (!analyzeForm.analysisConclusion) return ElMessage.warning('请填写研判结论')
  await analyzeEvent(id, analyzeForm)
  ElMessage.success('研判已保存')
  analyzeDlg.value = false
  load()
}
const openBlock = () => { blockForm.targets = raw.value?.main?.attackIps || ''; blockDlg.value = true }
const doBlock = async () => {
  if (!blockForm.targets) return ElMessage.warning('请填写封禁对象')
  await blockIpEvent(id, blockForm)
  ElMessage.success('已封禁并结案')
  blockDlg.value = false
  load()
}
const doFinish = async () => {
  await finishEvent(id, finishForm)
  ElMessage.success('已办结归档')
  finishDlg.value = false
  load()
}
const openAssign = async () => {
  handlers.value = await listHandlers()
  assignDlg.value = true
}
const doAssign = async () => {
  if (!assignForm.handlerId) return ElMessage.warning('请选择处置人')
  await assignEvent(id, assignForm)
  ElMessage.success('已转应急处置')
  assignDlg.value = false
  load()
}
const doDispose = async () => {
  if (!disposeForm.actions.length || !disposeForm.targets || !disposeForm.execDevice)
    return ElMessage.warning('请填写处置动作、处置对象和执行设备')
  await disposeEvent(id, disposeForm)
  ElMessage.success('处置已登记')
  disposeDlg.value = false
  load()
}
const submitReview = async () => { await submitReviewApi(id); ElMessage.success('已提交复核'); load() }
const doReview = async () => {
  await reviewEvent(id, reviewForm)
  ElMessage.success('复核完成')
  reviewDlg.value = false
  load()
}
const doClose = async () => {
  if (!closeForm.reason) return ElMessage.warning('请填写关闭原因')
  await closeEvent(id, closeForm)
  ElMessage.success('事件已关闭')
  closeEventDlg.value = false
  load()
}

onMounted(async () => {
  dict.value = await getAllDict()
  await user.loadMe()
  await load()
})
</script>

<style scoped>
.head { display: flex; justify-content: space-between; align-items: flex-start; gap: 12px; }
.no-row { display: flex; align-items: center; gap: 10px; }
.no { font-size: 16px; font-weight: 700; color: #0b3d91; }
.title { font-size: 17px; font-weight: 600; margin-top: 8px; }
.actions { display: flex; gap: 8px; flex-wrap: wrap; justify-content: flex-end; max-width: 620px; }
.stage-bar { display: flex; align-items: center; gap: 6px; margin-top: 16px; padding-top: 12px; border-top: 1px dashed #ebeef5; }
.stage { display: flex; align-items: center; gap: 6px; font-size: 12px; color: #a8b0ba; }
.stage .dot { width: 8px; height: 8px; border-radius: 50%; background: #d9dee6; }
.stage.done { color: #2e7d32; }
.stage.done .dot { background: #2e7d32; }
.stage.now { color: #0b3d91; font-weight: 600; }
.stage.now .dot { background: #0b3d91; box-shadow: 0 0 0 3px rgba(11,61,145,.15); }
.stage.skip { opacity: .55; }
.stage:not(:last-child)::after { content: '›'; margin-left: 6px; color: #d9dee6; }
.muted { color: #909399; }
.concl .row { font-size: 13px; margin-bottom: 6px; }
.concl .reason { color: #c62828; margin-left: 6px; font-size: 12px; }
.concl .body { background: #f7f9fc; border-left: 3px solid #1f5aa8; padding: 8px 10px;
  font-size: 13px; line-height: 1.7; white-space: pre-wrap; }
.raw { max-height: 300px; overflow: auto; font-size: 12px; white-space: pre-wrap; margin: 0; }
.field-change { font-size: 13px; color: #606266; margin-top: 2px; }
.field-change .old { color: #c62828; text-decoration: line-through; }
.field-change .new { color: #2e7d32; font-weight: 600; }
.opinion { color: #606266; font-size: 13px; margin-top: 2px; }
.evi { padding: 6px 0; border-bottom: 1px dashed #ebeef5; }
.evi-meta { font-size: 11px; color: #a0a7b0; font-family: Consolas, monospace; }
</style>
