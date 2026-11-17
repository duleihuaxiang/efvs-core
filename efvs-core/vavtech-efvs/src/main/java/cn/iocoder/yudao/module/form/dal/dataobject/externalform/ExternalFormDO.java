package cn.iocoder.yudao.module.form.dal.dataobject.externalform;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 外置表单配置 DO
 *
 * @author admin
 */
@TableName("efvs_external_form")
@KeySequence("efvs_external_form_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalFormDO extends BaseDO {

    /**
     * 主键ID（自增）
     */
    @TableId
    private Long id;
    /**
     * 流程ID（关联流程表主键/流程定义ID）
     */
    private String processId;
    /**
     * 活动ID（流程节点/活动ID）
     */
    private String activityId;
    /**
     * 表单类型（内置表单或外置表单）
     */
    private Integer formType;
    /**
     * 表单地址（完整URL，支持HTTP/HTTPS）
     */
    private String formUrl;
    /**
     * 内置表单
     */
    private Integer formKey;
    /**
     * 描述
     */
    private String remark;


}