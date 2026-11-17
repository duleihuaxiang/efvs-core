package cn.iocoder.yudao.module.reliability.controller.admin.reliabilitydata.vo;


import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MtburRespVO {
//    @Schema(description = "主键，自增ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1328")
//    @ExcelProperty("主键，自增ID")
//    private Integer id;

    @ExcelProperty("件号")
    private String partNo;

    @ExcelProperty("设备名称")
    private String equipmentName;

    @ExcelProperty("总飞行时间")
    private BigDecimal totalFlightTime;

    @ExcelProperty("总装机数")
    private Integer totalInstallCount;

    @ExcelProperty("非计划拆换次数（次）")
    private Integer unplannedReplaceCount;

    @ExcelProperty("MTBUR（实际值）")
    private BigDecimal mtburActual;

    @ExcelProperty("MTBUR（指标值）")
    private BigDecimal mtburTarget;

}
