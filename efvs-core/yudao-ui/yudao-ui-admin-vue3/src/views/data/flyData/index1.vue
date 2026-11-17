<template>
  <div class="container">
    <el-container class="layout-container">
      <!-- 上部分内容：左右分区 -->
      <el-main class="main-content">
        <el-container class="inner-container">
          <!-- 左侧背景图区域 -->
          <el-aside width="30%" class="left-panel">
            <div class="background-image-container">
              <div class="image-overlay">
                <div class="hud-container-wrapper">
                  <div class="hud-container hud-left">
                    <span class="hud-label">速度:</span>
                    <span class="hud-value">{{ speed }}</span>
                  </div>
                  <div class="hud-container hud-right">
                    <span class="hud-label">高度:</span>
                    <span class="hud-value">{{ height }}</span>
                  </div>
                </div>
                <h2>飞机降落模拟</h2>
                <p>水平视角与垂直视角分析</p>
              </div>
            </div>
          </el-aside>

          <!-- 右侧图表区域 -->
          <el-main class="right-panel">
            <div class="chart-container">
              <!-- 水平视角图表（跑道偏移） -->
              <div class="chart-section">
                <h3>水平视角（跑道偏移）</h3>
                <div ref="horizontalChartRef" class="chart"></div>
              </div>

              <!-- 垂直视角图表（高度-时间） -->
              <div class="chart-section">
                <h3>垂直视角（高度-距离）</h3>
                <div ref="verticalChartRef" class="chart"></div>
              </div>
            </div>
          </el-main>
        </el-container>
      </el-main>

      <!-- 下部分控制区域 -->
      <el-footer class="controls-container">
        <div class="controls">
          <button @click="togglePlay" class="control-btn">
            {{ isPlaying ? '⏸ 暂停' : '▶ 播放' }}
          </button>
          <button @click="reset" class="control-btn">⏹ 重置</button>
          <div class="speed-control">
            <label>播放速度:</label>
            <select v-model="playSpeed">
              <option :value="2000">0.5x</option>
              <option :value="1000">1x</option>
              <option :value="500">2x</option>
              <option :value="250">4x</option>
            </select>
          </div>
          <div class="progress-info">
            <span class="progress-text">进度 :</span>
            <el-progress
              :percentage="(currentIndex / maxDataPoints) * 100"
              :show-text="false"
              class="progress-bar"
            />
          </div>
        </div>
      </el-footer>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch, Ref } from 'vue'
import * as echarts from 'echarts'

// 定义类型
interface ChartDataPoint {
  [index: number]: number | null
}

// 图表引用
const horizontalChartRef = ref<HTMLDivElement | null>(null)
const verticalChartRef = ref<HTMLDivElement | null>(null)

// 图表实例
let horizontalChart: echarts.ECharts | null = null
let verticalChart: echarts.ECharts | null = null

// 数据状态
const speed = ref<number>(100)
const height = ref<number>(3000)
const isPlaying = ref<boolean>(false)
const timer = ref<NodeJS.Timeout | null>(null)
const playSpeed = ref<number>(1000)
const currentIndex = ref<number>(0)
const maxDataPoints = ref<number>(50)

// 垂直视角数据
const verticalDataX = ref<number[]>([])
const verticalDataY = ref<number[]>([])
const xVerticalMin = ref<number>(0)
const xVerticalMax = ref<number>(0)
const yMin = ref<number>(0)
const yMax = ref<number>(0)

// 水平视角数据
const horizontalDataX = ref<number[]>([])
const runwayBoundary1 = ref<ChartDataPoint[]>([])
const landingCurve = ref<ChartDataPoint[]>([])
const runwayBoundary2 = ref<ChartDataPoint[]>([])
const acceptableBoundary1 = ref<ChartDataPoint[]>([])
const acceptableBoundary2 = ref<ChartDataPoint[]>([])
const xMin = ref<number>(0)
const xMax = ref<number>(0)
const yHorizontalMin = ref<number>(-60)
const yHorizontalMax = ref<number>(60)

// 监听播放速度变化
watch(playSpeed, () => {
  if (isPlaying.value) {
    togglePlay() // 暂停
    togglePlay() // 重新开始以应用新速度
  }
})

