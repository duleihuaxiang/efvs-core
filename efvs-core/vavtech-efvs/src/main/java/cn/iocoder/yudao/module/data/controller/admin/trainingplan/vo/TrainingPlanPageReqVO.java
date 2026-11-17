package cn.iocoder.yudao.module.data.controller.admin.trainingplan.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import java.util.*;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Data
public class TrainingPlanPageReqVO extends PageParam {

    private String annualPlanProjectNumberId;

    private String trainingProgramsCourses;

    private String trainingTargets;

    private String trainingLocation;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private String trainingStartTime;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private String trainingEndTime;

    private Integer projectLeader;
}
