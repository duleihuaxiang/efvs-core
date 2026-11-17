<template>
  <div class="hud-display">
    <div ref="stageRef" class="hud-stage">
      <img ref="backgroundImageRef" class="hud-bg" :src="hudReference" alt="HUD" />
      <svg
        ref="overlayRef"
        class="hud-overlay"
        :viewBox="`0 0 ${HUD_W} ${HUD_H}`"
        preserveAspectRatio="none"
        :class="{ 'is-idle': !hasData }"
      >
        <!-- ============ A. 左：空速带 ============ -->
        <g v-if="speed !== null" class="hud-layer">
          <rect x="24" y="208" width="154" height="358" fill="#000" />
          <!-- 带面刻度梯 -->
          <line x1="118" y1="212" x2="118" y2="558" :stroke="GREEN" stroke-width="2" />
          <line x1="133" y1="212" x2="133" y2="558" :stroke="GREEN" stroke-width="2" />
          <line
            v-for="t in speedRungs"
            :key="'r' + t.v"
            x1="104"
            :y1="t.y"
            x2="133"
            :y2="t.y"
            :stroke="GREEN"
            stroke-width="2"
          />
          <!-- 刻度数字 -->
          <text
            v-for="t in speedLabels"
            :key="'s' + t.v"
            x="99"
            :y="t.y"
            text-anchor="end"
            dominant-baseline="central"
            class="hud-tape-num"
          >
            {{ t.v }}
          </text>
          <!-- 参考线/标记：原图有值但无对应数据，按占位重绘 -->
          <text x="152" y="318" dominant-baseline="central" class="hud-small-num">2</text>
          <text x="150" y="346" dominant-baseline="central" class="hud-small-num">R</text>
          <text x="150" y="374" dominant-baseline="central" class="hud-small-num">1</text>
          <text x="134" y="430" dominant-baseline="central" class="hud-small-num">REF</text>
          <!-- 当前空速框 -->
          <rect
            x="30"
            :y="SPEED_REF_Y - 30"
            width="96"
            height="60"
            fill="#000"
            :stroke="GREEN"
            stroke-width="2"
          />
          <text
            x="78"
            :y="SPEED_REF_Y"
            text-anchor="middle"
            dominant-baseline="central"
            class="hud-readout"
          >
            {{ speed.toFixed(0) }}
          </text>
        </g>

        <!-- ============ B. 左：选择速度 ============ -->
        <g v-if="selectedSpeed !== null" class="hud-layer">
          <rect x="60" y="178" width="68" height="40" fill="#000" />
          <text
            x="94"
            y="198"
            text-anchor="middle"
            dominant-baseline="central"
            class="hud-tape-num"
          >
            {{ selectedSpeed.toFixed(0) }}
          </text>
        </g>

        <!-- ============ C. 中：地平线与俯仰 ============ -->
        <g v-if="pitchAngle !== null" class="hud-layer">
          <rect x="166" y="264" width="580" height="76" fill="#000" />
          <g :transform="`translate(0 ${horizonDy})`">
            <line x1="173" y1="305" x2="382" y2="305" :stroke="GREEN" stroke-width="2" />
            <line x1="518" y1="305" x2="733" y2="305" :stroke="GREEN" stroke-width="2" />
            <line x1="340" y1="296" x2="340" y2="314" :stroke="GREEN" stroke-width="2" />
            <line x1="645" y1="296" x2="645" y2="314" :stroke="GREEN" stroke-width="2" />
            <path d="M441 302 L453 286 L465 302 Z" fill="none" :stroke="GREEN" stroke-width="2" />
            <text x="181" y="290" dominant-baseline="central" class="hud-tape-num">35</text>
            <text
              x="493"
              y="290"
              text-anchor="middle"
              dominant-baseline="central"
              class="hud-tape-num"
            >
              N
            </text>
          </g>
        </g>

        <!-- ============ D. 中：航道/下滑偏差 ============ -->
        <g v-if="locDeviation !== null" class="hud-layer">
          <rect x="312" y="482" width="88" height="38" fill="#000" />
          <text
            x="356"
            y="501"
            text-anchor="middle"
            dominant-baseline="central"
            class="hud-small-num"
          >
            {{ locText }}
          </text>
        </g>
        <g v-if="glideDeviation !== null" class="hud-layer">
          <rect x="500" y="482" width="88" height="38" fill="#000" />
          <text
            x="544"
            y="501"
            text-anchor="middle"
            dominant-baseline="central"
            class="hud-small-num"
          >
            {{ glideText }}
          </text>
        </g>

        <!-- ============ E. 中：无线电高度 ============ -->
        <g v-if="radioAltitude !== null" class="hud-layer">
          <rect x="404" y="470" width="92" height="48" fill="#000" />
          <text
            x="450"
            y="494"
            text-anchor="middle"
            dominant-baseline="central"
            class="hud-readout"
          >
            {{ radioAltitude.toFixed(0) }}
          </text>
        </g>

        <!-- ============ F. 中：磁航向读数 ============ -->
        <g v-if="heading !== null" class="hud-layer">
          <rect x="416" y="141" width="82" height="38" fill="#000" />
          <text
            x="457"
            y="160"
            text-anchor="middle"
            dominant-baseline="central"
            class="hud-tape-num"
          >
            {{ headingText }}
          </text>
        </g>

        <!-- ============ G. 右：高度带 ============ -->
        <g v-if="radioAltitude !== null" class="hud-layer">
          <rect x="700" y="210" width="208" height="376" fill="#000" />
          <line x1="767" y1="210" x2="767" y2="556" :stroke="GREEN" stroke-width="2" />
          <line
            v-for="t in altRungs"
            :key="'ar' + t.v"
            x1="767"
            :y1="t.y"
            x2="784"
            :y2="t.y"
            :stroke="GREEN"
            stroke-width="2"
          />
          <text
            v-for="t in altLabels"
            :key="'a' + t.v"
            x="894"
            :y="t.y"
            text-anchor="end"
            dominant-baseline="central"
            class="hud-tape-num"
          >
            {{ t.v }}
          </text>
          <!-- 当前高度框（米 / 英尺） -->
          <rect
            x="800"
            y="330"
            width="108"
            height="80"
            fill="#000"
            :stroke="GREEN"
            stroke-width="2"
          />
          <text
            x="854"
            y="352"
            text-anchor="middle"
            dominant-baseline="central"
            class="hud-small-num"
          >
            {{ altitudeMetresText }}
          </text>
          <text
            x="854"
            y="388"
            text-anchor="middle"
            dominant-baseline="central"
            class="hud-readout"
          >
            {{ radioAltitude.toFixed(0) }}
          </text>
          <!-- 静态占位 -->
          <text x="712" y="510" dominant-baseline="central" class="hud-small-num">BARO</text>
          <text x="762" y="564" dominant-baseline="central" class="hud-small-num">
            29.92 HPA QFE
          </text>
        </g>

        <!-- ============ H. 左下：地速 / 距离 ============ -->
        <g v-if="hasData" class="hud-layer">
          <rect x="38" y="554" width="186" height="136" fill="#000" />
          <text x="44" y="577" dominant-baseline="central" class="hud-small-num">M.492</text>
          <text x="120" y="577" dominant-baseline="central" class="hud-small-num">GS</text>
          <text x="210" y="577" text-anchor="end" dominant-baseline="central" class="hud-small-num">
            {{ groundSpeedText }}
          </text>
          <text x="44" y="609" dominant-baseline="central" class="hud-small-num">SEL HDG 338</text>
          <text x="80" y="641" dominant-baseline="central" class="hud-small-num">CRS 338</text>
          <text x="44" y="673" dominant-baseline="central" class="hud-small-num">DME</text>
          <text x="196" y="673" text-anchor="end" dominant-baseline="central" class="hud-small-num">
            {{ distanceText }}
          </text>
          <text x="200" y="673" dominant-baseline="central" class="hud-small-num">H</text>
        </g>

        <!-- ============ I. 下：罗盘 ============ -->
        <g v-if="heading !== null" class="hud-layer">
          <rect x="312" y="552" width="292" height="240" fill="#000" />
          <g :transform="`rotate(${-heading} ${ROSE_CX} ${ROSE_CY})`">
            <circle
              :cx="ROSE_CX"
              :cy="ROSE_CY"
              r="110"
              fill="none"
              :stroke="GREEN"
              stroke-width="2"
            />
            <line
              v-for="t in roseTicks"
              :key="'rt' + t.d"
              :x1="ROSE_CX"
              :y1="ROSE_CY - (t.major ? 110 : 100)"
              :x2="ROSE_CX"
              :y2="ROSE_CY - 110"
              :stroke="GREEN"
              stroke-width="2"
              :transform="`rotate(${t.d} ${ROSE_CX} ${ROSE_CY})`"
            />
            <text
              v-for="t in roseLabels"
              :key="'rl' + t.d"
              :x="ROSE_CX"
              :y="ROSE_CY - 82"
              text-anchor="middle"
              dominant-baseline="central"
              class="hud-small-num"
              :transform="`rotate(${t.d} ${ROSE_CX} ${ROSE_CY})`"
            >
              {{ t.text }}
            </text>
          </g>
          <!-- 固定的航向指标 -->
          <path :d="`M${ROSE_CX - 9} 566 L${ROSE_CX} 580 L${ROSE_CX + 9} 566 Z`" :fill="GREEN" />
        </g>
      </svg>
      <div v-if="!hasData" class="hud-hint"><span>等待飞行数据…</span></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import hudReference from '../assets/hud-reference.png'

