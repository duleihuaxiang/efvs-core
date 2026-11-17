package cn.iocoder.yudao.module.geo.dal.dataobject.area;

import lombok.*;
import java.util.*;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 地区 DO
 *
 * @author admin
 */
@TableName("efvs_area")
@KeySequence("efvs_area_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AreaDO extends BaseDO {

    public static final Integer PARENT_ID_ROOT = 1;

    /**
     * 地区的唯一标识符（ID）
     */
    @TableId
    private Integer id;
    /**
     * 地区的名称
     */
    private String name;
    /**
     * 地区的类型1 表示国家（例如中国）2 表示省份或直辖市（例如北京市、天津市）3 表示地级市（例如石家庄市）4 表示区/县（例如长安区）
     */
    private Integer type;
    /**
     * 该地区的父级地区 ID
     */
    private Integer parentId;
    /**
     * 备注
     */
    private String remark;
    /**
     * 子地区列表
     */
    @TableField(exist = false)
    private List<AreaDO> children;


}