package cn.iocoder.yudao.module.data.controller.admin.vo;

import lombok.*;
import java.util.*;
import javax.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Data
public class FaultInformationSaveReqVO {

    private Integer id;

    @NotNull(message = "故障日期不能为空")
    private LocalDateTime faultDate;

    private String faultPhenomenon;

    @NotNull(message = "故障状态0已解决1排查中不能为空")
    private Integer faultStatus;

    private String causeOfFailure;

    private String resolver;

    private String remark;

}
