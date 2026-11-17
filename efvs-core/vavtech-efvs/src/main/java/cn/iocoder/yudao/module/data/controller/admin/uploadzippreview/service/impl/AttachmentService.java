package cn.iocoder.yudao.module.data.controller.admin.uploadzippreview.service.impl;


import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.data.controller.admin.uploadzippreview.dto.FileUploadDto;
import cn.iocoder.yudao.module.data.controller.admin.uploadzippreview.dto.FileUploadResult;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.FileConfigDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.FileDO;
import cn.iocoder.yudao.module.infra.framework.file.core.client.FileClient;
import cn.iocoder.yudao.module.infra.framework.file.core.client.FileClientConfig;
import cn.iocoder.yudao.module.infra.framework.file.core.client.db.DBFileClientConfig;
import cn.iocoder.yudao.module.infra.framework.file.core.client.ftp.FtpFileClientConfig;
import cn.iocoder.yudao.module.infra.framework.file.core.client.local.LocalFileClientConfig;
import cn.iocoder.yudao.module.infra.framework.file.core.client.s3.S3FileClientConfig;
import cn.iocoder.yudao.module.infra.framework.file.core.client.sftp.SftpFileClientConfig;
import cn.iocoder.yudao.module.infra.framework.file.core.utils.FileTypeUtils;
import cn.iocoder.yudao.module.infra.service.file.FileConfigService;
import cn.iocoder.yudao.module.training.controller.admin.trainingexperience.vo.TrainingExperienceRespVO;
import cn.iocoder.yudao.module.training.controller.admin.trainingexperience.vo.TrainingExperienceSaveReqVO;
import cn.iocoder.yudao.module.training.dal.dataobject.trainingexperience.FileUploadDO;
import cn.iocoder.yudao.module.training.dal.mysql.trainingexperience.FileUploadMapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.annotations.VisibleForTesting;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.annotation.Resource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import static cn.hutool.core.date.DatePattern.PURE_DATE_PATTERN;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.data.enums.ErrorCodeConstants.FILE_NOT_SCORM_PACKAGE;
import static cn.iocoder.yudao.module.infra.enums.ErrorCodeConstants.FILE_NOT_EXISTS;

@Service
public class AttachmentService {

    @Autowired
    private FileUploadMapper attachmentMapper;

    // 本地文件存储根路径（实际项目建议用OSS/MinIO，这里简化为本地）
    @Value("${spring.servlet.multipart.location}")
    private String LOCAL_STORAGE_PATH;
    @Resource
    private FileConfigService fileConfigService;

    /**
     * 上传文件的前缀，是否包含日期（yyyyMMdd）
     *
     * 目的：按照日期，进行分目录
     */
    static boolean PATH_PREFIX_DATE_ENABLE = true;
    /**
     * 上传文件的后缀，是否包含时间戳
     *
     * 目的：保证文件的唯一性，避免覆盖
     * 定制：可按需调整成 UUID、或者其他方式
     */
    static boolean PATH_SUFFIX_TIMESTAMP_ENABLE = true;

    public List<FileUploadDO> getFileUploadResultsByIds(List<Integer> allFileUploadIds) {
        return attachmentMapper.selectListByIds(allFileUploadIds);
    }

    public List<FileUploadDO> selectListByIds(List<Integer> ids) {
        return attachmentMapper.selectListByIds(ids);
    }

    public class ScormInfo {
        private boolean isScorm;
        private String version;
        private String type;
        private String entryFileName; // 新增：记录入口文件名 (player.html 或 index.html)
        private String entryFileRelativePath; // 新增：记录入口文件的相对路径

        // 构造函数
        public ScormInfo(boolean isScorm, String version, String type,  String entryFileName, String entryFileRelativePath) {
            this.isScorm = isScorm;
            this.version = version;
            this.type = type;
            this.entryFileName = entryFileName;
            this.entryFileRelativePath = entryFileRelativePath;
        }

        // 原有的构造函数可以调用新构造函数传 null，或者保留兼容
        public ScormInfo(boolean isScorm, String version, String type) {
            this(isScorm, version, type, null, null);
        }

