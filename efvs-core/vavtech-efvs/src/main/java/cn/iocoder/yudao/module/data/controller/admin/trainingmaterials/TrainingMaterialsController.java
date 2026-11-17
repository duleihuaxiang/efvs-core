package cn.iocoder.yudao.module.data.controller.admin.trainingmaterials;

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


import cn.iocoder.yudao.module.data.controller.admin.trainingmaterials.vo.*;
import cn.iocoder.yudao.module.data.dal.dataobject.trainingmaterials.TrainingMaterialsDO;
import cn.iocoder.yudao.module.data.service.trainingmaterials.TrainingMaterialsService;

@RestController
@RequestMapping("/data/training-materials")
@Validated
public class TrainingMaterialsController {

    @Resource
    private TrainingMaterialsService trainingMaterialsService;

    @PostMapping("/create")
    @PreAuthorize("@ss.hasPermission('data:training-materials:create')")
    public CommonResult<Long> createTrainingMaterials(@Valid @RequestBody TrainingMaterialsSaveReqVO createReqVO) {
        return success(trainingMaterialsService.createTrainingMaterials(createReqVO));
    }

    @PutMapping("/update")
    @PreAuthorize("@ss.hasPermission('data:training-materials:update')")
    public CommonResult<Boolean> updateTrainingMaterials(@Valid @RequestBody TrainingMaterialsSaveReqVO updateReqVO) {
        trainingMaterialsService.updateTrainingMaterials(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @PreAuthorize("@ss.hasPermission('data:training-materials:delete')")
    public CommonResult<Boolean> deleteTrainingMaterials(@RequestParam("id") Integer id) {
        trainingMaterialsService.deleteTrainingMaterials(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
                @PreAuthorize("@ss.hasPermission('data:training-materials:delete')")
    public CommonResult<Boolean> deleteTrainingMaterialsList(@RequestParam("ids") List<Integer> ids) {
        trainingMaterialsService.deleteTrainingMaterialsListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('data:training-materials:query')")
    public CommonResult<TrainingMaterialsRespVO> getTrainingMaterials(@RequestParam("id") Integer id) {
        TrainingMaterialsRespVO trainingMaterials = trainingMaterialsService.getTrainingMaterials(id);
        return success(trainingMaterials);
    }

    @GetMapping("/page")
    @PreAuthorize("@ss.hasPermission('data:training-materials:query')")
    public CommonResult<PageResult<TrainingMaterialsRespVO>> getTrainingMaterialsPage(@Valid TrainingMaterialsPageReqVO pageReqVO) {
        PageResult<TrainingMaterialsDO> pageResult = trainingMaterialsService.getTrainingMaterialsPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, TrainingMaterialsRespVO.class));
    }

    // file: D:\vavtech\vavtech-efvs\src\main\java\cn\iocoder\yudao\module\data\controller\admin\trainingmaterials\TrainingMaterialsController.java

    @GetMapping("/export-excel")
    @PreAuthorize("@ss.hasPermission('data:training-materials:export')")
    public void exportTrainingMaterialsExcel(@Valid TrainingMaterialsPageReqVO pageReqVO,
                                             HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<TrainingMaterialsDO> list = trainingMaterialsService.getTrainingMaterialsPage(pageReqVO).getList();
        List<TrainingMaterialsRespVO> voList = getTrainingMaterialsRespVOS(list);
        // 3. 导出 Excel，此时 ExcelUtils 会读取带有 @ExcelProperty 的 categoryDesc 字段
        ExcelUtils.write(response, "培训资料管理.xlsx", "数据", TrainingMaterialsRespVO.class, voList);
    }

    private static List<TrainingMaterialsRespVO> getTrainingMaterialsRespVOS(List<TrainingMaterialsDO> list) {
        // 1. 先将 DO 转换为 VO
        List<TrainingMaterialsRespVO> voList = BeanUtils.toBean(list, TrainingMaterialsRespVO.class);
        // 2. 【新增】遍历处理 category 字段，转换为中文描述
        for (TrainingMaterialsRespVO vo : voList) {
            String cat = vo.getCategory();
            if ("1".equals(cat)) {
                vo.setCategoryDesc("机务人员");
            } else if ("2".equals(cat)) {
                vo.setCategoryDesc("试飞人员");
            } else {
                // 如果存在其他值或空值，保持为空或原样，视需求而定
                vo.setCategoryDesc(cat);
            }
        }
        return voList;
    }

}
