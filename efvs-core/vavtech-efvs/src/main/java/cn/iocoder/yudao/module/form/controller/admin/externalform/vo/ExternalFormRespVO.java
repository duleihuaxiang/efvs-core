package cn.iocoder.yudao.module.form.controller.admin.externalform.vo;

import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Data
@ExcelIgnoreUnannotated
public class ExternalFormRespVO {

    @ExcelProperty("主键ID（自增）")
    private Long id;

    @ExcelProperty("流程ID（关联流程表主键/流程定义ID）")
    private String processId;

    @ExcelProperty("活动ID（流程节点/活动ID）")
    private String activityId;

    @ExcelProperty("表单类型（内置表单或外置表单）")
    private Integer formType;

    @ExcelProperty("表单地址（完整URL，支持HTTP/HTTPS）")
    private String formUrl;

    @ExcelProperty("内置表单")
    private Integer formKey;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @ExcelProperty("描述")
    private String remark;

}