/** 底图原始像素尺寸，叠加层与该坐标系 1:1 对应 */
const HUD_W = 950
const HUD_H = 800
const GREEN = '#00e000'

/** 空速带：参考图中 150 kt 位于 y=386，每 kt 2.75 px */
const SPEED_REF_Y = 386
const SPEED_PX_PER_KT = 2.75
/** 高度带：每 ft 0.06 px */
const ALT_PX_PER_FT = 0.06
/** 罗盘中心与半径 */
const ROSE_CX = 452
const ROSE_CY = 672

interface HudChartItem {
  airspeed?: string
  selectedSpeed?: string
  pitchAngle?: string
  magneticHeading?: string
  radioAltitude?: string
  glideslopeDeviationRes?: string
  locDeviation?: string
  ground_speed_km?: string
  cumulativeDistance?: number
}

const props = defineProps<{ item?: HudChartItem | null }>()

const stageRef = ref<HTMLDivElement | null>(null)
const backgroundImageRef = ref<HTMLImageElement | null>(null)
const overlayRef = ref<SVGSVGElement | null>(null)

const waitForBackgroundImage = (): Promise<void> => {
  const image = backgroundImageRef.value
  if (!image) return Promise.reject(new Error('HUD 背景图片元素不存在'))
  if (image.complete) {
    return image.naturalWidth > 0
      ? Promise.resolve()
      : Promise.reject(new Error('HUD 背景图片加载失败'))
  }
  return new Promise<void>((resolve, reject) => {
    const cleanup = () => {
      image.removeEventListener('load', onLoad)
      image.removeEventListener('error', onError)
    }
    const onLoad = () => {
      cleanup()
      resolve()
    }
    const onError = () => {
      cleanup()
      reject(new Error('HUD 背景图片加载失败'))
    }
    image.addEventListener('load', onLoad, { once: true })
    image.addEventListener('error', onError, { once: true })
  })
}

