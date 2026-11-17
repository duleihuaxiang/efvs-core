package cn.iocoder.yudao.module.qar.websocket.service;

import cn.iocoder.yudao.module.qar.dal.dataobject.qardata.QarDataDO;
import cn.iocoder.yudao.module.qar.dal.mysql.qardata.QarDataMapper;
import cn.iocoder.yudao.module.qar.websocket.message.TaxiPhaseStreamMessage;
import cn.iocoder.yudao.module.qar.websocket.session.TaxiPhaseStreamSession;
import cn.iocoder.yudao.module.qar.websocket.util.SpeedUtils;
import cn.iocoder.yudao.module.qar.websocket.util.TimeUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 多架次对比数据流服务（性能分析）
 *
 * <p>四种状态（滑跑/爬升/进近/滑行）判定、累计距离公式、滑行后新起飞滑跑判定、进近阶段 isApproachPhase 等与飞行复现一致。</p>
 * <p>每条数据项设置 qarId，供前端性能分析页多架次分色绘制。</p>
 */
@Slf4j
@Service
public class TaxiCompareStreamService {

    /** 滑跑阶段机场跑道最大长度（米），累计距离不得超过此值 */
    private static final double MAX_TAXI_DISTANCE_METERS = 3600.0;

    /** 滑行阶段地速上限（节），ground_speed 小于此值且本流已出现 AIR 时判为滑行，可取 60/70/80 */
    private static final double MAX_GROUND_SPEED_KNOTS_FOR_TAXI = 80.0;

    @Autowired
    private QarDataMapper qarDataMapper;

