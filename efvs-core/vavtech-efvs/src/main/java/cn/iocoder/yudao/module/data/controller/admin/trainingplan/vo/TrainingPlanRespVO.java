package cn.iocoder.yudao.module.data.controller.admin.trainingplan.vo;

import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Data
@ExcelIgnoreUnannotated
public class TrainingPlanRespVO {

    private Integer id;

    private String processInstanceId;

    @ExcelProperty("培训主题")
    private String trainingTheme;

    @ExcelProperty("培训计划编号")
    private String annualPlanProjectNumberId;

    @ExcelProperty("培训课程")
    private String trainingProgramsCourses;

    @ExcelProperty("培训对象")
    private String trainingTargets;

    @ExcelProperty("培训人数")
    private Integer numberOfTrainees;

    @ExcelProperty("培训开始时间")
    private String  trainingStartTime;

    @ExcelProperty("培训结束时间")
    private String  trainingEndTime;

    @ExcelProperty("培训地点")
    private String trainingLocation;

    private Integer projectLeader;

    @ExcelProperty("项目负责人")
    private String efvsProjectLeader;

    private LocalDateTime createTime;

    @ExcelProperty("备注")
    private String remark;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    /**
     * 创建人
     */
    private String createName;

    private String projectLeaderNickName;

    /**
     * 任务ID
     */
    private String taskId;
    /**
     * 流程定义ID
     */
    private String processKey;
}
