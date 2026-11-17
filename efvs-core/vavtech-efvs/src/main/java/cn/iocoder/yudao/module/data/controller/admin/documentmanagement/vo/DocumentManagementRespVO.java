package cn.iocoder.yudao.module.data.controller.admin.documentmanagement.vo;

import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Data
@ExcelIgnoreUnannotated
public class DocumentManagementRespVO {

    @ExcelProperty("ID")
    private Integer id;

    @ExcelProperty("节点名称")
    private String name;

    @ExcelProperty("父节点ID 0表示无父节点（即一级节点）")
    private Integer parentId;

    @ExcelProperty("节点类型 可用于区分是目录还是具体资料类型")
    private String nodeType;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @ExcelProperty("备注")
    private String remark;

}
