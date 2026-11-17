package cn.iocoder.yudao.module.customerResponse.service.customerserviceissues;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.customerResponse.controller.admin.customerserviceissues.vo.*;
import cn.iocoder.yudao.module.customerResponse.dal.dataobject.customerserviceissues.CustomerServiceIssuesDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 客户服务问题 Service 接口
 *
 * @author admin
 */
public interface CustomerServiceIssuesService {

    /**
     * 创建客户服务问题
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Integer createCustomerServiceIssues(@Valid CustomerServiceIssuesSaveReqVO createReqVO);

    /**
     * 更新客户服务问题
     *
     * @param updateReqVO 更新信息
     */
    void updateCustomerServiceIssues(@Valid CustomerServiceIssuesSaveReqVO updateReqVO);

    /**
     * 删除客户服务问题
     *
     * @param id 编号
     */
    void deleteCustomerServiceIssues(Integer id);

    /**
    * 批量删除客户服务问题
    *
    * @param ids 编号
    */
    void deleteCustomerServiceIssuesListByIds(List<Integer> ids);

    /**
     * 获得客户服务问题
     *
     * @param id 编号
     * @return 客户服务问题
     */
    CustomerServiceIssuesDO getCustomerServiceIssues(Integer id);

    /**
     * 获得客户服务问题分页
     *
     * @param pageReqVO 分页查询
     * @return 客户服务问题分页
     */
    PageResult<CustomerServiceIssuesDO> getCustomerServiceIssuesPage(CustomerServiceIssuesPageReqVO pageReqVO);

    CustomerServiceIssuesRes getProcessInstanceId(String processInstanceId);

    /**
     * 批量查询客户服务问题根据processInstanceIds
     * @param processInstanceIds
     * @return
     */
    List<CustomerServiceIssuesRes> getCustomerServiceIssuesListByprocessInstanceIds(List<String> processInstanceIds);

    String getMaxQuestionNumber(Long tenantId);

    Integer updateStatusByProcessInstanceId(String processInstanceId);

    /**
     * 获取当前年度的客户响应月度统计
     *
     * @return 月度统计数据
     */
    CustomerResponseStatisticsVO getMonthlyStatistics();

    List<HashMap<Object, Object>> selectUsers(List<Integer> userId);
}