        // getters and setters
        public boolean isScorm() { return isScorm; }
        public String getVersion() { return version; }
        public String getType() { return type; }
        // 移除 getPlayerHtmlPath，改为通用的获取入口路径方法
        public String getEntryFileRelativePath() { return entryFileRelativePath; }
        public void setEntryFileRelativePath(String path) { this.entryFileRelativePath = path; }
        public String getEntryFileName() { return entryFileName; }
    }


    /**
     * 多文件上传核心方法
     * @param files 前端传递的多文件数组（MultipartFile[]）
     * @param directory 存储目录（前端传递的文本参数）
     * @return 保存成功的附件列表（含自动生成的ID）
     */
//    public List<Integer> uploadFiles(MultipartFile[] files, String directory) {
//        // 1. 校验参数
//        if (files == null || files.length == 0) {
//            throw new RuntimeException("请选择要上传的文件");
//        }
//        if (directory == null || directory.trim().isEmpty()) {
//            throw new RuntimeException("存储目录不能为空");
//        }
//
//        // 2. 获取当前日期作为文件夹名
//        String dateFolder = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
//
//
//        // 3. 初始化存储目录（本地路径）- 包含日期文件夹
//        File rootDir = new File(LOCAL_STORAGE_PATH + directory + File.separator + dateFolder);
//        if (!rootDir.exists()) {
//            rootDir.mkdirs(); // 递归创建目录
//        }
//
//        // 4. 遍历处理每个文件
//        List<Integer> attachmentIds = new ArrayList<>();
//        for (MultipartFile file : files) {
//            // 跳过空文件
//            if (file.isEmpty()) {
//                continue;
//            }
//            // 4.1 获取文件基本信息
//            String originalFileName = file.getOriginalFilename(); // 原始文件名（如test.jpg）
//            try {
//                Long fileSize = file.getSize(); // 文件大小（字节）
//                String fileType = file.getContentType(); // 文件MIME类型（如image/jpeg）
//
//                // 4.2 生成唯一文件名（避免重复）
//                String uniqueFileName = UUID.randomUUID() + originalFileName.substring(originalFileName.lastIndexOf("."));
//                // 4.3 构建文件存储路径
//                File targetFile = new File(rootDir, uniqueFileName);
//
//                // 4.4 保存文件到本地（实际项目替换为OSS上传逻辑）
//                file.transferTo(targetFile);
//
//                // 4.5 构建附件实体（文件访问路径用本地路径示例，实际用HTTP可访问地址）
//                String fileUrl = targetFile.getAbsolutePath(); // 本地路径（替换为OSS公网地址）
//                FileUploadDO fileUploadDO = new FileUploadDO(originalFileName,String.valueOf(fileSize), fileUrl, fileType, directory);
//                if (StringUtils.isNotBlank(fileUploadDO.getFileUrl())) {
//                    attachmentMapper.insert(fileUploadDO);
//                    attachmentIds.add(fileUploadDO.getId());
//                }
//            } catch (IOException e) {
//                throw new RuntimeException("文件上传失败：" + originalFileName, e);
//            }
//        }
//        return attachmentIds;
//    }
//
    public void updateByidByTenantId(List<Integer> zipId, Long trainingId, Long tenantId) {
        attachmentMapper.updateByidByTenantId(zipId, trainingId, tenantId);
    }
//
//    /**
//     * 从 JSON 数据中上传文件（base64 解码后保存）
//     * @param fileUploads 文件列表（包含 base64 编码）
//     * @param directory 存储目录
//     * @return 成功上传的附件 ID 列表
//     */
//    public List<Integer> uploadFilesFromJson(List<FileUploadDto> fileUploads, String directory) {
//        if (fileUploads == null || fileUploads.isEmpty()) {
//            throw new RuntimeException("文件列表不能为空");
//        }
//        if (directory == null || directory.trim().isEmpty()) {
//            throw new RuntimeException("存储目录不能为空");
//        }
//
//        String dateFolder = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
//        File rootDir = new File(LOCAL_STORAGE_PATH + directory + File.separator + dateFolder);
//        if (!rootDir.exists()) {
//            rootDir.mkdirs();
//        }
//
//        List<Integer> attachmentIds = new ArrayList<>();
//        for (FileUploadDto dto : fileUploads) {
//            if (dto == null || StringUtils.isBlank(dto.getFileBase64())) {
//                continue;
//            }
//
//            try {
//                byte[] bytes = Base64.getDecoder().decode(dto.getFileBase64());
//                String originalFileName = dto.getFileName();
//                String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
//                String uniqueFileName = UUID.randomUUID() + extension;
//
//                File targetFile = new File(rootDir, uniqueFileName);
//                FileUtils.writeByteArrayToFile(targetFile, bytes);
//
//                String fileUrl = targetFile.getAbsolutePath();
//                FileUploadDO fileUploadDO = new FileUploadDO(
//                        originalFileName,
//                        String.valueOf(bytes.length),
//                        fileUrl,
//                        dto.getFileType(),
//                        directory
//                );
//                attachmentMapper.insert(fileUploadDO);
//                attachmentIds.add(fileUploadDO.getId());
//
//            } catch (Exception e) {
//                throw new RuntimeException("文件上传失败：" + dto.getFileName(), e);
//            }
//        }
//        return attachmentIds;
//    }

