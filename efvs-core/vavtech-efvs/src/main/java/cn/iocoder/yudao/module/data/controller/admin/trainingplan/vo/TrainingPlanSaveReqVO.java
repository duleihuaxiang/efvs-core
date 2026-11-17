package cn.iocoder.yudao.module.data.controller.admin.trainingplan.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.*;

import java.time.LocalDate;
import java.util.*;
import javax.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Data
public class TrainingPlanSaveReqVO {

    private Integer id;

    private String trainingTheme;

    @NotNull(message = "培训计划编号不能为空")
    private String annualPlanProjectNumberId;

    private String trainingProgramsCourses;

    private String trainingTargets;

    private Integer numberOfTrainees;

    private String  trainingStartTime;

    private String  trainingEndTime;

    private String trainingLocation;

    private Integer projectLeader;

    private String remark;

}
