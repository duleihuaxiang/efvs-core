import { EChartsOption } from 'echarts'
import { areaReplace } from '@/utils'

const { t } = useI18n()
interface ProvinceData {
  name: string
  inFlight: number
  stopped: number
}

export const lineOptions: EChartsOption = {
  title: {
    text: t('analysis.monthlySales'),
    left: 'center'
  },
  xAxis: {
    data: [
      t('analysis.january'),
      t('analysis.february'),
      t('analysis.march'),
      t('analysis.april'),
      t('analysis.may'),
      t('analysis.june'),
      t('analysis.july'),
      t('analysis.august'),
      t('analysis.september'),
      t('analysis.october'),
      t('analysis.november'),
      t('analysis.december')
    ],
    boundaryGap: false,
    axisTick: {
      show: false
    }
  },
  grid: {
    left: 20,
    right: 20,
    bottom: 20,
    top: 80,
    containLabel: true
  },
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'cross'
    },
    padding: [5, 10]
  },
  yAxis: {
    axisTick: {
      show: false
    }
  },
  legend: {
    data: [t('analysis.estimate'), t('analysis.actual')],
    top: 50
  },
  series: [
    {
      name: t('analysis.estimate'),
      smooth: true,
      type: 'line',
      data: [100, 120, 161, 134, 105, 160, 165, 114, 163, 185, 118, 123],
      animationDuration: 2800,
      animationEasing: 'cubicInOut'
    },
    {
      name: t('analysis.actual'),
      smooth: true,
      type: 'line',
      itemStyle: {},
      data: [120, 82, 91, 154, 162, 140, 145, 250, 134, 56, 99, 123],
      animationDuration: 2800,
      animationEasing: 'quadraticOut'
    }
  ]
}

export const pieOptions: EChartsOption = {
  title: {
    text: t('analysis.userAccessSource'),
    left: 'center'
  },
  tooltip: {
    trigger: 'item',
    formatter: '{a} <br/>{b} : {c} ({d}%)'
  },
  legend: {
    orient: 'vertical',
    left: 'left',
    data: [
      t('analysis.directAccess'),
      t('analysis.mailMarketing'),
      t('analysis.allianceAdvertising'),
      t('analysis.videoAdvertising'),
      t('analysis.searchEngines')
    ]
  },
  series: [
    {
      name: t('analysis.userAccessSource'),
      type: 'pie',
      radius: '55%',
      center: ['50%', '60%'],
      data: [
        { value: 335, name: t('analysis.directAccess') },
        { value: 310, name: t('analysis.mailMarketing') },
        { value: 234, name: t('analysis.allianceAdvertising') },
        { value: 135, name: t('analysis.videoAdvertising') },
        { value: 1548, name: t('analysis.searchEngines') }
      ]
    }
  ]
}

export const barOptions: EChartsOption = {
  title: {
    text: t('analysis.weeklyUserActivity'),
    left: 'center'
  },
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'shadow'
    }
  },
  grid: {
    left: 50,
    right: 20,
    bottom: 20
  },
  xAxis: {
    type: 'category',
    data: [
      t('analysis.monday'),
      t('analysis.tuesday'),
      t('analysis.wednesday'),
      t('analysis.thursday'),
      t('analysis.friday'),
      t('analysis.saturday'),
      t('analysis.sunday')
    ],
    axisTick: {
      alignWithLabel: true
    }
  },
  yAxis: {
    type: 'value'
  },
  series: [
    {
      name: t('analysis.activeQuantity'),
      data: [13253, 34235, 26321, 12340, 24643, 1322, 1324],
      type: 'bar'
    }
  ]
}

export const radarOption: EChartsOption = {
  legend: {
    data: [t('workplace.personal'), t('workplace.team')]
  },
  radar: {
    // shape: 'circle',
    indicator: [
      { name: t('workplace.quote'), max: 65 },
      { name: t('workplace.contribution'), max: 160 },
      { name: t('workplace.hot'), max: 300 },
      { name: t('workplace.yield'), max: 130 },
      { name: t('workplace.follow'), max: 100 }
    ]
  },
  series: [
    {
      name: `xxx${t('workplace.index')}`,
      type: 'radar',
      data: [
        {
          value: [42, 30, 20, 35, 80],
          name: t('workplace.personal')
        },
        {
          value: [50, 140, 290, 100, 90],
          name: t('workplace.team')
        }
      ]
    }
  ]
}

