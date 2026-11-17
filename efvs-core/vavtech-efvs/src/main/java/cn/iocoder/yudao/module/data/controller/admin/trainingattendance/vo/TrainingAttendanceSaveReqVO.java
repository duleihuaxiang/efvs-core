package cn.iocoder.yudao.module.data.controller.admin.trainingattendance.vo;

import lombok.*;
import java.util.*;
import javax.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Data
public class TrainingAttendanceSaveReqVO {

    private Integer id;

//    @NotNull(message = "编号不能为空")
    private Integer serialNumber;

//    @NotEmpty(message = "姓名 不能为空")
    private String name;

//    @NotEmpty(message = "单位名称不能为空")
    private String unitName;

//    @NotEmpty(message = "培训主题不能为空")
    private String trainingTheme;

//    @NotNull(message = "培训时间不能为空")
    private LocalDateTime trainingTime;

//    @NotNull(message = "培训讲师 (0 否 1 是)不能为空")
    private Integer trainingInstructor;

//    @NotEmpty(message = "培训类别不能为空")
    private String trainingCategory;

//    @NotEmpty(message = "培训地点不能为空")
    private String trainingLocation;

//    @NotEmpty(message = "培训内容不能为空")
    private String trainingContent;

    private String remark;

}
