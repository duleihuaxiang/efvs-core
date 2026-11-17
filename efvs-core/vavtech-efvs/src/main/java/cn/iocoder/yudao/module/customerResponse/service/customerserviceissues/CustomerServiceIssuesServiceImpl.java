package cn.iocoder.yudao.module.customerResponse.service.customerserviceissues;

import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import java.util.*;
import cn.iocoder.yudao.module.customerResponse.controller.admin.customerserviceissues.vo.*;
import cn.iocoder.yudao.module.customerResponse.dal.dataobject.customerserviceissues.CustomerServiceIssuesDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.customerResponse.dal.mysql.customerserviceissues.CustomerServiceIssuesMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.data.enums.ErrorCodeConstants.CUSTOMER_SERVICE_ISSUES_NOT_EXISTS;

/**
 * 客户服务问题 Service 实现类
 *
 * @author admin
 */
@Slf4j
@Service
@Validated
public class CustomerServiceIssuesServiceImpl extends ServiceImpl<CustomerServiceIssuesMapper, CustomerServiceIssuesDO> implements CustomerServiceIssuesService {

    @Resource
    private CustomerServiceIssuesMapper customerServiceIssuesMapper;

    @Override
    public Integer createCustomerServiceIssues(CustomerServiceIssuesSaveReqVO createReqVO) {
        // 插入
        CustomerServiceIssuesDO customerServiceIssues = BeanUtils.toBean(createReqVO, CustomerServiceIssuesDO.class);
        customerServiceIssuesMapper.insert(customerServiceIssues);

        // 返回
        return customerServiceIssues.getId();
    }

    @Override
    public void updateCustomerServiceIssues(CustomerServiceIssuesSaveReqVO updateReqVO) {
        // 校验存在
        validateCustomerServiceIssuesExists(updateReqVO.getId());
        // 更新
        CustomerServiceIssuesDO updateObj = BeanUtils.toBean(updateReqVO, CustomerServiceIssuesDO.class);
        customerServiceIssuesMapper.updateById(updateObj);
    }

    @Override
    public void deleteCustomerServiceIssues(Integer id) {
        // 校验存在
        validateCustomerServiceIssuesExists(id);
        // 删除
        customerServiceIssuesMapper.deleteById(id);
    }

    @Override
        public void deleteCustomerServiceIssuesListByIds(List<Integer> ids) {
        // 删除
        customerServiceIssuesMapper.deleteByIds(ids);
        }


    private void validateCustomerServiceIssuesExists(Integer id) {
        if (customerServiceIssuesMapper.selectById(id) == null) {
            throw exception(CUSTOMER_SERVICE_ISSUES_NOT_EXISTS);
        }
    }

    @Override
    public CustomerServiceIssuesDO getCustomerServiceIssues(Integer id) {
        return customerServiceIssuesMapper.selectById(id);
    }

    @Override
    public PageResult<CustomerServiceIssuesDO> getCustomerServiceIssuesPage(CustomerServiceIssuesPageReqVO pageReqVO) {
        return customerServiceIssuesMapper.selectPage(pageReqVO);
    }

