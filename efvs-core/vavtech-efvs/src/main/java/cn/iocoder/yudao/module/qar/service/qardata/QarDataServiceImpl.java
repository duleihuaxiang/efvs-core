package cn.iocoder.yudao.module.qar.service.qardata;

import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.springframework.validation.annotation.Validated;

import java.io.*;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.CharsetDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import cn.iocoder.yudao.module.qar.controller.admin.qardata.vo.*;
import cn.iocoder.yudao.module.qar.dal.dataobject.qardata.QarDataDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.qar.dal.mysql.qardata.QarDataMapper;
import cn.iocoder.yudao.module.qar.websocket.util.TimeUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.data.enums.ErrorCodeConstants.DATA_NOT_EXISTS;
import static cn.iocoder.yudao.module.data.enums.ErrorCodeConstants.QAR_LOAD_LOCAL_DISABLED;


/**
 * QAR数据 Service 实现类
 *
 * @author admin
 */
@Slf4j
@Service
@Validated
public class QarDataServiceImpl implements QarDataService {

    /** 模板/CSV 列名 -> 数据库字段名，一一对应无等价名 */
    private static final Map<String, String> TEMPLATE_HEADER_TO_DB_FIELD = new LinkedHashMap<>();
    static {
        TEMPLATE_HEADER_TO_DB_FIELD.put("日期", "date");
        TEMPLATE_HEADER_TO_DB_FIELD.put("时间", "time");
        TEMPLATE_HEADER_TO_DB_FIELD.put("空地电门", "empty_space_switch");
        TEMPLATE_HEADER_TO_DB_FIELD.put("空速", "airspeed");
        TEMPLATE_HEADER_TO_DB_FIELD.put("仰角", "pitch_angle");
        TEMPLATE_HEADER_TO_DB_FIELD.put("坡度", "slope");
        TEMPLATE_HEADER_TO_DB_FIELD.put("磁航向", "magnetic_heading");
        TEMPLATE_HEADER_TO_DB_FIELD.put("航向道偏差", "loc_deviation");
        TEMPLATE_HEADER_TO_DB_FIELD.put("下滑道偏差", "glideslope_deviation");
        TEMPLATE_HEADER_TO_DB_FIELD.put("选择速度", "selected_speed");
        TEMPLATE_HEADER_TO_DB_FIELD.put("地速", "ground_speed");
        TEMPLATE_HEADER_TO_DB_FIELD.put("无线电高度", "radio_altitude");
        TEMPLATE_HEADER_TO_DB_FIELD.put("经度", "longitude");
        TEMPLATE_HEADER_TO_DB_FIELD.put("纬度", "latitude");
        TEMPLATE_HEADER_TO_DB_FIELD.put("下滑道截获", "glide_slope_capture");
    }

    /** DB 列名 -> 设置到 QarDataDO 的 setter（用于 local_infile 不可用时的分批 INSERT） */
    private static final Map<String, BiConsumer<QarDataDO, String>> DB_COLUMN_SETTERS = new HashMap<>();
    static {
        DB_COLUMN_SETTERS.put("date", QarDataDO::setDate);
        DB_COLUMN_SETTERS.put("time", QarDataDO::setTime);
        DB_COLUMN_SETTERS.put("empty_space_switch", QarDataDO::setEmptySpaceSwitch);
        DB_COLUMN_SETTERS.put("airspeed", QarDataDO::setAirspeed);
        DB_COLUMN_SETTERS.put("pitch_angle", QarDataDO::setPitchAngle);
        DB_COLUMN_SETTERS.put("slope", QarDataDO::setSlope);
        DB_COLUMN_SETTERS.put("magnetic_heading", QarDataDO::setMagneticHeading);
        DB_COLUMN_SETTERS.put("loc_deviation", QarDataDO::setLocDeviation);
        DB_COLUMN_SETTERS.put("glideslope_deviation", QarDataDO::setGlideslopeDeviation);
        DB_COLUMN_SETTERS.put("selected_speed", QarDataDO::setSelectedSpeed);
        DB_COLUMN_SETTERS.put("ground_speed", QarDataDO::setGroundSpeed);
        DB_COLUMN_SETTERS.put("radio_altitude", QarDataDO::setRadioAltitude);
        DB_COLUMN_SETTERS.put("longitude", QarDataDO::setLongitude);
        DB_COLUMN_SETTERS.put("latitude", QarDataDO::setLatitude);
        DB_COLUMN_SETTERS.put("glide_slope_capture", QarDataDO::setGlideSlopeCapture);
    }

    @Resource
    private QarDataMapper dataMapper;
    @Autowired
    private DataSource dataSource;

    @Override
    public Integer createData(QarDataSaveReqVO createReqVO) {
        // 插入
        QarDataDO data = BeanUtils.toBean(createReqVO, QarDataDO.class);
        dataMapper.insert(data);

        // 返回
        return data.getId();
    }

    @Override
    public void updateData(QarDataSaveReqVO updateReqVO) {
        // 校验存在
        validateDataExists(updateReqVO.getId());
        // 更新
        QarDataDO updateObj = BeanUtils.toBean(updateReqVO, QarDataDO.class);
        dataMapper.updateById(updateObj);
    }

    @Override
    public void deleteData(Integer id) {
        // 校验存在
        validateDataExists(id);
        // 删除
        dataMapper.deleteById(id);
    }

    @Override
    public void deleteDataListByIds(List<Integer> ids) {
        // 删除
        dataMapper.deleteByIds(ids);
    }


    private void validateDataExists(Integer id) {
        if (dataMapper.selectById(id) == null) {
            throw exception(DATA_NOT_EXISTS);
        }
    }

    @Override
    public QarDataDO getData(Integer id) {
        return dataMapper.selectById(id);
    }

    @Override
    public PageResult<QarDataDO> getDataPage(QarDataPageReqVO pageReqVO) {
        return dataMapper.selectPage(pageReqVO);
    }

    @Override
    public void downloadQRAMappingTemplate(HttpServletResponse response) throws IOException {
        // 指定resources目录下的文件路径
        String resourcePath = "/template/QAR数据_模版文件.csv";

        // 从classpath加载文件
        ClassPathResource resource = new ClassPathResource(resourcePath);

        // 检查文件是否存在
        if (!resource.exists()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("模板文件未找到");
            return;
        }

        // 设置响应头
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename*=UTF-8''" + URLEncoder.encode("QAR数据_模版文件.csv", "UTF-8"));

        // 将文件内容写入响应流
        try (InputStream inputStream = resource.getInputStream()) {
            IOUtils.copy(inputStream, response.getOutputStream());
            response.flushBuffer();
        }
    }

