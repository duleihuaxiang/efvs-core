package cn.iocoder.yudao.module.data.controller.admin.pretrainingpreparation.vo;

import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Data
@ExcelIgnoreUnannotated
public class PreTrainingPreparationRespVO {

    @ExcelProperty("ID")
    private Integer id;

    @ExcelProperty("编号")
    private Integer serialNumber;

    @ExcelProperty("培训前事项")
    private String preTrainingMatters;

    @ExcelProperty("是否完成 (0 否 1 是)")
    private Integer isCompleted;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @ExcelProperty("备注")
    private String remark;

}
