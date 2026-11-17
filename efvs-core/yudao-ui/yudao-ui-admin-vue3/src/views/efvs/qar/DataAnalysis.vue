<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="900" :auto-fullscreen="true">
    <div class="data-analysis">
      <div class="page-header">
        <span class="title">数据分析 · 架次图表（LOC / GS）</span>
        <span v-if="qarIds.length" class="qar-id">架次: {{ qarIds.join(', ') }}</span>
        <el-button
          v-if="qarIds.length && !isRunning"
          type="primary"
          size="small"
          :disabled="status === 'CONNECTING'"
          :loading="status === 'CONNECTING'"
          @click="startStream"
        >
          {{ status === 'CONNECTING' ? '连接中...' : '开始传输' }}
        </el-button>
        <el-button v-if="isRunning" type="danger" size="small" @click="stopStream"
          >停止传输</el-button
        >
        <el-button
          v-if="isRunning"
          :type="isPaused ? 'success' : 'warning'"
          size="small"
          @click="togglePause"
        >
          {{ isPaused ? '继续拉取' : '暂停拉取' }}
        </el-button>
        <el-tag size="small" :type="isRunning ? 'success' : 'info'">{{
          isRunning ? `HTTP 分页加载（每架次每批 ${COMPARE_PAGE_SIZE} 条）` : '未开始'
        }}</el-tag>
        <span class="label">已接收: {{ totalCount }} 条</span>
        <span v-if="flightPhaseLabels.length" class="label">当前阶段: {{ flightPhaseLabels }}</span>
        <el-button
          v-if="qarIds.length"
          type="success"
          size="small"
          :loading="exportLoading"
          @click="exportToWord"
        >
          导出 Word
        </el-button>
      </div>
      <div v-if="!qarIds.length" class="empty-tip">
        请从列表页勾选一条或多条记录后点击「数据分析」，将带 qarids 进入本页；点击「开始传输」后通过
        <code>/efvs/qar/taxi-phase/compare-page</code> 按架次自动分页拉取（每批与 3D 复现
        taxi-phase/play 一致为 10 条原始记录），逐步绘制 LOC/GS，多架次用不同颜色同时绘制。
      </div>
      <template v-else>
        <div v-if="error" class="error-tip">{{ error }}</div>
        <div class="charts-wrap">
          <div class="chart-block">
            <div class="chart-title">LOC 航向道偏差（滑跑）</div>
            <div ref="locRollChartRef" class="chart-container"></div>
          </div>
          <div class="chart-block">
            <div class="chart-title">LOC 航向道偏差（滑行）</div>
            <div ref="locTaxiChartRef" class="chart-container"></div>
          </div>
          <div class="chart-block">
            <div class="chart-title">LOC 航向道偏差（爬升）</div>
            <div ref="locClimbChartRef" class="chart-container"></div>
          </div>
          <div class="chart-block">
            <div class="chart-title">LOC 航向道偏差（进近）</div>
            <div ref="locApproachChartRef" class="chart-container"></div>
          </div>
          <div class="chart-block">
            <div class="chart-title">GS 下滑道偏差</div>
            <div ref="gsChartRef" class="chart-container"></div>
          </div>
        </div>
      </template>
    </div>
  </Dialog>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { QarApi } from '@/api/efvs/qar'
import * as echarts from 'echarts'
import type { MarkPointComponentOption } from 'echarts'
import dayjs from 'dayjs'
import { createPerformanceReportDocx } from './performanceReportDocx'

defineOptions({ name: 'DataAnalysis' })

/** 进近低高度超限标注（内联，避免独立 ts 文件在部分工程未同步导致构建失败） */
const LOW_ALT_RA_MAX_FT = 300
const LOW_ALT_WARN_MIU = 25
function isLowAltitudeDeviationExceedance(altFt: number, miuA: number): boolean {
  if (!Number.isFinite(altFt) || !Number.isFinite(miuA)) return false
  if (altFt > LOW_ALT_RA_MAX_FT || altFt < 0) return false
  return miuA > LOW_ALT_WARN_MIU || miuA < -LOW_ALT_WARN_MIU
}
function pickWorstLowAltitudeExceedance(data: [number, number][]): [number, number] | null {
  let best: [number, number] | null = null
  let bestAbs = 0
  for (const pt of data) {
    if (!pt || pt.length < 2) continue
    const [alt, y] = pt
    if (!isLowAltitudeDeviationExceedance(alt, y)) continue
    const abs = Math.abs(y)
    if (abs > bestAbs) {
      bestAbs = abs
      best = [alt, y]
    }
  }
  return best
}
function buildLowAltWarnMarkPoint(data: [number, number][]): MarkPointComponentOption | undefined {
  const p = pickWorstLowAltitudeExceedance(data)
  if (!p) return undefined
  const [, y] = p
  const alt = p[0]
  return {
    z: 50,
    symbol: 'pin',
    symbolSize: 52,
    itemStyle: { color: '#ef4444', shadowBlur: 6, shadowColor: 'rgba(0,0,0,0.35)' },
    label: {
      show: true,
      formatter: `${LOW_ALT_RA_MAX_FT}ft以下超限\n(${Number(y).toFixed(2)} μA)`,
      color: '#fecaca',
      fontSize: 11,
      lineHeight: 15,
      padding: [5, 7],
      backgroundColor: 'rgba(15,23,42,0.92)',
      borderColor: '#ef4444',
      borderWidth: 2,
      borderRadius: 4
    },
    data: [{ name: '超限', coord: [alt, y], value: y }]
  }
}