    /**
     * 上传 CSV 文件 -> 使用 LOAD DATA 导入
     *
     * @param file MultipartFile (.csv)
     * @return 导入的行数
     * @throws Exception
     */
//    @Override
//    public int importCsvViaLoadData(MultipartFile file) throws Exception {
//        //public int importCsvViaLoadData(MultipartFile file,String airlineQar,String attachmentName) throws Exception {
//        if (file == null || file.isEmpty()) {
//            throw new IllegalArgumentException("file is empty");
//        }
//
//        // 1. 临时文件存放目录
//        File tmpDir = new File(System.getProperty("java.io.tmpdir"), "qar_import");
////        System.out.println("===============" + tmpDir);
//
//        if (!tmpDir.exists()) tmpDir.mkdirs();
//
//        // 2. 直接保存上传的 CSV 文件
//        File csvFile = new File(tmpDir, System.currentTimeMillis() + "_" + file.getOriginalFilename());
//        try (OutputStream os = new FileOutputStream(csvFile)) {
//            os.write(file.getBytes());
//        }
//
//        // 3. 构造 col 列名列表
//        String colList = buildColumnListFromMapping(file);
//        Connection conn = null;
//        Statement stmt = null;
//        int rowsAffected = 0;
//
//        try {
//            conn = DataSourceUtils.getConnection(dataSource);
//            stmt = conn.createStatement();
//
//            // =========================================================
//            // A. 【关键修复】关闭自动提交，开启手动事务控制
//            // 确保整个 LOAD DATA 过程作为一个单一事务执行
//            conn.setAutoCommit(false);
//            // =========================================================
//
//            // 文件路径需要使用绝对路径，反斜杠做转义
//            String csvPath = csvFile.getAbsolutePath().replace("\\", "\\\\");
//
//            // 4. 清空目标表
//            String sql = "truncate table qar_data";
////            stmt.executeUpdate(sql);
////            System.out.println("Truncate table qar_data executed.");
//
//            // 5. 性能优化配置：关闭检查
//            long startTime = System.currentTimeMillis();
//
//            // 恢复被注释的优化设置 (这些设置是会话级别的)
//            sql = "SET unique_checks = 0;";
//            stmt.execute(sql);
//            sql = "SET foreign_key_checks = 0;";
//            stmt.execute(sql);
//            sql = "SET sql_log_bin = 0;"; // 如果权限允许
//            stmt.execute(sql);
//
//            // 6. 开启 LOCAL INFILE (如果 JDBC URL 中未设置)
//            sql = "SET GLOBAL local_infile = ON;";
//            try {
//                stmt.execute(sql);
//            } catch (SQLException e) {
//                // 忽略权限不足的错误，因为很多生产环境不允许普通用户设置 GLOBAL 变量
//                if (!e.getMessage().contains("SUPER privilege")) {
//                    throw e;
//                }
//            }
//            String loginUserName = SecurityFrameworkUtils.getLoginUserName();
//            Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
//            // 通过安全框架工具类获取当前登录用户
//            LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
//            Long tenantId = 0L;
//            if (loginUser != null) {
//                tenantId = loginUser.getTenantId();
//            }
//
//            // 7. 执行 LOAD DATA LOCAL INFILE
//            sql = "LOAD DATA LOCAL INFILE '" + csvPath + "' " +
//                    "INTO TABLE efvs_qar_data " +
//                    "FIELDS TERMINATED BY ',' " +
//                    "OPTIONALLY ENCLOSED BY '\"' " +
//                    "LINES TERMINATED BY '\\n' " +
//                    "IGNORE 1 ROWS " + // 跳过第一行
//                    "(" + colList + ")" +
//                    "SET create_time = NOW(), " +
//                    "creator = '" + loginUserId + "', " +
//                    "update_time = NOW(), " +
//                    "updater = '" + loginUserId + "', " +
//                    "tenant_id = '" + tenantId + "', " +
//                    //"airline_qar = '" + airlineQar + "', " +     // 添加 airlineQar 参数 "航司QAR"
//                    //"attachment_name = '" + attachmentName + "', " + // 添加 attachmentName 参数 "QAR关联附件名称"
//                    "create_name = '" + loginUserName + "', " +
//                    "update_name = '" + loginUserName + "', " +
//                    "remark = 'QAR数据导入', " +
//                    // 单位转换：海里转公里 (1海里 = 1.852公里)
//                    // 使用变量引用列值，避免字段名大小写问题
//                    // 注意：字段名需要与列列表中的字段名一致（selected_Speed 有大写S）
//                    "@selected_speed_val := selected_Speed, " +
//                    "@airspeed_val := airspeed, " +
//                    "selected_speed_km = CASE " +
//                    "WHEN @selected_speed_val IS NULL OR @selected_speed_val = '' OR @selected_speed_val = 'NULL' OR TRIM(@selected_speed_val) = '' THEN NULL " +
//                    "WHEN CAST(@selected_speed_val AS DECIMAL(20,6)) = 0 THEN '0' " +
//                    "ELSE CAST(CAST(@selected_speed_val AS DECIMAL(20,6)) * 1.852 AS CHAR) " +
//                    "END, " +
//                    "airspeed_km = CASE " +
//                    "WHEN @airspeed_val IS NULL OR @airspeed_val = '' OR @airspeed_val = 'NULL' OR TRIM(@airspeed_val) = '' THEN NULL " +
//                    "WHEN CAST(@airspeed_val AS DECIMAL(20,6)) = 0 THEN '0' " +
//                    "ELSE CAST(CAST(@airspeed_val AS DECIMAL(20,6)) * 1.852 AS CHAR) " +
//                    "END, " +
//                    // loc_capture 和 glideslope_capture 相关转换
//                    "@loc_capture_val := loc_Capture, " +
//                    "@loc_capture_res_val := loc_capture_res, " +
//                    "glideslope_capture_res = CASE " +
//                    "WHEN @loc_capture_val IS NULL OR @loc_capture_val = '' OR @loc_capture_val = 'NULL' OR TRIM(@loc_capture_val) = '' THEN NULL " +
//                    "WHEN CAST(@loc_capture_val AS DECIMAL(20,6)) = 0 THEN '0' " +
//                    "ELSE CAST(CAST(@loc_capture_val AS DECIMAL(20,6)) * 75 AS CHAR) " +
//                    "END, " +
//                    "loc_capture = CASE " +
//                    "WHEN @loc_capture_res_val IS NULL OR @loc_capture_res_val = '' OR @loc_capture_res_val = 'NULL' OR TRIM(@loc_capture_res_val) = '' THEN loc_Capture " +
//                    "WHEN CAST(@loc_capture_res_val AS DECIMAL(20,6)) = 0 THEN '0' " +
//                    "ELSE CAST(CAST(@loc_capture_res_val AS DECIMAL(20,6)) * 75 AS CHAR) " +
//                    "END ";
//
//            rowsAffected = stmt.executeUpdate(sql);
//
//            // =========================================================
//            // B. 【关键修复】执行 LOAD DATA INFILE 后，手动提交事务
//            conn.commit();
//            // =========================================================
//
//            long endTime = System.currentTimeMillis();
//
//            // 8. 恢复性能配置
//            sql = "SET unique_checks = 1;";
//            stmt.execute(sql);
//            sql = "SET foreign_key_checks = 1;";
//            stmt.execute(sql);
//            sql = "SET sql_log_bin = 1;";
//            stmt.execute(sql);
//
//            // =========================================================
//            // C. 【关键修复】将 AutoCommit 状态恢复为 true (如果不希望影响连接池中的其他连接)
//            conn.setAutoCommit(true);
//            // =========================================================
//
//            System.out.printf("LOAD DATA INFILE 导入完成，耗时: %.2f 秒, 影响行数: %d\n",
//                    (endTime - startTime) / 1000.0, rowsAffected);
//        } finally {
//            if (stmt != null) try {
//                stmt.close();
//            } catch (Exception ignored) {
//            }
//            // 释放连接
//            if (conn != null) {
//                // 如果在 try 块中失败，需要确保 AutoCommit 状态被恢复，
//                // 确保连接池中的连接是干净的
//                if (conn != null && !conn.getAutoCommit()) {
//                    try {
//                        conn.setAutoCommit(true);
//                    } catch (SQLException e) {
//                        // 忽略
//                    }
//                }
//                DataSourceUtils.releaseConnection(conn, dataSource);
//            }
//            // 清理临时 CSV 文件
//            if (csvFile.exists()) {
//                csvFile.delete();
//            }
//        }
//        return rowsAffected;
//    }

//    private String buildColumnListFromMapping(MultipartFile file) throws IOException {
//        // 1. 读取 CSV 文件的第一行，获取列名顺序
//        List<String> columnOrder = readFirstRowColumnNames(file);
//        QarDataImportMapping mapping = new QarDataImportMapping();
//        Map<String, String> fieldMap = mapping.getColumnToFieldMap();
//
//        // 2. 按照第一行的列顺序构建列列表
//        List<String> columns = new ArrayList<>();
//        for (int i = 0; i < columnOrder.size(); i++) {
//            String col = columnOrder.get(i).trim();
//            String fieldName = fieldMap.get(col);
//            if (fieldName != null) {
//                columns.add(fieldName);
//            } else {
//                throw new IllegalArgumentException("未找到列 '" + col + "' 对应的字段映射");
//            }
//        }
//        // 添加系统字段到末尾
////        columns.add("airline_qar");
////        columns.add("attachment_name");
//        columns.add("create_time");
//        columns.add("creator");
//        columns.add("update_time");
//        columns.add("updater");
//        columns.add("tenant_id");
//        columns.add("remark");
//        return String.join(",", columns);
//    }