// 初始化图表
const initCharts = () => {
  if (!horizontalChartRef.value || !verticalChartRef.value) return

  // 初始化水平视角图表
  horizontalChart = echarts.init(horizontalChartRef.value)
  const horizontalOption = {
    grid: {
      left: '10%',
      right: '10%',
      top: '20%',
      bottom: '10%',
      containLabel: true
    },
    title: {
      text: '跑道偏移情况',
      left: 'center',
      textStyle: {
        fontSize: 16,
        fontWeight: 'bold'
      }
    },
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'value',
      name: '距离(米)',
      nameLocation: 'end',
      nameGap: 25,
      min: xMin.value,
      max: xMax.value
    },
    yAxis: {
      type: 'value',
      name: '横向偏移(米)',
      nameLocation: 'middle',
      nameGap: 40,
      min: yHorizontalMin.value,
      max: yHorizontalMax.value
    },
    legend: {
      data: ['跑道边界1', '降落曲线', '跑道边界2', '合格边界1', '合格边界2'],
      left: 'left',
      bottom: 'bottom'
    },
    series: [
      {
        name: '跑道边界1',
        data: [],
        type: 'line',
        symbol: 'none',
        lineStyle: {
          width: 2,
          color: '#1890ff'
        }
      },
      {
        name: '降落曲线',
        data: [],
        type: 'line',
        smooth: true,
        symbol: 'none',
        lineStyle: {
          width: 3,
          color: '#f5222d'
        }
      },
      {
        name: '跑道边界2',
        data: [],
        type: 'line',
        symbol: 'none',
        lineStyle: {
          width: 2,
          color: '#1890ff'
        }
      },
      {
        name: '合格边界1',
        data: [],
        type: 'line',
        symbol: 'none',
        lineStyle: {
          width: 1,
          color: '#52c41a',
          type: 'dashed'
        }
      },
      {
        name: '合格边界2',
        data: [],
        type: 'line',
        symbol: 'none',
        lineStyle: {
          width: 1,
          color: '#52c41a',
          type: 'dashed'
        }
      }
    ],
    animation: false
  }
  horizontalChart.setOption(horizontalOption)

  // 初始化垂直视角图表
  verticalChart = echarts.init(verticalChartRef.value)
  const verticalOption = {
    grid: {
      left: '10%',
      right: '10%',
      top: '20%',
      bottom: '10%',
      containLabel: true
    },
    title: {
      text: '飞机降落过程',
      left: 'center',
      textStyle: {
        fontSize: 16,
        fontWeight: 'bold'
      }
    },
    tooltip: {
      trigger: 'axis',
      formatter: (params: any) => {
        const param = params[0]
        if (param.value !== null) {
          return `距离: ${param.value[0]}米<br/>高度: ${param.value[1]}米`
        }
        return ''
      }
    },
    xAxis: {
      type: 'value',
      name: '距离(米)',
      nameLocation: 'end',
      nameGap: 25,
      min: xVerticalMin.value,
      max: xVerticalMax.value,
      axisLabel: {
        formatter: '{value}'
      }
    },
    yAxis: {
      type: 'value',
      name: '高度(米)',
      nameLocation: 'middle',
      nameGap: 40,
      min: yMin.value,
      max: yMax.value,
      axisLabel: {
        formatter: '{value}'
      }
    },
    series: [
      {
        data: [],
        type: 'line',
        smooth: true,
        symbol: 'none',
        lineStyle: {
          width: 3,
          color: '#1890ff'
        },
        markPoint: {
          data: [
            { type: 'max', name: '最高点' },
            { type: 'min', name: '着陆点' }
          ]
        }
      }
    ],
    animation: false
  }
  verticalChart.setOption(verticalOption)
}

