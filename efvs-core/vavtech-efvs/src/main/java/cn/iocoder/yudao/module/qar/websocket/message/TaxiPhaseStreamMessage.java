package cn.iocoder.yudao.module.qar.websocket.message;

import lombok.Data;
import java.util.List;

/**
 * 滑跑阶段数据流消息（后端 -> 前端）
 * 
 * @author admin
 */
@Data
public class TaxiPhaseStreamMessage {

    /**
     * 消息类型：data-数据, status-状态
     */
    private String type;

    /**
     * 滑跑阶段LOC数据列表
     */
    private List<TaxiPhaseDataItem> data;

    /**
     * 当前速度倍数
     */
    private Integer speed;

    /**
     * 当前已传输的记录数
     */
    private Long totalCount;

    /**
     * 状态信息
     */
    private String status;

    /**
     * 当前周期编号（用于标识是第几次滑跑）
     */
    private Integer cycleIndex;

    /**
     * 3D 复现用：接地点沿跑道纵轴距跑道头 X=0 的米数（与 FlightDataProcessor 算法一致）。
     *
     * <p>用于 3D 黄色光柱、进近/滑行段衔接锚点；前端不再自行判断与计算。</p>
     */
    private Double touchdownXOnRunwayM;

    // ==================== 进近落地点几何计算结果 ====================

    /**
     * 跑道入口点 i 的纬度。
     *
     * <p>计算原理：飞机在 m 点时 mi⊥oi，oi = 1000ft（GS 台到跑道入口距离）。
     * 对每条进近数据行：horiz_dist_ft = radioAltitude / tan(3° + glideslopeDeviation_miuA × 0.0048°)，
     * 当 horiz_dist_ft 最接近 1000ft 时，该行经纬度即为点 i（跑道入口）的地理位置。</p>
     */
    private Double thresholdLat;

    /** 跑道入口点 i 的经度（同 thresholdLat）。 */
    private Double thresholdLon;

    /**
     * 飞机经过 m 点时计算出的到 GS 台水平距离（英尺）。
     * <p>理论值应约为 1000ft；偏差越小代表数据质量越好。</p>
     */
    private Double thresholdHorizDistFt;

    /**
     * 实际接地点 t 的纬度。
     * <p>取空地电门 emptySpaceSwitch 由 AIR 跳转为 GROUND（APPROACH→TAXI）的第一条记录经纬度。</p>
     */
    private Double touchdownLat;

    /** 实际接地点 t 的经度（同 touchdownLat）。 */
    private Double touchdownLon;

    /**
     * 跑道入口点 i 到实际接地点 t 的水平距离（米，Haversine 球面公式）。
     * <p>即飞机在跑道上距入口的落地距离，是评估进近精度的关键指标。</p>
     */
    private Double itDistanceMeters;

    /**
     * 滑跑阶段LOC数据项
     */
    @Data
    public static class TaxiPhaseDataItem {
        /**
         * 记录ID
         */
        private Integer id;

        /**
         * QAR 主表 id（架次）。用于对比分析场景按架次分组绘制。
         * <p>
         * 飞行复现场景可不下发（为 null 不影响原有逻辑）。
         */
        private Long qarId;
        
        /**
         * 时间
         */
        private String time;
        
        /**
         * X轴：累计滑跑距离（米），按 airspeed_km 积分计算，所有图表共用
         */
        private Double cumulativeDistance;
        
        /**
         * Y轴：航向道偏差
         */
        private String locDeviation;
        
        /**
         * 地速（用于计算，已废弃，使用airspeed）
         */
        private String groundSpeed;
        
        /**
         * 空速（km/h，用于空速图Y轴）
         */
        private String airspeed;
        
        /**
         * 选择速度（km/h，用于选择速度图Y轴）
         */
        private String selectedSpeed;
        
        /**
         * 仰角（度，用于仰角图Y轴，描述机头相对于地平线的抬升或下俯）
         */
        private String pitchAngle;
        
        /**
         * 坡度（度，用于坡度图Y轴，描述飞机左右倾斜的角度）
         */
        private String slope;
        
        /**
         * 磁航向（度，相对于磁北 0°～359°，用于磁航向图 Y 轴）
         */
        private String magneticHeading;

        /**
         * 跑道整体磁航向（度），当前周期有效滑跑阶段所有磁航向的中位数，用于 3D 姿态复现。
         * 仅爬升/进近阶段有值；一架次多次起降时每段跑道可能不同。
         */
        private Double runwayMagneticHeadingMedian;
        
        /**
         * 无线电高度（英尺，用于 GS 偏差图 X 轴，高度最高 1000 英尺）
         */
        private String radioAltitude;
        
        /**
         * 下滑道偏差×75（用于 GS 偏差图 Y 轴 glideslope_deviation_res）
         */
        private String glideslopeDeviationRes;

        /**
         * 下滑道偏差原始值（miuA，来自 glideslope_deviation 字段，未乘以 75）。
         * <p>用于跑道入口点 i 的几何计算：偏差角 = miuA × 0.0048°，实际角 = 3° + 偏差角。</p>
         */
        private String glideslopeDeviationMiuA;

        /**
         * 下滑道截获状态（如 GS ENGAGE）
         */
        private String glideslopeCapture;

        /**
         * 是否绘制 GS 偏差图：empty_space_switch=AIR 且 radio_altitude<=1100 且 glide_slope_capture=GS ENGAGE 时为 true
         */
        private Boolean gsDrawEnabled;

        /**
         * 飞行阶段：TAKEOFF_ROLL-滑跑阶段, CLIMB-爬升阶段, APPROACH-进近状态, TAXI-滑行状态。
         * 用于前端 7 个图表按阶段过滤/展示数据。
         */
        private String flightPhase;
        
        /**
         * 时间间隔（秒，与前一个点的间隔）
         */
        private Double timeInterval;

        // ---------- 3D 复现用字段 ----------
        /**
         * 经度（度），用于 3D 复现飞机位置
         */
        private String longitude;
        /**
         * 纬度（度），用于 3D 复现飞机位置
         */
        private String latitude;
    }

    /** 飞行阶段常量：供前端与后端统一使用 */
    public static final String PHASE_TAKEOFF_ROLL = "TAKEOFF_ROLL";
    public static final String PHASE_CLIMB = "CLIMB";
    public static final String PHASE_APPROACH = "APPROACH";
    /** 滑行：GROUND + 本流已出现过 AIR */
    public static final String PHASE_TAXI = "TAXI";

}
