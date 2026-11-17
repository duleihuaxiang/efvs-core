package cn.iocoder.yudao.module.data.dal.dataobject.ppql;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 培训前准备问题 DO
 *
 * @author admin
 */
@TableName("efvs_ppql")
@KeySequence("efvs_ppql_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PpqlDO extends BaseDO {

    /**
     * ID
     */
    @TableId
    private Integer id;
    /**
     * 问题描述
     */
    private String problemDescription;
    /**
     * 备注
     */
    private String remark;


}