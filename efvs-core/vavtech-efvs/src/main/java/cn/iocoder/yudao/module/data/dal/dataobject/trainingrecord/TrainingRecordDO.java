package cn.iocoder.yudao.module.data.dal.dataobject.trainingrecord;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 培训记录 DO
 *
 * @author admin
 */
@TableName("efvs_training_record")
@KeySequence("efvs_training_record_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingRecordDO extends BaseDO {

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
     * 课程名称
     */
    private String courseName;
    /**
     * 学习时间
     */
    private Long learningTime;
    /**
     * 是否完成 (0 否 1 是)
     */
    private Integer isCompleted;
    /**
     * 完成形式
     */
    private String perfectiveForm;
    /**
     * 成绩
     */
    private Integer grades;
    /**
     * 教员
     */
    private String instructor;
    /**
     * 完成日期
     */
    private Long completionDate;
    /**
     * 备注
     */
    private String remark;


}