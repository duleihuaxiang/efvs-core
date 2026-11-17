package cn.iocoder.yudao.module.data.dal.dataobject.trainingmaterials;

import cn.iocoder.yudao.module.training.dal.dataobject.trainingexperience.FileUploadDO;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 培训资料管理 DO
 *
 * @author admin
 */
@TableName("efvs_training_materials")
@KeySequence("efvs_training_materials_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingMaterialsDO extends BaseDO {

    /**
     * ID
     */
    @TableId
    private Long id;
    /**
     * 培训资料表ID
     */
    private Integer trainingId;
    /**
     * 资料名称
     */
    private String documentName;
    /**
     * 所属目录
     */
    private String containingDirectory;
    /**
     * 分类
     */
    private String category;
    /**
     * 大小(M)
     */
    private Double size;
    /**
     * 备注
     */
    private String remark;
    /**
     * 租户ID
     */
    private Long tenantId;
    /**
     * 文件列表
     */
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class, exist = false)
    private List<FileUploadDO> fileList;


}