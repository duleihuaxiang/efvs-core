package cn.iocoder.yudao.module.ReliabilityDataMetrics.controller.admin.ReliabilityDataMetrics.vo;

import lombok.*;
import java.util.*;
import javax.validation.constraints.*;

@Data
public class ReliabilityDataMetricsSaveReqVO {

    private Integer id;

    private String componentName;

    private String partNumber;

    private String componentCategory;

    private Integer mtbf;

    private Integer mtbur;

    private Integer count;

    private String remark;

}
