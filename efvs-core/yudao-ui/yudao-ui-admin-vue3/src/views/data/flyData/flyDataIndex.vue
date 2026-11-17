<template>
  <div class="app-container" :class="{ 'app-container--embedded': embedded }">
    <div class="toolbar">
      <!-- 左侧 3D 控制 -->
      <!-- 播放和重置按钮：重置用于回到进入页面初始状态 -->
      <!-- <el-button type="primary" @click="togglePlay">
        {{ playing ? '暂停' : '播放' }}
      </el-button>
      <el-button class="ff-btn ff-btn--muted" @click="returnToStart" :disabled="!realtimeFlightData">
        回到起点
      </el-button> -->
      <el-button class="ff-btn ff-btn--muted" @click="reset"> 重置 </el-button>
      <el-button class="ff-btn ff-btn--purple" @click="toggleFullScreen">
        {{ isFullScreen ? '取消全屏' : '全屏' }}
      </el-button>
      <span class="label">进度:</span>
      <el-slider
        v-model="realtimeCurrentIndex"
        :min="0"
        :max="maxIndex"
        @change="onScrub"
        class="ff-slider"
      />
      <el-button class="ff-btn ff-btn--muted" @click="stepFrame(-1)" :disabled="!realtimeFlightData"
        >上一帧</el-button
      >
      <el-button class="ff-btn ff-btn--muted" @click="stepFrame(1)" :disabled="!realtimeFlightData"
        >下一帧</el-button
      >
      <el-button class="ff-btn ff-btn--muted" @click="jumpPhase(-1)" :disabled="!realtimeFlightData"
        >上一阶段</el-button
      >
      <el-button class="ff-btn ff-btn--muted" @click="jumpPhase(1)" :disabled="!realtimeFlightData"
        >下一阶段</el-button
      >
      <div class="status">
        ALT: {{ currentAlt }}ft | T+{{
          (realtimeCurrentIndex / (realtimeFlightData?.sampleRate ?? 10)).toFixed(1)
        }}s | 接地点距跑道尾:
        {{ touchdownRunwayHeadMText }}
      </div>

      <span class="toolbar-divider">|</span>

      <!-- 右侧图表 WebSocket 控制 -->
      <el-button
        class="ff-btn ff-btn--primary"
        @click="startConnect"
        :disabled="isRunning || status === 'CONNECTING'"
        :loading="status === 'CONNECTING'"
      >
        {{ status === 'CONNECTING' ? '正在加载数据...' : '加载并播放' }}
      </el-button>
      <el-button class="ff-btn ff-btn--danger" @click="stopConnect" :disabled="!isRunning">
        停止并清空
      </el-button>
      <el-button
        v-if="isConnected && isRunning"
        class="ff-btn"
        :class="isPaused ? 'ff-btn--success' : 'ff-btn--warning'"
        @click="togglePause"
      >
        {{ isPaused ? '恢复' : '暂停' }}
      </el-button>
      <!-- 倍速用独立组件，避免切换时整页 patch 导致 __vnode 报错；与 playbackRate 双向绑定，避免重置后界面倍速与实际不一致 -->
      <SpeedSelect v-model="playbackRate" :disabled="!isConnected || isPaused" />
      <el-tag size="small" :type="isConnected ? 'success' : 'info'">{{ streamStatusText }}</el-tag>
      <span class="label">数据点: {{ totalCount }} 条</span>
      <span class="label phase-label"
        >当前阶段: {{ flightPhaseLabel(currentDataPhase ?? '') || '-' }}</span
      >
      <el-button class="ff-btn ff-btn--muted" @click="clearChart"> 清空图表 </el-button>
      <el-button class="ff-btn ff-btn--primary" :loading="exportWordLoading" @click="exportToWord">
        导出 Word
      </el-button>
    </div>

    <div class="content">
      <!-- 左侧：3D 姿态复现（不动） -->
      <div class="panel">
        <div class="panel-header panel-header--stacked">
          <span>3D 姿态复现 (点击“全屏”放大)</span>
          <span class="panel-header-hint">
            接地点距跑道尾 {{ touchdownRunwayHeadMText }}（0=跑道尾，与图表/轨迹一致）
          </span>
        </div>
        <div class="panel-body">
          <!-- 使用实时传输数据 -->
          <ThreeScene
            ref="threeSceneRef"
            :key="threeSceneKey"
            :data="realtimeFlightData"
            :runway-start-position="runwayStartPosition"
            :current-flight-phase="currentDataPhase"
          />
        </div>
      </div>

      <!-- 右侧：7 个图表，可滚动 -->
      <div class="panel chart-panel">
        <div class="panel-header panel-header--with-tabs">
          <div>飞参曲线（按阶段分别绘制/查看）</div>
          <el-tabs v-model="viewTab" class="phase-tabs" @tab-change="handlePhaseTabChange">
            <el-tab-pane label="HUD" name="HUD" />
            <el-tab-pane label="滑跑" name="TAKEOFF_ROLL" />
            <el-tab-pane label="爬升" name="CLIMB" />
            <el-tab-pane label="进近" name="APPROACH" />
            <el-tab-pane label="滑行" name="TAXI" />
          </el-tabs>
        </div>
        <div v-show="!hudVisible" ref="chartsScrollRef" class="panel-body charts-scroll">
          <div class="chart-block">
            <div class="chart-title">LOC航向道偏差</div>
            <div ref="chartRef" class="chart-container"></div>
          </div>
          <div class="chart-block" v-show="activePhaseTab === 'APPROACH'">
            <div class="chart-title">GS下滑道偏差</div>
            <div ref="gsChartRef" class="chart-container"></div>
          </div>
          <div class="chart-block">
            <div class="chart-title">空速 / 选择速度</div>
            <div ref="airspeedChartRef" class="chart-container"></div>
          </div>
          <div class="chart-block">
            <div class="chart-title">磁航向</div>
            <div ref="magneticHeadingChartRef" class="chart-container"></div>
          </div>
          <div class="chart-block">
            <div class="chart-title">仰角</div>
            <div ref="pitchAngleChartRef" class="chart-container"></div>
          </div>
          <div class="chart-block">
            <div class="chart-title">坡度</div>
            <div ref="slopeChartRef" class="chart-container"></div>
          </div>
        </div>
        <!-- HUD 动图：与图表块互斥显示，切回图表时 3D/曲线状态不变 -->
        <div v-show="hudVisible" class="panel-body hud-body">
          <HudDisplay ref="hudDisplayRef" :item="hudRenderItem" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, shallowRef, computed, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { useRoute } from 'vue-router'
import * as echarts from 'echarts'
import type { MarkPointComponentOption } from 'echarts'
import request from '@/config/axios'
import { ElMessage } from 'element-plus'
import ThreeScene from './components/ThreeScene.vue'
import SpeedSelect from './components/SpeedSelect.vue'
import HudDisplay from './components/HudDisplay.vue'
import type { FlightData, RealtimeDataItem } from './types'
import { createFlightReportDocx, type FlightReportCycleData } from './flightReportDocx'
import { selectHudExportItem } from './hudExportItem'

defineOptions({ name: 'FlyData' })

/** 进近低高度超限标注（内联，避免依赖其他目录下 ts 导致构建失败） */
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

const props = withDefaults(
  defineProps<{
    /** 弹窗内打开时传入，优先级高于路由 query.id */
    qarId?: number
    /** 嵌入弹窗时避免使用整窗 100vh */
    embedded?: boolean
  }>(),
  { embedded: false }
)

const route = useRoute()
/** QAR 记录 id，用于 WebSocket 传给后端：props 优先，否则取路由 query */
const qarId = computed(() => {
  if (props.qarId != null && Number.isFinite(Number(props.qarId))) {
    return Number(props.qarId)
  }
  const id = route.query.id
  if (id == null || id === '') return undefined
  const n = Number(id)
  return Number.isFinite(n) ? n : undefined
})

// ========== 左侧 3D 姿态复现 ==========
// 数据由WebSocket实时传输提供
const playing = ref(false)
const playbackRate = ref(1)
const lastTick = ref(0)
const accum = ref(0)
const threeSceneRef = ref<any>(null)
/** 浏览器全屏状态：同时覆盖按钮切换和按 Esc 等外部退出场景 */
const isFullScreen = ref(false)
// 重置时强制重载 3D 场景：确保与“刚进入页面”一致（飞机初始姿态/轨迹清空）
const threeSceneKey = ref(0)
/** 跑道起点位置：经度和纬度 */
const runwayStartPosition = ref<{ longitude: number; latitude: number } | undefined>(undefined)

/** 组件是否已挂载，用于避免卸载后 WebSocket 回调更新状态导致 __vnode 报错 */
const isComponentMounted = ref(false)
/** 实时动画数据：从WebSocket接收并映射 */
const realtimeFlightData = shallowRef<FlightData | null>(null)
/** 实时数据索引 */
const realtimeCurrentIndex = ref(0)
/** 图表已绘制到 accumulatedTaxiPhaseData 的哪个全局 index（-1 表示还没画） */
const chartAppliedUpToIndex = ref<number>(-1)
/** 是否正在播放实时数据 */
const isRealtimePlaying = ref(false)

const maxIndex = computed(() =>
  realtimeFlightData.value ? realtimeFlightData.value.time.length - 1 : 0
)
const currentAlt = computed(() => {
  if (!realtimeFlightData.value?.position?.[realtimeCurrentIndex.value]) return '0'
  return realtimeFlightData.value.position[realtimeCurrentIndex.value].y.toFixed(0)
})

/** 与 3D 进近/滑行轨迹同一套算法：接地点沿跑道纵轴距跑道头 X=0 的米数 */
const touchdownRunwayHeadMText = computed(() => {
  const x = realtimeFlightData.value?.touchdownXOnRunwayM
  if (x == null || !Number.isFinite(Number(x))) return '—'
  // 现在后端返回的 touchdownXOnRunwayM 已按“距跑道尾”的语义推导：
  // 跑道尾=RUNWAY_LENGTH_M，则距尾 = RUNWAY_LENGTH_M - x
  const tailDist = RUNWAY_LENGTH_M - Number(x)
  if (!Number.isFinite(tailDist)) return '—'
  return `${tailDist.toFixed(1)} m`
})

const syncFullScreenState = () => {
  isFullScreen.value = document.fullscreenElement !== null
}

const toggleFullScreen = async () => {
  // 全屏要包含工具栏按钮，否则全屏后按钮不在 fullscreenElement 内会无法点击
  const container = document.querySelector('.app-container') as HTMLElement | null
  if (!container) return
  // 以浏览器真实全屏状态判断，避免 fullscreenchange 延迟或遗漏时按钮状态失步。
  const enteringFullScreen = document.fullscreenElement === null
  // 先刷新按钮文字，确保进入全屏后立即显示“取消全屏”。若浏览器拒绝请求，catch 中会回滚。
  isFullScreen.value = enteringFullScreen
  try {
    if (enteringFullScreen) {
      await container.requestFullscreen()
    } else {
      await document.exitFullscreen()
    }
    syncFullScreenState()
  } catch (err: any) {
    syncFullScreenState()
    if (enteringFullScreen) {
      alert(`全屏失败: ${err.message}`)
    } else {
      alert(`退出全屏失败: ${err.message}`)
    }
  }
}

const initData = async () => {
  // 数据由WebSocket传输提供，不再使用模拟数据
}

const togglePlay = () => {
  playing.value = !playing.value
  if (playing.value) {
    lastTick.value = performance.now()
    loop()
  }
}

const returnToStart = () => {
  if (!realtimeFlightData.value || !accumulatedTaxiPhaseData.value.length) return
  playing.value = false
  lastTick.value = 0
  accum.value = 0
  isPaused.value = true
  realtimeCurrentIndex.value = 0
  rebuildChartsForIndex(0)
  totalCount.value = accumulatedTaxiPhaseData.value.length
  updateAll()
  ElMessage.info('已回到起点，点击“恢复”继续播放')
}

const onScrub = () => {
  seekToIndex(realtimeCurrentIndex.value)
}
const seekToIndex = (targetIndex: number) => {
  if (!realtimeFlightData.value || !accumulatedTaxiPhaseData.value.length) return
  const idx = Math.max(0, Math.min(Math.round(targetIndex), maxIndex.value))
  playing.value = false
  if (isRunning.value) isPaused.value = true
  realtimeCurrentIndex.value = idx
  updateAll()
  if (!accumulatedTaxiPhaseData.value?.length) return
  if (idx < chartAppliedUpToIndex.value) {
    rebuildChartsForIndex(idx)
  } else {
    applyChartsForwardToIndex(idx)
  }
}
const stepFrame = (direction: -1 | 1) => seekToIndex(realtimeCurrentIndex.value + direction)
const jumpPhase = (direction: -1 | 1) => {
  const items = accumulatedTaxiPhaseData.value
  if (!items.length) return
  const starts = [0]
  for (let i = 1; i < items.length; i++)
    if ((items[i]?.flightPhase ?? '') !== (items[i - 1]?.flightPhase ?? '')) starts.push(i)
  const current = starts.findIndex(
    (start, i) =>
      realtimeCurrentIndex.value >= start &&
      realtimeCurrentIndex.value < (starts[i + 1] ?? items.length)
  )
  seekToIndex(
    starts[Math.max(0, Math.min((current < 0 ? 0 : current) + direction, starts.length - 1))]
  )
}

/** 右侧页签：四个飞行阶段 + HUD 动图；进入页面默认停在 HUD */
type ViewTab = PhaseKey | 'HUD'
const viewTab = ref<ViewTab>('HUD')
/** HUD 页签下记住上一个真正看过的阶段，导出/切回时回到它 */
const lastPhaseTab = ref<PhaseKey>('TAKEOFF_ROLL')
/** 是否处于 HUD 页签 */
const hudVisible = computed(() => viewTab.value === 'HUD')
watch(viewTab, (v) => {
  if (v !== 'HUD') lastPhaseTab.value = v
})
/**
 * 当前查看的飞行阶段。始终是合法 PhaseKey：HUD 页签下回落到上一个阶段，
 * 这样快照/导出那套程序化切 tab 的逻辑不必感知 HUD。
 */
const activePhaseTab = computed<PhaseKey>({
  get: () => (viewTab.value === 'HUD' ? lastPhaseTab.value : viewTab.value),
  set: (v) => {
    // HUD 页签下只记不切，避免快照结束时把视图从 HUD 上拉走
    if (viewTab.value !== 'HUD') viewTab.value = v
  }
})
/** 当前帧的原始数据行，供 HUD 取值；与 realtimeCurrentIndex 对齐 */
const hudItem = ref<ChartDataItem | null>(null)
/** 仅导出截图期间使用，不改变播放索引和页面当前帧。 */
const hudExportItem = ref<ChartDataItem | null>(null)
const hudRenderItem = computed(() => hudExportItem.value ?? hudItem.value)
type HudDisplayExpose = { capturePng: () => Promise<string> }
const hudDisplayRef = ref<HudDisplayExpose | null>(null)

const syncHudItem = () => {
  hudItem.value = accumulatedTaxiPhaseData.value[realtimeCurrentIndex.value] ?? null
}

const updateAll = () => {
  if (!isComponentMounted.value || !realtimeFlightData.value) return
  threeSceneRef.value?.setIndex(realtimeCurrentIndex.value)
  syncHudItem()
}

const loop = (ts?: number) => {
  if (!isComponentMounted.value) {
    playing.value = false
    return
  }
  if (!playing.value || !realtimeFlightData.value) return
  const now = ts ?? performance.now()
  const dt = (now - lastTick.value) / 1000
  lastTick.value = now
  accum.value += dt * playbackRate.value * realtimeFlightData.value.sampleRate
  if (accum.value >= 1) {
    const step = Math.floor(accum.value)
    accum.value -= step
    realtimeCurrentIndex.value = Math.min(realtimeCurrentIndex.value + step, maxIndex.value)
    updateAll()
    applyChartsForwardToIndex(realtimeCurrentIndex.value)
    if (realtimeCurrentIndex.value >= maxIndex.value) {
      playing.value = false
      // 一架次播完保留在末帧，进度与 3D/图表不归零（需从头再看可拖进度条或点「重置」）
    }
  }
  requestAnimationFrame(loop)
}

// ========== 右侧（由 REST 接口一次性加载全量数据）+ 7 图表 ==========

type StreamStatus = 'CONNECTING' | 'OPEN' | 'CLOSED' | 'ERROR'
// status 仅保留原 UI 的 loading / 标签逻辑（原先来自 WebSocket）。
const status = ref<StreamStatus>('CLOSED')

