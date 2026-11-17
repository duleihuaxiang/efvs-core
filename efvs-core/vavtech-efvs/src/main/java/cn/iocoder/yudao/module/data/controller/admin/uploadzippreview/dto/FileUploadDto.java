package cn.iocoder.yudao.module.data.controller.admin.uploadzippreview.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadDto {
    private String fileName;
    private String fileBase64; // 或者是其他编码格式
    private String fileType;
}
