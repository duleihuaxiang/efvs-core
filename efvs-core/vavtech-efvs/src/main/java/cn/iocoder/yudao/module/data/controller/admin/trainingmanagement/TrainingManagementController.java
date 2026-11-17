package cn.iocoder.yudao.module.data.controller.admin.trainingmanagement;

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


import cn.iocoder.yudao.module.data.controller.admin.trainingmanagement.vo.*;
import cn.iocoder.yudao.module.data.dal.dataobject.trainingmanagement.TrainingManagementDO;
import cn.iocoder.yudao.module.data.service.trainingmanagement.TrainingManagementService;

@RestController
@RequestMapping("/data/training-management")
@Validated
public class TrainingManagementController {

    @Resource
    private TrainingManagementService trainingManagementService;

    @PostMapping("/create")
    @PreAuthorize("@ss.hasPermission('data:training-management:create')")
    public CommonResult<Integer> createTrainingManagement(@Valid @RequestBody TrainingManagementSaveReqVO createReqVO) {
        return success(trainingManagementService.createTrainingManagement(createReqVO));
    }

    @PutMapping("/update")
    @PreAuthorize("@ss.hasPermission('data:training-management:update')")
    public CommonResult<Boolean> updateTrainingManagement(@Valid @RequestBody TrainingManagementSaveReqVO updateReqVO) {
        trainingManagementService.updateTrainingManagement(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @PreAuthorize("@ss.hasPermission('data:training-management:delete')")
    public CommonResult<Boolean> deleteTrainingManagement(@RequestParam("id") Integer id) {
        trainingManagementService.deleteTrainingManagement(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
                @PreAuthorize("@ss.hasPermission('data:training-management:delete')")
    public CommonResult<Boolean> deleteTrainingManagementList(@RequestParam("ids") List<Integer> ids) {
        trainingManagementService.deleteTrainingManagementListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('data:training-management:query')")
    public CommonResult<TrainingManagementRespVO> getTrainingManagement(@RequestParam("id") Integer id) {
        TrainingManagementDO trainingManagement = trainingManagementService.getTrainingManagement(id);
        return success(BeanUtils.toBean(trainingManagement, TrainingManagementRespVO.class));
    }

    @GetMapping("/page")
    @PreAuthorize("@ss.hasPermission('data:training-management:query')")
    public CommonResult<PageResult<TrainingManagementRespVO>> getTrainingManagementPage(@Valid TrainingManagementPageReqVO pageReqVO) {
        PageResult<TrainingManagementDO> pageResult = trainingManagementService.getTrainingManagementPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, TrainingManagementRespVO.class));
    }

    @GetMapping("/export-excel")
    @PreAuthorize("@ss.hasPermission('data:training-management:export')")
    public void exportTrainingManagementExcel(@Valid TrainingManagementPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<TrainingManagementDO> list = trainingManagementService.getTrainingManagementPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "培训管理.xls", "数据", TrainingManagementRespVO.class,
                        BeanUtils.toBean(list, TrainingManagementRespVO.class));
    }

}
