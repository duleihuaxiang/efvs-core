package cn.iocoder.yudao.module.data.dal.dataobject.purchaseproductinfo;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 购买产品信息 DO
 *
 * @author admin
 */
@TableName("efvs_purchase_product_info")
@KeySequence("efvs_purchase_product_info_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseProductInfoDO extends BaseDO {

    /**
     * 产品编号
     */
    @TableId
    private Integer id;
    /**
     * 客户id
     */
    private String customerId;
    /**
     * 产品大类
     */
    private String productCategory;
    /**
     * 产品名称
     */
    private String productName;
    /**
     * 产品型号
     */
    private String productModel;
    /**
     * 产品序列号
     */
    private String productSerialNum;
    /**
     * 产品数量
     */
    private Integer productQuantity;
    /**
     * 备注
     */
    private String remark;


}