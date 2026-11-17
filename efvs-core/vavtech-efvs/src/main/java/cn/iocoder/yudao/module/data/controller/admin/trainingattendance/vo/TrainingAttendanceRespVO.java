package cn.iocoder.yudao.module.data.controller.admin.trainingattendance.vo;

import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Data
@ExcelIgnoreUnannotated
public class TrainingAttendanceRespVO {

    @ExcelProperty("ID")
    private Integer id;

    @ExcelProperty("编号")
    private Integer serialNumber;

    @ExcelProperty("姓名 ")
    private String name;

    @ExcelProperty("单位名称")
    private String unitName;

    @ExcelProperty("培训主题")
    private String trainingTheme;

    @ExcelProperty("培训时间")
    private LocalDateTime trainingTime;

    @ExcelProperty("培训讲师 (0 否 1 是)")
    private Integer trainingInstructor;

    @ExcelProperty("培训类别")
    private String trainingCategory;

    @ExcelProperty("培训地点")
    private String trainingLocation;

    @ExcelProperty("培训内容")
    private String trainingContent;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @ExcelProperty("备注")
    private String remark;

}
