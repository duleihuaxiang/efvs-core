package cn.iocoder.yudao.module.qar.controller.admin.qardata;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.qar.controller.admin.qardata.vo.QarPageReqVO;
import cn.iocoder.yudao.module.qar.controller.admin.qardata.vo.QarRespVO;
import cn.iocoder.yudao.module.qar.controller.admin.qardata.vo.QarSaveReqVO;
import cn.iocoder.yudao.module.qar.controller.admin.qardata.vo.QarTaxiComparePageReqVO;
import cn.iocoder.yudao.module.qar.controller.admin.qardata.vo.QarTaxiComparePageRespVO;
import cn.iocoder.yudao.module.qar.controller.admin.qardata.vo.QarTaxiComparePlaybackReqVO;
import cn.iocoder.yudao.module.qar.controller.admin.qardata.vo.QarTaxiComparePlaybackRespVO;
import cn.iocoder.yudao.module.qar.controller.admin.qardata.vo.QarTaxiPhasePlaybackRespVO;
import cn.iocoder.yudao.module.qar.controller.admin.qardata.vo.TaxiComparePlaybackMessage;
import cn.iocoder.yudao.module.qar.service.qardata.QarTaxiComparePageService;
import cn.iocoder.yudao.module.qar.service.qardata.QarTaxiComparePlaybackService;
import cn.iocoder.yudao.module.qar.service.qardata.QarTaxiPhasePlaybackService;
import cn.iocoder.yudao.module.qar.service.qardata.QarService;
import cn.iocoder.yudao.module.qar.websocket.message.TaxiPhaseStreamMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;

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


import cn.iocoder.yudao.module.qar.dal.dataobject.qar.QarDO;

@Slf4j
@RestController
@RequestMapping("/efvs/qar")
@Validated
public class QarController {

    @Resource
    private QarService qarService;

    @Resource
    private QarTaxiPhasePlaybackService playbackService;

    @Resource
    private QarTaxiComparePageService taxiComparePageService;

    @Resource
    private QarTaxiComparePlaybackService taxiComparePlaybackService;

    @PostMapping("/create")
    @PreAuthorize("@ss.hasPermission('efvs:qar:create')")
    public CommonResult<List<Long>> createQar(@Valid @RequestBody QarSaveReqVO createReqVO) {
        return success(qarService.createQar(createReqVO));
    }

    @PutMapping("/update")
    @PreAuthorize("@ss.hasPermission('efvs:qar:update')")
    public CommonResult<Boolean> updateQar(@Valid @RequestBody QarSaveReqVO updateReqVO) {
        qarService.updateQar(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @PreAuthorize("@ss.hasPermission('efvs:qar:delete')")
    public CommonResult<Boolean> deleteQar(@RequestParam("id") Long id) {
        qarService.deleteQar(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
                @PreAuthorize("@ss.hasPermission('efvs:qar:delete')")
    public CommonResult<Boolean> deleteQarList(@RequestParam("ids") List<Long> ids) {
        qarService.deleteQarListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('efvs:qar:query')")
    public CommonResult<QarRespVO> getQar(@RequestParam("id") Long id) {
        QarDO qar = qarService.getQar(id);
        return success(BeanUtils.toBean(qar, QarRespVO.class));
    }

    @GetMapping("/page")
    @PreAuthorize("@ss.hasPermission('efvs:qar:query')")
    public CommonResult<PageResult<QarRespVO>> getQarPage(@Valid QarPageReqVO pageReqVO) {
        PageResult<QarDO> pageResult = qarService.getQarPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, QarRespVO.class));
    }

    @GetMapping("/export-excel")
    @PreAuthorize("@ss.hasPermission('efvs:qar:export')")
    public void exportQarExcel(@Valid QarPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<QarDO> list = qarService.getQarPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "qar数据管理.xlsx", "数据", QarRespVO.class,
                        BeanUtils.toBean(list, QarRespVO.class));
    }

    /**
     * 3D 复现「开始播放」：一次性把 taxi-phase 需要的全部数据返回前端。
     *
     * <p>返回结构尽量与 WebSocket 帧一致：外层 {@code type=taxi-phase-stream}，
     * {@code content} 为 {@link TaxiPhaseStreamMessage} 的 JSON。</p>
     */
    @PostMapping("/taxi-phase/play")
    public CommonResult<QarTaxiPhasePlaybackRespVO> playTaxiPhase(
            @RequestParam("id") Long id,
            @RequestParam(value = "startId", required = false, defaultValue = "0") Integer startId,
            @RequestParam(value = "endId", required = false) Integer endId) {
        TaxiPhaseStreamMessage message = playbackService.playTaxiPhase(id, startId, endId);

        // 与前端当前 WebSocket 解析方式保持一致：外层 type + content(JSON string)
        String contentJson = JsonUtils.toJsonString(message);

        QarTaxiPhasePlaybackRespVO respVO = new QarTaxiPhasePlaybackRespVO();
        respVO.setType("taxi-phase-stream");
        respVO.setContent(contentJson);
        return success(respVO);
    }

    /**
     * 性能分析 / 多架次对比：HTTP 分页拉取，每架次每页条数与 {@code /taxi-phase/play} 内部批大小一致（默认 10），
     * 阶段判定与累计距离与 WebSocket {@code taxi-compare-stream} 同源（{@link cn.iocoder.yudao.module.qar.websocket.service.TaxiCompareStreamService}）。
     */
    @PostMapping("/taxi-phase/compare-page")
    @PreAuthorize("@ss.hasPermission('efvs:qar:query')")
    public CommonResult<QarTaxiComparePageRespVO> taxiPhaseComparePage(@Valid @RequestBody QarTaxiComparePageReqVO reqVO) {
        return success(taxiComparePageService.fetchPage(reqVO));
    }

    /**
     * 性能分析 / 多架次对比：一次性聚合返回全部数据（内部仍按 10 条分页循环读取），前端无需再循环请求。
     */
    @PostMapping("/taxi-phase/compare-play")
    @PreAuthorize("@ss.hasPermission('efvs:qar:query')")
    public CommonResult<QarTaxiComparePlaybackRespVO> taxiPhaseComparePlay(@Valid @RequestBody QarTaxiComparePlaybackReqVO reqVO) {
        long start = System.nanoTime();
        List<Long> ids = reqVO.getIds();
        TaxiComparePlaybackMessage msg = taxiComparePlaybackService.play(ids);
        long playNanos = System.nanoTime() - start;

        long jsonStart = System.nanoTime();
        String content = JsonUtils.toJsonString(msg);
        long jsonNanos = System.nanoTime() - jsonStart;

        long totalNanos = System.nanoTime() - start;
        log.info("[taxiPhaseComparePlay][ids={} 架次数={} items={} 服务耗时={}s 序列化耗时={}s 总耗时={}s]",
                ids, msg.getSorties() != null ? msg.getSorties().size() : 0, msg.getTotalCount(),
                String.format("%.4f", playNanos / 1_000_000_000.0),
                String.format("%.4f", jsonNanos / 1_000_000_000.0),
                String.format("%.4f", totalNanos / 1_000_000_000.0));

        QarTaxiComparePlaybackRespVO respVO = new QarTaxiComparePlaybackRespVO();
        respVO.setType("taxi-compare-stream");
        respVO.setContent(content);
        return success(respVO);
    }
}
