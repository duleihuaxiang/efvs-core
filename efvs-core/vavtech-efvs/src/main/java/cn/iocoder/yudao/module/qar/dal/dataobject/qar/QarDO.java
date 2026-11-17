package cn.iocoder.yudao.module.qar.dal.dataobject.qar;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * qar数据管理 DO
 *
 * @author admin
 */
@TableName("efvs_qar")
@KeySequence("efvs_qar_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QarDO extends BaseDO {

    /**
     * id
     */
    @TableId
    private Long id;
    /**
     * 航司
     */
    private String airline;
    /**
     * 机型
     */
    private String model;
    /**
     * efvs构型
     */
    private String efvsConfiguration;
    /**
     * 文件名称
     */
    private String name;
    /**
     * 文件大小(kb)
     */
    private String size;
    /**
     * 文件来源
     */
    private String fileSource;
    /**
     * 文件路径
     */
    private String fileUrl;
    /**
     * 备注
     */
    private String remark;
    /**
     * 用于区分性能数据和复现数据
     */
    private String flag;

}
