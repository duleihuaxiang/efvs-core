package cn.iocoder.yudao.module.data.dal.dataobject.productfile;

import lombok.*;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 档案 DO
 *
 * @author admin
 */
@TableName("efvs_product_file")
@KeySequence("efvs_product_file_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductFileDO extends BaseDO {

    /**
     * NOTE：如果库表 `efvs_product_file` 不包含 BaseDO 的 `creator` 字段列，
     * 则需要显式忽略，避免 insert/select SQL 引用不存在的列。
     */
    @TableField(exist = false)
    private String creator;

    /**
     * 档案主键
     */
    @TableId
    private Integer id;
    /**
     * 产品件号
     */
    private String partNumber;
    /**
     * 产品件号
     */
    private String productVersion;
    /**
     * 产品序列号
     */
    private String sequenceNumber;
    /**
     * 零部件名称
     */
    private String name;
    /**
     * 图号
     */
    private String drawingNumber;
    /**
     * 图纸版本
     */
    private String drawingVersion;
    /**
     * 生产编号
     */
    private String productionNumber;
    /**
     * 对应基线
     */
    private String baseline;
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


}