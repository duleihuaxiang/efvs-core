package cn.iocoder.yudao.module.qar.controller.admin.qardata.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 性能分析 HTTP 分页：单架次游标状态（与 {@link cn.iocoder.yudao.module.qar.websocket.session.TaxiPhaseStreamSession} 对齐）。
 * <p>首包可只传 {@code qarId}；后续请求将上一响应中的 {@code state} 原样带回。</p>
 */
@Data
public class QarTaxiCompareSortieStateVO {

    @NotNull(message = "qarId 不能为空")
    private Long qarId;

    private Integer lastId;

    private Double lastCumulativeDistance;

    private String lastTime;

    private String lastGroundSpeed;

    private Boolean hasSeenAirInStream;

    private Boolean hasSeenApproachInStream;

    private Boolean hasSeenTaxiInStream;

    private String lastProcessedPhase;

    private String lastGlideslopeCapture;

    private List<Double> takeoffRollMagneticHeadings;

    private Double runwayMagneticHeadingMedian;

    private Long totalCount;

    private Integer currentCycleIndex;

    private Integer endId;
}