// 生成完整数据
const generateFullData = () => {
  // 生成水平视角数据（跑道偏移）
  horizontalDataX.value = []
  runwayBoundary1.value = []
  landingCurve.value = []
  runwayBoundary2.value = []
  acceptableBoundary1.value = []
  acceptableBoundary2.value = []

  // 模拟飞机降落过程中的横向偏移
  const runwayWidth = 100 // 跑道宽度（米）
  const runwayCenter = 0 // 跑道中心线位置

  for (let i = 0; i < maxDataPoints.value; i++) {
    const distance = i * 50 // 每50米一个数据点
    horizontalDataX.value.push(distance)

    // 跑道边界（固定）
    runwayBoundary1.value.push([distance, runwayCenter - runwayWidth / 2])
    runwayBoundary2.value.push([distance, runwayCenter + runwayWidth / 2])

    // 降落曲线（模拟飞机逐渐对准跑道的过程）
    let offset
    if (distance < 500) {
      // 远离跑道时有较大偏移
      offset = runwayCenter + (Math.random() - 0.5) * 80
    } else if (distance < 1500) {
      // 接近跑道时逐渐对准
      const approachProgress = (distance - 500) / 1000
      offset = runwayCenter + (Math.random() - 0.5) * 40 * (1 - approachProgress)
    } else {
      // 着陆时基本对准跑道中心
      offset = runwayCenter + (Math.random() - 0.5) * 10
    }

    landingCurve.value.push([distance, offset])
  }

  // 生成合格边界线（只在右侧一段距离内显示）
  const startX = 1500 // 合格边界线起始位置
  const endX = 2500 // 合格边界线结束位置

  // 生成合格边界线数据点
  for (let x = startX; x <= endX; x += 50) {
    acceptableBoundary1.value.push([x, -30]) // 固定在-30位置
    acceptableBoundary2.value.push([x, 30]) // 固定在+30位置
  }

  // 计算x轴范围
  xMin.value = 0
  xMax.value = Math.max(...horizontalDataX.value) * 1.05

  // 固定水平视角y轴范围
  yHorizontalMin.value = -60
  yHorizontalMax.value = 60

  // 生成垂直视角数据（高度-距离）
  verticalDataX.value = []
  verticalDataY.value = []

  // 模拟飞机降落过程: 从巡航高度逐渐下降到地面
  const cruiseAltitude = 3000 // 巡航高度3000米
  const descentStart = 300 // 开始下降的距离点(米)
  const landingDistance = 1700 // 着陆距离点(米)

  for (let i = 0; i < maxDataPoints.value; i++) {
    const verticalDistance = i * 50 // 每50米一个数据点
    verticalDataX.value.push(verticalDistance)

    let altitude
    if (verticalDistance < descentStart) {
      // 巡航阶段 - 保持高度
      altitude = cruiseAltitude
    } else if (verticalDistance < landingDistance) {
      // 下降阶段 - 逐渐降低高度
      const descentProgress = (verticalDistance - descentStart) / (landingDistance - descentStart)
      // 使用二次函数使下降更加平滑
      altitude = cruiseAltitude * (1 - Math.pow(descentProgress, 1.5))
    } else {
      // 着陆后 - 高度为0
      altitude = 0
    }

    // 添加轻微的随机扰动模拟真实飞行
    const noise = (Math.random() - 0.5) * 20
    altitude = Math.max(0, altitude + noise) // 确保高度不低于0

    verticalDataY.value.push(parseFloat(altitude.toFixed(1)))
  }

  // 计算x轴范围
  xVerticalMin.value = 0
  xVerticalMax.value = Math.max(...verticalDataX.value) * 1.05

  // 计算y轴范围
  yMin.value = 0
  yMax.value = Math.ceil(cruiseAltitude * 1.1) // 留出10%的顶部空间
}

// 更新图表
const updateCharts = () => {
  if (!verticalChart || !horizontalChart) return

  // 更新水平视角图表
  const runwayBoundary1Current = runwayBoundary1.value
  const acceptableBoundary1Current = acceptableBoundary1.value
  const landingCurveCurrent = landingCurve.value.map((point, index) => {
    return index < currentIndex.value ? point : null
  })

  const runwayBoundary2Current = runwayBoundary2.value
  const acceptableBoundary2Current = acceptableBoundary2.value

  const horizontalOption = {
    xAxis: {
      min: xMin.value,
      max: xMax.value
    },
    yAxis: {
      min: yHorizontalMin.value,
      max: yHorizontalMax.value
    },
    series: [
      {
        data: runwayBoundary1Current
      },
      {
        data: landingCurveCurrent
      },
      {
        data: runwayBoundary2Current
      },
      {
        data: acceptableBoundary1Current
      },
      {
        data: acceptableBoundary2Current
      }
    ]
  }
  horizontalChart.setOption(horizontalOption)

  // 更新垂直视角图表
  const verticalSeriesData: number[][] = []
  for (let i = 0; i < currentIndex.value; i++) {
    if (i < verticalDataX.value.length && i < verticalDataY.value.length) {
      verticalSeriesData.push([verticalDataX.value[i], verticalDataY.value[i]])
      height.value = verticalDataY.value[i]
    }
  }

  const verticalOption = {
    xAxis: {
      min: xVerticalMin.value,
      max: xVerticalMax.value
    },
    yAxis: {
      min: yMin.value,
      max: yMax.value
    },
    series: [
      {
        data: verticalSeriesData
      }
    ]
  }
  verticalChart.setOption(verticalOption)
}

// 切换播放/暂停
const togglePlay = () => {
  if (isPlaying.value) {
    // 暂停
    if (timer.value) {
      clearInterval(timer.value)
      timer.value = null
    }
    isPlaying.value = false
  } else {
    // 播放
    if (currentIndex.value >= maxDataPoints.value) {
      reset()
    }

    isPlaying.value = true
    timer.value = setInterval(() => {
      if (currentIndex.value <= maxDataPoints.value) {
        updateCharts()
        speed.value--
        if (height.value === 0) {
          speed.value = 0
        }
        currentIndex.value++
      } else {
        togglePlay() // 自动暂停
      }
    }, playSpeed.value)
  }
}

