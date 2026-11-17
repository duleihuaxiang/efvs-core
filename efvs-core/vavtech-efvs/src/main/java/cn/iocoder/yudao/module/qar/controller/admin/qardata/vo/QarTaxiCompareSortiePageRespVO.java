package cn.iocoder.yudao.module.qar.controller.admin.qardata.vo;

import cn.iocoder.yudao.module.qar.websocket.message.TaxiPhaseStreamMessage;
import lombok.Data;

import java.util.List;

@Data
public class QarTaxiCompareSortiePageRespVO {

    private Long qarId;

    private List<TaxiPhaseStreamMessage.TaxiPhaseDataItem> items;

    private QarTaxiCompareSortieStateVO state;

    private Boolean completed;

    private Integer cycleCompletedIndex;
}
