package cn.iocoder.yudao.module.data.controller.admin.documentmanagement.vo;

import lombok.*;
import java.util.*;
import javax.validation.constraints.*;

@Data
public class DocumentManagementSaveReqVO {

    private Integer id;

    @NotEmpty(message = "节点名称不能为空")
    private String name;

    @NotNull(message = "父节点ID 0表示无父节点（即一级节点）不能为空")
    private Integer parentId;

    @NotEmpty(message = "节点类型 可用于区分是目录还是具体资料类型不能为空")
    private String nodeType;

    private String remark;

}
