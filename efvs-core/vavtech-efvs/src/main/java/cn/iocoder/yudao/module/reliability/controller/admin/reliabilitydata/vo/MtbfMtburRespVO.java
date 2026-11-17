package cn.iocoder.yudao.module.reliability.controller.admin.reliabilitydata.vo;


import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MtbfMtburRespVO {

    @ExcelProperty("主键，自增ID")
    private Integer id;

    @ExcelProperty("件号")
    private String partNo;

    @ExcelProperty("设备名称")
    private String equipmentName;

    @ExcelProperty("总飞行时间")
    private BigDecimal totalFlightTime;

    @ExcelProperty("总装机数")
    private Integer totalInstallCount;

    @ExcelProperty("故障次数（次）")
    private Integer failureCount;

    @ExcelProperty("非计划拆换次数（次）")
    private Integer unplannedReplaceCount;

    @ExcelProperty("MTBF实际值")
    private BigDecimal mtbfActual;

    @ExcelProperty("MTBF指标值")
    private BigDecimal mtbfTarget;

    @ExcelProperty("MTBUR实际值")
    private BigDecimal mtburActual;

    @ExcelProperty("MTBUR指标值")
    private BigDecimal mtburTarget;

}
