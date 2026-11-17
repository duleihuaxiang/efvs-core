package cn.iocoder.yudao.module.data.controller.admin.trainingrequirements.vo;

import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Data
@ExcelIgnoreUnannotated
public class TrainingRequirementsRespVO {

    @ExcelProperty("ID")
    private Integer id;

    @ExcelProperty("培训主题")
    private String trainingTheme;

    @ExcelProperty("客户名称")
    private String customerName;

    @ExcelProperty("培训内容")
    private String trainingContent;

    @ExcelProperty("培训类型")
    private String trainingType;

    @ExcelProperty("培训对象")
    private String trainingTarget;

    @ExcelProperty("培训时间")
    private LocalDateTime trainingTime;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @ExcelProperty("备注")
    private String remark;

}
