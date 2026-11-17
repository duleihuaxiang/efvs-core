package cn.iocoder.yudao.module.data.controller.admin.trainingattendanceassessment.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Data
public class TrainingAttendanceAssessmentSaveReqVO {

    private Integer id;
    /**
     * 流程实例ID
     */
    private String processInstanceId;

    private String name;

    private String courseName;

    private LocalDateTime checkInTime;

    private Integer learningTime;

    private Integer isCompleted;

    private Integer completedForm;

    private Integer score;

    private String receiveCertificate;

    private String instructor;

    private LocalDateTime completionDate;

    private String remark;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private String fileUploadId;

}
