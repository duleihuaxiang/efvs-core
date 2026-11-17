package cn.iocoder.yudao.module.data.controller.admin.trainingrecord;

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


import cn.iocoder.yudao.module.data.controller.admin.trainingrecord.vo.*;
import cn.iocoder.yudao.module.data.dal.dataobject.trainingrecord.TrainingRecordDO;
import cn.iocoder.yudao.module.data.service.trainingrecord.TrainingRecordService;

@RestController
@RequestMapping("/data/training-record")
@Validated
public class TrainingRecordController {

    @Resource
    private TrainingRecordService trainingRecordService;

    @PostMapping("/create")
    @PreAuthorize("@ss.hasPermission('data:training-record:create')")
    public CommonResult<Integer> createTrainingRecord(@Valid @RequestBody TrainingRecordSaveReqVO createReqVO) {
        return success(trainingRecordService.createTrainingRecord(createReqVO));
    }

    @PutMapping("/update")
    @PreAuthorize("@ss.hasPermission('data:training-record:update')")
    public CommonResult<Boolean> updateTrainingRecord(@Valid @RequestBody TrainingRecordSaveReqVO updateReqVO) {
        trainingRecordService.updateTrainingRecord(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @PreAuthorize("@ss.hasPermission('data:training-record:delete')")
    public CommonResult<Boolean> deleteTrainingRecord(@RequestParam("id") Integer id) {
        trainingRecordService.deleteTrainingRecord(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
                @PreAuthorize("@ss.hasPermission('data:training-record:delete')")
    public CommonResult<Boolean> deleteTrainingRecordList(@RequestParam("ids") List<Integer> ids) {
        trainingRecordService.deleteTrainingRecordListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('data:training-record:query')")
    public CommonResult<TrainingRecordRespVO> getTrainingRecord(@RequestParam("id") Integer id) {
        TrainingRecordDO trainingRecord = trainingRecordService.getTrainingRecord(id);
        return success(BeanUtils.toBean(trainingRecord, TrainingRecordRespVO.class));
    }

    @GetMapping("/page")
    @PreAuthorize("@ss.hasPermission('data:training-record:query')")
    public CommonResult<PageResult<TrainingRecordRespVO>> getTrainingRecordPage(@Valid TrainingRecordPageReqVO pageReqVO) {
        PageResult<TrainingRecordDO> pageResult = trainingRecordService.getTrainingRecordPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, TrainingRecordRespVO.class));
    }

    @GetMapping("/export-excel")
    @PreAuthorize("@ss.hasPermission('data:training-record:export')")
    public void exportTrainingRecordExcel(@Valid TrainingRecordPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<TrainingRecordDO> list = trainingRecordService.getTrainingRecordPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "培训记录.xls", "数据", TrainingRecordRespVO.class,
                        BeanUtils.toBean(list, TrainingRecordRespVO.class));
    }

}
