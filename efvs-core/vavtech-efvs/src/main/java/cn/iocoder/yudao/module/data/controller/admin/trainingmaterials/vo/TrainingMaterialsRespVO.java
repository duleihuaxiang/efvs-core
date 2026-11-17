package cn.iocoder.yudao.module.data.controller.admin.trainingmaterials.vo;

import cn.iocoder.yudao.module.training.dal.dataobject.trainingexperience.FileUploadDO;
import lombok.*;
import java.util.*;
import cn.idev.excel.annotation.*;

@Data
@ExcelIgnoreUnannotated
public class TrainingMaterialsRespVO {

//    @ExcelProperty("ID")
    private Integer id;

//    @ExcelProperty("培训资料表ID")
    private Integer trainingId;

    @ExcelProperty("资料名称")
    private String documentName;

    @ExcelProperty("所属目录")
    private String containingDirectory;

    private String category;

    @ExcelProperty("分类") // Excel 列名显示为"分类"
    private String categoryDesc;

    @ExcelProperty("大小(M)")
    private Double size;

    @ExcelProperty("备注")
    private String remark;
    /**
     * 该培训对应的所有附件（数组/集合）
     */
    private List<FileUploadDO> fileList;

}
