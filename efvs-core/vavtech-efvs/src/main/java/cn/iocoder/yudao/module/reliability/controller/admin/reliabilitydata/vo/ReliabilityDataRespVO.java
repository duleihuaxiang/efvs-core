package cn.iocoder.yudao.module.reliability.controller.admin.reliabilitydata.vo;

import cn.idev.excel.annotation.write.style.ColumnWidth;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.*;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Data
@ExcelIgnoreUnannotated
public class ReliabilityDataRespVO {

//    @ExcelProperty("主键，自增ID")
    private Integer id;

    private String processInstanceId;

    private String processKey;

    private String taskId;

    private Integer approvalStatus;

//    @ExcelProperty("可靠性数据收集表单编号")
    private String reliabilityDataFormNo;

//    @ExcelProperty("客户id")
    private String customerId;

    @ExcelProperty("客户名称")
    private String customerName;

    @ExcelProperty("部件名称")
    private String partName;

    @ExcelProperty("件号")
    private String partNumber;

//    @ExcelProperty("SRU名称")
    private String sruName;

//    @ExcelProperty("SRU代号")
    private String sruNumber;

    @ExcelProperty("序列号")
    private String serialNumber;

//    @ExcelProperty("所属部件代号（所属部件件号）")
    private String productPartNumber;

//    @ExcelProperty("层级")
    private Integer level;

    @ExcelProperty("机型")
    private String model;

    @ExcelProperty("系列")
    private String series;

    @ExcelProperty("版本")
    private String version;

    @ExcelProperty("机号")
    private String machineNumber;

    @ExcelProperty("报告开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime reportStartDate;

    @ExcelProperty("报告结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime reportEndDate;

    @ExcelProperty(value = "拆卸日期")
    @ColumnWidth(20)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime removalDate;

//    @ExcelProperty("装机日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime installationDate;

    @ExcelProperty("故障报告编码")
    private String failureReportNumber;

//    @ExcelProperty("是否是产品问题：0=否，1=是")
    private Integer isProductIssue;

    @ExcelProperty("拆卸类型：S-计划拆换；U-非计划拆换")
    private String removalType;

    @ExcelProperty("拆换原因")
    private String removalCause;

    @ExcelProperty("故障描述")
    private String failureDescription;

    @ExcelProperty("拆卸部件号")
    private String removedPartNumber;

    @ExcelProperty("拆卸部件序列号")
    private String removedPartSerialNumber;

//    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

//    @ExcelProperty("备注")
    private String remark;

}
