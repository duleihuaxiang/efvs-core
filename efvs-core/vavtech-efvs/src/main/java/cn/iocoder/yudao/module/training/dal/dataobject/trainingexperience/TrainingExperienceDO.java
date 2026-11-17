package cn.iocoder.yudao.module.training.dal.dataobject.trainingexperience;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import org.springframework.data.annotation.Transient;

/**
 * 培训经历 DO
 *
 * @author admin
 */
@TableName("efvs_training_experience")
@KeySequence("efvs_training_experience_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingExperienceDO extends BaseDO {

    /**
     * ID
     */
    @TableId
    private Long id;
    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 流程实例ID
     */
    private String processInstanceId;
    /**
     * 培训主题
     */
    private String trainingTheme;
    /**
     * 培训时间
     */
    private LocalDateTime trainingTime;
    /**
     * 培训地点
     */
    private String trainingLocation;
    /**
     * 培训时长
     */
    private Long trainingDuration;
    /**
     * 培训成绩
     */
    private String achieveResults;
    /**
     * 培训教师
     */
    private String trainingTeachers;
    /**
     * 备注
     */
    private String remark;
//    /**
//     * 文件路径
//     */
//    @Transient
//    private String fileUrl;
//    /**
//     * 文件名称
//     */
//    @Transient
//    private String name;
//    /**
//     * 文件ID
//     */
//    @Transient
//    private Integer fileId;
//    /**
//     * 文件大小
//     */
//    @Transient
//    private String size;

    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private List<FileUploadDO> fileList;


}