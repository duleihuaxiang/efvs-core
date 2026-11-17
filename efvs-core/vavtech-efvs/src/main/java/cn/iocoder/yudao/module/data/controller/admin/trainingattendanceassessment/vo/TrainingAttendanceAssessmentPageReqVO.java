package cn.iocoder.yudao.module.data.controller.admin.trainingattendanceassessment.vo;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.*;
import java.util.*;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Data
public class TrainingAttendanceAssessmentPageReqVO extends PageParam {

    private String trainingTheme;

    private String instructor;

    private String trainingLocation;

    private String name;

    private String courseName;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] checkInTime;

    private Integer completedForm;

    private Integer score;

    private String receiveCertificate;

}
