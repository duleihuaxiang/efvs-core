package cn.iocoder.yudao.module.data.service.trainingattendanceassessment;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.data.dal.dataobject.pretrainingpreparation.PreTrainingPreparationDO;
import cn.iocoder.yudao.module.data.dal.dataobject.trainingplan.TrainingPlanDO;
import cn.iocoder.yudao.module.data.dal.mysql.trainingplan.TrainingPlanMapper;
import cn.iocoder.yudao.module.training.dal.dataobject.trainingexperience.FileUploadDO;
import cn.iocoder.yudao.module.training.dal.mysql.trainingexperience.FileUploadMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import cn.iocoder.yudao.module.data.controller.admin.trainingattendanceassessment.vo.*;
import cn.iocoder.yudao.module.data.dal.dataobject.trainingattendanceassessment.TrainingAttendanceAssessmentDO;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.data.dal.mysql.trainingattendanceassessment.TrainingAttendanceAssessmentMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception0;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.diffList;
import static cn.iocoder.yudao.module.data.enums.ErrorCodeConstants.IMPORT_FAILED;
import static cn.iocoder.yudao.module.data.enums.ErrorCodeConstants.TRAINING_ATTENDANCE_ASSESSMENT_NOT_EXISTS;

/**
 * 培训签到考核证书 Service 实现类
 *
 * @author admin
 */
@Service
@Validated
public class TrainingAttendanceAssessmentServiceImpl extends ServiceImpl<TrainingAttendanceAssessmentMapper, TrainingAttendanceAssessmentDO> implements TrainingAttendanceAssessmentService {

    @Resource
    private TrainingAttendanceAssessmentMapper trainingAttendanceAssessmentMapper;

    @Resource
    private TrainingPlanMapper trainingPlanMapper;

    @Resource
    private FileUploadMapper fileUploadMapper;

    // 正则表达式：匹配JSON数组中的字符串（如["333","334"]中的333、334）
    private static final Pattern JSON_ARRAY_PATTERN = Pattern.compile("\"([^\"]+)\"");
    @Override
    public Integer createTrainingAttendanceAssessment(TrainingAttendanceAssessmentSaveReqVO createReqVO) {
        // 插入
        TrainingAttendanceAssessmentDO trainingAttendanceAssessment = BeanUtils.toBean(createReqVO, TrainingAttendanceAssessmentDO.class);
        trainingAttendanceAssessmentMapper.insert(trainingAttendanceAssessment);

        // 返回
        return trainingAttendanceAssessment.getId();
    }

    @Override
    public void updateTrainingAttendanceAssessment(TrainingAttendanceAssessmentSaveReqVO updateReqVO) {
        // 校验存在
        validateTrainingAttendanceAssessmentExists(updateReqVO.getId());
        // 更新
        TrainingAttendanceAssessmentDO updateObj = BeanUtils.toBean(updateReqVO, TrainingAttendanceAssessmentDO.class);
        trainingAttendanceAssessmentMapper.updateById(updateObj);
    }

    @Override
    public void deleteTrainingAttendanceAssessment(Integer id) {
        // 校验存在
        validateTrainingAttendanceAssessmentExists(id);
        // 删除
        trainingAttendanceAssessmentMapper.deleteById(id);
    }

    @Override
    public void deleteTrainingAttendanceAssessmentListByIds(List<Integer> ids) {
        // 删除
        trainingAttendanceAssessmentMapper.deleteByIds(ids);
        }

    private void validateTrainingAttendanceAssessmentExists(Integer id) {
        if (trainingAttendanceAssessmentMapper.selectById(id) == null) {
            throw exception(TRAINING_ATTENDANCE_ASSESSMENT_NOT_EXISTS);
        }
    }

    @Override
    public TrainingAttendanceAssessmentDO getTrainingAttendanceAssessment(String id) {
        return trainingAttendanceAssessmentMapper.selectById(id);
    }