export const wordOptions = {
  series: [
    {
      type: 'wordCloud',
      gridSize: 2,
      sizeRange: [12, 50],
      rotationRange: [-90, 90],
      shape: 'pentagon',
      width: 600,
      height: 400,
      drawOutOfBound: true,
      textStyle: {
        color: function () {
          return (
            'rgb(' +
            [
              Math.round(Math.random() * 160),
              Math.round(Math.random() * 160),
              Math.round(Math.random() * 160)
            ].join(',') +
            ')'
          )
        }
      },
      emphasis: {
        textStyle: {
          shadowBlur: 10,
          shadowColor: '#333'
        }
      },
      data: [
        {
          name: 'Sam S Club',
          value: 10000,
          textStyle: {
            color: 'black'
          },
          emphasis: {
            textStyle: {
              color: 'red'
            }
          }
        },
        {
          name: 'Macys',
          value: 6181
        },
        {
          name: 'Amy Schumer',
          value: 4386
        },
        {
          name: 'Jurassic World',
          value: 4055
        },
        {
          name: 'Charter Communications',
          value: 2467
        },
        {
          name: 'Chick Fil A',
          value: 2244
        },
        {
          name: 'Planet Fitness',
          value: 1898
        },
        {
          name: 'Pitch Perfect',
          value: 1484
        },
        {
          name: 'Express',
          value: 1112
        },
        {
          name: 'Home',
          value: 965
        },
        {
          name: 'Johnny Depp',
          value: 847
        },
        {
          name: 'Lena Dunham',
          value: 582
        },
        {
          name: 'Lewis Hamilton',
          value: 555
        },
        {
          name: 'KXAN',
          value: 550
        },
        {
          name: 'Mary Ellen Mark',
          value: 462
        },
        {
          name: 'Farrah Abraham',
          value: 366
        },
        {
          name: 'Rita Ora',
          value: 360
        },
        {
          name: 'Serena Williams',
          value: 282
        },
        {
          name: 'NCAA baseball tournament',
          value: 273
        },
        {
          name: 'Point Break',
          value: 265
        }
      ]
    }
  ]
}

export const createLineOption = (data?: any[]): EChartsOption => {
  return {
    title: {
      text: '可靠性趋势'
    },
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: data?.map((item) => `${item.month.toString()}月`)
    },
    yAxis: {
      type: 'value',
      name: '可靠性'
    },
    series: [
      {
        data: data?.map((item) => item.issueCount),
        type: 'line',
        smooth: false,
        itemStyle: {
          color: '#436EEE'
        },
        symbol: 'circle',
        symbolSize: 8,
        symbolRotate: 0, // 不旋转
        symbolKeepAspect: true // 保持宽高比
      }
    ]
  }
}
export const lineOption: EChartsOption = createLineOption()

