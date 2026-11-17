package cn.iocoder.yudao.module.qar.controller.admin.qardata.vo;

import lombok.Data;

/**
 * 滑跑周期VO
 *
 * @author admin
 */
@Data
public class TaxiPhaseCycleVO {

    private Integer cycleIndex;

    private String startTime;

    private String endTime;

    private Integer startId;

    private Integer endId;

    private Long dataCount;

    private String description;
}
