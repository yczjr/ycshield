<template>
  <div>
    <div class="page-card">
      <div class="page-title">事件主信息</div>
      <el-form :model="form" label-width="112px" class="fix-label">
        <el-row :gutter="18">
          <el-col :span="12">
            <el-form-item label="事件标题">
              <el-input v-model="form.title" placeholder="留空自动生成" maxlength="100" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="发现时间" required>
              <el-date-picker v-model="form.foundTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss"
                              style="width:100%" placeholder="首次发现时间" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="事件来源">
              <el-select v-model="form.eventSource" style="width:100%">
                <el-option v-for="d in dict.event_source || []" :key="d.code" :label="d.name" :value="d.code" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="攻击类型" required>
              <el-cascader v-model="typePath" :options="typeTree" :props="{ checkStrictly: false }"
                           placeholder="先选大类再选子类" style="width:100%" @change="onType" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="攻击方向" required>
              <el-select v-model="form.attackDirection" style="width:100%">
                <el-option v-for="d in dict.attack_direction || []" :key="d.code" :label="d.name" :value="d.code" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="攻击阶段">
              <el-select v-model="form.attackPhase" clearable style="width:100%">
                <el-option v-for="d in dict.attack_phase || []" :key="d.code" :label="d.name" :value="d.code" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="攻击成功与否" required>
              <el-radio-group v-model="form.attackResult">
                <el-radio-button v-for="d in dict.attack_result || []" :key="d.code" :value="d.code">
                  {{ d.name }}
                </el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="危害等级" required>
              <el-radio-group v-model="form.level">
                <el-radio-button :value="1">高危</el-radio-button>
                <el-radio-button :value="2">中危</el-radio-button>
                <el-radio-button :value="3">低危</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="攻击 IP" required>
              <el-input v-model="attackIpsText" type="textarea" :rows="2" placeholder="多个用逗号或换行分隔" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="初步研判结论" required>
              <el-input v-model="form.preliminaryConclusion" type="textarea" :rows="3" maxlength="2000"
                        show-word-limit placeholder="看到什么现象、判断是什么攻击、判成功/失败的依据、建议怎么处置" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </div>

    <div class="page-card">
      <div class="page-title">受害信息</div>
      <el-form label-width="112px" class="fix-label">
        <el-row :gutter="18">
          <el-col :span="12">
            <el-form-item label="受害 IP" required>
              <el-input v-model="victimIpsText" type="textarea" :rows="2"
                        placeholder="可多个；填写告警记录后会自动汇总去重" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="受害资产名称" required>
              <el-input v-model="form.assetName" placeholder="如：门户网站、OA 系统（仅作文本记录）" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="影响状态">
              <el-select v-model="form.impactStatus" clearable style="width:100%">
                <el-option v-for="d in dict.impact_status || []" :key="d.code" :label="d.name" :value="d.code" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="标记重要">
              <el-switch v-model="important" active-text="重要事件" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="业务影响">
              <el-input v-model="form.businessImpact" type="textarea" :rows="2" maxlength="1000" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </div>

    <div class="page-card">
      <div class="head-row">
        <div class="page-title" style="margin:0">告警记录（一条事件可挂多条告警）</div>
        <el-button type="primary" plain size="small" @click="addAlarm">+ 添加告警</el-button>
      </div>

      <el-card v-for="(a, i) in form.alarms" :key="i" shadow="never" class="alarm-card">
        <template #header>
          <div class="head-row" style="margin-bottom:0">
            <span>告警 #{{ i + 1 }}</span>
            <el-button v-if="form.alarms.length > 1" link type="danger" @click="form.alarms.splice(i, 1)">删除</el-button>
          </div>
        </template>
        <el-form label-width="92px" class="fix-label">
          <el-row :gutter="18">
            <el-col :span="8">
              <el-form-item label="来源设备" required>
                <el-select v-model="a.deviceType" style="width:100%" filterable placeholder="选择设备类型">
                  <el-option v-for="d in dict.device_type || []" :key="d.code" :label="d.name" :value="d.code" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="设备名称" required>
                <el-select v-model="a.deviceName" style="width:100%" filterable allow-create default-first-option
                           placeholder="管理员预设，可直接选">
                  <el-option v-for="d in dict.device_name || []" :key="d.code" :label="d.name" :value="d.name" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="告警时间" required>
                <el-date-picker v-model="a.alarmTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss"
                                style="width:100%" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="告警名称">
                <el-input v-model="a.alarmName" placeholder="设备规则名" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="源IP">
                <el-input v-model="a.srcIpsText" placeholder="多个用逗号分隔" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="受害IP">
                <el-input v-model="a.victimIpsText" placeholder="多个用逗号分隔" />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="原始报文">
                <el-input v-model="a.rawText" type="textarea" :rows="3" placeholder="粘贴设备告警原文（JSON/文本）" />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </el-card>
    </div>

    <div class="page-card" @paste="onPaste" tabindex="0">
      <div class="page-title">证据附件</div>
      <div class="tip">支持点击选择文件；也可直接在本区域按 <b>Ctrl+V</b> 粘贴截图。</div>
      <el-upload :auto-upload="false" :show-file-list="false" multiple @change="onFileChange">
        <el-button plain>选择文件</el-button>
      </el-upload>
      <div class="thumbs">
        <div v-for="(f, i) in pending" :key="i" class="thumb">
          <img v-if="f.url" :src="f.url" />
          <div v-else class="fname">{{ f.file.name }}</div>
          <el-button link type="danger" size="small" @click="pending.splice(i, 1)">移除</el-button>
        </div>
      </div>
    </div>

    <div class="page-card foot">
      <span class="tip" style="margin-right:12px">提交后进入研判台，由分析员认领研判</span>
      <el-button @click="$router.back()">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="submit">提交事件</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createEvent, checkDuplicate, uploadFile } from '@/api/event'
