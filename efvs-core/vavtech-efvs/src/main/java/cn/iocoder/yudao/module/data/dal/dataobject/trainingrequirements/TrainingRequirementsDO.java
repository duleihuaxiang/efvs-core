package cn.iocoder.yudao.module.data.dal.dataobject.trainingrequirements;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 培训需求 DO
 *
 * @author admin
 */
@TableName("efvs_training_requirements")
@KeySequence("efvs_training_requirements_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingRequirementsDO extends BaseDO {

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
     * 客户名称
     */
    private String customerName;
    /**
     * 培训内容
     */
    private String trainingContent;
    /**
     * 培训类型
     */
    private String trainingType;
    /**
     * 培训对象
     */
    private String trainingTarget;
    /**
     * 培训时间
     */
    private LocalDateTime trainingTime;
    /**
     * 备注
     */
    private String remark;


}