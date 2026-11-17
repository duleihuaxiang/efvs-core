package cn.iocoder.yudao.module.qar.controller.admin.qardata.vo;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * taxi-phase 3D 复现「开始播放」请求参数。
 */
@Data
public class QarTaxiPhasePlaybackReqVO {

    /**
     * QAR 主表 id。
     */
    @NotNull
    private Long id;

    /**
     * 起始记录 id（可选）。
     *
     * <p>用于与原 WebSocket {@code startId} 保持一致：查询 id &gt;= startId。</p>
     */
    @Min(0)
    private Integer startId;

    /**
     * 周期结束 id（可选）。
     *
     * <p>本次接口默认不做多周期拆分，仅用于限制返回数据范围：只返回 id &lt;= endId 的记录。</p>
     */
    @Min(0)
    private Integer endId;
}

