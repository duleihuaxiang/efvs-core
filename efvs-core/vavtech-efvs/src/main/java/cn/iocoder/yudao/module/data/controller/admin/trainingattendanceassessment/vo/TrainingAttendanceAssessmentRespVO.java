package cn.iocoder.yudao.module.data.controller.admin.trainingattendanceassessment.vo;

import cn.iocoder.yudao.module.data.controller.admin.uploadzippreview.dto.FileUploadResult;
import cn.iocoder.yudao.module.training.dal.dataobject.trainingexperience.FileUploadDO;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Data
@ExcelIgnoreUnannotated
public class TrainingAttendanceAssessmentRespVO {

    private Integer id;
    /**
     * 流程实例ID
     */
    private String processInstanceId;

    @ExcelProperty("姓名")
    private String name;

    @ExcelProperty("培训主题")
    private String trainingTheme;

    @ExcelProperty("培训地点")
    private String trainingLocation;

    @ExcelProperty("培训开始时间")
    private String  trainingStartTime;

    @ExcelProperty("培训结束时间")
    private String  trainingEndTime;

    private String courseName;

    @ExcelProperty("签到时间")
    private String checkInTime;

    private Integer learningTime;

    private Integer isCompleted;

    // 新增用于导出的中文字段
    @ExcelProperty("是否完成学习") // 将 Excel 列名指向这个新字段
    private String isCompletedDesc;

    private Integer completedForm;

    // 新增用于导出的中文字段
    @ExcelProperty("完成形式") // 将 Excel 列名指向这个新字段
    private String completedFormDesc;

    @ExcelProperty("成绩")
    private Integer score;

    @ExcelProperty("获得证书")
    private String receiveCertificate;

    private String instructor;

    @ExcelProperty("完成日期")
    private String completionDate;

    @ExcelProperty("备注")
    private String remark;

    private String fileUploadId;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<FileUploadDO>  fileUploadResults;

    private List<String> traningAttachments;

    /**
     * 任务ID
     */
    private String taskId;
    /**
     * 流程定义ID
     */
    private String processKey;
}
