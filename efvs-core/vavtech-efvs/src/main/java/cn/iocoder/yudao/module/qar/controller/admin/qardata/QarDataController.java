package cn.iocoder.yudao.module.qar.controller.admin.qardata;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;

import javax.validation.*;
import javax.servlet.http.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.io.IOException;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;



import cn.iocoder.yudao.module.qar.controller.admin.qardata.vo.*;
import cn.iocoder.yudao.module.qar.dal.dataobject.qardata.QarDataDO;
import cn.iocoder.yudao.module.qar.service.qardata.QarDataService;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/qar/data")
@Validated
public class QarDataController {

    @Resource
    private QarDataService dataService;

    @PostMapping("/create")
    @PreAuthorize("@ss.hasPermission('qar:data:create')")
    public CommonResult<Integer> createData(@Valid @RequestBody QarDataSaveReqVO createReqVO) {
        return success(dataService.createData(createReqVO));
    }

    @PutMapping("/update")
    @PreAuthorize("@ss.hasPermission('qar:data:update')")
    public CommonResult<Boolean> updateData(@Valid @RequestBody QarDataSaveReqVO updateReqVO) {
        dataService.updateData(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @PreAuthorize("@ss.hasPermission('qar:data:delete')")
    public CommonResult<Boolean> deleteData(@RequestParam("id") Integer id) {
        dataService.deleteData(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @PreAuthorize("@ss.hasPermission('qar:data:delete')")
    public CommonResult<Boolean> deleteDataList(@RequestParam("ids") List<Integer> ids) {
        dataService.deleteDataListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('qar:data:query')")
    public CommonResult<QarDataRespVO> getData(@RequestParam("id") Integer id) {
        QarDataDO data = dataService.getData(id);
        return success(BeanUtils.toBean(data, QarDataRespVO.class));
    }

    @GetMapping("/page")
    @PreAuthorize("@ss.hasPermission('qar:data:query')")
    public CommonResult<PageResult<QarDataRespVO>> getQarDataPage(@Valid QarDataPageReqVO pageReqVO) {
        PageResult<QarDataDO> pageResult = dataService.getDataPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, QarDataRespVO.class));
    }

    @GetMapping("/exportQarDataExcel")
    @PreAuthorize("@ss.hasPermission('qar:data:export')")
    public void exportQarDataExcel(@Valid QarDataPageReqVO pageReqVO,
                                HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<QarDataDO> list = dataService.getDataPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "QAR数据.xls", "数据", QarDataRespVO.class,
                BeanUtils.toBean(list, QarDataRespVO.class));
    }

    /**
     * 下载QAR数据导入模板
     */
    @GetMapping("/download-template")
    @PreAuthorize("@ss.hasPermission('qar:data:export')")
    public void downloadQarMappingTemplate(HttpServletResponse response) throws IOException {
        dataService.downloadQRAMappingTemplate(response);
    }

//    /**
//     * 上传 Excel 文件并导入数据库
//     *
//     * @param file Excel 文件 (.csv)
//     * @return 导入结果信息
//     */
//    @PostMapping(value = "/excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    @Operation(summary = "上传 Excel 并导入数据库", description = "将 Excel 文件转 CSV 并使用 LOAD DATA 导入 qar_data 表")
//    @PreAuthorize("@ss.hasPermission('qar:data:import')")
//    public ResponseEntity<String> importQarDataToDb(@RequestParam("file") MultipartFile file,
//                                                    //@RequestParam("airlineQar") String airlineQar,
//                                                    //@RequestParam("attachmentName") String attachmentName,
//                                                    @RequestParam("isEnable") String isEnable) {
//        // 添加启用状态检查
//        if ("0".equals(isEnable)) {
//            return ResponseEntity.badRequest().body("导入功能未启用");
//        }
//        if ("1".equals(isEnable)) {
//            if (file == null || file.isEmpty()) {
//                return ResponseEntity.badRequest().body("文件为空，请选择 Excel 文件上传！");
//            }
//
//            LocalDateTime startTime = LocalDateTime.now();
//            System.out.println("导入开始时间：" + startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
//
//            try {
//                // 调用 Service 返回导入总条数
//                int totalCount = dataService.importCsvViaLoadData(file);
//                //int totalCount = dataService.importCsvViaLoadData(file, airlineQar, attachmentName);
//
//                LocalDateTime endTime = LocalDateTime.now();
//                System.out.println("导入结束时间：" + endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
//
//                Duration duration = Duration.between(startTime, endTime);
//                System.out.println("导入耗时：" + duration.toMillis() + " 毫秒");
//                System.out.println("导入总条数：" + totalCount);
//
//                String message = String.format(
//                        "导入成功！\n总条数：%d\n导入开始：%s\n导入结束：%s\n耗时：%d 毫秒",
//                        totalCount,
//                        startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
//                        endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
//                        duration.toMillis()
//                );
//
//                return ResponseEntity.ok(message);
//            } catch (Exception e) {
//                e.printStackTrace();
//                return ResponseEntity.badRequest().body("导入失败：" + e.getMessage());
//            }
//        }
//        return ResponseEntity.badRequest().body("无效的启用状态参数");
//    }

    /**
     * 批量上传 CSV 文件并导入数据库
     *
     * 不再上传映射模板文件，固定从 resources/template/QAR数据_模版文件.xlsx 读取映射关系。
     *
     * @param csvFiles CSV文件列表（最多20个）
     * @return 导入结果信息
     */
    @PostMapping(value = "/batch-cvs", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> batchImportQarDataToDb(
            @RequestParam("csvFiles") List<MultipartFile> csvFiles) {
        if (csvFiles == null || csvFiles.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "CSV文件列表为空，请选择CSV文件上传！");
            return ResponseEntity.badRequest().body(error);
        }

        if (csvFiles.size() > 20) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "最多只能上传20个CSV文件！");
            return ResponseEntity.badRequest().body(error);
        }

        LocalDateTime startTime = LocalDateTime.now();
        System.out.println("批量导入开始时间：" + startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        try {
            // 调用 Service 返回导入结果（内部从固定模板读取映射关系）
            Map<String, Object> result = dataService.batchImportCsvViaLoadData(csvFiles);

            LocalDateTime endTime = LocalDateTime.now();
            System.out.println("批量导入结束时间：" + endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            Duration duration = Duration.between(startTime, endTime);
            System.out.println("批量导入耗时：" + duration.toMillis() + " 毫秒");

            // 添加导入时间信息到结果
            result.put("startTime", startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            result.put("endTime", endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            result.put("duration", duration.toMillis() + " 毫秒");
            result.put("success", true);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "导入失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/taxi-phase-cycles")
    public CommonResult<List<TaxiPhaseCycleVO>> getTaxiPhaseCycles() {
        List<TaxiPhaseCycleVO> cycles = dataService.getTaxiPhaseCycles();
        return success(cycles);
    }
}
