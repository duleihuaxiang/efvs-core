export interface Position {
  x: number;
  y: number;
  z: number;
}

export interface AttitudeDeg {
  pitch: number;
  roll: number;
  yaw: number;
  /** 跑道整体磁航向（度），有效滑跑阶段磁航向中位数，爬升/进近阶段有值，用于 3D 跑道朝向 */
  runwayMagneticHeadingMedian?: number;
}

export interface Deviation {
  loc: number;
  gs: number;
}

export interface FlightData {
  sampleRate: number;
  time: number[];
  position: Position[];
  attitudeDeg: AttitudeDeg[];
  deviation: Deviation[];
  /** 每个数据点对应的飞行阶段：TAKEOFF_ROLL-滑跑阶段, CLIMB-爬升阶段, APPROACH-进近状态, TAXI-滑行状态 */
  flightPhases?: string[];
  /**
   * Processor 算出的接地点沿跑道纵轴距跑道头 X=0 的米数（与 mapToFlightData / 场景一致）
   */
  touchdownXOnRunwayM?: number | null;
}

export interface ThreeController {
  setIndex: (index: number) => void;
  dispose: () => void;
}

/** 从WebSocket接收的实时数据项 */
export interface RealtimeDataItem {
  /** 累计距离(m) */
  cumulativeDistance?: number
  /** 航向道偏差 */
  locDeviation?: number
  /** 无线电高度(ft) */
  radioAltitude?: number
  /** 空速(km/h) */
  airspeed?: number
  /** 选择速度(km/h) */
  selectedSpeed?: number
  /** 仰角(°) */
  pitchAngle?: number
  /** 坡度(°) */
  slope?: number
  /** 磁航向(°) */
  magneticHeading?: number
  /** 下滑道偏差 */
  glideslopeDeviation?: number
  /** 飞行阶段 */
  flightPhase?: string
  /** 经度 */
  longitude?: number
  /** 纬度 */
  latitude?: number
}