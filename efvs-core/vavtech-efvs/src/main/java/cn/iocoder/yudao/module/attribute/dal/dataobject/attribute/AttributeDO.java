package cn.iocoder.yudao.module.attribute.dal.dataobject.attribute;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 产品属性 DO
 *
 * @author admin
 */
@TableName("efvs_attribute")
@KeySequence("efvs_attribute_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttributeDO extends BaseDO {

    /**
     * 属性id
     */
    @TableId
    private Integer id;
    /**
     * 产品id
     */
    private Integer productId;
    /**
     * 设计属性
     */
    private String designAttribute;
    /**
     * 保障属性
     */
    private String guaranteeAttribute;
    /**
     * 供应属性
     */
    private String supplyAttribute;
    /**
     * 机型属性
     */
    private String modelAttribute;
    /**
     * 保障属性
     */
    private String faultAttribute;
    /**
     * 备注
     */
    private String remark;


}