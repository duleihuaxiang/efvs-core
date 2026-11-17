package cn.iocoder.yudao.module.customerResponse.dal.mysql.customerserviceissues;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.customerResponse.dal.dataobject.customerserviceissues.CustomerServiceIssuesDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.customerResponse.controller.admin.customerserviceissues.vo.*;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;

/**
 * 客户服务问题 Mapper
 *
 * @author admin
 */
@Mapper
public interface CustomerServiceIssuesMapper extends BaseMapperX<CustomerServiceIssuesDO> {

    default PageResult<CustomerServiceIssuesDO> selectPage(CustomerServiceIssuesPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CustomerServiceIssuesDO>()
                .betweenIfPresent(CustomerServiceIssuesDO::getRecordDate, reqVO.getRecordDate())
                .eqIfPresent(CustomerServiceIssuesDO::getQuestionNumber, reqVO.getQuestionNumber())
                .eqIfPresent(CustomerServiceIssuesDO::getPriority, reqVO.getPriority())
                .eqIfPresent(CustomerServiceIssuesDO::getCustomerProblemDescription, reqVO.getCustomerProblemDescription())
                .eqIfPresent(CustomerServiceIssuesDO::getCustomerProblemImpact, reqVO.getCustomerProblemImpact())
                .eqIfPresent(CustomerServiceIssuesDO::getReplyContent, reqVO.getReplyContent())
                .orderByDesc(CustomerServiceIssuesDO::getId));
    }

    CustomerServiceIssuesDO selectByProcessInstanceId(@Param("processInstanceId") String processInstanceId,@Param("tenantId") Long tenantId);

    List<CustomerServiceIssuesRes> getCustomerServiceIssuesListByprocessInstanceIds(@Param("processInstanceIds") List<String> processInstanceIds,@Param("tenantId") Long tenantId);

    String getMaxQuestionNumber(@Param("tenantId") Long tenantId);

    Integer updateStatusByProcessInstanceId(@Param("tenantId") Long tenantId, @Param("processInstanceId") String processInstanceId);

    List<HashMap<Object, Object>> selectUsers(@Param("tenantId") Long tenantId, @Param("userIds") List<Integer> userIds);
}