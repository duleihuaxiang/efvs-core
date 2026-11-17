package cn.iocoder.yudao.module.data.controller.admin.trainingattendance;

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


import cn.iocoder.yudao.module.data.controller.admin.trainingattendance.vo.*;
import cn.iocoder.yudao.module.data.dal.dataobject.trainingattendance.TrainingAttendanceDO;
import cn.iocoder.yudao.module.data.service.trainingattendance.TrainingAttendanceService;

@RestController
@RequestMapping("/data/training-attendance")
@Validated
public class TrainingAttendanceController {

    @Resource
    private TrainingAttendanceService trainingAttendanceService;

    @PostMapping("/create")
    @PreAuthorize("@ss.hasPermission('data:training-attendance:create')")
    public CommonResult<Integer> createTrainingAttendance(@Valid @RequestBody TrainingAttendanceSaveReqVO createReqVO) {
        return success(trainingAttendanceService.createTrainingAttendance(createReqVO));
    }

    @PutMapping("/update")
    @PreAuthorize("@ss.hasPermission('data:training-attendance:update')")
    public CommonResult<Boolean> updateTrainingAttendance(@Valid @RequestBody TrainingAttendanceSaveReqVO updateReqVO) {
        trainingAttendanceService.updateTrainingAttendance(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @PreAuthorize("@ss.hasPermission('data:training-attendance:delete')")
    public CommonResult<Boolean> deleteTrainingAttendance(@RequestParam("id") Integer id) {
        trainingAttendanceService.deleteTrainingAttendance(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
                @PreAuthorize("@ss.hasPermission('data:training-attendance:delete')")
    public CommonResult<Boolean> deleteTrainingAttendanceList(@RequestParam("ids") List<Integer> ids) {
        trainingAttendanceService.deleteTrainingAttendanceListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('data:training-attendance:query')")
    public CommonResult<TrainingAttendanceRespVO> getTrainingAttendance(@RequestParam("id") Integer id) {
        TrainingAttendanceDO trainingAttendance = trainingAttendanceService.getTrainingAttendance(id);
        return success(BeanUtils.toBean(trainingAttendance, TrainingAttendanceRespVO.class));
    }

    @GetMapping("/page")
    @PreAuthorize("@ss.hasPermission('data:training-attendance:query')")
    public CommonResult<PageResult<TrainingAttendanceRespVO>> getTrainingAttendancePage(@Valid TrainingAttendancePageReqVO pageReqVO) {
        PageResult<TrainingAttendanceDO> pageResult = trainingAttendanceService.getTrainingAttendancePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, TrainingAttendanceRespVO.class));
    }

    @GetMapping("/export-excel")
    @PreAuthorize("@ss.hasPermission('data:training-attendance:export')")
    public void exportTrainingAttendanceExcel(@Valid TrainingAttendancePageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<TrainingAttendanceDO> list = trainingAttendanceService.getTrainingAttendancePage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "培训签到.xls", "数据", TrainingAttendanceRespVO.class,
                        BeanUtils.toBean(list, TrainingAttendanceRespVO.class));
    }

}
