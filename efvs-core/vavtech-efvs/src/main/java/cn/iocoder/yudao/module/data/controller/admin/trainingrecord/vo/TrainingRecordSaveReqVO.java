package cn.iocoder.yudao.module.data.controller.admin.trainingrecord.vo;

import lombok.*;
import java.util.*;
import javax.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Data
public class TrainingRecordSaveReqVO {

    private Integer id;

    @NotNull(message = "编号不能为空")
    private Integer serialNumber;

    @NotEmpty(message = "姓名不能为空")
    private String name;

    @NotEmpty(message = "课程名称不能为空")
    private String courseName;

    @NotNull(message = "学习时间不能为空")
    private Long learningTime;

    @NotNull(message = "是否完成 (0 否 1 是)不能为空")
    private Integer isCompleted;

    @NotEmpty(message = "完成形式不能为空")
    private String perfectiveForm;

    @NotNull(message = "成绩不能为空")
    private Integer grades;

    @NotEmpty(message = "教员不能为空")
    private String instructor;

    @NotNull(message = "完成日期不能为空")
    private Long completionDate;

    private String remark;

}
