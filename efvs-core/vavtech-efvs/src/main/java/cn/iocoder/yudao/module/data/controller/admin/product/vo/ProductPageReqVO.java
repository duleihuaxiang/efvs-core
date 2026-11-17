package cn.iocoder.yudao.module.data.controller.admin.product.vo;

import lombok.*;
import java.util.*;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Data
public class ProductPageReqVO extends PageParam {

    private String name;

    private String signVersion;

    private String partNumber;

    private String customerNumber;

    private String baseline;

    private String deliveryDestination;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") // 加这一行！
    private List<LocalDateTime> deliveryTime;
}
