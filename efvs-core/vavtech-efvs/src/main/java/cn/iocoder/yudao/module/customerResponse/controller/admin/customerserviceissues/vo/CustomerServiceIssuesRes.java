package cn.iocoder.yudao.module.customerResponse.controller.admin.customerserviceissues.vo;

import cn.idev.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerServiceIssuesRes {

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
     * 客户名称
     */
    private String customerName;
    /**
     * 客户id
     */
    private String customerId;
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime recordDate;
    /**
     * 问题编号
     */
    private String questionNumber;
    /**
     * 客户要求时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
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
    private List<String> customerAttachments;
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
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
    private List<String> solutionAttachment;
    /**
     * 回复时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
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
    private List<String> replyAttachment;
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
}
