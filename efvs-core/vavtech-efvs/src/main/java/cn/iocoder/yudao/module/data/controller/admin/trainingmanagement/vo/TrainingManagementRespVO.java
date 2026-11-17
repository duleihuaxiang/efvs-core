package cn.iocoder.yudao.module.data.controller.admin.trainingmanagement.vo;

import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Data
@ExcelIgnoreUnannotated
public class TrainingManagementRespVO {

    @ExcelProperty("ID")
    private Integer id;

    @ExcelProperty("编号ID")
    private Integer serialNumber;

    @ExcelProperty("流程名称")
    private String processName;

    @ExcelProperty("发起时间")
    private LocalDateTime initiationTime;

    @ExcelProperty("发起人")
    private String initiator;

    @ExcelProperty("当前环节")
    private String currentStage;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @ExcelProperty("备注")
    private String remark;

}
