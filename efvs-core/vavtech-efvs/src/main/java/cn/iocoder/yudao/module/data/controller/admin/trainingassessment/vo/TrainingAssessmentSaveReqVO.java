package cn.iocoder.yudao.module.data.controller.admin.trainingassessment.vo;

import lombok.*;
import java.util.*;
import javax.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Data
public class TrainingAssessmentSaveReqVO {

    private Integer id;

    private Integer serialNumber;

    private String name;

    private String courseName;

    private Integer learningTime;

    private Integer isCompleted;

    private String completedForm;

    private String score;

    private String instructor;

    private LocalDateTime completionDate;

    private String remark;

}