/** 飞行状态（TAKEOFF_ROLL/CLIMB/APPROACH/TAXI）及 LOC/GS 图表绘制逻辑与飞行复现 flyDataIndex.vue 保持一致 */

const route = useRoute()
const error = ref('')
const dialogTitle = ref('') // 弹窗的标题
const dialogVisible = ref(false) // 弹窗的是否展示

/** 从路由 query 获取 qarids：支持 ids=21&ids=20 或 id=21（每个 id 代表一个架次） */
const qarIds = ref<number[]>([])
/* const qarIds = computed(() => {
  const ids = route.query.ids
  const id = route.query.id
  if (ids != null) {
    const arr = Array.isArray(ids) ? ids : [ids]
    return arr
      .filter((v) => v != null && String(v).trim() !== '')
      .map((v) => Number(v))
      .filter((n) => Number.isFinite(n))
  }
  if (id != null && String(id).trim() !== '') {
    const n = Number(id)
    return Number.isFinite(n) ? [n] : []
  }
  return []
}) */
/** 验证并转换 IDs：支持单个数字、数字数组、字符串、字符串数组，过滤无效值 */
function parseQarIds(ids: number | number[] | string | string[]): number[] {
  // 统一转为数组
  const arr = Array.isArray(ids) ? ids : [ids]

  return arr
    .filter((v) => v != null && String(v).trim() !== '') // 过滤空值
    .map((v) => Number(v)) // 转为数字
    .filter((n) => Number.isFinite(n)) // 验证有效数字
}

const isRunning = ref(false)
const isPaused = ref(false)
const totalCount = ref(0)
const exportLoading = ref(false)

/** 与后端 QarTaxiComparePageReqVO.DEFAULT_PAGE_SIZE、QarTaxiPhasePlaybackService BATCH_SIZE 一致 */
const COMPARE_PAGE_SIZE = 10

/** 用户停止或关闭时置 true，打断分页循环 */
let compareHttpAbort = false

/** 按 QAR id 分组的累计数据：key = qarId，value = 该架次已接收的全部阶段数据（与飞行复现一致，不做阶段清空） */
const sortieDataByQarId = ref<Record<number, any[]>>({})

/** 与飞行复现一致的阶段中文名 */
const FLIGHT_PHASE_LABELS: Record<string, string> = {
  TAKEOFF_ROLL: '滑跑阶段',
  CLIMB: '爬升阶段',
  APPROACH: '进近状态',
  TAXI: '滑行阶段'
}

/**
 * 传输状态判定说明（页头展示）：
 * - HTTP 分页：每架次每批 10 条（与 taxi-phase/play 一致），自动循环直至各架次 completed。
 * - 已接收 X 条：所有架次累计已接收的数据条数（sortieDataByQarId 各架次数组长度之和）。
 * - 当前阶段：取已接收数据中 id 最大的一条所属阶段，只展示当前阶段。
 */
const flightPhaseLabels = computed(() => {
  const map = sortieDataByQarId.value
  let latestId = -1
  let latestPhase: string | null = null
  Object.values(map).forEach((list) => {
    ;(list || []).forEach((item: any) => {
      const id = item?.id != null ? Number(item.id) : -1
      if (Number.isFinite(id) && id > latestId) {
        latestId = id
        const p = item?.flightPhase
        latestPhase = p && typeof p === 'string' && p.trim() !== '' ? p.trim() : null
      }
    })
  })
  if (latestPhase == null) return ''
  return FLIGHT_PHASE_LABELS[latestPhase] || latestPhase
})

const locRollChartRef = ref<HTMLDivElement>()
const locTaxiChartRef = ref<HTMLDivElement>()
const locClimbChartRef = ref<HTMLDivElement>()
const locApproachChartRef = ref<HTMLDivElement>()
const gsChartRef = ref<HTMLDivElement>()
let locRollChartInstance: echarts.ECharts | null = null
let locTaxiChartInstance: echarts.ECharts | null = null
let locClimbChartInstance: echarts.ECharts | null = null
let locApproachChartInstance: echarts.ECharts | null = null
let gsChartInstance: echarts.ECharts | null = null

const grid = { left: '8%', right: '12%', top: '15%', bottom: '15%', containLabel: true }
const axisLabel = { color: '#94a3b8', fontSize: 11 }
const textStyle = { color: '#94a3b8', fontSize: 12, fontWeight: 'bold' as const }
const splitLine = { show: true, lineStyle: { color: 'rgba(148, 163, 184, 0.15)', width: 1 } }
const axisLine = { onZero: false, lineStyle: { color: 'rgba(148, 163, 184, 0.25)' } }
const axisTick = { show: true, lineStyle: { color: 'rgba(148, 163, 184, 0.2)' } }
const lineCapJoin = { cap: 'round' as const, join: 'round' as const }
const xAxisName = { nameLocation: 'middle' as const, nameGap: 32, splitNumber: 6 }
const yAxisName = {
  position: 'left' as const,
  nameLocation: 'end' as const,
  nameGap: 12,
  splitNumber: 6
}

