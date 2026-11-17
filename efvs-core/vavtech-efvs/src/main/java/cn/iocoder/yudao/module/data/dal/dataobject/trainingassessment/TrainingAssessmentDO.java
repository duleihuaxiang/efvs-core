package cn.iocoder.yudao.module.data.dal.dataobject.trainingassessment;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 培训考核 DO
 *
 * @author admin
 */
@TableName("efvs_training_assessment")
@KeySequence("efvs_training_assessment_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingAssessmentDO extends BaseDO {

    /**
     * ID，主键自增长
     */
    @TableId
    private Integer id;
    /**
     * 编号
     */
    private Integer serialNumber;
    /**
     * 姓名
     */
    private String name;
    /**
     * 课程名称
     */
    private String courseName;
    /**
     * 学习时间
     */
    private Integer learningTime;
    /**
     * 是否完成，0是1否
     */
    private Integer isCompleted;
    /**
     * 完成形式
     */
    private String completedForm;
    /**
     * 成绩
     */
    private String score;
    /**
     * 教员
     */
    private String instructor;
    /**
     * 完成日期
     */
    private LocalDateTime completionDate;
    /**
     * 备注
     */
    private String remark;


}