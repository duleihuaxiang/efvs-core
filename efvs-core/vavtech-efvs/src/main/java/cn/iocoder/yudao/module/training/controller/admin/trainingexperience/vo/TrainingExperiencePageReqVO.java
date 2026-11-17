package cn.iocoder.yudao.module.training.controller.admin.trainingexperience.vo;

import lombok.*;
import java.util.*;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Data
public class TrainingExperiencePageReqVO extends PageParam {

    private String trainingTheme;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] trainingTime;

    private String trainingLocation;

    private String achieveResults;

    private String trainingTeachers;

}
