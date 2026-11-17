package cn.iocoder.yudao.module.data.dal.dataobject.documentmanagement;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 文档管理 DO
 *
 * @author admin
 */
@TableName("efvs_document_management")
@KeySequence("efvs_document_management_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentManagementDO extends BaseDO {

    /**
     * ID
     */
    @TableId
    private Integer id;
    /**
     * 节点名称
     */
    private String name;
    /**
     * 父节点ID 0表示无父节点（即一级节点）
     */
    private Integer parentId;
    /**
     * 节点类型 可用于区分是目录还是具体资料类型
     */
    private String nodeType;
    /**
     * 备注
     */
    private String remark;


}