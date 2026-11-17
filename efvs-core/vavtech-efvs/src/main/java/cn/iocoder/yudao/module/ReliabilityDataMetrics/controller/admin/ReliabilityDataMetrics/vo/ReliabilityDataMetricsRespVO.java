package cn.iocoder.yudao.module.ReliabilityDataMetrics.controller.admin.ReliabilityDataMetrics.vo;

import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Data
@ExcelIgnoreUnannotated
public class ReliabilityDataMetricsRespVO {

    @ExcelProperty("id")
    private Integer id;

    @ExcelProperty("部件名称")
    private String componentName;

    @ExcelProperty("件号")
    private String partNumber;

    @ExcelProperty("部件类别")
    private String componentCategory;

    @ExcelProperty("MTBUR指标")
    private Integer mtbf;

    @ExcelProperty("MTBF指标")
    private Integer mtbur;

    @ExcelProperty("数量")
    private Integer count;

    @ExcelProperty("修改时间")
    private LocalDateTime updateTime;

    @ExcelProperty("备注")
    private String remark;

}
