package cn.iocoder.yudao.module.data.controller.admin.trainingattendanceassessment;

import cn.hutool.core.bean.BeanUtil;
import cn.iocoder.yudao.module.data.controller.admin.uploadzippreview.service.impl.AttachmentService;
import cn.iocoder.yudao.module.data.dal.dataobject.trainingplan.TrainingPlanDO;
import cn.iocoder.yudao.module.data.service.trainingplan.TrainingPlanService;
import cn.iocoder.yudao.module.training.dal.dataobject.trainingexperience.FileUploadDO;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;

import javax.validation.*;
import javax.servlet.http.*;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.io.IOException;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;



import cn.iocoder.yudao.module.data.controller.admin.trainingattendanceassessment.vo.*;
import cn.iocoder.yudao.module.data.dal.dataobject.trainingattendanceassessment.TrainingAttendanceAssessmentDO;
import cn.iocoder.yudao.module.data.service.trainingattendanceassessment.TrainingAttendanceAssessmentService;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/data/training-attendance-assessment")
@Validated
public class TrainingAttendanceAssessmentController {

    @Resource
    private TrainingAttendanceAssessmentService trainingAttendanceAssessmentService;
    @Autowired
    private AttachmentService attachmentService;
    @Resource
    private TrainingPlanService trainingPlanService;
    // 正则表达式：匹配JSON数组中的字符串（如["333","334"]中的333、334）
    private static final java.util.regex.Pattern JSON_ARRAY_PATTERN = Pattern.compile("\"([^\"]+)\"");

    @PostMapping("/create")
    @PreAuthorize("@ss.hasPermission('data:training-attendance-assessment:create')")
    public CommonResult<Integer> createTrainingAttendanceAssessment(@Valid @RequestBody TrainingAttendanceAssessmentSaveReqVO createReqVO) {
        return success(trainingAttendanceAssessmentService.createTrainingAttendanceAssessment(createReqVO));
    }

