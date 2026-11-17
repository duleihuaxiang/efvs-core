package cn.iocoder.yudao.module.customerResponse.controller.admin.customerserviceissues.vo;

import lombok.Data;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.ExcelIgnore;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;

@Data
@ExcelIgnoreUnannotated
public class CustomerServiceIssuesRespVO {

    @ExcelProperty("客户名称")
    private String customerName;

    @ExcelProperty("客户ID")
    private String customerId;

    @ExcelProperty("所属部门")
    private String department;

    @ExcelProperty("联系人")
    private String contact;

    @ExcelProperty("联系电话")
    private String contactNumber;

    @ExcelProperty("联系人邮箱")
    private String contactEmail;

    @ExcelProperty("问题编号")
    private String questionNumber;

    @ExcelProperty("客户要求时间")
    private LocalDateTime customerRequestsTime;

    @ExcelIgnore
    private Integer priority;

    @ExcelProperty("优先级")
    private String priorityLabel;

    @ExcelProperty("客户问题描述")
    private String customerProblemDescription;

    @ExcelProperty("客户期望")
    private String customerExpectations;

//    @Schema(description = "客户问题适用性")
//    @ExcelProperty("客户问题适用性")
//    private String applicableToCustomerIssues;
//
//    @Schema(description = "客户问题行业标准")
//    @ExcelProperty("客户问题行业标准")
//    private String customerIssuesIndustryStandards;
//
//    @Schema(description = "客户问题影响")
//    @ExcelProperty("客户问题影响")
//    private String customerProblemImpact;

    @ExcelIgnore
    private Integer customerProblemTypes;

    @ExcelProperty("客户问题类型")
    private String customerProblemTypesLabel;

    @ExcelProperty("回复时间")
    private LocalDateTime responseTime;

    @ExcelIgnore
    private Integer respondent;

    @ExcelProperty("回复内容")
    private String replyContent;

    @ExcelProperty("可靠性数据编号")
    private String reliabilityDataFormNo;

    @ExcelIgnore
    private Integer problemTypes;

    @ExcelProperty("问题类型")
    private String problemTypesLabel;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @ExcelProperty("修改时间")
    private LocalDateTime updateTime;

    @ExcelProperty("回复人")
    private String nickName;

    @ExcelProperty("流程key")
    private String processKey;

    @ExcelProperty("任务id")
    private String taskId;

    @ExcelProperty("流程实例id")
    private String processInstanceId;

    /**
     * 记录日期
     */
    @ExcelIgnore
    private LocalDateTime recordDate;

    @ExcelProperty("记录日期")
    private String recordDateLabel;

}