    /**
     * HTTP 分页与 WebSocket 共用：按 id 拉取一页原始行，按时间与阶段规则生成 taxi-phase 数据项，并更新 session。
     *
     * @param logRef 仅用于日志（WebSocket 用 sessionId，REST 用 rest-{qarId}）
     */
    public CompareDataBatchOutcome runCompareDataBatchCore(TaxiPhaseStreamSession session, int batchSize, String logRef) {
        if (session == null || session.getQarId() == null) {
            return CompareDataBatchOutcome.noMoreRows();
        }
        Long qarId = session.getQarId();
        LambdaQueryWrapper<QarDataDO> queryWrapper = new LambdaQueryWrapper<QarDataDO>()
                .gt(QarDataDO::getId, session.getLastId())
                .eq(QarDataDO::getQarId, qarId)
                .eq(QarDataDO::getDeleted, 0)
                .isNotNull(QarDataDO::getEmptySpaceSwitch)
                .ne(QarDataDO::getEmptySpaceSwitch, "")
                .ne(QarDataDO::getEmptySpaceSwitch, "NULL")
                .orderByAsc(QarDataDO::getId);
        Integer endId = session.getEndId();
        if (endId != null) {
            queryWrapper.le(QarDataDO::getId, endId);
        }

        queryWrapper.last("LIMIT " + batchSize);

        long t0 = System.nanoTime();
        long sqlStart = System.nanoTime();
        List<QarDataDO> dataList = qarDataMapper.selectList(queryWrapper);
        long sqlNanos = System.nanoTime() - sqlStart;

        if (dataList.isEmpty()) {
            long tEnd = System.nanoTime();
            return new CompareDataBatchOutcome(Collections.emptyList(), true, false, null, sqlNanos, tEnd - t0 - sqlNanos);
        }

        Integer cycleCompletedIndex = null;

        endId = session.getEndId();
        if (endId != null) {
            List<QarDataDO> filteredList = new ArrayList<>();
            boolean reachedEnd = false;
            for (QarDataDO data : dataList) {
                if (data.getId() <= endId) {
                    filteredList.add(data);
                } else {
                    reachedEnd = true;
                    break;
                }
            }
            dataList = filteredList;

            if (reachedEnd) {
                Integer currentCycle = session.getCurrentCycleIndex();
                log.info("[runCompareDataBatchCore][ref({}) 达到周期结束ID: {}，第{}周期完成，清除限制继续传输后续数据]", logRef, endId, currentCycle);
                session.setEndId(null);
                cycleCompletedIndex = currentCycle;
                session.setCurrentCycleIndex(currentCycle + 1);
                session.setLastCumulativeDistance(0.0);
                session.setLastTime(null);
                session.setLastGroundSpeed(null);
                session.setHasSeenAirInStream(false);
                if (Boolean.TRUE.equals(session.getHasSeenApproachInStream())) {
                    log.info("[TAXI_APPROACH_FLAG][ref({}) qarId({}) approach=false] reset by cycle endId reached at batch-filter stage, endId={}",
                            logRef, session.getQarId(), endId);
                }
                session.setHasSeenApproachInStream(false);
                session.setLastProcessedPhase(null);
                if (session.getTakeoffRollMagneticHeadings() != null) {
                    session.getTakeoffRollMagneticHeadings().clear();
                }
                session.setRunwayMagneticHeadingMedian(null);
                session.setLastGlideslopeCapture(null);
            } else if (dataList.isEmpty()) {
                long tEnd = System.nanoTime();
                return new CompareDataBatchOutcome(Collections.emptyList(), false, true, null, sqlNanos, tEnd - t0 - sqlNanos);
            }
        }

        dataList.sort(Comparator.comparing(QarDataDO::getTime, Comparator.nullsLast(
                Comparator.comparing(t -> TimeUtils.parseTime(t), Comparator.nullsLast(Comparator.naturalOrder())))));

        List<TaxiPhaseStreamMessage.TaxiPhaseDataItem> items = new ArrayList<>();
        Double currentCumulativeDistance = session.getLastCumulativeDistance();
        String lastTime = session.getLastTime();
        String lastAirspeedForDistance = session.getLastGroundSpeed();

        for (QarDataDO data : dataList) {
            String emptySpaceSwitch = data.getEmptySpaceSwitch();
            boolean isGroundPhase = emptySpaceSwitch != null && emptySpaceSwitch.toUpperCase().contains("GROUND");
            boolean isAirPhase = emptySpaceSwitch != null && emptySpaceSwitch.toUpperCase().contains("AIR");
            String radioAltStr = data.getRadioAltitude();
            Double radioAltFt = parseRadioAltitudeFt(radioAltStr);

            if (isAirPhase) {
                if (radioAltFt != null && radioAltFt > 1100) {
                    log.debug("[runCompareDataBatchCore][跳过平飞阶段数据] ID: {}, radio_altitude: {} ft > 1100", data.getId(), radioAltFt);
                } else if (radioAltFt != null && radioAltFt <= 1100) {
                    List<Double> headings = session.getTakeoffRollMagneticHeadings();
                    if (headings != null && !headings.isEmpty()) {
                        int count = headings.size();
                        Double median = computeMedian(headings);
                        session.setRunwayMagneticHeadingMedian(median);
                        headings.clear();
                        log.debug("[runCompareDataBatchCore][本周期滑跑磁航向中位数] 样本数: {}, 跑道磁航向: {}°", count, median);
                    }
                    String groundKm = data.getGroundSpeedKm();
                    if (groundKm == null || groundKm.trim().isEmpty()) {
                        groundKm = data.getAirspeedKm();
                    }
                    Double timeIntervalAir = TimeUtils.calculateTimeInterval(data.getTime(), lastTime);
                    Double cumulativeDistanceAir;
                    if (lastTime == null || lastAirspeedForDistance == null) {
                        cumulativeDistanceAir = currentCumulativeDistance != null ? currentCumulativeDistance : 0.0;
                    } else {
                        cumulativeDistanceAir = calculateCumulativeDistance(
                                lastAirspeedForDistance,
                                timeIntervalAir,
                                currentCumulativeDistance != null ? currentCumulativeDistance : 0.0);
                    }
                    currentCumulativeDistance = cumulativeDistanceAir;
                    lastTime = data.getTime();
                    if (groundKm != null && !groundKm.trim().isEmpty()) {
                        lastAirspeedForDistance = groundKm;
                    }

                    TaxiPhaseStreamMessage.TaxiPhaseDataItem item = new TaxiPhaseStreamMessage.TaxiPhaseDataItem();
                    item.setQarId(qarId);
                    item.setId(data.getId());
                    item.setTime(data.getTime());
                    item.setRadioAltitude(radioAltStr);
                    item.setAirspeed(data.getAirspeedKm());
                    item.setGroundSpeed(groundKm != null && !groundKm.isEmpty() ? groundKm : data.getAirspeedKm());
                    item.setSelectedSpeed(data.getSelectedSpeedKm());
                    item.setPitchAngle(data.getPitchAngle());
                    item.setSlope(data.getSlope());
                    item.setMagneticHeading(data.getMagneticHeading());
                    item.setLocDeviation(data.getLocDeviationRes());
                    item.setGlideslopeDeviationRes(data.getGlideslopeDeviationRes());
                    String gsCapture = data.getGlideSlopeCapture();
                    item.setGlideslopeCapture(gsCapture);
                    session.setLastGlideslopeCapture(gsCapture);
                    boolean isApproach = isApproachPhase(data, radioAltFt, gsCapture);
                    item.setGsDrawEnabled(isApproach);
                    item.setCumulativeDistance(cumulativeDistanceAir);
                    item.setLongitude(data.getLongitude());
                    item.setLatitude(data.getLatitude());
                    item.setRunwayMagneticHeadingMedian(session.getRunwayMagneticHeadingMedian());
                    item.setFlightPhase(isApproach
                            ? TaxiPhaseStreamMessage.PHASE_APPROACH
                            : TaxiPhaseStreamMessage.PHASE_CLIMB);
                    items.add(item);
                    if (isApproach) {
                        if (!Boolean.TRUE.equals(session.getHasSeenApproachInStream())) {
                            log.info("[TAXI_APPROACH_FLAG][ref({}) qarId({}) approach=true] AIR id={}, time={}, radioAlt(ft)={}, gsCapture={}",
                                    logRef, session.getQarId(), data.getId(), data.getTime(), radioAltStr, gsCapture);
                        }
                        session.setHasSeenApproachInStream(true);
                    }
                    session.setHasSeenAirInStream(true);
                    session.setLastProcessedPhase("AIR");
                }
            }

            if (isGroundPhase && !(isAirPhase && radioAltFt != null && radioAltFt <= 1100.0)) {
                boolean seenAir = Boolean.TRUE.equals(session.getHasSeenAirInStream());
                boolean seenApproachInStream = Boolean.TRUE.equals(session.getHasSeenApproachInStream());
                boolean isFirstGroundAfterAir = "AIR".equals(session.getLastProcessedPhase());
                if (isFirstGroundAfterAir && Boolean.TRUE.equals(session.getHasSeenTaxiInStream()) && !seenApproachInStream) {
                    session.setHasSeenAirInStream(false);
                    if (Boolean.TRUE.equals(session.getHasSeenApproachInStream())) {
                        log.info("[TAXI_APPROACH_FLAG][ref({}) qarId({}) approach=false] reset by ground-after-air new takeoff roll, GROUND id={}, time={}",
                                logRef, session.getQarId(), data.getId(), data.getTime());
                    }
                    session.setHasSeenApproachInStream(false);
                    session.setLastCumulativeDistance(0.0);
                    session.setLastTime(null);
                    session.setLastGroundSpeed(null);
                    currentCumulativeDistance = 0.0;
                    lastTime = null;
                    lastAirspeedForDistance = null;
                    seenAir = false;
                    if (session.getTakeoffRollMagneticHeadings() != null) {
                        session.getTakeoffRollMagneticHeadings().clear();
                    }
                    session.setRunwayMagneticHeadingMedian(null);
                    session.setLastGlideslopeCapture(null);
                    log.debug("[runCompareDataBatchCore][滑行后再次 GROUND，视为新起飞滑跑] ID: {}", data.getId());
                }
                String airspeedKm = data.getAirspeedKm();
                String groundSpeedKm = data.getGroundSpeedKm();
                if (seenAir) {
                    session.setHasSeenTaxiInStream(true);
                }
                String taxiCapture = data.getGlideSlopeCapture();
                Double timeInterval = TimeUtils.calculateTimeInterval(data.getTime(), lastTime);
                Double lastCumulative = session.getLastCumulativeDistance();
                final double TAXI_TO_TAKEOFF_ROLL_GAP_SECONDS = 30.0;
                final double TAXI_NEAR_RUNWAY_END_METERS = 2500.0;
                boolean isGroundAfterGround =
                        "GROUND".equals(session.getLastProcessedPhase())
                                && Boolean.TRUE.equals(session.getHasSeenTaxiInStream());
                boolean timeGapLarge = timeInterval != null && timeInterval > TAXI_TO_TAKEOFF_ROLL_GAP_SECONDS;
                boolean previousNearRunwayEnd = lastCumulative != null && lastCumulative >= TAXI_NEAR_RUNWAY_END_METERS;
                if (isGroundAfterGround && (timeGapLarge || previousNearRunwayEnd) && !seenApproachInStream) {
                    session.setHasSeenAirInStream(false);
                    if (Boolean.TRUE.equals(session.getHasSeenApproachInStream())) {
                        log.info("[TAXI_APPROACH_FLAG][ref({}) qarId({}) approach=false] reset by ground-after-ground new takeoff roll, GROUND id={}, time={}, timeGap={}s, lastCumulative={}m",
                                logRef, session.getQarId(), data.getId(), data.getTime(), timeInterval, lastCumulative);
                    }
                    session.setHasSeenApproachInStream(false);
                    session.setLastCumulativeDistance(0.0);
                    session.setLastTime(null);
                    session.setLastGroundSpeed(null);
                    currentCumulativeDistance = 0.0;
                    lastTime = null;
                    lastAirspeedForDistance = null;
                    seenAir = false;
                    if (session.getTakeoffRollMagneticHeadings() != null) {
                        session.getTakeoffRollMagneticHeadings().clear();
                    }
                    session.setRunwayMagneticHeadingMedian(null);
                    session.setLastGlideslopeCapture(null);
                    log.debug("[runCompareDataBatchCore][滑行后同批内 GROUND 视为新起飞滑跑] ID: {}, timeGap={}s, lastCumulative={}m", data.getId(), timeInterval, lastCumulative);
                }
                boolean isFirstTaxiPoint = seenAir && isFirstGroundAfterAir;

                Double cumulativeDistance;
                if (isFirstTaxiPoint) {
                    cumulativeDistance = 0.0;
                    log.debug("[runCompareDataBatchCore][滑行首点，累计距离从 0 开始] ID: {}, 时间: {}, 空速(km/h): {}", data.getId(), data.getTime(), airspeedKm);
                } else if (lastTime == null || lastAirspeedForDistance == null) {
                    cumulativeDistance = 0.0;
                    log.debug("[runCompareDataBatchCore][第一个点] ID: {}, 时间: {}, 空速(km/h): {}, 累计距离: 0.0", data.getId(), data.getTime(), airspeedKm);
                } else {
                    cumulativeDistance = calculateCumulativeDistance(lastAirspeedForDistance, timeInterval, currentCumulativeDistance);
                    log.debug("[runCompareDataBatchCore][计算累计距离] ID: {}, 时间: {} -> {}, 前一个空速(km/h): {}, 时间间隔: {}s ({}分钟), 前一个累计: {}m, 当前累计: {}m ({}km)",
                            data.getId(), lastTime, data.getTime(), lastAirspeedForDistance, timeInterval, timeInterval / 60.0, currentCumulativeDistance, cumulativeDistance, cumulativeDistance / 1000.0);
                    if (timeInterval != null && timeInterval > 300) {
                        log.warn("[runCompareDataBatchCore][时间间隔异常] ID: {}, 时间间隔: {}s ({}分钟), 可能导致累计距离异常", data.getId(), timeInterval, timeInterval / 60.0);
                    }
                }

                if (cumulativeDistance != null && cumulativeDistance > MAX_TAXI_DISTANCE_METERS) {
                    log.debug("[runCompareDataBatchCore][累计距离 {}m 超过跑道最大 {}m，按上限计]", cumulativeDistance, MAX_TAXI_DISTANCE_METERS);
                    cumulativeDistance = MAX_TAXI_DISTANCE_METERS;
                }

                TaxiPhaseStreamMessage.TaxiPhaseDataItem item = new TaxiPhaseStreamMessage.TaxiPhaseDataItem();
                item.setQarId(qarId);
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
                boolean seenApproach = Boolean.TRUE.equals(session.getHasSeenApproachInStream());
                item.setFlightPhase(seenApproach
                        ? TaxiPhaseStreamMessage.PHASE_TAXI
                        : TaxiPhaseStreamMessage.PHASE_TAKEOFF_ROLL);

                if (!seenAir || isZeroGroundSpeed) {
                    Double groundSpeedKnots = groundSpeedKnotsForPhase != null ? groundSpeedKnotsForPhase : parseGroundSpeedKnots(data.getGroundSpeed());
                    boolean groundSpeedOk = groundSpeedKnots != null && groundSpeedKnots > 40;
                    Double pitchDeg = parsePitchAngle(data.getPitchAngle());
                    boolean pitchOk = pitchDeg != null && pitchDeg >= -1.0 && pitchDeg <= 1.0;
                    if (groundSpeedOk && pitchOk) {
                        Double hdg = parseMagneticHeading(data.getMagneticHeading());
                        if (hdg != null) {
                            if (session.getTakeoffRollMagneticHeadings() == null) {
                                session.setTakeoffRollMagneticHeadings(new ArrayList<>());
                            }
                            session.getTakeoffRollMagneticHeadings().add(hdg);
                        }
                    }
                }

                currentCumulativeDistance = cumulativeDistance;
                lastTime = data.getTime();
                Double groundSpeedKnotsForUpdate = parseGroundSpeedKnots(data.getGroundSpeed());
                if (groundSpeedKm != null && !groundSpeedKm.trim().isEmpty()) {
                    lastAirspeedForDistance = groundSpeedKm;
                } else if (groundSpeedKnotsForUpdate != null && groundSpeedKnotsForUpdate == 0) {
                    lastAirspeedForDistance = "0";
                }
                session.setLastProcessedPhase("GROUND");
                items.add(item);
            } else if (!isAirPhase) {
                log.debug("[runCompareDataBatchCore][跳过非滑跑/非AIR数据] ID: {}, empty_space_switch: {}", data.getId(), emptySpaceSwitch);
            }
        }

        QarDataDO lastRecord = dataList.stream().max(Comparator.comparing(QarDataDO::getId)).orElse(dataList.get(dataList.size() - 1));
        session.setLastId(lastRecord.getId());
        session.setTotalCount(session.getTotalCount() + items.size());
        if (!items.isEmpty()) {
            session.setLastCumulativeDistance(currentCumulativeDistance);
            session.setLastTime(lastTime);
            session.setLastGroundSpeed(lastAirspeedForDistance);
        }

        endId = session.getEndId();
        if (endId != null && lastRecord.getId() >= endId) {
            Integer currentCycle = session.getCurrentCycleIndex();
            log.info("[runCompareDataBatchCore][ref({}) 处理完数据后达到周期结束ID: {}，第{}周期完成，清除限制继续传输后续数据]", logRef, endId, currentCycle);
            session.setEndId(null);
            cycleCompletedIndex = currentCycle;
            session.setCurrentCycleIndex(currentCycle + 1);
            session.setLastCumulativeDistance(0.0);
            session.setLastTime(null);
            session.setLastGroundSpeed(null);
            session.setHasSeenAirInStream(false);
            if (Boolean.TRUE.equals(session.getHasSeenApproachInStream())) {
                log.info("[TAXI_APPROACH_FLAG][ref({}) qarId({}) approach=false] reset by cycle endId reached after batch, endId={}",
                        logRef, session.getQarId(), endId);
            }
            session.setHasSeenApproachInStream(false);
            session.setLastProcessedPhase(null);
            if (session.getTakeoffRollMagneticHeadings() != null) {
                session.getTakeoffRollMagneticHeadings().clear();
            }
            session.setRunwayMagneticHeadingMedian(null);
            session.setLastGlideslopeCapture(null);
        }

        long tEnd = System.nanoTime();
        long codeNanos = tEnd - t0 - sqlNanos;
        double sqlSeconds = sqlNanos / 1_000_000_000.0;
        double codeSeconds = codeNanos / 1_000_000_000.0;
        log.debug("[runCompareDataBatchCore][ref({}) 单批完成 行数={} items={} sql={}s code={}s]",
                logRef, dataList.size(), items.size(), String.format("%.4f", sqlSeconds), String.format("%.4f", codeSeconds));
        if (sqlNanos > 1_000_000_000L) {
            log.warn("[runCompareDataBatchCore][ref({}) 单批SQL耗时偏高 sql={}s 行数={} lastId={}]",
                    logRef, String.format("%.4f", sqlSeconds), dataList.size(), session.getLastId());
        }
        return new CompareDataBatchOutcome(items, false, false, cycleCompletedIndex, sqlNanos, codeNanos);
    }

