package cn.iocoder.yudao.module.data.dal.dataobject.pretrainingpreparation;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 培训前准备 DO
 *
 * @author admin
 */
@TableName("efvs_pre_training_preparation")
@KeySequence("efvs_pre_training_preparation_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreTrainingPreparationDO extends BaseDO {

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
     * 编号
     */
    private Integer serialNumber;
    /**
     * 培训前事项
     */
    private String preTrainingMatters;
    /**
     * 是否完成 (0 否 1 是)
     */
    private Integer isCompleted;
    /**
     * 备注
     */
    private String remark;
    /**
     * 培训前准备问题ID
     */
    private Integer ppqlId;
    /**
     * 任务ID
     */
    private String taskId;
    /**
     * 流程定义ID
     */
    private String processKey;
}