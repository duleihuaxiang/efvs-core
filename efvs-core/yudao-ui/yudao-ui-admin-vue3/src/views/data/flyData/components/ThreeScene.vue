<template>
  <div ref="containerRef" class="three-container"></div>
  <div class="view-buttons">
    <span class="view-label">视角</span>
    <button :class="{ active: currentViewMode === 'free' }" @click="setViewMode('free')">
      自由观察
    </button>
    <button :class="{ active: currentViewMode === 'follow' }" @click="setViewMode('follow')">
      跟随飞机
    </button>
    <button :class="{ active: currentViewMode === 'side' }" @click="setViewMode('side')">
      侧视角
    </button>
    <button :class="{ active: currentViewMode === 'top' }" @click="setViewMode('top')">
      俯视角
    </button>
    <button class="view-reset" @click="reframeCamera">重新取景</button>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as THREE from 'three'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js'
import { GLTFLoader } from 'three/examples/jsm/loaders/GLTFLoader.js'
import type { FlightData, ThreeController, Position, AttitudeDeg } from '../types'

const props = defineProps<{
  data: FlightData
  /** 跑道起点位置：经度和纬度 */
  runwayStartPosition?: { longitude: number; latitude: number }
  /** 当前飞行阶段：TAKEOFF_ROLL-滑跑阶段, CLIMB-爬升阶段, APPROACH-进近状态, TAXI-滑行状态 */
  currentFlightPhase?: string
}>()

const containerRef = ref<HTMLDivElement | null>(null)
let scene: THREE.Scene | null = null
let camera: THREE.PerspectiveCamera | null = null
let renderer: THREE.WebGLRenderer | null = null
let controls: OrbitControls | null = null
let group: THREE.Group | null = null
let stopped = false

// 视角模式: 'free' 自由视角, 'side' 侧视角, 'top' 俯视角
type ViewMode = 'free' | 'follow' | 'side' | 'top'
const currentViewMode = ref<ViewMode>('free')

/** updateData：飞机对齐到哪一帧；默认最后一帧（流式追加）；全量 REST 加载应传 0 以便首次构图在起点后上方 */
type UpdateFlightDataOptions = {
  planeIndex?: number
  /**
   * 父组件刚写入的跑道起点（与 props 同帧时子组件 props 可能尚未更新）。
   * 必须先平移 mainGroup，再 setIndex 构图，否则飞机会在随后 watch 里被移走而镜头不动 → 黑屏/空景。
   */
  runwayStartPosition?: { longitude: number; latitude: number }
}

const resetControlsResidual = () => {
  if (!controls) return
  // 清零阻尼/平移残留，否则会“飞”到之前拖动的方向
  const c = controls as any
  if (c._sphericalDelta?.set) c._sphericalDelta.set(0, 0, 0)
  if (c._panOffset?.set) c._panOffset.set(0, 0, 0)
}

/**
 * 自由视角：左键旋转、滚轮/中键缩放、右键平移。
 * lockPlane=true（未把目标每帧绑在飞机上）时可平移；lockPlane=false 时每帧写入 target，与平移冲突故关闭平移。
 */
const syncFreeViewPanEnabled = () => {
  if (!controls) return
  controls.enablePan = currentViewMode.value === 'free'
}

const setViewMode = (mode: ViewMode) => {
  if (currentViewMode.value === mode) return
  currentViewMode.value = mode
  followSmoothingReady = false
  if (!controls || !group || !camera) return
  group.getWorldPosition(planeWorldPos)
  smoothPlanePos.copy(planeWorldPos)
  followSmoothingReady = true
  camera.up.set(0, 1, 0)
  if (mode === 'free') {
    controls.enabled = true
    controls.target.copy(planeWorldPos)
  } else if (mode === 'follow') {
    controls.enabled = true
    followCameraOffset.copy(camera.position).sub(planeWorldPos)
    if (followCameraOffset.lengthSq() < 1) followCameraOffset.set(-DEFAULT_CAM_BACK, DEFAULT_CAM_UP, 0)
    controls.target.copy(planeWorldPos)
  } else {
    controls.enabled = false
  }
  resetControlsResidual()
  syncFreeViewPanEnabled()
  controls.update()
}

const reframeCamera = () => {
  if (!controls || !camera || !group) return
  group.getWorldPosition(planeWorldPos)
  camera.up.set(0, 1, 0)
  followCameraOffset.set(-DEFAULT_CAM_BACK, DEFAULT_CAM_UP, 0)
  camera.position.copy(planeWorldPos).add(followCameraOffset)
  controls.target.copy(planeWorldPos)
  resetControlsResidual()
  controls.update()
}

const deg2rad = (d: number): number => {
  return (d * Math.PI) / 180
}