    @Override
    public PageResult<TrainingAttendanceAssessmentDO> getTrainingAttendanceAssessmentPage(TrainingAttendanceAssessmentPageReqVO pageReqVO) {
        // 如果按培训主题或培训地点筛选，先查询培训计划表获取对应的 processInstanceIds
        boolean hasTrainingFilter = StringUtils.isNotBlank(pageReqVO.getTrainingTheme())
                || StringUtils.isNotBlank(pageReqVO.getTrainingLocation());

        if (hasTrainingFilter) {
            List<TrainingPlanDO> plans = trainingPlanMapper.selectList(new LambdaQueryWrapperX<TrainingPlanDO>()
                    .likeIfPresent(TrainingPlanDO::getTrainingTheme, pageReqVO.getTrainingTheme())
                    .likeIfPresent(TrainingPlanDO::getTrainingLocation, pageReqVO.getTrainingLocation())
                    .select(TrainingPlanDO::getProcessInstanceId));

            List<String> processInstanceIds = plans.stream()
                    .map(TrainingPlanDO::getProcessInstanceId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (processInstanceIds.isEmpty()) {
                return PageResult.empty();
            }

            // 使用 MyBatis-Plus 内置分页方法，额外加上 processInstanceId 筛选
            LambdaQueryWrapperX<TrainingAttendanceAssessmentDO> wrapper = new LambdaQueryWrapperX<TrainingAttendanceAssessmentDO>()
                    .likeIfPresent(TrainingAttendanceAssessmentDO::getName, pageReqVO.getName())
                    .likeIfPresent(TrainingAttendanceAssessmentDO::getCourseName, pageReqVO.getCourseName())
                    .likeIfPresent(TrainingAttendanceAssessmentDO::getInstructor, pageReqVO.getInstructor())
                    .in(TrainingAttendanceAssessmentDO::getProcessInstanceId, processInstanceIds)
                    .betweenIfPresent(TrainingAttendanceAssessmentDO::getCheckInTime, pageReqVO.getCheckInTime())
                    .eqIfPresent(TrainingAttendanceAssessmentDO::getCompletedForm, pageReqVO.getCompletedForm())
                    .eqIfPresent(TrainingAttendanceAssessmentDO::getScore, pageReqVO.getScore())
                    .eqIfPresent(TrainingAttendanceAssessmentDO::getReceiveCertificate, pageReqVO.getReceiveCertificate())
                    .orderByDesc(TrainingAttendanceAssessmentDO::getId);
            return trainingAttendanceAssessmentMapper.selectPage(pageReqVO, wrapper);
        }

        return trainingAttendanceAssessmentMapper.selectPage(pageReqVO);
    }

    @Override
    public List<TrainingAttendanceAssessmentDO> getTrainingAttendanceAssessmentByProcessInstanceId(String processInstanceId) {
        return trainingAttendanceAssessmentMapper.selectList(new QueryWrapper<TrainingAttendanceAssessmentDO>().eq("process_instance_id", processInstanceId));
    }

    /**
     * 解析数据库中的file_upload_id字符串为List<String>
     * @param jsonStr 数据库中的原始字符串（如["333","334"]或null）
     * @return 解析后的List，空值/格式错误返回空List
     */
    private List<String> parseFileUploadId(String jsonStr) {
        List<String> result = new ArrayList<>();
        // 1. 空值判断：null/空字符串/纯"null"都返回空List
        if (StringUtils.isBlank(jsonStr) || "null".equalsIgnoreCase(jsonStr)) {
            return result;
        }
        // 2. 正则匹配：提取所有双引号内的内容
        Matcher matcher = JSON_ARRAY_PATTERN.matcher(jsonStr);
        while (matcher.find()) {
            String id = matcher.group(1); // 拿到双引号内的纯字符串（如333）
            if (StringUtils.isNotBlank(id)) {
                result.add(id);
            }
        }
        return result;
    }
    @Override
    public List<TrainingAttendanceAssessmentDO> getTrainingAttendanceAssessmentByIds(String processInstanceId) {
        // 2. 查询考核表主数据
        List<TrainingAttendanceAssessmentDO> assessmentList = trainingAttendanceAssessmentMapper.getTrainingAttendanceAssessmentByIds(processInstanceId);
        if (CollectionUtil.isEmpty(assessmentList)) {
//            log.info("根据ID列表{}未查询到考核数据", integerIds);
            return new ArrayList<>();
        }
        // 3. 收集所有附件ID（去重，避免重复查询）
        List<Integer> allFileUploadIds = new ArrayList<>();
        for (TrainingAttendanceAssessmentDO assessmentDO : assessmentList) {
            // 空值处理：避免fileUploadId为null导致空指针
            if (assessmentDO.getFileUploadId() == null) {
                continue;
            }
            // 解析为List<String>
            List<String> fileUploadIdList = parseFileUploadId(assessmentDO.getFileUploadId());
            for (String idStr : fileUploadIdList) {
                try {
                    // 字符串转Integer，异常时记录日志并跳过
                    allFileUploadIds.add(Integer.valueOf(idStr));
                } catch (NumberFormatException e) {
                    throw new RuntimeException("附件ID转换失败");
//                    log.error("附件ID转换失败，ID字符串：{}，考核ID：{}", idStr, assessmentDO.getId(), e);
                }
            }
        }

        // 4. 批量查询附件数据（仅1次数据库查询，高性能）
        Map<Integer, FileUploadDO> fileUploadMap = new HashMap<>();
        if (CollectionUtil.isNotEmpty(allFileUploadIds)) {
            // 去重：避免重复查询相同附件ID
            Set<Integer> distinctFileUploadIds = new HashSet<>(allFileUploadIds);
            // 批量查询附件
            List<FileUploadDO> fileUploadList = fileUploadMapper.selectListByFileUploadIds(distinctFileUploadIds);
            // 转换为ID->附件DO的Map，方便快速匹配
            fileUploadMap = fileUploadList.stream()
                    .collect(Collectors.toMap(FileUploadDO::getId, fileUploadDO -> fileUploadDO, (a, b) -> a));
        }

        // 5. 给每个考核对象赋值附件列表
        for (TrainingAttendanceAssessmentDO assessmentDO : assessmentList) {
            // 初始化附件列表（避免null）
            List<FileUploadDO> fileUploadResults = new ArrayList<>();
            // 空值处理：获取当前考核的附件ID列表
            if (assessmentDO.getFileUploadId() == null) {
                continue;
            }
            // 解析为List<String>
            List<String> currentFileUploadIds = parseFileUploadId(assessmentDO.getFileUploadId());
            for (String idStr : currentFileUploadIds) {
                try {
                    Integer attachId = Integer.valueOf(idStr);
                    // 从Map中获取附件DO，非空则添加
                    FileUploadDO fileUploadDO = fileUploadMap.get(attachId);
                    if (fileUploadDO != null) {
                        fileUploadResults.add(fileUploadDO);
                    }
                } catch (NumberFormatException e) {
                    throw new RuntimeException("附件ID转换失败2");
//                    log.error("考核ID{}的附件ID{}转换失败", assessmentDO.getId(), idStr, e);
                }
            }

            // 赋值附件列表到考核对象
            assessmentDO.setFileUploadResults(fileUploadResults);
        }

        // 6. 返回赋值后的完整列表
        return assessmentList;
    }

    @Override
    public List<TrainingPlanDO> getTrainingPlansByProcessInstanceIds(Collection<String> processInstanceIds) {

        return trainingPlanMapper.selectList(new QueryWrapper<TrainingPlanDO>().in("process_instance_id", processInstanceIds));
    }

    @Override
    public List<TrainingAttendanceAssessmentExcelParseRowVO> parseTrainingAttendanceExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw exception0(IMPORT_FAILED.getCode(), "导入失败：{}", "请上传 Excel 文件");
        }
        try (InputStream in = file.getInputStream(); Workbook workbook = WorkbookFactory.create(in)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                throw exception0(IMPORT_FAILED.getCode(), "导入失败：{}", "Excel 中没有任何工作表");
            }
            final int headerRowIndex = 0;
            Row headerRow = sheet.getRow(headerRowIndex);
            if (headerRow == null) {
                throw exception0(IMPORT_FAILED.getCode(), "导入失败：{}", "第一行表头为空");
            }
            DataFormatter formatter = new DataFormatter();
            Map<String, Integer> headerIndex = buildHeaderIndex(headerRow, formatter);

