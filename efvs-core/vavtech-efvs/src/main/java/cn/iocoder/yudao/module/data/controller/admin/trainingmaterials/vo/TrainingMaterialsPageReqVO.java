package cn.iocoder.yudao.module.data.controller.admin.trainingmaterials.vo;

import lombok.*;
import java.util.*;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Data
public class TrainingMaterialsPageReqVO extends PageParam {

    private Integer trainingId;

    private String documentName;

    private String containingDirectory;

    private String category;

}
