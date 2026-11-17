package cn.iocoder.yudao.module.reliability.controller.admin.reliabilitydata.vo;

import lombok.*;
import javax.validation.constraints.*;
import java.time.LocalDateTime;

@Data
public class ReliabilityDataSaveReqVO {

    private Integer id;

    private String processInstanceId;

    private String processKey;

    private String taskId;

    private Integer approvalStatus;

    private String reliabilityDataFormNo;

    private String customerId;

    private String customerName;

    private String partName;

    private String partNumber;

    private String sruName;

    private String sruNumber;

    private String serialNumber;

    private String productPartNumber;

    private Integer level;

    private String model;

    private String series;

    private String machineNumber;

    private LocalDateTime reportStartDate;

    private LocalDateTime reportEndDate;

    private LocalDateTime removalDate;

    private LocalDateTime installationDate;

    private String failureReportNumber;

    @NotNull(message = "是否是产品问题：0=否，1=是不能为空")
    private Integer isProductIssue;

    private String removalType;

    private String removalCause;

    private String failureDescription;

    private String removedPartNumber;

    private String removedPartSerialNumber;

    private String remark;

}
