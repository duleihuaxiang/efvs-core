
package cn.iocoder.yudao.module.reliability.controller.admin.reliabilitydata.vo;

import cn.idev.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FlightReliableDataMgrRespVO {
    @ExcelProperty("主键，自增ID")
    private Integer id;

    @ExcelProperty("部件名称")
    private String partName;

    @ExcelProperty("件号")
    private String partNumber;

    private String serialNumber;

    private String version;

    @ExcelProperty("故障次数")
    private Integer issueCount;

    @ExcelProperty("最新报告日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime reportStartDate;

    @ExcelProperty("故障描述")
    private String failureDescription;

    @ExcelProperty("最新创建日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime createTime;

    @ExcelProperty("层级")
    private Integer level;

    @ExcelProperty("拆卸日期")
    private String removalDate;

}