import { getAllDict, getAttackTypeTree } from '@/api/dict'

const router = useRouter()
const dict = ref<any>({})
const typeTree = ref<any[]>([])
const typePath = ref<any>([])
const important = ref(false)
const submitting = ref(false)
const attackIpsText = ref('')
const victimIpsText = ref('')
const pending = ref<any[]>([])

const now = () => {
  const d = new Date()
  const p = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`
}

const form = reactive<any>({
  title: '', foundTime: now(), eventSource: 'MONITOR',
  attackTypeCategory: '', attackType: '', attackPhase: '',
  attackDirection: 'OUT2IN', attackResult: '', level: 2,
  preliminaryConclusion: '', assetName: '', impactStatus: '', businessImpact: '',
  isImportant: 0,
  alarms: [newAlarm()]
})

function newAlarm() {
  return { deviceType: '', deviceName: '', alarmTime: now(), alarmName: '', direction: '',
    srcIpsText: '', victimIpsText: '', rawText: '' }
}

const addAlarm = () => form.alarms.push(newAlarm())
const onType = (v: any) => {
  form.attackTypeCategory = v?.[0] || ''
  form.attackType = v?.[1] || ''
}

const onFileChange = (f: any) => {
  const file = f.raw || f
  pending.value.push({ file, url: file.type?.startsWith('image') ? URL.createObjectURL(file) : '' })
}
const onPaste = (e: ClipboardEvent) => {
  const items = e.clipboardData?.items || []
  for (const it of items as any) {
    if (it.type && it.type.startsWith('image/')) {
      const file = it.getAsFile()
      if (file) {
        pending.value.push({ file, url: URL.createObjectURL(file), paste: true })
        ElMessage.success('已粘贴截图')
      }
    }
  }
}

const splitIps = (s: string) =>
  (s || '').split(/[,，\s]+/).map((x) => x.trim()).filter(Boolean)

const submit = async () => {
  if (!form.foundTime) return ElMessage.warning('请填写发现时间')
  if (!form.attackTypeCategory || !form.attackType) return ElMessage.warning('请选择攻击类型')
  if (!form.attackResult) return ElMessage.warning('请选择攻击成功与否')
  if (!form.preliminaryConclusion) return ElMessage.warning('请填写初步研判结论')
  if (!form.alarms.length || !form.alarms[0].deviceType) return ElMessage.warning('请填写来源设备')
  if (!form.alarms[0].deviceName) return ElMessage.warning('请选择设备名称')

  const attackIps = splitIps(attackIpsText.value)
  const victimIps = splitIps(victimIpsText.value)
  form.alarms.forEach((a: any) => victimIps.push(...splitIps(a.victimIpsText)))

  const dup: any = await checkDuplicate({ attackIps, victimIps, foundTime: form.foundTime })
  if (dup && dup.length) {
    const text = dup.map((d: any) => `${d.eventNo}（${d.statusName}）${d.title}`).join('\n')
    try {
      await ElMessageBox.confirm('发现疑似重复事件：\n' + text + '\n\n是否仍要提交？', '重复提示', { type: 'warning' })
    } catch { return }
  }

  submitting.value = true
  try {
    const payload: any = {
      ...form,
      attackIps,
      victimIps,
      isImportant: important.value ? 1 : 0,
      alarms: form.alarms.map((a: any) => ({
        deviceType: a.deviceType, deviceName: a.deviceName, alarmTime: a.alarmTime,
        alarmName: a.alarmName, direction: a.direction || form.attackDirection,
        srcIps: splitIps(a.srcIpsText), victimIps: splitIps(a.victimIpsText), rawText: a.rawText
      }))
    }
    const detail: any = await createEvent(payload)
    for (const p of pending.value) {
      try { await uploadFile(p.file, detail.main.id) } catch { /* 单张失败不影响 */ }
    }
    ElMessage.success('事件已提交：' + detail.main.eventNo)
    router.push('/events/' + detail.main.id)
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  dict.value = await getAllDict()
  const tree: any = await getAttackTypeTree()
  typeTree.value = tree.map((c: any) => ({
    value: c.code, label: c.name,
    children: (c.children || []).map((s: any) => ({ value: s.code, label: s.name }))
  }))
})
</script>

<style scoped>
.fix-label :deep(.el-form-item__label) { white-space: nowrap; }
.head-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.alarm-card { margin-bottom: 14px; border-radius: 6px; }
.tip { font-size: 12px; color: #909399; margin-bottom: 8px; }
.thumbs { display: flex; flex-wrap: wrap; gap: 10px; margin-top: 10px; }
.thumb { width: 130px; text-align: center; }
.thumb img { width: 130px; height: 90px; object-fit: cover; border: 1px solid #e4e7ed; border-radius: 4px; }
.fname { font-size: 12px; padding: 30px 4px; border: 1px dashed #dcdfe6; border-radius: 4px; word-break: break-all; }
.foot { display: flex; justify-content: flex-end; align-items: center; }
</style>
