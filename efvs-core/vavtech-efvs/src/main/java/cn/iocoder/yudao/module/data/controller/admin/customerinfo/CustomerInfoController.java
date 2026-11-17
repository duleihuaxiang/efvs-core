package cn.iocoder.yudao.module.data.controller.admin.customerinfo;

import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;

import javax.validation.*;
import javax.servlet.http.*;
import java.util.*;
import java.io.IOException;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;


import cn.iocoder.yudao.module.data.controller.admin.customerinfo.vo.*;
import cn.iocoder.yudao.module.data.dal.dataobject.customerinfo.CustomerInfoDO;
import cn.iocoder.yudao.module.data.service.customerinfo.CustomerInfoService;

@RestController
@RequestMapping("/data/customer-info")
@Validated
public class CustomerInfoController {

    @Resource
    private CustomerInfoService customerInfoService;

    @PostMapping("/createCustomerInfo")
    @PreAuthorize("@ss.hasPermission('data:customer-info:create')")
    public CommonResult<Integer> createCustomerInfo(@Valid @RequestBody CustomerInfoSaveReqVO createReqVO) {
        return success(customerInfoService.createCustomerInfo(createReqVO));
    }

    @PostMapping("/updateCustomerInfo")
    @PreAuthorize("@ss.hasPermission('data:customer-info:update')")
    public CommonResult<Boolean> updateCustomerInfo(@Valid @RequestBody CustomerInfoSaveReqVO updateReqVO) {
        customerInfoService.updateCustomerInfo(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/deleteCustomerInfoById")
    @PreAuthorize("@ss.hasPermission('data:customer-info:delete')")
    public CommonResult<Boolean> deleteCustomerInfoById(@RequestParam("id") Integer id) {
        customerInfoService.deleteCustomerInfoById(id);
        return success(true);
    }

    @DeleteMapping("/deleteCustomerInfoByIds")
    @PreAuthorize("@ss.hasPermission('data:customer-info:delete')")
    public CommonResult<Boolean> deleteCustomerInfoByIds(@RequestParam("ids") List<Integer> ids) {
        customerInfoService.deleteCustomerInfoByIds(ids);
        return success(true);
    }

    @GetMapping("/getCustomerInfoById")
    @PreAuthorize("@ss.hasPermission('data:customer-info:query')")
    public CommonResult<CustomerInfoRespVO> getCustomerInfoById(@RequestParam("id") Integer id) {
        CustomerInfoDO customerInfo = customerInfoService.getCustomerInfoById(id);
        return success(BeanUtils.toBean(customerInfo, CustomerInfoRespVO.class));
    }

    @GetMapping("/getCustomerInfoPage")
    @PreAuthorize("@ss.hasPermission('data:customer-info:query')")
    public CommonResult<PageResult<CustomerInfoRespVO>> getCustomerInfoPage(@Valid CustomerInfoPageReqVO pageReqVO) {
        PageResult<CustomerInfoDO> pageResult = customerInfoService.getCustomerInfoPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, CustomerInfoRespVO.class));
    }

    @GetMapping("/exportCustomerInfoExcel")
    @PreAuthorize("@ss.hasPermission('data:customer-info:export')")
    public void exportCustomerInfoExcel(@Valid CustomerInfoPageReqVO pageReqVO,
                                        HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<CustomerInfoDO> list = customerInfoService.getCustomerInfoPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "客户信息.xls", "数据", CustomerInfoRespVO.class,
                BeanUtils.toBean(list, CustomerInfoRespVO.class));
    }

    @GetMapping("/getCustomerInfoAll")
    public CommonResult<List<CustomerInfoRespVO>> getCustomerInfoAll() {
        List<CustomerInfoDO> customerInfos = customerInfoService.getCustomerInfoAll();
        return success(BeanUtils.toBean(customerInfos, CustomerInfoRespVO.class));
    }

    @GetMapping("/provinceCustomerCount")
    public CommonResult<List<ProvinceCustomerCountVO>> statProvinceCustomerCount() {
        List<ProvinceCustomerCountVO> result = customerInfoService.statProvinceCustomerCount();
        return CommonResult.success(result);
    }

}