export const createMapOptions = (data?: any[]): EChartsOption => {
  // 适配 echarts map 的省份名称（china.json 中一般为“上海/北京”，而接口可能返回“上海市/北京市/香港特别行政区”等）
  const normalizeProvinceName = (name: string) => {
    if (!name) return name
    return areaReplace(name).replace('特别行政区', '').replace('市', '')
  }

  // 动态计算 visualMap 的 min 和 max 值
  let min = 0
  let max = 5000 // 默认值，如果没有数据时使用

  if (data && Array.isArray(data) && data.length > 0) {
    // 计算实际数据中的最小值和最大值
    const values = data.map((item) => Number(item?.provinceCustomerCount) || 0)
    min = Math.min(...values)
    max = Math.max(...values)

    // 如果最大值和最小值相同，设置一个合理的范围
    if (min === max) {
      max = min + 1
    }
  }

  return {
    title: {
      text: '各地客户分布',
      left: 'center'
    },
    tooltip: {
      trigger: 'item',
      formatter: function (params) {
        /*       const count =
          params?.data?.extra?.provinceCustomerCount ?? params?.data?.value ?? params?.value ?? 0
        return `${params.name}<br/>客户数量: ${count}` */
        const raw = params?.data?.extra?.provinceCustomerCount ?? params?.data?.value
        const count = Number.isFinite(Number(raw)) ? Number(raw) : 0
        return `${params.name}<br/>客户数量: ${count}`
      }
    },
    visualMap: {
      min: min,
      max: max,
      left: 'left',
      top: 'bottom',
      text: ['高', '低'],
      calculable: true,
      inRange: {
        color: ['#e0ffff', '#006edd']
      }
    },
    series: [
      {
        name: '客户数量',
        type: 'map',
        map: 'china',
        roam: false,
        emphasis: {
          label: {
            show: true
          }
          // itemStyle: { color: '#EAB308' }
        },
        // 核心：单系列数据，value 映射 visualMap（选 inFlight 作为主视觉），extra 存双数值
        data: data?.map((item) => ({
          name: normalizeProvinceName(item?.provinceName),
          value: Number(item?.provinceCustomerCount) || 0, // visualMap 基于此值着色
          extra: {
            provinceCustomerCount: Number(item?.provinceCustomerCount) || 0
          }
        }))
      }
    ]
  }
}
/* const mergedData: ProvinceData[] = [
  { name: '北京', inFlight: 12, stopped: 12 },
  { name: '天津', inFlight: 800, stopped: 8 },
  { name: '上海', inFlight: 2000, stopped: 20 },
  { name: '重庆', inFlight: 1500, stopped: 15 },
  { name: '河北', inFlight: 3000, stopped: 30 },
  { name: '河南', inFlight: 4000, stopped: 40 },
  { name: '云南', inFlight: 1800, stopped: 18 },
  { name: '辽宁', inFlight: 2200, stopped: 22 },
  { name: '黑龙江', inFlight: 1600, stopped: 16 },
  { name: '湖南', inFlight: 2500, stopped: 2 },
  { name: '安徽', inFlight: 2300, stopped: 23 },
  { name: '山东', inFlight: 3500, stopped: 35 },
  { name: '新疆', inFlight: 1200, stopped: 12 },
  { name: '江苏', inFlight: 3200, stopped: 32 },
  { name: '浙江', inFlight: 2800, stopped: 28 },
  { name: '江西', inFlight: 1800, stopped: 18 },
  { name: '湖北', inFlight: 2600, stopped: 26 },
  { name: '广西', inFlight: 1900, stopped: 19 },
  { name: '甘肃', inFlight: 1400, stopped: 14 },
  { name: '山西', inFlight: 1700, stopped: 17 },
  { name: '内蒙古', inFlight: 1300, stopped: 13 },
  { name: '陕西', inFlight: 2100, stopped: 21 },
  { name: '吉林', inFlight: 1500, stopped: 15 },
  { name: '福建', inFlight: 2400, stopped: 24 },
  { name: '贵州', inFlight: 1600, stopped: 16 },
  { name: '广东', inFlight: 4200, stopped: 42 },
  { name: '青海', inFlight: 900, stopped: 9 },
  { name: '西藏', inFlight: 700, stopped: 7 },
  { name: '四川', inFlight: 3100, stopped: 31 },
  { name: '宁夏', inFlight: 1000, stopped: 10 },
  { name: '海南', inFlight: 1100, stopped: 11 },
  { name: '台湾', inFlight: 1800, stopped: 18 },
  { name: '香港', inFlight: 1300, stopped: 13 },
  { name: '澳门', inFlight: 500, stopped: 50 }
] */

export const mapOptions: EChartsOption = createMapOptions()

