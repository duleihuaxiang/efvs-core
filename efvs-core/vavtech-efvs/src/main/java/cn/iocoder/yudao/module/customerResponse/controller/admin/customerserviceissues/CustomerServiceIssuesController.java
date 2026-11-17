package cn.iocoder.yudao.module.customerResponse.controller.admin.customerserviceissues;

import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.dict.core.DictFrameworkUtils;


import cn.iocoder.yudao.module.customerResponse.controller.admin.customerserviceissues.vo.*;
import cn.iocoder.yudao.module.customerResponse.dal.dataobject.customerserviceissues.CustomerServiceIssuesDO;
import cn.iocoder.yudao.module.customerResponse.service.customerserviceissues.CustomerServiceIssuesService;

import javax.validation.Valid;

@RestController
@RequestMapping("/customerResponse/customer-service-issues")
@Validated
public class CustomerServiceIssuesController {

    @Resource
    private CustomerServiceIssuesService customerServiceIssuesService;

    @PostMapping("/create")
    @PreAuthorize("@ss.hasPermission('customerResponse:customer-service-issues:create')")
    public CommonResult<Integer> createCustomerServiceIssues(@Valid @RequestBody CustomerServiceIssuesSaveReqVO createReqVO) {
        return success(customerServiceIssuesService.createCustomerServiceIssues(createReqVO));
    }