    /**
     * {@link #runCompareDataBatchCore} 的单次结果（供 WebSocket 与 HTTP 分页共用）。
     */
    public static final class CompareDataBatchOutcome {
        private final List<TaxiPhaseStreamMessage.TaxiPhaseDataItem> items;
        private final boolean noMoreDatabaseRows;
        private final boolean terminalAfterEndIdFilter;
        private final Integer cycleCompletedIndex;
        /** 本批 selectList 的 SQL 耗时（纳秒） */
        private final long sqlNanos;
        /** 本批除 SQL 外的代码处理耗时（纳秒） */
        private final long codeNanos;

        private CompareDataBatchOutcome(List<TaxiPhaseStreamMessage.TaxiPhaseDataItem> items,
                                        boolean noMoreDatabaseRows,
                                        boolean terminalAfterEndIdFilter,
                                        Integer cycleCompletedIndex,
                                        long sqlNanos,
                                        long codeNanos) {
            this.items = items != null ? items : Collections.emptyList();
            this.noMoreDatabaseRows = noMoreDatabaseRows;
            this.terminalAfterEndIdFilter = terminalAfterEndIdFilter;
            this.cycleCompletedIndex = cycleCompletedIndex;
            this.sqlNanos = sqlNanos;
            this.codeNanos = codeNanos;
        }

