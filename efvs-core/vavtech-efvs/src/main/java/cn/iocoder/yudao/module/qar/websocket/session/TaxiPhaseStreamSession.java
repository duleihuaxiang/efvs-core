package cn.iocoder.yudao.module.qar.websocket.session;

import cn.iocoder.yudao.module.qar.websocket.base.BaseStreamSession;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 滑跑阶段数据流Session状态
 * 
 * @author admin
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TaxiPhaseStreamSession extends BaseStreamSession {

    /**
     * 上一条记录的累计距离（用于计算）
     */
    private Double lastCumulativeDistance;

    /**
     * 上一条记录的时间（用于计算时间间隔）
     */
    private String lastTime;

    /**
     * 上一条记录的空速 airspeed_km（用于计算累计距离）
     */
    private String lastGroundSpeed;

    /**
     * QAR 主表 id，不为 null 时只读取 efvs_qar_data 中 qar_id = 该 id 的数据
     */
    private Long qarId;

    /**
     * 本流中是否已出现过 AIR 数据：用于区分 GROUND 是滑跑阶段（未见过 AIR）还是滑行状态（见过 AIR 后的地面）
     */
    private Boolean hasSeenAirInStream;

    /**
     * 本流中是否已进入过进近阶段（用于判定“进近后滑行”）。
     * 当 AIR 点满足进近判定（例如 glide_slope_capture 包含 GS ENGAGE）时置为 true；
     * 之后的 GROUND 点才允许标记为滑行阶段。
     */
    private Boolean hasSeenApproachInStream;

    /**
     * 上一条处理的数据所属阶段：AIR 或 GROUND。用于检测“首个滑行点”（GROUND 紧接在 AIR 后），
     * 进入滑行时累计距离从 0 重新累加，与滑跑阶段计算方式一致。
     */
    private String lastProcessedPhase;

    /**
     * 本流中是否已经历过至少一次滑行段（GROUND 且 hasSeenAirInStream 为 true 的连续段）。
     * 用于在不传 endId 时判断：滑行之后再次出现「GROUND 紧接在 AIR 后」时，应视为新起飞的滑跑，而非滑行。
     */
    private Boolean hasSeenTaxiInStream;

    /**
     * 最近一次 AIR 点的下滑道截获状态（glide_slope_capture，如 GS ENGAGE）。
     * 用于地面阶段 glide_slope_capture 缺失时，按“GROUND + GS ENGAGE”规则判定滑行状态。
     */
    private String lastGlideslopeCapture;

    /**
     * 当前周期内有效滑跑阶段收集到的磁航向（度），用于进入爬升时计算中位数作为跑道整体磁航向，供 3D 姿态复现使用。
     * 每次进入爬升阶段计算完中位数后清空；周期重置时清空。
     */
    private List<Double> takeoffRollMagneticHeadings;

    /**
     * 当前周期跑道整体磁航向（度），为当前周期有效滑跑阶段磁航向的中位数。进入爬升后计算并下发给前端。
     * 周期重置时清空。
     */
    private Double runwayMagneticHeadingMedian;

    public TaxiPhaseStreamSession(String sessionId) {
        super(sessionId);
        this.lastCumulativeDistance = 0.0;
        this.lastTime = null;
        this.lastGroundSpeed = null;
        this.hasSeenAirInStream = false;
        this.hasSeenApproachInStream = false;
        this.hasSeenTaxiInStream = false;
        this.lastGlideslopeCapture = null;
        this.takeoffRollMagneticHeadings = new ArrayList<>();
    }

}