const sanitizeMaterial = (mat: THREE.Material): THREE.MeshStandardMaterial => {
  if (mat instanceof THREE.MeshStandardMaterial) {
    return mat
  }

  const safe = new THREE.MeshStandardMaterial({
    color: (mat as any)?.color ?? new THREE.Color(0xcccccc),
    map: (mat as any)?.map ?? null,
    normalMap: (mat as any)?.normalMap ?? null,
    roughnessMap: (mat as any)?.roughnessMap ?? null,
    metalnessMap: (mat as any)?.metalnessMap ?? null,
    emissiveMap: (mat as any)?.emissiveMap ?? null,
    transparent: (mat as any)?.transparent ?? false,
    opacity: (mat as any)?.opacity ?? 1.0
  })

  return safe
}
// 基于飞机运动轨迹计算其水平方向（偏航角）
const computeYawFromPath = (p0: Position, p1: Position): number => {
  const dx = p1.x - p0.x
  const dz = p1.z - p0.z
  return Math.atan2(dx, dz)
}

const MODEL_YAW_OFFSET = 0

/** 与场景跑道几何一致 */
const RUNWAY_LENGTH_SCENE = 3600
/** 下滑道 GS 台：距跑道头（局部 X=0）3300m，与 flyDataIndex 中 GS_STATION_X_FROM_RUNWAY_HEAD_M 一致 */
const GS_STATION_X_SCENE = 3300

/** 自由视角：默认在机身后上方；Orbit 目标为飞机，与「固定跑道点」的参考 demo 场景不同 */
const DEFAULT_CAM_BACK = 240
const DEFAULT_CAM_UP = 140
const MIN_ORBIT_DISTANCE = 45
const MAX_ORBIT_DISTANCE = 1200

/** 已存在的 Line 上直接 setFromPoints 在点数变多时缓冲区不够，Three 会警告；换新几何体并 dispose 旧几何体 */
const replaceLineGeometryFromPoints = (line: THREE.Line, points: THREE.Vector3[]) => {
  const prev = line.geometry
  line.geometry = new THREE.BufferGeometry().setFromPoints(points)
  prev.dispose()
}

/** 轨迹线仅用于视觉：万级以上顶点会拖慢 Buffer 上传与每帧 draw；抽稀保留首尾 */
const TRAJECTORY_LINE_MAX_POINTS = 12000
function decimateVector3Path(points: THREE.Vector3[], maxPoints: number): THREE.Vector3[] {
  if (points.length <= maxPoints) return points
  const n = points.length
  const step = Math.max(1, Math.ceil((n - 1) / (maxPoints - 1)))
  const out: THREE.Vector3[] = []
  for (let i = 0; i < n; i += step) {
    out.push(points[i])
  }
  const last = points[n - 1]
  const oLast = out[out.length - 1]
  if (oLast.x !== last.x || oLast.y !== last.y || oLast.z !== last.z) {
    out.push(last)
  }
  return out
}

function lonLatToSceneOffset(longitude: number, latitude: number): { ox: number; oz: number } {
  const metersPerDegreeLng = 111320 * Math.cos((latitude * Math.PI) / 180)
  const metersPerDegreeLat = 111320
  const scaleFactor = 0.01
  return {
    ox: longitude * metersPerDegreeLng * scaleFactor,
    oz: latitude * metersPerDegreeLat * scaleFactor
  }
}

/** 将跑道起点经纬度同步到 mainGroup（与 runway watch、updateData 共用） */
function applyMainGroupFromRunwayPosition(pos: { longitude: number; latitude: number } | null | undefined): void {
  if (!mainGroup) return
  if (
    !pos ||
    !Number.isFinite(pos.longitude) ||
    !Number.isFinite(pos.latitude)
  ) {
    mainGroup.position.set(0, 0, 0)
    currentRunwayOffsetX = 0
    currentRunwayOffsetZ = 0
    return
  }
  const { ox, oz } = lonLatToSceneOffset(pos.longitude, pos.latitude)
  currentRunwayOffsetX = ox
  currentRunwayOffsetZ = oz
  mainGroup.position.set(ox, 0, oz)
}

