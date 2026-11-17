package cn.iocoder.yudao.module.reliability.controller.admin.reliabilitydata.vo;

import lombok.*;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Data
public class ReliabilityDataPageReqVO extends PageParam {

    private String reliabilityDataFormNo;

    private String customerName;

    private String partName;

    private String version;

    private String partNumber;

    private String serialNumber;

    private String model;

    private String series;

    private String machineNumber;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] reportStartDate;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] reportEndDate;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] removalDate;

    private Integer isProductIssue;

    private String removalType;

    private String removedPartNumber;

    private String removedPartSerialNumber;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    private Integer level;

    private List<Integer> levelList;

    private List<Integer> approvalStatuslList;

}
