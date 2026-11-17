package cn.iocoder.yudao.module.data.controller.admin.pretrainingpreparation.vo;

import lombok.*;
import java.util.*;
import javax.validation.constraints.*;

@Data
public class PreTrainingPreparationSaveReqVO {

    private Integer id;

    private Integer serialNumber;

    private String preTrainingMatters;

    private Integer isCompleted;

    private String remark;

}
