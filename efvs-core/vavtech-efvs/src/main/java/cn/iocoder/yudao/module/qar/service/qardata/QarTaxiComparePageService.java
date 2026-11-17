package cn.iocoder.yudao.module.qar.service.qardata;

import cn.iocoder.yudao.module.qar.controller.admin.qardata.vo.QarTaxiComparePageReqVO;
import cn.iocoder.yudao.module.qar.controller.admin.qardata.vo.QarTaxiComparePageRespVO;
import cn.iocoder.yudao.module.qar.controller.admin.qardata.vo.QarTaxiCompareSortiePageRespVO;
import cn.iocoder.yudao.module.qar.controller.admin.qardata.vo.QarTaxiCompareSortieStateVO;
import cn.iocoder.yudao.module.qar.websocket.service.TaxiCompareStreamService;
import cn.iocoder.yudao.module.qar.websocket.session.TaxiPhaseStreamSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 性能分析：HTTP 分页拉取多架次 taxi-phase 数据，逻辑与 {@link TaxiCompareStreamService} WebSocket 一致。
 */
@Service
@RequiredArgsConstructor
public class QarTaxiComparePageService {

    private final TaxiCompareStreamService taxiCompareStreamService;

    public QarTaxiComparePageRespVO fetchPage(QarTaxiComparePageReqVO req) {
        int pageSize = req.getPageSize() != null && req.getPageSize() > 0
                ? req.getPageSize()
                : QarTaxiComparePageReqVO.DEFAULT_PAGE_SIZE;

        List<QarTaxiCompareSortiePageRespVO> rows = new ArrayList<>();
        for (QarTaxiCompareSortieStateVO st : req.getSorties()) {
            if (st.getQarId() == null) {
                continue;
            }
            TaxiPhaseStreamSession session = toSession(st);
            TaxiCompareStreamService.CompareDataBatchOutcome outcome =
                    taxiCompareStreamService.runCompareDataBatchCore(session, pageSize, "rest-" + st.getQarId());

            QarTaxiCompareSortiePageRespVO row = new QarTaxiCompareSortiePageRespVO();
            row.setQarId(st.getQarId());
            row.setItems(outcome.getItems());
            row.setState(fromSession(session));
            row.setCompleted(outcome.isNoMoreDatabaseRows() || outcome.isTerminalAfterEndIdFilter());
            row.setCycleCompletedIndex(outcome.getCycleCompletedIndex());
            rows.add(row);
        }

        QarTaxiComparePageRespVO resp = new QarTaxiComparePageRespVO();
        resp.setPageSize(pageSize);
        resp.setSorties(rows);
        return resp;
    }

    private static TaxiPhaseStreamSession toSession(QarTaxiCompareSortieStateVO vo) {
        TaxiPhaseStreamSession s = new TaxiPhaseStreamSession("rest-http");
        s.setQarId(vo.getQarId());
        int lid = vo.getLastId() != null ? vo.getLastId() : -1;
        s.setLastId(lid);
        s.setLastCumulativeDistance(vo.getLastCumulativeDistance() != null ? vo.getLastCumulativeDistance() : 0.0);
        s.setLastTime(vo.getLastTime());
        s.setLastGroundSpeed(vo.getLastGroundSpeed());
        s.setHasSeenAirInStream(Boolean.TRUE.equals(vo.getHasSeenAirInStream()));
        s.setHasSeenApproachInStream(Boolean.TRUE.equals(vo.getHasSeenApproachInStream()));
        s.setHasSeenTaxiInStream(Boolean.TRUE.equals(vo.getHasSeenTaxiInStream()));
        s.setLastProcessedPhase(vo.getLastProcessedPhase());
        s.setLastGlideslopeCapture(vo.getLastGlideslopeCapture());
        if (vo.getTakeoffRollMagneticHeadings() != null) {
            s.setTakeoffRollMagneticHeadings(new ArrayList<>(vo.getTakeoffRollMagneticHeadings()));
        } else {
            s.setTakeoffRollMagneticHeadings(new ArrayList<>());
        }
        s.setRunwayMagneticHeadingMedian(vo.getRunwayMagneticHeadingMedian());
        s.setTotalCount(vo.getTotalCount() != null ? vo.getTotalCount() : 0L);
        s.setCurrentCycleIndex(vo.getCurrentCycleIndex() != null ? vo.getCurrentCycleIndex() : 1);
        s.setEndId(vo.getEndId());
        return s;
    }

    private static QarTaxiCompareSortieStateVO fromSession(TaxiPhaseStreamSession s) {
        QarTaxiCompareSortieStateVO vo = new QarTaxiCompareSortieStateVO();
        vo.setQarId(s.getQarId());
        vo.setLastId(s.getLastId());
        vo.setLastCumulativeDistance(s.getLastCumulativeDistance());
        vo.setLastTime(s.getLastTime());
        vo.setLastGroundSpeed(s.getLastGroundSpeed());
        vo.setHasSeenAirInStream(s.getHasSeenAirInStream());
        vo.setHasSeenApproachInStream(s.getHasSeenApproachInStream());
        vo.setHasSeenTaxiInStream(s.getHasSeenTaxiInStream());
        vo.setLastProcessedPhase(s.getLastProcessedPhase());
        vo.setLastGlideslopeCapture(s.getLastGlideslopeCapture());
        if (s.getTakeoffRollMagneticHeadings() != null) {
            vo.setTakeoffRollMagneticHeadings(new ArrayList<>(s.getTakeoffRollMagneticHeadings()));
        }
        vo.setRunwayMagneticHeadingMedian(s.getRunwayMagneticHeadingMedian());
        vo.setTotalCount(s.getTotalCount());
        vo.setCurrentCycleIndex(s.getCurrentCycleIndex());
        vo.setEndId(s.getEndId());
        return vo;
    }
}
