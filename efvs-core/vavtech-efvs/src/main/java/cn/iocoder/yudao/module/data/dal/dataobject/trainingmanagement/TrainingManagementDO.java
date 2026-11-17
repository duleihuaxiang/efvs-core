package cn.iocoder.yudao.module.data.dal.dataobject.trainingmanagement;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 培训管理 DO
 *
 * @author admin
 */
@TableName("efvs_training_management")
@KeySequence("efvs_training_management_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingManagementDO extends BaseDO {

    /**
     * ID
     */
    @TableId
    private Integer id;
    /**
     * 编号ID
     */
    private Integer serialNumber;
    /**
     * 流程名称
     */
    private String processName;
    /**
     * 发起时间
     */
    private LocalDateTime initiationTime;
    /**
     * 发起人
     */
    private String initiator;
    /**
     * 当前环节
     */
    private String currentStage;
    /**
     * 备注
     */
    private String remark;


}