<template>
  <div>
    <el-card shadow="never">
      <el-skeleton :loading="loading" animated>
        <el-row :gutter="16" justify="space-between">
          <el-col :xl="12" :lg="12" :md="12" :sm="24" :xs="24">
            <div class="flex items-center">
              <el-avatar :src="avatar" :size="70" class="mr-16px">
                <img src="@/assets/imgs/avatar.gif" alt="" />
              </el-avatar>
              <div>
                <div class="text-20px">
                  {{ t('workplace.welcome') }} {{ username }} {{ t('workplace.happyDay') }}
                </div>
                <div class="mt-10px text-14px text-gray-500"> 今天是：{{ currentDate }} </div>
              </div>
            </div>
          </el-col>
          <!--    <el-col :xl="12" :lg="12" :md="12" :sm="24" :xs="24">
            <div class="h-70px flex items-center justify-end lt-sm:mt-10px">
              <div class="px-8px text-right">
                <div class="mb-16px text-14px text-gray-400">{{ t('workplace.project') }}</div>
                <CountTo
                  class="text-20px"
                  :start-val="0"
                  :end-val="totalSate.project"
                  :duration="2600"
                />
              </div>
              <el-divider direction="vertical" />
              <div class="px-8px text-right">
                <div class="mb-16px text-14px text-gray-400">{{ t('workplace.toDo') }}</div>
                <CountTo
                  class="text-20px"
                  :start-val="0"
                  :end-val="totalSate.todo"
                  :duration="2600"
                />
              </div>
              <el-divider direction="vertical" border-style="dashed" />
              <div class="px-8px text-right">
                <div class="mb-16px text-14px text-gray-400">{{ t('workplace.access') }}</div>
                <CountTo
                  class="text-20px"
                  :start-val="0"
                  :end-val="totalSate.access"
                  :duration="2600"
                />
              </div>
            </div>
          </el-col> -->
        </el-row>
      </el-skeleton>
    </el-card>
  </div>

  <el-row class="mt-8px" :gutter="8" justify="center">
    <el-col :xl="24" :lg="24" :md="24" :sm="24" :xs="24" class="mb-8px">
      <!--   <el-card shadow="never" class="mt-8px">
        <el-skeleton :loading="loading" animated>
          <el-row :gutter="20" justify="space-between">
            <el-col :xl="6" :lg="6" :md="12" :sm="12" :xs="12">
              <div class="card">
                <div class="card-title">总销售额</div>
                <div class="card-value">¥ 126,560</div>
                <div class="card-desc">日同比 12% ↑</div>
              </div>
            </el-col>
            <el-col :xl="6" :lg="6" :md="12" :sm="12" :xs="12">
              <div class="card">
                <div class="card-title">访问量</div>
                <div class="card-value">8,848</div>
                <div class="card-desc">日同比 11% ↑</div>
              </div> </el-col
            ><el-col :xl="6" :lg="6" :md="12" :sm="12" :xs="12">
              <div class="card">
                <div class="card-title">支付笔数</div>
                <div class="card-value">6,560</div>
                <div class="card-desc">转化率 60%</div>
              </div> </el-col
            ><el-col :xl="6" :lg="6" :md="12" :sm="12" :xs="12">
              <div class="card">
                <div class="card-title">运营活动效果</div>
                <div class="card-value">78%</div>
                <div class="card-desc">周同比 15% ↑</div>
              </div>
            </el-col>
          </el-row>
        </el-skeleton>
      </el-card> -->

      <el-card shadow="never" class="mt-8px">
        <el-skeleton :loading="loading" animated>
          <el-row :gutter="20" justify="space-between">
            <el-col :xl="12" :lg="12" :md="24" :sm="24" :xs="24">
              <el-card shadow="hover" class="mb-8px">
                <el-skeleton :loading="loading" animated>
                  <Echart :options="lineOptionData" :height="280" />
                </el-skeleton>
              </el-card>
            </el-col>
            <el-col :xl="12" :lg="12" :md="24" :sm="24" :xs="24">
              <el-card shadow="hover" class="mb-8px">
                <el-skeleton :loading="loading" animated>
                  <Echart :options="mapOptionsData" :height="280" />
                </el-skeleton>
              </el-card>
            </el-col>
          </el-row>
        </el-skeleton>
        <el-skeleton :loading="loading" animated>
          <el-row :gutter="20" justify="space-evenly">
            <el-col :xl="12" :lg="12" :md="24" :sm="24" :xs="24">
              <el-card shadow="hover" class="mb-8px">
                <el-skeleton :loading="loading" animated>
                  <Echart :options="doubleBarOptionData" :height="280" /></el-skeleton></el-card
            ></el-col>
            <!--   <el-col :xl="7" :lg="7" :md="24" :sm="24" :xs="24">
              <el-card shadow="hover" class="mb-8px">
                <el-skeleton :loading="loading" animated>
                  <Echart :options="pieOptionsData" :height="280" /></el-skeleton></el-card
            ></el-col> -->
            <el-col :xl="12" :lg="12" :md="24" :sm="24" :xs="24">
              <el-card shadow="hover" class="mb-8px">
                <el-skeleton :loading="loading" animated>
                  <div style="height: 280px">
                    <ToDoTask ref="todoTaskRef" />
                  </div>
                </el-skeleton> </el-card
            ></el-col>
          </el-row>
        </el-skeleton>
      </el-card>
    </el-col>
  </el-row>
