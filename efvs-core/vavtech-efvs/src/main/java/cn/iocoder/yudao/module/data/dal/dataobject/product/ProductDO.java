package cn.iocoder.yudao.module.data.dal.dataobject.product;

import lombok.*;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 产品 DO
 *
 * @author admin
 */
@TableName("efvs_product")
@KeySequence("efvs_product_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDO extends BaseDO {

    /**
     * NOTE：当前库表 `efvs_product` 不包含 BaseDO 的 `creator` 字段列。
     * 为了避免 MyBatis-Plus 生成 SQL 引用不存在的列，这里显式忽略。
     */
    @TableField(exist = false)
    private String creator;

    /**
     * 商品编号
     */
    @TableId
    private Integer id;
    /**
     * 文档名称
     */
    private String documentNumber;
    /**
     * 名称
     */
    private String name;
    /**
     * 产品件号
     */
    private String partNumber;
    /**
     * 序列号
     */
    private String sequenceNumber;
    /**
     * 客户编号
     */
    private String customerNumber;
    /**
     * 客户名称
     */
    private String customerName;
    /**
     * 备注
     */
    private String remark;

    /**
     * 图标版本
     */
    private String signVersion;

    /**
     * 对应基线
     */
    private String baseline;

    private String revision;

    private String deliveryDestination;

    private String deliveryTime;

}