    @Override
    public CustomerServiceIssuesRes getProcessInstanceId(String processInstanceId) {
        // 流程实例编号在不同租户之间可能重复，查询时必须同时带上当前租户，避免跨租户读取客户问题。
        Long tenantId = TenantContextHolder.getTenantId();
        CustomerServiceIssuesDO customerServiceIssuesDO = customerServiceIssuesMapper.selectByProcessInstanceId(processInstanceId, tenantId);
        if (customerServiceIssuesDO  == null){
            return null;
        }
        CustomerServiceIssuesRes customerServiceIssuesRes = new CustomerServiceIssuesRes();
        customerServiceIssuesRes.setId(customerServiceIssuesDO.getId());
        customerServiceIssuesRes.setProcessInstanceId(customerServiceIssuesDO.getProcessInstanceId());
        customerServiceIssuesRes.setCustomerName(customerServiceIssuesDO.getCustomerName());
        customerServiceIssuesRes.setCustomerId(customerServiceIssuesDO.getCustomerId());
        customerServiceIssuesRes.setDepartment(customerServiceIssuesDO.getDepartment());
        customerServiceIssuesRes.setContact(customerServiceIssuesDO.getContact());
        customerServiceIssuesRes.setContactNumber(customerServiceIssuesDO.getContactNumber());
        customerServiceIssuesRes.setContactEmail(customerServiceIssuesDO.getContactEmail());
        customerServiceIssuesRes.setRecordDate(customerServiceIssuesDO.getRecordDate());
        customerServiceIssuesRes.setQuestionNumber(customerServiceIssuesDO.getQuestionNumber());
        customerServiceIssuesRes.setCustomerRequestsTime(customerServiceIssuesDO.getCustomerRequestsTime());
        customerServiceIssuesRes.setPriority(customerServiceIssuesDO.getPriority());
        customerServiceIssuesRes.setCustomerProblemDescription(customerServiceIssuesDO.getCustomerProblemDescription());
        customerServiceIssuesRes.setCustomerExpectations(customerServiceIssuesDO.getCustomerExpectations());
        customerServiceIssuesRes.setCustomerAttachments(parseCommaSeparatedList(customerServiceIssuesDO.getCustomerAttachments()));
        customerServiceIssuesRes.setApplicableToCustomerIssues(customerServiceIssuesDO.getApplicableToCustomerIssues());
        customerServiceIssuesRes.setCustomerIssuesIndustryStandards(customerServiceIssuesDO.getCustomerIssuesIndustryStandards());
        customerServiceIssuesRes.setCustomerProblemImpact(customerServiceIssuesDO.getCustomerProblemImpact());
        customerServiceIssuesRes.setCustomerProblemTypes(customerServiceIssuesDO.getCustomerProblemTypes());
        customerServiceIssuesRes.setIsReply(customerServiceIssuesDO.getIsReply());
        customerServiceIssuesRes.setSolutionManager(customerServiceIssuesDO.getSolutionManager());
        customerServiceIssuesRes.setPreparationTime(customerServiceIssuesDO.getPreparationTime());
        customerServiceIssuesRes.setPreparedBy(customerServiceIssuesDO.getPreparedBy());
        customerServiceIssuesRes.setSolutionContent(customerServiceIssuesDO.getSolutionContent());
        customerServiceIssuesRes.setSolutionAttachment(parseCommaSeparatedList(customerServiceIssuesDO.getSolutionAttachment()));
        customerServiceIssuesRes.setResponseTime(customerServiceIssuesDO.getResponseTime());
        customerServiceIssuesRes.setRespondent(customerServiceIssuesDO.getRespondent());
        customerServiceIssuesRes.setReplyContent(customerServiceIssuesDO.getReplyContent());
        customerServiceIssuesRes.setReplyAttachment(parseCommaSeparatedList(customerServiceIssuesDO.getReplyAttachment()));
        customerServiceIssuesRes.setRemark(customerServiceIssuesDO.getRemark());
        customerServiceIssuesRes.setReliabilityDataFormNo(customerServiceIssuesDO.getReliabilityDataFormNo());
        customerServiceIssuesRes.setProblemTypes(customerServiceIssuesDO.getProblemTypes());
        return customerServiceIssuesRes;
    }

