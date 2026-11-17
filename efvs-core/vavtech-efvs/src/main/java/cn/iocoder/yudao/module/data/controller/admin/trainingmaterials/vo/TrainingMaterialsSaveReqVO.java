package cn.iocoder.yudao.module.data.controller.admin.trainingmaterials.vo;

import cn.iocoder.yudao.module.data.controller.admin.uploadzippreview.dto.FileUploadResult;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;

@Data
public class TrainingMaterialsSaveReqVO {

    private Integer id;

//    @NotNull(message = "培训资料表ID不能为空")
    private Integer trainingId;

    @Size(max = 1000, message = "培训资料名称不能超过1000 个字符")
//    @NotEmpty(message = "资料名称不能为空")
    private String documentName;

//    @NotEmpty(message = "所属目录不能为空")
    private String containingDirectory;

//    @NotEmpty(message = "分类不能为空")
    private String category;

//    @NotNull(message = "大小(M)不能为空")
    private Double size;

    private String remark;

    private List<FileUploadResult>  fileUploadResults;
}