const isConnected = computed(() => status.value === 'OPEN')
const streamStatusText = computed(() => {
  if (status.value === 'CONNECTING') return '正在加载'
  if (status.value === 'OPEN') return '数据已就绪'
  if (status.value === 'ERROR') return '加载失败'
  return '尚未加载'
})
const isRunning = ref(false)
const isPaused = ref(false)
const totalCount = ref(0)

const chartRef = ref<HTMLDivElement>()
const airspeedChartRef = ref<HTMLDivElement>()
const pitchAngleChartRef = ref<HTMLDivElement>()
const slopeChartRef = ref<HTMLDivElement>()
const gsChartRef = ref<HTMLDivElement>()
const magneticHeadingChartRef = ref<HTMLDivElement>()
const chartsScrollRef = ref<HTMLDivElement>()

let chartInstance: echarts.ECharts | null = null
/** 避免 initChart 多次调用重复注册 window resize */
let resizeChartsListenerAttached = false
let airspeedChartInstance: echarts.ECharts | null = null
let pitchAngleChartInstance: echarts.ECharts | null = null
let slopeChartInstance: echarts.ECharts | null = null
let gsChartInstance: echarts.ECharts | null = null
let magneticHeadingChartInstance: echarts.ECharts | null = null

const xAxisData = ref<number[]>([])
const yAxisData = ref<number[]>([])
const yAxisDataAirspeed = ref<number[]>([])
const yAxisDataSelectedSpeed = ref<number[]>([])
const yAxisDataPitchAngle = ref<number[]>([])
const yAxisDataSlope = ref<number[]>([])
const gsXAxisData = ref<number[]>([])
const gsYAxisData = ref<number[]>([])
/** 爬升/进近阶段 LOC：X=高度(ft) radio_altitude 0~1100，Y=航向道偏差miuA loc_deviation_res。进近状态=empty_space_switch含AIR且radio_altitude<=1100且glide_slope_capture含GS ENGAGE */
const climbLocXAxisData = ref<number[]>([])
const climbLocYAxisData = ref<number[]>([])
/** 爬升/进近阶段空速图：X轴高度ft取值radio_altitude，Y轴速度取值airspeed_km，在原空速图上绘制 */
const climbAirspeedXAxisData = ref<number[]>([])
const climbAirspeedYAxisData = ref<number[]>([])
/** 爬升/进近阶段选择速度图：X轴高度ft取值radio_altitude，Y轴速度取值selected_speed_km，在原选择速度图上绘制 */
const climbSelectedSpeedXAxisData = ref<number[]>([])
const climbSelectedSpeedYAxisData = ref<number[]>([])
/** 爬升/进近阶段仰角图：X轴高度ft取值radio_altitude，Y轴角度取值pitch_angle，在原仰角图上绘制 */
const climbPitchAngleXAxisData = ref<number[]>([])
const climbPitchAngleYAxisData = ref<number[]>([])
/** 爬升/进近阶段坡度图：X轴高度ft取值radio_altitude，Y轴坡度取值slope，在原坡度图上绘制 */
const climbSlopeXAxisData = ref<number[]>([])
const climbSlopeYAxisData = ref<number[]>([])
/** 爬升/进近阶段磁航向图：X轴高度ft取值radio_altitude，Y轴角度取值magnetic_heading(0-359°)，在原磁航向图上绘制 */
const climbMagneticHeadingXAxisData = ref<number[]>([])
const climbMagneticHeadingYAxisData = ref<number[]>([])
const yAxisDataMagneticHeading = ref<number[]>([])
const gsLevelFlightReached = ref(false)
const currentCycleIndex = ref(1)
/** 与 xAxisData 一一对应：每个点对应的飞行阶段，用于 7 个图按阶段展示/提示 */
const flightPhaseForCumulativeCharts = ref<string[]>([])
/** 当前接收数据所属阶段（取本批最后一条的 flightPhase），用于界面展示 */
const currentDataPhase = ref('')

type PhaseKey = 'TAKEOFF_ROLL' | 'CLIMB' | 'APPROACH' | 'TAXI'
const exportWordLoading = ref(false)

type PhaseSnapshot = {
  phase: PhaseKey
  capturedAt: string
  images: Partial<
    Record<'loc' | 'gs' | 'airspeed' | 'magneticHeading' | 'pitchAngle' | 'slope', string>
  >
}
const phaseSnapshots = ref<Partial<Record<PhaseKey, PhaseSnapshot>>>({})
// 支持一个 Word 导出多个 cycle：快照按 cycleIndex + phase 缓存
const phaseSnapshotsByCycle = ref<Record<number, Partial<Record<PhaseKey, PhaseSnapshot>>>>({})

type PhaseSeriesStore = {
  // 地面阶段：TAKEOFF 存累计距离；TAXI 存原始累计距离，绘图时用接地点换算为距跑道头米数
  distX: number[]
  locY: number[]
  airspeedY: number[]
  selectedSpeedY: number[]
  pitchAngleY: number[]
  slopeY: number[]
  magneticHeadingY: number[]

  // 空中阶段：X=无线电高度(ft, 0~1100)
  altX: number[]
  locAltY: number[]
  airspeedAltY: number[]
  selectedSpeedAltY: number[]
  pitchAngleAltY: number[]
  slopeAltY: number[]
  magneticHeadingAltY: number[]

  // 仅进近阶段：GS 偏差图
  gsX: number[]
  gsY: number[]
}

const createEmptyPhaseStore = (): PhaseSeriesStore => ({
  distX: [],
  locY: [],
  airspeedY: [],
  selectedSpeedY: [],
  pitchAngleY: [],
  slopeY: [],
  magneticHeadingY: [],
  altX: [],
  locAltY: [],
  airspeedAltY: [],
  selectedSpeedAltY: [],
  pitchAngleAltY: [],
  slopeAltY: [],
  magneticHeadingAltY: [],
  gsX: [],
  gsY: []
})

const phaseSeries = reactive<Record<PhaseKey, PhaseSeriesStore>>({
  TAKEOFF_ROLL: createEmptyPhaseStore(),
  CLIMB: createEmptyPhaseStore(),
  APPROACH: createEmptyPhaseStore(),
  TAXI: createEmptyPhaseStore()
})

/** 跑道总长(m)，与 ThreeScene、mapToFlightData 一致 */
const RUNWAY_LENGTH_M = 3600

function firstFiniteInTaxiDistX(): number | null {
  const txs = phaseSeries.TAXI.distX
  for (let i = 0; i < txs.length; i++) {
    const v = txs[i]
    if (Number.isFinite(v)) return v
  }
  return null
}

/**
 * 滑行阶段图表横轴：距跑道头 X=0 的米数（与 3D mapToFlightData 一致：接地点 anchor − Δ累计）。
 * touchdownXOnRunwayM 为 Processor 给出的接地点距跑道头米数；若用「距跑道尾」表示接地点，则首点横坐标 = 3600 − 距尾。
 */
function taxiCumDistToRunwayHeadM(cum: number): number | null {
  if (!Number.isFinite(cum)) return null
  const first = firstFiniteInTaxiDistX()
  if (first == null) return null
  const tdRaw = realtimeFlightData.value?.touchdownXOnRunwayM
  const anchor = tdRaw != null && Number.isFinite(Number(tdRaw)) ? Number(tdRaw) : RUNWAY_LENGTH_M
  const x = anchor - (cum - first)
  return Math.max(0, Math.min(RUNWAY_LENGTH_M, x))
}

/** Word 导出 / ECharts：滑行段 distX 为累计距离，转成 [距跑道头 m, y] */
function mapTaxiDistToChartPairs(distX: number[], yArr: number[]): Array<[number, number] | null> {
  return distX.map((cum, i) => {
    if (!Number.isFinite(cum) || !Number.isFinite(yArr[i])) return null
    const xv = taxiCumDistToRunwayHeadM(cum)
    return xv != null ? [xv, yArr[i]] : null
  })
}

const isPhaseKey = (p: any): p is PhaseKey =>
  p === 'TAKEOFF_ROLL' || p === 'CLIMB' || p === 'APPROACH' || p === 'TAXI'

/** 导出快照时程序化切换 Tab 会触发 tab-change；须抑制重复 setOption，避免与快照内重绘打架（仍用 merge，因本函数只传局部 option，不能 notMerge） */
let suppressPhaseTabChartHandler = false

const handlePhaseTabChange = () => {
  if (suppressPhaseTabChartHandler) return
  // 切到 HUD 页签时不重建图表，只需把 HUD 对齐到当前帧（加载后未播放时 updateAll 不会被调用）
  if (hudVisible.value) {
    syncHudItem()
    return
  }
  // 四个阶段不能复用上一个阶段的 ECharts option。重建基础实例后再绘制当前阶段，
  // 避免进近的辅助线、刻度、GS 配置等残留到滑跑、爬升或滑行。
  nextTick(() => renderPhaseChartsFromScratch(activePhaseTab.value))
}

const raf = () => new Promise<void>((resolve) => requestAnimationFrame(() => resolve()))

const waitChartFinished = (chart: echarts.ECharts | null | undefined, timeoutMs = 1200) => {
  if (!chart) return Promise.resolve(false)
  return new Promise<boolean>((resolve) => {
    let done = false
    const timer = setTimeout(() => {
      if (done) return
      done = true
      resolve(false)
    }, timeoutMs)
    try {
      chart.once('finished', () => {
        if (done) return
        done = true
        clearTimeout(timer)
        resolve(true)
      })
    } catch {
      clearTimeout(timer)
      resolve(false)
    }
  })
}

const resetChartZoom = (chart: echarts.ECharts | null | undefined) => {
  if (!chart) return
  try {
    // 若存在 dataZoom 组件，重置到 0-100，确保快照导出为“全量视图”
    chart.dispatchAction({ type: 'dataZoom', start: 0, end: 100 } as any)
  } catch {
    // ignore
  }
}

const canvasDataUrlFromEl = (el?: HTMLElement | null): string | undefined => {
  try {
    const c = el?.querySelector?.('canvas') as HTMLCanvasElement | null
    const url = c?.toDataURL?.('image/png')
    return typeof url === 'string' && url.startsWith('data:image') ? url : undefined
  } catch {
    return undefined
  }
}

/** 优先用 ECharts 官方导出，避免容器内多个 canvas 时 querySelector 取错层导致快照为空 */
const chartInstanceDataUrl = (chart: echarts.ECharts | null | undefined): string | undefined => {
  if (!chart) return undefined
  try {
    const url = chart.getDataURL({
      type: 'png',
      pixelRatio: 2,
      backgroundColor: '#ffffff'
    })
    return typeof url === 'string' && url.startsWith('data:image') ? url : undefined
  } catch {
    return undefined
  }
}

// 快照抓取必须串行，否则会互相切 tab 导致“阶段串图”
let phaseSnapshotQueue: Promise<void> = Promise.resolve()
// 暂停实时图表的增量绘制：避免快照过程中因新 cycle 数据刷新导致“快照污染”
const isSnapshottingCharts = ref(false)

const capturePhaseSnapshotInternal = async (
  phase: PhaseKey,
  cycleIndex: number = currentCycleIndex.value
) => {
  const originalTab = activePhaseTab.value
  const scrollEl = chartsScrollRef.value
  const originalScrollTop = scrollEl?.scrollTop ?? 0
  suppressPhaseTabChartHandler = true
  try {
    isSnapshottingCharts.value = true
    if (!chartInstance) {
      initChart()
    }
    activePhaseTab.value = phase
    await nextTick()
    renderPhaseChartsFromScratch(phase)
    await nextTick()
    resizeCharts()
    await raf()
    await raf()

    // 重置各图表的缩放/视窗，避免导出快照“缺一段数据”
    resetChartZoom(chartInstance)
    resetChartZoom(airspeedChartInstance)
    resetChartZoom(magneticHeadingChartInstance)
    resetChartZoom(pitchAngleChartInstance)
    resetChartZoom(slopeChartInstance)
    if (phase === 'APPROACH') resetChartZoom(gsChartInstance)
    await raf()

    await waitChartFinished(chartInstance)
    await waitChartFinished(airspeedChartInstance)
    await waitChartFinished(magneticHeadingChartInstance)
    await waitChartFinished(pitchAngleChartInstance)
    await waitChartFinished(slopeChartInstance)
    if (phase === 'APPROACH') {
      // GS 图仅进近阶段；若尚未 init，尝试初始化一次
      if (!gsChartInstance) {
        initChart()
        resizeCharts()
        await nextTick()
      }
      await waitChartFinished(gsChartInstance)
    }

    const images: PhaseSnapshot['images'] = {}
    const items: {
      key: keyof PhaseSnapshot['images']
      inst: () => echarts.ECharts | null | undefined
      el?: HTMLElement | null
      enabled: boolean
    }[] = [
      { key: 'loc', inst: () => chartInstance, el: chartRef.value, enabled: true },
      {
        key: 'gs',
        inst: () => gsChartInstance,
        el: gsChartRef.value,
        enabled: phase === 'APPROACH'
      },
      {
        key: 'airspeed',
        inst: () => airspeedChartInstance,
        el: airspeedChartRef.value,
        enabled: true
      },
      {
        key: 'magneticHeading',
        inst: () => magneticHeadingChartInstance,
        el: magneticHeadingChartRef.value,
        enabled: true
      },
      {
        key: 'pitchAngle',
        inst: () => pitchAngleChartInstance,
        el: pitchAngleChartRef.value,
        enabled: true
      },
      { key: 'slope', inst: () => slopeChartInstance, el: slopeChartRef.value, enabled: true }
    ]
    for (const it of items) {
      if (!it.enabled) continue
      if (it.el && typeof it.el.scrollIntoView === 'function') {
        it.el.scrollIntoView({ block: 'center', inline: 'nearest', behavior: 'auto' })
        await raf()
        await raf()
      }
      const url = chartInstanceDataUrl(it.inst()) || canvasDataUrlFromEl(it.el)
      if (url) images[it.key] = url
    }

    if (Object.keys(images).length > 0) {
      const prev = phaseSnapshotsByCycle.value[cycleIndex] ?? {}
      phaseSnapshotsByCycle.value = {
        ...phaseSnapshotsByCycle.value,
        [cycleIndex]: {
          ...prev,
          [phase]: { phase, capturedAt: formatNow(), images }
        }
      }
      // 兼容保留：仍让“当前 cycle 的 phaseSnapshots”可用于调试/兜底
      phaseSnapshots.value = {
        ...phaseSnapshots.value,
        [phase]: { phase, capturedAt: formatNow(), images }
      }
    }
  } finally {
    isSnapshottingCharts.value = false
    activePhaseTab.value = originalTab
    await nextTick()
    renderPhaseCharts(originalTab)
    resizeCharts()
    await nextTick()
    if (scrollEl) scrollEl.scrollTop = originalScrollTop
    suppressPhaseTabChartHandler = false
  }
}

const capturePhaseSnapshot = (phase: PhaseKey, cycleIndex: number = currentCycleIndex.value) => {
  phaseSnapshotQueue = phaseSnapshotQueue
    .then(() => capturePhaseSnapshotInternal(phase, cycleIndex))
    .catch(() => undefined as any)
  return phaseSnapshotQueue
}

const phaseOrder: PhaseKey[] = ['TAKEOFF_ROLL', 'CLIMB', 'APPROACH', 'TAXI']
const hasDataInPhaseSeries = (phase: PhaseKey): boolean => {
  const s = phaseSeries[phase]
  return (
    s.distX.length +
      s.altX.length +
      s.locY.length +
      s.locAltY.length +
      s.airspeedY.length +
      s.airspeedAltY.length +
      s.selectedSpeedY.length +
      s.selectedSpeedAltY.length >
    0
  )
}

// 当后端阶段切换时，立即把“上一阶段”的快照固化到对应 cycle，避免后续重绘污染
watch(
  () => currentDataPhase.value,
  (newPhaseStr, oldPhaseStr) => {
    if (!isComponentMounted.value) return
    if (!oldPhaseStr || !newPhaseStr) return
    if (!isPhaseKey(oldPhaseStr) || !isPhaseKey(newPhaseStr)) return
    const oldPhase = oldPhaseStr as PhaseKey
    const newPhase = newPhaseStr as PhaseKey
    if (oldPhase === newPhase) return

    const cycleIndex = currentCycleIndex.value
    const existed = phaseSnapshotsByCycle.value[cycleIndex]?.[oldPhase]
    if (existed) return

    // 不需要阻塞 UI，这些快照会被串行队列化（phaseSnapshotQueue）
    capturePhaseSnapshot(oldPhase, cycleIndex)
  },
  { flush: 'post' }
)