// 3D 复现的 LOC 地面图使用 Dots 刻度（-1.5 ~ +1.5），而后端在本页直接使用 miuA。
// 通过对照 3D 图：+1.5 dots ~= +75 miuA => 1 dot ~= 50 miuA
const LOC_DOTS_TO_MIU = 50
const LOC_RUNWAY_EDGE_DOTS = 1.5
const LOC_RUNWAY_EDGE_MIU = LOC_RUNWAY_EDGE_DOTS * LOC_DOTS_TO_MIU // 75

// 地面（滑跑/滑行）LOC 标准线：y=±35 miuA，x 只绘制末端 300ft 区间（换算成米）
const RUNWAY_LENGTH_M = 3600
const LOC_STD_X_END_FT = 300
const FT_TO_M = 0.3048
const LOC_STD_X_END_M = LOC_STD_X_END_FT * FT_TO_M
const LOC_STD_X_START_M = RUNWAY_LENGTH_M - LOC_STD_X_END_M
const LOC_STD_MIU = 35

/** 多架次线条颜色（与飞行复现风格一致，区分架次） */
const SERIES_COLORS = ['#f97316', '#38bdf8', '#22c55e', '#fbbf24', '#a855f7', '#ec4899', '#06b6d4']

function sleep(ms: number) {
  return new Promise<void>((resolve) => setTimeout(resolve, ms))
}

/** 取 LOC 值：后端推送的 locDeviation 已是 locDeviationRes(miuA) */
function locVal(item: any): number {
  const v =
    item.locDeviation != null && item.locDeviation !== ''
      ? parseFloat(String(item.locDeviation))
      : NaN
  return v
}

/** 取 GS 值：glideslopeDeviationRes */
function gsVal(item: any): number {
  const v =
    item.glideslopeDeviationRes != null && item.glideslopeDeviationRes !== ''
      ? parseFloat(String(item.glideslopeDeviationRes))
      : NaN
  return v
}

/** 将 time 字符串（如 HH:MM:SS）解析为当日秒数，用于排序；解析失败返回 NaN */
function parseTimeToSeconds(t: unknown): number {
  if (t == null || typeof t !== 'string' || !String(t).trim()) return NaN
  const parts = String(t).trim().split(':')
  if (parts.length < 3) return NaN
  const h = parseFloat(parts[0])
  const m = parseFloat(parts[1])
  const s = parseFloat(parts[2])
  if (!Number.isFinite(h) || !Number.isFinite(m) || !Number.isFinite(s)) return NaN
  return h * 3600 + m * 60 + s
}

/** LOC 滑跑：TAKEOFF_ROLL，X=累计距离(m)，Y=航向道偏差 miuA */
function buildLocRollSeries(
  sortieLists: any[][]
): { name: string; data: [number, number][]; color: string }[] {
  return sortieLists.map((list, idx) => {
    const data: [number, number][] = []
    list.forEach((item) => {
      if ((item.flightPhase ?? '') !== 'TAKEOFF_ROLL') return
      const cum =
        item.cumulativeDistance != null ? parseFloat(String(item.cumulativeDistance)) : NaN
      const y = locVal(item)
      if (!Number.isNaN(cum) && !Number.isNaN(y)) data.push([cum, y])
    })
    return {
      name: `架次${idx + 1} (QAR ${qarIds.value[idx] ?? ''})`,
      data,
      color: SERIES_COLORS[idx % SERIES_COLORS.length]
    }
  })
}

/** LOC 滑行：TAXI，X=3600-累计距离(m)，Y=航向道偏差 miuA */
function buildLocTaxiSeries(
  sortieLists: any[][]
): { name: string; data: [number, number][]; color: string }[] {
  return sortieLists.map((list, idx) => {
    const data: [number, number][] = []
    list.forEach((item) => {
      if ((item.flightPhase ?? '') !== 'TAXI') return
      const cum =
        item.cumulativeDistance != null ? parseFloat(String(item.cumulativeDistance)) : NaN
      const y = locVal(item)
      if (!Number.isNaN(cum) && !Number.isNaN(y)) data.push([3600 - cum, y])
    })
    return {
      name: `架次${idx + 1} (QAR ${qarIds.value[idx] ?? ''})`,
      data,
      color: SERIES_COLORS[idx % SERIES_COLORS.length]
    }
  })
}

/** LOC 爬升：CLIMB，X=高度(ft) radioAltitude≤1100，Y=航向道偏差 miuA */
function buildLocClimbSeries(
  sortieLists: any[][]
): { name: string; data: [number, number][]; color: string }[] {
  return sortieLists.map((list, idx) => {
    const data: [number, number][] = []
    list.forEach((item) => {
      if ((item.flightPhase ?? '') !== 'CLIMB') return
      const alt =
        item.radioAltitude != null && item.radioAltitude !== ''
          ? parseFloat(String(item.radioAltitude))
          : NaN
      const y = locVal(item)
      if (!Number.isNaN(alt) && alt <= 1100 && !Number.isNaN(y)) data.push([alt, y])
    })
    return {
      name: `架次${idx + 1} (QAR ${qarIds.value[idx] ?? ''})`,
      data,
      color: SERIES_COLORS[idx % SERIES_COLORS.length]
    }
  })
}

/**
 * 生成水平标准线的密集采样点。
 * 若只用两个端点，dataZoom 放大到区间内部时两端点都被过滤掉，整段标准线会消失；
 * 按段采样后窗口内始终有点，标准线即可随缩放保留。
 */
