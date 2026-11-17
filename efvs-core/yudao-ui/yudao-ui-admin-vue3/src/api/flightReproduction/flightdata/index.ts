import request from '@/config/axios'
import type { Dayjs } from 'dayjs';

/** 飞行复现信息 */
export interface FlightData {
          id: number; // 飞行id
          time: string | Dayjs; // 时间
          beijingTime: string | Dayjs; // 北京时间
          date: string | Dayjs; // 日期
          airGroundSwitch: string; // 空地电门状态
          frontAirGroundSwitch: string; // 前空地电门状态
          leftMainAirGroundSwitch: string; // 左主空地电门状态
          rightMainAirGroundSwitch: string; // 右主空地电门状态
          vorLocCapture: string; // VOR/LOC截获状态
          glideslopeCapture: string; // 下滑道截获状态
          leftFlightDirector: string; // 左飞行指引状态
          rightFlightDirector: string; // 右飞行指引状态
          hudUsage: string; // HUD使用状态
          hudAiiiCapability: string; // HUD AIII能力
          hudAiiiMode: string; // HUD AIII模式
          hudApproachWarning: string; // HUD进近警告
          cautionTerrain: string; // CAUTION TERRAIN警告
          terrainCaution: string; // TERRAIN CAUTION警告
          tooLowTerrain: string; // TOO LOW TERRAIN警告
          tooLowFlaps: string; // TOO LOW FLAPS警告
          tooLowGear: string; // TOO LOW GEAR警告
          dontSink: string; // Don't Sink警告
          fmcNavGps: string; // FMC/NAV/GPS状态
          gpArmed: string; // GP预位状态
          gpCaptured: string; // GP截获状态
          speedbrakeArmedLight: string; // 减速板预位灯状态
          speedbrakeDisarmedLight: string; // 减速板未预位灯状态
          flapUnload: string; // 襟翼卸载状态
          leftStallShaker: string; // 左抖杆状态
          rightStallShaker: string; // 右抖杆状态
          leftWeatherPosition: string; // 左气象位状态
          rightWeatherPosition: string; // 右气象位状态
          leftTerrainPosition: string; // 左地形位状态
          rightTerrainPosition: string; // 右地形位状态
          autopilotStatus: string; // 自动驾驶状态
          cwsA: string; // CWS A模式状态
          cwsB: string; // CWS B模式状态
          cwsPitch: string; // CWS PITCH模式状态
          cwsRoll: string; // CWS ROLL模式状态
          cwsWarning: string; // CWS警告状态
          alternateFlapSwitch: string; // 备用放襟翼电门状态
          mainAlternateBrakeSel: string; // 主用/备用刹车选择
          verticalLoad: number; // 垂直载荷
          barometricAltitude: number; // 气压高度
          altitudeAboveAirport: number; // 机场上高度
          radioAltitude: number; // 无线电高度
          airspeed: number; // 空速
          leftFdPitchDeviation: number; // 左飞行指引仰角偏差
          rightFdPitchDeviation: number; // 右飞行指引仰角偏差
          rollAngle: number; // 坡度
          controlWheelPosition: number; // 驾驶盘位置
          leftFdRollDeviation: number; // 左飞行指引坡度偏差
          rightFdRollDeviation: number; // 右飞行指引坡度偏差
          magneticHeading: number; // 磁航向
          windDirection: number; // 风向
          windSpeed: number; // 风速
          glideslopeDeviation: number; // 下滑道偏差
          localizerDeviation: number; // 航向道偏差
          barometricDatum: number; // 气压基准
          leftIlsFrequency: number; // 左ILS频率
          rightIlsFrequency: number; // 右ILS频率
          leftVorFrequency: number; // 左VOR频率
          rightVorFrequency: number; // 右VOR频率
          grossWeight: number; // 全重
          vrefSpeed: number; // VREF速度
          selectedSpeed: number; // 选择速度
          staticTemperature: number; // 静温
          totalTemperature: number; // 全温
          verticalLoadMin: number; // 垂直载荷最小值
          machNumber: number; // 马赫数
          selectedMachNumber: number; // 选择马赫数
          leftMinStandardRef: string; // 左最低标准基准选择
          rightMinStandardRef: string; // 右最低标准基准选择
          leftBaroMinMsd: number; // 左气压最低标准（MSD）
          leftBaroMinLsd: number; // 左气压最低标准（LSD）
          rightBaroMinMsd: number; // 右气压最低标准（MSD）
          rightBaroMinLsd: number; // 右气压最低标准（LSD）
          leftRadioMinStandard: number; // 左无线电最低标准
          rightRadioMinStandard: number; // 右无线电最低标准
          pitchTrim: number; // 俯仰配平
          rudderPedalPosition: number; // 方向舵踏板位置
          rudderPosition: number; // 方向舵位置
          minimumManeuveringSpeed: number; // 最小机动速度
          stallShakerSpeed: number; // 抖杆速度
          leftDetectionRange: number; // 左探测范围
          rightDetectionRange: number; // 右探测范围
          leftFlapRaw: number; // 左襟翼(原始值)
          rightFlapRaw: number; // 右襟翼(原始值)
          leftSelectedCourse: number; // 左选择航道
          rightSelectedCourse: number; // 右选择航道
          v1Speed: number; // V1速度
          vrSpeed: number; // VR速度
          v2Speed: number; // V2速度
          leftMainBrakePressure: number; // 左主刹车压力
          rightMainBrakePressure: number; // 右主刹车压力
          leftAltBrakePressure: number; // 左备用刹车压力
          rightAltBrakePressure: number; // 右备用刹车压力
          longitude: number; // 经度
          latitude: number; // 纬度
          trackAngle: number; // 航迹角
          targetTrackAngle: number; // 目标航迹角
          leftCenterDisplay: string; // 左中心显示
          rightCenterDisplay: string; // 右中心显示
          lowerCenterDisplayUnit: string; // 中央下显示组件
          upperCenterDisplayUnit: string; // 中央上显示组件
          leftInnerDisplayUnit: string; // 左内显示组件
          leftOuterDisplayUnit: string; // 左外显示组件
          rightInnerDisplayUnit: string; // 右内显示组件
          rightOuterDisplayUnit: string; // 右外显示组件
          altBaro: string; // _ALT_BARO
          altMode: string; // _ALT_MODE
          remark: string; // 备注
  }