// 重置
const reset = () => {
  if (timer.value) {
    clearInterval(timer.value)
    timer.value = null
  }

  isPlaying.value = false
  currentIndex.value = 0
  speed.value = 100
  height.value = 3000

  // 重置图表
  if (verticalChart && horizontalChart) {
    const horizontalOption = {
      xAxis: {
        min: xMin.value,
        max: xMax.value
      },
      yAxis: {
        min: yHorizontalMin.value,
        max: yHorizontalMax.value
      },
      series: [{ data: [] }, { data: [] }, { data: [] }, { data: [] }, { data: [] }]
    }
    horizontalChart.setOption(horizontalOption)

    const verticalOption = {
      xAxis: {
        min: xVerticalMin.value,
        max: xVerticalMax.value
      },
      yAxis: {
        min: yMin.value,
        max: yMax.value
      },
      series: [{ data: [] }]
    }
    verticalChart.setOption(verticalOption)
  }
}

// 生命周期钩子
onMounted(() => {
  initCharts()
  generateFullData()
  reset()
})

onBeforeUnmount(() => {
  if (timer.value) {
    clearInterval(timer.value)
  }
  if (verticalChart) {
    verticalChart.dispose()
  }
  if (horizontalChart) {
    horizontalChart.dispose()
  }
})
</script>

<style scoped>
/* 响应式设计 */

/* @media (width <= 768px) {
  .inner-container {
    flex-direction: column;
  }

  .left-panel {
    width: 100%;
    height: 50%;
    background-color: white;
  }

  .right-panel {
    width: 100%;
    height: 50%;
  }

  .chart-container {
    padding: 10px;
  }

  .chart-section {
    padding: 15px;
  }

  .controls {
    flex-direction: column;
    gap: 10px;
  }

  .control-btn {
    width: 100%;
  }
} */

.container {
  display: flex;
  width: 100%;
  height: 100vh;
  flex-direction: column;
}

.layout-container {
  height: 100%;
}

.main-content {
  height: 90%; /* 占比8 */
  padding: 0;
}

.inner-container {
  height: 100%;
}

.left-panel {
  position: relative;
  height: 100%;
  overflow: hidden;
  background-color: white;
}

.background-image-container {
  position: relative;
  width: 100%;
  height: 100%;
}

.image-overlay {
  position: absolute;
  top: 30%;
  left: 0;
  display: flex;
  width: 100%;
  height: 40%;
  color: white;
  text-align: center;
  background: rgb(0 0 0 / 80%);
  background-image: url('@/assets/imgs/hud.png');
  background-repeat: no-repeat;
  background-size: cover;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.image-overlay h2 {
  margin-bottom: 1rem;
  font-size: 2.5rem;
}

.image-overlay p {
  font-size: 1.2rem;
}

.right-panel {
  height: 100%;
  padding: 0;
  overflow-y: auto;
  background-color: #f5f5f5;
}

.chart-container {
  display: flex;
  height: 100%;
  flex-direction: column;
  padding: 0;
}

.chart-section {
  padding: 20px;
  background-color: white;
  flex: 1;
}

.chart-section h3 {
  margin-bottom: 15px;
  color: #333;
  text-align: center;
}

.chart {
  width: 100%;
  height: calc(100% - 60px);
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.controls-container {
  display: flex;
  height: 10%; /* 占比1 */
  padding: 10px;
  background-color: white;
  border-top: 1px solid #e0e0e0;
  align-items: center;
  justify-content: center;
}

.controls {
  display: flex;
  gap: 20px;
  align-items: center;
  flex-wrap: wrap;
  justify-content: center;
  width: 100%;
  max-width: 1200px;
}

.control-btn {
  min-width: 80px;
  padding: 10px 20px;
  font-size: 16px;
  color: white;
  cursor: pointer;
  background-color: #409eff;
  border: none;
  border-radius: 6px;
  transition: background-color 0.3s;
}

.control-btn:hover {
  background-color: #337ecc;
}

.speed-control {
  display: flex;
  align-items: center;
  gap: 8px;
}

.speed-control label {
  font-size: 16px;
  white-space: nowrap;
}

.speed-control select {
  padding: 8px 12px;
  font-size: 16px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.progress-info {
  display: flex;
  align-items: center;
  gap: 15px;
  font-size: 16px;
  color: #606266;
  white-space: nowrap;
}

.progress-text {
  font-weight: bold;
}

.progress-bar {
  width: 150px;
}

.hud-container-wrapper {
  display: flex;
  justify-content: space-between;
  width: 100%;
  max-width: 800px;
  padding: 0 20px;
  box-sizing: border-box;
}

.hud-container {
  display: flex;
  min-width: 120px;
  padding: 10px 20px;
  background-color: rgb(255 255 255 / 90%);
  border-radius: 8px;
  flex-direction: column;
  align-items: center;
}

.hud-label {
  margin-bottom: 5px;
  font-size: 1rem;
  color: #333;
}

.hud-value {
  font-size: 1.5rem;
  font-weight: bold;
  color: #000;
}
</style>
