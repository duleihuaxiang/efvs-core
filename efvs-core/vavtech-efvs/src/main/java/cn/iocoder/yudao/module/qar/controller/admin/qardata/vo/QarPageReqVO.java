package cn.iocoder.yudao.module.qar.controller.admin.qardata.vo;

import lombok.*;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class QarPageReqVO extends PageParam {

    private String airline;

    private String model;

    private String efvsConfiguration;

    private String fileName;

    private String flag;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") // 加这一行！
    private List<LocalDateTime> timeList;

    private List<Long> ids;

}
