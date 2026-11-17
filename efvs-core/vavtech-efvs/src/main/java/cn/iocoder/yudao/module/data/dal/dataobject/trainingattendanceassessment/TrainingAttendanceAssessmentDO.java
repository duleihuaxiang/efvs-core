package cn.iocoder.yudao.module.data.dal.dataobject.trainingattendanceassessment;

import cn.idev.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.module.data.controller.admin.uploadzippreview.dto.FileUploadResult;
import cn.iocoder.yudao.module.training.dal.dataobject.trainingexperience.FileUploadDO;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 培训签到考核证书 DO
 *
 * @author admin
 */
@TableName("efvs_training_attendance_assessment")
@KeySequence("efvs_training_attendance_assessment_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingAttendanceAssessmentDO extends BaseDO {

    /**
     * ID
     */
    @TableId
    private Integer id;
    /**
     * 流程实例ID
     */
    private String processInstanceId;
    /**
     * 姓名
     */
    private String name;
    /**
     * 课程名称
     */
    private String courseName;
    /**
     * 签到时间
     */
    private String checkInTime;
    /**
     * 学习时长
     */
    private Integer learningTime;
    /**
     * 是否完成学习 0是1否
     */
    private Integer isCompleted;
    /**
     * 完成形式
     */
    private Integer completedForm;
    /**
     * 成绩
     */
    private Integer score;
    /**
     * 获得证书
     */
    private String receiveCertificate;
    /**
     * 教员
     */
    private String instructor;
    /**
     * 完成日期
     */
    private String completionDate;
    /**
     * 备注
     */
    private String remark;
    /**
     * 文件上传ID
     */
    @TableField(value = "file_upload_id")
    private String fileUploadId;
    /**
     * 文件上传结果
     */
    @TableField(exist = false)
    private List<FileUploadDO>  fileUploadResults;
    /**
     * 培训附件
     */
    private String traningAttachments;

    @TableField(exist = false)
    private String trainingTheme;

    @TableField(exist = false)
    private String trainingLocation;

    @TableField(exist = false)
    private String  trainingStartTime;

    @TableField(exist = false)
    private String  trainingEndTime;
    /**
     * 任务ID
     */
    private String taskId;
    /**
     * 流程定义ID
     */
    private String processKey;
}