function buildFlatStdPoints(xFrom: number, xTo: number, y: number, segments = 60): [number, number][] {
  const pts: [number, number][] = []
  for (let i = 0; i <= segments; i++) {
    pts.push([xFrom + ((xTo - xFrom) * i) / segments, y])
  }
  return pts
}

/** LOC 爬升标准线：y=±35，x=800~1100ft */
function getLocClimbStandardLineSeries(): {
  name: string
  data: [number, number][]
  color: string
}[] {
  return [
    {
      name: '标准线 +35',
      data: buildFlatStdPoints(800, 1100, 35),
      color: '#facc15'
    },
    {
      name: '标准线 -35',
      data: buildFlatStdPoints(800, 1100, -35),
      color: '#facc15'
    }
  ]
}

/** LOC 进近：APPROACH，X=高度(ft) radioAltitude≤1100，Y=航向道偏差 miuA */
function buildLocApproachSeries(
  sortieLists: any[][]
): { name: string; data: [number, number][]; color: string }[] {
  return sortieLists.map((list, idx) => {
    const data: [number, number][] = []
    list.forEach((item) => {
      if ((item.flightPhase ?? '') !== 'APPROACH') return
      const alt =
        item.radioAltitude != null && item.radioAltitude !== ''
          ? parseFloat(String(item.radioAltitude))
          : NaN
      const y = locVal(item)
      if (!Number.isNaN(alt) && alt <= 1100 && !Number.isNaN(y)) data.push([alt, y])
    })
    return {
      name: `架次${idx + 1} (QAR ${qarIds.value[idx] ?? ''})`,
      data,
      color: SERIES_COLORS[idx % SERIES_COLORS.length]
    }
  })
}

/** LOC 进近标准线：y=±35，x=300ft~0ft */
function getLocApproachStandardLineSeries(): {
  name: string
  data: [number, number][]
  color: string
}[] {
  return [
    {
      name: '标准线 +35',
      data: buildFlatStdPoints(300, 0, 35),
      color: '#facc15'
    },
    {
      name: '标准线 -35',
      data: buildFlatStdPoints(300, 0, -35),
      color: '#facc15'
    }
  ]
}

/**
 * GS 下滑道偏差：进近状态时绘制。
 * 条件：flightPhase === 'APPROACH' 且 radioAltitude≤1100 且 glideslopeDeviationRes 有效；
 * 进近阶段所有点都绘制 GS 偏差（不依赖 gsDrawEnabled，避免后端 isApproachPhase 过严导致无数据）。
 */
function buildGSSeries(
  sortieLists: any[][]
): { name: string; data: [number, number][]; color: string }[] {
  return sortieLists.map((list, idx) => {
    const points: { timeSec: number; alt: number; y: number }[] = []
    list.forEach((item) => {
      if ((item.flightPhase ?? '') !== 'APPROACH') return
      const alt =
        item.radioAltitude != null && item.radioAltitude !== ''
          ? parseFloat(String(item.radioAltitude))
          : NaN
      const y = gsVal(item)
      if (!Number.isNaN(alt) && alt <= 1100 && !Number.isNaN(y)) {
        const timeSec = parseTimeToSeconds(item.time)
        points.push({ timeSec: Number.isNaN(timeSec) ? 0 : timeSec, alt, y })
      }
    })
    points.sort((a, b) => a.timeSec - b.timeSec)
    const data = points.map((p) => [p.alt, p.y] as [number, number])
    return {
      name: `架次${idx + 1} (QAR ${qarIds.value[idx] ?? ''})`,
      data,
      color: SERIES_COLORS[idx % SERIES_COLORS.length]
    }
  })
}

const GS_DEG = 3
const GS_L_FT = 12
const TAN_GS = Math.tan((GS_DEG * Math.PI) / 180)
const K = 150 / 0.7

function gsStandardYH(hFt: number): number {
  if (hFt <= 0) return 35
  const d = hFt / TAN_GS
  const angleDeg = (Math.atan((hFt + GS_L_FT) / d) * 180) / Math.PI
  return Math.max(35, (angleDeg - GS_DEG) * K)
}

function gsStandardYL(hFt: number): number {
  if (hFt <= GS_L_FT) return -35
  const d = hFt / TAN_GS
  const angleDeg = (Math.atan((hFt - GS_L_FT) / d) * 180) / Math.PI
  return Math.min(-35, (angleDeg - GS_DEG) * K)
}

