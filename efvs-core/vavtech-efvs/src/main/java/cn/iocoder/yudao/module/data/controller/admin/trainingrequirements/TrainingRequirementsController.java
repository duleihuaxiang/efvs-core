package cn.iocoder.yudao.module.data.controller.admin.trainingrequirements;

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


import cn.iocoder.yudao.module.data.controller.admin.trainingrequirements.vo.*;
import cn.iocoder.yudao.module.data.dal.dataobject.trainingrequirements.TrainingRequirementsDO;
import cn.iocoder.yudao.module.data.service.trainingrequirements.TrainingRequirementsService;

@RestController
@RequestMapping("/data/training-requirements")
@Validated
public class TrainingRequirementsController {

    @Resource
    private TrainingRequirementsService trainingRequirementsService;

    @PostMapping("/create")
    @PreAuthorize("@ss.hasPermission('data:training-requirements:create')")
    public CommonResult<Integer> createTrainingRequirements(@Valid @RequestBody TrainingRequirementsSaveReqVO createReqVO) {
        return success(trainingRequirementsService.createTrainingRequirements(createReqVO));
    }

    @PutMapping("/update")
    @PreAuthorize("@ss.hasPermission('data:training-requirements:update')")
    public CommonResult<Boolean> updateTrainingRequirements(@Valid @RequestBody TrainingRequirementsSaveReqVO updateReqVO) {
        trainingRequirementsService.updateTrainingRequirements(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @PreAuthorize("@ss.hasPermission('data:training-requirements:delete')")
    public CommonResult<Boolean> deleteTrainingRequirements(@RequestParam("id") Integer id) {
        trainingRequirementsService.deleteTrainingRequirements(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
                @PreAuthorize("@ss.hasPermission('data:training-requirements:delete')")
    public CommonResult<Boolean> deleteTrainingRequirementsList(@RequestParam("ids") List<Integer> ids) {
        trainingRequirementsService.deleteTrainingRequirementsListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('data:training-requirements:query')")
    public CommonResult<TrainingRequirementsRespVO> getTrainingRequirements(@RequestParam("id") Integer id) {
        TrainingRequirementsDO trainingRequirements = trainingRequirementsService.getTrainingRequirements(id);
        return success(BeanUtils.toBean(trainingRequirements, TrainingRequirementsRespVO.class));
    }

    @GetMapping("/page")
    @PreAuthorize("@ss.hasPermission('data:training-requirements:query')")
    public CommonResult<PageResult<TrainingRequirementsRespVO>> getTrainingRequirementsPage(@Valid TrainingRequirementsPageReqVO pageReqVO) {
        PageResult<TrainingRequirementsDO> pageResult = trainingRequirementsService.getTrainingRequirementsPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, TrainingRequirementsRespVO.class));
    }

    @GetMapping("/export-excel")
    @PreAuthorize("@ss.hasPermission('data:training-requirements:export')")
    public void exportTrainingRequirementsExcel(@Valid TrainingRequirementsPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<TrainingRequirementsDO> list = trainingRequirementsService.getTrainingRequirementsPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "培训需求.xls", "数据", TrainingRequirementsRespVO.class,
                        BeanUtils.toBean(list, TrainingRequirementsRespVO.class));
    }

}
