package cn.iocoder.yudao.module.data.controller.admin.productfile.vo;

import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Data
@ExcelIgnoreUnannotated
public class ProductFileRespVO {

    @ExcelProperty("档案主键")
    private Integer id;

    @ExcelProperty("产品件号")
    private String partNumber;

    @ExcelProperty("产品序列号")
    private String sequenceNumber;

    @ExcelProperty("零部件名称")
    private String name;

    @ExcelProperty("图号")
    private String drawingNumber;

    @ExcelProperty("生产编号")
    private String productionNumber;

    @ExcelProperty("对应基线")
    private String baseline;

    @ExcelProperty("客户编号")
    private String customerNumber;

    @ExcelProperty("图纸版本")
    private String drawingVersion;

    @ExcelProperty("客户名称")
    private String customerName;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @ExcelProperty("备注")
    private String remark;

}