function getGSStandardLineSeries(): {
  name: string
  data: [number, number][]
  lineStyle: object
  showSymbol: boolean
}[] {
  const zeroLine: [number, number][] = []
  const upperFlat: [number, number][] = []
  const lowerFlat: [number, number][] = []
  const upperCurve: [number, number][] = []
  const lowerCurve: [number, number][] = []
  const ref35: [number, number][] = []
  const refMinus35: [number, number][] = []
  const GS_X_MAX = 1100
  for (let h = GS_X_MAX; h >= 0; h -= 5) zeroLine.push([h, 0])
  for (let h = GS_X_MAX; h >= 200; h -= 5) {
    upperFlat.push([h, 75])
    lowerFlat.push([h, -75])
  }
  for (let h = 300; h >= 100; h -= 5) {
    upperCurve.push([h, gsStandardYH(h)])
    lowerCurve.push([h, gsStandardYL(h)])
    ref35.push([h, 35])
    refMinus35.push([h, -35])
  }
  return [
    {
      name: '零偏差标准线',
      data: zeroLine,
      lineStyle: { color: '#94a3b8', type: 'dashed', width: 1.5 },
      showSymbol: false
    },
    {
      name: '标准上限 ±75',
      data: upperFlat,
      lineStyle: { color: '#fbbf24', type: 'dashed', width: 2 },
      showSymbol: false
    },
    {
      name: '标准下限 ±75',
      data: lowerFlat,
      lineStyle: { color: '#fbbf24', type: 'dashed', width: 2 },
      showSymbol: false
    },
    {
      name: '标准上限曲线',
      data: upperCurve,
      lineStyle: { color: '#f87171', type: 'dashed', width: 2 },
      showSymbol: false
    },
    {
      name: '标准下限曲线',
      data: lowerCurve,
      lineStyle: { color: '#f87171', type: 'dashed', width: 2 },
      showSymbol: false
    },
    {
      name: '参考 ±35',
      data: ref35,
      lineStyle: { color: '#64748b', type: 'dotted', width: 1 },
      showSymbol: false
    },
    {
      name: '参考 -35',
      data: refMinus35,
      lineStyle: { color: '#64748b', type: 'dotted', width: 1 },
      showSymbol: false
    }
  ]
}

function getLocGroundChartOption(
  series: { name: string; data: [number, number][]; color: string }[],
  inverse?: boolean
): echarts.EChartsOption {
  // 标准线是“死的”：即使还没拉到任何架次数据，也画出来。
  // 用独立折线 series 绘制（markLine 会随 dataZoom 过滤两端点而整段消失）。
  return {
    title: { show: false },
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' },
      backgroundColor: 'rgba(15, 23, 42, 0.92)',
      borderColor: '#475569',
      textStyle: { color: '#e2e8f0', fontSize: 12 }
    },
    legend: {
      data: series.map((s) => s.name).filter((n) => String(n).trim() !== ''),
      textStyle: { color: '#94a3b8' },
      top: 8
    },
    darkMode: true,
    grid: { ...grid, top: '22%' },
    xAxis: {
      type: 'value',
      name: '距离(m)',
      nameTextStyle: textStyle,
      min: 0,
      max: 3600,
      inverse: inverse,
      ...xAxisName,
      axisLabel,
      splitLine,
      axisLine,
      axisTick
    },
    yAxis: {
      type: 'value',
      name: '航向道偏差（miuA）',
      nameTextStyle: textStyle,
      // 地面阶段 LOC 标准线：±35 miuA（只在末端 300ft 区间绘制）
      // 固定范围避免 y 轴被数据缩放导致标准线“看不见”
      min: -90,
      max: 90,
      ...yAxisName,
      axisLabel,
      splitLine,
      axisLine,
      axisTick
    },
    dataZoom: [{ type: 'inside', xAxisIndex: [0], start: 0, end: 100 }],
    series: [
      ...series.map((s, idx) => ({
        id: `loc-data-${idx}`,
        name: s.name,
        type: 'line' as const,
        smooth: true,
        showSymbol: false,
        data: s.data,
        lineStyle: { color: s.color, width: 2, ...lineCapJoin },
        itemStyle: { color: s.color }
      })),
      {
        id: 'loc-std-plus',
        name: '标准线 +35',
        type: 'line' as const,
        smooth: false,
        showSymbol: false,
        silent: true,
        data: buildFlatStdPoints(LOC_STD_X_START_M, RUNWAY_LENGTH_M, LOC_STD_MIU),
        lineStyle: { color: '#facc15', type: 'solid' as const, width: 2 },
        itemStyle: { color: '#facc15' }
      },
      {
        id: 'loc-std-minus',
        name: '标准线 -35',
        type: 'line' as const,
        smooth: false,
        showSymbol: false,
        silent: true,
        data: buildFlatStdPoints(LOC_STD_X_START_M, RUNWAY_LENGTH_M, -LOC_STD_MIU),
        lineStyle: { color: '#facc15', type: 'solid' as const, width: 2 },
        itemStyle: { color: '#facc15' }
      }
    ]
  }
}

function getLocClimbChartOption(
  series: { name: string; data: [number, number][]; color: string }[],
  inverse?: boolean,
  /** 进近图：Y 轴随数据与标准线自动伸缩；爬升图仍为固定 ±50 便于对照 ±35 标准线 */
  adaptiveY?: boolean,
  /** 进近：≤300ft 且 |偏差|>25μA 时超限标注（与飞行复现一致） */
  lowAltWarn?: boolean
): echarts.EChartsOption {
  const lineSeries: echarts.SeriesOption[] = series.map((s) => {
    const row: echarts.LineSeriesOption = {
      name: s.name,
      type: 'line',
      smooth: true,
      showSymbol: false,
      data: s.data,
      lineStyle: { color: s.color, width: 2, ...lineCapJoin },
      itemStyle: { color: s.color },
      z: 6
    }
    if (lowAltWarn && !String(s.name ?? '').startsWith('标准线')) {
      const mp = buildLowAltWarnMarkPoint(s.data)
      if (mp) row.markPoint = mp
    }
    return row
  })
  return {
    title: { show: false },
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' },
      backgroundColor: 'rgba(15, 23, 42, 0.92)',
      borderColor: '#475569',
      textStyle: { color: '#e2e8f0', fontSize: 12 }
    },
    legend: { data: series.map((s) => s.name), textStyle: { color: '#94a3b8' }, top: 8 },
    darkMode: true,
    grid: { ...grid, top: '22%' },
    xAxis: {
      type: 'value',
      name: '高度(ft)',
      nameTextStyle: textStyle,
      min: 0,
      max: 1100,
      inverse: inverse,
      axisLabel,
      splitLine,
      axisLine,
      axisTick,
      ...xAxisName
    },
    yAxis: {
      type: 'value',
      name: '航向道偏差（miuA）',
      nameTextStyle: textStyle,
      ...(adaptiveY
        ? { scale: true }
        : {
            // 标准线在 +35/-35；固定范围避免被数据缩放“挤出视窗”
            min: -50,
            max: 50
          }),
      ...yAxisName,
      axisLabel,
      splitLine,
      axisLine,
      axisTick
    },
    dataZoom: [{ type: 'inside', xAxisIndex: [0], start: 0, end: 100 }],
    series: lineSeries
  }
}