            List<TrainingAttendanceAssessmentExcelParseRowVO> result = new ArrayList<>();
            for (int r = headerRowIndex + 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null || isAllBlankRow(row, formatter)) {
                    continue;
                }
                TrainingAttendanceAssessmentExcelParseRowVO vo = new TrainingAttendanceAssessmentExcelParseRowVO();
                vo.setName(readString(row, headerIndex, formatter, "姓名"));
                vo.setCheckInTime(readString(row, headerIndex, formatter, "签到时间"));
                vo.setLearningTime(readIntegerLoose(row, headerIndex, formatter, "学习时长"));
                vo.setIsCompleted(readIntegerLoose(row, headerIndex, formatter, "是否完成学习"));
                vo.setCompletedForm(parseCompletedForm(row, headerIndex, formatter));
                vo.setScore(readIntegerLoose(row, headerIndex, formatter, "成绩"));
                vo.setTraningAttachments(readString(row, headerIndex, formatter, "获得证书"));
                vo.setCompletionDate(readString(row, headerIndex, formatter, "完成日期"));
                vo.setRemark(readString(row, headerIndex, formatter, "备注"));
                result.add(vo);
            }
            return result;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            String detail = e.getMessage();
            if (detail == null || detail.isEmpty()) {
                detail = e.getClass().getSimpleName();
            }
            throw exception0(IMPORT_FAILED.getCode(), "导入失败：{}", detail);
        }
    }

    private static Map<String, Integer> buildHeaderIndex(Row headerRow, DataFormatter formatter) {
        Map<String, Integer> map = new HashMap<>();
        short last = headerRow.getLastCellNum();
        if (last <= 0) {
            return map;
        }
        for (int c = 0; c < last; c++) {
            String v = formatter.formatCellValue(headerRow.getCell(c));
            if (v == null) {
                continue;
            }
            String key = normalizeHeader(v);
            if (!key.isEmpty()) {
                map.put(key, c);
            }
        }
        return map;
    }

    private static String readString(Row row, Map<String, Integer> headerIndex, DataFormatter formatter, String... names) {
        Integer col = pickCol(headerIndex, names);
        if (col == null) {
            return null;
        }
        String value = formatter.formatCellValue(row.getCell(col));
        return excelOptionalText(value);
    }

    private static Integer readIntegerLoose(Row row, Map<String, Integer> headerIndex, DataFormatter formatter, String... names) {
        String text = readString(row, headerIndex, formatter, names);
        if (text == null) {
            return null;
        }
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException ignored) {
            try {
                return (int) Double.parseDouble(text.trim());
            } catch (NumberFormatException ex) {
                return null;
            }
        }
    }

    /**
     * 完成形式：理论考试→1，实操考试→2；Excel 中已为 1/2 时原样解析。
     */
    private static Integer parseCompletedForm(Row row, Map<String, Integer> headerIndex, DataFormatter formatter) {
        String text = readString(row, headerIndex, formatter, "完成形式");
        if (text == null) {
            return null;
        }
        String t = text.trim();
        if ("1".equals(t) || "理论考试".equals(t)) {
            return 1;
        }
        if ("2".equals(t) || "实操考试".equals(t)) {
            return 2;
        }
        try {
            int n = (int) Double.parseDouble(t);
            if (n == 1 || n == 2) {
                return n;
            }
        } catch (NumberFormatException ignored) {
            // fall through
        }
        return null;
    }

    private static Integer pickCol(Map<String, Integer> headerIndex, String... names) {
        for (String name : names) {
            Integer idx = headerIndex.get(normalizeHeader(name));
            if (idx != null) {
                return idx;
            }
        }
        return null;
    }

    private static String normalizeHeader(String value) {
        if (value == null) {
            return "";
        }
        return value.replace(" ", "").replace("\n", "").replace("\r", "").replace("：", "").replace(":", "").trim();
    }

    private static boolean isAllBlankRow(Row row, DataFormatter formatter) {
        short last = row.getLastCellNum();
        if (last <= 0) {
            return true;
        }
        for (int c = 0; c < last; c++) {
            String v = formatter.formatCellValue(row.getCell(c));
            if (excelOptionalText(v) != null) {
                return false;
            }
        }
        return true;
    }

    private static String excelOptionalText(String value) {
        if (value == null) {
            return null;
        }
        String t = value.trim();
        if (t.isEmpty() || "null".equalsIgnoreCase(t)) {
            return null;
        }
        return t;
    }

}