// 飞行复现 API
export const FlightDataApi = {
  // 查询飞行复现分页
  getFlightDataPage: async (params: any) => {
    return await request.get({ url: `/flightReproduction/flight-data/page`, params })
  },

  // 查询飞行复现详情
  getFlightData: async (id: number) => {
    return await request.get({ url: `/flightReproduction/flight-data/get?id=` + id })
  },

  // 新增飞行复现
  createFlightData: async (data: FlightData) => {
    return await request.post({ url: `/flightReproduction/flight-data/create`, data })
  },

  // 修改飞行复现
  updateFlightData: async (data: FlightData) => {
    return await request.put({ url: `/flightReproduction/flight-data/update`, data })
  },

  // 删除飞行复现
  deleteFlightData: async (id: number) => {
    return await request.delete({ url: `/flightReproduction/flight-data/delete?id=` + id })
  },

  /** 批量删除飞行复现 */
  deleteFlightDataList: async (ids: number[]) => {
    return await request.delete({ url: `/flightReproduction/flight-data/delete-list?ids=${ids.join(',')}` })
  },

  // 导出飞行复现 Excel
  exportFlightData: async (params) => {
    return await request.download({ url: `/flightReproduction/flight-data/export-excel`, params })
  },
  /** 导入飞行复现 Excel */
  importFlightData: (data: FormData) => {
    return request.post({
      url: '/flightReproduction/flight-data/import-excel',
      data,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  }
}
