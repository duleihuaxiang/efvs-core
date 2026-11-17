package cn.iocoder.yudao.module.data.dal.dataobject.docdirectory;

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
@TableName("efvs_doc_directory")
@KeySequence("efvs_doc_directory_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocDirectoryDO extends BaseDO {

    public static final Long PARENT_ID_ROOT = 0L;

    /**
     * ID
     */
    @TableId
    private Integer id;
    /**
     * 目录名称 （如文档管理，培训资料等）
     */
    private String name;
    /**
     * 父级目录id
     */
    private Integer parentId;
    /**
     * 目录层级 （顶级为1，每下一级+1）
     */
    private Integer level;
    /**
     * 备注
     */
    private String remark;
    /**
     * 状态
     */
    private Integer status;


    private Integer tenantId;   // 租户编号
    

    private List<DocDirectoryDO> children;


}