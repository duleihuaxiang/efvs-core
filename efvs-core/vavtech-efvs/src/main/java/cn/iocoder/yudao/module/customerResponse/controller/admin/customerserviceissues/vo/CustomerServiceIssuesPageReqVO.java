package cn.iocoder.yudao.module.customerResponse.controller.admin.customerserviceissues.vo;

import lombok.*;
import java.util.*;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Data
public class CustomerServiceIssuesPageReqVO extends PageParam {

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] recordDate;

    private String questionNumber;

    private Integer priority;

    private String customerProblemDescription;

    private String customerProblemImpact;

    private String replyContent;

}
