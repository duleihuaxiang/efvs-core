package cn.iocoder.yudao.module.training.controller.admin.trainingexperience.vo;

import lombok.*;
import java.util.*;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Data
public class FileUploadPageReqVO extends PageParam {

    private Integer bizId;

    private String fileName;

    private String fileSize;

    private String fileUrl;

    private String fileAbsoluteUrl;

}
