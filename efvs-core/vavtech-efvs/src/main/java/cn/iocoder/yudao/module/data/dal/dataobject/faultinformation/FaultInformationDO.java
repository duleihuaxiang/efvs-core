package cn.iocoder.yudao.module.data.dal.dataobject.faultinformation;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 故障信息 DO
 *
 * @author admin
 */
@TableName("efvs_fault_information")
@KeySequence("efvs_fault_information_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FaultInformationDO extends BaseDO {

    /**
     * 故障信息
     */
    @TableId
    private Integer id;
    /**
     * 故障日期
     */
    private LocalDateTime faultDate;
    /**
     * 故障现象
     */
    private String faultPhenomenon;
    /**
     * 故障状态0已解决1排查中
     */
    private Boolean faultStatus;
    /**
     * 故障原因
     */
    private String causeOfFailure;
    /**
     * 解决人
     */
    private String resolver;
    /**
     *  备注
     */
    private String remark;


}