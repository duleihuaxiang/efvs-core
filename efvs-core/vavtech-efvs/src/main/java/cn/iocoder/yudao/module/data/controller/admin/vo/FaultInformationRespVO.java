package cn.iocoder.yudao.module.data.controller.admin.vo;

import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Data
@ExcelIgnoreUnannotated
public class FaultInformationRespVO {

    @ExcelProperty("故障信息")
    private Integer id;

    @ExcelProperty("故障日期")
    private String faultDate;

    @ExcelProperty("故障现象")
    private String faultPhenomenon;

    @ExcelProperty("故障状态0已解决1排查中")
    private Integer faultStatus;

    @ExcelProperty("故障原因")
    private String causeOfFailure;

    @ExcelProperty("解决人")
    private String resolver;

    @ExcelProperty(" 备注")
    private String remark;

}