</template>
<script lang="ts" setup>
import { set } from 'lodash-es'
import { EChartsOption } from 'echarts'
import echarts from '@/plugins/echarts'
import china from '@/assets/map/json/china.json'
import { useUserStore } from '@/store/modules/user'
import ToDoTask from './components/ToDoTask.vue'
import type { WorkplaceTotal } from './types'
import {
  pieOptions,
  barOptions,
  lineOption,
  mapOptions,
  doubleBarOption,
  createDoubleBarOption,
  createLineOption,
  createMapOptions
} from './echarts-data'
import { CustomerServiceIssuesApi } from '@/api/customerResponse/customerserviceissues'
import { DataApi } from '@/api/reliability/reliabilitydata'
import { useRouter, useRoute } from 'vue-router'
import { useTagsView } from '@/hooks/web/useTagsView'

defineOptions({ name: 'Index' })
// 注册地图
echarts?.registerMap('china', china as any)
const { t } = useI18n()
const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
// const { setWatermark } = useWatermark()
const loading = ref(true)
const avatar = userStore.getUser.avatar
const username = userStore.getUser.nickname
const pieOptionsData = reactive<EChartsOption>(pieOptions) as EChartsOption
// 获取统计数
let totalSate = reactive<WorkplaceTotal>({
  project: 0,
  access: 0,
  todo: 0
})
const { refreshPage } = useTagsView()

// 待办组件引用
const todoTaskRef = ref()

// 当前日期
const currentDate = computed(() => {
  const now = new Date()
  const dateStr = now.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
  const weekdays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
  const weekday = weekdays[now.getDay()]
  return `${dateStr} ${weekday}`
})

const getCount = async () => {
  const data = {
    project: 40,
    access: 2340,
    todo: 10
  }
  totalSate = Object.assign(totalSate, data)
}

// 用户来源
const getUserAccessSource = async () => {
  const data = [
    { value: 335, name: 'analysis.directAccess' },
    { value: 310, name: 'analysis.mailMarketing' },
    { value: 234, name: 'analysis.allianceAdvertising' },
    { value: 135, name: 'analysis.videoAdvertising' },
    { value: 1548, name: 'analysis.searchEngines' }
  ]
  set(
    pieOptionsData,
    'legend.data',
    data.map((v) => t(v.name))
  )
  pieOptionsData!.series![0].data = data.map((v) => {
    return {
      name: t(v.name),
      value: v.value
    }
  })
}
const barOptionsData = reactive<EChartsOption>(barOptions) as EChartsOption
const lineOptionData = reactive<EChartsOption>(lineOption) as EChartsOption
const mapOptionsData = reactive<EChartsOption>(mapOptions) as EChartsOption
const doubleBarOptionData = reactive<EChartsOption>(doubleBarOption) as EChartsOption

// 周活跃量
const getWeeklyUserActivity = async () => {
  const data = [
    { value: 13253, name: 'analysis.monday' },
    { value: 34235, name: 'analysis.tuesday' },
    { value: 26321, name: 'analysis.wednesday' },
    { value: 12340, name: 'analysis.thursday' },
    { value: 24643, name: 'analysis.friday' },
    { value: 1322, name: 'analysis.saturday' },
    { value: 1324, name: 'analysis.sunday' }
  ]
  set(
    barOptionsData,
    'xAxis.data',
    data.map((v) => t(v.name))
  )
  set(barOptionsData, 'series', [
    {
      name: t('analysis.activeQuantity'),
      data: data.map((v) => v.value),
      type: 'bar'
    }
  ])
}

// 获取客户响应统计数据
const getCustomerResponseStatistics = async () => {
  try {
    // axios 配置中 request.get 已经返回 res.data，所以 res 就是数据本身
    const data = await CustomerServiceIssuesApi.getMonthlyStatistics()

    if (data && data.months && Array.isArray(data.months) && data.months.length > 0) {
      // 使用 API 数据更新图表配置
      const newOption = createDoubleBarOption({
        months: data.months,
        respondedCounts: data.respondedCounts || [],
        unrespondedCounts: data.unrespondedCounts || []
      })

      console.log('[getCustomerResponseStatistics] 生成的图表配置:', newOption)

      // 更新响应式数据 - 直接替换整个对象以确保响应式更新
      Object.assign(doubleBarOptionData, newOption)

      // 确保关键属性被正确更新
      if (newOption.series) {
        doubleBarOptionData.series = newOption.series as any
      }
      if (newOption.xAxis) {
        doubleBarOptionData.xAxis = newOption.xAxis as any
      }
      if (newOption.yAxis) {
        doubleBarOptionData.yAxis = newOption.yAxis as any
      }
    } else {
      console.warn('[getCustomerResponseStatistics] 数据为空或格式不正确:', {
        data,
        hasMonths: !!data?.months,
        monthsLength: data?.months?.length
      })
    }
  } catch (error) {
    console.error('[getCustomerResponseStatistics] 获取客户响应统计数据失败:', error)
    // 即使失败也显示空图表，避免页面报错
    const emptyOption = createDoubleBarOption()
    Object.assign(doubleBarOptionData, emptyOption)
  }
}

