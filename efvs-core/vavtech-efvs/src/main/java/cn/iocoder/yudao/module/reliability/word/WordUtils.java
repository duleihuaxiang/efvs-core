package cn.iocoder.yudao.module.reliability.word;

import cn.idev.excel.annotation.ExcelProperty;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.List;
import java.util.stream.Collectors;


public class WordUtils {

    public static void write(HttpServletResponse response, String fileName,
                             String tableTitle, List<?> dataList, Class<?> clazz) throws IOException {
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);

        try (XWPFDocument doc = new XWPFDocument()) {
            // 标题
            XWPFParagraph title = doc.createParagraph();
            XWPFRun run = title.createRun();
            run.setBold(true);
            run.setFontSize(16);
            run.setText(tableTitle);

            // 表格（至少1行，避免空表异常）
            int rowCount = dataList.isEmpty() ? 1 : dataList.size() + 1;
            int colCount = getDeclaredFieldCount(clazz);
            XWPFTable table = doc.createTable(rowCount, colCount);
            table.setWidth("100%");

            // 表头（字段名）
            List<String> fieldNames = getFieldNames(clazz);
            for (int i = 0; i < fieldNames.size(); i++) {
                table.getRow(0).getCell(i).setText(fieldNames.get(i));
            }

//            // 数据行
//            for (int r = 0; r < dataList.size(); r++) {
//                Object item = dataList.get(r);
//                XWPFTableRow row = table.getRow(r + 1);
//                for (int c = 0; c < fieldNames.size(); c++) {
//                    String fieldName = fieldNames.get(c);
//                    String value = getFieldValue(item, fieldName);
//                    row.getCell(c).setText(value);
//                }
//            }
            // 数据行
            List<String> actualFieldNames = getFieldNamesForReflection(clazz); // 原始字段名（如 partNo）
            for (int r = 0; r < dataList.size(); r++) {
                Object item = dataList.get(r);
                XWPFTableRow row = table.getRow(r + 1);
                for (int c = 0; c < fieldNames.size(); c++) {
                    String actualFieldName = actualFieldNames.get(c); // 用原始字段名取值
                    String value = getFieldValue(item, actualFieldName);
                    row.getCell(c).setText(value);
                }
            }
            doc.write(response.getOutputStream());
        }
    }

    private static int getDeclaredFieldCount(Class<?> clazz) {
        return clazz.getDeclaredFields().length;
    }

    private static List<String> getFieldNames(Class<?> clazz) {
        return java.util.Arrays.stream(clazz.getDeclaredFields())
                .map(field -> {
                    // 优先读取 @ExcelProperty 的 value
                    ExcelProperty excelProp = field.getAnnotation(ExcelProperty.class);
                    if (excelProp != null && excelProp.value().length > 0 && !excelProp.value()[0].isEmpty()) {
                        return excelProp.value()[0]; // 返回中文标题（取第一个值）
                    }
                    // 回退到字段名（兜底）
                    return field.getName();
                })
                .collect(Collectors.toList());
    }

    // 新增：获取原始字段名列表（用于反射取值）
    private static List<String> getFieldNamesForReflection(Class<?> clazz) {
        return java.util.Arrays.stream(clazz.getDeclaredFields())
                .map(java.lang.reflect.Field::getName)
                .collect(Collectors.toList());
    }

    private static String getFieldValue(Object obj, String fieldName) {
        try {
            String methodName = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
            Method method = obj.getClass().getMethod(methodName);
            Object val = method.invoke(obj);
            return val == null ? "" : String.valueOf(val);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 写入多个表格到 Word 文档
     *
     * @param response     HTTP 响应对象
     * @param fileName     文件名
     * @param tableTitles  表格标题列表
     * @param dataListList 数据列表列表（每个元素对应一个表格的数据）
     */
    public static void writeMultipleTables(HttpServletResponse response, String fileName,
                                           List<String> tableTitles, List<List<?>> dataListList) throws IOException {
        XWPFDocument document = new XWPFDocument();

        for (int i = 0; i < tableTitles.size(); i++) {
            String title = tableTitles.get(i);
            List<?> dataList = dataListList.get(i);

            // 创建表格并填充数据
            if (!dataList.isEmpty()) {
                Class<?> clazz = dataList.get(0).getClass();
                createTable(document, title, dataList, clazz);
            }

            // 添加空行作为分隔
            document.createParagraph();
        }

        // 输出文件
        response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8"));
        document.write(response.getOutputStream());
        document.close();
    }

    /**
     * 创建表格并填充数据
     */
    private static void createTable(XWPFDocument doc, String tableTitle, List<?> dataList, Class<?> clazz) {
        // 创建标题段落
        XWPFParagraph titleParagraph = doc.createParagraph();
        XWPFRun titleRun = titleParagraph.createRun();
        titleRun.setBold(true);
        titleRun.setFontSize(16);
        titleRun.setText(tableTitle);

        // 计算表格行列数
        int rowCount = dataList.isEmpty() ? 1 : dataList.size() + 1; // 至少保留一行表头
        int colCount = getDeclaredFieldCount(clazz); // 字段数量即列数

        // 创建表格
        XWPFTable table = doc.createTable(rowCount, colCount);
        table.setWidth("100%");

        // 获取表头字段名（支持 @ExcelProperty 注解）
        List<String> fieldNames = getFieldNames(clazz);
        List<String> actualFieldNames = getFieldNamesForReflection(clazz); // 原始字段名用于反射取值

        // 填充表头
        for (int i = 0; i < fieldNames.size(); i++) {
            table.getRow(0).getCell(i).setText(fieldNames.get(i));
        }

        // 填充数据行
        for (int r = 0; r < dataList.size(); r++) {
            Object item = dataList.get(r);
            XWPFTableRow row = table.getRow(r + 1);
            for (int c = 0; c < fieldNames.size(); c++) {
                String actualFieldName = actualFieldNames.get(c); // 使用原始字段名取值
                String value = getFieldValue(item, actualFieldName);
                row.getCell(c).setText(value);
            }
        }
    }

}