    public List<FileUploadResult> createFile(byte[] content, String originalFilename, String directory, String contentType) throws Exception {
        // 1.1 处理 type 为空的情况
        if (StrUtil.isEmpty(contentType)) {
            contentType = FileTypeUtils.getMineType(content, originalFilename);
        }
        // 1.2 处理 name 为空的情况
        if (StrUtil.isEmpty(originalFilename)) {
            originalFilename = DigestUtil.sha256Hex(content);
        }
        if (StrUtil.isEmpty(FileUtil.extName(originalFilename))) {
            // 如果 name 没有后缀 type，则补充后缀
            String extension = FileTypeUtils.getExtension(contentType);
            if (StrUtil.isNotEmpty(extension)) {
                originalFilename = originalFilename + extension;
            }
        }

        // 2.1 生成上传的 path，需要保证唯一
        String path = generateUploadPath(originalFilename, directory);
        // 2.2 上传到文件存储器
        FileClient client = fileConfigService.getMasterFileClient();
        Assert.notNull(client, "客户端(master) 不能为空");
        String url = client.upload(content, path, contentType);

        // 2.3 处理解压逻辑和SCORM类型识别
        String filePreviewPath = null;
        String typeName = null; // 新增：记录入口文件类型
        String remark = contentType; // 默认使用contentType作为remark
        if (isZipFile(originalFilename)) {
            // 对于ZIP文件，检查是否为SCORM包
            ScormInfo scormInfo = getScormInfoFromBytes(content);
            if (scormInfo.isScorm()) {
                // 是SCORM包，执行解压
                String unzipBasePath = unzipFile(content, originalFilename);
                // 新增：获取入口文件类型
                typeName = scormInfo.getEntryFileName();

                String entryPath = scormInfo.getEntryFileRelativePath();
                // 如果有player.html路径，则组合完整路径
                if (entryPath != null) {
                    // 获取player.html所在目录的父路径
                    int lastSlashIndex = Math.max(entryPath.lastIndexOf('/'), entryPath.lastIndexOf('\\'));
                    if (lastSlashIndex != -1) {
                        String entryFileParentPath = entryPath.substring(0, lastSlashIndex);
                        filePreviewPath = unzipBasePath + File.separator + entryFileParentPath;
                    } else {
                        filePreviewPath = unzipBasePath; // 如果player.html在根目录
                    }
                } else {
                    filePreviewPath = unzipBasePath;
                }
                // 将SCORM类型信息设置到remark字段
                remark = scormInfo.getType() + " (Version: " + scormInfo.getVersion() + ")";
            } else {
                // 不是SCORM包，抛出异常
                throw exception(FILE_NOT_SCORM_PACKAGE);
            }
        }
        FileConfigDO fileConfig = attachmentMapper.selectFileConfig();
        LocalFileClientConfig config = (LocalFileClientConfig) fileConfig.getConfig();
        // 计算文件大小（以KB为单位），并保留两位小数
        // 预览路径会写入数据库并返回前端，必须使用 URL 标准分隔符，避免 Windows 返回反斜杠。
        filePreviewPath = filePreviewPath == null ? null : filePreviewPath.replace('\\', '/');
        BigDecimal fileSizeInKB = new BigDecimal(content.length)
                .divide(new BigDecimal(1024), 2, RoundingMode.HALF_UP);
        FileUploadDO fileUploadDO = new FileUploadDO();
        fileUploadDO.setFileName(originalFilename)
                .setFileAbsoluteUrl(config.getBasePath() + "/" + path)
                .setFileUrl(url)
                .setFileSize(String.valueOf(fileSizeInKB))
                .setRemark(remark) // 使用更新后的remark值
                .setFilePreviewPath(filePreviewPath)
                .setTypeName(typeName);
        // 3. 保存到数据库
        List<FileUploadResult> urls = new ArrayList<>();
        attachmentMapper.insert(fileUploadDO);
        FileUploadResult fileUploadResult = new FileUploadResult();
        fileUploadResult.setId(fileUploadDO.getId());
        fileUploadResult.setFileUrl(fileUploadDO.getFileUrl());
        fileUploadResult.setFilePreviewPath(fileUploadDO.getFilePreviewPath());
        fileUploadResult.setName(fileUploadDO.getFileName());
        fileUploadResult.setSize(fileUploadDO.getFileSize());
        fileUploadResult.setTypeName(typeName);
        urls.add(fileUploadResult);
        return urls;
    }


