package cn.iocoder.yudao.module.training.controller.admin.trainingexperience;

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


import cn.iocoder.yudao.module.training.controller.admin.trainingexperience.vo.*;
import cn.iocoder.yudao.module.training.dal.dataobject.trainingexperience.TrainingExperienceDO;
import cn.iocoder.yudao.module.training.service.trainingexperience.TrainingExperienceService;

@RestController
@RequestMapping("/training/experience")
@Validated
public class TrainingExperienceController {

    @Resource
    private TrainingExperienceService experienceService;

    @PostMapping("/create")
    @PreAuthorize("@ss.hasPermission('training:experience:create')")
    public CommonResult<List<Long>>createExperience(@Valid @RequestBody TrainingExperienceSaveReqVO createReqVO) {
        List<Long> id = experienceService.createExperience(createReqVO, createReqVO.getUserId());
        return success(id);
    }

    @PostMapping("/update")
    @PreAuthorize("@ss.hasPermission('training:experience:update')")
    public CommonResult<Boolean> updateExperience(@Valid @RequestBody TrainingExperienceSaveReqVO updateReqVO) {
        experienceService.updateExperience(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @PreAuthorize("@ss.hasPermission('training:experience:delete')")
    public CommonResult<Boolean> deleteExperienceById(@RequestParam("id") Integer id) {
        experienceService.deleteExperience(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
                @PreAuthorize("@ss.hasPermission('training:experience:delete')")
    public CommonResult<Boolean> deleteExperienceByIds(@RequestParam("ids") List<Integer> ids) {
        experienceService.deleteExperienceListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('training:experience:query')")
    public CommonResult<TrainingExperienceRespVO> getExperienceById(@RequestParam("id") Integer id) {
        TrainingExperienceRespVO experience = experienceService.getExperience(id);
        return CommonResult.success(experience);
    }

    @GetMapping("/page")
    @PreAuthorize("@ss.hasPermission('training:experience:query')")
    public CommonResult<PageResult<TrainingExperienceRespVO>> getExperiencePage(@Valid TrainingExperiencePageReqVO pageReqVO,

                                                                                @RequestParam(value = "userId", required = false) Integer userId) {
        PageResult<TrainingExperienceDO> pageResult = experienceService.getExperiencePage(pageReqVO,userId);
        return success(BeanUtils.toBean(pageResult, TrainingExperienceRespVO.class));
    }

    @GetMapping("/export-excel")
    @PreAuthorize("@ss.hasPermission('training:experience:export')")
    public void exportExperienceExcel(@Valid TrainingExperiencePageReqVO pageReqVO,
              HttpServletResponse response, @RequestParam("userId") Integer userId) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<TrainingExperienceDO> list = experienceService.getExperiencePage(pageReqVO, userId).getList();
        // 导出 Excel
        ExcelUtils.write(response, "培训经历.xls", "数据", TrainingExperienceRespVO.class,
                        BeanUtils.toBean(list, TrainingExperienceRespVO.class));
    }

}