const initThree = (container: HTMLDivElement, data: FlightData): ThreeController => {
  scene = new THREE.Scene()
  scene!.background = new THREE.Color(0x0a0d14)

  const initRunway = props.runwayStartPosition
  const initRo = initRunway ? lonLatToSceneOffset(initRunway.longitude, initRunway.latitude) : { ox: 0, oz: 0 }
  const runwayOffsetX = initRo.ox
  const runwayOffsetZ = initRo.oz
  if (initRunway) {
    currentRunwayOffsetX = runwayOffsetX
    currentRunwayOffsetZ = runwayOffsetZ
  } else {
    currentRunwayOffsetX = 0
    currentRunwayOffsetZ = 0
  }

  camera = new THREE.PerspectiveCamera(
    60,
    container.clientWidth / container.clientHeight,
    0.1,
    20000
  )
  // 占位；首帧有飞行数据后在 setIndex 用机身后上方对准（参考 demo 的固定世界点不适合本场景）
  camera.position.set(runwayOffsetX - 200, 1050, runwayOffsetZ + 180)

  renderer = new THREE.WebGLRenderer({ antialias: true })
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
  renderer.setSize(container.clientWidth, container.clientHeight)

  renderer.outputColorSpace = THREE.SRGBColorSpace
  ;(renderer as any).physicallyCorrectLights = true
  renderer.toneMapping = THREE.ACESFilmicToneMapping
  renderer.toneMappingExposure = 1.0
  renderer.localClippingEnabled = true

  container.appendChild(renderer.domElement)

  renderer.domElement.addEventListener('contextmenu', (e) => e.preventDefault())

  controls = new OrbitControls(camera, renderer.domElement)
  controls.enableDamping = true
  controls.dampingFactor = 0.1
  controls.screenSpacePanning = true
  controls.minDistance = MIN_ORBIT_DISTANCE
  controls.maxDistance = MAX_ORBIT_DISTANCE
  controls.mouseButtons = {
    LEFT: THREE.MOUSE.ROTATE,
    MIDDLE: THREE.MOUSE.DOLLY,
    RIGHT: THREE.MOUSE.PAN
  }
  controls.touches = {
    ONE: THREE.TOUCH.ROTATE,
    TWO: THREE.TOUCH.DOLLY_PAN
  }
  controls.target.set(runwayOffsetX, 900, runwayOffsetZ)
  controls.addEventListener('start', () => {
    isOrbitInteracting = true
  })
  controls.addEventListener('end', () => {
    isOrbitInteracting = false
  })
  syncFreeViewPanEnabled()

  scene!.add(new THREE.AmbientLight(0xffffff, 0.6))

  const sunLight = new THREE.DirectionalLight(0xffffff, 1.2)
  sunLight.position.set(500, 1000, 500)
  scene!.add(sunLight)

  // 添加跑道
  const runwayWidth = 60
  const runwayLength = RUNWAY_LENGTH_SCENE
  const grassWidth = 200 // 跑道两侧草地宽度

  // 主场景组：跑道+飞机+轨迹线同组，统一做位置偏移和磁航向旋转，保证飞机始终在跑道上
  mainGroup = new THREE.Group()
  applyMainGroupFromRunwayPosition(initRunway ?? undefined)
  scene!.add(mainGroup)

  // 跑道组 - 跑道起点在局部原点，仅负责跑道几何
  runwayGroup = new THREE.Group()
  runwayGroup.position.set(0, 0, 0)
  mainGroup.add(runwayGroup)

  // 草地背景 - 左右两侧
  const grassGeometry = new THREE.BoxGeometry(runwayLength, 0.1, grassWidth)
  const grassMaterial = new THREE.MeshStandardMaterial({
    color: 0x228b22, // 深绿色草地
    roughness: 0.9
    // metalness: 0.5,
  })

  // 左侧草地
  const grassLeft = new THREE.Mesh(grassGeometry, grassMaterial)
  grassLeft.position.set(
    runwayLength / 2, // 跑道起点在x=0，左侧草地在跑道左侧
    899.9,
    -runwayWidth / 2 - grassWidth / 2
  )
  runwayGroup.add(grassLeft)

  // 右侧草地
  const grassRight = new THREE.Mesh(grassGeometry, grassMaterial)
  grassRight.position.set(
    runwayLength / 2, // 跑道起点在x=0，右侧草地在跑道右侧
    899.9,
    runwayWidth / 2 + grassWidth / 2
  )
  runwayGroup.add(grassRight)

  // 跑道表面 - 灰色跑道
  const runwayGeometry = new THREE.BoxGeometry(runwayLength, 0.2, runwayWidth)
  const runwayMaterial = new THREE.MeshStandardMaterial({
    color: 0x808080, // 灰色跑道表面
    roughness: 0.85,
    metalness: 0.15
  })
  const runway = new THREE.Mesh(runwayGeometry, runwayMaterial)
  runway.position.set(runwayLength / 2, 900, 0) // 跑道从x=0开始，长度3000
  runwayGroup.add(runway)

  // 跑道中线 - 实线
  const centerLineGeometry = new THREE.BoxGeometry(runwayLength, 0.3, 0.3)
  const centerLineMaterial = new THREE.MeshStandardMaterial({
    color: 0xffffff,
    emissive: 0xffffcc,
    emissiveIntensity: 0.2
  })
  const centerLine = new THREE.Mesh(centerLineGeometry, centerLineMaterial)
  centerLine.position.set(runwayLength / 2, 900, 0)
  runwayGroup.add(centerLine)

  // 跑道边缘线 - 黄色
  const edgeLineGeometry = new THREE.BoxGeometry(runwayLength, 0.3, 0.3)
  const edgeLineMaterial = new THREE.MeshStandardMaterial({
    color: 0xffff00,
    emissive: 0xffff00,
    emissiveIntensity: 0.3
  })
  const edgeLineLeft = new THREE.Mesh(edgeLineGeometry, edgeLineMaterial)
  edgeLineLeft.position.set(runwayLength / 2, 901, -runwayWidth / 2 + 1)
  runwayGroup.add(edgeLineLeft)
  const edgeLineRight = new THREE.Mesh(edgeLineGeometry, edgeLineMaterial)
  edgeLineRight.position.set(runwayLength / 2, 901, runwayWidth / 2 - 1)
  runwayGroup.add(edgeLineRight)

  // GS 下滑台位置示意：纵轴 X=3300m（与 Processor 一致），放在跑道左侧草地上，不在跑道中心线上
  const gsZOnGrass = -runwayWidth / 2 - grassWidth * 0.35
  const gsMarkerGeometry = new THREE.BoxGeometry(10, 6, 10)
  const gsMarkerMaterial = new THREE.MeshStandardMaterial({
    color: 0xff8800,
    emissive: 0xaa5500,
    emissiveIntensity: 0.25
  })
  const gsMarker = new THREE.Mesh(gsMarkerGeometry, gsMarkerMaterial)
  gsMarker.position.set(GS_STATION_X_SCENE, 900.2, gsZOnGrass)
  runwayGroup.add(gsMarker)

  // 跑道标志 - 白色方块标记（如图所示）
  const markerGeometry = new THREE.BoxGeometry(20, 0.6, 4)
  const markerMaterial = new THREE.MeshStandardMaterial({
    color: 0xffffff,
    emissive: 0xffffcc,
    emissiveIntensity: 0.3
  })

  // 左侧标记
  for (let i = 50; i < runwayLength - 50; i += 200) {
    const marker = new THREE.Mesh(markerGeometry, markerMaterial)
    marker.position.set(i, 901, -runwayWidth / 2 + 3) // 跑道从x=0开始
    runwayGroup.add(marker)
  }

  // 右侧标记
  for (let i = 50; i < runwayLength - 50; i += 200) {
    const marker = new THREE.Mesh(markerGeometry, markerMaterial)
    marker.position.set(i, 901, runwayWidth / 2 - 3) // 跑道从x=0开始
    runwayGroup.add(marker)
  }

  // 跑道入口白色方块标记
  const thresholdMarkerGeometry = new THREE.BoxGeometry(8, 0.4, 8)
  const thresholdMarkers = [
    { x: -runwayLength / 2 + 20, z: -runwayWidth / 2 + 5 }, // 跑道居中：调整x坐标
    { x: -runwayLength / 2 + 20, z: runwayWidth / 2 - 5 }, // 跑道居中：调整x坐标
    { x: -runwayLength / 2 + 20, z: 0 }, // 跑道居中：调整x坐标
    { x: runwayLength / 2 - 20, z: -runwayWidth / 2 + 5 }, // 跑道居中：调整x坐标
    { x: runwayLength / 2 - 20, z: runwayWidth / 2 - 5 }, // 跑道居中：调整x坐标
    { x: runwayLength / 2 - 20, z: 0 } // 跑道居中：调整x坐标
  ]

  thresholdMarkers.forEach((pos) => {
    const marker = new THREE.Mesh(thresholdMarkerGeometry, markerMaterial)
    marker.position.set(pos.x, 901, pos.z)
    runwayGroup.add(marker)
  })

  // 简单的跑道灯效果 - 跑道内红色灯光
  const runwayLightGeometry = new THREE.CylinderGeometry(0.2, 0.2, 0.5, 8)
  const runwayLightMaterial = new THREE.MeshStandardMaterial({
    color: 0xff0000,
    emissive: 0xff0000,
    emissiveIntensity: 1.0
  })

  // 跑道内左侧红色灯
  for (let i = 0; i < runwayLength; i += 50) {
    const light = new THREE.Mesh(runwayLightGeometry, runwayLightMaterial)
    light.position.set(i, 900.5, -runwayWidth / 2 + 10)
    runwayGroup.add(light)
  }

  // 跑道内右侧红色灯
  for (let i = 0; i < runwayLength; i += 50) {
    const light = new THREE.Mesh(runwayLightGeometry, runwayLightMaterial)
    light.position.set(i, 900.5, runwayWidth / 2 - 10)
    runwayGroup.add(light)
  }

  // 不添加 GridHelper / AxesHelper，避免场景中出现坐标轴与网格线

  if (data.position && data.position.length > 0) {
    const pathPoints = data.position
      .filter(
        (p): p is Position =>
          p && Number.isFinite(p.x) && Number.isFinite(p.y) && Number.isFinite(p.z)
      )
      .map((p) => new THREE.Vector3(p.x, p.y, p.z))

    const pathGeom = new THREE.BufferGeometry().setFromPoints(pathPoints)
    trajectoryLine = new THREE.Line(
      pathGeom,
      new THREE.LineBasicMaterial({
        // 复现轨迹线：蓝色 + 稍粗，便于对比识别
        color: 0x3b82f6,
        transparent: true,
        opacity: 0.95,
        linewidth: 5
      })
    )
    mainGroup.add(trajectoryLine)
    currentDataLength = data.position.length
  }

  group = new THREE.Group()
  // 飞机模型初始化时放在跑道起点（y=900 与跑道表面一致，x=0 为跑道头）
  group.position.set(0, 900, 0)
  // 未开始传输数据时：默认机头朝向跑道尽头（跑道沿局部 +X 延伸）
  // 与 computeYawFromPath 坐标约定保持一致：沿 +X 前进时 yaw = π/2
  group.rotation.set(0, Math.PI / 2 + MODEL_YAW_OFFSET, 0)
  mainGroup.add(group)

  const loader = new GLTFLoader()
  loader.load(
    '/plane-module.glb',
    (gltf: any) => {
      const model = gltf.scene

      model.traverse((obj: any) => {
        if (!obj.isMesh || !obj.material) return

        const materials = Array.isArray(obj.material) ? obj.material : [obj.material]

        const cleaned = materials.map((m: any) => {
          const mat = sanitizeMaterial(m)

          if (mat.map) {
            mat.map.colorSpace = THREE.SRGBColorSpace
            mat.map.anisotropy = renderer!.capabilities.getMaxAnisotropy()
            mat.map.needsUpdate = true
          }

          if (mat.normalMap) {
            mat.normalScale.set(1, 1)
          }

          mat.metalness = Math.min(mat.metalness ?? 0.1, 0.2)
          mat.roughness = Math.max(mat.roughness ?? 0.35, 0.25)

          mat.needsUpdate = true
          return mat
        })

        obj.material = Array.isArray(obj.material) ? cleaned : cleaned[0]
      })

      // 自动按当前模型包围盒缩放：让机翼宽度≈30m（跑道宽60m的一半）
      const box = new THREE.Box3().setFromObject(model)
      const size = new THREE.Vector3()
      box.getSize(size)
      // 机翼横向一般在 X 或 Z 方向，取两者中较大者作为当前宽度
      const currentWidth = Math.max(size.x, size.z)
      const targetWidth = 30
      const scaleFactor = currentWidth > 0 ? targetWidth / currentWidth : 1
      model.scale.setScalar(scaleFactor)
      group!.add(model)
      console.log('✅ 飞机模型 & 贴图加载成功')
    },
    undefined,
    (err: any) => {
      console.error('❌ 模型加载失败，使用占位模型', err)
      group!.add(
        new THREE.Mesh(
          new THREE.BoxGeometry(10, 2, 5),
          new THREE.MeshStandardMaterial({ color: 0xff0000 })
        )
      )
    }
  )

  let hasFramedOnce = false

  // 保存当前数据引用，用于 setIndex 更新
  let currentData = data

  const setIndex = (i: number, dataToUse?: FlightData) => {
    // 如果传入了新数据，使用新数据；否则使用保存的当前数据
    const useData = dataToUse || currentData
    if (!useData || !useData.position || useData.position.length === 0) return
    const idx = Math.min(i, useData.position.length - 1)
    const p0 = useData.position[idx]
    if (!p0) return

    group!.position.set(p0.x, p0.y, p0.z)
    // 轨迹线始终为完整航线（拿到数据后即绘制）；仅飞机姿态/位置随 index 变化

    const phaseAtIdx = (useData.flightPhases?.[idx] ?? props.currentFlightPhase) as
      | string
      | undefined

    // 需要两点才能算航向，只有一点时用姿态角 yaw
    const p1 = useData.position[idx + 1]
    const a = (useData.attitudeDeg[idx] as AttitudeDeg) || {
      pitch: 0,
      roll: 0,
      yaw: 0
    }
    const yawRad = (() => {
      const phase = String(phaseAtIdx ?? '')
      // 有下一个点：用轨迹切线方向（进近从跑道尾侧向跑道头，水平方向为 -X；滑行沿跑道头→尾的反方向也为 -X）
      if (p1 != null) return computeYawFromPath(p0, p1) + MODEL_YAW_OFFSET

      // 只有一个点：进近/滑行与跑道尾→头一致用 -X（π/2 + π）；滑跑/爬升用 +X
      if (phase === 'APPROACH' || phase === 'TAXI') {
        return Math.PI / 2 + Math.PI + MODEL_YAW_OFFSET
      }
      if (phase === 'TAKEOFF_ROLL' || phase === 'CLIMB') {
        return Math.PI / 2 + MODEL_YAW_OFFSET
      }

      return deg2rad(a.yaw ?? 0) + MODEL_YAW_OFFSET
    })()

    group!.rotation.set(deg2rad(a.pitch || 0), yawRad, deg2rad(a.roll || 0), 'ZYX')

    // 跑道坐标：局部 +X = 跑道头→跑道尾端(0→3600)，-X = 跑道尾端→跑道头
    // 后端 runwayMagneticHeadingMedian 是“跑道头→尾端”的磁航向。若进近/滑行阶段每帧都用 yaw 参与旋转，会出现参考系抖动，视觉上像飞机没往前飞。
    // 处理策略：CLIMB/APPROACH/TAXI 阶段锁定跑道磁航向为“首次拿到的中位数”，阶段切换时最多更新一次；其它阶段清空锁定值便于下一次周期重新锁定。
    const phaseStr = String(phaseAtIdx ?? '')
    const runwayMedian =
      (a as AttitudeDeg & { runwayMagneticHeadingMedian?: number }).runwayMagneticHeadingMedian
    const shouldLockHeading = phaseStr === 'CLIMB' || phaseStr === 'APPROACH' || phaseStr === 'TAXI'
    if (shouldLockHeading) {
      if (fixedRunwayHeadingPhase !== phaseStr) {
        fixedRunwayHeadingPhase = phaseStr
        if (runwayMedian != null && !Number.isNaN(runwayMedian)) {
          fixedRunwayHeading = runwayMedian
        }
      } else if (fixedRunwayHeading == null) {
        if (runwayMedian != null && !Number.isNaN(runwayMedian)) {
          fixedRunwayHeading = runwayMedian
        }
      }
    } else {
      fixedRunwayHeadingPhase = null
      fixedRunwayHeading = null
    }

    // 关键阶段如果还拿不到 runwayMedian（后端未下发/尚未到位），不要用 a.yaw 去驱动跑道旋转，否则会把跑道参考系“带跑”
    // 此时保持 mainGroup.rotation.y 不变，等拿到 runwayMedian 后再一次性对齐
    const hasRunwayMedian = runwayMedian != null && !Number.isNaN(runwayMedian)
    const headingRunway =
      fixedRunwayHeading != null ? fixedRunwayHeading : (hasRunwayMedian ? runwayMedian : null)
    if (mainGroup && headingRunway != null && !Number.isNaN(headingRunway)) {
      // 180 - headingRunway：使局部 +X 对准跑道头→尾端磁航向
      mainGroup.rotation.y = deg2rad(180 - headingRunway)
    }

    // 首次有有效位姿：相机放到机身后上方；自由视角下 Orbit 目标仅此一次对齐飞机，之后不每帧跟随
    if (!hasFramedOnce && controls && camera && group) {
      group.getWorldPosition(planeWorldPos)
      controls.target.copy(planeWorldPos)
      group.getWorldDirection(forwardDir)
      forwardDir.setY(0)
      if (forwardDir.lengthSq() < 1e-10) forwardDir.set(0, 0, -1)
      else forwardDir.normalize()
      rearAboveScratch
        .copy(planeWorldPos)
        .addScaledVector(forwardDir, -DEFAULT_CAM_BACK)
        .addScaledVector(upDir, DEFAULT_CAM_UP)
      camera.up.copy(upDir)
      camera.position.copy(rearAboveScratch)
      resetControlsResidual()
      controls.update()
      hasFramedOnce = true
    }
  }

  if (data.position && data.position.length > 0) {
    setIndex(0)
  }

  const onResize = () => {
    const w = Math.max(1, container.clientWidth)
    const h = Math.max(1, container.clientHeight)
    renderer!.setSize(w, h)
    camera!.aspect = w / h
    camera!.updateProjectionMatrix()
  }
  window.addEventListener('resize', onResize)
  // 避免容器首帧 0 宽高导致画布为 0（表现为黑屏）
  requestAnimationFrame(() => onResize())

  stopped = false
  const animate = () => {
    if (stopped) return
    // 自由视角：lockPlane=true 时不每帧把 Orbit 目标绑在飞机上（由用户拖动）；lockPlane=false 时 target 跟随飞机
    // 侧/俯：下面分支锁定飞机，并对目标点做 lerp 减轻阶跃感
    controls!.update()

    // 根据视角模式更新相机位置（使用飞机世界坐标，与 mainGroup 旋转一致）
    const mode = currentViewMode.value
    if (group) {
      group.getWorldPosition(planeWorldPos)
      if (mode !== 'free') {
        if (!followSmoothingReady) {
          smoothPlanePos.copy(planeWorldPos)
          followSmoothingReady = true
        } else {
          smoothPlanePos.lerp(planeWorldPos, PLANE_FOLLOW_SMOOTH)
        }
      }

      if (mode === 'side') {
        // 侧视角恢复默认 up，避免与俯视角互相影响
        camera!.up.set(0, 1, 0)
        // 侧视角：严格锁定到“飞机模型左侧”（随机体朝向变化），避免左右反转/受跑道参考系影响
        const sideOffset = 80
        const backOffset = -30 // 负值 = 稍微在机身后方
        const heightOffset = 20
        // 用 Three.js 的“真实前向”作为 forward（通常为世界空间 -Z），避免不同模型轴向导致侧视角偏成斜后方
        group.getWorldDirection(forwardDir)
        forwardDir.setY(0).normalize()
        // 右方向 = forward × up；左方向 = -right
        rightDir.crossVectors(forwardDir, upDir).normalize()
        leftDir.copy(rightDir).multiplyScalar(-1)

        const cameraX =
          smoothPlanePos.x + leftDir.x * sideOffset + forwardDir.x * backOffset + upDir.x * heightOffset
        const cameraY =
          smoothPlanePos.y + leftDir.y * sideOffset + forwardDir.y * backOffset + upDir.y * heightOffset
        const cameraZ =
          smoothPlanePos.z + leftDir.z * sideOffset + forwardDir.z * backOffset + upDir.z * heightOffset
        camera!.position.set(cameraX, cameraY, cameraZ)
        camera!.lookAt(smoothPlanePos.x, smoothPlanePos.y, smoothPlanePos.z)
      } else if (mode === 'top') {
        const topOffset = 100
        // 俯视角希望“机头朝向屏幕上方”：把相机 up 设为世界 +X，让前向(+X)映射为屏幕向上
        camera!.up.set(1, 0, 0)
        camera!.position.set(smoothPlanePos.x, smoothPlanePos.y + topOffset, smoothPlanePos.z)
        camera!.lookAt(smoothPlanePos.x, smoothPlanePos.y, smoothPlanePos.z)
      } else if (mode === 'follow') {
        camera!.up.set(0, 1, 0)
        if (isOrbitInteracting) {
          followCameraOffset.copy(camera!.position).sub(planeWorldPos)
        } else {
          camera!.position.copy(smoothPlanePos).add(followCameraOffset)
        }
        controls!.target.copy(smoothPlanePos)
      }
    }

    renderer!.render(scene!, camera!)
    requestAnimationFrame(animate)
  }
  animate()

  return {
    setIndex,
    /** 切换实时模式 - 实时数据传输时调用，避免相机视角被重置 */
    setRealtimeMode: (enabled: boolean) => {
      isRealtimeMode = enabled
    },
    /** 更新整个数据集 - 用于实时数据；planeIndex 默认最后一帧，全量加载从开头播放时请传 0（否则首次构图锁在终点会“黑屏/空景”） */
    updateData: (newData: FlightData, options?: UpdateFlightDataOptions) => {
      if (!scene || !group || !newData || !newData.position || newData.position.length === 0) return
      if (typeof newData.position.length !== 'number') return

      // 父组件可能刚写入 runwayStartPosition，子 props 尚未 flush；优先用 options 里的坐标，避免 mainGroup 滞后导致镜头与飞机分离
      applyMainGroupFromRunwayPosition(options?.runwayStartPosition ?? props.runwayStartPosition)

      // 启用实时模式，避免相机视角被重置
      isRealtimeMode = true

      try {
        const len = newData.position.length
        const needNewTrajectory =
          !trajectoryLine || currentDataLength !== len

        if (needNewTrajectory) {
          currentDataLength = len

          if (trajectoryLine && mainGroup) {
            mainGroup.remove(trajectoryLine)
            trajectoryLine.geometry.dispose()
            ;(trajectoryLine.material as THREE.Material).dispose()
            trajectoryLine = null
          }

          const pathPointsRaw = newData.position
            .filter(
              (p): p is Position =>
                p && Number.isFinite(p.x) && Number.isFinite(p.y) && Number.isFinite(p.z)
            )
            .map((p) => new THREE.Vector3(p.x, p.y, p.z))
          const pathPoints = decimateVector3Path(pathPointsRaw, TRAJECTORY_LINE_MAX_POINTS)

          const pathGeom = new THREE.BufferGeometry().setFromPoints(pathPoints)
          trajectoryLine = new THREE.Line(
            pathGeom,
            new THREE.LineBasicMaterial({
              color: 0x3b82f6,
              transparent: true,
              opacity: 0.95,
              linewidth: 5
            })
          )
          mainGroup.add(trajectoryLine)
        } else if (trajectoryLine) {
          const pathPointsRaw = newData.position
            .filter(
              (p): p is Position =>
                p && Number.isFinite(p.x) && Number.isFinite(p.y) && Number.isFinite(p.z)
            )
            .map((p) => new THREE.Vector3(p.x, p.y, p.z))
          const pathPoints = decimateVector3Path(pathPointsRaw, TRAJECTORY_LINE_MAX_POINTS)
          replaceLineGeometryFromPoints(trajectoryLine, pathPoints)
        }

        const last = len - 1
        const rawIdx = options?.planeIndex
        const planeIdx =
          rawIdx !== undefined && Number.isFinite(rawIdx)
            ? Math.max(0, Math.min(Math.floor(Number(rawIdx)), last))
            : last
        setIndex(planeIdx, newData)
        // 保存当前数据引用
        currentData = newData
      } catch (e) {
        console.error('[ThreeScene] updateData error', e)
      }
    },
    dispose() {
      stopped = true
      window.removeEventListener('resize', onResize)
      controls!.dispose()
      renderer!.dispose()
      if (trajectoryLine) {
        if (mainGroup) mainGroup.remove(trajectoryLine)
        trajectoryLine.geometry.dispose()
        ;(trajectoryLine.material as THREE.Material).dispose()
        trajectoryLine = null
      }
      scene!.clear()
      container.innerHTML = ''
    }
  }
}