function getGSChartOption(
  series: { name: string; data: [number, number][]; color: string }[]
): echarts.EChartsOption {
  const standardSeries = getGSStandardLineSeries().map((s) => ({
    name: s.name,
    type: 'line' as const,
    xAxisIndex: 0,
    yAxisIndex: 0,
    data: s.data,
    lineStyle: s.lineStyle,
    showSymbol: s.showSymbol
  }))
  const dataSeries = series.map((s) => {
    const row: echarts.LineSeriesOption = {
      name: s.name,
      type: 'line',
      xAxisIndex: 0,
      yAxisIndex: 0,
      smooth: true,
      showSymbol: false,
      data: s.data,
      lineStyle: { color: s.color, width: 2, ...lineCapJoin },
      itemStyle: { color: s.color },
      z: 6
    }
    const mp = buildLowAltWarnMarkPoint(s.data)
    if (mp) row.markPoint = mp
    return row
  })
  return {
    title: { show: false },
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' },
      backgroundColor: 'rgba(15, 23, 42, 0.92)',
      borderColor: '#475569',
      textStyle: { color: '#e2e8f0', fontSize: 12 }
    },
    legend: { data: series.map((s) => s.name), textStyle: { color: '#94a3b8' }, top: 8 },
    darkMode: true,
    grid: { ...grid, top: '22%' },
    xAxis: {
      type: 'value',
      name: '无线电高度(ft)',
      nameTextStyle: textStyle,
      min: 0,
      max: 1100,
      inverse: true,
      axisLabel,
      splitLine,
      axisLine,
      axisTick,
      ...xAxisName
    },
    yAxis: {
      type: 'value',
      name: '下滑道偏差（miuA）',
      nameTextStyle: textStyle,
      scale: true,
      ...yAxisName,
      axisLabel,
      splitLine,
      axisLine,
      axisTick
    },
    dataZoom: [{ type: 'inside', xAxisIndex: [0], start: 0, end: 100 }],
    series: [...standardSeries, ...dataSeries]
  }
}

function initCharts() {
  if (locRollChartRef.value) {
    locRollChartInstance?.dispose()
    locRollChartInstance = echarts.init(locRollChartRef.value)
  }
  if (locTaxiChartRef.value) {
    locTaxiChartInstance?.dispose()
    locTaxiChartInstance = echarts.init(locTaxiChartRef.value)
  }
  if (locClimbChartRef.value) {
    locClimbChartInstance?.dispose()
    locClimbChartInstance = echarts.init(locClimbChartRef.value)
  }
  if (locApproachChartRef.value) {
    locApproachChartInstance?.dispose()
    locApproachChartInstance = echarts.init(locApproachChartRef.value)
  }
  if (gsChartRef.value) {
    gsChartInstance?.dispose()
    gsChartInstance = echarts.init(gsChartRef.value)
  }

  // 进入页面即把“死的”标准线画好（即使还没开始拉取/还没收到数据）
  locRollChartInstance?.setOption(getLocGroundChartOption([], false), { replaceMerge: ['series'] })
  locTaxiChartInstance?.setOption(getLocGroundChartOption([], true), { replaceMerge: ['series'] })
  locClimbChartInstance?.setOption(getLocClimbChartOption(getLocClimbStandardLineSeries(), false))
  locApproachChartInstance?.setOption(
    getLocClimbChartOption(getLocApproachStandardLineSeries(), true, true, true)
  )
  gsChartInstance?.setOption(getGSChartOption([]))
}

/** 根据当前按 QAR id 分组的数据，生成按顺序的架次列表 */
function getSortieLists(): any[][] {
  const result: any[][] = []
  const ids = qarIds.value
  ids.forEach((id, idx) => {
    const key = Number(id)
    result[idx] = sortieDataByQarId.value[key] ?? []
  })
  return result
}

