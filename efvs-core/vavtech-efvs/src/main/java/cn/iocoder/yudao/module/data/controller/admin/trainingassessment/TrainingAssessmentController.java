package cn.iocoder.yudao.module.data.controller.admin.trainingassessment;

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


import cn.iocoder.yudao.module.data.controller.admin.trainingassessment.vo.*;
import cn.iocoder.yudao.module.data.dal.dataobject.trainingassessment.TrainingAssessmentDO;
import cn.iocoder.yudao.module.data.service.trainingassessment.TrainingAssessmentService;

@RestController
@RequestMapping("/data/training-assessment")
@Validated
public class TrainingAssessmentController {

    @Resource
    private TrainingAssessmentService trainingAssessmentService;

    @PostMapping("/create")
    @PreAuthorize("@ss.hasPermission('data:training-assessment:create')")
    public CommonResult<Integer> createTrainingAssessment(@Valid @RequestBody TrainingAssessmentSaveReqVO createReqVO) {
        return success(trainingAssessmentService.createTrainingAssessment(createReqVO));
    }

    @PutMapping("/update")
    @PreAuthorize("@ss.hasPermission('data:training-assessment:update')")
    public CommonResult<Boolean> updateTrainingAssessment(@Valid @RequestBody TrainingAssessmentSaveReqVO updateReqVO) {
        trainingAssessmentService.updateTrainingAssessment(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @PreAuthorize("@ss.hasPermission('data:training-assessment:delete')")
    public CommonResult<Boolean> deleteTrainingAssessment(@RequestParam("id") Integer id) {
        trainingAssessmentService.deleteTrainingAssessment(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
                @PreAuthorize("@ss.hasPermission('data:training-assessment:delete')")
    public CommonResult<Boolean> deleteTrainingAssessmentList(@RequestParam("ids") List<Integer> ids) {
        trainingAssessmentService.deleteTrainingAssessmentListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('data:training-assessment:query')")
    public CommonResult<TrainingAssessmentRespVO> getTrainingAssessment(@RequestParam("id") Integer id) {
        TrainingAssessmentDO trainingAssessment = trainingAssessmentService.getTrainingAssessment(id);
        return success(BeanUtils.toBean(trainingAssessment, TrainingAssessmentRespVO.class));
    }

    @GetMapping("/page")
    @PreAuthorize("@ss.hasPermission('data:training-assessment:query')")
    public CommonResult<PageResult<TrainingAssessmentRespVO>> getTrainingAssessmentPage(@Valid TrainingAssessmentPageReqVO pageReqVO) {
        PageResult<TrainingAssessmentDO> pageResult = trainingAssessmentService.getTrainingAssessmentPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, TrainingAssessmentRespVO.class));
    }

    @GetMapping("/export-excel")
    @PreAuthorize("@ss.hasPermission('data:training-assessment:export')")
    public void exportTrainingAssessmentExcel(@Valid TrainingAssessmentPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<TrainingAssessmentDO> list = trainingAssessmentService.getTrainingAssessmentPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "培训考核.xls", "数据", TrainingAssessmentRespVO.class,
                        BeanUtils.toBean(list, TrainingAssessmentRespVO.class));
    }

}
