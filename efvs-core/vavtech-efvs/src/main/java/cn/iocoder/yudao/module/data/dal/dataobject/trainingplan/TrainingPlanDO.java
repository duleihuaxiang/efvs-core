package cn.iocoder.yudao.module.data.dal.dataobject.trainingplan;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 培训计划 DO
 *
 * @author admin
 */
@TableName("efvs_training_plan")
@KeySequence("efvs_training_plan_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingPlanDO extends BaseDO {

    /**
     * ID
     */
    @TableId
    private Integer id;
    /**
     * 培训主题
     */
    private String trainingTheme;
    /**
     * 流程实例ID
     */
    private String processInstanceId;
    /**
     * 培训计划编号
     */
    private String annualPlanProjectNumberId;
    /**
     * 培训项目/课程
     */
    private String trainingProgramsCourses;
    /**
     * 培训对象
     */
    private String trainingTargets;
    /**
     * 培训人数
     */
    private Integer numberOfTrainees;
    /**
     * 培训时间
     */
    private String  trainingStartTime;
    /**
     * 培训结束时间
     */
    private String  trainingEndTime;
    /**
     * 培训地点
     */
    private String trainingLocation;
    /**
     * 项目负责人
     */
    private Integer projectLeader;
    /**
     * 备注
     */
    private String remark;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    /**
     * 创建人
     */
    private String createName;
    /**
     * 项目负责人昵称
     */
    @TableField(exist = false)
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