let threeCtrl: ThreeController | null = null
let currentDataLength = 0
let trajectoryLine: THREE.Line | null = null
let isOrbitInteracting = false
/** 是否处于实时数据传输模式 */
let isRealtimeMode = false
/** 是否已经初始化过场景 */
let hasInitialized = false
/** 当前的跑道偏移量，用于实时数据更新时重新计算位置 */
let currentRunwayOffsetX = 0
let currentRunwayOffsetZ = 0
/** 主场景组：跑道+飞机+轨迹线，统一做位置偏移与磁航向旋转，保证飞机始终在跑道上 */
let mainGroup: THREE.Group | null = null
/** 跑道组对象，用于跑道几何 */
let runwayGroup: THREE.Group | null = null
/** 飞机世界坐标（用于相机目标与侧/俯视角），避免 mainGroup 旋转后错位 */
const planeWorldPos = new THREE.Vector3()
/** 侧视角计算用的临时向量，避免每帧分配对象 */
const upDir = new THREE.Vector3(0, 1, 0)
const forwardDir = new THREE.Vector3()
const rightDir = new THREE.Vector3()
const leftDir = new THREE.Vector3()
const rearAboveScratch = new THREE.Vector3()
const followCameraOffset = new THREE.Vector3(-DEFAULT_CAM_BACK, DEFAULT_CAM_UP, 0)
/** 侧/俯视角：平滑跟随用的插值目标（减轻数据阶跃导致的镜头抖动） */
const smoothPlanePos = new THREE.Vector3()
/** 侧/俯：是否已初始化 smoothPlanePos（与 toggle 进入侧/俯时同步） */
let followSmoothingReady = false
/** 每帧向真实飞机位置靠拢的比例（略低更顺，略高更跟手） */
const PLANE_FOLLOW_SMOOTH = 0.22