    /**
     * 构建列名列表（只处理映射表中存在的字段），CSV 列名必须与映射中的列名完全一致（除去首尾空格）。
     * @param csvFile CSV文件
     * @param mapping 映射关系（CSV列名 -> 数据库字段名）
     * @return 数据库字段名列表（逗号分隔）
     */
    private String buildColumnListFromMapping(File csvFile, Map<String, String> mapping) throws IOException {
        if (csvFile == null) {
            throw new IllegalArgumentException("csvFile 不能为 null");
        }
        if (mapping == null) {
            throw new IllegalArgumentException("mapping 不能为 null");
        }
        if (mapping.isEmpty()) {
            throw new IllegalArgumentException("mapping 不能为空");
        }
        
        // 1. 读取 CSV 文件的第一行，获取列名顺序
        List<String> columnOrder = readFirstRowColumnNames(csvFile);
        if (columnOrder == null || columnOrder.isEmpty()) {
            throw new IllegalArgumentException("CSV文件列名为空: " + csvFile.getAbsolutePath());
        }
        
        log.info("[buildColumnListFromMapping] CSV文件列数: {}, 映射表大小: {}", columnOrder.size(), mapping.size());
        
        // 2. 按照第一行的列顺序构建列列表
        // 关键：LOAD DATA 要求列列表必须与CSV列顺序一致
        // 对于映射表中不存在的列，使用用户变量(@var)接收但不存储，避免数据错位
        List<String> columns = new ArrayList<>();
        List<String> unmatchedColumns = new ArrayList<>(); // 记录未匹配的列（用于日志）
        int varIndex = 1; // 用户变量索引
        
        for (int i = 0; i < columnOrder.size(); i++) {
            String col = columnOrder.get(i);
            if (col == null) {
                log.warn("[buildColumnListFromMapping] 第{}列为null，跳过", i + 1);
                continue;
            }
            col = normalizeCsvHeader(col);
            if (col.isEmpty()) {
                log.warn("[buildColumnListFromMapping] 第{}列为空，跳过", i + 1);
                continue;
            }
            
            String fieldName = mapping.get(col);
            // 如果fieldName不为null，检查是否为有效值
            if (fieldName != null && fieldName.trim().isEmpty()) {
                log.warn("[buildColumnListFromMapping] 映射表中列 '{}' 对应的字段名为空字符串，视为未匹配", col);
                fieldName = null;
            }
            
            if (fieldName != null) {
                // 找到映射关系，添加数据库字段名
                columns.add(fieldName);
            } else {
                // 未找到映射关系，使用用户变量接收该列的值但不存储
                // 这样可以保持CSV列顺序与LOAD DATA列列表一致，避免数据错位
                columns.add("@dummy" + varIndex);
                unmatchedColumns.add("第" + (i + 1) + "列: '" + col + "' (已忽略)");
                varIndex++;
            }
        }
        
        // 3. 记录未匹配的列（仅用于日志，不影响导入）
        if (!unmatchedColumns.isEmpty()) {
            System.out.println("信息: 以下CSV列在映射表中未找到，已自动忽略（不影响导入）:");
            for (String unmatched : unmatchedColumns) {
                System.out.println("  - " + unmatched  );
            }
        }
        
        // 4. 如果没有找到任何映射的字段，抛出异常
        boolean hasMappedField = columns.stream().anyMatch(col -> !col.startsWith("@dummy"));
        if (!hasMappedField) {
            throw new IllegalArgumentException("CSV文件中没有找到任何在映射表中定义的字段，请检查映射关系是否正确");
        }
        
        return String.join(",", columns);
    }
    
    /**
     * 根据映射关系查找字段名（支持大小写不敏感匹配）
     * @param mapping 映射关系（CSV列名 -> 数据库字段名）
     * @param targetFieldName 目标字段名（如 "selected_speed", "airspeed", "loc_capture"）
     * @return 映射中的实际字段名，如果不存在则返回null
     */
    private String findFieldNameInMapping(Map<String, String> mapping, String targetFieldName) {
        // 先尝试精确匹配（忽略大小写）
        for (Map.Entry<String, String> entry : mapping.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(targetFieldName)) {
                return entry.getValue();
            }
        }
        return null;
    }
    
    /**
     * 根据映射关系和列列表查找字段名（支持大小写不敏感匹配）
     * 确保字段既在 mapping 中存在，也在 colList 中存在（即 CSV 中确实有该列）
     * @param mapping 映射关系（CSV列名 -> 数据库字段名）
     * @param colList 列列表（逗号分隔的数据库字段名）
     * @param targetFieldName 目标字段名（如 "selected_speed", "airspeed", "ground_speed"）
     * @return 映射中的实际字段名，如果不存在或不在 colList 中则返回null
     */
    private String findFieldNameInMappingAndColList(Map<String, String> mapping, String colList, String targetFieldName) {
        // 先查找 mapping 中的字段名
        String fieldName = findFieldNameInMapping(mapping, targetFieldName);
        if (fieldName == null) {
            log.debug("[findFieldNameInMappingAndColList][{}] 在 mapping 中未找到", targetFieldName);
            return null;
        }
        
        log.debug("[findFieldNameInMappingAndColList][{}] 在 mapping 中找到字段名: {}", targetFieldName, fieldName);
        
        // 检查字段是否在 colList 中存在（支持大小写不敏感）
        String[] columns = colList.split(",");
        for (String col : columns) {
            String colTrimmed = col.trim();
            if (colTrimmed.equalsIgnoreCase(fieldName)) {
                log.debug("[findFieldNameInMappingAndColList][{}] 在 colList 中找到匹配字段: {}", targetFieldName, colTrimmed);
                return colTrimmed; // 返回 colList 中的实际字段名（保持大小写一致）
            }
        }
        
        // 如果 mapping 中存在但 colList 中不存在，返回 null
        log.warn("[findFieldNameInMappingAndColList][{}] mapping 中找到字段名 {}，但 colList 中不存在", targetFieldName, fieldName);
        return null;
    }

/*
    private String buildColumnListFromMapping(File csvFile) throws IOException {
        // 使用默认映射
        QarDataImportMapping mapping = new QarDataImportMapping();
        Map<String, String> fieldMap = mapping.getColumnToFieldMap();
        return buildColumnListFromMapping(csvFile, fieldMap);
    }
*/

    /** 规范化 CSV 表头：去除 BOM、首尾空白，便于与映射表匹配 */
    private static String normalizeCsvHeader(String header) {
        if (header == null) return "";
        String s = header.trim();
        if (s.isEmpty()) return s;
        if (s.charAt(0) == '\uFEFF') s = s.substring(1).trim();
        return s;
    }

    // 辅助方法：读取 CSV 文件第一行的列名（统一使用 UTF-8，避免系统默认编码导致表头与映射不匹配）
    private List<String> readFirstRowColumnNames(File csvFile) throws IOException {
        if (csvFile == null) {
            throw new IllegalArgumentException("csvFile 不能为 null");
        }
        if (!csvFile.exists()) {
            throw new IllegalArgumentException("CSV文件不存在: " + csvFile.getAbsolutePath());
        }
        if (!csvFile.isFile()) {
            throw new IllegalArgumentException("CSV路径不是文件: " + csvFile.getAbsolutePath());
        }
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(csvFile), StandardCharsets.UTF_8))) {
            String firstLine = reader.readLine(); // 读取第一行
            if (firstLine == null) {
                throw new IllegalArgumentException("CSV 文件为空: " + csvFile.getAbsolutePath());
            }

            // 处理BOM字符 - 在分割前清理第一行内容
            if (firstLine.length() > 0 && firstLine.charAt(0) == '\uFEFF') {
                firstLine = firstLine.substring(1);
            }

            // 使用逗号分割列名（CSV 格式）
            if (firstLine.trim().isEmpty()) {
                throw new IllegalArgumentException("CSV 文件第一行为空: " + csvFile.getAbsolutePath());
            }
            
            List<String> columns = Arrays.stream(firstLine.split(","))
                    .map(s -> normalizeCsvHeader(s))
                    .filter(col -> !col.isEmpty()) // 过滤空列
                    .collect(Collectors.toList());
            
            if (columns.isEmpty()) {
                throw new IllegalArgumentException("CSV 文件第一行没有有效的列名: " + csvFile.getAbsolutePath());
            }
            
            return columns;
        } catch (IOException e) {
            log.error("[readFirstRowColumnNames] 读取CSV文件失败: {}", csvFile.getAbsolutePath(), e);
            throw new IOException("读取CSV文件失败: " + csvFile.getAbsolutePath() + ", 错误: " + e.getMessage(), e);
        }
    }
    // 读取 MultipartFile 文件第一行的列名
    private List<String> readFirstRowColumnNames(MultipartFile file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String firstLine = reader.readLine();
            if (firstLine == null) {
                throw new IllegalArgumentException("CSV 文件为空");
            }

            // 处理BOM字符 - 在分割前清理第一行内容
            if (firstLine != null && firstLine.length() > 0 && firstLine.charAt(0) == '\uFEFF') {
                firstLine = firstLine.substring(1);
            }

            // 使用逗号分割列名（CSV 格式）
            return Arrays.stream(firstLine.split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());
        }
    }

    /**
     * 构建列名列表（支持自定义映射）
     */