const captureAllPhasesForCycle = async (cycleIndex: number) => {
  if (!isComponentMounted.value) return
  for (const p of phaseOrder) {
    if (!hasDataInPhaseSeries(p)) continue
    const existed = phaseSnapshotsByCycle.value[cycleIndex]?.[p]
    if (existed) continue
    await capturePhaseSnapshot(p, cycleIndex)
  }
}

/** 与后端 TaxiPhaseStreamService 一致：新一轮滑跑/滑行会重置累计距离，需在序列中插入断点，否则多段被连成 Z 字折返 */
const GROUND_CUMULATIVE_DISTANCE_RESET_THRESHOLD_M = 80

const pushGroundPhaseSeriesBreak = (store: PhaseSeriesStore) => {
  store.distX.push(NaN)
  store.locY.push(NaN)
  store.airspeedY.push(NaN)
  store.selectedSpeedY.push(NaN)
  store.pitchAngleY.push(NaN)
  store.slopeY.push(NaN)
  store.magneticHeadingY.push(NaN)
}

const lastFiniteGroundDist = (distX: number[]): number => {
  for (let k = distX.length - 1; k >= 0; k--) {
    const v = distX[k]
    if (Number.isFinite(v)) return v
  }
  return NaN
}

const appendItemToPhaseSeries = (item: ChartDataItem) => {
  const phase = isPhaseKey(item.flightPhase) ? (item.flightPhase as PhaseKey) : undefined
  if (!phase) return
  const store = phaseSeries[phase]

  const cumDist =
    item.cumulativeDistance != null && item.cumulativeDistance !== ''
      ? parseFloat(String(item.cumulativeDistance))
      : NaN
  const locVal =
    item.locDeviation != null && item.locDeviation !== ''
      ? parseFloat(String(item.locDeviation))
      : NaN
  const airspeedVal =
    item.airspeed != null && item.airspeed !== '' ? parseFloat(String(item.airspeed)) : NaN
  const selectedSpeedVal =
    item.selectedSpeed != null && item.selectedSpeed !== ''
      ? parseFloat(String(item.selectedSpeed))
      : NaN
  const pitchVal =
    item.pitchAngle != null && item.pitchAngle !== '' ? parseFloat(String(item.pitchAngle)) : NaN
  const slopeVal = item.slope != null && item.slope !== '' ? parseFloat(String(item.slope)) : NaN
  const headingVal =
    item.magneticHeading != null && item.magneticHeading !== ''
      ? parseFloat(String(item.magneticHeading))
      : NaN

  // 地面阶段
  if ((phase === 'TAKEOFF_ROLL' || phase === 'TAXI') && Number.isFinite(cumDist)) {
    const prev = lastFiniteGroundDist(store.distX)
    if (Number.isFinite(prev) && cumDist < prev - GROUND_CUMULATIVE_DISTANCE_RESET_THRESHOLD_M) {
      pushGroundPhaseSeriesBreak(store)
    }
    store.distX.push(cumDist)
    store.locY.push(Number.isFinite(locVal) ? locVal : NaN)
    store.airspeedY.push(Number.isFinite(airspeedVal) ? airspeedVal : NaN)
    store.selectedSpeedY.push(Number.isFinite(selectedSpeedVal) ? selectedSpeedVal : NaN)
    store.pitchAngleY.push(Number.isFinite(pitchVal) ? pitchVal : NaN)
    store.slopeY.push(Number.isFinite(slopeVal) ? slopeVal : NaN)
    store.magneticHeadingY.push(
      Number.isFinite(headingVal) && headingVal >= 0 && headingVal <= 359 ? headingVal : NaN
    )
    return
  }

  // 空中阶段
  const alt =
    item.radioAltitude != null && item.radioAltitude !== ''
      ? parseFloat(String(item.radioAltitude))
      : NaN
  if ((phase === 'CLIMB' || phase === 'APPROACH') && Number.isFinite(alt) && alt <= 1100) {
    store.altX.push(alt)
    store.locAltY.push(Number.isFinite(locVal) ? locVal : NaN)
    store.airspeedAltY.push(Number.isFinite(airspeedVal) ? airspeedVal : NaN)
    store.selectedSpeedAltY.push(Number.isFinite(selectedSpeedVal) ? selectedSpeedVal : NaN)
    store.pitchAngleAltY.push(Number.isFinite(pitchVal) ? pitchVal : NaN)
    store.slopeAltY.push(Number.isFinite(slopeVal) ? slopeVal : NaN)
    store.magneticHeadingAltY.push(
      Number.isFinite(headingVal) && headingVal >= 0 && headingVal <= 359 ? headingVal : NaN
    )
  }

  // 仅进近：GS
  if (
    phase === 'APPROACH' &&
    item.gsDrawEnabled === true &&
    item.radioAltitude != null &&
    item.radioAltitude !== '' &&
    item.glideslopeDeviationRes != null &&
    item.glideslopeDeviationRes !== ''
  ) {
    const radioAlt = parseFloat(String(item.radioAltitude))
    const gsDev = parseFloat(String(item.glideslopeDeviationRes))
    if (Number.isFinite(radioAlt) && radioAlt <= 1100 && Number.isFinite(gsDev)) {
      store.gsX.push(radioAlt)
      store.gsY.push(gsDev)
    }
  }
}

/** 流式更新：合并同帧内的多次 setOption，减轻主线程卡顿 */
const CHART_OPT_MERGE = { notMerge: false, lazyUpdate: true, silent: true } as const

/** 当前图表实例最后一次用 renderPhaseCharts 配置过的阶段；用于决定是否可安全 appendData */
let lastChartsRenderedPhase: PhaseKey | null = null

/**
 * GS 图里「GS偏差」动态折线所在 series 下标 = 标准线数量（7）。
 * 不可在 setup 顶层调用 getGSStandardLineSeries().length：该函数依赖 TAN_GS 等常量，晚于本段初始化，会触发 TDZ。
 */
const GS_DYNAMIC_SERIES_INDEX = 7
const toAppendY = (v: number): number | null => (Number.isFinite(v) ? v : null)
const toAppendHeading = (v: number): number | null =>
  Number.isFinite(v) && v >= 0 && v <= 359 ? v : null

const appendBreakToActiveCharts = (phase: PhaseKey) => {
  if (phase !== activePhaseTab.value) return
  if (lastChartsRenderedPhase !== phase) return
  if (isSnapshottingCharts.value) return
  try {
    chartInstance?.appendData({ seriesIndex: 0, data: [null] } as any)
    airspeedChartInstance?.appendData({ seriesIndex: 0, data: [null] } as any)
    airspeedChartInstance?.appendData({ seriesIndex: 1, data: [null] } as any)
    pitchAngleChartInstance?.appendData({ seriesIndex: 0, data: [null] } as any)
    slopeChartInstance?.appendData({ seriesIndex: 0, data: [null] } as any)
    magneticHeadingChartInstance?.appendData({ seriesIndex: 0, data: [null] } as any)
  } catch {
    // ignore
  }
}

/** 仅用于“向前播放”时的流式追加，让折线像视频一样连续生长（回退/拖动仍走全量重建） */
const appendItemToActiveCharts = (item: ChartDataItem): boolean => {
  const phase = isPhaseKey(item.flightPhase) ? (item.flightPhase as PhaseKey) : undefined
  if (!phase) return false
  if (phase !== activePhaseTab.value) return false
  if (lastChartsRenderedPhase !== phase) return false
  if (!isComponentMounted.value) return false
  if (isSnapshottingCharts.value) return false

  const cumDist =
    item.cumulativeDistance != null && item.cumulativeDistance !== ''
      ? parseFloat(String(item.cumulativeDistance))
      : NaN
  const locVal =
    item.locDeviation != null && item.locDeviation !== ''
      ? parseFloat(String(item.locDeviation))
      : NaN
  const airspeedVal =
    item.airspeed != null && item.airspeed !== '' ? parseFloat(String(item.airspeed)) : NaN
  const selectedSpeedVal =
    item.selectedSpeed != null && item.selectedSpeed !== ''
      ? parseFloat(String(item.selectedSpeed))
      : NaN
  const pitchVal =
    item.pitchAngle != null && item.pitchAngle !== '' ? parseFloat(String(item.pitchAngle)) : NaN
  const slopeVal = item.slope != null && item.slope !== '' ? parseFloat(String(item.slope)) : NaN
  const headingVal =
    item.magneticHeading != null && item.magneticHeading !== ''
      ? parseFloat(String(item.magneticHeading))
      : NaN

  // 地面阶段：TAXI 横轴为距跑道头米数（接地点 anchor − Δ累计），与 3D 一致
  if ((phase === 'TAKEOFF_ROLL' || phase === 'TAXI') && Number.isFinite(cumDist)) {
    const x = phase === 'TAXI' ? taxiCumDistToRunwayHeadM(cumDist) : cumDist
    if (phase === 'TAXI' && (x == null || !Number.isFinite(x))) return false
    try {
      chartInstance?.appendData({ seriesIndex: 0, data: [[x, toAppendY(locVal)]] } as any)
      airspeedChartInstance?.appendData({
        seriesIndex: 0,
        data: [[x, toAppendY(airspeedVal)]]
      } as any)
      airspeedChartInstance?.appendData({
        seriesIndex: 1,
        data: [[x, toAppendY(selectedSpeedVal)]]
      } as any)
      pitchAngleChartInstance?.appendData({
        seriesIndex: 0,
        data: [[x, toAppendY(pitchVal)]]
      } as any)
      slopeChartInstance?.appendData({ seriesIndex: 0, data: [[x, toAppendY(slopeVal)]] } as any)
      magneticHeadingChartInstance?.appendData({
        seriesIndex: 0,
        data: [[x, toAppendHeading(headingVal)]]
      } as any)
      return true
    } catch {
      return false
    }
  }

  // 空中阶段：x=无线电高度(ft)
  const alt =
    item.radioAltitude != null && item.radioAltitude !== ''
      ? parseFloat(String(item.radioAltitude))
      : NaN
  if ((phase === 'CLIMB' || phase === 'APPROACH') && Number.isFinite(alt) && alt <= 1100) {
    const x = alt
    try {
      chartInstance?.appendData({ seriesIndex: 0, data: [[x, toAppendY(locVal)]] } as any)
      airspeedChartInstance?.appendData({
        seriesIndex: 0,
        data: [[x, toAppendY(airspeedVal)]]
      } as any)
      airspeedChartInstance?.appendData({
        seriesIndex: 1,
        data: [[x, toAppendY(selectedSpeedVal)]]
      } as any)
      pitchAngleChartInstance?.appendData({
        seriesIndex: 0,
        data: [[x, toAppendY(pitchVal)]]
      } as any)
      slopeChartInstance?.appendData({ seriesIndex: 0, data: [[x, toAppendY(slopeVal)]] } as any)
      magneticHeadingChartInstance?.appendData({
        seriesIndex: 0,
        data: [[x, toAppendHeading(headingVal)]]
      } as any)
    } catch {
      // ignore
    }

    // 仅进近：GS 偏差单独追加（标准线不动）
    if (
      phase === 'APPROACH' &&
      item.gsDrawEnabled === true &&
      item.glideslopeDeviationRes != null &&
      item.glideslopeDeviationRes !== ''
    ) {
      const gsDev = parseFloat(String(item.glideslopeDeviationRes))
      if (Number.isFinite(gsDev)) {
        try {
          gsChartInstance?.appendData({
            seriesIndex: GS_DYNAMIC_SERIES_INDEX,
            data: [[x, gsDev]]
          } as any)
        } catch {
          // ignore
        }
      }
    }

    return true
  }

  return false
}

let chartRenderRafId = 0
function scheduleRenderPhaseCharts() {
  if (chartRenderRafId !== 0) return
  chartRenderRafId = requestAnimationFrame(() => {
    chartRenderRafId = 0
    if (!isComponentMounted.value) return
    renderPhaseCharts(activePhaseTab.value)
  })
}

function cancelChartRenderRaf() {
  if (chartRenderRafId !== 0) {
    cancelAnimationFrame(chartRenderRafId)
    chartRenderRafId = 0
  }
}

/**
 * 阶段级全量绘制入口。
 * 图表 DOM 仍复用，但实例与完整 option 每次按阶段重建，确保四阶段的坐标、辅助线、
 * 标准线、GS 系列和曲线彼此独立；不能用局部 option 合并替代。
 */
function renderPhaseChartsFromScratch(phase: PhaseKey) {
  if (!isComponentMounted.value) return
  initChart()
  renderPhaseCharts(phase)
  resizeCharts()
}

/** 流式多批 concat 后合并到同一帧再 mapToFlightData，避免每批 O(n) 全量重算导致卡顿 */
let flightDataRemapRafId = 0
function cancelFlightDataRemapRaf() {
  if (flightDataRemapRafId !== 0) {
    cancelAnimationFrame(flightDataRemapRafId)
    flightDataRemapRafId = 0
  }
}

