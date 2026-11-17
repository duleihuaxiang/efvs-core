package cn.iocoder.yudao.module.customerResponse.dal.dataobject.customerserviceissues;

import cn.hutool.core.date.DateTime;
import cn.iocoder.yudao.framework.common.util.json.databind.TimestampLocalDateTimeDeserializer;
import cn.iocoder.yudao.framework.common.util.json.databind.TimestampLocalDateTimeSerializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 客户服务问题 DO
 *
 * @author admin
 */
@TableName("efvs_customer_service_issues")
@KeySequence("efvs_customer_service_issues_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerServiceIssuesDO extends BaseDO {

    /**
     * 客服服务问题表id
     */
    @TableId
    private Integer id;
    /**
     * 流程实例id
     */
    private String processInstanceId;
    /**
     * 当前任务id
     */
    private String taskId;
    /**
     * 任务名称
     */
    private String processKey;
    /**
     * 客户id
     */
    private String customerId;
    /**
     * 客户名称
     */
    private String customerName;
    /**
     * 所属部门
     */
    private String department;
    /**
     * 联系人
     */
    private String contact;
    /**
     * 联系电话
     */
    private String contactNumber;
    /**
     * 联系人邮箱
     */
    private String contactEmail;
    /**
     * 记录日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime recordDate;
    /**
     * 问题编号
     */
    private String questionNumber;
    /**
     * 客户要求时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime customerRequestsTime;
    /**
     * 优先级
     */
    private Integer priority;
    /**
     * 客户问题描述
     */
    private String customerProblemDescription;
    /**
     * 客户期望
     */
    private String customerExpectations;
    /**
     * 客户附件
     */
    private String customerAttachments;
    /**
     * 客户问题适用性
     */
    private String applicableToCustomerIssues;
    /**
     * 客户问题行业标准
     */
    private String customerIssuesIndustryStandards;
    /**
     * 客户问题影响
     */
    private String customerProblemImpact;
    /**
     * 客户问题类型
     */
    private Integer customerProblemTypes;
    /**
     * 是否可以直接答复(0是1否)
     */
    private Integer isReply;
    /**
     * 解决方案负责人
     */
    private Integer solutionManager;
    /**
     * 编制时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime preparationTime;
    /**
     * 编制人
     */
    private Integer preparedBy;
    /**
     * 解决方案内容
     */
    private String solutionContent;
    /**
     * 解决方案的附件
     */
    private String solutionAttachment;
    /**
     * 回复时间
     */

    private LocalDateTime responseTime;
    /**
     * 回复人
     */
    private Integer respondent;
    /**
     * 回复内容
     */
    private String replyContent;
    /**
     * 回复附件
     */
    private String replyAttachment;
    /**
     * 可靠性数据编号
     */
    private String reliabilityDataFormNo;
    /**
     * 问题类型
     */
    private Integer problemTypes;
    /**
     * 备注
     */
    private String remark;

    /**
     * 状态：0-未结束（未响应），1-结束（已响应）
     */
    private Integer status;

}