        public static CompareDataBatchOutcome noMoreRows() {
            return new CompareDataBatchOutcome(Collections.emptyList(), true, false, null, 0L, 0L);
        }

        public static CompareDataBatchOutcome terminalAfterEndIdFilter() {
            return new CompareDataBatchOutcome(Collections.emptyList(), false, true, null, 0L, 0L);
        }

        public List<TaxiPhaseStreamMessage.TaxiPhaseDataItem> getItems() {
            return items;
        }

        public boolean isNoMoreDatabaseRows() {
            return noMoreDatabaseRows;
        }

        public boolean isTerminalAfterEndIdFilter() {
            return terminalAfterEndIdFilter;
        }

        public Integer getCycleCompletedIndex() {
            return cycleCompletedIndex;
        }

        public long getSqlNanos() {
            return sqlNanos;
        }

        public long getCodeNanos() {
            return codeNanos;
        }
    }

    /**
     * 计算累计滑跑距离（米），与飞行复现 TaxiPhaseStreamService 完全一致。
     * 公式：距离 = ∫(速度×时间)，离散为 当前累计 = 上一累计 + Δ距离
     */
    private Double calculateCumulativeDistance(String lastAirspeedKmh, Double timeInterval, Double lastCumulativeDistance) {
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
        log.debug("[calculateCumulativeDistance] 上一条空速: {} km/h = {} m/s, 时间间隔: {}s, Δ距离: {}m, 累计: {}m",
                speedKmh, speedMetersPerSecond, timeInterval, deltaDistance, (lastCumulativeDistance != null ? lastCumulativeDistance : 0.0) + deltaDistance);
        return (lastCumulativeDistance != null ? lastCumulativeDistance : 0.0) + deltaDistance;
    }

