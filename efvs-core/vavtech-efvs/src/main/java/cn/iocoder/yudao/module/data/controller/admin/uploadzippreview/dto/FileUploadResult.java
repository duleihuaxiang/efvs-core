package cn.iocoder.yudao.module.data.controller.admin.uploadzippreview.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileUploadResult {
    private Integer id;
    private String fileUrl;
    private String name;
    private String size;
    /**
     * 文件预览路径
     */
    private String filePreviewPath;

    /**
     * 文件类型名称
     */
    private String typeName;

}
