package cn.iocoder.yudao.module.data.controller.admin;

import cn.iocoder.yudao.module.data.controller.admin.vo.FaultInformationPageReqVO;
import cn.iocoder.yudao.module.data.controller.admin.vo.FaultInformationRespVO;
import cn.iocoder.yudao.module.data.controller.admin.vo.FaultInformationSaveReqVO;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;

import javax.validation.constraints.*;
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


import cn.iocoder.yudao.module.data.dal.dataobject.faultinformation.FaultInformationDO;
import cn.iocoder.yudao.module.data.service.faultinformation.FaultInformationService;

@RestController
@RequestMapping("/data/fault-information")
@Validated
public class FaultInformationController {

    @Resource
    private FaultInformationService faultInformationService;

    @PostMapping("/create")
    @PreAuthorize("@ss.hasPermission('data:fault-information:create')")
    public CommonResult<Integer> createFaultInformation(@Valid @RequestBody FaultInformationSaveReqVO createReqVO) {
        return success(faultInformationService.createFaultInformation(createReqVO));
    }

    @PutMapping("/update")
    @PreAuthorize("@ss.hasPermission('data:fault-information:update')")
    public CommonResult<Boolean> updateFaultInformation(@Valid @RequestBody FaultInformationSaveReqVO updateReqVO) {
        faultInformationService.updateFaultInformation(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @PreAuthorize("@ss.hasPermission('data:fault-information:delete')")
    public CommonResult<Boolean> deleteFaultInformation(@RequestParam("id") Integer id) {
        faultInformationService.deleteFaultInformation(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
                @PreAuthorize("@ss.hasPermission('data:fault-information:delete')")
    public CommonResult<Boolean> deleteFaultInformationList(@RequestParam("ids") List<Integer> ids) {
        faultInformationService.deleteFaultInformationListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('data:fault-information:query')")
    public CommonResult<List<FaultInformationRespVO>> getFaultInformation(@RequestParam("ids") List<Integer> ids) {
        List<FaultInformationDO> faultInformationList = faultInformationService.getFaultInformationList(ids);
        return success(BeanUtils.toBean(faultInformationList, FaultInformationRespVO.class));
    }

    @GetMapping("/page")
    @PreAuthorize("@ss.hasPermission('data:fault-information:query')")
    public CommonResult<PageResult<FaultInformationRespVO>> getFaultInformationPage(@Valid FaultInformationPageReqVO pageReqVO) {
        PageResult<FaultInformationDO> pageResult = faultInformationService.getFaultInformationPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, FaultInformationRespVO.class));
    }

    @GetMapping("/export-excel")
    @PreAuthorize("@ss.hasPermission('data:fault-information:export')")
    public void exportFaultInformationExcel(@Valid FaultInformationPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<FaultInformationDO> list = faultInformationService.getFaultInformationPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "故障信息.xls", "数据", FaultInformationRespVO.class,
                        BeanUtils.toBean(list, FaultInformationRespVO.class));
    }

    @GetMapping("/getAllFaultInformation")
    public CommonResult<List<FaultInformationDO>> getAllFaultInformation() {
        List<FaultInformationDO> FaultInformationDOs = faultInformationService.selectAllFaultInformation();
        return success(FaultInformationDOs);
    }

}