// 创建基础配置，数据将通过 API 动态加载
export const createDoubleBarOption = (data?: {
  months: number[]
  respondedCounts: number[]
  unrespondedCounts: number[]
}): EChartsOption => {
  // 计算 Y 轴最大值（用于 tooltip 位置计算）
  let yAxisMax = 18 // 默认值
  if (data && data.respondedCounts && data.unrespondedCounts) {
    const allValues = [...data.respondedCounts, ...data.unrespondedCounts]
    const maxValue = Math.max(...allValues, 0)
    yAxisMax = maxValue > 0 ? maxValue * 1.2 : 18 // 增加 20% 的余量
  }
  // 如果没有数据，返回空配置
  if (!data || !data.months || data.months.length === 0) {
    return {
      title: {
        text: '客户响应总数，已响应数量',
        textStyle: {
          fontSize: 16,
          fontWeight: 'bold'
        },
        right: '30%'
      },
      xAxis: {
        type: 'category',
        data: []
      },
      yAxis: {
        type: 'value',
        name: '数量'
      },
      series: []
    }
  }

  // 总响应数量就是 respondedCounts（后端已经返回该月所有记录数）
  const totalCounts = data.respondedCounts

  // 计算最大值和最小值用于 markPoint
  const unrespondedMax = Math.max(...data.unrespondedCounts)
  const unrespondedMaxIndex = data.unrespondedCounts.indexOf(unrespondedMax)
  const unrespondedMin = Math.min(...data.unrespondedCounts.filter((v) => v > 0))
  const unrespondedMinIndex = data.unrespondedCounts.indexOf(unrespondedMin)

  const totalMax = Math.max(...totalCounts)
  const totalMaxIndex = totalCounts.indexOf(totalMax)
  const totalMin = Math.min(...totalCounts.filter((v) => v > 0))
  const totalMinIndex = totalCounts.indexOf(totalMin)

  return {
    title: {
      text: '客户响应总数，已响应数量',
      textStyle: {
        fontSize: 16,
        fontWeight: 'bold'
      },
      right: '30%'
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      /* position: function (point: any, params: any, dom: any, rect: any, size: any) {
        // 将 tooltip 显示在柱状图上方
        const tooltipHeight = size.contentSize[1] || 50
        const tooltipWidth = size.contentSize[0] || 100

        // 获取当前数据点的最大值（两个柱状图中较高的那个）
        let maxValue = 0
        if (params && params.length > 0) {
          params.forEach((param: any) => {
            if (param.value > maxValue) {
              maxValue = param.value
            }
          })
        }

        // 计算 Y 轴最大值（用于计算柱状图高度比例）
        const allValues = [...data.respondedCounts, ...data.unrespondedCounts]
        const calculatedYAxisMax = Math.max(...allValues, 0) * 1.2 || 18

        // 计算柱状图顶部的位置
        // rect.y 是图表区域的顶部 y 坐标
        // rect.height 是图表区域的高度
        // 柱状图顶部 = rect.y + rect.height * (1 - 数据值/最大值)
        // 使用 yAxisMax（闭包变量）来计算
        const effectiveYAxisMax = Math.max(yAxisMax, calculatedYAxisMax, maxValue * 1.2)
        const barHeightRatio = maxValue / effectiveYAxisMax // 柱状图高度比例
        const barTopY = rect.y + rect.height * (1 - barHeightRatio) // 柱状图顶部的 y 坐标

        // 将 tooltip 放在柱状图顶部上方，水平居中
        return [point[0] - tooltipWidth / 2, barTopY - tooltipHeight - 10]
      }, */
      // confine: true, // 确保 tooltip 不超出图表区域
      formatter: (params: any) => {
        return `<div style="padding: 5px; font-size: 12px;">
          <span style="color: #436EEE;">已响应: ${params[0].value}</span><br/>
          <span style="color: #B8D661;">总响应: ${params[1].value}</span>
        </div>`
      }
    },
    legend: {
      data: ['已响应', '总响应数量'],
      bottom: 0,
      textStyle: {
        fontSize: 12
      }
    },
    xAxis: {
      type: 'category',
      data: data.months.map((m) => m.toString()),
      axisLabel: {
        fontSize: 12
      }
    },
    yAxis: {
      type: 'value',
      name: '数量',
      axisLabel: {
        fontSize: 12
      }
    },
    series: [
      {
        name: '已响应',
        type: 'bar',
        barWidth: '35%',
        barGap: '20%', // 同一系列的柱状图之间的间距
        data: data.unrespondedCounts,
        itemStyle: {
          color: '#5470c6'
        },
        /*  label: {
          show: true,
          position: 'top',
          formatter: (params: any) => {
            return params.value > 0 ? params.value : ''
          },
          textStyle: {
            fontSize: 12,
            color: '#333'
          },
          distance: 5 // 标签距离柱状图顶部的距离
          // 不使用 offset，使用 position: 'top' 和 distance 来控制位置
        }, */
        markPoint:
          unrespondedMax > 0
            ? {
                data: [
                  {
                    type: 'max',
                    name: '最大值',
                    coord: [data.months[unrespondedMaxIndex].toString(), unrespondedMax],
                    label: { show: true, formatter: unrespondedMax.toString() }
                  },
                  unrespondedMin > 0
                    ? {
                        type: 'min',
                        name: '最小值',
                        coord: [data.months[unrespondedMinIndex].toString(), unrespondedMin],
                        label: { show: true, formatter: unrespondedMin.toString() }
                      }
                    : null
                ].filter(Boolean) as any
              }
            : undefined,
        emphasis: {
          focus: 'series'
        }
      },
      {
        name: '总响应数量',
        type: 'bar',
        barWidth: '35%',
        data: totalCounts,
        itemStyle: {
          color: '#91cc75'
        },
        /*   label: {
          show: true,
          position: 'top',
          formatter: (params: any) => {
            return params.value > 0 ? params.value : ''
          },
          textStyle: {
            fontSize: 12,
            color: '#333'
          },
          distance: 5 // 标签距离柱状图顶部的距离
          // 不使用 offset，使用 position: 'top' 和 distance 来控制位置
        }, */
        markPoint:
          totalMax > 0
            ? {
                data: [
                  {
                    type: 'max',
                    name: '最大值',
                    coord: [data.months[totalMaxIndex].toString(), totalMax],
                    label: { show: true, formatter: totalMax.toString() }
                  },
                  totalMin > 0
                    ? {
                        type: 'min',
                        name: '最小值',
                        coord: [data.months[totalMinIndex].toString(), totalMin],
                        label: { show: true, formatter: totalMin.toString() }
                      }
                    : null
                ].filter(Boolean) as any
              }
            : undefined,
        emphasis: {
          focus: 'series'
        }
      }
    ]
  }
}

export const doubleBarOption: EChartsOption = createDoubleBarOption()
