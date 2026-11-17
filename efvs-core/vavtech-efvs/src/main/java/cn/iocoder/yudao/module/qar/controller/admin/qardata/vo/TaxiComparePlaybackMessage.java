package cn.iocoder.yudao.module.qar.controller.admin.qardata.vo;

import cn.iocoder.yudao.module.qar.websocket.message.TaxiPhaseStreamMessage;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 性能分析一次性聚合返回的 content 结构。
 */
@Data
public class TaxiComparePlaybackMessage {

    /**
     * 固定为 data，表示携带数据。
     */
    private String type = "data";

    /**
     * 每个架次的完整数据列表。
     */
    private List<Sortie> sorties = new ArrayList<>();

    /**
     * 所有架次合计条数（items 总和）。
     */
    private Long totalCount;

    @Data
    public static class Sortie {
        private Long qarId;
        private List<TaxiPhaseStreamMessage.TaxiPhaseDataItem> data;
    }
}

