package cn.iocoder.yudao.module.data.controller.admin.docdirectory.vo;

import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Data
@ExcelIgnoreUnannotated
public class DocDirectoryRespVO {

    @ExcelProperty("ID")
    private Integer id;

    @ExcelProperty("目录名称 （如文档管理，培训资料等）")
    private String name;

    @ExcelProperty("父级目录id")
    private Integer parentId;

    @ExcelProperty("目录层级 （顶级为1，每下一级+1）")
    private Integer level;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @ExcelProperty("备注")
    private String remark;

    @ExcelProperty("状态")
    private Integer status;

}
