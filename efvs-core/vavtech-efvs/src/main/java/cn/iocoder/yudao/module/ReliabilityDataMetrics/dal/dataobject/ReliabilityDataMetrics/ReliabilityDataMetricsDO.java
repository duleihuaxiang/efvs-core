package cn.iocoder.yudao.module.ReliabilityDataMetrics.dal.dataobject.ReliabilityDataMetrics;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 可靠性指标 DO
 *
 * @author admin
 */
@TableName("efvs_reliability_data_metrics")
@KeySequence("efvs_reliability_data_metrics_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReliabilityDataMetricsDO extends BaseDO {

    /**
     * id
     */
    @TableId
    private Integer id;
    /**
     * 部件名称
     */
    private String componentName;
    /**
     * 件号
     */
    private String partNumber;
    /**
     * 部件类别
     */
    private String componentCategory;
    /**
     * MTBUR指标
     */
    private Integer mtbf;
    /**
     * MTBF指标
     */
    private Integer mtbur;
    /**
     * 数量
     */
    private Integer count;
    /**
     * 备注
     */
    private String remark;


}