    private static Double parseRadioAltitudeFt(String radioAltitudeStr) {
        if (radioAltitudeStr == null || radioAltitudeStr.trim().isEmpty()
                || "NULL".equalsIgnoreCase(radioAltitudeStr.trim())) {
            return null;
        }
        try {
            return Double.parseDouble(radioAltitudeStr.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Double parseGroundSpeedKnots(String groundSpeedStr) {
        if (groundSpeedStr == null || groundSpeedStr.trim().isEmpty()
                || "NULL".equalsIgnoreCase(groundSpeedStr.trim())) {
            return null;
        }
        try {
            return Double.parseDouble(groundSpeedStr.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Double parsePitchAngle(String pitchAngleStr) {
        if (pitchAngleStr == null || pitchAngleStr.trim().isEmpty()
                || "NULL".equalsIgnoreCase(pitchAngleStr.trim())) {
            return null;
        }
        try {
            return Double.parseDouble(pitchAngleStr.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Double parseMagneticHeading(String magneticHeadingStr) {
        if (magneticHeadingStr == null || magneticHeadingStr.trim().isEmpty()
                || "NULL".equalsIgnoreCase(magneticHeadingStr.trim())) {
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

    /**
     * 进近状态判定方式（与飞行复现 TaxiPhaseStreamService 完全一致）：
     * (empty_space_switch 包含 AIR) && (radio_altitude <= 1100) &&
     * 仅以 glide_slope_capture 是否包含 "GS ENGAGE" 作为进近判定条件。
     * <p>调用时已保证 isAirPhase 且 radioAlt<=1100。</p>
     */
    private static boolean isApproachPhase(QarDataDO data, Double radioAlt, String gsCapture) {
        return gsCapture != null && gsCapture.toUpperCase().contains("GS ENGAGE");
    }

}