function updateCharts() {
  const lists = getSortieLists()
  const locRollSeries = buildLocRollSeries(lists)
  const locTaxiSeries = buildLocTaxiSeries(lists)
  const locClimbSeries = buildLocClimbSeries(lists)
  const locApproachSeries = buildLocApproachSeries(lists)
  const gsSeries = buildGSSeries(lists)
  // 地面标准线已改为独立 series，随数据整体重建（replaceMerge 避免缩小时残留旧 series）
  // 滑跑：x=0 -> 3600
  locRollChartInstance?.setOption(getLocGroundChartOption(locRollSeries, false), {
    replaceMerge: ['series']
  })
  // 滑行：x=3600 -> 0
  locTaxiChartInstance?.setOption(getLocGroundChartOption(locTaxiSeries, true), {
    replaceMerge: ['series']
  })
  locClimbChartInstance?.setOption(
    getLocClimbChartOption([...locClimbSeries, ...getLocClimbStandardLineSeries()], false)
  )
  locApproachChartInstance?.setOption(
    // 进近：x=1100 -> 0
    getLocClimbChartOption(
      [...locApproachSeries, ...getLocApproachStandardLineSeries()],
      true,
      true,
      true
    )
  )
  gsChartInstance?.setOption(getGSChartOption(gsSeries))
}

function resizeCharts() {
  locRollChartInstance?.resize()
  locTaxiChartInstance?.resize()
  locClimbChartInstance?.resize()
  locApproachChartInstance?.resize()
  gsChartInstance?.resize()
}

async function startStream() {
  if (qarIds.value.length === 0) {
    ElMessage.warning('请从列表页勾选一个或多个架次后点击「数据分析」进入')
    return
  }
  if (qarIds.value.length > 20) {
    ElMessage.warning('最多支持 20 个架次同时分析')
    return
  }
  error.value = ''
  sortieDataByQarId.value = {}
  totalCount.value = 0
  isPaused.value = false
  compareHttpAbort = false
  isRunning.value = true
  nextTick(() => {
    initCharts()
    updateCharts()
    window.addEventListener('resize', resizeCharts)
  })

  const ids = qarIds.value.map((v) => Number(v)).filter((n) => Number.isFinite(n))

  ElMessage.info('开始一次性聚合加载（全量）…')

  try {
    // 优先：一次性聚合接口（对齐飞行复现 taxi-phase/play 的使用体验）
    const raw = await QarApi.taxiPhaseComparePlay({ ids })
    const respData = (raw as any)?.data ?? raw
    const content = respData?.content ?? respData?.data?.content
    const msg = typeof content === 'string' ? JSON.parse(content) : content
    const sorties = msg?.sorties ?? []

    if (!Array.isArray(sorties)) {
      ElMessage.warning('聚合接口返回结构异常')
      return
    }

    const map: Record<number, any[]> = {}
    sorties.forEach((s: any) => {
      const qid = s?.qarId != null ? Number(s.qarId) : NaN
      const data = s?.data
      if (!Number.isFinite(qid) || !Array.isArray(data)) return
      data.forEach((it: any) => {
        it.qarId = qid
      })
      map[qid] = data
    })
    sortieDataByQarId.value = map
    totalCount.value = Object.values(map).reduce((sum, arr) => sum + (arr?.length ?? 0), 0)
    await nextTick()
    updateCharts()
    ElMessage.success('全量数据加载完成')
  } catch (e) {
    // 兜底：若一次性接口超时/被限制，可继续用分页循环接口
    console.error('[DataAnalysis] compare-play 请求失败，fallback compare-page', e)
    ElMessage.warning('一次性加载失败，改用分页自动拉取…')
    try {
      const sortiesState: Record<string, any>[] = ids.map((qarId) => ({ qarId }))
      // eslint-disable-next-line no-constant-condition
      while (true) {
        if (compareHttpAbort || !isRunning.value) break
        while (isPaused.value && isRunning.value && !compareHttpAbort) {
          await sleep(120)
        }
        if (compareHttpAbort || !isRunning.value) break

        const raw2 = await QarApi.taxiPhaseComparePage({
          pageSize: COMPARE_PAGE_SIZE,
          sorties: sortiesState
        })
        const body = (raw2 as any)?.data ?? raw2
        const rows = body?.sorties ?? []
        if (!Array.isArray(rows) || rows.length === 0) {
          ElMessage.warning('接口未返回 sorties')
          break
        }

        let allCompleted = true
        const map: Record<number, any[]> = { ...sortieDataByQarId.value }

        for (let i = 0; i < rows.length; i++) {
          const row = rows[i]
          const qid = row?.qarId != null ? Number(row.qarId) : ids[i]
          if (row?.state) {
            sortiesState[i] = row.state
          }
          if (row?.completed !== true) {
            allCompleted = false
          }
          const payload = row?.items
          if (Array.isArray(payload) && payload.length > 0 && Number.isFinite(qid)) {
            const arr = map[qid] ?? []
            payload.forEach((it: any) => {
              it.qarId = qid
            })
            map[qid] = [...arr, ...payload]
          }
        }

        sortieDataByQarId.value = map
        totalCount.value = Object.values(map).reduce((s, list) => s + (list?.length ?? 0), 0)
        await nextTick()
        updateCharts()

        if (allCompleted) {
          ElMessage.success('各架次数据已全部拉取完成')
          break
        }
      }
    } catch (e2) {
      console.error('[DataAnalysis] compare-page 请求失败', e2)
      error.value = '加载失败，请检查网络或权限后重试'
      ElMessage.error('加载失败')
    }
  } finally {
    isRunning.value = false
    compareHttpAbort = false
  }
}