    /**
     * 从字节数组直接检查是否为SCORM课件包（无需先解压到磁盘）
     */

    private ScormInfo getScormInfoFromBytes(byte[] content) throws IOException {
        // 前置校验：字节数组为空直接返回false
        if (content == null || content.length == 0) {
            return new ScormInfo(false, null, null);
        }

        File tempZipFile = null;
        ZipFile zipFile = null;
        try {
            // 1. 创建临时文件存储ZIP字节数组（适配JDK8+低版本zip4j）
            tempZipFile = Files.createTempFile("scorm_temp_", ".zip").toFile();
            try (FileOutputStream fos = new FileOutputStream(tempZipFile)) {
                fos.write(content);
            }

            // 2. 初始化ZipFile
            zipFile = new ZipFile(tempZipFile);
            // 【关键修改】自动检测并设置字符集
            Charset charset = detectZipCharset(tempZipFile);
            zipFile.setCharset(charset);


            // 3. 遍历ZIP内所有文件，查找任意路径下的imsmanifest.xml（兼容嵌套文件夹）
            List<FileHeader> allFileHeaders = zipFile.getFileHeaders();
            FileHeader manifestHeader = null;
            FileHeader entryFileHeader = null; // 统一处理 player.html 或 index.html
            String entryFilePath = null;
            String entryFileNameLower = null;

            // 一次遍历查找所有需要的文件
            for (FileHeader header : allFileHeaders) {
                String fileName = header.getFileName().toLowerCase();
                System.out.println("ZIP 内文件：" + fileName);
                System.out.println("ZIP 打印原始文件名内文件：" + header.getFileName());
                // 过滤文件夹，只匹配文件；且文件名以imsmanifest.xml结尾（兼容任意嵌套路径）
                if (!header.isDirectory()) {
                    if (fileName.endsWith("imsmanifest.xml")) {
                        manifestHeader = header;
                    }
                    // 优先查找 player.html
                    else if (fileName.endsWith("player.html")) {
                        entryFileHeader = header;
                        entryFilePath = header.getFileName();
                        entryFileNameLower = "player.html";
                        System.out.println("匹配到 player.html: " + fileName);
                    }
                    // 如果没有 player.html，暂时记录 index.html (可能被覆盖，但逻辑上优先 player)
                    else if (fileName.endsWith("index.html") && entryFileHeader == null) {
                        entryFileHeader = header;
                        entryFilePath = header.getFileName();
                        entryFileNameLower = "index.html";
                        System.out.println("匹配到 index.html: " + fileName);
                    }
                }

                // 如果两个文件都找到了，可以提前结束循环
                if (manifestHeader != null && entryFileHeader != null) {
                    break;
                }
            }

            // 未找到任何imsmanifest.xml，直接返回false
            if (manifestHeader == null) {
                return new ScormInfo(false, null, null);
            }

            // 4. 解析找到的imsmanifest.xml（无论在哪个路径下）
            try (InputStream manifestStream = zipFile.getInputStream(manifestHeader)) {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(manifestStream);
                Element root = doc.getDocumentElement();

                // 5. 提取SCORM信息
                // 5.1 获取命名空间
                String namespace = root.getNamespaceURI();

                // 5.2 获取metadata下的schemaversion
                NodeList metadataList = doc.getElementsByTagName("metadata");
                String version = null;
                if (metadataList.getLength() > 0) {
                    Element metadata = (Element) metadataList.item(0);
                    NodeList schemaVersionList = metadata.getElementsByTagName("schemaversion");
                    if (schemaVersionList.getLength() > 0) {
                        version = schemaVersionList.item(0).getTextContent().trim();
                    }
                }

                // 5.3 确定SCORM类型
                String scormType = "Unknown";
                if ("http://www.imsproject.org/xsd/imscp_rootv1p1p2".equals(namespace) &&
                        "1.2".equals(version)) {
                    scormType = "SCORM 1.2";
                } else if ("http://www.imsglobal.org/xsd/imscp_v1p1".equals(namespace) &&
                        "CAM 1.3".equals(version)) {
                    scormType = "SCORM 2004";
                }

                // 返回SCORM信息和player.html路径（只有在找到imsmanifest.xml时才会到这里）
                return new ScormInfo(true, version, scormType, entryFileNameLower, entryFilePath);

            } catch (Exception e) {
                // XML解析失败（非标准SCORM XML）
                return new ScormInfo(false, null, null);
            }

        } catch (ZipException e) {
            // 不是有效ZIP包
            return new ScormInfo(false, null, null);
        } finally {
            // 释放资源：关闭ZipFile + 删除临时文件
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (tempZipFile != null && tempZipFile.exists()) {
                boolean deleted = tempZipFile.delete();
                if (!deleted) {
                    tempZipFile.deleteOnExit();
                }
            }
        }
    }