const capturePng = async (): Promise<string> => {
  const stage = stageRef.value
  const background = backgroundImageRef.value
  const overlay = overlayRef.value
  if (!stage) throw new Error('HUD 截图区域尚未渲染')
  if (!background || !overlay) throw new Error('HUD 截图图层尚未渲染')
  if (!hasData.value) throw new Error('HUD 当前没有可显示的飞行参数')
  await waitForBackgroundImage()
  await document.fonts?.ready
  await new Promise<void>((resolve) => requestAnimationFrame(() => resolve()))

  const pixelRatio = 2
  const canvas = document.createElement('canvas')
  canvas.width = HUD_W * pixelRatio
  canvas.height = HUD_H * pixelRatio
  const context = canvas.getContext('2d')
  if (!context) throw new Error('浏览器无法创建 HUD Canvas')
  context.scale(pixelRatio, pixelRatio)
  context.fillStyle = '#000000'
  context.fillRect(0, 0, HUD_W, HUD_H)
  context.drawImage(background, 0, 0, HUD_W, HUD_H)

  // SVG 序列化后不会自动携带 Vue scoped CSS，因此把动态文字样式直接嵌入克隆图层。
  const clonedOverlay = overlay.cloneNode(true) as SVGSVGElement
  clonedOverlay.classList.remove('is-idle')
  clonedOverlay.setAttribute('xmlns', 'http://www.w3.org/2000/svg')
  clonedOverlay.setAttribute('width', String(HUD_W))
  clonedOverlay.setAttribute('height', String(HUD_H))
  const style = document.createElementNS('http://www.w3.org/2000/svg', 'style')
  style.textContent = `
    .hud-layer text { fill: #00e000; font-family: Consolas, 'Courier New', monospace; font-weight: 700; }
    .hud-tape-num { font-size: 28px; }
    .hud-small-num { font-size: 24px; }
    .hud-readout { font-size: 36px; }
  `
  clonedOverlay.insertBefore(style, clonedOverlay.firstChild)

  const svgText = new XMLSerializer().serializeToString(clonedOverlay)
  const svgUrl = URL.createObjectURL(new Blob([svgText], { type: 'image/svg+xml;charset=utf-8' }))
  try {
    const overlayImage = new Image()
    await new Promise<void>((resolve, reject) => {
      overlayImage.onload = () => resolve()
      overlayImage.onerror = () => reject(new Error('HUD 动态 SVG 图层加载失败'))
      overlayImage.src = svgUrl
    })
    context.drawImage(overlayImage, 0, 0, HUD_W, HUD_H)
  } finally {
    URL.revokeObjectURL(svgUrl)
  }

  const dataUrl = canvas.toDataURL('image/png')
  if (!dataUrl.startsWith('data:image/png;base64,')) {
    throw new Error('HUD 截图未生成有效 PNG')
  }
  return dataUrl
}