/** 父组件在 threeCtrl 就绪前调用的 updateData 暂存于此，init 完成后立即应用 */
let pendingFlightData: FlightData | null = null
let pendingUpdateFlightDataOptions: UpdateFlightDataOptions | undefined = undefined
/** 锁定后的跑道磁航向（避免进近/滑行阶段每帧抖动导致“飞机看起来不前进”） */
let fixedRunwayHeading: number | null = null
let fixedRunwayHeadingPhase: string | null = null

onMounted(() => {
  // 初始化由 watch 的 immediate 执行，这里不再重复初始化
  console.log('ThreeScene 组件已挂载')
})

/** 空数据占位，用于在无飞行数据时仍可初始化 3D 场景（跑道等，轨迹线随数据绘制） */
const EMPTY_FLIGHT_DATA: FlightData = {
  sampleRate: 10,
  time: [],
  position: [],
  attitudeDeg: [],
  deviation: [],
  flightPhases: []
}

watch(
  () => [props.data, containerRef.value] as const,
  ([newData, container], oldTuple) => {
    const prevData = (oldTuple as readonly [FlightData | null, HTMLDivElement | null] | undefined)?.[0]
    try {
      if (!container) return

      const dataToUse =
        newData && newData.position && newData.position.length > 0 ? newData : EMPTY_FLIGHT_DATA

      const doInit = () => {
        if (!containerRef.value) return
        const w = containerRef.value.clientWidth
        const h = containerRef.value.clientHeight
        if (w <= 0 || h <= 0) {
          // 容器尚未获得有效尺寸，延迟重试
          requestAnimationFrame(doInit)
          return
        }
        if (!hasInitialized) {
          threeCtrl = initThree(containerRef.value, dataToUse)
          hasInitialized = true
          // 若首次用空数据初始化，但当前已有真实数据，立刻同步飞机位置
          const toApply = (newData?.position?.length ? newData : null) ?? pendingFlightData
          if (toApply?.position?.length && threeCtrl) {
            threeCtrl.setRealtimeMode(true)
            threeCtrl.updateData(toApply, pendingUpdateFlightDataOptions)
            pendingFlightData = null
            pendingUpdateFlightDataOptions = undefined
          }
        }
        // 不在此 watch 里同步 props.data → updateData：否则会默认对齐末帧，覆盖父组件传入的 planeIndex（如从 0 开始播放）
        // 轨迹与机位由父组件 threeSceneRef.updateData / setIndex 显式驱动
      }

      nextTick(() => doInit())
    } catch (e) {
      console.error('[ThreeScene] watch data error', e)
    }
  },
  { immediate: true }
)

