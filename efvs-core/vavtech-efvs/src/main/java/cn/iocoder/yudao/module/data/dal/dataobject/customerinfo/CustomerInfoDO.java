package cn.iocoder.yudao.module.data.dal.dataobject.customerinfo;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 客户信息 DO
 *
 * @author admin
 */
@TableName("efvs_customer_info")
@KeySequence("efvs_customer_info_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerInfoDO extends BaseDO {

    /**
     * ID
     */
    @TableId
    private Integer id;
    /**
     * 客户名称
     */
    private String customerName;
    /**
     * 客户编号
     */
    private String customerId;
    /**
     * 客户联系人
     */
    private String customerContactPerson;
    /**
     * 联系人电话
     */
    private Long phone;
    /**
     * 所属部门
     */
    private String dept;
    /**
     * 邮箱地址
     */
    private String email;
    /**
     * 客户地址（省）
     */
    private String province;
    /**
     * 客户地址（省）id
     */
    private Integer provinceId;
    /**
     * 客户地址（市）
     */
    private String city;
    /**
     * 客户地址（市）id
     */
    private Integer cityId;
    /**
     * 客户地址（区）
     */
    private String district;
    /**
     * 客户地址（区）id
     */
    private Integer districtId;
    /**
     * 邮编
     */
    private Integer postalCode;
    /**
     * 详细地址
     */
    private String address;
    /**
     * 数据获取渠道
     */
    private String dataSource;
    /**
     * 备注
     */
    private String remark;


}