// 打开弹窗
const open = (ids: number | number[], title?: string) => {
  compareHttpAbort = true
  isRunning.value = false
  isPaused.value = false

  // 验证并转换 IDs（添加非空检查和数据过滤）
  qarIds.value = parseQarIds(ids)

  // 检查是否有有效 IDs
  if (qarIds.value.length === 0) {
    ElMessage.warning('QAR 记录 ID 无效，请提供有效的数字 ID')
    return
  }

  // 无条件清空上次的传输数据和状态
  sortieDataByQarId.value = {}
  totalCount.value = 0

  // 初始化图表
  if (qarIds.value.length > 0) {
    nextTick(() => {
      initCharts()
      updateCharts()
      resizeCharts()
    })
  }

  // 设置标题并打开弹窗
  dialogTitle.value = title ?? '性能分析'
  dialogVisible.value = true
}

function togglePause() {
  if (!isRunning.value) return
  isPaused.value = !isPaused.value
  ElMessage.info(isPaused.value ? '已暂停拉取（当前请求完成后生效）' : '已继续拉取')
}

function stopStream() {
  if (!isRunning.value) return
  compareHttpAbort = true
  isRunning.value = false
  isPaused.value = false
  ElMessage.info('已停止拉取')
}

/** 导出当前图表信息为标准 DOCX。 */
async function exportToWord() {
  if (
    !locRollChartInstance ||
    !locTaxiChartInstance ||
    !locClimbChartInstance ||
    !locApproachChartInstance ||
    !gsChartInstance
  ) {
    ElMessage.warning('请先等待图表加载完成后再导出')
    return
  }
  let exportStep = '生成图表图片'
  exportLoading.value = true
  try {
    const opts = { type: 'png' as const, pixelRatio: 2, backgroundColor: '#ffffff' }
    const captureChart = (title: string, chart: echarts.ECharts): string => {
      const dataUrl = chart.getDataURL(opts)
      if (!dataUrl.startsWith('data:image/png;base64,')) {
        throw new Error(`${title}未生成有效的 PNG 图片`)
      }
      return dataUrl
    }
    const images = [
      {
        title: 'LOC 航向道偏差（滑跑）',
        dataUrl: captureChart('LOC 航向道偏差（滑跑）', locRollChartInstance)
      },
      {
        title: 'LOC 航向道偏差（滑行）',
        dataUrl: captureChart('LOC 航向道偏差（滑行）', locTaxiChartInstance)
      },
      {
        title: 'LOC 航向道偏差（爬升）',
        dataUrl: captureChart('LOC 航向道偏差（爬升）', locClimbChartInstance)
      },
      {
        title: 'LOC 航向道偏差（进近）',
        dataUrl: captureChart('LOC 航向道偏差（进近）', locApproachChartInstance)
      },
      {
        title: 'GS 下滑道偏差',
        dataUrl: captureChart('GS 下滑道偏差', gsChartInstance)
      }
    ]

    exportStep = '生成 DOCX 文档'
    const { blob, fileName } = await createPerformanceReportDocx({
      qarIds: qarIds.value,
      flightPhaseLabel: flightPhaseLabels.value || '-',
      totalCount: totalCount.value,
      exportedAt: dayjs().format('YYYY-MM-DD HH:mm:ss'),
      images
    })

    exportStep = '下载 DOCX 文档'
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = fileName
    document.body.appendChild(a)
    try {
      a.click()
    } finally {
      a.remove()
      window.setTimeout(() => URL.revokeObjectURL(url), 1000)
    }
    ElMessage.success('导出成功，可用 Word 或 WPS 打开')
  } catch (e) {
    console.error('[DataAnalysis] 导出失败', e)
    const reason = e instanceof Error ? e.message : String(e)
    ElMessage.error(`${exportStep}失败：${reason || '未知错误'}`)
  } finally {
    exportLoading.value = false
  }
}

/* watch(
  () => [route.query.ids, route.query.id],
  () => {
    if (qarIds.value.length > 0 && !isRunning.value) {
      sortieDataByQarId.value = {}
      totalCount.value = 0
      connectedCount.value = 0
      nextTick(() => {
        initCharts()
        updateCharts()
        resizeCharts()
      })
    }
  }
) */

onMounted(() => {
  if (qarIds.value.length > 0) {
    nextTick(() => {
      setTimeout(() => {
        initCharts()
        updateCharts()
        resizeCharts()
      }, 200)
    })
  }
})
// ========== 监听弹框关闭，自动切断连接并重置状态 ==========
watch(
  () => dialogVisible.value,
  (newVal) => {
    if (!newVal) {
      compareHttpAbort = true
      isRunning.value = false
      isPaused.value = false
      sortieDataByQarId.value = {}
      totalCount.value = 0
    }
  }
)
onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  compareHttpAbort = true
  locRollChartInstance?.dispose()
  locTaxiChartInstance?.dispose()
  locClimbChartInstance?.dispose()
  locApproachChartInstance?.dispose()
  gsChartInstance?.dispose()
})
defineExpose({
  open
})
</script>

<style scoped>
.data-analysis {
  min-height: 400px;
  padding: 16px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.page-header .title {
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.page-header .qar-id {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.page-header .label {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.speed-select {
  width: 96px;
}

.empty-tip,
.error-tip {
  padding: 24px;
  color: var(--el-text-color-secondary);
}

.charts-wrap {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.chart-block {
  padding: 16px;
  background: var(--el-bg-color);
  border-radius: 8px;
  box-shadow: 0 1px 3px rgb(0 0 0 / 8%);
}

.chart-title {
  margin-bottom: 12px;
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.chart-container {
  width: 100%;
  height: 360px;
}
</style>