const getReliability = async () => {
  try {
    const data = await DataApi.getDataStatCurrentYear()

    if (data && Array.isArray(data) && data.length > 0) {
      // 使用 API 数据更新图表配置
      const newOption = createLineOption(data)
      // 更新线图数据
      Object.assign(lineOptionData, newOption)
      // 确保关键属性被正确更新
      if (newOption.series) {
        lineOptionData.series = newOption.series as any
      }
      if (newOption.xAxis) {
        lineOptionData.xAxis = newOption.xAxis as any
      }
      if (newOption.yAxis) {
        lineOptionData.yAxis = newOption.yAxis as any
      }
    } else {
      console.warn('[getReliability] 数据为空或格式不正确:', data)
    }
  } catch (error) {
    // 即使失败也显示空图表，避免页面报错
    const emptyOption = createLineOption()
    Object.assign(lineOptionData, emptyOption)
  }
}

const getProvinceCustomer = async () => {
  try {
    const data = await CustomerServiceIssuesApi.getProvinceCustomerCount()

    if (data && Array.isArray(data) && data.length > 0) {
      // 使用 API 数据更新图表配置
      const newOption = createMapOptions(data)

      // 更新线图数据
      Object.assign(mapOptionsData, newOption)
    } else {
      console.warn('[getProvinceCustomer] 数据为空或格式不正确:', data)
    }
  } catch (error) {
    // 即使失败也显示空图表，避免页面报错
    const emptyOption = createMapOptions()
    Object.assign(mapOptionsData, emptyOption)
  }
}
const getAllApi = async () => {
  await Promise.all([
    getCount(),
    getUserAccessSource(),
    getWeeklyUserActivity(),
    getCustomerResponseStatistics(),
    getReliability(),
    getProvinceCustomer()
  ])
  // 刷新待办组件数据
  todoTaskRef.value?.refresh()
  loading.value = false
}

getAllApi()
//  定义定时器变量
let refreshTimer: number | null = null

// 组件挂载时启动定时器
onMounted(() => {
  // 1分钟刷新一次首页数据
  refreshTimer = window.setInterval(
    () => {
      console.log('首页数据自动刷新...')
      // 只刷新首页数据，不刷新整个页面
      getAllApi()
    },
    5 * 60 * 1000
  )
})

// 4. 组件卸载时清除定时器
onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
})
</script>
<style scoped>
.dashboard-container {
  --header-height: 60px;

  width: 100%;
  height: 100%;
  min-height: calc(100vh - var(--header-height));
  padding: 16px;
  font-family: '微软雅黑', sans-serif;
  background: #f5f6f7;
  box-sizing: border-box;
}

.dashboard-main {
  width: 100%;
  max-width: 1400px;
  margin: 0 auto;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 16px;
  margin-bottom: 16px;
  justify-content: center;
}

.card {
  display: flex;
  padding: 16px;
  color: white;
  background: linear-gradient(135deg, #1890ff, #0050b3);
  border-radius: 8px;
  box-shadow: 0 2px 8px rgb(0 0 0 / 15%);
  flex-direction: column;
}

.card-title {
  font-size: 14px;
  opacity: 0.8;
}

.card-value {
  margin: 8px 0;
  font-size: clamp(1.2rem, 2.5vw, 1.8rem);
  font-weight: bold;
}

.card-desc {
  font-size: 12px;
  opacity: 0.9;
}

.charts-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-top: 16px;
}

.charts-row:last-child {
  grid-template-columns: 1fr 1fr 1fr;
}

.chart-container {
  position: relative;
  padding: 16px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgb(0 0 0 / 15%);
}

.customer-count-badge {
  position: absolute;
  top: 20px;
  right: 20px;
  z-index: 10;
  padding: 5px 10px;
  font-size: 14px;
  color: white;
  background: rgb(0 0 0 / 70%);
  border-radius: 10px;
}

/* @media (max-width: 768px) {
  .charts-row {
    grid-template-columns: 1fr;
  }

  .charts-row:last-child {
    grid-template-columns: 1fr;
  }

  .dashboard-container {
    padding: 10px;
  }
} */
</style>
