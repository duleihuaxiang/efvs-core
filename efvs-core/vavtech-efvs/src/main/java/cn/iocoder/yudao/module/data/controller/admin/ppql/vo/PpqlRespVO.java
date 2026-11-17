package cn.iocoder.yudao.module.data.controller.admin.ppql.vo;

import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Data
@ExcelIgnoreUnannotated
public class PpqlRespVO {

    @ExcelProperty("ID")
    private Integer id;

    @ExcelProperty("问题描述")
    private String problemDescription;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @ExcelProperty("备注")
    private String remark;

}