    /**
     * 获取XML元素的属性值
     */
    private String getAttributeValue(Element element, String attributeName) {
        return element.getAttribute(attributeName);
    }


    @VisibleForTesting
    String generateUploadPath(String name, String directory) {
        // 1. 生成前缀、后缀
        String prefix = null;
        if (PATH_PREFIX_DATE_ENABLE) {
            prefix = LocalDateTimeUtil.format(LocalDateTimeUtil.now(), PURE_DATE_PATTERN);
        }
        String suffix = null;
        if (PATH_SUFFIX_TIMESTAMP_ENABLE) {
            suffix = String.valueOf(System.currentTimeMillis());
        }

        // 2.1 先拼接 suffix 后缀
        if (StrUtil.isNotEmpty(suffix)) {
            String ext = FileUtil.extName(name);
            if (StrUtil.isNotEmpty(ext)) {
                name = FileUtil.mainName(name) + StrUtil.C_UNDERLINE + suffix + StrUtil.DOT + ext;
            } else {
                name = name + StrUtil.C_UNDERLINE + suffix;
            }
        }
        // 2.2 再拼接 prefix 前缀
        if (StrUtil.isNotEmpty(prefix)) {
            name = prefix + StrUtil.SLASH + name;
        }
        // 2.3 最后拼接 directory 目录
        if (StrUtil.isNotEmpty(directory)) {
            name = directory + StrUtil.SLASH + name;
        }
        return name;
    }
    /**
     * 判断是否为ZIP文件
     */
    private boolean isZipFile(String filename) {
        return filename != null && (filename.toLowerCase().endsWith(".zip")
                || filename.toLowerCase().endsWith(".rar")
                || filename.toLowerCase().endsWith(".7z"));
    }
    /**
     * 解压文件并返回预览路径
     */
    private String unzipFile(byte[] content, String originalFilename) throws IOException {
        // 生成唯一的解压目录
        FileConfigDO fileConfig = attachmentMapper.selectFileConfig();
        LocalFileClientConfig config = (LocalFileClientConfig) fileConfig.getConfig();
        String unzipUuid = UUID.randomUUID().toString();
        String unzipBasePath = config.getBasePath() + File.separator + "filePreviewPath" + File.separator + unzipUuid;
        File unzipDir = new File(unzipBasePath);

        if (!unzipDir.exists()) {
            unzipDir.mkdirs();
        }

        // 将字节数组写入临时文件
        String tempFilePath = LOCAL_STORAGE_PATH + File.separator + "temp" + File.separator + System.currentTimeMillis() + "_" + originalFilename;
        File tempFile = new File(tempFilePath);
        File tempDir = tempFile.getParentFile();
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }
        FileUtils.writeByteArrayToFile(tempFile, content);