    private List<String> parseCommaSeparatedList(String value) {
        // 数据库存储为逗号分隔字符串，接口层转换为列表，避免前端重复处理空值和空白项。
        if (value == null) {
            return Collections.emptyList();
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(trimmed.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    @Override
    public List<CustomerServiceIssuesRes> getCustomerServiceIssuesListByprocessInstanceIds(List<String> processInstanceIds) {
        Long tenantId = TenantContextHolder.getTenantId();
        return customerServiceIssuesMapper.getCustomerServiceIssuesListByprocessInstanceIds(processInstanceIds, tenantId);
    }

    @Override
    public String getMaxQuestionNumber(Long tenantId) {
        return customerServiceIssuesMapper.getMaxQuestionNumber(tenantId);
    }

    @Override
    public Integer updateStatusByProcessInstanceId(String processInstanceId) {
        // 流程结束/响应完成后按流程实例更新状态，而不是依赖页面传入业务主键。
        Long tenantId = TenantContextHolder.getTenantId();
        return customerServiceIssuesMapper.updateStatusByProcessInstanceId(tenantId, processInstanceId);
    }

    @Override
    public CustomerResponseStatisticsVO getMonthlyStatistics() {
        // 获取当前年份
        int currentYear = Year.now().getValue();
        
        // 构建查询条件：当前年度的数据
        LocalDateTime yearStart = LocalDateTime.of(currentYear, 1, 1, 0, 0, 0);
        LocalDateTime yearEnd = LocalDateTime.of(currentYear, 12, 31, 23, 59, 59);
        
        // 查询当前年度的所有数据
        LambdaQueryWrapper<CustomerServiceIssuesDO> queryWrapper = new LambdaQueryWrapper<CustomerServiceIssuesDO>()
                .ge(CustomerServiceIssuesDO::getCreateTime, yearStart)
                .le(CustomerServiceIssuesDO::getCreateTime, yearEnd);
        
        List<CustomerServiceIssuesDO> allData = customerServiceIssuesMapper.selectList(queryWrapper);
        
        log.info("[getMonthlyStatistics] 查询到当前年度数据总数: {}", allData.size());
        
        // 初始化12个月的数据
        List<CustomerResponseStatisticsVO.MonthStatistics> monthStatisticsList = new ArrayList<>();
        List<Integer> months = new ArrayList<>();
        List<Long> totalCounts = new ArrayList<>(); // 总响应数量（该月所有记录数）
        List<Long> unrespondedCounts = new ArrayList<>(); // 未响应数量（status=0）
        
        for (int month = 1; month <= 12; month++) {
            final int currentMonth = month;
            
            // 统计该月的总记录数（所有记录，不管status）
            long totalCount = allData.stream()
                    .filter(data -> {
                        if (data.getCreateTime() == null) return false;
                        int dataMonth = data.getCreateTime().getMonthValue();
                        return dataMonth == currentMonth;
                    })
                    .count();
            
            // 统计该月的未响应数量（status=0）
            long unrespondedCount = allData.stream()
                    .filter(data -> {
                        if (data.getCreateTime() == null) return false;
                        int dataMonth = data.getCreateTime().getMonthValue();
                        return dataMonth == currentMonth && data.getStatus() != null && data.getStatus() == 0;
                    })
                    .count();
            
            CustomerResponseStatisticsVO.MonthStatistics monthStats = new CustomerResponseStatisticsVO.MonthStatistics();
            monthStats.setMonth(month);
            monthStats.setRespondedCount(totalCount - unrespondedCount); // 已响应数 = 总数 - 未响应数
            monthStats.setUnrespondedCount(unrespondedCount);
            monthStats.setTotalCount(totalCount); // 总响应数 = 该月所有记录数
            
            monthStatisticsList.add(monthStats);
            months.add(month);
            totalCounts.add(totalCount); // 总响应数量
            unrespondedCounts.add(unrespondedCount); // 未响应数量
            
            log.info("[getMonthlyStatistics] 月份: {}, 总记录数: {}, 未响应数(status=0): {}, 已响应数: {}", 
                    month, totalCount, unrespondedCount, totalCount - unrespondedCount);
        }
        
        CustomerResponseStatisticsVO result = new CustomerResponseStatisticsVO();
        result.setMonths(months);
        result.setRespondedCounts(totalCounts); // 总响应数量（该月所有记录数）
        result.setUnrespondedCounts(unrespondedCounts); // 未响应数量（status=0）
        result.setMonthStatistics(monthStatisticsList);
        
        return result;
    }

    // 原方法
public List<HashMap<Object, Object>> selectUsers(List<Integer> userIds) {
    Long tenantId = TenantContextHolder.getTenantId();
    return customerServiceIssuesMapper.selectUsers(tenantId, userIds);
}


}
