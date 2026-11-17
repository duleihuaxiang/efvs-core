package cn.iocoder.yudao.module.qar.service.qardata;

import cn.iocoder.yudao.module.qar.controller.admin.qardata.vo.TaxiComparePlaybackMessage;
import cn.iocoder.yudao.module.qar.websocket.message.TaxiPhaseStreamMessage;
import cn.iocoder.yudao.module.qar.websocket.service.TaxiCompareStreamService;
import cn.iocoder.yudao.module.qar.websocket.session.TaxiPhaseStreamSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 性能分析（多架次对比）一次性聚合：内部仍按每批 10 条读取并复用阶段/累计距离逻辑，
 * 但对每个架次循环分页直至完成后再一次性返回给前端。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QarTaxiComparePlaybackService {

    /**
     * REST 一次性聚合的读取批大小。
     *
     * <p>WebSocket 流式取 10 条是为了推送平滑；但 compare-play 是在一次请求里把整架次读完，
     * 用 10 会让 SQL 往返次数 = 行数 / 批大小（3 万行 = 3000 次查询），这是接口慢的主因。
     * 改成大页后往返次数降为 行数 / 批大小（3 万行 = 6 次），SQL 不再是瓶颈；
     * 每批内存占用有界（约几千个 DO），不会随整架次行数增长。</p>
     */
    private static final int REST_AGGREGATE_BATCH_SIZE = 5000;

    private final TaxiCompareStreamService taxiCompareStreamService;

    public TaxiComparePlaybackMessage play(List<Long> qarIds) {
        TaxiComparePlaybackMessage msg = new TaxiComparePlaybackMessage();
        if (qarIds == null || qarIds.isEmpty()) {
            msg.setTotalCount(0L);
            return msg;
        }

        // 去重保持稳定：HashSet + 重新按输入顺序输出
        Set<Long> seen = new HashSet<>();
        List<Long> distinct = new ArrayList<>();
        for (Long id : qarIds) {
            if (id == null) continue;
            if (seen.add(id)) distinct.add(id);
        }

        long total = 0L;
        long totalSqlNanos = 0L;
        long totalCodeNanos = 0L;
        long totalBatchCount = 0L;
        long playStart = System.nanoTime();
        for (Long qarId : distinct) {
            TaxiPhaseStreamSession session = new TaxiPhaseStreamSession("rest-compare-play");
            session.setQarId(qarId);
            session.setLastId(-1);
            session.setLastCumulativeDistance(0.0);
            session.setTotalCount(0L);
            session.setCurrentCycleIndex(1);

            long sortieSqlNanos = 0L;
            long sortieCodeNanos = 0L;
            long sortieBatchCount = 0L;
            List<TaxiPhaseStreamMessage.TaxiPhaseDataItem> all = new ArrayList<>();
            while (true) {
                TaxiCompareStreamService.CompareDataBatchOutcome out =
                        taxiCompareStreamService.runCompareDataBatchCore(session, REST_AGGREGATE_BATCH_SIZE, "rest-play-" + qarId);
                sortieSqlNanos += out.getSqlNanos();
                sortieCodeNanos += out.getCodeNanos();
                sortieBatchCount++;
                if (out.getItems() != null && !out.getItems().isEmpty()) {
                    all.addAll(out.getItems());
                }
                if (out.isNoMoreDatabaseRows() || out.isTerminalAfterEndIdFilter()) {
                    break;
                }
                // 防止极端情况死循环：lastId 不前进则终止
                if (session.getLastId() == null) break;
            }

            TaxiComparePlaybackMessage.Sortie s = new TaxiComparePlaybackMessage.Sortie();
            s.setQarId(qarId);
            s.setData(all);
            msg.getSorties().add(s);
            total += all.size();
            totalSqlNanos += sortieSqlNanos;
            totalCodeNanos += sortieCodeNanos;
            totalBatchCount += sortieBatchCount;
            log.info("[play][qarId({}) 聚合完成，items={}，批次数={}，SQL耗时={}s，代码耗时={}s]",
                    qarId, all.size(), sortieBatchCount,
                    seconds(sortieSqlNanos), seconds(sortieCodeNanos));
        }

        long playNanos = System.nanoTime() - playStart;
        log.info("[play][全部完成，架次数={}，items={}，总批次数={}，SQL合计={}s，代码合计={}s，服务总耗时={}s（差值即其他开销={}s）]",
                distinct.size(), total, totalBatchCount,
                seconds(totalSqlNanos), seconds(totalCodeNanos), seconds(playNanos),
                seconds(Math.max(0L, playNanos - totalSqlNanos - totalCodeNanos)));

        msg.setTotalCount(total);
        return msg;
    }

    private static String seconds(long nanos) {
        return String.format("%.4f", nanos / 1_000_000_000.0);
    }
}

