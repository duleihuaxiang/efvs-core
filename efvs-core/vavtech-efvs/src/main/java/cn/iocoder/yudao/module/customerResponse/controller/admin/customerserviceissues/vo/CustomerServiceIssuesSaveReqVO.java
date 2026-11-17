package cn.iocoder.yudao.module.customerResponse.controller.admin.customerserviceissues.vo;

import lombok.*;
import java.util.*;
import javax.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Data
public class CustomerServiceIssuesSaveReqVO {

    private Integer id;

    private String processInstanceId;

    private String customerName;

    private String department;

    private String contact;

    private String contactNumber;

    private String contactEmail;

    private LocalDateTime recordDate;

    private String questionNumber;

    private LocalDateTime customerRequestsTime;

    private Integer priority;

    private String customerProblemDescription;

    private String customerExpectations;

    private String customerAttachments;

    private String applicableToCustomerIssues;

    private String customerIssuesIndustryStandards;

    private String customerProblemImpact;

    private Integer customerProblemTypes;

    private Integer isReply;

    private Integer solutionManager;

    private LocalDateTime preparationTime;

    private String preparedBy;

    private String solutionContent;

    private String solutionAttachment;

    private LocalDateTime responseTime;

    private Integer respondent;

    private String replyContent;

    private String replyAttachment;

    private String remark;

}