        try {
            // 执行解压操作
            net.lingala.zip4j.ZipFile zipFile = new net.lingala.zip4j.ZipFile(tempFile);
            // 【关键修改】自动检测并设置字符集
            Charset charset = detectZipCharset(tempFile);
            zipFile.setCharset(charset);
            zipFile.extractAll(unzipBasePath);

            // 返回实际的本地解压路径
            return unzipBasePath; // 返回实际路径而不是固定IP
        } catch (Exception e) {
            throw new IOException("文件解压失败: " + originalFilename, e);
        } finally {
            // 删除临时文件
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }
    /**
     * 检测 ZIP 文件应使用的字符集
     * 优先尝试 UTF-8，如果检测到乱码则回退到 GBK
     */
    private Charset detectZipCharset(File zipFile) throws IOException {
        // 先尝试 UTF-8
        ZipFile zipFileUtf8 = new ZipFile(zipFile);
        zipFileUtf8.setCharset(StandardCharsets.UTF_8);

        List<FileHeader> headers = zipFileUtf8.getFileHeaders();
        for (FileHeader header : headers) {
            if (!header.isDirectory()) {
                String fileName = header.getFileName();
                // 检测是否包含乱码特征（UTF-8 解码 GBK 文件时常见乱码）
                if (containsMojibake(fileName)) {
                    zipFileUtf8.close();
                    return Charset.forName("GBK");
                }
            }
        }
        zipFileUtf8.close();
        return StandardCharsets.UTF_8;
    }

    /**
     * 检测字符串是否包含乱码特征。
     *
     * 不能仅凭「含有非 ASCII 字符」就判定乱码——汉字本身就是非 ASCII。
     * 正常中文文件名的非 ASCII 字符几乎全是汉字；而真乱码（字节被按错误码页解出）
     * 的非 ASCII 字符多为拉丁扩展/符号，汉字占比很低。据此区分。
     */
    private boolean containsMojibake(String str) {
        if (str == null || str.isEmpty()) return false;
        int nonAscii = 0;
        int cjk = 0;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c > 0x7F) {
                nonAscii++;
                if (c >= 0x4E00 && c <= 0x9FFF) {
                    cjk++;
                }
            }
        }
        if (nonAscii == 0) {
            return false;
        }
        // 汉字不到非 ASCII 字符的一半 → 判为乱码，回退 GBK
        return cjk * 2 < nonAscii;
    }


    public String getFileNameById(Integer fileId) {
        return attachmentMapper.selectNameById(fileId);

    }

    public void updateByexperienceIdByTenantId(Long experienceId, Long userId, Long tenantId) {
        attachmentMapper.updateByexperienceIdByTenantId(experienceId, userId, tenantId);
    }

    public List<FileUploadDO> selectListByBizId(int trainingId, Long tenantId) {
        return attachmentMapper.selectListByBizId(trainingId, tenantId);
    }

    public void deleteFile(List<Integer> ids) {
        attachmentMapper.deleteByIds(ids);
    }


    public List<FileUploadDO> selectListBytrainingMaterialsIds(List<Long> trainingMaterialsIds, Long tenantId) {
        return attachmentMapper.selectListBytrainingMaterialsIds(trainingMaterialsIds, tenantId);


    }
}