    @PutMapping("/update")
    @PreAuthorize("@ss.hasPermission('customerResponse:customer-service-issues:update')")
    public CommonResult<Boolean> updateCustomerServiceIssues(@Valid @RequestBody CustomerServiceIssuesSaveReqVO updateReqVO) {
        customerServiceIssuesService.updateCustomerServiceIssues(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @PreAuthorize("@ss.hasPermission('customerResponse:customer-service-issues:delete')")
    public CommonResult<Boolean> deleteCustomerServiceIssues(@RequestParam("id") Integer id) {
        customerServiceIssuesService.deleteCustomerServiceIssues(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
                @PreAuthorize("@ss.hasPermission('customerResponse:customer-service-issues:delete')")
    public CommonResult<Boolean> deleteCustomerServiceIssuesList(@RequestParam("ids") List<Integer> ids) {
        customerServiceIssuesService.deleteCustomerServiceIssuesListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('customerResponse:customer-service-issues:query')")
    public CommonResult<CustomerServiceIssuesRespVO> getCustomerServiceIssues(@RequestParam("id") Integer id) {
        CustomerServiceIssuesDO customerServiceIssues = customerServiceIssuesService.getCustomerServiceIssues(id);
        return success(BeanUtils.toBean(customerServiceIssues, CustomerServiceIssuesRespVO.class));
    }

    @GetMapping("/page")
    @PreAuthorize("@ss.hasPermission('customerResponse:customer-service-issues:query')")
    public CommonResult<PageResult<CustomerServiceIssuesRespVO>> getCustomerServiceIssuesPage(@Valid CustomerServiceIssuesPageReqVO pageReqVO) {
        PageResult<CustomerServiceIssuesDO> pageResult = customerServiceIssuesService.getCustomerServiceIssuesPage(pageReqVO);
        List<Integer> userId = new ArrayList<>();
        for (CustomerServiceIssuesDO customerServiceIssuesDO : pageResult.getList()) {
            userId.add(customerServiceIssuesDO.getRespondent());
        }
        PageResult<CustomerServiceIssuesRespVO> bean = BeanUtils.toBean(pageResult, CustomerServiceIssuesRespVO.class);
        if (CollectionUtils.isEmpty(userId)){
            return success(bean);
        }
        List<HashMap<Object, Object>> userMap = customerServiceIssuesService.selectUsers(userId);
        for (CustomerServiceIssuesRespVO customerServiceIssuesRespVO : bean.getList()) {
            for (HashMap<Object, Object> objectObjectHashMap : userMap) {
                if(customerServiceIssuesRespVO.getRespondent()!= null && Integer.parseInt(objectObjectHashMap.get("id").toString()) == customerServiceIssuesRespVO.getRespondent()){
                    customerServiceIssuesRespVO.setNickName(objectObjectHashMap.get("nickname").toString());
                }
            }
        }
        return success(bean);
    }

    /** 字典类型：客户问题类型 */
    private static final String DICT_TYPE_CUSTOMER_PROBLEM_TYPES = "customer_problem_types";
    /** 字典类型：问题类型 */
    private static final String DICT_TYPE_PROBLEM_TYPES = "problem_types";
    /** 字典类型：客户响应级别（优先级） */
    private static final String DICT_TYPE_CUSTOMER_RESPONSE_LEVEL = "customer_response_level";

    @GetMapping("/export-excel")
    @PreAuthorize("@ss.hasPermission('customerResponse:customer-service-issues:export')")
    public void exportCustomerServiceIssuesExcel(@Valid CustomerServiceIssuesPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<CustomerServiceIssuesDO> list = customerServiceIssuesService.getCustomerServiceIssuesPage(pageReqVO).getList();
        // 收集回复人 ID，用于查询昵称
        List<Integer> userIds = new ArrayList<>();
        for (CustomerServiceIssuesDO item : list) {
            if (item.getRespondent() != null) {
                userIds.add(item.getRespondent());
            }
        }
        List<HashMap<Object, Object>> userMap = new ArrayList<>();
        if (!userIds.isEmpty()) {
            userMap = customerServiceIssuesService.selectUsers(userIds);
        }
        List<CustomerServiceIssuesRespVO> voList = BeanUtils.toBean(list, CustomerServiceIssuesRespVO.class);
        // 从字典表反查优先级、客户问题类型、问题类型的标签，供 Excel 导出显示，并将回复人 ID 转成昵称
        for (CustomerServiceIssuesRespVO vo : voList) {
            if (vo.getRecordDate() != null) {
                vo.setRecordDateLabel(vo.getRecordDate().toLocalDate().toString());
            }
            vo.setPriorityLabel(DictFrameworkUtils.parseDictDataLabel(DICT_TYPE_CUSTOMER_RESPONSE_LEVEL, vo.getPriority()));
            vo.setCustomerProblemTypesLabel(DictFrameworkUtils.parseDictDataLabel(DICT_TYPE_CUSTOMER_PROBLEM_TYPES, vo.getCustomerProblemTypes()));
            vo.setProblemTypesLabel(DictFrameworkUtils.parseDictDataLabel(DICT_TYPE_PROBLEM_TYPES, vo.getProblemTypes()));
            if (vo.getRespondent() != null && !userMap.isEmpty()) {
                for (HashMap<Object, Object> map : userMap) {
                    if (map.get("id") != null &&
                            vo.getRespondent().intValue() == Integer.parseInt(map.get("id").toString())) {
                        vo.setNickName(map.get("nickname") != null ? map.get("nickname").toString() : null);
                        break;
                    }
                }
            }
        }
        ExcelUtils.write(response, "客户服务问题.xlsx", "数据", CustomerServiceIssuesRespVO.class, voList);
    }

    @GetMapping("/getProcessInstanceId")
    public CommonResult<CustomerServiceIssuesRes> getProcessInstanceId( String processInstanceId) throws IOException {
        return success(customerServiceIssuesService.getProcessInstanceId(processInstanceId));
    }

    @GetMapping("/updateStatusByProcessInstanceId")
    public CommonResult<Integer> updateStatusByProcessInstanceId( String processInstanceId) throws IOException {
        return success(customerServiceIssuesService.updateStatusByProcessInstanceId(processInstanceId));
    }

    @GetMapping("/monthly-statistics")
    public CommonResult<CustomerResponseStatisticsVO> getMonthlyStatistics() {
        CustomerResponseStatisticsVO statistics = customerServiceIssuesService.getMonthlyStatistics();
        return success(statistics);
    }

}
