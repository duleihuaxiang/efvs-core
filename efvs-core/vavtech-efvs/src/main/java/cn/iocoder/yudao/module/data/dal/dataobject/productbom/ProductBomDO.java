package cn.iocoder.yudao.module.data.dal.dataobject.productbom;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 产品BOM DO
 *
 * @author admin
 */
@TableName("efvs_product_bom")
@KeySequence("efvs_product_bom_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductBomDO extends BaseDO {

    /**
     * 产品BOMID
     */
    @TableId
    private Integer id;
    /**
     * PMN
     */
    private String pmn;
    /**
     * 层级
     */
    private Integer level;
    /**
     * 版本
     */
    private String productVersion;
    /**
     * 类型
     */
    private Integer productType;
    /**
     * 产品代号
     */
    private String partNumber;
    /**
     * 名称
     */
    private String name;
    /**
     * 所属产品件号
     */
    private String productPartNumber;
    /**
     * 数量
     */
    private Integer productNum;
    /**
     * 单位
     */
    private String unit;
    /**
     * 位号
     */
    private String tagNumber;
    /**
     * 细类
     */
    private String subcategory;
    /**
     * 规格或图号
     */
    private String drawingNumber;
    /**
     * 大类
     */
    private Integer category;
    /**
     * 小类
     */
    private Integer smallSubcategory;
    /**
     * 备注
     */
    private String remark;

    @TableField(exist = false)
    private List<ProductBomDO> children;


}