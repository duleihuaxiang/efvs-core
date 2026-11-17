package cn.iocoder.yudao.module.data.controller.admin.uploadzippreview;
import cn.hutool.core.io.IoUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.data.controller.admin.uploadzippreview.dto.FileUploadDto;
import cn.iocoder.yudao.module.data.controller.admin.uploadzippreview.dto.FileUploadResult;
import cn.iocoder.yudao.module.data.controller.admin.uploadzippreview.service.impl.AttachmentService;
import cn.iocoder.yudao.module.training.dal.dataobject.trainingexperience.FileUploadDO;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;


@RestController
@RequestMapping("/api/files")
public class AttachmentController {

    @Autowired
    private AttachmentService attachmentService;

    /**
     * 多文件上传接口
     * @param files 多文件数组（参数名files需与前端一致）
     * @param directory 存储目录（文本参数）
     * @return 上传结果（含文件ID、路径等）
     */
//    @PostMapping("/upload")
//    public ResponseEntity<List<Integer>> upload(
//            @RequestParam("files") MultipartFile[] files, // 接收MultipartFile数组
//            @RequestParam("directory") String directory) {
//        // 调用服务层处理上传
//        List<Integer> attachmentList = attachmentService.uploadFiles(files, directory);
//        return ResponseEntity.ok(attachmentList);
//    }

//    @PostMapping("/uploadFilesFromJson")
//    public ResponseEntity<List<Integer>> upload(
//            @RequestBody List<FileUploadDto> fileUploads, // 接收 JSON 数组
//            @RequestParam("directory") String directory) {
//
//        List<Integer> attachmentIds = attachmentService.uploadFilesFromJson(fileUploads, directory);
//        return ResponseEntity.ok(attachmentIds);
//    }

    /**
     * 通用附件上传入口。
     * <p>
     * 当前实现会先将每个 MultipartFile 完整读入内存，再交给附件服务保存；因此调用方应遵循前端文件大小限制，
     * 大文件场景应改为流式上传或对象存储预签名上传，避免占用过多 JVM 堆内存。
     */
    @PostMapping("/uploads")
    public CommonResult<List<FileUploadResult>> uploadFiles(
            @RequestParam("file") MultipartFile[] files,
            @RequestParam("directory") String directory) throws Exception {
//        @RequestParam(value = "fileIds", required = false)List<Integer> fileIds
        List<FileUploadResult> results = new ArrayList<>();
        for (MultipartFile file : files) {
            byte[] content = IoUtil.readBytes(file.getInputStream());
            List<FileUploadResult> fileResults = attachmentService.createFile(content, file.getOriginalFilename(), directory, file.getContentType());
            results.addAll(fileResults);
        }
        return success(results);
    }
        @DeleteMapping("/deleteByIds")
    public CommonResult<Boolean> deleteFileByIds(@RequestParam("fileIds") List<Integer> ids) {
        attachmentService.deleteFile(ids);
        return success(true);
    }
    @GetMapping("/getFileByIds")
    public CommonResult<List<FileUploadDO>> getFileByIds(@RequestParam("fileIds") List<Integer> ids) {
        List<FileUploadDO> fileUploads = attachmentService.selectListByIds(ids);
        return success(fileUploads);
    }

}