function scheduleFlightDataRemap() {
  if (flightDataRemapRafId !== 0) return
  flightDataRemapRafId = requestAnimationFrame(() => {
    flightDataRemapRafId = 0
    if (!isComponentMounted.value) return
    const acc = accumulatedTaxiPhaseData.value
    if (!acc.length) return
    realtimeFlightData.value = mapToFlightData(acc)
    realtimeCurrentIndex.value = acc.length - 1
    const fdSync = realtimeFlightData.value
    if (fdSync?.position?.length) {
      const rw = runwayStartPosition.value
      threeSceneRef.value?.setRealtimeMode?.(true)
      threeSceneRef.value?.updateData?.(fdSync, rw ? { runwayStartPosition: rw } : undefined)
    }
    const syncFlightDataTo3D = () => {
      if (!isComponentMounted.value) return
      const fd = realtimeFlightData.value
      if (fd?.position?.length) {
        const rw = runwayStartPosition.value
        threeSceneRef.value?.setRealtimeMode?.(true)
        threeSceneRef.value?.updateData?.(fd, rw ? { runwayStartPosition: rw } : undefined)
      }
    }
    nextTick(syncFlightDataTo3D)
    const accN = acc.length
    if (accN < 5000) {
      setTimeout(syncFlightDataTo3D, 80)
      setTimeout(syncFlightDataTo3D, 250)
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

/** LOC ±35 miuA 标准线：用独立折线 series 绘制（markLine 会随 dataZoom 过滤而整段消失） */
function buildLocStdSeries(xFrom: number, xTo: number) {
  const make = (y: number, name: string) => ({
    name,
    type: 'line' as const,
    smooth: false,
    showSymbol: false,
    silent: true,
    data: buildFlatStdPoints(xFrom, xTo, y),
    lineStyle: { color: '#facc15', type: 'solid' as const, width: 2 },
    itemStyle: { color: '#facc15' }
  })
  return [make(35, '标准线 +35'), make(-35, '标准线 -35')]
}

/** 仅传入局部 option，必须与 initChart 的完整配置 merge；若 setOption(..., true) 会丢掉 grid/yAxis/dataZoom 等，曲线将无法绘制 */
const renderPhaseCharts = (phase: PhaseKey) => {
  if (!isComponentMounted.value) return
  if (!chartInstance) initChart()
  if (!chartInstance) return

  const store = phaseSeries[phase]
  const isGround = phase === 'TAKEOFF_ROLL' || phase === 'TAXI'
  const isAir = phase === 'CLIMB' || phase === 'APPROACH'

  /** 地面阶段：累计距离回跳处插入 NaN，此处映射为 null 以断开折线（避免多段滑跑连成 Z 字） */
  const groundPair = (cumDist: number, y: number): [number, number] | null => {
    if (!Number.isFinite(cumDist) || !Number.isFinite(y)) return null
    if (phase === 'TAKEOFF_ROLL') return [cumDist, y]
    if (phase === 'TAXI') {
      const xv = taxiCumDistToRunwayHeadM(cumDist)
      return xv != null ? [xv, y] : null
    }
    return null
  }

  // LOC
  if (isGround) {
    const locData = store.distX.map((x, i) => groundPair(x, store.locY[i]))
    // 滑跑、滑行也需要在首次绘制时显示 LOC ±35 miuA 标准线；参考区间仅为 0~300m。
    chartInstance?.setOption(
      {
        xAxis: { name: '距离(m)', min: 0, max: 3600, inverse: false },
        yAxis: { name: '航向道偏差（miuA）', min: -50, max: 50, ...yAxisName },
        tooltip: {
          formatter: (params: unknown) => {
            const p = Array.isArray(params) ? params[0] : (params as { dataIndex?: number })
            const idx = p?.dataIndex
            if (typeof idx !== 'number' || idx < 0) return ''
            const cum = store.distX[idx]
            const y = store.locY[idx]
            if (!Number.isFinite(cum) || !Number.isFinite(y)) return ''
            const viewX = phase === 'TAXI' ? (taxiCumDistToRunwayHeadM(cum) ?? cum) : cum
            return `距离(m): ${viewX ?? ''}<br/>航向道偏差miuA: ${y ?? ''}<br/>阶段: ${flightPhaseLabel(phase)}`
          }
        },
        series: [
          { data: locData, ...echartsLinePerf(store.distX.length) },
          ...buildLocStdSeries(0, 300)
        ]
      },
      CHART_OPT_MERGE
    )
  } else if (isAir) {
    const locData = store.altX.map((x, i) => [x, store.locAltY[i]] as [number, number])
    // 标准线（黄色）：y 轴 +35 / -35 miuA
    // CLIMB：x=800ft~1100ft
    // APPROACH：x=300ft~0ft（该图采用 inverse=true，视觉上从高到低）
    const locStdSeries =
      phase === 'CLIMB'
        ? buildLocStdSeries(800, 1100)
        : phase === 'APPROACH'
          ? buildLocStdSeries(300, 0)
          : []
    const locApproachMp =
      phase === 'APPROACH' ? buildLowAltWarnMarkPoint(locData as [number, number][]) : undefined
    chartInstance?.setOption(
      {
        xAxis: { name: '高度(ft)', min: 0, max: 1100, inverse: phase === 'APPROACH' },
        // 标准线在 +35/-35 miuA，必须保证 y 轴可视范围覆盖它们
        yAxis: { name: '航向道偏差（miuA）', min: -50, max: 50, ...yAxisName },
        tooltip: {
          formatter: (params: unknown) => {
            const p = Array.isArray(params) ? params[0] : (params as { dataIndex?: number })
            const idx = p?.dataIndex
            if (typeof idx !== 'number' || idx < 0) return ''
            const x = store.altX[idx]
            const y = store.locAltY[idx]
            return `高度(ft): ${x ?? ''}<br/>航向道偏差miuA: ${y ?? ''}<br/>阶段: ${flightPhaseLabel(phase)}`
          }
        },
        series: [
          {
            data: locData,
            ...echartsLinePerf(store.altX.length),
            ...(locApproachMp ? { markPoint: locApproachMp } : {})
          },
          ...locStdSeries
        ]
      },
      CHART_OPT_MERGE
    )
  }

  // 空速 + 选择速度（同轴对比）
  if (isGround) {
    const dataA = store.distX.map((x, i) => groundPair(x, store.airspeedY[i]))
    const dataS = store.distX.map((x, i) => groundPair(x, store.selectedSpeedY[i]))
    airspeedChartInstance?.setOption(
      {
        xAxis: { name: '距离(m)', min: 0, max: 3600, inverse: false },
        series: [
          { data: dataA, ...echartsLinePerf(store.distX.length) },
          { data: dataS, ...echartsLinePerf(store.distX.length) }
        ]
      },
      CHART_OPT_MERGE
    )
  } else if (isAir) {
    const dataA = store.altX.map((x, i) => [x, store.airspeedAltY[i]] as [number, number])
    const dataS = store.altX.map((x, i) => [x, store.selectedSpeedAltY[i]] as [number, number])
    airspeedChartInstance?.setOption(
      {
        xAxis: { name: '高度(ft)', min: 0, max: 1100, inverse: phase === 'APPROACH' },
        series: [
          { data: dataA, ...echartsLinePerf(store.altX.length) },
          { data: dataS, ...echartsLinePerf(store.altX.length) }
        ]
      },
      CHART_OPT_MERGE
    )
  }

  // 仰角
  if (isGround) {
    const data = store.distX.map((x, i) => groundPair(x, store.pitchAngleY[i]))
    pitchAngleChartInstance?.setOption(
      {
        xAxis: { name: '距离(m)', min: 0, max: 3600, inverse: false },
        yAxis: { name: '仰角(°)', min: -15, max: 15, ...yAxisName },
        series: [{ data, ...echartsLinePerf(store.distX.length) }]
      },
      CHART_OPT_MERGE
    )
  } else if (isAir) {
    const data = store.altX.map((x, i) => [x, store.pitchAngleAltY[i]] as [number, number])
    pitchAngleChartInstance?.setOption(
      {
        xAxis: { name: '高度(ft)', min: 0, max: 1100, inverse: phase === 'APPROACH' },
        yAxis: { name: '仰角(°)', min: -90, max: 90, ...yAxisName },
        series: [{ data, ...echartsLinePerf(store.altX.length) }]
      },
      CHART_OPT_MERGE
    )
  }

  // 坡度
  if (isGround) {
    const data = store.distX.map((x, i) => groundPair(x, store.slopeY[i]))
    slopeChartInstance?.setOption(
      {
        xAxis: { name: '距离(m)', min: 0, max: 3600, inverse: false },
        yAxis: { name: '坡度(°)', min: -45, max: 45, ...yAxisName },
        series: [{ data, ...echartsLinePerf(store.distX.length) }]
      },
      CHART_OPT_MERGE
    )
  } else if (isAir) {
    const data = store.altX.map((x, i) => [x, store.slopeAltY[i]] as [number, number])
    slopeChartInstance?.setOption(
      {
        xAxis: { name: '高度(ft)', min: 0, max: 1100, inverse: phase === 'APPROACH' },
        yAxis: { name: '坡度(°)', min: -45, max: 45, ...yAxisName },
        series: [{ data, ...echartsLinePerf(store.altX.length) }]
      },
      CHART_OPT_MERGE
    )
  }

  // 磁航向
  if (isGround) {
    const data = store.distX.map((x, i) => groundPair(x, store.magneticHeadingY[i]))
    magneticHeadingChartInstance?.setOption(
      {
        xAxis: { name: '距离(m)', min: 0, max: 3600, inverse: false },
        yAxis: { name: '磁航向(°)', min: 0, max: 359, ...yAxisName },
        series: [{ data, ...echartsLinePerf(store.distX.length) }]
      },
      CHART_OPT_MERGE
    )
  } else if (isAir) {
    const data = store.altX.map((x, i) => [x, store.magneticHeadingAltY[i]] as [number, number])
    magneticHeadingChartInstance?.setOption(
      {
        xAxis: { name: '高度(ft)', min: 0, max: 1100, inverse: phase === 'APPROACH' },
        yAxis: { name: '磁航向(°)', min: 0, max: 359, ...yAxisName },
        series: [{ data, ...echartsLinePerf(store.altX.length) }]
      },
      CHART_OPT_MERGE
    )
  }

  // GS（仅进近）
  if (phase === 'APPROACH' && gsChartInstance) {
    const standardSeries = getGSStandardLineSeries()
    const gsSeriesData = store.gsX.map((x, i) => [x, store.gsY[i]] as [number, number])
    const gsWarnMp = buildLowAltWarnMarkPoint(gsSeriesData)
    gsChartInstance.setOption(
      {
        xAxis: { min: 0, max: 1100, inverse: true },
        series: [
          ...standardSeries.map((s) => ({
            name: s.name,
            type: 'line',
            xAxisIndex: 0,
            yAxisIndex: 0,
            data: s.data,
            lineStyle: s.lineStyle,
            showSymbol: false
          })),
          {
            name: 'GS偏差',
            type: 'line',
            xAxisIndex: 0,
            yAxisIndex: 0,
            smooth: false,
            showSymbol: false,
            data: gsSeriesData,
            ...echartsLinePerf(store.gsX.length),
            lineStyle: { color: '#38bdf8', width: 2, ...lineCapJoin },
            itemStyle: { color: '#38bdf8' },
            ...(gsWarnMp ? { markPoint: gsWarnMp } : {})
          }
        ]
      },
      CHART_OPT_MERGE
    )
  }

  lastChartsRenderedPhase = phase
  resizeCharts()
}

const formatNow = (): string => {
  const d = new Date()
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(
    d.getMinutes()
  )}:${pad(d.getSeconds())}`
}
/** 是否已弹出过首次进近状态的记录 ID，避免多次提示 */
let firstApproachIdLogged = false
/** 累积的滑跑/爬升/进近/滑行数据，用于 3D 姿态复现完整轨迹（与图表一致，按批追加） */
const accumulatedTaxiPhaseData = ref<ChartDataItem[]>([])

/** 与后端一致：仅四种阶段 滑跑、爬升、进近、滑行 */
const flightPhaseLabel = (phase: string): string => {
  const map: Record<string, string> = {
    TAKEOFF_ROLL: '滑跑阶段',
    CLIMB: '爬升阶段',
    APPROACH: '进近状态',
    TAXI: '滑行阶段'
  }
  return map[phase] ?? (phase || '-')
}

// 左右和下方留白缩小，绘图区域更大；左侧仍留足空间避免 y 轴名称被裁切
const grid = { left: '8%', right: '12%', top: '15%', bottom: '15%', containLabel: true }
const axisLabel = { color: '#94a3b8', fontSize: 11 }
const textStyle = { color: '#94a3b8', fontSize: 12, fontWeight: 'bold' as const }
// 浅色网格线，类似参考图：若有若无的细线
const splitLine = { show: true, lineStyle: { color: 'rgba(148, 163, 184, 0.15)', width: 1 } }
const axisLine = { onZero: false, lineStyle: { color: 'rgba(148, 163, 184, 0.25)' } }
const axisTick = { show: true, lineStyle: { color: 'rgba(148, 163, 184, 0.2)' } }
// 折线两端圆角，避免开头线段因默认线帽(butt)显得更细
const lineCapJoin = { cap: 'round' as const, join: 'round' as const }

/**
 * 高频数据性能（约 1 秒 10000 条量级）说明：
 *
 * - WebSocket 已在 flushWsChartPending 合并为「每动画帧最多一次 updateChart」，但单帧内仍会：
 *   concat 全量 accumulatedTaxiPhaseData；mapToFlightData 对全量 n 做 O(n) 重算（含接地点 Haversine 等）；
 *   renderPhaseCharts 对当前 Tab 多图 setOption，数组长度可达 n。
 * - 缓解：ECharts line 启用 large（不启用 LTTB 抽样：抽样会丢点，视觉上像“跳着画”）；ThreeScene 轨迹线顶点上限；海量点时减少重复的 3D updateData 定时器。
 * - 若仍吃紧：后端降采样、或对 mapToFlightData 做增量更新（改动面大）。
 */
const ECHARTS_LARGE_THRESHOLD = 2000
/** 流式高频更新：关闭平滑与动画，避免样条/过渡每帧重算造成“一顿一顿” */
const STREAM_LINE_SERIES = {
  smooth: false as const,
  animation: false as const,
  hoverAnimation: false as const
}
function echartsLinePerf(pointCount: number) {
  if (pointCount < ECHARTS_LARGE_THRESHOLD) return { ...STREAM_LINE_SERIES }
  return {
    ...STREAM_LINE_SERIES,
    large: true,
    largeThreshold: 2000
    // 勿加 sampling：'lttb' 等抽样，会抽掉中间点导致折线跳跃
  }
}
// x 轴名称居中，nameGap 留足空间避免被裁切
const xAxisName = { nameLocation: 'middle' as const, nameGap: 32, splitNumber: 6 }
// y 轴名称放到图表左上方
const yAxisName = {
  position: 'left' as const,
  nameLocation: 'end' as const,
  nameGap: 12,
  splitNumber: 6
}

const getChartOption = (): echarts.EChartsOption => ({
  title: { show: false },
  backgroundColor: 'transparent',
  tooltip: {
    trigger: 'axis',
    axisPointer: { type: 'cross' },
    backgroundColor: 'rgba(15, 23, 42, 0.92)',
    borderColor: '#475569',
    textStyle: { color: '#e2e8f0', fontSize: 12 },
    formatter: (params: unknown) => {
      const p = Array.isArray(params)
        ? params[0]
        : (params as { dataIndex?: number; value?: unknown; axisValue?: number })
      const idx = p?.dataIndex
      if (typeof idx !== 'number' || idx < 0) return ''
      const arrX = xAxisData.value
      const arrY = yAxisData.value
      const arrPhase = flightPhaseForCumulativeCharts.value
      if (idx >= arrX.length || idx >= arrY.length) return ''
      const dist = arrX[idx]
      const val = arrY[idx]
      const phase = arrPhase[idx]
      const phaseStr = flightPhaseLabel(phase ?? '')
      return `距离(m): ${dist ?? ''}<br/>航向道偏差: ${val ?? ''}<br/>阶段: ${phaseStr}`
    }
  },
  darkMode: true,
  grid,
  xAxis: {
    type: 'value',
    name: '距离(m)',
    nameTextStyle: textStyle,
    min: 0,
    max: 3600,
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
    ...yAxisName,
    axisLabel,
    splitLine,
    axisLine,
    axisTick
  },
  dataZoom: [{ type: 'inside', xAxisIndex: [0], start: 0, end: 100 }],
  series: [
    {
      name: 'LOC偏差',
      type: 'line',
      smooth: true,
      showSymbol: false,
      data: [],
      lineStyle: { color: '#f97316', width: 2, ...lineCapJoin },
      itemStyle: { color: '#f97316' }
    }
  ]
})

const getAirspeedChartOption = (): echarts.EChartsOption => ({
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
    data: ['空速', '选择速度'],
    textStyle: { color: '#94a3b8', fontSize: 12 },
    top: 4,
    left: 'center'
  },
  darkMode: true,
  grid,
  xAxis: {
    type: 'value',
    name: '距离(m)',
    nameTextStyle: textStyle,
    min: 0,
    max: 3600,
    ...xAxisName,
    axisLabel,
    splitLine,
    axisLine,
    axisTick
  },
  yAxis: {
    type: 'value',
    name: '空速(km/h)',
    nameTextStyle: textStyle,
    ...yAxisName,
    axisLabel,
    splitLine,
    axisLine,
    axisTick
  },
  dataZoom: [{ type: 'inside', xAxisIndex: [0], start: 0, end: 100 }],
  series: [
    {
      name: '空速',
      type: 'line',
      smooth: true,
      showSymbol: false,
      data: [],
      lineStyle: { color: '#22c55e', width: 2, ...lineCapJoin },
      itemStyle: { color: '#22c55e' }
    },
    {
      name: '选择速度',
      type: 'line',
      smooth: true,
      showSymbol: false,
      data: [],
      lineStyle: { color: '#fbbf24', width: 2.5, ...lineCapJoin },
      itemStyle: { color: '#fbbf24' }
    }
  ]
})

const getPitchAngleChartOption = (): echarts.EChartsOption => ({
  title: { show: false },
  backgroundColor: 'transparent',
  tooltip: {
    trigger: 'axis',
    axisPointer: { type: 'cross' },
    backgroundColor: 'rgba(15, 23, 42, 0.92)',
    borderColor: '#475569',
    textStyle: { color: '#e2e8f0', fontSize: 12 }
  },
  darkMode: true,
  grid,
  xAxis: {
    type: 'value',
    name: '距离(m)',
    nameTextStyle: textStyle,
    min: 0,
    max: 3600,
    axisLabel,
    splitLine,
    axisLine,
    axisTick,
    ...xAxisName
  },
  yAxis: {
    type: 'value',
    name: '仰角(°)',
    nameTextStyle: textStyle,
    ...yAxisName,
    min: -15,
    max: 15,
    splitNumber: 10,
    axisLabel: { ...axisLabel, formatter: '{value}°' },
    splitLine,
    axisLine,
    axisTick
  },
  dataZoom: [{ type: 'inside', xAxisIndex: [0], start: 0, end: 100 }],
  series: [
    {
      name: '仰角',
      type: 'line',
      smooth: true,
      showSymbol: false,
      data: [],
      lineStyle: { color: '#f87171', width: 2, ...lineCapJoin },
      itemStyle: { color: '#f87171' },
      markLine: {
        symbol: ['none', 'arrow'],
        symbolSize: [8, 12],
        data: [
          {
            yAxis: 0,
            lineStyle: { color: '#64748b', type: 'dashed' },
            label: { formatter: '水平' }
          }
        ]
      }
    }
  ]
})

const getMagneticHeadingChartOption = (): echarts.EChartsOption => ({
  title: { show: false },
  backgroundColor: 'transparent',
  tooltip: {
    trigger: 'axis',
    axisPointer: { type: 'cross' },
    backgroundColor: 'rgba(15, 23, 42, 0.92)',
    borderColor: '#475569',
    textStyle: { color: '#e2e8f0', fontSize: 12 }
  },
  darkMode: true,
  grid,
  xAxis: {
    type: 'value',
    name: '距离(m)',
    nameTextStyle: textStyle,
    min: 0,
    max: 3600,
    ...xAxisName,
    axisLabel,
    splitLine,
    axisLine,
    axisTick
  },
  yAxis: {
    type: 'value',
    name: '磁航向(°)',
    nameTextStyle: textStyle,
    ...yAxisName,
    min: 0,
    max: 359,
    axisLabel: { ...axisLabel, formatter: '{value}°' },
    splitLine,
    axisLine,
    axisTick
  },
  dataZoom: [{ type: 'inside', xAxisIndex: [0], start: 0, end: 100 }],
  series: [
    {
      name: '磁航向',
      type: 'line',
      smooth: true,
      showSymbol: false,
      data: [] as [number, number][],
      lineStyle: { color: '#22d3ee', width: 2, ...lineCapJoin },
      itemStyle: { color: '#22d3ee' }
    }
  ]
})

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

const getGSChartOption = (): echarts.EChartsOption => {
  const standardSeries = getGSStandardLineSeries().map((s) => ({
    name: s.name,
    type: 'line' as const,
    xAxisIndex: 0,
    yAxisIndex: 0,
    data: s.data,
    lineStyle: s.lineStyle,
    showSymbol: s.showSymbol
  }))
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
    darkMode: true,
    grid,
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
      ...standardSeries,
      {
        name: 'GS偏差',
        type: 'line',
        xAxisIndex: 0,
        yAxisIndex: 0,
        smooth: true,
        showSymbol: false,
        data: [] as [number, number][],
        lineStyle: { color: '#38bdf8', width: 2, ...lineCapJoin },
        itemStyle: { color: '#38bdf8' }
      }
    ]
  }
}

const getSlopeChartOption = (): echarts.EChartsOption => ({
  title: { show: false },
  backgroundColor: 'transparent',
  tooltip: {
    trigger: 'axis',
    axisPointer: { type: 'cross' },
    backgroundColor: 'rgba(15, 23, 42, 0.92)',
    borderColor: '#475569',
    textStyle: { color: '#e2e8f0', fontSize: 12 }
  },
  darkMode: true,
  grid,
  xAxis: {
    type: 'value',
    name: '距离(m)',
    nameTextStyle: textStyle,
    min: 0,
    max: 3600,
    axisLabel,
    splitLine,
    axisLine,
    axisTick,
    ...xAxisName
  },
  yAxis: {
    type: 'value',
    name: '坡度(度)',
    nameTextStyle: textStyle,
    ...yAxisName,
    min: -45,
    max: 45,
    axisLabel: { ...axisLabel, formatter: '{value}°' },
    splitLine,
    axisLine,
    axisTick
  },
  dataZoom: [{ type: 'inside', xAxisIndex: [0], start: 0, end: 100 }],
  series: [
    {
      name: '坡度',
      type: 'line',
      smooth: true,
      showSymbol: false,
      data: [],
      lineStyle: { color: '#a855f7', width: 2, ...lineCapJoin },
      itemStyle: { color: '#a855f7' },
      markLine: {
        symbol: ['none', 'arrow'],
        symbolSize: [8, 12],
        data: [
          {
            yAxis: 0,
            lineStyle: { color: '#64748b', type: 'dashed' },
            label: { formatter: '水平' }
          }
        ]
      }
    }
  ]
})

const resizeCharts = () => {
  if (!isComponentMounted.value) return
  ;[
    chartInstance,
    airspeedChartInstance,
    pitchAngleChartInstance,
    slopeChartInstance,
    gsChartInstance,
    magneticHeadingChartInstance
  ].forEach((c) => c?.resize())
}

const initChart = () => {
  if (!isComponentMounted.value) return
  if (chartRef.value) {
    chartInstance?.dispose()
    chartInstance = echarts.init(chartRef.value)
    chartInstance.setOption(getChartOption())
  }
  if (airspeedChartRef.value) {
    airspeedChartInstance?.dispose()
    airspeedChartInstance = echarts.init(airspeedChartRef.value)
    airspeedChartInstance.setOption(getAirspeedChartOption())
  }
  if (pitchAngleChartRef.value) {
    pitchAngleChartInstance?.dispose()
    pitchAngleChartInstance = echarts.init(pitchAngleChartRef.value)
    pitchAngleChartInstance.setOption(getPitchAngleChartOption())
  }
  if (slopeChartRef.value) {
    slopeChartInstance?.dispose()
    slopeChartInstance = echarts.init(slopeChartRef.value)
    slopeChartInstance.setOption(getSlopeChartOption())
  }
  if (magneticHeadingChartRef.value) {
    magneticHeadingChartInstance?.dispose()
    magneticHeadingChartInstance = echarts.init(magneticHeadingChartRef.value)
    magneticHeadingChartInstance.setOption(getMagneticHeadingChartOption())
  }
  if (gsChartRef.value) {
    gsChartInstance?.dispose()
    gsChartInstance = echarts.init(gsChartRef.value)
    gsChartInstance.setOption(getGSChartOption())
  }
  if (!resizeChartsListenerAttached) {
    window.addEventListener('resize', resizeCharts)
    resizeChartsListenerAttached = true
  }
}

/** 将后端时间字符串（如 HH:MM:SS 或 HH:MM:SS.fff）解析为当日秒数，用于计算时间间隔；解析失败返回 null */
function parseTimeToSeconds(t?: string | null): number | null {
  if (t == null || typeof t !== 'string' || !t.trim()) return null
  const parts = t.trim().split(':')
  if (parts.length < 3) return null
  const h = parseFloat(parts[0])
  const m = parseFloat(parts[1])
  const s = parseFloat(parts[2])
  if (!Number.isFinite(h) || !Number.isFinite(m) || !Number.isFinite(s)) return null
  return h * 3600 + m * 60 + s
}

type ChartDataItem = {
  /** 时间字符串，用于爬升阶段按真实时间差计算水平距离 */
  time?: string
  cumulativeDistance?: number
  locDeviation?: string
  airspeed?: string
  selectedSpeed?: string
  pitchAngle?: string
  slope?: string
  magneticHeading?: string
  radioAltitude?: string
  glideslopeDeviationRes?: string
  glide_slope_capture?: string
  /** 地速（km/h）- 用于爬升阶段X轴累计距离计算 */
  ground_speed_km?: string
  /** 后端判断：AIR 且 radio_altitude<=1100 且 glide_slope_capture=GS ENGAGE 时为 true，仅此时绘制 GS 偏差点 */
  gsDrawEnabled?: boolean
  /** 飞行阶段：TAKEOFF_ROLL-滑跑阶段, CLIMB-爬升阶段, APPROACH-进近状态, TAXI-滑行状态。用于 7 个图表按阶段判断/展示 */
  flightPhase?: string
  /** 经度 - 用于定义跑道起点 */
  longitude?: string
  /** 纬度 - 用于定义跑道起点 */
  latitude?: string
  /** 跑道整体磁航向（度），后端下发的滑跑阶段磁航向中位数，爬升/进近阶段有值，3D 复现用 */
  runwayMagneticHeadingMedian?: number
}

/** 爬升阶段垂直方向缩放系数：小于 1 时轨迹视觉上更平缓（缓慢爬升），1 为真实比例 */
const CLIMB_VERTICAL_SCALE = 0.6

/** 将ChartDataItem数组转换为FlightData动画数据结构 */
const mapToFlightData = (
  data: ChartDataItem[],
  touchdownXOnRunwayMFromBackend?: number | null
): FlightData => {
  const flightData: FlightData = {
    sampleRate: 20,
    time: [],
    position: [],
    attitudeDeg: [],
    deviation: [],
    flightPhases: []
  }

  // 用于爬升阶段累计X坐标
  let cumulativeX = 0
  let prevX = 0
  /** 爬升段第一个点的累计距离（与 cumulativeX 同系），用于水平延伸 delta */
  let climbStartRawX: number | null = null
  /** 爬升段在跑道轴上的起点 X（米）：有滑跑时取滑跑末点，无滑跑时取进入 CLIMB 时的累计距离，不再固定跑道尾 */
  let climbRunwayAnchorX: number | null = null
  /** 滑跑段最后一个点的 X（累计距离），用于衔接爬升起点 */
  let takeoffEndX: number | null = null

  /**
   * 进近段起点累计距离（用于把进近映射到“跑道末端外侧 → 跑道末端”）
   *
   * 之前用 maxApproachCum 会随着实时追加数据不断变大，而本函数每次都会全量重算轨迹，导致历史点被不断往外推，
   * 视觉上像“飞机不往前飞，只在往后画线”。改为以进近首点 cumulativeDistance 作为固定起点，历史点不会漂移。
   */
  const firstApproachCum = (() => {
    const first = data.find((i) => i.flightPhase === 'APPROACH')
    if (!first) return null
    const c =
      first.cumulativeDistance != null
        ? typeof first.cumulativeDistance === 'number'
          ? first.cumulativeDistance
          : parseFloat(String(first.cumulativeDistance))
        : NaN
    return Number.isFinite(c) ? c : null
  })()
  /** 进近段最后一点的累计距离，与 firstApproachCum 一起用于按 FlightDataProcessor 接地点插值 */
  const lastApproachCum = (() => {
    const last = [...data].reverse().find((i) => i.flightPhase === 'APPROACH')
    if (!last) return null
    const c =
      last.cumulativeDistance != null
        ? typeof last.cumulativeDistance === 'number'
          ? last.cumulativeDistance
          : parseFloat(String(last.cumulativeDistance))
        : NaN
    return Number.isFinite(c) ? c : null
  })()

  /** 后端下发：接地点沿跑道纵轴 X（米，0=跑道头）；供进近末点与滑行首点对齐 */
  const touchdownXOnRunwayM = touchdownXOnRunwayMFromBackend ?? null

  /** 进近外侧距离（米）：在跑道尾(X=RUNWAY_LENGTH_M)之外沿航迹再延伸一段，表示从尾端方向飞来 */
  const APPROACH_INBOUND_DISTANCE_M = 1200

  /** 进近段最后一个采样点的场景 X，供滑行衔接：接地若在 3000m 则从 3000 向 0 滑，避免误用 RUNWAY_LENGTH - 全航程 cum */
  let lastApproachSceneX: number | null = null
  /** 滑行段首点的 cumulativeDistance；滑行 X = taxiAnchorX0 - (cum - firstTaxiCum) */
  let firstTaxiCum: number | null = null
  let taxiAnchorX0: number | null = null

  // 阶段区分（与后端一致）：滑跑=GROUND 且本流尚未出现 AIR；滑行=GROUND 且本流已出现 AIR（落地后）；爬升/进近=AIR
  data.forEach((item, index) => {
    const isTakeoffPhase = item.flightPhase === 'TAKEOFF_ROLL' // 滑跑：0m → 3600m
    const isTaxiPhase = item.flightPhase === 'TAXI' // 滑行：3600m → 0m（与滑跑反向）
    const isClimbPhase = item.flightPhase === 'CLIMB'
    const isApproachPhase = item.flightPhase === 'APPROACH'

    let x: number, y: number, z: number

    if (isApproachPhase) {
      // ========== 进近(APPROACH)：场景 X 轴约定 0=跑道头、RUNWAY_LENGTH_M=跑道尾；进近从「尾端方向」飞向「头端」，即 X 从大变小 ==========
      const approachCum =
        item.cumulativeDistance != null
          ? typeof item.cumulativeDistance === 'number'
            ? item.cumulativeDistance
            : parseFloat(String(item.cumulativeDistance))
          : 0
      /** 跑道尾侧基准（米），与 ThreeScene 一致；进近只从尾向头，不沿用上一段 takeoffEndX 当作「尾」 */
      const approachTailX = RUNWAY_LENGTH_M
      if (
        touchdownXOnRunwayM != null &&
        firstApproachCum != null &&
        lastApproachCum != null &&
        lastApproachCum >= firstApproachCum &&
        Number.isFinite(approachCum)
      ) {
        const offset = Math.max(0, approachCum - firstApproachCum)
        const span = lastApproachCum - firstApproachCum
        const t = span > 1e-9 ? Math.min(1, offset / span) : 0
        // 起点在尾端外侧（大 X），终点为接地点（近头端、小 X），t↑ 则 X↓ = 尾→头
        const xAtTailOutbound = approachTailX + APPROACH_INBOUND_DISTANCE_M
        const xTouchdown = Math.min(touchdownXOnRunwayM, approachTailX)
        x = xAtTailOutbound + (xTouchdown - xAtTailOutbound) * t
        x = Math.max(0, x)
      } else if (Number.isFinite(approachCum) && firstApproachCum != null) {
        const offset = Math.max(0, approachCum - firstApproachCum)
        const xRaw = approachTailX + APPROACH_INBOUND_DISTANCE_M - offset
        x = Math.max(0, xRaw)
      } else {
        x = approachTailX
      }
      const radioAlt =
        item.radioAltitude != null && item.radioAltitude !== ''
          ? parseFloat(String(item.radioAltitude))
          : 0
      const heightM = radioAlt * 0.3048
      y = 900 + heightM * CLIMB_VERTICAL_SCALE
      const locDev =
        item.locDeviation != null && item.locDeviation !== ''
          ? parseFloat(String(item.locDeviation))
          : 0
      const slope = item.slope != null && item.slope !== '' ? parseFloat(String(item.slope)) : 0
      z = locDev + slope * 0.5
      lastApproachSceneX = x
    } else if (isClimbPhase) {
      // ========== 爬升阶段(CLIMB)的3D坐标：从跑道尽头起飞，X 前向累加，Y 上升 ==========
      const groundSpeedKmRaw = item.ground_speed_km ?? (item as any).groundSpeed
      const groundSpeedKm =
        groundSpeedKmRaw != null && String(groundSpeedKmRaw) !== ''
          ? parseFloat(String(groundSpeedKmRaw))
          : item.airspeed != null && String(item.airspeed) !== ''
            ? parseFloat(String(item.airspeed))
            : 0
      const groundSpeedMps = (groundSpeedKm * 1000) / 3600

      if (index === 0) {
        cumulativeX =
          item.cumulativeDistance != null ? parseFloat(String(item.cumulativeDistance)) : 0
        prevX = cumulativeX
      } else {
        const prevItem = data[index - 1]
        const tPrev = parseTimeToSeconds(prevItem?.time)
        const tCur = parseTimeToSeconds(item.time)
        let deltaSeconds = 0.1
        if (tPrev != null && tCur != null && tCur > tPrev && tCur - tPrev < 3600) {
          deltaSeconds = tCur - tPrev
        }
        cumulativeX = prevX + groundSpeedMps * deltaSeconds
        prevX = cumulativeX
      }
      const isFirstClimbPoint = climbStartRawX === null
      if (climbStartRawX === null) {
        climbStartRawX = cumulativeX
        if (takeoffEndX != null && Number.isFinite(takeoffEndX)) {
          climbRunwayAnchorX = takeoffEndX
        } else {
          const cd0 =
            item.cumulativeDistance != null && String(item.cumulativeDistance).trim() !== ''
              ? parseFloat(String(item.cumulativeDistance))
              : cumulativeX
          climbRunwayAnchorX = Number.isFinite(cd0) ? cd0 : cumulativeX
        }
      }
      x = (climbRunwayAnchorX ?? 0) + (cumulativeX - climbStartRawX)

      const radioAlt =
        item.radioAltitude != null && item.radioAltitude !== ''
          ? parseFloat(String(item.radioAltitude))
          : 0
      const heightM = radioAlt * 0.3048
      y = isFirstClimbPoint ? 900 : 900 + heightM * CLIMB_VERTICAL_SCALE

      const locDev =
        item.locDeviation != null && item.locDeviation !== ''
          ? parseFloat(String(item.locDeviation))
          : 0
      const slope = item.slope != null && item.slope !== '' ? parseFloat(String(item.slope)) : 0
      z = locDev + slope * 0.5
    } else if (isTakeoffPhase) {
      // ========== 滑跑阶段(TAKEOFF_ROLL)的3D坐标 ==========
      // X轴：累计距离 (cumulativeDistance)
      x =
        item.cumulativeDistance != null
          ? typeof item.cumulativeDistance === 'number'
            ? item.cumulativeDistance
            : parseFloat(String(item.cumulativeDistance))
          : index * flightData.sampleRate

      // 记录滑跑段末端位置，作为后续爬升段的真实起飞点锚点
      if (Number.isFinite(x)) takeoffEndX = x

      // Y轴：无线电高度（ft转m + 基准高度900）
      const alt =
        item.radioAltitude != null && item.radioAltitude !== ''
          ? parseFloat(String(item.radioAltitude))
          : 0
      y = alt * 0.3048 + 900

      // Z轴：航向道偏差
      const loc =
        item.locDeviation != null && item.locDeviation !== ''
          ? parseFloat(String(item.locDeviation))
          : 0
      z = loc
    } else if (isTaxiPhase) {
      // ========== 滑行阶段(TAXI)：首次采样即「接地瞬间」——跑道位置优先用 FlightDataProcessor 与桌面 Java 一致的接地点 X ==========
      // cumulativeDistance 多为全航程单调递增，不能再用 RUNWAY_LENGTH - cum（会与进近末点 X 断层）
      const cum =
        item.cumulativeDistance != null
          ? typeof item.cumulativeDistance === 'number'
            ? item.cumulativeDistance
            : parseFloat(String(item.cumulativeDistance))
          : index * flightData.sampleRate
      if (firstTaxiCum === null) {
        firstTaxiCum = Number.isFinite(cum) ? cum : 0
        // 全量数据：接地点已由 touchdownXOnRunwayM 算出；无经纬/GS 时退回进近末点或兜底
        taxiAnchorX0 = touchdownXOnRunwayM ?? lastApproachSceneX ?? takeoffEndX ?? RUNWAY_LENGTH_M
      }
      const delta =
        Number.isFinite(cum) && firstTaxiCum != null ? Math.max(0, cum - firstTaxiCum) : 0
      const anchor = taxiAnchorX0 ?? touchdownXOnRunwayM ?? takeoffEndX ?? RUNWAY_LENGTH_M
      x = Math.max(0, anchor - delta)

      const alt =
        item.radioAltitude != null && item.radioAltitude !== ''
          ? parseFloat(String(item.radioAltitude))
          : 0
      y = alt * 0.3048 + 900

      const loc =
        item.locDeviation != null && item.locDeviation !== ''
          ? parseFloat(String(item.locDeviation))
          : 0
      z = loc
    } else {
      // ========== 其他阶段（临时使用滑跑逻辑）==========
      x =
        item.cumulativeDistance != null
          ? typeof item.cumulativeDistance === 'number'
            ? item.cumulativeDistance
            : parseFloat(String(item.cumulativeDistance))
          : index * flightData.sampleRate

      const alt =
        item.radioAltitude != null && item.radioAltitude !== ''
          ? parseFloat(String(item.radioAltitude))
          : 0
      y = alt * 0.3048 + 900

      const loc =
        item.locDeviation != null && item.locDeviation !== ''
          ? parseFloat(String(item.locDeviation))
          : 0
      z = loc
    }

    flightData.time.push(index / flightData.sampleRate)
    flightData.position.push({ x, y, z })
    const runwayMedian =
      item.runwayMagneticHeadingMedian != null && item.runwayMagneticHeadingMedian !== ''
        ? typeof item.runwayMagneticHeadingMedian === 'number'
          ? item.runwayMagneticHeadingMedian
          : parseFloat(String(item.runwayMagneticHeadingMedian))
        : undefined
    flightData.attitudeDeg.push({
      pitch:
        item.pitchAngle != null && item.pitchAngle !== '' ? parseFloat(String(item.pitchAngle)) : 0,
      roll: item.slope != null && item.slope !== '' ? parseFloat(String(item.slope)) : 0,
      yaw:
        item.magneticHeading != null && item.magneticHeading !== ''
          ? parseFloat(String(item.magneticHeading))
          : 90,
      ...(runwayMedian !== undefined && !Number.isNaN(runwayMedian)
        ? { runwayMagneticHeadingMedian: runwayMedian }
        : {})
    })
    flightData.deviation.push({
      loc: z,
      gs:
        item.glideslopeDeviationRes != null && item.glideslopeDeviationRes !== ''
          ? parseFloat(String(item.glideslopeDeviationRes))
          : 0
    })
    // 保存每个点对应的飞行阶段，用于 ThreeScene 中绘制不同颜色的轨迹线
    flightData.flightPhases!.push(item.flightPhase || '')
  })

  flightData.touchdownXOnRunwayM = touchdownXOnRunwayM
  return flightData
}

const updateChart = (data: ChartDataItem[]) => {
  if (!isComponentMounted.value || !data?.length) return

  // 如果还没有设置跑道起点，且当前数据包含经纬度信息，则设置为跑道起点
  if (!runwayStartPosition.value && data[0]) {
    const firstItem = data[0]
    const lng =
      firstItem.longitude != null && firstItem.longitude !== ''
        ? parseFloat(firstItem.longitude)
        : NaN
    const lat =
      firstItem.latitude != null && firstItem.latitude !== '' ? parseFloat(firstItem.latitude) : NaN
    if (!isNaN(lng) && !isNaN(lat)) {
      runwayStartPosition.value = { longitude: lng, latitude: lat }
      console.log('设置跑道起点:', runwayStartPosition.value)
    }
  }

  // 3D 复现使用累积数据：按批追加，保证滑跑→爬升→进近→滑行完整轨迹
  accumulatedTaxiPhaseData.value = accumulatedTaxiPhaseData.value.concat(data)
  scheduleFlightDataRemap()

  if (!chartInstance) {
    initChart()
    if (!chartInstance) return
  }

  // 当前阶段仅用于顶部展示（不再用于“清空/重绘”逻辑）
  const newPhase = (data[data.length - 1]?.flightPhase ?? '') as string
  currentDataPhase.value = newPhase

  data.forEach((item) => {
    // 首次进入进近状态时，弹出第一条进近记录的 ID（用于校验后台判定是否生效）
    if (!firstApproachIdLogged && item.flightPhase === 'APPROACH') {
      const id = (item as any).id
      if (id !== undefined && id !== null) {
        ElMessage.info(`进近第一条记录ID: ${id}`)
        console.log('[flyData] first APPROACH record id:', id)
        firstApproachIdLogged = true
      }
    }
    appendItemToPhaseSeries(item)
  })

  // 只重绘当前 Tab：合并到下一帧，避免同批数据内多次 setOption 卡主线程
  scheduleRenderPhaseCharts()
}

/**
 * REST 一次性加载全量数据后的应用逻辑：
 * - 3D：从 index=0 开始播放
 * - 图表：填充 phaseSeries 并只重绘当前 Tab
 *
 * <p>它不会复用 updateChart 的“流式追加并把 index 推到末尾”的行为。</p>
 */
const applyLoadedTaxiPhaseData = (
  items: ChartDataItem[],
  total: number,
  touchdownXOnRunwayMFromBackend?: number | null
) => {
  if (!isComponentMounted.value) return
  if (!items?.length) return

  // 设置跑道起点（用于 ThreeScene 定位）
  if (!runwayStartPosition.value && items[0]) {
    const firstItem = items[0]
    const lng =
      firstItem.longitude != null && firstItem.longitude !== ''
        ? parseFloat(String(firstItem.longitude))
        : NaN
    const lat =
      firstItem.latitude != null && firstItem.latitude !== ''
        ? parseFloat(String(firstItem.latitude))
        : NaN
    if (!isNaN(lng) && !isNaN(lat)) {
      runwayStartPosition.value = { longitude: lng, latitude: lat }
    }
  }

  accumulatedTaxiPhaseData.value = items
  realtimeFlightData.value = mapToFlightData(
    accumulatedTaxiPhaseData.value,
    touchdownXOnRunwayMFromBackend
  )

  // 从开头开始播放，避免“加载完立刻播放但 index 已经是末尾”的情况
  realtimeCurrentIndex.value = 0
  chartAppliedUpToIndex.value = -1

  if (isComponentMounted.value) {
    const fdSync = realtimeFlightData.value
    if (fdSync?.position?.length) {
      const rw = runwayStartPosition.value
      threeSceneRef.value?.setRealtimeMode?.(true)
      // 与 realtimeCurrentIndex=0 一致：首次构图必须在第 0 帧后上方；runwayStartPosition 同帧传入，避免子组件 props 未更新时 mainGroup 与镜头错位
      threeSceneRef.value?.updateData?.(fdSync, {
        planeIndex: 0,
        ...(rw ? { runwayStartPosition: rw } : {})
      })
      threeSceneRef.value?.setIndex?.(0)
    }
  }

  // 同步最新数据到 ThreeScene（短时重试，避免组件尚未完成初始化）
  const syncFlightDataTo3D = () => {
    if (!isComponentMounted.value) return
    const fd = realtimeFlightData.value
    if (fd?.position?.length) {
      const rw = runwayStartPosition.value
      threeSceneRef.value?.setRealtimeMode?.(true)
      threeSceneRef.value?.updateData?.(fd, {
        planeIndex: 0,
        ...(rw ? { runwayStartPosition: rw } : {})
      })
      threeSceneRef.value?.setIndex?.(0)
    }
  }
  nextTick(syncFlightDataTo3D)
  setTimeout(syncFlightDataTo3D, 80)
  setTimeout(syncFlightDataTo3D, 250)

  // 只绘制到 index=0，确保右侧图表与飞机模型一致
  currentDataPhase.value = (items[0]?.flightPhase ?? '') as string
  appendItemToPhaseSeries(items[0])
  chartAppliedUpToIndex.value = 0

  scheduleRenderPhaseCharts()
  totalCount.value = total ?? items.length
}

/**
 * 前进播放时：把 right charts 增量补齐到目标 index（只追加，不重建，保证性能）。
 */
const applyChartsForwardToIndex = (targetIndex: number) => {
  const items = accumulatedTaxiPhaseData.value
  if (!items?.length) return

  const maxIndex = items.length - 1
  const to = Math.max(0, Math.min(targetIndex, maxIndex))
  if (to <= chartAppliedUpToIndex.value) return

  let didAppendToActive = false
  for (let i = chartAppliedUpToIndex.value + 1; i <= to; i++) {
    const it = items[i]
    // 若累计距离回跳，先在“当前激活图表”里插入断点，视觉上不断线折返
    const phase = isPhaseKey(it.flightPhase) ? (it.flightPhase as PhaseKey) : undefined
    if (phase === 'TAKEOFF_ROLL' || phase === 'TAXI') {
      const store = phaseSeries[phase]
      const cumDist =
        it.cumulativeDistance != null && it.cumulativeDistance !== ''
          ? parseFloat(String(it.cumulativeDistance))
          : NaN
      if (Number.isFinite(cumDist)) {
        const prev = lastFiniteGroundDist(store.distX)
        if (
          Number.isFinite(prev) &&
          cumDist < prev - GROUND_CUMULATIVE_DISTANCE_RESET_THRESHOLD_M
        ) {
          appendBreakToActiveCharts(phase)
        }
      }
    }
    appendItemToPhaseSeries(items[i])
    if (appendItemToActiveCharts(it)) didAppendToActive = true
  }

  chartAppliedUpToIndex.value = to
  currentDataPhase.value = (items[to]?.flightPhase ?? '') as string
  // 若已对当前 Tab 走 appendData 流式追加，则不要再全量 setOption（否则又变成“跳着画”）
  if (!didAppendToActive) {
    scheduleRenderPhaseCharts()
  }
}

/**
 * 拖动/回退时：重建 phaseSeries 到目标 index（保证图表与飞机模型一致）。
 */
const rebuildChartsForIndex = (targetIndex: number) => {
  const items = accumulatedTaxiPhaseData.value
  if (!items?.length) return

  const maxIndex = items.length - 1
  const to = Math.max(-1, Math.min(targetIndex, maxIndex))

  clearChart()
  chartAppliedUpToIndex.value = -1

  if (to >= 0) {
    for (let i = 0; i <= to; i++) {
      appendItemToPhaseSeries(items[i])
    }
    chartAppliedUpToIndex.value = to
    currentDataPhase.value = (items[to]?.flightPhase ?? '') as string
    scheduleRenderPhaseCharts()
  }
}

const clearChart = () => {
  if (!isComponentMounted.value) return
  cancelChartRenderRaf()
  cancelFlightDataRemapRaf()
  currentDataPhase.value = ''
  hudItem.value = null
  ;(['TAKEOFF_ROLL', 'CLIMB', 'APPROACH', 'TAXI'] as PhaseKey[]).forEach((p) => {
    phaseSeries[p] = createEmptyPhaseStore()
  })
  chartInstance?.setOption(
    { xAxis: { name: '距离(m)', min: 0, max: 3600 }, series: [{ data: [] }] },
    false
  )
  airspeedChartInstance?.setOption(
    {
      xAxis: { name: '距离(m)', min: 0, max: 3600 },
      series: [{ data: [] }, { data: [] }]
    },
    false
  )
  pitchAngleChartInstance?.setOption(
    {
      xAxis: { name: '距离(m)', min: 0, max: 3600 },
      yAxis: { name: '仰角(°)', min: -15, max: 15, ...yAxisName },
      series: [{ data: [] }]
    },
    false
  )
  slopeChartInstance?.setOption(
    {
      xAxis: { name: '距离(m)', min: 0, max: 3600 },
      yAxis: { name: '坡度(°)', min: -45, max: 45, ...yAxisName },
      series: [{ data: [] }]
    },
    false
  )
  magneticHeadingChartInstance?.setOption(
    {
      xAxis: { name: '距离(m)', min: 0, max: 3600 },
      yAxis: { name: '磁航向(°)', min: 0, max: 359, ...yAxisName },
      series: [{ data: [] }]
    },
    false
  )
  if (gsChartInstance) {
    const standardSeries = getGSStandardLineSeries()
    gsChartInstance.setOption(
      {
        xAxis: {
          min: 0,
          max: 1100,
          inverse: true
        },
        series: [
          ...standardSeries.map((s) => ({
            name: s.name,
            type: 'line',
            xAxisIndex: 0,
            yAxisIndex: 0,
            data: s.data,
            lineStyle: s.lineStyle,
            showSymbol: false
          })),
          {
            name: 'GS偏差',
            type: 'line',
            xAxisIndex: 0,
            yAxisIndex: 0,
            showSymbol: false,
            data: [],
            lineStyle: { color: '#38bdf8', width: 2, ...lineCapJoin },
            itemStyle: { color: '#38bdf8' }
          }
        ]
      },
      CHART_OPT_MERGE
    )
  }
  totalCount.value = 0
  resizeCharts()
}

async function exportToWord() {
  const originalViewTab = viewTab.value
  const exportPhaseTab = activePhaseTab.value
  let exportStep = '准备导出数据'
  exportWordLoading.value = true
  try {
    const phaseOrder: PhaseKey[] = ['TAKEOFF_ROLL', 'CLIMB', 'APPROACH', 'TAXI']

    // 多 cycle 导出：优先使用已缓存的 phaseSnapshotsByCycle；若缓存为空则尝试基于当前 phaseSeries 快照兜底
    const cyclesInSnapshots = Object.keys(phaseSnapshotsByCycle.value)
      .map((k) => Number(k))
      .filter((n) => Number.isFinite(n))
      .sort((a, b) => a - b)
    const hasAnyPhaseDataNow = phaseOrder.some((p) => hasDataInPhaseSeries(p))
    if (cyclesInSnapshots.length === 0 && !hasAnyPhaseDataNow) {
      ElMessage.warning('暂无可导出的图表数据。请先开始播放并加载数据后再导出')
      return
    }
    let cyclesToExport: number[] =
      cyclesInSnapshots.length > 0 ? cyclesInSnapshots : [currentCycleIndex.value]

    // HUD 是报告首页，不属于飞行阶段。首帧可能没有 HUD 参数：当前帧不足时用最近且最完整的有效帧截图。
    exportStep = '选择 HUD 有效数据帧'
    const exportHudItem = selectHudExportItem(
      accumulatedTaxiPhaseData.value,
      realtimeCurrentIndex.value
    )
    if (!exportHudItem) {
      throw new Error('飞行数据中没有可用于 HUD 导出的有效参数')
    }
    hudExportItem.value = exportHudItem
    viewTab.value = 'HUD'
    await nextTick()
    await raf()
    await raf()
    exportStep = '生成 HUD 图片'
    const hudImage = await hudDisplayRef.value?.capturePng()
    if (!hudImage) throw new Error('HUD 组件尚未准备好，无法生成截图')

    // HUD 截图完成后再显示图表，供现有阶段快照逻辑使用。
    viewTab.value = exportPhaseTab
    await nextTick()
    await raf()

    exportStep = '生成飞行阶段图表'
    // ===== 离屏渲染生成“阶段快照” =====
    const offscreenRender = async (
      option: echarts.EChartsOption,
      opts: { pixelRatio?: number; backgroundColor?: string } = {}
    ): Promise<string | undefined> => {
      const host = document.createElement('div')
      host.style.cssText =
        'position:fixed;left:-10000px;top:-10000px;width:980px;height:520px;pointer-events:none;'
      document.body.appendChild(host)
      let inst: echarts.ECharts | null = null
      try {
        inst = echarts.init(host, undefined, { renderer: 'canvas' })
        inst.setOption(option, true)
        await raf()
        await raf()
        // 离屏导出优先用 getDataURL（最接近 ECharts 官方导出路径）
        try {
          const url = inst.getDataURL({
            type: 'png',
            pixelRatio: opts.pixelRatio ?? 2,
            backgroundColor: opts.backgroundColor ?? '#ffffff'
          } as any)
          if (typeof url === 'string' && url.startsWith('data:image')) return url
        } catch {
          // fall through
        }
        // 兜底：部分构建下 getDataURL 可能返回空，改用 getRenderedCanvas
        try {
          const canvas = (inst as any).getRenderedCanvas?.({
            pixelRatio: opts.pixelRatio ?? 2,
            backgroundColor: opts.backgroundColor ?? '#ffffff'
          }) as HTMLCanvasElement | undefined
          const url2 = canvas?.toDataURL?.('image/png')
          return typeof url2 === 'string' && url2.startsWith('data:image') ? url2 : undefined
        } catch {
          return undefined
        }
      } catch {
        return undefined
      } finally {
        try {
          inst?.dispose()
        } catch {}
        try {
          document.body.removeChild(host)
        } catch {}
      }
    }

    const stripTooltip = (o: any) => {
      if (o?.tooltip) o.tooltip = { ...o.tooltip, formatter: undefined }
      return o
    }

    const buildLocOptionForPhase = (phase: PhaseKey): echarts.EChartsOption => {
      const base = stripTooltip({ ...(getChartOption() as any) })
      const s = phaseSeries[phase]
      if (phase === 'CLIMB' || phase === 'APPROACH') {
        const data = s.altX.map((x, i) => [x, s.locAltY[i]] as [number, number])
        base.xAxis = {
          ...(base.xAxis ?? {}),
          name: '高度(ft)',
          min: 0,
          max: 1100,
          inverse: phase === 'APPROACH'
        }
        // 标准线在 +35/-35 miuA，必须保证 y 轴可视范围覆盖它们
        base.yAxis = { ...(base.yAxis ?? {}), name: '航向道偏差（miuA）', min: -50, max: 50 }
        if (phase === 'APPROACH') {
          // 进近阶段：标准线（黄色）
          // y 轴：+35 / -35 miuA
          // x 轴：300ft -> 0ft（该图 xAxis=高度(ft)，并设置 inverse=true）
          const approachLocMarkLine = {
            silent: true,
            symbol: 'none',
            data: [
              [
                { xAxis: 300, yAxis: 35, lineStyle: { color: '#facc15', type: 'solid', width: 2 } },
                { xAxis: 0, yAxis: 35 }
              ],
              [
                {
                  xAxis: 300,
                  yAxis: -35,
                  lineStyle: { color: '#facc15', type: 'solid', width: 2 }
                },
                { xAxis: 0, yAxis: -35 }
              ]
            ]
          }
          const locExMp = buildLowAltWarnMarkPoint(data)
          base.series = [
            {
              ...(base.series?.[0] ?? {}),
              data,
              markLine: approachLocMarkLine,
              ...(locExMp ? { markPoint: locExMp } : {})
            }
          ]
        } else {
          // 爬升阶段：标准线（黄色）
          // y 轴：+35 / -35 miuA
          // x 轴：800ft -> 1100ft（该图 xAxis=高度(ft)，inverse=false）
          const climbLocMarkLine = {
            silent: true,
            symbol: 'none',
            lineStyle: { color: '#facc15', type: 'solid', width: 2 },
            data: [
              [
                { xAxis: 800, yAxis: 35 },
                { xAxis: 1100, yAxis: 35 }
              ],
              [
                { xAxis: 800, yAxis: -35 },
                { xAxis: 1100, yAxis: -35 }
              ]
            ]
          }
          base.series = [{ ...(base.series?.[0] ?? {}), data, markLine: climbLocMarkLine }]
        }
      } else {
        const data =
          phase === 'TAXI'
            ? mapTaxiDistToChartPairs(s.distX, s.locY)
            : s.distX.map((x, i) => [x, s.locY[i]] as [number, number])
        base.xAxis = { ...(base.xAxis ?? {}), name: '距离(m)', min: 0, max: 3600, inverse: false }
        base.yAxis = { ...(base.yAxis ?? {}), name: '航向道偏差（miuA）' }
        base.series = [{ ...(base.series?.[0] ?? {}), data }]
      }
      return base
    }

    const setXYSeries = (
      baseOpt: echarts.EChartsOption,
      xAxis: Record<string, any>,
      yAxis: Record<string, any>,
      data: [number, number][]
    ): echarts.EChartsOption => {
      const o: any = stripTooltip({ ...(baseOpt as any) })
      o.xAxis = { ...(o.xAxis ?? {}), ...xAxis }
      o.yAxis = { ...yAxisName, ...(o.yAxis ?? {}), ...yAxis }
      o.series = [{ ...(o.series?.[0] ?? {}), data }]
      return o
    }

    const buildMagHeadingOptionForPhase = (phase: PhaseKey): echarts.EChartsOption => {
      const base = getMagneticHeadingChartOption()
      const s = phaseSeries[phase]
      if (phase === 'CLIMB') {
        if (s.altX.length > 0) {
          return setXYSeries(
            base,
            { name: '高度(ft)', min: 0, max: 1100, inverse: false },
            { name: '磁航向(°)', min: 0, max: 359 },
            s.altX.map((x, i) => [x, s.magneticHeadingAltY[i]] as [number, number])
          )
        }
        return setXYSeries(
          base,
          { name: '距离(m)', min: 0, max: 3600 },
          { name: '磁航向(°)', min: 0, max: 359 },
          s.distX.map((x, i) => [x, s.magneticHeadingY[i]] as [number, number])
        )
      }
      if (phase === 'APPROACH') {
        return setXYSeries(
          base,
          { name: '高度(ft)', min: 0, max: 1100, inverse: true },
          { name: '磁航向(°)', min: 0, max: 359 },
          s.altX.map((x, i) => [x, s.magneticHeadingAltY[i]] as [number, number])
        )
      }
      if (phase === 'TAXI') {
        return setXYSeries(
          base,
          { name: '距离(m)', min: 0, max: 3600 },
          { name: '磁航向(°)', min: 0, max: 359 },
          mapTaxiDistToChartPairs(s.distX, s.magneticHeadingY) as [number, number][]
        )
      }
      return setXYSeries(
        base,
        { name: '距离(m)', min: 0, max: 3600 },
        { name: '磁航向(°)', min: 0, max: 359 },
        s.distX.map((x, i) => [x, s.magneticHeadingY[i]] as [number, number])
      )
    }

    const buildPitchOptionForPhase = (phase: PhaseKey): echarts.EChartsOption => {
      const base = getPitchAngleChartOption()
      const s = phaseSeries[phase]
      if (phase === 'CLIMB') {
        if (s.altX.length > 0) {
          return setXYSeries(
            base,
            { name: '高度(ft)', min: 0, max: 1100, inverse: false },
            { name: '仰角(°)', min: -90, max: 90 },
            s.altX.map((x, i) => [x, s.pitchAngleAltY[i]] as [number, number])
          )
        }
        return setXYSeries(
          base,
          { name: '距离(m)', min: 0, max: 3600 },
          { name: '仰角(°)', min: -15, max: 15 },
          s.distX.map((x, i) => [x, s.pitchAngleY[i]] as [number, number])
        )
      }
      if (phase === 'APPROACH') {
        return setXYSeries(
          base,
          { name: '高度(ft)', min: 0, max: 1100, inverse: true },
          { name: '仰角(°)', min: -90, max: 90 },
          s.altX.map((x, i) => [x, s.pitchAngleAltY[i]] as [number, number])
        )
      }
      if (phase === 'TAXI') {
        return setXYSeries(
          base,
          { name: '距离(m)', min: 0, max: 3600 },
          { name: '仰角(°)', min: -15, max: 15 },
          mapTaxiDistToChartPairs(s.distX, s.pitchAngleY) as [number, number][]
        )
      }
      return setXYSeries(
        base,
        { name: '距离(m)', min: 0, max: 3600 },
        { name: '仰角(°)', min: -15, max: 15 },
        s.distX.map((x, i) => [x, s.pitchAngleY[i]] as [number, number])
      )
    }

    const buildSlopeOptionForPhase = (phase: PhaseKey): echarts.EChartsOption => {
      const base = getSlopeChartOption()
      const s = phaseSeries[phase]
      if (phase === 'CLIMB') {
        if (s.altX.length > 0) {
          return setXYSeries(
            base,
            { name: '高度(ft)', min: 0, max: 1100, inverse: false },
            { name: '坡度(°)', min: -45, max: 45 },
            s.altX.map((x, i) => [x, s.slopeAltY[i]] as [number, number])
          )
        }
        return setXYSeries(
          base,
          { name: '距离(m)', min: 0, max: 3600 },
          { name: '坡度(°)', min: -45, max: 45 },
          s.distX.map((x, i) => [x, s.slopeY[i]] as [number, number])
        )
      }
      if (phase === 'APPROACH') {
        return setXYSeries(
          base,
          { name: '高度(ft)', min: 0, max: 1100, inverse: true },
          { name: '坡度(°)', min: -45, max: 45 },
          s.altX.map((x, i) => [x, s.slopeAltY[i]] as [number, number])
        )
      }
      if (phase === 'TAXI') {
        return setXYSeries(
          base,
          { name: '距离(m)', min: 0, max: 3600 },
          { name: '坡度(°)', min: -45, max: 45 },
          mapTaxiDistToChartPairs(s.distX, s.slopeY) as [number, number][]
        )
      }
      return setXYSeries(
        base,
        { name: '距离(m)', min: 0, max: 3600 },
        { name: '坡度(°)', min: -45, max: 45 },
        s.distX.map((x, i) => [x, s.slopeY[i]] as [number, number])
      )
    }

    const buildGsOptionForApproach = (): echarts.EChartsOption => {
      const opt: any = stripTooltip({ ...(getGSChartOption() as any) })
      const s = phaseSeries.APPROACH
      const standardSeries = getGSStandardLineSeries()
      const gsSeriesData = s.gsX.map((x, i) => [x, s.gsY[i]] as [number, number])
      const gsExMp = buildLowAltWarnMarkPoint(gsSeriesData)
      opt.xAxis = { ...(opt.xAxis ?? {}), min: 0, max: 1100, inverse: true }
      opt.series = [
        ...standardSeries.map((ss) => ({
          name: ss.name,
          type: 'line',
          xAxisIndex: 0,
          yAxisIndex: 0,
          data: ss.data,
          lineStyle: ss.lineStyle,
          showSymbol: false
        })),
        {
          name: 'GS偏差',
          type: 'line',
          xAxisIndex: 0,
          yAxisIndex: 0,
          smooth: true,
          showSymbol: false,
          data: gsSeriesData,
          lineStyle: { color: '#38bdf8', width: 2, ...lineCapJoin },
          itemStyle: { color: '#38bdf8' },
          ...(gsExMp ? { markPoint: gsExMp } : {})
        }
      ]
      return opt
    }

    const buildAirspeedOptionForPhase = (phase: PhaseKey): echarts.EChartsOption => {
      const opt: any = stripTooltip({ ...(getAirspeedChartOption() as any) })
      const s = phaseSeries[phase]
      const isAir = phase === 'CLIMB' || phase === 'APPROACH'
      const air = isAir
        ? s.altX.map((x, i) => [x, s.airspeedAltY[i]] as [number, number])
        : phase === 'TAXI'
          ? mapTaxiDistToChartPairs(s.distX, s.airspeedY)
          : s.distX.map((x, i) => [x, s.airspeedY[i]] as [number, number])
      const selected = isAir
        ? s.altX.map((x, i) => [x, s.selectedSpeedAltY[i]] as [number, number])
        : phase === 'TAXI'
          ? mapTaxiDistToChartPairs(s.distX, s.selectedSpeedY)
          : s.distX.map((x, i) => [x, s.selectedSpeedY[i]] as [number, number])
      opt.xAxis = isAir
        ? {
            ...(opt.xAxis ?? {}),
            name: '高度(ft)',
            min: 0,
            max: 1100,
            inverse: phase === 'APPROACH'
          }
        : { ...(opt.xAxis ?? {}), name: '距离(m)', min: 0, max: 3600, inverse: false }
      opt.series = [
        { ...(opt.series?.[0] ?? {}), data: air },
        { ...(opt.series?.[1] ?? {}), data: selected }
      ]
      return opt
    }

    /**
     * Word 导出不能只依赖页面上已缓存的截图：切换阶段过快时，某一阶段（尤其爬升）
     * 的截图队列可能尚未完成。此处从阶段序列离屏重建，确保阶段不会被静默遗漏。
     */
    const buildOffscreenImagesForPhase = async (
      phase: PhaseKey
    ): Promise<PhaseSnapshot['images']> => {
      const images: PhaseSnapshot['images'] = {}
      const render = async (key: keyof PhaseSnapshot['images'], option: echarts.EChartsOption) => {
        const url = await offscreenRender(option)
        if (url) images[key] = url
      }
      // LOC 的标准线、刻度与低高度标记由页面 renderPhaseCharts 统一维护。
      // 不在这里离屏重建，避免 Word 与页面出现两套 LOC 绘制规则。
      await render('airspeed', buildAirspeedOptionForPhase(phase))
      await render('magneticHeading', buildMagHeadingOptionForPhase(phase))
      await render('pitchAngle', buildPitchOptionForPhase(phase))
      await render('slope', buildSlopeOptionForPhase(phase))
      if (phase === 'APPROACH') await render('gs', buildGsOptionForApproach())
      return images
    }

    // 导出前：确保“当前 cycle”至少把缺失阶段快照补齐
    const currentCycle = currentCycleIndex.value
    if (!phaseSnapshotsByCycle.value[currentCycle]) {
      phaseSnapshotsByCycle.value = { ...phaseSnapshotsByCycle.value, [currentCycle]: {} }
    }
    // 每次导出均重新使用页面同一套 renderPhaseCharts 生成阶段截图，避免复用旧截图。
    await capturePhaseSnapshot(activePhaseTab.value, currentCycle)
    for (const p of phaseOrder) {
      if (!hasDataInPhaseSeries(p)) continue
      await capturePhaseSnapshot(p, currentCycle)
    }
    // 仅当页面阶段快照缺失时才离屏重绘非 LOC 图表。LOC 必须来自页面快照，
    // 以保证 Word 与页面完全使用同一套辅助线、刻度和标记规则。
    for (const p of phaseOrder) {
      if (phaseSnapshotsByCycle.value[currentCycle]?.[p]) continue
      if (!hasDataInPhaseSeries(p)) continue
      const images = await buildOffscreenImagesForPhase(p)
      if (Object.keys(images).length === 0) continue
      phaseSnapshotsByCycle.value = {
        ...phaseSnapshotsByCycle.value,
        [currentCycle]: {
          ...(phaseSnapshotsByCycle.value[currentCycle] ?? {}),
          [p]: { phase: p, capturedAt: formatNow(), images }
        }
      }
    }
    if (!cyclesToExport.includes(currentCycle))
      cyclesToExport = [...cyclesToExport, currentCycle].sort((a, b) => a - b)

    const reportCycles: FlightReportCycleData[] = cyclesToExport
      .map((cycle) => {
        const phases: FlightReportCycleData['phases'] = {}
        for (const phase of phaseOrder) {
          const snapshot = phaseSnapshotsByCycle.value[cycle]?.[phase]
          if (!snapshot) continue
          phases[phase] = {
            capturedAt: snapshot.capturedAt,
            images: { ...snapshot.images }
          }
        }
        return { cycle, phases }
      })
      .filter((cycle) => Object.keys(cycle.phases).length > 0)

    if (reportCycles.length === 0) {
      ElMessage.warning(
        '未能生成阶段快照图片（图表实例导出失败）。请确认右侧图表已有曲线后重试；若仍失败，请刷新页面后重新传输再导出。'
      )
      return
    }

    const title = '飞行复现 · 多周期按飞行状态分段导出（阶段快照）'
    exportStep = '生成 DOCX 文档'
    const { blob, fileName } = await createFlightReportDocx({
      title,
      exportedAt: formatNow(),
      hudImage,
      cycles: reportCycles
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
      window.setTimeout(() => URL.revokeObjectURL(url), 0)
    }
    ElMessage.success('导出成功，可用 Word 或 WPS 打开')
  } catch (e) {
    console.error('[flyData] 导出 Word 失败', e)
    const reason = e instanceof Error ? e.message : String(e)
    ElMessage.error(`${exportStep}失败：${reason || '未知错误'}`)
  } finally {
    hudExportItem.value = null
    viewTab.value = originalViewTab
    await nextTick()
    if (originalViewTab !== 'HUD') {
      renderPhaseCharts(originalViewTab)
      resizeCharts()
    } else {
      syncHudItem()
    }
    exportWordLoading.value = false
  }
}

/** WS 高频推送合并为每帧最多一次 updateChart，避免与 Toolbar/ElSelect/Dialog 并发 patch 导致 __vnode */
const wsPendingRows: ChartDataItem[] = []
let wsPendingTotal = 0
let wsChartFlushRafId = 0
/** 为了让折线“连续生长”，每帧最多消费多少条（过大=单帧突变像跳，过小=延迟增大） */
const WS_MAX_ROWS_PER_FRAME = 120

function cancelWsChartFlush() {
  if (wsChartFlushRafId !== 0) {
    cancelAnimationFrame(wsChartFlushRafId)
    wsChartFlushRafId = 0
  }
  wsPendingRows.length = 0
  cancelChartRenderRaf()
  cancelFlightDataRemapRaf()
}

function flushWsChartPending() {
  wsChartFlushRafId = 0
  if (!isComponentMounted.value) {
    wsPendingRows.length = 0
    return
  }
  if (!isRunning.value) {
    wsPendingRows.length = 0
    return
  }
  // 快照过程中暂停增量绘制：保持“当前 cycle 的 phaseSeries 不被新数据污染”
  if (isSnapshottingCharts.value) {
    scheduleWsChartAccumulated()
    return
  }
  if (wsPendingRows.length === 0) return

  // 分片逐帧消费：避免一次喂入几千点导致线条“跳着画”
  const batch = wsPendingRows.splice(0, WS_MAX_ROWS_PER_FRAME)
  const total = wsPendingTotal
  updateChart(batch)
  totalCount.value = total
  if (wsPendingRows.length > 0) {
    scheduleWsChartAccumulated()
  }
}

function scheduleWsChartAccumulated() {
  if (wsChartFlushRafId !== 0) return
  wsChartFlushRafId = requestAnimationFrame(flushWsChartPending)
}

function scheduleWsChartRows(fragment: ChartDataItem[], total: number) {
  // 重置/停止后忽略到达的旧消息，避免“重置后又被补数据”
  if (!isComponentMounted.value || !isRunning.value) return
  wsPendingRows.push(...fragment)
  wsPendingTotal = total
  scheduleWsChartAccumulated()
}

const startConnect = () => {
  if (isRunning.value) {
    ElMessage.warning('正在播放/加载中')
    return
  }
  if (status.value === 'CONNECTING') {
    ElMessage.warning('正在加载数据，请稍候...')
    return
  }
  startConnectInternal()
}

const startConnectInternal = async () => {
  if (!isComponentMounted.value) return
  if (!qarId.value) {
    ElMessage.warning('必须选择一条 QAR 数据才能开始播放')
    return
  }

  status.value = 'CONNECTING'
  isRunning.value = true
  isPaused.value = false

  // 重置跑道起点位置，以便重新设置
  runwayStartPosition.value = undefined
  // 重置实时动画数据与 3D 累积数据
  realtimeFlightData.value = null
  realtimeCurrentIndex.value = 0
  currentCycleIndex.value = 1
  accumulatedTaxiPhaseData.value = []
  phaseSnapshots.value = {}
  phaseSnapshotsByCycle.value = {}
  firstApproachIdLogged = false
  cancelWsChartFlush()
  clearChart()

  try {
    ElMessage.info('正在请求架次复现数据...')
    const resp = await request.post({
      url: '/efvs/qar/taxi-phase/play',
      params: { id: qarId.value, startId: 0 }
    })

    // 兼容：request 返回值可能是 CommonResult 或其直接 data 字段
    const respData = resp?.data ?? resp
    const content = respData?.content ?? respData?.data?.content
    const msg = typeof content === 'string' ? JSON.parse(content) : content

    const items = (msg?.data ?? []) as ChartDataItem[]
    const total = msg?.totalCount ?? items.length
    const touchdownXOnRunwayMFromBackend =
      msg?.touchdownXOnRunwayM != null && Number.isFinite(Number(msg?.touchdownXOnRunwayM))
        ? Number(msg.touchdownXOnRunwayM)
        : null

    if (!items.length) {
      ElMessage.warning('未加载到 taxi-phase 数据')
      status.value = 'OPEN'
      isRunning.value = false
      return
    }

    ElMessage.info(`正在准备 ${total} 条飞参数据...`)
    applyLoadedTaxiPhaseData(items, total, touchdownXOnRunwayMFromBackend)

    try {
      // eslint-disable-next-line no-console
      console.log('[landing] touchdownXOnRunwayM:', realtimeFlightData.value?.touchdownXOnRunwayM)
    } catch {}

    // 自动从开头播放
    accum.value = 0
    lastTick.value = performance.now()
    playing.value = true
    loop()

    status.value = 'OPEN'
    ElMessage.success(`数据已就绪，正在从起点播放（${total} 条）`)
  } catch (e) {
    logError(e)
    status.value = 'ERROR'
    isRunning.value = false
    isPaused.value = false
    ElMessage.error('加载数据失败，请重试')
  }
}

const logError = (e: any) => {
  try {
    console.error('[flyData] 加载 taxi-phase 失败', e)
  } catch {}
}

const stopConnect = () => {
  if (!isRunning.value) return
  status.value = 'CLOSED'
  isRunning.value = false
  isPaused.value = false
  playing.value = false

  // 清空实时动画数据与 3D 累积数据
  realtimeFlightData.value = null
  realtimeCurrentIndex.value = 0
  chartAppliedUpToIndex.value = -1
  accumulatedTaxiPhaseData.value = []
  hudItem.value = null
  cancelChartRenderRaf()
  cancelFlightDataRemapRaf()
  ElMessage.info('已停止播放')
}

const togglePause = () => {
  if (isPaused.value) {
    isPaused.value = false
    ElMessage.success('已恢复播放')
    if (!playing.value && realtimeFlightData.value) {
      lastTick.value = performance.now()
      playing.value = true
      loop()
    }
  } else {
    isPaused.value = true
    ElMessage.info('已暂停播放')
    playing.value = false
  }
}

/** 是否处于输入态（避免空格快捷键影响输入框） */
const isTypingTarget = (e: KeyboardEvent): boolean => {
  const t = e.target as HTMLElement | null
  if (!t) return false
  const tag = (t.tagName || '').toLowerCase()
  if (tag === 'input' || tag === 'textarea' || tag === 'select') return true
  if ((t as any).isContentEditable) return true
  return false
}

/** 键盘快捷键：空格播放/暂停，左右方向键逐帧，F 跟随飞机，R 重新取景。 */
const onKeydown = (e: KeyboardEvent) => {
  if (!isComponentMounted.value) return
  if (isTypingTarget(e)) return
  if (e.code === 'KeyF') {
    e.preventDefault()
    threeSceneRef.value?.setViewMode?.('follow')
    return
  }
  if (e.code === 'KeyR') {
    e.preventDefault()
    threeSceneRef.value?.reframeCamera?.()
    return
  }
  if (e.code === 'ArrowLeft' || e.code === 'ArrowRight') {
    if (!realtimeFlightData.value) return
    e.preventDefault()
    stepFrame(e.code === 'ArrowLeft' ? -1 : 1)
    return
  }
  if (e.code !== 'Space') return
  // 避免页面滚动
  e.preventDefault()
  // 已在传输：空格切换暂停/恢复
  if (isRunning.value && isConnected.value) {
    togglePause()
    return
  }
  // 未传输：切换本地播放（如后续启用播放按钮/回放）
  togglePlay()
}

// REST 模式：无需 WebSocket 监听推送

onMounted(async () => {
  isComponentMounted.value = true
  syncFullScreenState()
  document.addEventListener('fullscreenchange', syncFullScreenState)
  await initData()
  if (realtimeFlightData.value) {
    updateAll()
  }
  ;(window as any).app = { realtimeFlightData }
  window.addEventListener('keydown', onKeydown, { passive: false })
  nextTick(() => {
    setTimeout(() => {
      if (!isComponentMounted.value) return
      initChart()
      resizeCharts()
    }, 200)
  })
})

onBeforeUnmount(() => {
  isComponentMounted.value = false
  document.removeEventListener('fullscreenchange', syncFullScreenState)
  cancelWsChartFlush()
  cancelFlightDataRemapRaf()
  playing.value = false
  isRunning.value = false
  isPaused.value = false
  window.removeEventListener('keydown', onKeydown)
  status.value = 'CLOSED'
  if (resizeChartsListenerAttached) {
    window.removeEventListener('resize', resizeCharts)
    resizeChartsListenerAttached = false
  }
  ;[
    chartInstance,
    airspeedChartInstance,
    pitchAngleChartInstance,
    slopeChartInstance,
    gsChartInstance,
    magneticHeadingChartInstance
  ].forEach((c) => c?.dispose())
})
</script>

<style scoped>
.app-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  min-height: 0;
}

.app-container--embedded {
  flex: 1;
  min-height: 0;
  height: 100%;
  max-height: 100%;
}

.toolbar {
  z-index: 10;
  display: flex;
  padding: 10px 20px;
  background: #14172a;
  border-bottom: 1px solid #23263a;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.ff-btn {
  --el-button-text-color: #e2e8f0;
  --el-button-bg-color: #1f2937;
  --el-button-border-color: #334155;
  --el-button-hover-text-color: #ffffff;
  --el-button-hover-bg-color: #243244;
  --el-button-hover-border-color: #475569;
  --el-button-active-text-color: #ffffff;
  --el-button-active-bg-color: #243244;
  --el-button-active-border-color: #60a5fa;
  --el-button-disabled-text-color: rgba(226, 232, 240, 0.45);
  --el-button-disabled-bg-color: rgba(31, 41, 55, 0.6);
  --el-button-disabled-border-color: rgba(51, 65, 85, 0.6);

  height: 30px;
  padding: 0 12px;
  border-radius: 6px;
  font-weight: 600;
  letter-spacing: 0.2px;
}

.ff-btn--primary {
  --el-button-bg-color: #0ea5e9;
  --el-button-border-color: #38bdf8;
  --el-button-hover-bg-color: #0284c7;
  --el-button-hover-border-color: #38bdf8;
  --el-button-active-bg-color: #0369a1;
  --el-button-active-border-color: #7dd3fc;
}

.ff-btn--danger {
  --el-button-bg-color: #ef4444;
  --el-button-border-color: #f87171;
  --el-button-hover-bg-color: #dc2626;
  --el-button-hover-border-color: #f87171;
  --el-button-active-bg-color: #b91c1c;
  --el-button-active-border-color: #fca5a5;
}

.ff-btn--success {
  --el-button-bg-color: #10b981;
  --el-button-border-color: #34d399;
  --el-button-hover-bg-color: #059669;
  --el-button-hover-border-color: #34d399;
  --el-button-active-bg-color: #047857;
  --el-button-active-border-color: #6ee7b7;
}

.ff-btn--warning {
  --el-button-bg-color: #f59e0b;
  --el-button-border-color: #fbbf24;
  --el-button-hover-bg-color: #d97706;
  --el-button-hover-border-color: #fbbf24;
  --el-button-active-bg-color: #b45309;
  --el-button-active-border-color: #fde68a;
}

.ff-btn--purple {
  --el-button-bg-color: #8b5cf6;
  --el-button-border-color: #a78bfa;
  --el-button-hover-bg-color: #7c3aed;
  --el-button-hover-border-color: #a78bfa;
  --el-button-active-bg-color: #6d28d9;
  --el-button-active-border-color: #c4b5fd;
}

.ff-btn--muted {
  --el-button-bg-color: #475569;
  --el-button-border-color: #64748b;
  --el-button-hover-bg-color: #334155;
  --el-button-hover-border-color: #94a3b8;
  --el-button-active-bg-color: #1f2937;
  --el-button-active-border-color: #94a3b8;
}

.ff-slider {
  width: 260px;
  --el-slider-main-bg-color: #38bdf8;
  --el-slider-runway-bg-color: #334155;
}

.app-container:fullscreen {
  width: 100vw;
  height: 100vh;
  background: #0a0d14;
}

.app-container:fullscreen .toolbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 10000;
  pointer-events: auto;
}

.app-container:fullscreen .content {
  padding-top: 56px;
}

.toolbar .label {
  margin-left: 6px;
  font-size: 12px;
  color: #94a3b8;
}

.toolbar .phase-label {
  margin-left: 10px;
  font-weight: 600;
  color: #38bdf8;
}

.toolbar-divider {
  margin: 0 4px;
  font-size: 12px;
  color: #475569;
}

.content {
  display: grid;
  min-height: 0;
  padding: 10px;
  grid-template-columns: 1.2fr 1fr;
  gap: 10px;
  flex: 1;
}

.panel {
  display: flex;
  min-height: 0;
  background: #14172a;
  border: 1px solid #23263a;
  border-radius: 8px;
  flex-direction: column;
}

.panel.chart-panel {
  min-height: 0;
}

.panel-header {
  display: flex;
  padding: 10px 15px;
  font-size: 13px;
  font-weight: bold;
  color: #94a3b8;
  border-bottom: 1px solid #23263a;
  justify-content: space-between;
  flex-shrink: 0;
}

.panel-header--with-tabs {
  align-items: center;
  gap: 12px;
}

.panel-header--stacked {
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
}

.panel-header-hint {
  font-size: 11px;
  font-weight: normal;
  color: #ca8a04;
}

.panel-body.hud-body {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 8px 12px;
  overflow: auto;
  background: #000;
}

.phase-tabs {
  --el-tabs-header-height: 26px;
}

.phase-tabs :deep(.el-tabs__header) {
  margin: 0;
}

/* 去掉 Element 默认的滑动下划线与底部灰线，选中态改用实心色块表达 */
.phase-tabs :deep(.el-tabs__active-bar),
.phase-tabs :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.phase-tabs :deep(.el-tabs__nav) {
  gap: 6px;
}

.phase-tabs :deep(.el-tabs__item) {
  height: 26px;
  padding: 0 12px;
  font-size: 13px;
  font-weight: 600;
  color: #94a3b8;
  background: #1b1f33;
  border: 1px solid #2b2f45;
  border-radius: 6px;
  transition:
    color 0.15s,
    background-color 0.15s,
    border-color 0.15s;
}

.phase-tabs :deep(.el-tabs__item:hover) {
  color: #cbd5e1;
  background: #232744;
  border-color: #3b4260;
}

.phase-tabs :deep(.el-tabs__item.is-active) {
  color: #0f172a;
  font-weight: 700;
  background: #0ea5e9;
  border-color: #0ea5e9;
}

.panel-body {
  flex: 1;
  position: relative;
  min-height: 0;
}

.panel-body.charts-scroll {
  display: flex;
  padding: 8px 12px 8px 8px;
  overflow: hidden auto;
  flex-direction: column;
  gap: 10px;
}

.chart-block {
  display: flex;
  min-height: 460px;
  padding: 6px 8px 8px;
  background: #1e293b;
  border: 1px solid #334155;
  border-radius: 6px;
  box-sizing: border-box;
  flex-shrink: 0;
  flex-direction: column;
}

.chart-title {
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: bold;
  letter-spacing: 0.5px;
  color: #e2e8f0;
  text-align: center;
}

.chart-container {
  width: 100%;
  min-height: 380px;
  flex: 1;
}

.status {
  margin-left: 10px;
  font-family: monospace;
  font-size: 14px;
  color: #fbbf24;
}

.three-container:fullscreen {
  width: 100vw;
  height: 100vh;
}
</style>
