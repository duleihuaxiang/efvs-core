package cn.iocoder.yudao.module.reliability.controller.admin.reliabilitydata;

import cn.iocoder.yudao.module.reliability.word.WordUtils;
import cn.iocoder.yudao.module.reliability.xmlUtils.XmlExportUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;
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



import cn.iocoder.yudao.module.reliability.controller.admin.reliabilitydata.vo.*;
import cn.iocoder.yudao.module.reliability.dal.dataobject.reliabilitydata.ReliabilityDataDO;
import cn.iocoder.yudao.module.reliability.service.reliabilitydata.ReliabilityDataService;

@RestController
@RequestMapping("/reliability/data")
@Validated
@Slf4j
public class ReliabilityDataController {

    @Resource
    private ReliabilityDataService dataService;

    @PostMapping("/create")
    @PreAuthorize("@ss.hasPermission('reliability:data:create')")
    public CommonResult<Integer> createData(@Valid @RequestBody ReliabilityDataSaveReqVO createReqVO) {
        return success(dataService.createData(createReqVO));
    }

    @PutMapping("/update")
    @PreAuthorize("@ss.hasPermission('reliability:data:update')")
    public CommonResult<Boolean> updateData(@Valid @RequestBody ReliabilityDataSaveReqVO updateReqVO) {
        dataService.updateData(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @PreAuthorize("@ss.hasPermission('reliability:data:delete')")
    public CommonResult<Boolean> deleteData(@RequestParam("id") Integer id) {
        dataService.deleteData(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @PreAuthorize("@ss.hasPermission('reliability:data:delete')")
    public CommonResult<Boolean> deleteDataList(@RequestParam("ids") List<Integer> ids) {
        dataService.deleteDataListByIds(ids);
        return success(true);
    }

    @GetMapping("/getReliabilityDataById")
    @PreAuthorize("@ss.hasPermission('reliability:data:query')")
    public CommonResult<ReliabilityDataRespVO> getReliabilityDataById(@RequestParam("id") Integer id) {
        ReliabilityDataDO data = dataService.getReliabilityDataById(id);
        return success(BeanUtils.toBean(data, ReliabilityDataRespVO.class));
    }

    @PostMapping("/getReliabilityDataDetailPage")
    @PreAuthorize("@ss.hasPermission('reliability:data:query')")
    public CommonResult<PageResult<ReliabilityDataRespVO>> getReliabilityDataDetailPage(
            @Valid @RequestBody ReliabilityDataPageReqVO pageReqVO) {
        // 不分页，返回全部数据
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        PageResult<ReliabilityDataRespVO> pageResult = dataService.getDataDetailPage(pageReqVO);
        return success(pageResult);
    }

    @PostMapping("/exportReliabilityDataDetail")
    @PreAuthorize("@ss.hasPermission('reliability:data:export')")
    public void exportDataExcel(@Valid ReliabilityDataPageReqVO pageReqVO,
                                HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ReliabilityDataRespVO> list = dataService.getDataDetailPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "可靠性数据明细.xls", "明细", ReliabilityDataRespVO.class,
                BeanUtils.toBean(list, ReliabilityDataRespVO.class));
    }

    @GetMapping("/form-no")
    public CommonResult<String> getReliabilityDataFormNo() {
        // 调用服务层方法获取表单编号
        String reliabilityDataFormNo = dataService.getGenerateReliabilityDataFormNo();

        // 返回结果
        return success(reliabilityDataFormNo);
    }

    @GetMapping("/getMTBFAndMTBUR")
    @PreAuthorize("@ss.hasPermission('reliability:data:query')")
    public CommonResult<MtbfMtburRespVO> calculateReliabilityMetrics(@RequestParam("partNumber") String partNumber) {
        MtbfMtburRespVO respVO = dataService.calculateReliabilityMetrics(partNumber);
        return success(respVO);
    }

/*    @GetMapping("/exportMtbfAndMtbur")
    @Operation(summary = "导出MTBF或MTBUR Excel")
    @PreAuthorize("@ss.hasPermission('reliability:data:export')")
    public void exportMetricsExcel(
            @RequestParam("type") String type, // "mtbf" 或 "mtbur"
            @Valid ReliabilityDataPageReqVO pageReqVO,
            HttpServletResponse response) throws IOException {
        // 校验导出类型非空且合法，同时校验查询参数全部非空
        if (ObjectUtils.isEmpty(type)
                || !("mtbf".equalsIgnoreCase(type) || "mtbur".equalsIgnoreCase(type))
                || ObjectUtils.isEmpty(pageReqVO.getPartName())
                || ObjectUtils.isEmpty(pageReqVO.getSerialNumber())
                || ObjectUtils.isEmpty(pageReqVO.getPartNumber())) {
            // 按需抛出对应精准异常，保留原错误提示，避免通用提示不友好
            if (ObjectUtils.isEmpty(type)) {
                throw new IllegalArgumentException("导出失败：type 参数不能为空");
            } else if (!("mtbf".equalsIgnoreCase(type) || "mtbur".equalsIgnoreCase(type))) {
                throw new IllegalArgumentException("导出失败：type 参数必须为 'mtbf' 或 'mtbur'");
            } else {
                throw new IllegalArgumentException("导出失败：partName、partNumber、serialNumber 所有参数都不能为空");
            }
        }
        // 根据类型设置文件名和标题，调用Service导出单条数据
        if ("mtbf".equalsIgnoreCase(type)) {
            String fileName = "平均故障间隔时间(MTBF)-" + pageReqVO.getPartNumber() + ".xls";
            String sheetName = "MTBF数据";
            List<MtbfRespVO> voList = dataService.getMtbfDataExport(pageReqVO);
            ExcelUtils.write(response, fileName, sheetName, MtbfRespVO.class, voList);
        } else if ("mtbur".equalsIgnoreCase(type)) {
            String fileName = "平均非计划拆换时间(MTBUR)-" + pageReqVO.getPartNumber() + ".xls";
            String sheetName = "MTBUR数据";
            List<MtburRespVO> voList = dataService.getMtburDataExport(pageReqVO);
            ExcelUtils.write(response, fileName, sheetName, MtburRespVO.class, voList);
        } else {
            throw new IllegalArgumentException("不支持的导出类型: " + type);
        }
    }*/

    @GetMapping("/exportMtbfAndMtbur")
    @PreAuthorize("@ss.hasPermission('reliability:data:export')")
    public void exportMtbfAndMtbur(
            @RequestParam("type") String type,
            @Valid ReliabilityDataPageReqVO pageReqVO,
            HttpServletResponse response) throws IOException {

        // 校验逻辑不变（略，同原代码）
        if (ObjectUtils.isEmpty(type)
                || !("mtbf".equalsIgnoreCase(type) || "mtbur".equalsIgnoreCase(type))
                || ObjectUtils.isEmpty(pageReqVO.getPartName())
                || ObjectUtils.isEmpty(pageReqVO.getSerialNumber())
                || ObjectUtils.isEmpty(pageReqVO.getPartNumber())) {
            if (ObjectUtils.isEmpty(type)) {
                throw new IllegalArgumentException("导出失败：type 参数不能为空");
            } else if (!("mtbf".equalsIgnoreCase(type) || "mtbur".equalsIgnoreCase(type))) {
                throw new IllegalArgumentException("导出失败：type 参数必须为 'mtbf' 或 'mtbur'");
            } else {
                throw new IllegalArgumentException("导出失败：partName、partNumber、serialNumber 所有参数都不能为空");
            }
        }

        String fileName, tableTitle;
        List<?> voList;

        if ("mtbf".equalsIgnoreCase(type)) {
            fileName = "平均故障间隔时间(MTBF)-" + pageReqVO.getPartNumber() + ".docx";
            tableTitle = "平均故障间隔时间(MTBF)";
            voList = dataService.getMtbfDataExport(pageReqVO);
        } else { // mtbur
            fileName = "平均非计划拆换时间(MTBUR)-" + pageReqVO.getPartNumber() + ".docx";
            tableTitle = "平均非计划拆换时间(MTBUR)";
            voList = dataService.getMtburDataExport(pageReqVO);
        }

        WordUtils.write(response, fileName, tableTitle, voList,
                voList.isEmpty() ? Object.class : voList.get(0).getClass());
    }

    @GetMapping("/exportMtbfAndMtburCombined")
    @PreAuthorize("@ss.hasPermission('reliability:data:export')")
    public void exportMtbfAndMtburCombined(
            @Valid ReliabilityDataPageReqVO pageReqVO,
            HttpServletResponse response) throws IOException {

        // 校验必要参数
        if (ObjectUtils.isEmpty(pageReqVO.getPartName())
                || ObjectUtils.isEmpty(pageReqVO.getSerialNumber())
                || ObjectUtils.isEmpty(pageReqVO.getPartNumber())) {
            throw new IllegalArgumentException("导出失败：partName、partNumber、serialNumber 所有参数都不能为空");
        }

        // 获取 MTBF 和 MTBUR 数据
        List<?> mtbfDataList = dataService.getMtbfDataExport(pageReqVO);
        List<?> mtburDataList = dataService.getMtburDataExport(pageReqVO);

        // 设置文件名和标题
        String fileName = "MTBF与MTBUR综合报告-" + pageReqVO.getPartNumber() + ".docx";
        String mtbfTableTitle = "平均故障间隔时间(MTBF)";
        String mtburTableTitle = "平均非计划拆换时间(MTBUR)";

        // 写入 Word 文档（支持多表格）
        WordUtils.writeMultipleTables(response, fileName,
                Arrays.asList(mtbfTableTitle, mtburTableTitle),
                Arrays.asList(mtbfDataList, mtburDataList));
    }

    @GetMapping("/exportMtbfAndMtburCombinedXml")
    @PreAuthorize("@ss.hasPermission('reliability:data:export')")
    public void exportMtbfAndMtburCombinedXml(
            @Valid ReliabilityDataPageReqVO pageReqVO,
            HttpServletResponse response) throws IOException {
        // 参数校验
        if (ObjectUtils.isEmpty(pageReqVO.getPartName())
                || ObjectUtils.isEmpty(pageReqVO.getSerialNumber())
                || ObjectUtils.isEmpty(pageReqVO.getPartNumber())) {
            throw new IllegalArgumentException("导出失败：partName、partNumber、serialNumber 所有参数都不能为空");
        }
        // 获取数据
        List<?> mtbfData = dataService.getMtbfDataExport(pageReqVO);
        List<?> mtburData = dataService.getMtburDataExport(pageReqVO);
        // 构建区块（显式传入各自的真实 class）
        String partNumber = pageReqVO.getPartNumber();
        String fileName = "MTBF与MTBUR综合报告-" + partNumber + ".xml";
        List<XmlExportUtils.Section> sections = Arrays.asList(
                new XmlExportUtils.Section("mtbf", "平均故障间隔时间(MTBF)", mtbfData,
                        mtbfData.isEmpty() ? Object.class : mtbfData.get(0).getClass()),
                new XmlExportUtils.Section("mtbur", "平均非计划拆换时间(MTBUR)", mtburData,
                        mtburData.isEmpty() ? Object.class : mtburData.get(0).getClass())
        );
        // 通用导出
        XmlExportUtils.write(response, fileName, sections);
    }

    @GetMapping("/queryMtbfAndMtbur")
    @PreAuthorize("@ss.hasPermission('reliability:data:query')")
    public CommonResult<List<?>> queryMtbfAndMtbur(
            @RequestParam("type") String type,
            @Valid ReliabilityDataPageReqVO pageReqVO) {
        // 校验参数
        if (ObjectUtils.isEmpty(type)
                || !("mtbf".equalsIgnoreCase(type) || "mtbur".equalsIgnoreCase(type))
                || ObjectUtils.isEmpty(pageReqVO.getPartName())
                // || ObjectUtils.isEmpty(pageReqVO.getSerialNumber())
                || ObjectUtils.isEmpty(pageReqVO.getPartNumber())) {
            if (ObjectUtils.isEmpty(type)) {
                throw new IllegalArgumentException("查询失败：type 参数不能为空");
            } else if (!("mtbf".equalsIgnoreCase(type) || "mtbur".equalsIgnoreCase(type))) {
                throw new IllegalArgumentException("查询失败：type 参数必须为 'mtbf' 或 'mtbur'");
            } else {
                throw new IllegalArgumentException("查询失败：partName、partNumber、serialNumber 所有参数都不能为空");
            }
        }
        // 根据 type 调用不同的 Service 方法
        List<?> resultList;
        if ("mtbf".equalsIgnoreCase(type)) {
            resultList = dataService.getMtbfDataExport(pageReqVO);
        } else { // mtbur
            resultList = dataService.getMtburDataExport(pageReqVO);
        }
        // 返回结果
        return CommonResult.success(resultList);
    }

    @GetMapping("/getAll")
    public CommonResult<List<reliabilityDataFormNoVO>> getAll(@RequestParam(value = "reliabilityDataFormNo", required = false) String reliabilityDataFormNo) {
        List<reliabilityDataFormNoVO> data = dataService.getAll(reliabilityDataFormNo);
        return success(data);
    }

    @GetMapping("/getReliabilityByProcessInstanceId")
    public CommonResult<ReliabilityDataRespVO> getReliabilityByProcessInstanceId(@RequestParam("processInstanceId") String processInstanceId) {
        ReliabilityDataDO reliabilityData = dataService.getReliabilityByProcessInstanceId(processInstanceId);
        return success(BeanUtils.toBean(reliabilityData, ReliabilityDataRespVO.class));
    }

    @GetMapping("/pageByLevelAndIssue")
    @PreAuthorize("@ss.hasPermission('reliability:data:query')")
    public CommonResult<PageResult<FlightReliableDataMgrRespVO>> getDataPageForMgr(
            @Valid ReliabilityDataPageReqVO reqVO) {
        // 【可选条件】partName（模糊）、partNumber（精确）由前端传入
        PageResult<FlightReliableDataMgrRespVO> pageResult = dataService.getDataPageForMgrBySql(reqVO);
        return success(BeanUtils.toBean(pageResult, FlightReliableDataMgrRespVO.class));
    }

    @GetMapping("/statCurrentYear")
    public CommonResult<List<ReliabilityTrendRespVO>> statCurrentYearTrend() {
        return CommonResult.success(dataService.statReliabilityTrend());
    }

    @GetMapping({"/options/partNames", "/options/partNameOptions"})
    @PreAuthorize("@ss.hasPermission('reliability:data:query')")
    public CommonResult<List<Map<String,String>>> getPartNameOptions() {
        return success(dataService.getPartNameOptions());
    }

    @GetMapping({"/options/series", "/options/versions"})
    @PreAuthorize("@ss.hasPermission('reliability:data:query')")
    public CommonResult<List<String>> getSeriesOptionsByPartName(@RequestParam("partName") String partName,
                                                                 @RequestParam(name = "serialNumber", required = false) String serialNumber) {
        return success(dataService.getSeriesOptionsByPartName(partName, serialNumber));
    }

    @GetMapping({"/options/serialNumbers", "/options/serialNumberOptions"})
    @PreAuthorize("@ss.hasPermission('reliability:data:query')")
    public CommonResult<List<String>> getSerialNumberOptionsByPartName(@RequestParam("partName") String partName,
                                                                       @RequestParam(name = "series", required = false) String series
                                                                       ) {
        return success(dataService.getSerialNumberOptionsByPartName(partName, series));
    }

    @GetMapping({
            "/options/fillPartCodeAndDeliveryTime",
            "/options/getPartCodeAndDeliveryTime"
    })
    @PreAuthorize("@ss.hasPermission('reliability:data:query')")
    public CommonResult<ReliabilityDataPartCascadeFillRespVO> fillPartCodeAndDeliveryTime(
            @RequestParam("partName") String partName,
            @RequestParam("serialNumber") String serialNumber,
            @RequestParam(name = "series", required = false) String series,
            @RequestParam(name = "version", required = false) String version) {

        String effectiveSeries = (series != null && !series.trim().isEmpty()) ? series : version;
        return success(dataService.getPartCodeAndDeliveryTime(partName, effectiveSeries, serialNumber));
    }

    @GetMapping("/findSRU")
    public CommonResult<List<Map<String, String>>> findSRU(
            @RequestParam("partNumber") String partNumber,
            @RequestParam("serialNumber") String serialNumber,
            @RequestParam(name = "version") String version) {
        return success(dataService.findSRU(partNumber, version, serialNumber));
    }

}
