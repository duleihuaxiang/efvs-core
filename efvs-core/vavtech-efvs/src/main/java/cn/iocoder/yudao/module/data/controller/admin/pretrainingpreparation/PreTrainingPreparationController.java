package cn.iocoder.yudao.module.data.controller.admin.pretrainingpreparation;

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


import cn.iocoder.yudao.module.data.controller.admin.pretrainingpreparation.vo.*;
import cn.iocoder.yudao.module.data.dal.dataobject.pretrainingpreparation.PreTrainingPreparationDO;
import cn.iocoder.yudao.module.data.service.pretrainingpreparation.PreTrainingPreparationService;

@RestController
@RequestMapping("/data/pre-training-preparation")
@Validated
public class PreTrainingPreparationController {

    @Resource
    private PreTrainingPreparationService preTrainingPreparationService;

    @PostMapping("/create")
    @PreAuthorize("@ss.hasPermission('data:pre-training-preparation:create')")
    public CommonResult<Boolean> createPreTrainingPreparationBatch(@Valid @RequestBody List<PreTrainingPreparationSaveReqVO> createReqVOList) {
        preTrainingPreparationService.createPreTrainingPreparationBatch(createReqVOList);
        return success(true);
    }

    @PutMapping("/update")
    @PreAuthorize("@ss.hasPermission('data:pre-training-preparation:update')")
    public CommonResult<Boolean> updatePreTrainingPreparation(@Valid @RequestBody PreTrainingPreparationSaveReqVO updateReqVO) {
        preTrainingPreparationService.updatePreTrainingPreparation(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @PreAuthorize("@ss.hasPermission('data:pre-training-preparation:delete')")
    public CommonResult<Boolean> deletePreTrainingPreparation(@RequestParam("id") Integer id) {
        preTrainingPreparationService.deletePreTrainingPreparation(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
                @PreAuthorize("@ss.hasPermission('data:pre-training-preparation:delete')")
    public CommonResult<Boolean> deletePreTrainingPreparationList(@RequestParam("ids") List<Integer> ids) {
        preTrainingPreparationService.deletePreTrainingPreparationListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('data:pre-training-preparation:query')")
    public CommonResult<PreTrainingPreparationRespVO> getPreTrainingPreparation(@RequestParam("id") Integer id) {
        PreTrainingPreparationDO preTrainingPreparation = preTrainingPreparationService.getPreTrainingPreparation(id);
        return success(BeanUtils.toBean(preTrainingPreparation, PreTrainingPreparationRespVO.class));
    }

    @GetMapping("/page")
    @PreAuthorize("@ss.hasPermission('data:pre-training-preparation:query')")
    public CommonResult<PageResult<PreTrainingPreparationRespVO>> getPreTrainingPreparationPage(@Valid PreTrainingPreparationPageReqVO pageReqVO) {
        PageResult<PreTrainingPreparationDO> pageResult = preTrainingPreparationService.getPreTrainingPreparationPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, PreTrainingPreparationRespVO.class));
    }

    @GetMapping("/export-excel")
    @PreAuthorize("@ss.hasPermission('data:pre-training-preparation:export')")
    public void exportPreTrainingPreparationExcel(@Valid PreTrainingPreparationPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<PreTrainingPreparationDO> list = preTrainingPreparationService.getPreTrainingPreparationPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "培训前准备.xls", "数据", PreTrainingPreparationRespVO.class,
                        BeanUtils.toBean(list, PreTrainingPreparationRespVO.class));
    }

    @GetMapping("/getPreTrainingPreparation")
    public CommonResult<List<PreTrainingPreparationRespVO>> getPreTrainingPreparationByProcessInstanceId(@RequestParam("processInstanceId") String processInstanceId) {
        List<PreTrainingPreparationDO> preTrainingPreparationByProcessInstanceId = preTrainingPreparationService.getPreTrainingPreparationByProcessInstanceId(processInstanceId);
        return success(BeanUtils.toBean(preTrainingPreparationByProcessInstanceId, PreTrainingPreparationRespVO.class));
    }
}