defineExpose({ capturePng })

const toNum = (v: unknown): number | null => {
  if (v === null || v === undefined || v === '') return null
  const n = typeof v === 'number' ? v : Number.parseFloat(String(v))
  return Number.isFinite(n) ? n : null
}

const hasData = computed(() => props.item != null)
const speed = computed(() => toNum(props.item?.airspeed))
const selectedSpeed = computed(() => toNum(props.item?.selectedSpeed))
const pitchAngle = computed(() => toNum(props.item?.pitchAngle))
const heading = computed(() => toNum(props.item?.magneticHeading))
const radioAltitude = computed(() => toNum(props.item?.radioAltitude))
const locDeviation = computed(() => toNum(props.item?.locDeviation))
const glideDeviation = computed(() => toNum(props.item?.glideslopeDeviationRes))
const groundSpeed = computed(() => toNum(props.item?.ground_speed_km))
const distance = computed(() => toNum(props.item?.cumulativeDistance))

/** 空速带刻线：每 10 kt 一道 */
const speedRungs = computed(() => {
  if (speed.value === null) return []
  const out: { v: number; y: number }[] = []
  const lo = Math.floor((speed.value - 60) / 10) * 10
  for (let v = lo; v <= speed.value + 60; v += 10) {
    const y = SPEED_REF_Y - (v - speed.value) * SPEED_PX_PER_KT
    if (y >= 214 && y <= 556) out.push({ v, y })
  }
  return out
})

/** 空速带数字：每 20 kt 一个 */
const speedLabels = computed(() => {
  if (speed.value === null) return []
  const out: { v: number; y: number }[] = []
  const lo = Math.floor((speed.value - 60) / 20) * 20
  for (let v = lo; v <= speed.value + 60; v += 20) {
    if (v < 0) continue
    const y = SPEED_REF_Y - (v - speed.value) * SPEED_PX_PER_KT
    if (y >= 214 && y <= 556) out.push({ v, y })
  }
  return out
})

