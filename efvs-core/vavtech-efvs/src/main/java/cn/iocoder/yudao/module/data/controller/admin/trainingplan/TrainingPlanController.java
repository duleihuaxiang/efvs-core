package cn.iocoder.yudao.module.data.controller.admin.trainingplan;

import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;

import javax.validation.constraints.*;
import javax.validation.*;
import javax.servlet.http.*;
import java.util.*;
import java.io.IOException;
import java.util.stream.Collectors;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;


import cn.iocoder.yudao.module.data.controller.admin.trainingplan.vo.*;
import cn.iocoder.yudao.module.data.dal.dataobject.trainingplan.TrainingPlanDO;
import cn.iocoder.yudao.module.data.service.trainingplan.TrainingPlanService;

@RestController
@RequestMapping("/data/training-plan")
@Validated
public class TrainingPlanController {

    @Resource
    private TrainingPlanService trainingPlanService;

    @PostMapping("/create")
    @PreAuthorize("@ss.hasPermission('data:training-plan:create')")
    public CommonResult<Integer> createTrainingPlan(@Valid @RequestBody TrainingPlanSaveReqVO createReqVO) {
        return success(trainingPlanService.createTrainingPlan(createReqVO));
    }

    @PutMapping("/update")
    @PreAuthorize("@ss.hasPermission('data:training-plan:update')")
    public CommonResult<Boolean> updateTrainingPlan(@Valid @RequestBody TrainingPlanSaveReqVO updateReqVO) {
        trainingPlanService.updateTrainingPlan(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @PreAuthorize("@ss.hasPermission('data:training-plan:delete')")
    public CommonResult<Boolean> deleteTrainingPlan(@RequestParam("id") Integer id) {
        trainingPlanService.deleteTrainingPlan(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
                @PreAuthorize("@ss.hasPermission('data:training-plan:delete')")
    public CommonResult<Boolean> deleteTrainingPlanList(@RequestParam("ids") List<Integer> ids) {
        trainingPlanService.deleteTrainingPlanListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('data:training-plan:query')")
    public CommonResult<TrainingPlanRespVO> getTrainingPlan(@RequestParam("id") Integer id) {
        TrainingPlanDO trainingPlan = trainingPlanService.getTrainingPlan(id);
        return success(BeanUtils.toBean(trainingPlan, TrainingPlanRespVO.class));
    }

    @GetMapping("/page")
    @PreAuthorize("@ss.hasPermission('data:training-plan:query')")
    public CommonResult<PageResult<TrainingPlanRespVO>> getTrainingPlanPage(@Valid TrainingPlanPageReqVO pageReqVO) {
        PageResult<TrainingPlanDO> pageResult = trainingPlanService.getTrainingPlanPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, TrainingPlanRespVO.class));
    }

    @GetMapping("/export-excel")
    @PreAuthorize("@ss.hasPermission('data:training-plan:export')")
    public void exportTrainingPlanExcel(@Valid TrainingPlanPageReqVO pageReqVO,
                                        HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<TrainingPlanDO> list = trainingPlanService.getTrainingPlanPage(pageReqVO).getList();

        // 提取所有 projectLeader 的 ID
        List<Integer> leaderIds = list.stream()
                .map(TrainingPlanDO::getProjectLeader)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        // 获取 projectLeader 对应的昵称映射
        Map<Integer, String> leaderNickNames = trainingPlanService.getUserNickNamesByIds(leaderIds);

        // 将 TrainingPlanDO 转换为 TrainingPlanRespVO 并设置 efvsProjectLeader
        List<TrainingPlanRespVO> respVOList = list.stream()
                .map(trainingPlan -> {
                    TrainingPlanRespVO respVO = BeanUtils.toBean(trainingPlan, TrainingPlanRespVO.class);
                    Integer projectLeaderId = trainingPlan.getProjectLeader();
                    respVO.setEfvsProjectLeader(leaderNickNames.getOrDefault(projectLeaderId, "未知"));
                    return respVO;
                })
                .collect(Collectors.toList());

        // 导出 Excel
        ExcelUtils.write(response, "培训计划.xlsx", "数据", TrainingPlanRespVO.class, respVOList);
    }

    @GetMapping("/getTrainingPlan")
    public CommonResult<TrainingPlanRespVO> getTrainingPlanByProcessInstanceId(@RequestParam("processInstanceId") String processInstanceId) {
        TrainingPlanDO trainingPlan = trainingPlanService.getTrainingPlanByProcessInstanceId(processInstanceId);
        return success(BeanUtils.toBean(trainingPlan, TrainingPlanRespVO.class));
    }

    @GetMapping("/getTrainingById")
    @PreAuthorize("@ss.hasPermission('data:training-plan:query')")
    @TenantIgnore
    public CommonResult<Map<Integer,String>> getTrainingById(@RequestParam(value = "ids", required = false, defaultValue = "") List<String> idsStr
    ) {
        // 转换为 Integer 列表
        List<Integer> ids = idsStr.stream()
                .map(Integer::parseInt)
                .collect(Collectors.toList());
        Map<Integer,String> nickNameList = trainingPlanService.gettrainingByid(ids);
        return success(nickNameList);
    }
}