// 监听跑道起点位置变化，只更新主场景组位置不重新初始化场景
watch(
  () => props.runwayStartPosition,
  (newPosition) => {
    console.log('ThreeScene 跑道起点位置更新：', newPosition)

    if (!scene || !threeCtrl || !mainGroup) return

    applyMainGroupFromRunwayPosition(newPosition ?? undefined)
  }
)

onUnmounted(() => {
  isOrbitInteracting = false
  if (threeCtrl) {
    threeCtrl.dispose()
  }
  threeCtrl = null
  hasInitialized = false
  pendingFlightData = null
  pendingUpdateFlightDataOptions = undefined
  currentDataLength = 0
  fixedRunwayHeading = null
  fixedRunwayHeadingPhase = null
  followSmoothingReady = false
})

defineExpose({
  setIndex: (index: number) => {
    if (threeCtrl) {
      threeCtrl.setIndex(index)
    }
  },
  updateData: (data: FlightData, options?: UpdateFlightDataOptions) => {
    if (threeCtrl) {
      threeCtrl.updateData(data, options)
    } else if (data?.position?.length) {
      // 3D 尚未初始化，先缓存，init 完成后 watch 内会应用
      pendingFlightData = data
      pendingUpdateFlightDataOptions = options
    }
  },
  setRealtimeMode: (enabled: boolean) => {
    if (threeCtrl) {
      threeCtrl.setRealtimeMode(enabled)
    }
  },
  setViewMode: (mode: ViewMode) => setViewMode(mode),
  reframeCamera
})
</script>

<style scoped>
.three-container {
  position: absolute;
  inset: 0;
  background: #0f1220;
}

.view-buttons {
  position: absolute;
  top: 20px;
  right: 20px;
  z-index: 100;
  display: flex;
  gap: 10px;
  align-items: center;
}

.view-label {
  padding: 0 4px;
  font-size: 12px;
  font-weight: 600;
  color: #94a3b8;
  user-select: none;
}

.view-sep {
  width: 1px;
  height: 16px;
  background: #334155;
  opacity: 0.9;
}

.view-buttons button {
  height: 30px;
  padding: 0 12px;
  font-size: 12px;
  font-weight: 600;
  color: #fff;
  cursor: pointer;
  background: #1f2937;
  border: 1px solid #334155;
  border-radius: 6px;
  transition: all 0.15s ease;
}

.view-buttons button:hover {
  background: #243244;
  border-color: #475569;
}

.view-buttons button.active {
  background: #0ea5e9;
  border-color: #38bdf8;
}
</style>
