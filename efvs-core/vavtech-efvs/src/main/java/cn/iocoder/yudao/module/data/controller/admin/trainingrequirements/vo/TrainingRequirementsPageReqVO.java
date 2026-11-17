package cn.iocoder.yudao.module.data.controller.admin.trainingrequirements.vo;

import lombok.*;
import java.util.*;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Data
public class TrainingRequirementsPageReqVO extends PageParam {

    private String trainingTheme;

    private String customerName;

    private String trainingContent;

    private String trainingType;

    private String trainingTarget;

}
