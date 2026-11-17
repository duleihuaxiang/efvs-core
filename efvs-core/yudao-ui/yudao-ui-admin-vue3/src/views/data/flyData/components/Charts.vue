<template>
  <div class="charts-container">
    <div ref="locChartRef" class="chart"></div>
    <div ref="gsChartRef" class="chart"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts'
import type { FlightData } from '../types'

const props = defineProps<{
  data: FlightData
  currentIndex: number
}>()

const locChartRef = ref<HTMLDivElement | null>(null)
const gsChartRef = ref<HTMLDivElement | null>(null)
let locChart: echarts.ECharts | null = null
let gsChart: echarts.ECharts | null = null

const initCharts = () => {
  console.log(
    'initCharts 被调用，locChartRef：',
    locChartRef.value,
    'gsChartRef：',
    gsChartRef.value
  )
  if (!locChartRef.value || !gsChartRef.value) return

  const AMBER = '#FFB000'
  const AMBER_AREA = 'rgba(255, 176, 0, 0.12)'
  const t = props.data.time
  console.log('props.data.time.length：', t.length)

  const baseOpt = (title: string, min: number, max: number, yName: string) => ({
    title: {
      text: title,
      left: 'center',
      textStyle: {
        color: AMBER,
        fontSize: 13,
        fontWeight: 'bold'
      }
    },
    darkMode: true,
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' }
    },
    grid: { left: 55, right: 30, top: 40, bottom: 45 },
    dataZoom: [{ type: 'inside' }, { type: 'slider', height: 18, bottom: 0 }],
    xAxis: {
      type: 'category',
      data: t,
      axisLabel: { color: '#64748b' }
    },
    yAxis: {
      type: 'value',
      min,
      max,
      name: yName,
      splitLine: { lineStyle: { color: '#1e293b' } }
    }
  })

  locChart = echarts.init(locChartRef.value)
  locChart.setOption({
    ...baseOpt('LOC 航向偏差 (琥珀色=跑道区域)', -3, 3, 'Dots'),
    series: [
      {
        name: 'LOC',
        type: 'line',
        showSymbol: false,
        data: props.data.deviation.map((d) => d.loc),
        lineStyle: { color: '#38bdf8', width: 2 },
        markArea: {
          silent: true,
          itemStyle: { color: AMBER_AREA },
          data: [[{ yAxis: -1.5 }, { yAxis: 1.5 }]]
        },
        markLine: {
          symbol: 'none',
          z: 10,
          lineStyle: { color: AMBER },
          data: [
            {
              yAxis: 0,
              label: { formatter: '跑道中心' },
              lineStyle: { color: AMBER, width: 2 }
            },
            {
              yAxis: 1.5,
              lineStyle: {
                color: AMBER,
                width: 2,
                type: 'dashed',
                opacity: 0.5
              }
            },
            {
              yAxis: -1.5,
              lineStyle: {
                color: AMBER,
                width: 2,
                type: 'dashed',
                opacity: 0.5
              }
            }
          ]
        }
      }
    ]
  })

  gsChart = echarts.init(gsChartRef.value)
  gsChart.setOption({
    ...baseOpt('GS 下滑偏差 (琥珀色=安全包线)', -1, 1, 'Dots'),
    series: [
      {
        name: 'GS',
        type: 'line',
        showSymbol: false,
        data: props.data.deviation.map((d) => d.gs),
        lineStyle: { color: '#a855f7', width: 2 },
        markArea: {
          silent: true,
          itemStyle: { color: AMBER_AREA },
          data: [[{ yAxis: -0.3 }, { yAxis: 0.3 }]]
        },
        markLine: {
          symbol: 'none',
          z: 10,
          lineStyle: { color: AMBER, width: 2 },
          data: [
            { yAxis: 0, label: { formatter: '下滑中心' } },
            { yAxis: 0.3, lineStyle: { type: 'dotted' } },
            { yAxis: -0.3, lineStyle: { type: 'dotted' } }
          ]
        }
      }
    ]
  })

  echarts.connect([locChart, gsChart])
  window.addEventListener('resize', () => {
    locChart?.resize()
    gsChart?.resize()
  })
}

const updateChartsCursor = (i: number) => {
  if (!locChart || !gsChart) return
  const action = { type: 'showTip', seriesIndex: 0, dataIndex: i }
  locChart.dispatchAction(action)
  gsChart.dispatchAction(action)
}

watch(
  () => props.currentIndex,
  (newIndex) => {
    updateChartsCursor(newIndex)
  }
)

onMounted(() => {
  console.log('Charts组件onMounted，props.data：', props.data)
  if (props.data && props.data.time && props.data.time.length > 0) {
    initCharts()
    updateChartsCursor(props.currentIndex)
  }
})

watch(
  () => props.data,
  (newData: FlightData | null) => {
    console.log('Charts 组件数据更新：', newData)

    if (newData && newData.time && newData.time.length > 0) {
      if (locChart) {
        locChart.dispose()
      }
      if (gsChart) {
        gsChart.dispose()
      }
      initCharts()
      updateChartsCursor(props.currentIndex)
    }
  },
  { immediate: true }
)

onUnmounted(() => {
  locChart?.dispose()
  gsChart?.dispose()
  window.removeEventListener('resize', () => {
    locChart?.resize()
    gsChart?.resize()
  })
})
</script>

<style scoped>
.charts-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
  height: 100%;
  padding: 10px;
  box-sizing: border-box;
}

.chart {
  width: 100%;
  height: 50%; /* 明确设置每个图表的高度 */
  min-height: 0;
  min-height: 200px; /* 确保最小高度 */
  flex: 1;
}
</style>