//    private String buildColumnListFromMapping(MultipartFile file, Map<String, String> mapping) throws IOException {
//        // 1. 读取 CSV 文件的第一行，获取列名顺序
//        List<String> columnOrder = readFirstRowColumnNames(file);
//
//        // 2. 按照第一行的列顺序构建列列表
//        List<String> columns = new ArrayList<>();
//        for (int i = 0; i < columnOrder.size(); i++) {
//            String col = columnOrder.get(i).trim();
//            String fieldName = mapping.get(col);
//            if (fieldName != null) {
//                columns.add(fieldName);
//            } else {
//                throw new IllegalArgumentException("未找到列 '" + col + "' 对应的字段映射");
//            }
//        }
//
//        return String.join(",", columns);
//    }

    //=============================================================wxy实现的cvs入库代码==============================================================================

    @Override
    public Map<String, Object> batchImportCsvViaLoadData(List<MultipartFile> csvFiles) throws Exception {
        return batchImportCsvViaLoadData(csvFiles, null);
    }

    @Override
    public Map<String, Object> batchImportCsvViaLoadData(List<MultipartFile> csvFiles, List<Long> qarIds) throws Exception {
        // 1. 验证参数。qarIds 与文件列表按下标对应，用于将每个 CSV 的明细关联到指定 QAR 主记录。
        if (csvFiles == null || csvFiles.isEmpty()) {
            throw new IllegalArgumentException("CSV文件列表不能为空");
        }
        if (csvFiles.size() > 20) {
            throw new IllegalArgumentException("一次最多只能上传20个CSV文件");
        }
        
        // 2. 从固定模板读取映射关系（resources/template/QAR数据_模版文件.xlsx）。
        // 模板是业务字段白名单，CSV 中未映射的列会被忽略，避免动态列直接写库。
        Map<String, String> mapping = readMappingFromTemplateExcel();

        
        // 3. 初始化结果收集器
        Map<String, Object> result = new HashMap<>();
        List<String> successFiles = new ArrayList<>();
        List<String> failedFiles = new ArrayList<>();
        int totalRowsImported = 0;
        // 5. 循环处理每个CSV文件（支持按顺序传入 qarIds，入库时写入 qar_id）
        for (int i = 0; i < csvFiles.size(); i++) {
            MultipartFile csvFile = csvFiles.get(i);
            Long qarId = (qarIds != null && i < qarIds.size()) ? qarIds.get(i) : null;
            try {
                // 获取文件名
                String fileName = csvFile.getOriginalFilename();
                if (fileName == null) {
                    failedFiles.add("未知文件名");
                    continue;
                }
                // 调用单个导入方法（传入 qar_id，大文件入库时写入 efvs_qar_data.qar_id）
                int rowsImported = importCsvViaLoadData(csvFile, mapping, qarId);
                totalRowsImported += rowsImported;
                successFiles.add(fileName + " (导入" + rowsImported + "行)");
            } catch (Exception e) {
                // 收集失败的文件信息
                String fileName = csvFile.getOriginalFilename() != null ? csvFile.getOriginalFilename() : "未知文件名";
                failedFiles.add(fileName + " (失败原因: " + e.getMessage() + ")");
            }
        }
        
        // 6. 组装结果
        result.put("totalFiles", csvFiles.size());
        result.put("successFilesCount", successFiles.size());
        result.put("failedFilesCount", failedFiles.size());
        result.put("totalRowsImported", totalRowsImported);
        result.put("successFiles", successFiles);
        result.put("failedFiles", failedFiles);
        result.put("status", "完成");
        return result;
    }

    @Override
    public int importCsvViaLoadDataFromPath(String csvFilePath, Long qarId) throws Exception {
        return importCsvViaLoadDataFromPath(csvFilePath, qarId, false);
    }

    @Override
    public int importCsvViaLoadDataFromPath(String csvFilePath, Long qarId, boolean logicalDeleteByQarIdFirst) throws Exception {
        if (csvFilePath == null || csvFilePath.trim().isEmpty()) {
            throw new IllegalArgumentException("CSV文件路径不能为空");
        }
        if (qarId == null) {
            throw new IllegalArgumentException("qarId 不能为空");
        }
        File csvFile = null;
        boolean isTempFile = false;
        try {
            // 支持两类来源：上传服务返回的 HTTP(S) 地址，或后端可访问的本地绝对路径。
            // URL 文件会先落到系统临时目录，再复用统一的 UTF-8 校验与 CSV 导入逻辑。
            if (csvFilePath.trim().startsWith("http://") || csvFilePath.trim().startsWith("https://")) {
                log.info("[importCsvViaLoadDataFromPath] 从URL下载文件: {}", csvFilePath);
                csvFilePath = normalizeUrl(csvFilePath.trim());
                byte[] fileBytes = cn.hutool.http.HttpUtil.downloadBytes(csvFilePath.trim());
                if (fileBytes == null || fileBytes.length == 0) {
                    throw new IllegalArgumentException("从URL下载的文件为空: " + csvFilePath);
                }
                File tmpDir = new File(System.getProperty("java.io.tmpdir"), "qar_import");
                if (!tmpDir.exists()) {
                    tmpDir.mkdirs();
                }
                String fileName = csvFilePath.substring(csvFilePath.lastIndexOf('/') + 1);
                if (fileName.isEmpty() || !fileName.contains(".")) {
                    fileName = System.currentTimeMillis() + ".csv";
                }
                csvFile = new File(tmpDir, System.currentTimeMillis() + "_" + fileName);
                try (FileOutputStream fos = new FileOutputStream(csvFile)) {
                    fos.write(fileBytes);
                }
                isTempFile = true;
            } else {
                csvFile = new File(csvFilePath);
                if (!csvFile.exists() || !csvFile.isFile()) {
                    throw new IllegalArgumentException("CSV文件不存在或不是文件: " + csvFilePath);
                }
            }
            // 校验并按需转码为 UTF-8：非 UTF-8（如 GBK）文件自动转码后入库。
            // 若来源是用户本地文件，转码结果另存临时文件，绝不改动原文件。
            byte[] rawBytes = Files.readAllBytes(csvFile.toPath());
            byte[] utf8Bytes = normalizeCsvToUtf8(rawBytes, csvFile.getName());
            if (utf8Bytes != rawBytes) {
                if (isTempFile) {
                    // 已是本方法从URL下载生成的临时文件，可就地覆盖为 UTF-8
                    Files.write(csvFile.toPath(), utf8Bytes);
                } else {
                    File tmpDir = new File(System.getProperty("java.io.tmpdir"), "qar_import");
                    if (!tmpDir.exists()) {
                        tmpDir.mkdirs();
                    }
                    File utf8File = new File(tmpDir, System.currentTimeMillis() + "_utf8_" + csvFile.getName());
                    Files.write(utf8File.toPath(), utf8Bytes);
                    csvFile = utf8File;
                    isTempFile = true;
                }
            }
            // create/update 从路径导入使用完整内置映射，确保关键飞行参数列能够稳定入库。
            Map<String, String> mapping = new LinkedHashMap<>(TEMPLATE_HEADER_TO_DB_FIELD);
            log.info("[importCsvViaLoadDataFromPath] 使用内置映射，共 {} 列", mapping.size());
            return importCsvViaLoadData(csvFile, mapping, qarId, false, logicalDeleteByQarIdFirst);
        } finally {
            // 仅删除本方法从 URL 下载生成的临时文件，调用方传入的本地源文件不得删除。
            if (isTempFile && csvFile != null && csvFile.exists()) {
                try {
                    csvFile.delete();
                } catch (Exception e) {
                    log.warn("[importCsvViaLoadDataFromPath] 删除临时文件时发生异常: {}", csvFile != null ? csvFile.getAbsolutePath() : "", e);
                }
            }
        }
    }

    /** UTF-8 校验采样大小（仅校验前 1MB，大文件不全部加载） */
    private static final int UTF8_VALIDATE_SAMPLE_SIZE = 1024 * 1024;

    /**
     * 将 CSV 内容规范化为 UTF-8。已是 UTF-8 时原样返回同一数组；否则按 GB18030 解码后重新编码为 UTF-8。
     * 编码校验仅采样前 1MB，大文件不整体加载判断。
     *
     * @param bytes    CSV 原始字节
     * @param fileName 文件名（仅用于日志与异常信息）
     * @return UTF-8 编码的字节；若入参已是 UTF-8，则返回同一个数组引用（供调用方据此判断是否需要重写）
     * @throws IllegalArgumentException 既不是 UTF-8，也无法按 GB18030 解码
     */
    private byte[] normalizeCsvToUtf8(byte[] bytes, String fileName) {
        if (bytes == null || bytes.length == 0) {
            return bytes;
        }
        int sampleLength = Math.min(bytes.length, UTF8_VALIDATE_SAMPLE_SIZE);
        CharsetDecoder utf8Decoder = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);
        try {
            utf8Decoder.decode(ByteBuffer.wrap(bytes, 0, sampleLength));
            return bytes;
        } catch (CharacterCodingException e) {
            // 非 UTF-8，继续按 GB18030 转码
        }
        try {
            CharsetDecoder gb18030Decoder = Charset.forName("GB18030").newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT);
            String text = gb18030Decoder.decode(ByteBuffer.wrap(bytes)).toString();
            log.info("[normalizeCsvToUtf8] {} 非 UTF-8，已按 GB18030 转码为 UTF-8", fileName);
            return text.getBytes(StandardCharsets.UTF_8);
        } catch (CharacterCodingException e) {
            throw new IllegalArgumentException(fileName + " 文件编码无法识别，请使用 UTF-8 或 GBK/GB18030 编码");
        }
    }

    /**
     * 从Excel映射模板中读取映射关系（第二列和第四列）
     *
     * @param mappingExcel Excel映射模板文件
     * @return 映射关系 (Excel列名 → 数据库字段名)
     * @throws IOException 读取失败时抛出
     */
    private Map<String, String> readMappingFromExcel(MultipartFile mappingExcel) throws IOException {
        Map<String, String> mapping = new HashMap<>();

        // 正确的类型声明
        List<QarDataMappingVO> read = ExcelUtils.read(mappingExcel, QarDataMappingVO.class);
        if (read == null || read.isEmpty()) {
            throw new IllegalArgumentException("映射文件为空或无法读取");
        }
        
        for (QarDataMappingVO qarDataMappingVO : read) {
            if (qarDataMappingVO == null) {
                log.warn("[readMappingFromExcel] 映射文件中存在null行，跳过");
                continue;
            }
            
            String columnName = qarDataMappingVO.getColumnName();
            String fieldName = qarDataMappingVO.getFieldName();
            
            // 跳过columnName为null或空的条目
            if (columnName == null || columnName.trim().isEmpty()) {
                log.warn("[readMappingFromExcel] 映射文件中存在columnName为null或空的行，跳过");
                continue;
            }
            
            // 跳过fieldName为null或空的条目
            if (fieldName == null || fieldName.trim().isEmpty()) {
                log.warn("[readMappingFromExcel] 映射文件中columnName '{}' 对应的fieldName为null或空，跳过", columnName);
                continue;
            }
            
            //存储是time:时间
            mapping.put(columnName.trim(), fieldName.trim());
        }
        
        if (mapping.isEmpty()) {
            throw new IllegalArgumentException("映射文件中没有有效的映射关系");
        }
        
        log.info("[readMappingFromExcel] 成功读取 {} 条映射关系", mapping.size());
        return mapping;
    }

    /**
     * 从 resources/template/QAR数据_模版文件.xlsx 读取固定映射关系。
     * 优先按「模板首行为表头」解析：仅导入模板第一行中出现的列，与内置列名映射后入库；
     * 导入数据中不在模板里的列（如 北京时间、番直载荷 等）一律忽略。
     * 若按首行解析不到有效映射，则回退为旧版两列映射表格式（fieldName/columnName）。
     */
    private Map<String, String> readMappingFromTemplateExcel() throws IOException {
        String resourcePath = "/template/QAR数据_模版文件.xlsx";
        ClassPathResource resource = new ClassPathResource(resourcePath);
        if (!resource.exists()) {
            throw new IllegalArgumentException("模板文件未找到: " + resourcePath);
        }

        byte[] bytes;
        try (InputStream is = resource.getInputStream()) {
            bytes = IOUtils.toByteArray(is);
        }

        // 1) 尝试按「第一行表头」解析：只导入模板里列出的列
        List<String> firstRowHeaders = readExcelFirstRowAsHeaders(new ByteArrayInputStream(bytes));
        if (firstRowHeaders != null && !firstRowHeaders.isEmpty()) {
            Map<String, String> mapping = buildMappingFromTemplateHeaders(firstRowHeaders);
            if (!mapping.isEmpty()) {
                log.info("[readMappingFromTemplateExcel] 按模板首行解析到 {} 个需导入列，其余列将忽略", mapping.size());
                return mapping;
            }
        }

        // 2) 回退：按旧版两列映射表格式解析
        MultipartFile multipartFile =
                new InputStreamBackedMultipartFile("mappingTemplate", "QAR数据_模版文件.xlsx", bytes);
        return readMappingFromExcel(multipartFile);
    }

    /**
     * 读取 Excel 第一行作为表头列名列表（用于「仅导入模板中列」）
     */
    private List<String> readExcelFirstRowAsHeaders(InputStream inputStream) {
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            Row row = sheet.getRow(0);
            if (row == null) return Collections.emptyList();
            List<String> headers = new ArrayList<>();
            for (int i = 0; i < row.getLastCellNum(); i++) {
                Cell cell = row.getCell(i);
                String value = getCellStringValue(cell);
                if (value != null && !value.trim().isEmpty()) {
                    headers.add(value.trim());
                }
            }
            return headers;
        } catch (Exception e) {
            log.warn("[readExcelFirstRowAsHeaders] 读取模板首行失败，将尝试旧版映射格式: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            default:
                return null;
        }
    }

    /**
     * 根据模板首行表头构建「列名 -> 数据库字段」映射，一一对应无等价名。
     */
    private Map<String, String> buildMappingFromTemplateHeaders(List<String> templateHeaders) {
        Map<String, String> mapping = new LinkedHashMap<>();
        for (String header : templateHeaders) {
            if (header == null || header.trim().isEmpty()) continue;
            String trimmed = header.trim();
            String dbField = TEMPLATE_HEADER_TO_DB_FIELD.get(trimmed);
            if (dbField != null) {
                mapping.put(trimmed, dbField);
            }
        }
        return mapping;
    }

    /**
     * 从映射文件路径（mappingFile 字段）读取映射关系，不读取 resource 下模板
     * 支持本地文件路径和URL
     */
    private Map<String, String> readMappingFromPath(String mappingFilePath) throws IOException {
        File file = null;
        boolean isTempFile = false;
        
        try {
            // 判断是URL还是本地文件路径
            if (mappingFilePath.trim().startsWith("http://") || mappingFilePath.trim().startsWith("https://")) {
                // 从URL下载文件到临时目录
                log.info("[readMappingFromPath] 从URL下载映射文件: {}", mappingFilePath);
                byte[] fileBytes = cn.hutool.http.HttpUtil.downloadBytes(mappingFilePath.trim());
                if (fileBytes == null || fileBytes.length == 0) {
                    throw new IllegalArgumentException("从URL下载的映射文件为空: " + mappingFilePath);
                }
                
                // 创建临时文件
                File tmpDir = new File(System.getProperty("java.io.tmpdir"), "qar_import");
                if (!tmpDir.exists()) {
                    tmpDir.mkdirs();
                }
                
                // 从URL中提取文件名，如果没有则使用时间戳
                String fileName = mappingFilePath.substring(mappingFilePath.lastIndexOf('/') + 1);
                if (fileName.isEmpty() || !fileName.contains(".")) {
                    fileName = System.currentTimeMillis() + ".xlsx";
                }
                
                file = new File(tmpDir, System.currentTimeMillis() + "_" + fileName);
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    fos.write(fileBytes);
                }
                isTempFile = true; // 标记为临时文件
                log.info("[readMappingFromPath] 映射文件已下载到临时目录: {}", file.getAbsolutePath());
            } else {
                // 本地文件路径
                file = new File(mappingFilePath);
                if (!file.exists() || !file.isFile()) {
                    throw new IllegalArgumentException("映射文件不存在或不是文件: " + mappingFilePath);
                }
            }
            
            byte[] bytes = Files.readAllBytes(file.toPath());
            MultipartFile multipartFile = new InputStreamBackedMultipartFile("mapping", file.getName(), bytes);
            return readMappingFromExcel(multipartFile);
        } finally {
            // 如果是临时文件，读取完成后删除
            if (isTempFile && file != null && file.exists()) {
                try {
                    boolean deleted = file.delete();
                    if (deleted) {
                        log.info("[readMappingFromPath] 临时映射文件已删除: {}", file.getAbsolutePath());
                    } else {
                        log.warn("[readMappingFromPath] 临时映射文件删除失败: {}", file.getAbsolutePath());
                    }
                } catch (Exception e) {
                    log.warn("[readMappingFromPath] 删除临时映射文件时发生异常: {}", file.getAbsolutePath(), e);
                }
            }
        }
    }

    /**
     * 基于 InputStream/byte[] 的 MultipartFile 适配
     */
    private static class InputStreamBackedMultipartFile implements MultipartFile {
        private final String name;
        private final String originalFilename;
        private final byte[] content;

        InputStreamBackedMultipartFile(String name, String originalFilename, byte[] content) {
            this.name = name;
            this.originalFilename = originalFilename;
            this.content = content != null ? content : new byte[0];
        }

        @Override
        public String getName() { return name; }

        @Override
        public String getOriginalFilename() { return originalFilename; }

        @Override
        public String getContentType() { return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"; }

        @Override
        public boolean isEmpty() { return content.length == 0; }

        @Override
        public long getSize() { return content.length; }

        @Override
        public byte[] getBytes() { return content; }

        @Override
        public InputStream getInputStream() { return new ByteArrayInputStream(content); }

        @Override
        public void transferTo(File dest) throws IOException {
            Files.write(dest.toPath(), content);
        }
    }
    


    
    /**
     * 导入单个CSV文件（内部方法，支持自定义映射与 qar_id）
     *
     * @param file          CSV文件
     * @param mapping       自定义映射关系
     * @param qarId         QAR 主表主键 id，不为 null 时写入 efvs_qar_data.qar_id
     * @return 导入的行数
     * @throws Exception 导入失败时抛出
     */
    private int importCsvViaLoadData(MultipartFile file, Map<String, String> mapping, Long qarId) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("file is empty");
        }
        if (mapping == null || mapping.isEmpty()) {
            throw new IllegalArgumentException("mapping is empty");
        }
        File tmpDir = new File(System.getProperty("java.io.tmpdir"), "qar_import");
        if (!tmpDir.exists()) tmpDir.mkdirs();
        File csvFile = new File(tmpDir, System.currentTimeMillis() + "_" + file.getOriginalFilename());
        // 上传内容非 UTF-8（如 GBK）时自动转码，保证表头匹配与入库取值正确
        byte[] bytes = normalizeCsvToUtf8(file.getBytes(), file.getOriginalFilename());
        try (OutputStream os = new FileOutputStream(csvFile)) {
            os.write(bytes);
        }
        return importCsvViaLoadData(csvFile, mapping, qarId, true, false);
    }

    /**
     * 导入单个CSV文件（基于已存在的 File，支持 qar_id）
     * @param deleteFileAfterImport true 时在导入后删除文件（仅用于临时文件）；从路径导入时传 false 避免删除用户文件
     */
    private int importCsvViaLoadData(File csvFile, Map<String, String> mapping, Long qarId, boolean deleteFileAfterImport, boolean logicalDeleteByQarIdFirst) throws Exception {
        // 参数校验
        if (csvFile == null) {
            throw new IllegalArgumentException("csvFile 不能为 null");
        }
        if (!csvFile.exists()) {
            throw new IllegalArgumentException("CSV文件不存在: " + csvFile.getAbsolutePath());
        }
        if (!csvFile.isFile()) {
            throw new IllegalArgumentException("CSV路径不是文件: " + csvFile.getAbsolutePath());
        }
        if (mapping == null) {
            throw new IllegalArgumentException("mapping 不能为 null");
        }
        if (mapping.isEmpty()) {
            throw new IllegalArgumentException("mapping 不能为空");
        }
        if (qarId == null) {
            throw new IllegalArgumentException("qarId 不能为 null");
        }

        log.info("[importCsvViaLoadData] 开始导入CSV文件: {}, qarId: {}, mapping size: {}", 
                csvFile.getAbsolutePath(), qarId, mapping.size());

        // 3. 构造 col 列名列表
        String colList;
        try {
            colList = buildColumnListFromMapping(csvFile, mapping);
            if (colList == null || colList.trim().isEmpty()) {
                throw new IllegalArgumentException("构建的列列表为空");
            }
            log.info("[importCsvViaLoadData][colList构建成功] {}", colList);
        } catch (Exception e) {
            log.error("[importCsvViaLoadData][构建列列表失败] csvFile: {}", csvFile.getAbsolutePath(), e);
            throw new IllegalArgumentException("构建列列表失败: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()), e);
        }
        
        // 4. 查找需要转换的字段名（支持大小写不敏感）
        // 注意：不仅要检查 mapping 中是否存在，还要检查 colList 中是否存在（因为 CSV 中可能没有该列）
        String selectedSpeedField = findFieldNameInMappingAndColList(mapping, colList, "selected_speed");
        String airspeedField = findFieldNameInMappingAndColList(mapping, colList, "airspeed");
        String groundSpeedField = findFieldNameInMappingAndColList(mapping, colList, "ground_speed");
        String locDeviationField = findFieldNameInMappingAndColList(mapping, colList, "loc_deviation");
        String glideslopeDeviationField = findFieldNameInMappingAndColList(mapping, colList, "glideslope_deviation");
        
        // 调试日志：输出查找结果
        log.info("[importCsvViaLoadData][字段查找结果] selectedSpeedField: {}, airspeedField: {}, groundSpeedField: {}, locDeviationField: {}, glideslopeDeviationField: {}", 
                selectedSpeedField, airspeedField, groundSpeedField, locDeviationField, glideslopeDeviationField);
        
        Connection conn = null;
        Statement stmt = null;
        int rowsAffected = 0;
        boolean managedTx = false; // 是否由 Spring 管理事务，在 try 内赋值，供 catch/finally 使用

        try {
            if (dataSource == null) {
                throw new IllegalStateException("dataSource 不能为 null");
            }
            
            log.info("[importCsvViaLoadData] 开始获取数据库连接");
            conn = DataSourceUtils.getConnection(dataSource);
            if (conn == null) {
                throw new IllegalStateException("无法获取数据库连接");
            }
            
            log.info("[importCsvViaLoadData] 创建Statement");
            stmt = conn.createStatement();
            if (stmt == null) {
                throw new IllegalStateException("无法创建Statement");
            }

            // 若为更新场景：在同一连接上先逻辑删除该 qarId 下原数据（仅 deleted=0），再执行 LOAD DATA，避免新入库数据被误标删
            if (logicalDeleteByQarIdFirst && qarId != null) {
                try (java.sql.PreparedStatement ps = conn.prepareStatement("UPDATE efvs_qar_data SET deleted = 1 WHERE qar_id = ? AND deleted = 0")) {
                    ps.setLong(1, qarId);
                    int deletedRows = ps.executeUpdate();
                    log.info("[importCsvViaLoadData] 已按 qarId 逻辑删除原数据，影响行数: {}", deletedRows);
                }
            }

            // 仅在没有 Spring 事务时手动控制提交（否则会与 Spring 冲突，导致 Can't call commit when autocommit=true）
            managedTx = TransactionSynchronizationManager.isSynchronizationActive();
            if (!managedTx) {
                conn.setAutoCommit(false);
            }

            // 文件路径需要使用绝对路径，反斜杠做转义
            if (csvFile == null) {
                throw new IllegalStateException("csvFile 在数据库操作时为 null");
            }
            String csvPath = csvFile.getAbsolutePath();
            if (csvPath == null || csvPath.trim().isEmpty()) {
                throw new IllegalStateException("CSV文件路径为空");
            }
            csvPath = csvPath.replace("\\", "\\\\");
            log.info("[importCsvViaLoadData] CSV文件路径: {}", csvPath);

            // 性能优化配置：关闭检查
            String sql = "SET unique_checks = 0;";
            stmt.execute(sql);
            sql = "SET foreign_key_checks = 0;";
            stmt.execute(sql);
            
            // 启用 LOAD DATA LOCAL INFILE（服务端设置）
            // 先尝试设置 SESSION 级别（不需要特殊权限）
            try {
                sql = "SET SESSION local_infile = 1;";
                stmt.execute(sql);
            } catch (SQLException e) {
                // 如果 SESSION 级别失败，尝试 GLOBAL 级别（需要 SUPER 权限）
                try {
                    sql = "SET GLOBAL local_infile = 1;";
                    stmt.execute(sql);
                } catch (SQLException ex) {
                    // 如果都没有权限，记录警告但继续执行
                    // 这种情况下，需要数据库管理员手动启用 local_infile
                    System.err.println("警告: 无法启用 local_infile，可能需要数据库管理员手动设置: " + ex.getMessage());
                }
            }

            String loginUserName = SecurityFrameworkUtils.getLoginUserName();
            Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
            Long tenantId = 0L;
            LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
            if (loginUser != null) {
                tenantId = loginUser.getTenantId();
            }

            // 构建SET子句（动态处理字段名）
            // 注意：在LOAD DATA的SET子句中，可以直接引用列列表中的字段名，不需要使用变量
            StringBuilder setClause = new StringBuilder();
            setClause.append("SET create_time = NOW(), ");
            setClause.append("creator = '").append(loginUserId).append("', ");
            setClause.append("update_time = NOW(), ");
            setClause.append("updater = '").append(loginUserId).append("', ");
            setClause.append("tenant_id = '").append(tenantId).append("', ");
            setClause.append("create_name = '").append(loginUserName).append("', ");
            setClause.append("update_name = '").append(loginUserName).append("', ");
            setClause.append("remark = 'QAR数据导入'");
            if (qarId != null) {
                setClause.append(", qar_id = '").append(qarId).append("'");
            }
            
            // 单位转换：海里转公里 (1海里 = 1.852公里)
            // 当 selected_speed、airspeed 和 ground_speed 不为空时，把值从海里改为公里赋值给对应的 _km 字段
            if (selectedSpeedField != null) {
                setClause.append(", selected_speed_km = CASE ");
                setClause.append("WHEN ").append(selectedSpeedField).append(" IS NULL OR ").append(selectedSpeedField).append(" = '' OR ").append(selectedSpeedField).append(" = 'NULL' OR TRIM(").append(selectedSpeedField).append(") = '' THEN NULL ");
                setClause.append("ELSE CAST(CAST(").append(selectedSpeedField).append(" AS DECIMAL(20,6)) * 1.852 AS CHAR) ");
                setClause.append("END");
            }
            
            if (airspeedField != null) {
                setClause.append(", airspeed_km = CASE ");
                setClause.append("WHEN ").append(airspeedField).append(" IS NULL OR ").append(airspeedField).append(" = '' OR ").append(airspeedField).append(" = 'NULL' OR TRIM(").append(airspeedField).append(") = '' THEN NULL ");
                setClause.append("ELSE CAST(CAST(").append(airspeedField).append(" AS DECIMAL(20,6)) * 1.852 AS CHAR) ");
                setClause.append("END");
            }
            
            if (groundSpeedField != null) {
                setClause.append(", ground_speed_km = CASE ");
                setClause.append("WHEN ").append(groundSpeedField).append(" IS NULL OR ").append(groundSpeedField).append(" = '' OR ").append(groundSpeedField).append(" = 'NULL' OR TRIM(").append(groundSpeedField).append(") = '' THEN NULL ");
                setClause.append("ELSE CAST(CAST(").append(groundSpeedField).append(" AS DECIMAL(20,6)) * 1.852 AS CHAR) ");
                setClause.append("END");
            }
            
            // glideslope_deviation 和 loc_deviation 相关转换
            // 当 glideslope_deviation 不为空时，glideslope_deviation * 75 = glideslope_deviation_res
            if (glideslopeDeviationField != null) {
                setClause.append(", glideslope_deviation_res = CASE ");
                setClause.append("WHEN ").append(glideslopeDeviationField).append(" IS NULL OR ").append(glideslopeDeviationField).append(" = '' OR ").append(glideslopeDeviationField).append(" = 'NULL' OR TRIM(").append(glideslopeDeviationField).append(") = '' THEN NULL ");
                setClause.append("ELSE CAST(CAST(").append(glideslopeDeviationField).append(" AS DECIMAL(20,6)) * 75 AS CHAR) ");
                setClause.append("END");
            }
            
            // 当 loc_deviation 不为空时，loc_deviation_res =   * 75
            if (locDeviationField != null) {
                setClause.append(", loc_deviation_res = CASE ");
                setClause.append("WHEN ").append(locDeviationField).append(" IS NULL OR ").append(locDeviationField).append(" = '' OR ").append(locDeviationField).append(" = 'NULL' OR TRIM(").append(locDeviationField).append(") = '' THEN NULL ");
                setClause.append("ELSE CAST(CAST(").append(locDeviationField).append(" AS DECIMAL(20,6)) * 75 AS CHAR) ");
                setClause.append("END");
            }

            // 执行 LOAD DATA LOCAL INFILE
            sql = "LOAD DATA LOCAL INFILE '" + csvPath + "' " +
                    "INTO TABLE efvs_qar_data " +
                    "FIELDS TERMINATED BY ',' " +
                    "OPTIONALLY ENCLOSED BY '\"' " +
                    "LINES TERMINATED BY '\\n' " +
                    "IGNORE 1 ROWS " + // 跳过第一行
                    "(" + colList + ") " +
                    setClause.toString();

            // 调试日志：输出 SET 子句（包含敏感信息，仅用于调试）
            log.info("[importCsvViaLoadData][SET子句] {}", setClause.toString());
            log.debug("[importCsvViaLoadData][完整SQL] {}", sql);

            rowsAffected = stmt.executeUpdate(sql);

            if (!managedTx) {
                conn.commit();
            }

            // 恢复性能配置
            sql = "SET unique_checks = 1;";
            stmt.execute(sql);
            sql = "SET foreign_key_checks = 1;";
            stmt.execute(sql);

            if (!managedTx) {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            if (!managedTx && conn != null && !conn.getAutoCommit()) {
                try { conn.rollback(); } catch (SQLException ex) { /* 忽略回滚异常 */ }
            }
            String msg = e.getMessage() != null ? e.getMessage() : "";
            if (msg.contains("Loading local data is disabled") || msg.contains("local_infile")) {
                if (stmt != null) try { stmt.close(); } catch (SQLException ex) { /* ignore */ }
                if (conn != null) DataSourceUtils.releaseConnection(conn, dataSource);
                log.warn("[importCsvViaLoadData] MySQL local_infile 未启用，改用分批 INSERT 导入");
                return importCsvViaBatchInsert(csvFile, mapping, qarId, logicalDeleteByQarIdFirst);
            }
            throw e;
        } finally {
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { /* 忽略关闭异常 */ }
            if (conn != null) {
                if (!managedTx && !conn.getAutoCommit()) {
                    try { conn.setAutoCommit(true); } catch (SQLException e) { /* 忽略关闭异常 */ }
                }
                DataSourceUtils.releaseConnection(conn, dataSource);
            }
            // 仅对临时文件清理；从路径导入时不删除用户文件
            if (deleteFileAfterImport && csvFile.exists()) {
                try {
                    Thread.sleep(100); // 等待一段时间确保文件不再被使用
                    csvFile.delete();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        return rowsAffected;
    }

    /**
     * 当 MySQL local_infile 未启用时，通过逐行解析 CSV 并分批 INSERT 导入（与 LOAD DATA 同逻辑）
     */
    private int importCsvViaBatchInsert(File csvFile, Map<String, String> mapping, Long qarId, boolean logicalDeleteByQarIdFirst) throws Exception {
        if (logicalDeleteByQarIdFirst && qarId != null && dataSource != null) {
            try (Connection conn = DataSourceUtils.getConnection(dataSource);
                 java.sql.PreparedStatement ps = conn.prepareStatement("UPDATE efvs_qar_data SET deleted = 1 WHERE qar_id = ? AND (deleted = 0 OR deleted IS NULL)")) {
                ps.setLong(1, qarId);
                int n = ps.executeUpdate();
                log.info("[importCsvViaBatchInsert] 已按 qarId 逻辑删除原数据，影响行数: {}", n);
            }
        }
        String colList = buildColumnListFromMapping(csvFile, mapping);
        String[] dbColumns = colList.split(",");
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        final int batchSize = 200;
        int totalInserted = 0;
        List<QarDataDO> batch = new ArrayList<>(batchSize);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(csvFile), StandardCharsets.UTF_8))) {
            String firstLine = reader.readLine(); // skip header
            if (firstLine != null && firstLine.length() > 0 && firstLine.charAt(0) == '\uFEFF') {
                firstLine = firstLine.substring(1);
            }
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] values = line.split(",", -1);
                QarDataDO row = new QarDataDO();
                row.setQarId(qarId);
                row.setCreator(String.valueOf(loginUserId != null ? loginUserId : ""));
                row.setUpdater(String.valueOf(loginUserId != null ? loginUserId : ""));
                for (int i = 0; i < dbColumns.length && i < values.length; i++) {
                    String col = dbColumns[i].trim();
                    if (col.startsWith("@dummy")) continue;
                    BiConsumer<QarDataDO, String> setter = DB_COLUMN_SETTERS.get(col);
                    if (setter != null) {
                        String val = values[i] == null ? null : values[i].trim();
                        if ("NULL".equalsIgnoreCase(val) || "".equals(val)) val = null;
                        setter.accept(row, val);
                    }
                }
                batch.add(row);
                if (batch.size() >= batchSize) {
                    for (QarDataDO d : batch) dataMapper.insert(d);
                    totalInserted += batch.size();
                    batch.clear();
                }
            }
            for (QarDataDO d : batch) dataMapper.insert(d);
            totalInserted += batch.size();
        }
        log.info("[importCsvViaBatchInsert] 分批 INSERT 完成，共 {} 条", totalInserted);
        return totalInserted;
    }

    @Override
    public List<TaxiPhaseCycleVO> getTaxiPhaseCycles() {
        // 查询所有数据（包括AIR和GROUND状态，按时间排序）
        // 用于识别起飞事件（从GROUND变为AIR，或从AIR变为GROUND）
        LambdaQueryWrapper<QarDataDO> queryWrapper = new LambdaQueryWrapper<QarDataDO>()
                .isNotNull(QarDataDO::getEmptySpaceSwitch)
                .ne(QarDataDO::getEmptySpaceSwitch, "")
                .ne(QarDataDO::getEmptySpaceSwitch, "NULL")
                .orderByAsc(QarDataDO::getTime);

        List<QarDataDO> allData = dataMapper.selectList(queryWrapper);

        if (allData.isEmpty()) {
            return new ArrayList<>();
        }

        // 第一步：识别所有滑跑阶段（GROUND状态，且不是AIR）
        // 滑跑阶段：从GROUND开始，到下一次AIR开始前结束
        List<TaxiPhaseCycleVO> cycles = new ArrayList<>();
        int cycleIndex = 1;
        Integer startId = null;
        String startTime = null;
        int dataCount = 0;
        String lastEmptySpaceSwitch = null;

        for (int i = 0; i < allData.size(); i++) {
            QarDataDO data = allData.get(i);
            String currentEmptySpaceSwitch = data.getEmptySpaceSwitch();
            
            // 判断是否为滑跑阶段（GROUND状态，且不是AIR）
            boolean isGroundPhase = currentEmptySpaceSwitch != null 
                    && !currentEmptySpaceSwitch.equalsIgnoreCase("AIR")
                    && !currentEmptySpaceSwitch.trim().isEmpty()
                    && !currentEmptySpaceSwitch.equalsIgnoreCase("NULL");

            if (isGroundPhase) {
                // 当前是滑跑阶段
                if (startId == null) {
                    // 开始新的滑跑周期（从GROUND开始）
                    startId = data.getId();
                    startTime = data.getTime();
                    dataCount = 1;
                } else {
                    // 继续当前周期
                    dataCount++;
                }
            } else {
                // 当前是AIR状态（空中阶段）
                if (startId != null) {
                    // 遇到AIR状态，结束当前滑跑周期
                    // 找到前一个GROUND数据的ID作为结束ID
                    Integer endId = null;
                    String endTime = null;
                    for (int j = i - 1; j >= 0; j--) {
                        QarDataDO prevData = allData.get(j);
                        String prevSwitch = prevData.getEmptySpaceSwitch();
                        if (prevSwitch != null 
                                && !prevSwitch.equalsIgnoreCase("AIR")
                                && !prevSwitch.trim().isEmpty()
                                && !prevSwitch.equalsIgnoreCase("NULL")) {
                            endId = prevData.getId();
                            endTime = prevData.getTime();
                            break;
                        }
                    }
                    
                    if (endId != null) {
                        // 保存当前周期
                        TaxiPhaseCycleVO cycle = new TaxiPhaseCycleVO();
                        cycle.setCycleIndex(cycleIndex++);
                        cycle.setStartTime(startTime);
                        cycle.setStartId(startId);
                        cycle.setEndTime(endTime);
                        cycle.setEndId(endId);
                        cycle.setDataCount((long) dataCount);
                        cycle.setDescription("第" + (cycleIndex - 1) + "次滑跑");
                        cycles.add(cycle);
                    }
                    
                    // 重置，等待下一个GROUND开始
                    startId = null;
                    dataCount = 0;
                }
            }
            
            lastEmptySpaceSwitch = currentEmptySpaceSwitch;
        }

        // 添加最后一个周期（如果数据以GROUND结束）
        if (startId != null) {
            TaxiPhaseCycleVO cycle = new TaxiPhaseCycleVO();
            cycle.setCycleIndex(cycleIndex);
            cycle.setStartTime(startTime);
            cycle.setStartId(startId);
            // 找到最后一个GROUND数据
            QarDataDO lastGroundData = null;
            for (int i = allData.size() - 1; i >= 0; i--) {
                QarDataDO data = allData.get(i);
                String switchState = data.getEmptySpaceSwitch();
                if (switchState != null 
                        && !switchState.equalsIgnoreCase("AIR")
                        && !switchState.trim().isEmpty()
                        && !switchState.equalsIgnoreCase("NULL")) {
                    lastGroundData = data;
                    break;
                }
            }
            if (lastGroundData != null) {
                cycle.setEndTime(lastGroundData.getTime());
                cycle.setEndId(lastGroundData.getId());
            } else {
                cycle.setEndTime(startTime);
                cycle.setEndId(startId);
            }
            cycle.setDataCount((long) dataCount);
            cycle.setDescription("第" + cycleIndex + "次滑跑");
            cycles.add(cycle);
        }

        return cycles;
    }

    @Override
    public void deleteByQarId(Long id) {
        dataMapper.deleteByQarId(id);
    }

    private String normalizePath(String path) {
        if (path == null || path.isEmpty()) {
            return path;
        }
        // 替换多个连续的斜杠为单个斜杠（兼容 Windows 和 Linux）
        // 注意：Windows 盘符后的冒号不会被影响，如 C://test -> C:/test
        return path.replaceAll("[/\\\\]+", File.separator);
    }

    /**
     * 规范化 URL，移除路径部分多余的斜杠，避免出现 "//"（协议部分的 :// 除外）
     *
     * @param url 待规范化的 URL
     * @return 规范化后的 URL
     */
    private String normalizeUrl(String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }

        // 保留协议部分（http:// 或 https://），只处理路径部分的多余斜杠
        String protocol = "";
        String path = url;

        if (url.startsWith("http://")) {
            protocol = "http://";
            path = url.substring(7);
        } else if (url.startsWith("https://")) {
            protocol = "https://";
            path = url.substring(8);
        }

        // 替换路径中多个连续的斜杠为单个斜杠
        path = path.replaceAll("/+", "/");

        return protocol + path;
    }

}
