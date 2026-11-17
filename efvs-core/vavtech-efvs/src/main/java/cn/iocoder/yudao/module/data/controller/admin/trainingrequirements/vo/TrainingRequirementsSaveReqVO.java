package cn.iocoder.yudao.module.data.controller.admin.trainingrequirements.vo;

import lombok.*;
import java.util.*;
import javax.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Data
public class TrainingRequirementsSaveReqVO {

    private Integer id;

    private String trainingTheme;

    private String customerName;

    private String trainingContent;

    private String trainingType;

    private String trainingTarget;

    private LocalDateTime trainingTime;

    private String remark;

}
