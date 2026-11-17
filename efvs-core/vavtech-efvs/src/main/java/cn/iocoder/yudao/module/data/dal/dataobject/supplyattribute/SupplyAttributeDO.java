package cn.iocoder.yudao.module.data.dal.dataobject.supplyattribute;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 供应属性 DO
 *
 * @author admin
 */
@TableName("efvs_supply_attribute")
@KeySequence("efvs_supply_attribute_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplyAttributeDO extends BaseDO {

    /**
     * 供应属性编号
     */
    @TableId
    private Integer id;
    /**
     * 供应商名称
     */
    private String name;
    /**
     * 供应商编号
     */
    private String supplierNumber;
    /**
     * 生产日期
     */
    private LocalDateTime productionDate;
    /**
     * 保修状态0是1否
     */
    private String batchNumber;
    /**
     * 保修状态
     */
    private Integer warrantyStatus;

    /**
     * 入库批次号
     */
    private String inboundBatchNumber;
    /**
     * 采购订单号
     */
    private String purchaseOrderNumber;
    /**
     * 备注
     */
    private String remark;

}
