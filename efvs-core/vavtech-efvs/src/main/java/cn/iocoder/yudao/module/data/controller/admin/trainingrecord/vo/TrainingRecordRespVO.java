package cn.iocoder.yudao.module.data.controller.admin.trainingrecord.vo;

import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Data
@ExcelIgnoreUnannotated
public class TrainingRecordRespVO {

    @ExcelProperty("ID")
    private Integer id;

    @ExcelProperty("编号")
    private Integer serialNumber;

    @ExcelProperty("姓名")
    private String name;

    @ExcelProperty("课程名称")
    private String courseName;

    @ExcelProperty("学习时间")
    private Integer learningTime;

    @ExcelProperty("是否完成 (0 否 1 是)")
    private Integer isCompleted;

    @ExcelProperty("完成形式")
    private String perfectiveForm;

    @ExcelProperty("成绩")
    private Integer grades;

    @ExcelProperty("教员")
    private String instructor;

    @ExcelProperty("完成日期")
    private LocalDateTime completionDate;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @ExcelProperty("备注")
    private String remark;

}
