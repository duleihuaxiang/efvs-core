package cn.iocoder.yudao.module.form.controller.admin.externalform;

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


import cn.iocoder.yudao.module.form.controller.admin.externalform.vo.*;
import cn.iocoder.yudao.module.form.dal.dataobject.externalform.ExternalFormDO;
import cn.iocoder.yudao.module.form.service.externalform.ExternalFormService;

@RestController
@RequestMapping("/form/external-form")
@Validated
public class ExternalFormController {

    @Resource
    private ExternalFormService externalFormService;

    @PostMapping("/creatProcessActFormConfig")
    public CommonResult<Long> creatProcessActFormConfig(@Valid @RequestBody ExternalFormSaveReqVO createReqVO) {
        return success(externalFormService.creatProcessActFormConfig(createReqVO));
    }

    @GetMapping("/processActFormConfigByProDefIDAndActID")
    public CommonResult<ExternalFormRespVO> getProcessActFormConfigByProDefIDAndActID(@RequestParam("processId") String processId, @RequestParam("activityId") String activityId) {
        ExternalFormDO externalForm = externalFormService.getExternalForm(processId, activityId);
        return success(BeanUtils.toBean(externalForm, ExternalFormRespVO.class));
    }

    @PutMapping("/update")
    @PreAuthorize("@ss.hasPermission('form:external-form:update')")
    public CommonResult<Boolean> updateExternalForm(@Valid @RequestBody ExternalFormSaveReqVO updateReqVO) {
        externalFormService.updateExternalForm(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @PreAuthorize("@ss.hasPermission('form:external-form:delete')")
    public CommonResult<Boolean> deleteExternalForm(@RequestParam("id") Long id) {
        externalFormService.deleteExternalForm(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
                @PreAuthorize("@ss.hasPermission('form:external-form:delete')")
    public CommonResult<Boolean> deleteExternalFormList(@RequestParam("ids") List<Long> ids) {
        externalFormService.deleteExternalFormListByIds(ids);
        return success(true);
    }

    @GetMapping("/page")
    @PreAuthorize("@ss.hasPermission('form:external-form:query')")
    public CommonResult<PageResult<ExternalFormRespVO>> getExternalFormPage(@Valid ExternalFormPageReqVO pageReqVO) {
        PageResult<ExternalFormDO> pageResult = externalFormService.getExternalFormPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ExternalFormRespVO.class));
    }

    @GetMapping("/export-excel")
    @PreAuthorize("@ss.hasPermission('form:external-form:export')")
    public void exportExternalFormExcel(@Valid ExternalFormPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ExternalFormDO> list = externalFormService.getExternalFormPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "外置表单配置.xls", "数据", ExternalFormRespVO.class,
                        BeanUtils.toBean(list, ExternalFormRespVO.class));
    }

}
