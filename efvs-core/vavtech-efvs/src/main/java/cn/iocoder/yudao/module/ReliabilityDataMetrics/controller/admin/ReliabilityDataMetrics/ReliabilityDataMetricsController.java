package cn.iocoder.yudao.module.ReliabilityDataMetrics.controller.admin.ReliabilityDataMetrics;

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


import cn.iocoder.yudao.module.ReliabilityDataMetrics.controller.admin.ReliabilityDataMetrics.vo.*;
import cn.iocoder.yudao.module.ReliabilityDataMetrics.dal.dataobject.ReliabilityDataMetrics.ReliabilityDataMetricsDO;
import cn.iocoder.yudao.module.ReliabilityDataMetrics.service.ReliabilityDataMetrics.ReliabilityDataMetricsService;

@RestController
@RequestMapping("/ReliabilityDataMetrics/reliability-data-metrics")
@Validated
public class ReliabilityDataMetricsController {

    @Resource
    private ReliabilityDataMetricsService reliabilityDataMetricsService;

    @PostMapping("/create")
    @PreAuthorize("@ss.hasPermission('ReliabilityDataMetrics:reliability-data-metrics:create')")
    public CommonResult<Integer> createReliabilityDataMetrics(@Valid @RequestBody ReliabilityDataMetricsSaveReqVO createReqVO) {
        return success(reliabilityDataMetricsService.createReliabilityDataMetrics(createReqVO));
    }

    @PutMapping("/update")
    @PreAuthorize("@ss.hasPermission('ReliabilityDataMetrics:reliability-data-metrics:update')")
    public CommonResult<Boolean> updateReliabilityDataMetrics(@Valid @RequestBody ReliabilityDataMetricsSaveReqVO updateReqVO) {
        reliabilityDataMetricsService.updateReliabilityDataMetrics(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @PreAuthorize("@ss.hasPermission('ReliabilityDataMetrics:reliability-data-metrics:delete')")
    public CommonResult<Boolean> deleteReliabilityDataMetrics(@RequestParam("id") Integer id) {
        reliabilityDataMetricsService.deleteReliabilityDataMetrics(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
                @PreAuthorize("@ss.hasPermission('ReliabilityDataMetrics:reliability-data-metrics:delete')")
    public CommonResult<Boolean> deleteReliabilityDataMetricsList(@RequestParam("ids") List<Integer> ids) {
        reliabilityDataMetricsService.deleteReliabilityDataMetricsListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('ReliabilityDataMetrics:reliability-data-metrics:query')")
    public CommonResult<ReliabilityDataMetricsRespVO> getReliabilityDataMetrics(@RequestParam("id") Integer id) {
        ReliabilityDataMetricsDO reliabilityDataMetrics = reliabilityDataMetricsService.getReliabilityDataMetrics(id);
        return success(BeanUtils.toBean(reliabilityDataMetrics, ReliabilityDataMetricsRespVO.class));
    }

    @GetMapping("/page")
    @PreAuthorize("@ss.hasPermission('ReliabilityDataMetrics:reliability-data-metrics:query')")
    public CommonResult<PageResult<ReliabilityDataMetricsRespVO>> getReliabilityDataMetricsPage(@Valid ReliabilityDataMetricsPageReqVO pageReqVO) {
        PageResult<ReliabilityDataMetricsDO> pageResult = reliabilityDataMetricsService.getReliabilityDataMetricsPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ReliabilityDataMetricsRespVO.class));
    }

    @GetMapping("/export-excel")
    @PreAuthorize("@ss.hasPermission('ReliabilityDataMetrics:reliability-data-metrics:export')")
    public void exportReliabilityDataMetricsExcel(@Valid ReliabilityDataMetricsPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ReliabilityDataMetricsDO> list = reliabilityDataMetricsService.getReliabilityDataMetricsPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "可靠性指标.xlsx", "数据", ReliabilityDataMetricsRespVO.class,
                        BeanUtils.toBean(list, ReliabilityDataMetricsRespVO.class));
    }

}
