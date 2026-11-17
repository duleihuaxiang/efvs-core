package cn.iocoder.yudao.module.data.controller.admin.trainingmanagement.vo;

import lombok.*;
import java.util.*;
import javax.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Data
public class TrainingManagementSaveReqVO {

    private Integer id;

    @NotNull(message = "编号ID不能为空")
    private Integer serialNumber;

    @NotEmpty(message = "流程名称不能为空")
    private String processName;

    @NotNull(message = "发起时间不能为空")
    private LocalDateTime initiationTime;

    @NotEmpty(message = "发起人不能为空")
    private String initiator;

    @NotEmpty(message = "当前环节不能为空")
    private String currentStage;

    private String remark;

}
