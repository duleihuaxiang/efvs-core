package cn.iocoder.yudao.module.reliability.dal.dataobject.reliabilitydata;

import lombok.*;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 可靠性数据收集 DO
 *
 * @author admin
 */
@TableName("efvs_reliability_data")
@KeySequence("efvs_reliability_data_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReliabilityDataDO extends BaseDO {

    /**
     * 主键，自增ID
     */
    @TableId
    private Integer id;
    /**
     * 流程实例ID
     */
    private String processInstanceId;
    /**
     * 任务编号
     */
    private String taskId;
    /**
     * 审批状态：0-待审批；1-通过；2-不通过
     */
    private Integer approvalStatus;
    /**
     * 可靠性数据收集表单编号
     */
    private String reliabilityDataFormNo;
    /**
     * 客户id
     */
    private String customerId;
    /**
     * 客户名称
     */
    private String customerName;
    /**
     * 部件名称
     */
    private String partName;
    /**
     * 代号（件号）
     */
    private String partNumber;
    /**
     * SRU名称
     */
    private String sruName;
    /**
     * SRU代号
     */
    private String sruNumber;
    /**
     * 序列号
     */
    private String serialNumber;
    /**
     * 所属部件代号（所属部件件号）
     */
    private String productPartNumber;
    /**
     * 机型
     */
    private String model;
    /**
     * 系列
     */
    private String series;
    /**
     * 机号
     */
    private String machineNumber;
    /**
     * 报告开始日期
     */
    private LocalDateTime reportStartDate;
    /**
     * 报告结束日期
     */
    private LocalDateTime reportEndDate;
    /**
     * 拆卸日期
     */
    private LocalDateTime removalDate;
    /**
     * 装机日期
     */
    private LocalDateTime installationDate;
    /**
     * 故障报告编码
     */
    private String failureReportNumber;
    /**
     * 是否是产品问题：0=否，1=是
     */
    private Integer isProductIssue;
    /**
     * 拆卸类型：S-计划拆换；U-非计划拆换
     */
    private String removalType;
    /**
     * 拆换原因
     */
    private String removalCause;
    /**
     * 故障描述
     */
    private String failureDescription;
    /**
     * 拆卸部件件号
     */
    private String removedPartNumber;
    /**
     * 版本
     */
    private String version;
    /**
     * 拆卸部件序列号
     */
    private String removedPartSerialNumber;
    /**
     * 备注
     */
    private String remark;
    /**
     * 租户id
     */
    private Integer tenantId;

    /**
     * 流程定义键
     */
    private String processKey;

    /**
     * 层级
     */
    private Integer level;

    @TableField(exist = false)
    private Integer issueCount;

}