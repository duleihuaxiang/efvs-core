package cn.iocoder.yudao.module.data.dal.dataobject.trainingattendance;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 培训签到 DO
 *
 * @author admin
 */
@TableName("efvs_training_attendance")
@KeySequence("efvs_training_attendance_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingAttendanceDO extends BaseDO {

    /**
     * ID
     */
    @TableId
    private Integer id;
    /**
     * 编号
     */
    private Integer serialNumber;
    /**
     * 姓名 
     */
    private String name;
    /**
     * 单位名称
     */
    private String unitName;
    /**
     * 培训主题
     */
    private String trainingTheme;
    /**
     * 培训时间
     */
    private LocalDateTime trainingTime;
    /**
     * 培训讲师 (0 否 1 是)
     */
    private Integer trainingInstructor;
    /**
     * 培训类别
     */
    private String trainingCategory;
    /**
     * 培训地点
     */
    private String trainingLocation;
    /**
     * 培训内容
     */
    private String trainingContent;
    /**
     * 备注
     */
    private String remark;


}