/** 高度带：可见范围约 ±2900 ft，每 1000 ft 一个标注、每 200 ft 一道刻线 */
const altVisible = computed(() => {
  if (radioAltitude.value === null) return null
  const refY = 386
  const half = 176
  return {
    refY,
    lo: radioAltitude.value - half / ALT_PX_PER_FT,
    hi: radioAltitude.value + half / ALT_PX_PER_FT
  }
})

const altY = (v: number) => {
  const vis = altVisible.value
  if (!vis) return 0
  return vis.refY - (v - radioAltitude.value!) * ALT_PX_PER_FT
}

const altRungs = computed(() => {
  const vis = altVisible.value
  if (!vis) return []
  const out: { v: number; y: number }[] = []
  const lo = Math.ceil(vis.lo / 200) * 200
  for (let v = lo; v <= vis.hi; v += 200) {
    if (v < 0) continue
    const y = altY(v)
    if (y >= 214 && y <= 552) out.push({ v, y })
  }
  return out
})

const altLabels = computed(() => {
  const vis = altVisible.value
  if (!vis) return []
  const out: { v: number; y: number }[] = []
  const lo = Math.ceil(vis.lo / 1000) * 1000
  for (let v = lo; v <= vis.hi; v += 1000) {
    if (v < 0) continue
    const y = altY(v)
    if (y >= 214 && y <= 552) out.push({ v, y })
  }
  return out
})

/** 高度框内米制值 */
const altitudeMetresText = computed(() =>
  radioAltitude.value === null ? '' : `${Math.round(radioAltitude.value * 0.3048)} M`
)

const headingText = computed(() =>
  heading.value === null ? '' : `${Math.round(heading.value) % 360}T`
)

/** 偏差读数：数值 + 左/右（上/下） */
const locText = computed(() => {
  if (locDeviation.value === null) return ''
  const v = locDeviation.value
  return `${Math.abs(v).toFixed(1)} ${v >= 0 ? 'R' : 'L'}`
})

const glideText = computed(() => {
  if (glideDeviation.value === null) return ''
  const v = glideDeviation.value
  return `${Math.abs(v).toFixed(1)} ${v >= 0 ? 'U' : 'D'}`
})

const groundSpeedText = computed(() =>
  groundSpeed.value === null ? '' : groundSpeed.value.toFixed(0)
)

const distanceText = computed(() => (distance.value === null ? '' : distance.value.toFixed(1)))

/** 俯仰：正俯仰时机头抬起，地平线下移；带宽 76px，超出即钳位 */
const horizonDy = computed(() => {
  if (pitchAngle.value === null) return 0
  return Math.max(-34, Math.min(34, pitchAngle.value * 5))
})

const roseTicks = computed(() => {
  const out: { d: number; major: boolean }[] = []
  for (let d = 0; d < 360; d += 10) out.push({ d, major: d % 30 === 0 })
  return out
})

const CARDINALS: Record<number, string> = { 0: 'N', 90: 'E', 180: 'S', 270: 'W' }

const roseLabels = computed(() => {
  const out: { d: number; text: string }[] = []
  for (let d = 0; d < 360; d += 30) {
    out.push({ d, text: CARDINALS[d] ?? String(d / 10) })
  }
  return out
})
</script>

<style lang="scss" scoped>
.hud-display {
  display: flex;
  justify-content: center;
  width: 100%;
}

.hud-stage {
  position: relative;
  width: 100%;
  max-width: 950px;
  aspect-ratio: 950 / 800;
  background: #000;
  overflow: hidden;
}

.hud-bg {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: fill;
}

.hud-overlay {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;

  &.is-idle {
    display: none;
  }
}

.hud-layer text {
  fill: #00e000;
  font-family: Consolas, 'Courier New', monospace;
  font-weight: 700;
}

.hud-tape-num {
  font-size: 28px;
}

.hud-small-num {
  font-size: 24px;
}

.hud-readout {
  font-size: 36px;
}

.hud-hint {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;

  span {
    padding: 6px 18px;
    color: #00e000;
    font-size: 15px;
    letter-spacing: 2px;
    background: rgb(0 0 0 / 80%);
    border: 1px solid rgb(0 224 0 / 40%);
    border-radius: 4px;
  }
}
</style>
