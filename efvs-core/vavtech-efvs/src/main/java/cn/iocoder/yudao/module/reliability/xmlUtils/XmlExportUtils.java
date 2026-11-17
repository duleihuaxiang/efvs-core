package cn.iocoder.yudao.module.reliability.xmlUtils;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * XML 导出工具类 —— 支持多区块（section）结构，每个区块使用其对应的数据类进行反射
 */
public class XmlExportUtils {

    /**
     * 通用 XML 导出：支持多块（section）结构，每块含标题 + 数据列表
     *
     * @param response      HTTP 响应
     * @param fileName      文件名（建议含后缀 .xml）
     * @param sections      各区块配置：每个元素为 (sectionName, title, dataList, dataClass)
     */
    public static void write(HttpServletResponse response,
                             String fileName,
                             List<Section> sections
                             ) throws IOException {
        // 安全化文件名（避免非法字符导致下载失败）
        String safeFileName = fileName.replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5._-]", "_");
        response.setContentType(MediaType.APPLICATION_XML_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=" + URLEncoder.encode(safeFileName, "UTF-8")); // ✅ 修复：String charset

        try (Writer writer = new java.io.OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8)) {
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writer.write("<reliabilityReport>\n");

            for (Section section : sections) {
                writer.write("  <" + escapeXml(section.sectionName) + ">\n");
                writer.write("    <title>" + escapeXml(section.title) + "</title>\n");
                for (Object item : section.dataList) {
                    writer.write("    <record>");
                    writeObjectAsXml(writer, item, section.dataClass); // ✅ 修复：按 section.dataClass 反射
                    writer.write("</record>\n");
                }
                writer.write("  </" + escapeXml(section.sectionName) + ">\n");
            }

            writer.write("</reliabilityReport>");
        } catch (IOException e) {
            throw new IOException("导出 XML 失败", e); // ✅ 修复：不依赖 ServiceExceptionUtil
        }
    }

    // 辅助：XML 转义
    public static String escapeXml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    // 辅助：反射写对象为 XML 字段（仅基础类型）
    public static void writeObjectAsXml(Writer writer, Object obj, Class<?> clazz) throws IOException {
        if (obj == null) {
            writer.write("<null/>");
            return;
        }
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            String fieldName = field.getName();
            try {
                Object value = field.get(obj);
                String escapedValue = escapeXml(String.valueOf(value));
                writer.write("<" + escapeXml(fieldName) + ">" + escapedValue + "</" + escapeXml(fieldName) + ">");
            } catch (IllegalAccessException e) {
                writer.write("<" + escapeXml(fieldName) + ">[access_error]</" + escapeXml(fieldName) + ">");
            }
        }
    }

    // 内部类：表示一个 XML 区块（含数据类信息）
    public static class Section {
        public final String sectionName; // 如 "mtbf"
        public final String title;      // 如 "平均故障间隔时间(MTBF)"
        public final List<?> dataList;
        public final Class<?> dataClass; // ✅ 关键：记录该数据的实际类

        public Section(String sectionName, String title, List<?> dataList, Class<?> dataClass) {
            this.sectionName = sectionName;
            this.title = title;
            this.dataList = dataList;
            this.dataClass = dataClass;
        }
    }
}
