package cn.iocoder.yudao.module.qar.dal.dataobject.qardata;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

@TableName("efvs_qar_data")
@KeySequence("efvs_qar_data_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QarDataDO extends BaseDO {
    /**
     * QAR主键id
     */
    @TableId
    private Integer id;
    /**
     * 时间
     */
    private String time;
    /**
     * 空地电门
     */
    private String emptySpaceSwitch;
    /**
     * 空速
     */
    private String airspeed;
    /**
     * 空速（千米）
     */
    private String airspeedKm;
    /**
     * 选择速度
     */
    private String selectedSpeed;
    /**
     * 选择速度(KM)
     */
    private String selectedSpeedKm;
    /**
     * 航向道偏差
     */
    private String locDeviation;
    /**
     * 航向道偏差*75
     */
    private String locDeviationRes;
    /**
     * 下滑道偏差
     */
    private String glideslopeDeviation;
    /**
     * 下滑道偏差*75
     */
    private String glideslopeDeviationRes;
    /**
     * 下滑道截获
     */
    private String glideSlopeCapture;
    /**
     * 仰角
     */
    private String pitchAngle;
    /**
     * 坡度
     */
    private String slope;
    /**
     * 磁航向
     */
    private String magneticHeading;
    /**
     * 经度
     */
    private String longitude;
    /**
     * 纬度
     */
    private String latitude;
    /**
     * 地速（千米）
     */
    private String groundSpeedKm;
    /**
     * 地速
     */
    private String groundSpeed;
    /**
     * 无线电高度
     */
    private String radioAltitude;
    /**
     * 日期
     */
    private String date;
    /**
     * QAR主表id（关联 efvs_qar）
     */
    private Long qarId;
}