    @PutMapping("/update")
    @PreAuthorize("@ss.hasPermission('data:training-attendance-assessment:update')")
    public CommonResult<Boolean> updateTrainingAttendanceAssessment(@Valid @RequestBody TrainingAttendanceAssessmentSaveReqVO updateReqVO) {
        trainingAttendanceAssessmentService.updateTrainingAttendanceAssessment(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @PreAuthorize("@ss.hasPermission('data:training-attendance-assessment:delete')")
    public CommonResult<Boolean> deleteTrainingAttendanceAssessment(@RequestParam("id") Integer id) {
        trainingAttendanceAssessmentService.deleteTrainingAttendanceAssessment(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @PreAuthorize("@ss.hasPermission('data:training-attendance-assessment:delete')")
    public CommonResult<Boolean> deleteTrainingAttendanceAssessmentList(@RequestParam("ids") List<Integer> ids) {
        trainingAttendanceAssessmentService.deleteTrainingAttendanceAssessmentListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('data:training-attendance-assessment:query')")
    public CommonResult<TrainingAttendanceAssessmentRespVO> getTrainingAttendanceAssessment(@RequestParam("id") String id) {
        TrainingAttendanceAssessmentDO trainingAttendanceAssessment = trainingAttendanceAssessmentService.getTrainingAttendanceAssessment(id);
        return success(BeanUtils.toBean(trainingAttendanceAssessment, TrainingAttendanceAssessmentRespVO.class));
    }

    @GetMapping("/page")
//    @PreAuthorize("@ss.hasPermission('data:training-attendance-assessment:query')")
    public CommonResult<PageResult<TrainingAttendanceAssessmentRespVO>> getTrainingAttendanceAssessmentPage(@Valid TrainingAttendanceAssessmentPageReqVO pageReqVO) {
        // 1. 查询考核记录分页数据
        PageResult<TrainingAttendanceAssessmentDO> pageResult = trainingAttendanceAssessmentService.getTrainingAttendanceAssessmentPage(pageReqVO);

        // 2. 收集所有有效的附件ID（去重+空值处理）
        List<Integer> allFileUploadIds = new ArrayList<>();
        for (TrainingAttendanceAssessmentDO assessmentDO : pageResult.getList()) {
            String fileUploadIdStr = assessmentDO.getFileUploadId();
            if (fileUploadIdStr == null || fileUploadIdStr.trim().isEmpty()) {
                continue;
            }
            List<String> idStrList = parseFileUploadId(fileUploadIdStr);
            for (String idStr : idStrList) {
                try {
                    Integer id = Integer.parseInt(idStr.trim());
                    allFileUploadIds.add(id);
                } catch (NumberFormatException e) {
                    System.out.println("附件ID转换失败: " + idStr); // 实际项目中应使用logger
                }
            }
        }
        // === 新增：收集所有唯一的 processInstanceId ===
        Set<String> processInstanceIds = pageResult.getList().stream()
                .map(TrainingAttendanceAssessmentDO::getProcessInstanceId)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
// === 新增：批量查询培训计划信息 ===
        Map<String, TrainingPlanDO> planMap;
        if (processInstanceIds.isEmpty()) {
            planMap = Collections.emptyMap();
        } else {
            List<TrainingPlanDO> plans = trainingAttendanceAssessmentService.getTrainingPlansByProcessInstanceIds(processInstanceIds);
            planMap = plans.stream()
                    .collect(Collectors.toMap(TrainingPlanDO::getProcessInstanceId, Function.identity(), (v1, v2) -> v1));
        }

        // 3. 批量查询附件信息并构建ID映射
        // 直接初始化为不可变空map，避免后续修改
        Map<Integer, FileUploadDO> fileUploadMap = allFileUploadIds.isEmpty() ?
                Collections.emptyMap() :
                attachmentService.getFileUploadResultsByIds(allFileUploadIds).stream()
                        .collect(Collectors.toMap(FileUploadDO::getId, Function.identity(), (v1, v2) -> v1));

        // === 修改步骤 4：转换 DO → VO 时，同时注入 trainingPlan 字段 ===
        List<TrainingAttendanceAssessmentRespVO> respVOList = pageResult.getList().stream()
                .map(assessmentDO -> {
                    TrainingAttendanceAssessmentRespVO respVO = BeanUtil.toBean(assessmentDO, TrainingAttendanceAssessmentRespVO.class);

                    // ===== 注入 trainingPlan 相关字段 =====
                    String pid = assessmentDO.getProcessInstanceId();
                    if (StringUtils.isNotBlank(pid)) {
                        TrainingPlanDO plan = planMap.get(pid);
                        if (plan != null) {
                            respVO.setTrainingTheme(plan.getTrainingTheme());
                            respVO.setTrainingLocation(plan.getTrainingLocation());
                            respVO.setTrainingStartTime(plan.getTrainingStartTime());
                            respVO.setTrainingEndTime(plan.getTrainingEndTime());
                        }
                    }

                    // ===== 原有附件处理逻辑（保持不变） =====
                    List<FileUploadDO> currentFileUploads = new ArrayList<>();
                    String fileUploadIdStr = assessmentDO.getFileUploadId();
                    if (StringUtils.isNotBlank(fileUploadIdStr)) {
                        List<String> currentFileUploadIds = parseFileUploadId(fileUploadIdStr);
                        for (String idStr : currentFileUploadIds) {
                            try {
                                Integer attachId = Integer.valueOf(idStr.trim());
                                FileUploadDO fileUploadDO = fileUploadMap.get(attachId);
                                if (fileUploadDO != null) {
                                    currentFileUploads.add(fileUploadDO);
                                }
                            } catch (NumberFormatException e) {
                                System.out.println("考核ID " + assessmentDO.getId() + " 的附件ID " + idStr + " 转换失败");
                            }
                        }
                    }
                    respVO.setFileUploadResults(currentFileUploads);

                    return respVO;
                })
                .collect(Collectors.toList());

        // 5. 构建分页结果返回
        PageResult<TrainingAttendanceAssessmentRespVO> result = new PageResult<>();
        result.setTotal(pageResult.getTotal());
        result.setList(respVOList);

        return CommonResult.success(result);
    }

    @GetMapping("/export-excel")
    @PreAuthorize("@ss.hasPermission('data:training-attendance-assessment:export')")
    public void exportTrainingAttendanceAssessmentExcel(@Valid TrainingAttendanceAssessmentPageReqVO pageReqVO,
                                                        HttpServletResponse response) throws IOException {

        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<TrainingAttendanceAssessmentDO> list = trainingAttendanceAssessmentService.getTrainingAttendanceAssessmentPage(pageReqVO).getList();
        List<TrainingAttendanceAssessmentRespVO> respVOList = getTrainingAttendanceAssessmentRespVOS(list);
        // === 4. 导出 Excel ===
        ExcelUtils.write(response, "培训签到考核证书.xlsx", "数据", TrainingAttendanceAssessmentRespVO.class, respVOList);
    }

    @GetMapping("/getTrainingAttendanceAssessmentByProcessInstanceId")
//    @PreAuthorize("@ss.hasPermission('data:training-attendance-assessment:query')")
    public CommonResult<List<TrainingAttendanceAssessmentRespVO>> getTrainingAttendanceAssessmentByProcessInstanceId(@RequestParam("processInstanceId") String processInstanceId) {
        List<TrainingAttendanceAssessmentDO> trainingAttendanceAssessmentByProcessInstanceId = trainingAttendanceAssessmentService.getTrainingAttendanceAssessmentByProcessInstanceId(processInstanceId);
        return success(BeanUtils.toBean(trainingAttendanceAssessmentByProcessInstanceId, TrainingAttendanceAssessmentRespVO.class));
    }

    @GetMapping("/getTrainingAttendanceAssessmentByIds")
    public CommonResult<List<TrainingAttendanceAssessmentRespVO>> getTrainingAttendanceAssessmentByIds(@RequestParam("processInstanceId") String processInstanceId) {
        List<TrainingAttendanceAssessmentDO> dos = trainingAttendanceAssessmentService.getTrainingAttendanceAssessmentByIds(processInstanceId);

        List<TrainingAttendanceAssessmentRespVO> vos = dos.stream()
                .map(doItem -> {
                    TrainingAttendanceAssessmentRespVO vo = BeanUtil.toBean(doItem, TrainingAttendanceAssessmentRespVO.class);
                    // 手动解析附件ID字符串 → List<String>
                    String fileUploadIdStr = doItem.getTraningAttachments();
                    vo.setTraningAttachments(parseFileUploadId(fileUploadIdStr));
                    return vo;
                })
                .collect(Collectors.toList());

        return success(vos);
    }

    @PostMapping("/parse-excel")
    public CommonResult<List<TrainingAttendanceAssessmentExcelParseRowVO>> parseTrainingAttendanceExcel(
            @RequestParam("file") MultipartFile file) {
        return success(trainingAttendanceAssessmentService.parseTrainingAttendanceExcel(file));
    }

    @GetMapping("/get-import-template")
    public void importTemplate (HttpServletResponse response) throws IOException {
// 1. 创建 Excel 工作簿
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("培训签到考核导入模板");

        // 2. 设置列宽（不含教员列）
        sheet.setColumnWidth(0, 15 * 256);  // 姓名
        sheet.setColumnWidth(1, 20 * 256);  // 签到时间
        sheet.setColumnWidth(2, 15 * 256);  // 学习时长
        sheet.setColumnWidth(3, 15 * 256);  // 是否完成学习
        sheet.setColumnWidth(4, 15 * 256);  // 完成形式
        sheet.setColumnWidth(5, 10 * 256);  // 成绩
        sheet.setColumnWidth(6, 30 * 256);  // 获得证书
        sheet.setColumnWidth(7, 20 * 256);  // 完成日期
        sheet.setColumnWidth(8, 20 * 256);  // 备注
        sheet.setColumnWidth(9, 40 * 256); // 提示信息

        // 3. 创建表头样式（灰色背景）
        XSSFCellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        XSSFFont headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        // 4. 创建红色字体样式（用于提示信息）
        XSSFCellStyle redFontStyle = workbook.createCellStyle();
        XSSFFont redFont = workbook.createFont();
        redFont.setColor(IndexedColors.RED.getIndex());
        redFont.setBold(true);
        redFont.setFontHeightInPoints((short) 10);
        redFontStyle.setFont(redFont);

        // 5. 创建普通单元格样式
        XSSFCellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.LEFT);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // 6. 创建表头行（第 1 行）- 与 TrainingAttendanceAssessmentImportExcelVO 字段一致
        XSSFRow headerRow = sheet.createRow(0);
        String[] headers = {
                "姓名", "签到时间", "学习时长", "是否完成学习", "完成形式",
                "成绩", "获得证书", "完成日期", "备注"
        };
        for (int i = 0; i < headers.length; i++) {
            XSSFCell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // 7. 在最后一列添加红色提示文字
        XSSFCell tipCell = headerRow.createCell(9);
        tipCell.setCellValue("第一行表头请保留，0已完成1未完成");
        tipCell.setCellStyle(redFontStyle);

        // 8. 添加测试数据（第 2 行）
        XSSFRow dataRow1 = sheet.createRow(1);
        String[] data1 = {
                "白小白", "2026-03-25 00:00:00", "120", "0", "理论考试",
                "85", "", "2026-03-26", "测试数据 1"
        };
        for (int i = 0; i < data1.length; i++) {
            XSSFCell cell = dataRow1.createCell(i);
            cell.setCellValue(data1[i]);
            cell.setCellStyle(cellStyle);
        }

        // 9. 添加测试数据（第 3 行）
        XSSFRow dataRow2 = sheet.createRow(2);
        String[] data2 = {
                "张三", "2026-03-27 00:00:00", "90", "1", "实操考试",
                "0", "", "2026-03-28", "测试数据 2"
        };
        for (int i = 0; i < data2.length; i++) {
            XSSFCell cell = dataRow2.createCell(i);
            cell.setCellValue(data2[i]);
            cell.setCellStyle(cellStyle);
        }

        // 10. 设置响应头（修复中文文件名编码问题）
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = "培训签到考核导入模板.xlsx";
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()).replace("+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);

        // 11. 输出 Excel
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    /**
     * 解析数据库中的附件ID字符串为 List<String>
     * 支持两种格式：
     * - JSON 数组: "[\"1\",\"2\"]"
     * - 逗号分隔: "1,2,3"
     *
     * @param idStr 数据库中的原始字符串（如 "[\"1\",\"2\"]" 或 "1,2,3" 或 null）
     * @return 解析后的 List，空值/格式错误返回空 List
     */
    private List<String> parseFileUploadId(String idStr) {
        List<String> result = new ArrayList<>();
        if (StringUtils.isBlank(idStr) || "null".equalsIgnoreCase(idStr)) {
            return result;
        }

        // 优先尝试 JSON 数组格式（含双引号）
        Matcher jsonMatcher = JSON_ARRAY_PATTERN.matcher(idStr);
        if (jsonMatcher.find()) {
            // 是 JSON 数组格式，重置 matcher 并提取所有
            jsonMatcher.reset();
            while (jsonMatcher.find()) {
                String id = jsonMatcher.group(1);
                if (StringUtils.isNotBlank(id)) {
                    result.add(id);
                }
            }
            return result;
        }

        // 否则尝试逗号分隔格式
        String[] parts = idStr.split(",");
        for (String part : parts) {
            String trimmed = part.trim();
            if (StringUtils.isNotBlank(trimmed)) {
                result.add(trimmed);
            }
        }

        return result;
    }

    private List<TrainingAttendanceAssessmentRespVO> getTrainingAttendanceAssessmentRespVOS(List<TrainingAttendanceAssessmentDO> list) {
        // 1. 收集 processInstanceId
        Set<String> processInstanceIds = list.stream()
                .map(TrainingAttendanceAssessmentDO::getProcessInstanceId)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());

        // 2. 批量查询 TrainingPlanDO
        Map<String, TrainingPlanDO> planMap = processInstanceIds.isEmpty()
                ? Collections.emptyMap()
                : trainingPlanService.getTrainingPlansByProcessInstanceIds(processInstanceIds).stream()
                .collect(Collectors.toMap(TrainingPlanDO::getProcessInstanceId, Function.identity(), (v1, v2) -> v1));

        // 3. === 新增：收集所有文件 ID ===
        Set<Integer> allFileUploadIds = new HashSet<>();
        for (TrainingAttendanceAssessmentDO assessmentDO : list) {
            String fileUploadIdStr = assessmentDO.getFileUploadId();
            if (StringUtils.isNotBlank(fileUploadIdStr)) {
                List<String> idStrList = parseFileUploadId(fileUploadIdStr);
                for (String idStr : idStrList) {
                    try {
                        allFileUploadIds.add(Integer.parseInt(idStr.trim()));
                    } catch (NumberFormatException e) {
                        // 忽略转换失败的 ID
                    }
                }
            }
        }

        // 4. === 新增：批量查询文件信息 ===
        Map<Integer, FileUploadDO> fileUploadMap = allFileUploadIds.isEmpty()
                ? Collections.emptyMap()
                : attachmentService.getFileUploadResultsByIds(new ArrayList<>(allFileUploadIds)).stream()
                .collect(Collectors.toMap(FileUploadDO::getId, Function.identity(), (v1, v2) -> v1));

        // 5. 转换 DO → VO
        List<TrainingAttendanceAssessmentRespVO> respVOList = list.stream()
                .map(assessmentDO -> {
                    TrainingAttendanceAssessmentRespVO respVO = BeanUtil.toBean(
                            assessmentDO,
                            TrainingAttendanceAssessmentRespVO.class);

                    // 注入培训计划信息
                    String pid = assessmentDO.getProcessInstanceId();
                    if (StringUtils.isNotBlank(pid)) {
                        TrainingPlanDO plan = planMap.get(pid);
                        if (plan != null) {
                            respVO.setTrainingTheme(plan.getTrainingTheme());
                            respVO.setTrainingLocation(plan.getTrainingLocation());
                            respVO.setTrainingStartTime(plan.getTrainingStartTime());
                            respVO.setTrainingEndTime(plan.getTrainingEndTime());
                        }
                    }

                    // === 新增：赋值 receiveCertificate（证书名称）===
                    String fileUploadIdStr = assessmentDO.getFileUploadId();
                    if (StringUtils.isNotBlank(fileUploadIdStr)) {
                        List<String> idStrList = parseFileUploadId(fileUploadIdStr);
                        List<String> certNames = new ArrayList<>();
                        for (String idStr : idStrList) {
                            try {
                                Integer fileId = Integer.parseInt(idStr.trim());
                                FileUploadDO fileUploadDO = fileUploadMap.get(fileId);
                                if (fileUploadDO != null) {
                                    // 使用文件名作为证书名称
                                    certNames.add(fileUploadDO.getFileName());
                                }
                            } catch (NumberFormatException e) {
                                // 忽略转换失败的 ID
                            }
                        }
                        // 多个证书名称用逗号分隔
                        if (!certNames.isEmpty()) {
                            respVO.setReceiveCertificate(String.join(",", certNames));
                        }
                    }

                    // 转换 isCompleted 为中文描述
                    if (respVO.getIsCompleted() != null) {
                        respVO.setIsCompletedDesc(respVO.getIsCompleted() == 0 ? "是" : "否");
                    }

                    // 转换 completedForm 为中文描述
                    if (respVO.getCompletedForm() != null) {
                        if (respVO.getCompletedForm() == 1) {
                            respVO.setCompletedFormDesc("理论考试");
                        } else if (respVO.getCompletedForm() == 2) {
                            respVO.setCompletedFormDesc("实操考试");
                        }
                    }

                    return respVO;
                })
                .collect(Collectors.toList());

        return respVOList;
    }

}
