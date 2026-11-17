package cn.iocoder.yudao.module.qar.service.qardata;

import cn.iocoder.yudao.module.qar.dal.dataobject.qardata.QarDataDO;
import cn.iocoder.yudao.module.qar.dal.mysql.qardata.QarDataMapper;
import cn.iocoder.yudao.module.qar.websocket.message.TaxiPhaseStreamMessage;
import cn.iocoder.yudao.module.qar.websocket.util.SpeedUtils;
import cn.iocoder.yudao.module.qar.websocket.util.TimeUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * taxi-phase 3D 复现：开始播放时一次性返回全部数据。
 *
 * <p>业务逻辑从 {@link cn.iocoder.yudao.module.qar.websocket.service.TaxiPhaseStreamService}
 * 的分页读取 + 阶段判定 + 累计距离计算逻辑复制而来，区别在于本实现不会通过 WebSocket 分批推送，
 * 而是同步聚合为一次响应。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QarTaxiPhasePlaybackService {

    private static final double MAX_TAXI_DISTANCE_METERS = 3600.0;
    /** 与 ThreeScene / 前端常量一致：跑道总长（米） */
    private static final double RUNWAY_LENGTH_M = 3600.0;
    /** 下滑道 GS 台在跑道纵轴的位置：距跑道头（X=0）3300m */
    private static final double GS_STATION_X_FROM_RUNWAY_HEAD_M = 3300.0;
    /** 1000ft -> 米（约 304.8m），用于“入口点”与 O 点距离约束 */
    private static final double FT1000_TO_METERS = 1000.0 * 0.3048;
    /** 接地前搜索 GS/入口 的时间窗（秒），按数据行 time 字段 */
    private static final int GS_SEARCH_WINDOW_SECONDS = 200;
    /** 时间不可解析时，按约 10Hz 估算 200 秒对应的最多回溯采样数 */
    private static final int GS_SEARCH_FALLBACK_MAX_SAMPLES = GS_SEARCH_WINDOW_SECONDS * 10;

    /**
     * 与 WebSocket 1 倍速对应：DEFAULT_RECORDS_PER_SECOND=10
     */
    private static final int BATCH_SIZE = 10;

    private final QarDataMapper qarDataMapper;

    /**
     * 构建一次性返回的 taxi-phase 数据。
     *
     * @param qarId   QAR 主表 id
     * @param startId 起始记录 id（可选）
     * @param endId   结束记录 id（可选，仅用于限制返回范围；本实现不做多周期拆分）
     */
    public TaxiPhaseStreamMessage playTaxiPhase(Long qarId, Integer startId, Integer endId) {
        if (qarId == null) {
            throw new IllegalArgumentException("qarId is required");
        }

        Integer initialLastId = (startId == null || startId <= 0) ? -1 : startId - 1;
        Integer cursorLastId = initialLastId;

        // session 状态（用于跨 batch 连续计算）
        Double lastCumulativeDistance = 0.0;
        String lastTime = null;
        String lastAirspeedForDistance = null;

        Boolean hasSeenAirInStream = false;
        Boolean hasSeenApproachInStream = false;
        Boolean hasSeenTaxiInStream = false;
        String lastProcessedPhase = null;

        Double runwayMagneticHeadingMedian = null;
        List<Double> takeoffRollMagneticHeadings = new ArrayList<>();

        // endId 仅用于限制返回范围：到达后直接停止
        Integer effectiveEndId = endId;

        List<TaxiPhaseStreamMessage.TaxiPhaseDataItem> allItems = new ArrayList<>();
        long totalCount = 0L;

        while (true) {
            LambdaQueryWrapper<QarDataDO> queryWrapper = new LambdaQueryWrapper<QarDataDO>()
                    .gt(QarDataDO::getId, cursorLastId)
                    .eq(QarDataDO::getQarId, qarId)
                    .eq(QarDataDO::getDeleted, 0)
                    .isNotNull(QarDataDO::getEmptySpaceSwitch)
                    .ne(QarDataDO::getEmptySpaceSwitch, "")
                    .ne(QarDataDO::getEmptySpaceSwitch, "NULL")
                    .orderByAsc(QarDataDO::getId);

            if (effectiveEndId != null) {
                queryWrapper.le(QarDataDO::getId, effectiveEndId);
            }

            queryWrapper.last("LIMIT " + BATCH_SIZE);

            List<QarDataDO> dataList = qarDataMapper.selectList(queryWrapper);
            if (dataList == null || dataList.isEmpty()) {
                break;
            }

            // 与 WebSocket 一致：batch 级别按时间排序后再计算累计距离
            dataList.sort(
                    Comparator.comparing(QarDataDO::getTime,
                            Comparator.nullsLast(Comparator.comparing(
                                    t -> TimeUtils.parseTime(t),
                                    Comparator.nullsLast(Comparator.naturalOrder())
                            )))
            );

            List<TaxiPhaseStreamMessage.TaxiPhaseDataItem> items = new ArrayList<>();
            Double currentCumulativeDistance = lastCumulativeDistance;

            // 当前 batch 的“上一点”时间/速度（与 WebSocket 相同：lastTime/lastAirspeedForDistance 仅在 batch 内部滚动）
            String lastTimeInBatch = lastTime;
            String lastAirspeedForDistanceInBatch = lastAirspeedForDistance;

            for (QarDataDO data : dataList) {
                String emptySpaceSwitch = data.getEmptySpaceSwitch();
                boolean isGroundPhase = emptySpaceSwitch != null && emptySpaceSwitch.toUpperCase().contains("GROUND");
                boolean isAirPhase = emptySpaceSwitch != null && emptySpaceSwitch.toUpperCase().contains("AIR");

                String radioAltStr = data.getRadioAltitude();
                Double radioAltFt = parseRadioAltitudeFt(radioAltStr);

                // 空中阶段：AIR 且 radio_altitude<=1100
                if (isAirPhase) {
                    if (radioAltFt != null && radioAltFt > 1100) {
                        // 跳过平飞阶段数据
                    } else if (radioAltFt != null && radioAltFt <= 1100) {
                        if (takeoffRollMagneticHeadings != null && !takeoffRollMagneticHeadings.isEmpty()) {
                            int count = takeoffRollMagneticHeadings.size();
                            Double median = computeMedian(takeoffRollMagneticHeadings);
                            runwayMagneticHeadingMedian = median;
                            takeoffRollMagneticHeadings.clear();
                            log.debug("[playTaxiPhase][本周期滑跑磁航向中位数] 样本数: {}, 跑道磁航向: {}°", count, median);
                        }

                        String groundKm = data.getGroundSpeedKm();
                        if (groundKm == null || groundKm.trim().isEmpty()) {
                            groundKm = data.getAirspeedKm();
                        }

                        Double timeIntervalAir = TimeUtils.calculateTimeInterval(data.getTime(), lastTimeInBatch);
                        Double cumulativeDistanceAir;
                        if (lastTimeInBatch == null || lastAirspeedForDistanceInBatch == null) {
                            cumulativeDistanceAir = currentCumulativeDistance != null ? currentCumulativeDistance : 0.0;
                        } else {
                            cumulativeDistanceAir = calculateCumulativeDistance(
                                    lastAirspeedForDistanceInBatch,
                                    timeIntervalAir,
                                    currentCumulativeDistance != null ? currentCumulativeDistance : 0.0
                            );
                        }
                        currentCumulativeDistance = cumulativeDistanceAir;
                        lastTimeInBatch = data.getTime();

                        if (groundKm != null && !groundKm.trim().isEmpty()) {
                            lastAirspeedForDistanceInBatch = groundKm;
                        }

                        TaxiPhaseStreamMessage.TaxiPhaseDataItem item = new TaxiPhaseStreamMessage.TaxiPhaseDataItem();
                        item.setId(data.getId());
                        item.setTime(data.getTime());
                        item.setRadioAltitude(radioAltStr);
                        item.setAirspeed(data.getAirspeedKm());
                        item.setGroundSpeed(
                                groundKm != null && !groundKm.isEmpty() ? groundKm : data.getAirspeedKm()
                        );
                        item.setSelectedSpeed(data.getSelectedSpeedKm());
                        item.setPitchAngle(data.getPitchAngle());
                        item.setSlope(data.getSlope());
                        item.setMagneticHeading(data.getMagneticHeading());
                        item.setLocDeviation(data.getLocDeviationRes());
                        item.setGlideslopeDeviationRes(data.getGlideslopeDeviationRes());
                        // 原始 miuA 值（未乘以 75），供跑道入口点几何计算使用
                        item.setGlideslopeDeviationMiuA(data.getGlideslopeDeviation());

                        String gsCapture = data.getGlideSlopeCapture();
                        item.setGlideslopeCapture(gsCapture);

                        boolean isApproach = isApproachPhase(gsCapture);
                        item.setGsDrawEnabled(isApproach);

                        item.setCumulativeDistance(cumulativeDistanceAir);
                        item.setLongitude(data.getLongitude());
                        item.setLatitude(data.getLatitude());
                        item.setRunwayMagneticHeadingMedian(runwayMagneticHeadingMedian);

                        item.setFlightPhase(isApproach ? TaxiPhaseStreamMessage.PHASE_APPROACH : TaxiPhaseStreamMessage.PHASE_CLIMB);
                        items.add(item);

                        if (isApproach) {
                            if (!Boolean.TRUE.equals(hasSeenApproachInStream)) {
                                log.debug("[playTaxiPhase][approach=true] AIR id={}, time={}, radioAlt(ft)={}, gsCapture={}",
                                        data.getId(), data.getTime(), radioAltStr, gsCapture);
                            }
                            hasSeenApproachInStream = true;
                        }

                        hasSeenAirInStream = true;
                        lastProcessedPhase = "AIR";
                    }
                }

                // 地面阶段：GROUND 且不与“有效 AIR(<=1100)”冲突
                if (isGroundPhase && !(isAirPhase && radioAltFt != null && radioAltFt <= 1100.0)) {
                    boolean seenAir = Boolean.TRUE.equals(hasSeenAirInStream);
                    boolean seenApproachInStream = Boolean.TRUE.equals(hasSeenApproachInStream);
                    boolean isFirstGroundAfterAir = "AIR".equals(lastProcessedPhase);

                    // 滑行后再次出现「GROUND 紧接在 AIR 后」= 新起飞滑跑
                    if (isFirstGroundAfterAir && Boolean.TRUE.equals(hasSeenTaxiInStream) && !seenApproachInStream) {
                        hasSeenAirInStream = false;
                        hasSeenApproachInStream = false;

                        lastProcessedPhase = null;
                        lastCumulativeDistance = 0.0;
                        currentCumulativeDistance = 0.0;
                        lastTimeInBatch = null;
                        lastAirspeedForDistanceInBatch = null;

                        takeoffRollMagneticHeadings.clear();
                        runwayMagneticHeadingMedian = null;
                        // 本实现不使用 lastGlideslopeCapture，但保留语义一致

                        seenAir = false;
                    }

                    String airspeedKm = data.getAirspeedKm();
                    String groundSpeedKm = data.getGroundSpeedKm();

                    if (seenAir) {
                        hasSeenTaxiInStream = true;
                    }

                    String taxiCapture = data.getGlideSlopeCapture();

                    Double timeInterval = TimeUtils.calculateTimeInterval(data.getTime(), lastTimeInBatch);
                    Double lastCumulative = lastCumulativeDistance;

                    // 滑行与滑跑在同批、中间无 AIR：满足任一条件即视为新起飞滑跑
                    final double TAXI_TO_TAKEOFF_ROLL_GAP_SECONDS = 30.0;
                    final double TAXI_NEAR_RUNWAY_END_METERS = 2500.0;

                    boolean isGroundAfterGround =
                            "GROUND".equals(lastProcessedPhase) && Boolean.TRUE.equals(hasSeenTaxiInStream);
                    boolean timeGapLarge = timeInterval != null && timeInterval > TAXI_TO_TAKEOFF_ROLL_GAP_SECONDS;
                    boolean previousNearRunwayEnd =
                            lastCumulative != null && lastCumulative >= TAXI_NEAR_RUNWAY_END_METERS;

                    if (isGroundAfterGround && (timeGapLarge || previousNearRunwayEnd) && !seenApproachInStream) {
                        hasSeenAirInStream = false;
                        hasSeenApproachInStream = false;

                        lastProcessedPhase = null;
                        lastCumulativeDistance = 0.0;
                        currentCumulativeDistance = 0.0;
                        lastTimeInBatch = null;
                        lastAirspeedForDistanceInBatch = null;

                        takeoffRollMagneticHeadings.clear();
                        runwayMagneticHeadingMedian = null;
                        seenAir = false;
                    }

                    boolean isFirstTaxiPoint = seenAir && isFirstGroundAfterAir;

                    Double cumulativeDistance;
                    if (isFirstTaxiPoint) {
                        cumulativeDistance = 0.0;
                    } else if (lastTimeInBatch == null || lastAirspeedForDistanceInBatch == null) {
                        cumulativeDistance = 0.0;
                    } else {
                        cumulativeDistance = calculateCumulativeDistance(
                                lastAirspeedForDistanceInBatch,
                                timeInterval,
                                currentCumulativeDistance != null ? currentCumulativeDistance : 0.0
                        );
                    }

                    // 跑道上限保护
                    if (cumulativeDistance != null && cumulativeDistance > MAX_TAXI_DISTANCE_METERS) {
                        cumulativeDistance = MAX_TAXI_DISTANCE_METERS;
                    }

                    TaxiPhaseStreamMessage.TaxiPhaseDataItem item = new TaxiPhaseStreamMessage.TaxiPhaseDataItem();
                    item.setId(data.getId());
                    item.setTime(data.getTime());
                    item.setLocDeviation(data.getLocDeviationRes());
                    item.setGroundSpeed(groundSpeedKm != null ? groundSpeedKm : airspeedKm);
                    item.setAirspeed(airspeedKm);
                    item.setSelectedSpeed(data.getSelectedSpeedKm());
                    item.setPitchAngle(data.getPitchAngle());
                    item.setSlope(data.getSlope());
                    item.setMagneticHeading(data.getMagneticHeading());
                    item.setRadioAltitude(data.getRadioAltitude());
                    item.setGlideslopeDeviationRes(data.getGlideslopeDeviationRes());
                    item.setGlideslopeCapture(taxiCapture);
                    item.setGsDrawEnabled(false);
                    item.setTimeInterval(timeInterval);
                    item.setCumulativeDistance(cumulativeDistance);
                    item.setLongitude(data.getLongitude());
                    item.setLatitude(data.getLatitude());

                    Double groundSpeedKnotsForPhase = parseGroundSpeedKnots(data.getGroundSpeed());
                    boolean isZeroGroundSpeed = groundSpeedKnotsForPhase != null && groundSpeedKnotsForPhase == 0;
                    boolean seenApproach = Boolean.TRUE.equals(hasSeenApproachInStream);

                    // 滑跑=GROUND 且未在进近后/或地速=0；滑行=TAXI=已见 AIR 后且地速!=0
                    item.setFlightPhase(seenApproach && !isZeroGroundSpeed ? TaxiPhaseStreamMessage.PHASE_TAXI : TaxiPhaseStreamMessage.PHASE_TAKEOFF_ROLL);

                    // 有效滑跑阶段采样：ground_speed>40 且 pitchAngle in [-1,+1]
                    if (!seenAir || isZeroGroundSpeed) {
                        Double groundSpeedKnots = groundSpeedKnotsForPhase != null
                                ? groundSpeedKnotsForPhase
                                : parseGroundSpeedKnots(data.getGroundSpeed());
                        boolean groundSpeedOk = groundSpeedKnots != null && groundSpeedKnots > 40;
                        Double pitchDeg = parsePitchAngle(data.getPitchAngle());
                        boolean pitchOk = pitchDeg != null && pitchDeg >= -1.0 && pitchDeg <= 1.0;
                        if (groundSpeedOk && pitchOk) {
                            Double hdg = parseMagneticHeading(data.getMagneticHeading());
                            if (hdg != null) {
                                if (takeoffRollMagneticHeadings == null) {
                                    takeoffRollMagneticHeadings = new ArrayList<>();
                                }
                                takeoffRollMagneticHeadings.add(hdg);
                            }
                        }
                    }

                    // 更新下一点距离计算的“上一点速度/时间”
                    currentCumulativeDistance = cumulativeDistance;
                    lastTimeInBatch = data.getTime();

                    Double groundSpeedKnotsForUpdate = parseGroundSpeedKnots(data.getGroundSpeed());
                    if (groundSpeedKm != null && !groundSpeedKm.trim().isEmpty()) {
                        lastAirspeedForDistanceInBatch = groundSpeedKm;
                    } else if (groundSpeedKnotsForUpdate != null && groundSpeedKnotsForUpdate == 0) {
                        lastAirspeedForDistanceInBatch = "0";
                    }

                    lastProcessedPhase = "GROUND";

                    items.add(item);
                }
            }

            QarDataDO lastRecord = dataList.stream()
                    .max(Comparator.comparing(QarDataDO::getId))
                    .orElse(dataList.get(dataList.size() - 1));

            // 更新“跨 batch”状态
            cursorLastId = lastRecord.getId();
            totalCount += items.size();
            if (!items.isEmpty()) {
                lastCumulativeDistance = currentCumulativeDistance;
                lastTime = lastTimeInBatch;
                lastAirspeedForDistance = lastAirspeedForDistanceInBatch;
            }

            allItems.addAll(items);

            if (effectiveEndId != null && lastRecord.getId() >= effectiveEndId) {
                break;
            }
        }

        TaxiPhaseStreamMessage message = new TaxiPhaseStreamMessage();
        message.setType("data");
        message.setData(allItems);
        message.setSpeed(1);
        message.setTotalCount(totalCount);
        // 进近落地点几何计算：跑道入口点 i、实际接地点 t、it 距离
        ApproachLandingResult landingResult = computeApproachLandingPoints(allItems);
        message.setThresholdLat(landingResult.thresholdLat);
        message.setThresholdLon(landingResult.thresholdLon);
        message.setThresholdHorizDistFt(landingResult.thresholdHorizDistFt);
        message.setTouchdownLat(landingResult.touchdownLat);
        message.setTouchdownLon(landingResult.touchdownLon);
        message.setItDistanceMeters(landingResult.itDistanceMeters);

        // 3D 复现使用的接地点 X（米）：按“距跑道尾”的语义对齐展示与降落
        // 跑道尾坐标为 RUNWAY_LENGTH_M，则「距跑道尾 d」对应 X = RUNWAY_LENGTH_M - d
        Double touchdownX = null;
        if (landingResult.itDistanceMeters != null && Double.isFinite(landingResult.itDistanceMeters)) {
            touchdownX = Math.min(RUNWAY_LENGTH_M, Math.max(0.0, RUNWAY_LENGTH_M - landingResult.itDistanceMeters));
            log.info("[touchdownXOnRunwayM][by itDistanceMeters] itDist={}m => x={}m (0=head, {}=tail)",
                    landingResult.itDistanceMeters, touchdownX, RUNWAY_LENGTH_M);
        } else {
            // 兜底：仍保留原 Processor 算法（用于无经纬/无入口点时）
            touchdownX = computeTouchdownXOnRunwayM(allItems);
            log.info("[touchdownXOnRunwayM][fallback] x={}m (0=head, {}=tail)", touchdownX, RUNWAY_LENGTH_M);
        }
        message.setTouchdownXOnRunwayM(touchdownX);

        return message;
    }

    /**
     * 计算进近接地点在跑道纵轴上的 X（米，0=跑道头）。
     *
     * <p>等价迁移前端原 `computeApproachLandingXFromProcessor`：
     * 接地帧（APPROACH->TAXI 或首条 TAXI）→ 接地前 200s 窗口 → O 点（|GS偏差|最小） →
     * 入口点（距 O 最接近 1000ft）→ X = (GS_x - 1000ft) - distance(entry, touchdown) 并 clamp。</p>
     *
     * <p>无有效链路时返回 null。</p>
     */
    private static Double computeTouchdownXOnRunwayM(List<TaxiPhaseStreamMessage.TaxiPhaseDataItem> data) {
        if (data == null || data.size() < 2) {
            return null;
        }

        int touchdownIndex = -1;
        for (int i = 1; i < data.size(); i++) {
            String prev = data.get(i - 1).getFlightPhase();
            String cur = data.get(i).getFlightPhase();
            if (TaxiPhaseStreamMessage.PHASE_APPROACH.equals(prev) && TaxiPhaseStreamMessage.PHASE_TAXI.equals(cur)) {
                touchdownIndex = i;
                break;
            }
        }
        if (touchdownIndex < 0) {
            for (int i = 0; i < data.size(); i++) {
                if (TaxiPhaseStreamMessage.PHASE_TAXI.equals(data.get(i).getFlightPhase())) {
                    touchdownIndex = i;
                    break;
                }
            }
        }
        if (touchdownIndex < 1) {
            return null;
        }

        LonLat tdPos = parseLonLat(data.get(touchdownIndex));
        if (tdPos == null) {
            return null;
        }

        List<Integer> window = collectIndicesInSecondsBeforeTouchdown(data, touchdownIndex);
        if (window.isEmpty()) {
            return null;
        }

        LonLat pointO = null;
        double minGsAbs = Double.POSITIVE_INFINITY;
        for (Integer idx : window) {
            TaxiPhaseStreamMessage.TaxiPhaseDataItem row = data.get(idx);
            if (!TaxiPhaseStreamMessage.PHASE_APPROACH.equals(row.getFlightPhase())) {
                continue;
            }
            Double gsDev = parseDouble(row.getGlideslopeDeviationRes());
            if (gsDev == null || !Double.isFinite(gsDev)) {
                continue;
            }
            double abs = Math.abs(gsDev);
            if (abs >= minGsAbs) {
                continue;
            }
            LonLat p = parseLonLat(row);
            if (p == null) {
                continue;
            }
            minGsAbs = abs;
            pointO = p;
        }
        if (pointO == null) {
            return null;
        }

        LonLat entryPoint = null;
        double minDiff = Double.POSITIVE_INFINITY;
        for (Integer idx : window) {
            LonLat p = parseLonLat(data.get(idx));
            if (p == null) {
                continue;
            }
            double distToO = haversineMeters(p.lat, p.lon, pointO.lat, pointO.lon);
            double diff = Math.abs(distToO - FT1000_TO_METERS);
            if (diff < minDiff) {
                minDiff = diff;
                entryPoint = p;
            }
        }

        double distOTd = haversineMeters(pointO.lat, pointO.lon, tdPos.lat, tdPos.lon);
        double entryToTdFromO = Math.max(0.0, distOTd - FT1000_TO_METERS);

        Double entryToTdM = null;
        if (entryPoint != null) {
            double d = haversineMeters(entryPoint.lat, entryPoint.lon, tdPos.lat, tdPos.lon);
            if (Double.isFinite(d)) {
                entryToTdM = d;
            }
        }
        if (entryToTdM == null || !Double.isFinite(entryToTdM)) {
            entryToTdM = entryToTdFromO;
        } else if (entryToTdM < 200.0 && entryToTdFromO > 250.0) {
            entryToTdM = entryToTdFromO;
        }

        if (!Double.isFinite(entryToTdM) || entryToTdM <= 1e-6) {
            return null;
        }

        double entryXFromHead = GS_STATION_X_FROM_RUNWAY_HEAD_M - FT1000_TO_METERS;
        double xFromGs = entryXFromHead - entryToTdM;
        return Math.min(RUNWAY_LENGTH_M, Math.max(0.0, xFromGs));
    }

    // ========================================================================================
    // ===  进近落地点几何计算（独立方法）                                                 ===
    // ========================================================================================

    /**
     * 跑道入口点几何计算结果（三个独立方法的汇总出口）。
     */
    public static final class ApproachLandingResult {
        /** 跑道入口点 i 的纬度（点 m 正下方地面点，oi = 1000ft 处）。null 表示无有效数据。 */
        public final Double thresholdLat;
        /** 跑道入口点 i 的经度。 */
        public final Double thresholdLon;
        /** 计算点 m 时的实际水平距离（英尺），理论值 ≈ 1000ft。 */
        public final Double thresholdHorizDistFt;
        /** 实际接地点 t 的纬度（APPROACH→TAXI 第一条记录）。 */
        public final Double touchdownLat;
        /** 实际接地点 t 的经度。 */
        public final Double touchdownLon;
        /** 跑道入口点 i 到实际接地点 t 的水平距离（米）。 */
        public final Double itDistanceMeters;

        private ApproachLandingResult(Double thresholdLat, Double thresholdLon, Double thresholdHorizDistFt,
                                      Double touchdownLat, Double touchdownLon, Double itDistanceMeters) {
            this.thresholdLat = thresholdLat;
            this.thresholdLon = thresholdLon;
            this.thresholdHorizDistFt = thresholdHorizDistFt;
            this.touchdownLat = touchdownLat;
            this.touchdownLon = touchdownLon;
            this.itDistanceMeters = itDistanceMeters;
        }
    }

    /**
     * 进近落地点综合计算入口：依次调用三个独立方法，返回汇总结果。
     *
     * @param allItems 全量处理后的数据项（已含 flightPhase / glideslopeDeviationMiuA / radioAltitude / 经纬度）
     * @return 计算结果；若数据不足则各字段为 null
     */
    static ApproachLandingResult computeApproachLandingPoints(
            List<TaxiPhaseStreamMessage.TaxiPhaseDataItem> allItems) {

        // 方法一：计算跑道入口点 i
        double[] thresholdPoint = computeRunwayThresholdPoint(allItems);
        Double thresholdLat  = thresholdPoint != null ? thresholdPoint[0] : null;
        Double thresholdLon  = thresholdPoint != null ? thresholdPoint[1] : null;
        Double horizDistFt   = thresholdPoint != null ? thresholdPoint[2] : null;

        // 方法二：计算实际接地点 t（空地电门 AIR→GROUND 第一条）
        double[] touchdownPoint = computeTouchdownPoint(allItems);
        Double touchdownLat = touchdownPoint != null ? touchdownPoint[0] : null;
        Double touchdownLon = touchdownPoint != null ? touchdownPoint[1] : null;

        // 方法三：计算 i-t 水平距离（米）
        Double itDist = null;
        if (thresholdLat != null && touchdownLat != null) {
            itDist = computeItDistanceMeters(thresholdLat, thresholdLon, touchdownLat, touchdownLon);
        }

        log.info("[computeApproachLandingPoints] thresholdI=({}, {}), horizDistFt={}ft, " +
                        "touchdownT=({}, {}), itDist={}m",
                thresholdLat, thresholdLon, horizDistFt,
                touchdownLat, touchdownLon, itDist);

        return new ApproachLandingResult(thresholdLat, thresholdLon, horizDistFt,
                touchdownLat, touchdownLon, itDist);
    }

    // ---- 方法一：计算跑道入口点 i -------------------------------------------------------

    /**
     * 方法一：计算跑道入口点 i 的经纬度。
     *
     * <p>几何依据（参考图示）：</p>
     * <pre>
     *   m ← 飞机所在点（进近中，mi ⊥ oi）
     *   |
     *   | mi = radio_altitude（无线电高度，单位：ft）
     *   |
     *   i ← 跑道入口（距 GS 台 o 水平距离 1000ft）
     *        ←—— oi = 1000ft ——→ o（GS 电台）
     * </pre>
     *
     * <p>计算步骤：</p>
     * <ol>
     *   <li>偏差角（°）= glideslopeDeviation_miuA × 0.0048</li>
     *   <li>实际下滑角（°）= 3.0 + 偏差角</li>
     *   <li>到 GS 台水平距离（ft）= radioAltitude_ft / tan(实际角 in radians)</li>
     *   <li>遍历所有 APPROACH 阶段数据行，找到水平距离最接近 1000ft 的行</li>
     *   <li>该行的经纬度即为点 i（m 在地面上的投影）</li>
     * </ol>
     *
     * @param allItems 全量数据项
     * @return double[3]：{lat_i, lon_i, horiz_dist_ft}；无有效数据时返回 null
     */
    static double[] computeRunwayThresholdPoint(
            List<TaxiPhaseStreamMessage.TaxiPhaseDataItem> allItems) {

        if (allItems == null || allItems.isEmpty()) {
            return null;
        }

        /** 跑道入口距 GS 台的标准水平距离（英尺），mi ⊥ oi 时 oi = 1000ft */
        final double OI_DIST_FT = 1000.0;
        /** 与 FlightDataProcessor(6).java 一致：容差（英尺） */
        final double OI_TOLERANCE_FT = 10.0;
        /** 标准下滑角（度） */
        final double BASE_GLIDE_ANGLE_DEG = 3.0;
        /** 下滑道偏差 miuA → 角度的转换系数：1 miuA = 0.0048° */
        final double MIUA_TO_DEG = 0.0048;

        for (TaxiPhaseStreamMessage.TaxiPhaseDataItem item : allItems) {
            if (!TaxiPhaseStreamMessage.PHASE_APPROACH.equals(item.getFlightPhase())) {
                continue;
            }

            // 无线电高度（ft）
            Double radioAltFt = parseDouble(item.getRadioAltitude());
            if (radioAltFt == null || radioAltFt <= 0) {
                continue;
            }

            // FlightDataProcessor(6).java 使用的下滑道偏差：miuA（不是 *Res）。
            // Res 字段为 miuA × 75；若原始 miuA 缺失则用 Res 反推兜底。
            Double miuA = parseDouble(item.getGlideslopeDeviationMiuA());
            Double resValRaw = null;
            if (miuA == null) {
                resValRaw = parseDouble(item.getGlideslopeDeviationRes());
                if (resValRaw == null) {
                    continue;
                }
                miuA = resValRaw / 75.0;
            } else {
                resValRaw = parseDouble(item.getGlideslopeDeviationRes());
            }

            // 实际下滑角（度）
            double actualAngleDeg = BASE_GLIDE_ANGLE_DEG + miuA * MIUA_TO_DEG;
            if (actualAngleDeg <= 0 || actualAngleDeg >= 90) {
                continue; // 角度异常，跳过
            }

            // 到 GS 台的水平距离（ft）= 无线电高度 / tan(实际角)
            // 注意：tan 入参必须为弧度（与 FlightDataProcessor(6).java 逻辑一致）
            double actualAngleRad = Math.toRadians(actualAngleDeg);
            double horizDistFt = computeHorizDistToGsFt(radioAltFt, actualAngleRad);
            if (!Double.isFinite(horizDistFt) || horizDistFt < 0) {
                continue;
            }

            // 与 FlightDataProcessor(6).java 一致：找到第一条 |distToO-1000|<=10 的行就作为入口点 i
            if (Math.abs(horizDistFt - OI_DIST_FT) <= OI_TOLERANCE_FT) {
                LonLat pos = parseLonLat(item);
                if (pos == null) {
                    continue; // 经纬度无效，跳过
                }
                log.info("[computeRunwayThresholdPoint][HIT] id={}, time={}, radioAltFt={}, miuA={}, res(miuA*75)={}, " +
                                "actualAngleDeg={}, horizDistFt={}, lon={}, lat={}",
                        item.getId(), item.getTime(), radioAltFt, miuA, resValRaw,
                        actualAngleDeg, horizDistFt,
                        item.getLongitude(), item.getLatitude());
                return new double[]{pos.lat, pos.lon, horizDistFt};
            }
        }

        log.warn("[computeRunwayThresholdPoint] 未找到满足 |distToO-1000|<=10ft 的入口点 i");
        return null;
    }

    /**
     * 计算飞机到 GS 电台的水平距离（英尺）。
     *
     * @param radioAltFt      无线电高度（英尺）
     * @param actualAngleRad  实际下滑角（弧度）。注意：必须为弧度。
     */
    private static double computeHorizDistToGsFt(double radioAltFt, double actualAngleRad) {
        // distToO = radioAlt / tan(actualAngle)
        return radioAltFt / Math.tan(actualAngleRad);
    }

    // ---- 方法二：计算实际接地点 t -------------------------------------------------------

    /**
     * 方法二：计算实际接地点 t 的经纬度。
     *
     * <p>定义：空地电门从 AIR（APPROACH 阶段）切换到 GROUND（TAXI 阶段）的第一条数据记录，
     * 即飞机触地瞬间的经纬度。</p>
     *
     * @param allItems 全量数据项
     * @return double[2]：{lat_t, lon_t}；无有效数据时返回 null
     */
    static double[] computeTouchdownPoint(
            List<TaxiPhaseStreamMessage.TaxiPhaseDataItem> allItems) {

        if (allItems == null || allItems.size() < 2) {
            return null;
        }

        // 与 FlightDataProcessor(6).java 一致：直接取第一条 GROUND（这里对应 TAXI 阶段）的记录作为接地点 t
        for (TaxiPhaseStreamMessage.TaxiPhaseDataItem item : allItems) {
            if (TaxiPhaseStreamMessage.PHASE_TAXI.equals(item.getFlightPhase())) {
                LonLat pos = parseLonLat(item);
                if (pos != null) {
                    log.info("[computeTouchdownPoint][HIT] id={}, time={}, flightPhase={}, lon={}, lat={}",
                            item.getId(), item.getTime(), item.getFlightPhase(),
                            item.getLongitude(), item.getLatitude());
                    return new double[]{pos.lat, pos.lon};
                }
            }
        }

        log.warn("[computeTouchdownPoint] 未找到有效接地点（无 TAXI 数据）");
        return null;
    }

    // ---- 方法三：计算 i-t 水平距离（米）-----------------------------------------------

    /**
     * 方法三：计算跑道入口点 i 到实际接地点 t 之间的水平距离（米）。
     *
     * <p>使用 Haversine 球面公式，适用于地面短距离（跑道量级，误差 < 0.1%）。</p>
     *
     * @param thresholdLat 跑道入口点 i 的纬度
     * @param thresholdLon 跑道入口点 i 的经度
     * @param touchdownLat 实际接地点 t 的纬度
     * @param touchdownLon 实际接地点 t 的经度
     * @return i-t 水平距离（米）
     */
    static double computeItDistanceMeters(double thresholdLat, double thresholdLon,
                                          double touchdownLat, double touchdownLon) {
        double distMeters = haversineMeters(thresholdLat, thresholdLon, touchdownLat, touchdownLon);
        log.debug("[computeItDistanceMeters] i=({}, {}), t=({}, {}), 距离={}m",
                thresholdLat, thresholdLon, touchdownLat, touchdownLon, distMeters);
        return distMeters;
    }

    // ---- 原有辅助方法（不变） -----------------------------------------------------------

    private static List<Integer> collectIndicesInSecondsBeforeTouchdown(
            List<TaxiPhaseStreamMessage.TaxiPhaseDataItem> data,
            int touchdownIndex) {
        List<Integer> out = new ArrayList<>();
        if (touchdownIndex <= 0) {
            return out;
        }
        Long tdTs = parseEpochSeconds(data.get(touchdownIndex).getTime());
        if (tdTs != null) {
            for (int j = 0; j < touchdownIndex; j++) {
                Long tj = parseEpochSeconds(data.get(j).getTime());
                if (tj == null) {
                    continue;
                }
                long dt = tdTs - tj;
                if (dt >= 0 && dt <= GS_SEARCH_WINDOW_SECONDS) {
                    out.add(j);
                }
            }
        }
        if (out.isEmpty()) {
            int start = Math.max(0, touchdownIndex - GS_SEARCH_FALLBACK_MAX_SAMPLES);
            for (int j = start; j < touchdownIndex; j++) {
                out.add(j);
            }
        }
        return out;
    }

    private static Long parseEpochSeconds(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return null;
        }
        try {
            java.util.Date d = TimeUtils.parseTime(timeStr);
            return d != null ? (d.getTime() / 1000L) : null;
        } catch (Exception ignore) {
            return null;
        }
    }

    private static Double parseDouble(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        if (t.isEmpty() || "NULL".equalsIgnoreCase(t)) {
            return null;
        }
        try {
            double v = Double.parseDouble(t);
            return Double.isFinite(v) ? v : null;
        } catch (Exception ignore) {
            return null;
        }
    }

    private static LonLat parseLonLat(TaxiPhaseStreamMessage.TaxiPhaseDataItem item) {
        if (item == null) {
            return null;
        }
        Double lon = parseDouble(item.getLongitude());
        Double lat = parseDouble(item.getLatitude());
        if (lon == null || lat == null) {
            return null;
        }
        return new LonLat(lat, lon);
    }

    private static double haversineMeters(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371000.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));
    }

    private static final class LonLat {
        private final double lat;
        private final double lon;

        private LonLat(double lat, double lon) {
            this.lat = lat;
            this.lon = lon;
        }
    }

    private static Double calculateCumulativeDistance(String lastAirspeedKmh, Double timeInterval, Double lastCumulativeDistance) {
        if (timeInterval == null || timeInterval <= 0) {
            return lastCumulativeDistance != null ? lastCumulativeDistance : 0.0;
        }

        Double speedKmh = SpeedUtils.parseSpeed(lastAirspeedKmh);
        if (speedKmh == null || speedKmh <= 0) {
            log.warn("[calculateCumulativeDistance][空速解析失败或为0] lastAirspeedKmh: {}", lastAirspeedKmh);
            return lastCumulativeDistance != null ? lastCumulativeDistance : 0.0;
        }

        Double speedMetersPerSecond = SpeedUtils.convertToMetersPerSecond(speedKmh);
        if (speedMetersPerSecond == null) {
            log.warn("[calculateCumulativeDistance][空速转换失败] speed: {} km/h", speedKmh);
            return lastCumulativeDistance != null ? lastCumulativeDistance : 0.0;
        }

        double deltaDistance = speedMetersPerSecond * timeInterval;
        return (lastCumulativeDistance != null ? lastCumulativeDistance : 0.0) + deltaDistance;
    }

    private static Double parseRadioAltitudeFt(String radioAltitudeStr) {
        if (radioAltitudeStr == null || radioAltitudeStr.trim().isEmpty() || "NULL".equalsIgnoreCase(radioAltitudeStr.trim())) {
            return null;
        }
        try {
            return Double.parseDouble(radioAltitudeStr.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Double parseGroundSpeedKnots(String groundSpeedStr) {
        if (groundSpeedStr == null || groundSpeedStr.trim().isEmpty() || "NULL".equalsIgnoreCase(groundSpeedStr.trim())) {
            return null;
        }
        try {
            return Double.parseDouble(groundSpeedStr.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Double parsePitchAngle(String pitchAngleStr) {
        if (pitchAngleStr == null || pitchAngleStr.trim().isEmpty() || "NULL".equalsIgnoreCase(pitchAngleStr.trim())) {
            return null;
        }
        try {
            return Double.parseDouble(pitchAngleStr.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Double parseMagneticHeading(String magneticHeadingStr) {
        if (magneticHeadingStr == null || magneticHeadingStr.trim().isEmpty() || "NULL".equalsIgnoreCase(magneticHeadingStr.trim())) {
            return null;
        }
        try {
            return Double.parseDouble(magneticHeadingStr.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Double computeMedian(List<Double> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        List<Double> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        int n = sorted.size();
        if (n % 2 == 1) {
            return sorted.get(n / 2);
        }
        return (sorted.get(n / 2 - 1) + sorted.get(n / 2)) / 2.0;
    }

    private static boolean isApproachPhase(String gsCapture) {
        return gsCapture != null && gsCapture.toUpperCase().contains("GS ENGAGE");
    }
}

