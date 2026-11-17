package cn.iocoder.yudao.module.qar.controller.admin.qardata.vo;

import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Data
@ExcelIgnoreUnannotated
public class QarDataRespVO {

    @ExcelProperty("QAR主键id")
    private Integer id;

    @ExcelProperty("时间")
    private String time;

    @ExcelProperty("北京时间")
    private String beijingTime;

    @ExcelProperty("空地电门")
    private String emptySpaceSwitch;

    @ExcelProperty("垂直载荷")
    private String verticalLoad;

    @ExcelProperty("气压高度")
    private String pressureAltitude;

    @ExcelProperty("机场上高度")
    private String airfieldAltitude;

    @ExcelProperty("无线电高度")
    private String radioAltitude;

    @ExcelProperty("选择高度")
    private String selectHeight;

    @ExcelProperty("高度截获")
    private String altitudeCapture;

    @ExcelProperty("高度保持")
    private String maintainHeight;

    @ExcelProperty("升降率")
    private String climbDescentRate;

    @ExcelProperty("选择升降率")
    private String selectedVerticalRate;

    @ExcelProperty("TO/GA方式")
    private String toGaMode;

    @ExcelProperty("TO/GA电门按压")
    private String toGaSwitchPress;

    @ExcelProperty("CMD A")
    private String cmdA;

    @ExcelProperty("CMD B")
    private String cmdB;

    @ExcelProperty("自动油门")
    private String autoThrottle;

    @ExcelProperty("左DME")
    private String leftDme;

    @ExcelProperty("右DME")
    private String rightDme;

    @ExcelProperty("主警戒")
    private String mainAlert;

    @ExcelProperty("下滑道警告")
    private String downslideWarning;

    @ExcelProperty("GPWS ALERT(GPWS警报)")
    private String gpwsAlert;

    @ExcelProperty("GPWS WARNING(GPWS警告)")
    private String gpwsWarning;

    @ExcelProperty("PULL UP(拉起)")
    private String pullUp;

    @ExcelProperty("TERRAIN(地形)")
    private String terrain;

    @ExcelProperty("SINK RATE（下沉速率）")
    private String sinkRate;

    @ExcelProperty("空速")
    private String airspeed;

    @ExcelProperty("左发N1")
    private String leftEngN1;

    @ExcelProperty("右发N1")
    private String rightEngN1;

    @ExcelProperty("左油门杆")
    private String leftThrottle;

    @ExcelProperty("右油门杆")
    private String rightThrottle;

    @ExcelProperty("地速")
    private String groundSpeed;

    @ExcelProperty("左攻角")
    private String leftAoa;

    @ExcelProperty("右攻角")
    private String rightAoa;

    @ExcelProperty("仰角")
    private String pitchAngle;

    @ExcelProperty("驾驶杆位置")
    private String controlColumnPosition;

    @ExcelProperty("左飞行指引仰角偏差")
    private String leftFdPitchDeviation;

    @ExcelProperty("右飞行指引仰角偏差")
    private String rightFdPitchDeviation;

    @ExcelProperty("坡度")
    private String slope;

    @ExcelProperty("驾驶盘位置")
    private String yokePosition;

    @ExcelProperty("左飞行指引坡度偏差")
    private String leftFdBankDeviation;

    @ExcelProperty("右飞行指引坡度偏差")
    private String rightFdBankDeviation;

    @ExcelProperty("磁航向")
    private String magneticHeading;

    @ExcelProperty("风向")
    private String windDirection;

    @ExcelProperty("风速")
    private String windSpeed;

    @ExcelProperty("预测式风切变")
    private String predictiveWindshear;

    @ExcelProperty("风切变警告")
    private String windshearWarning;

    @ExcelProperty("风切变警戒")
    private String windshearCaution;

    @ExcelProperty("风切变不工作")
    private String windshearInop;

    @ExcelProperty("襟翼手柄位置")
    private String flapHandlePosition;

    @ExcelProperty("襟翼位置")
    private String flapPosition;

    @ExcelProperty("起落架红灯")
    private String landingGearRedLight;

    @ExcelProperty("左起落架")
    private String leftLandingGear;

    @ExcelProperty("前起落架")
    private String noseLandingGear;

    @ExcelProperty("右起落架")
    private String rightLandingGear;

    @ExcelProperty("减速板手柄")
    private String speedbrakeHandle;

    @ExcelProperty("3#扰流板")
    private String spoilerThree;

    @ExcelProperty("10#扰流板")
    private String spoilerTen;

    @ExcelProperty("4#扰流板")
    private String spoilerFour;

    @ExcelProperty("9#扰流板")
    private String spoilerNine;

    @ExcelProperty("选择航向")
    private String selectedHeading;

    @ExcelProperty("航向选择")
    private String headingSelection;

    @ExcelProperty("水平导航")
    private String lateralNav;

    @ExcelProperty("垂直导航")
    private String verticalNav;

    @ExcelProperty("垂直偏差")
    private String verticalDeviation;

    @ExcelProperty("垂直导航航径")
    private String verticalNavPath;

    @ExcelProperty("垂直导航速度")
    private String verticalNavSpeed;

    @ExcelProperty("VOR/LOC预位")
    private String vorLocArm;

    @ExcelProperty("VOR/LOC截获")
    private String vorLocCapture;

    @ExcelProperty("航向道截获")
    private String locCapture;

    @ExcelProperty("下滑道预位")
    private String glideslopeArm;

    @ExcelProperty("下滑道截获")
    private String glideslopeCapture;

    @ExcelProperty("APP")
    private String app;

    @ExcelProperty("V/S接通")
    private String vsEngage;

    @ExcelProperty("高度层改变")
    private String flightLevelChange;

    @ExcelProperty("下滑道偏差")
    private String glideslopeDeviation;

    @ExcelProperty("航向道偏差")
    private String locDeviation;

    @ExcelProperty("左飞行指引")
    private String leftFd;

    @ExcelProperty("右飞行指引")
    private String rightFd;

    @ExcelProperty("气压基准")
    private String barometricReference;

    @ExcelProperty("HUD使用")
    private String hudUse;

    @ExcelProperty("HUD AIII能力")
    private String hudAiiiCapability;

    @ExcelProperty("HUD AIII模式")
    private String hudAiiiMode;

    @ExcelProperty("HUD进近警告")
    private String hudApproachWarning;

    @ExcelProperty("左ILS频率")
    private String leftIlsFreq;

    @ExcelProperty("右ILS频率")
    private String rightIlsFreq;

    @ExcelProperty("左VOR频率")
    private String leftVorFreq;

    @ExcelProperty("右VOR频率")
    private String rightVorFreq;

    @ExcelProperty("全重")
    private String grossWeight;

    @ExcelProperty("VREF(基准参考速度)")
    private String vref;

    @ExcelProperty("选择速度")
    private String selectedSpeed;

    @ExcelProperty("静温")
    private String staticTemperature;

    @ExcelProperty("全温")
    private String totalTemperature;

    @ExcelProperty("垂直载荷最小值")
    private String verticalLoadMin;

    @ExcelProperty("马赫数")
    private String machNumber;

    @ExcelProperty("选择马赫数")
    private String selectedMach;

    @ExcelProperty("左最低标准基准选择")
    private String leftMinStandardRefSelect;

    @ExcelProperty("右最低标准基准选择")
    private String rightMinStandardRefSelect;

    @ExcelProperty("左气压最低标准（MSD）")
    private String leftBaroMinStandardMsd;

    @ExcelProperty("左气压最低标准（LSD）")
    private String leftBaroMinStandardLsd;

    @ExcelProperty("右气压最低标准（MSD）")
    private String rightBaroMinStandardMsd;

    @ExcelProperty("右气压最低标准（LSD）")
    private String rightBaroMinStandardLsd;

    @ExcelProperty("左无线电最低标准")
    private String leftRadioMinStandard;

    @ExcelProperty("右无线电最低标准")
    private String rightRadioMinStandard;

    @ExcelProperty("日期")
    private String date;

    @ExcelProperty("CAUTION TERRAIN(小心地形)")
    private String cautionTerrain;

    @ExcelProperty("TERRAIN CAUTION(地形警告)")
    private String terrainCaution;

    @ExcelProperty("TOO LOW TERRAIN(地势太低)")
    private String tooLowTerrain;

    @ExcelProperty("TOO LOW FLAPS(太低襟翼)")
    private String tooLowFlaps;

    @ExcelProperty("TOO LOW GEAR(太低起落架)")
    private String tooLowGear;

    @ExcelProperty("Dont Sink(不要下沉)")
    private String dontSink;

    @ExcelProperty("FMC_NAV_GPS(FMC导航GPS)")
    private String fmcNavGps;

    @ExcelProperty("俯仰配平")
    private String pitchTrim;

    @ExcelProperty("GP预位")
    private String gpArm;

    @ExcelProperty("GP截获")
    private String gpCapture;

    @ExcelProperty("减速板预位灯")
    private String speedbrakeArmedLight;

    @ExcelProperty("减速板未预位灯")
    private String speedbrakeDisarmedLight;

    @ExcelProperty("襟翼卸载")
    private String flapUnload;

    @ExcelProperty("方向舵踏板")
    private String rudderPedal;

    @ExcelProperty("方向舵")
    private String rudder;

    @ExcelProperty("左抖杆")
    private String leftStickShaker;

    @ExcelProperty("右抖杆")
    private String rightStickShaker;

    @ExcelProperty("最小机动速度")
    private String minManeuverSpeed;

    @ExcelProperty("抖杆速度")
    private String stickShakerSpeed;

    @ExcelProperty("左气象位")
    private String leftWeatherPosition;

    @ExcelProperty("右气象位")
    private String rightWeatherPosition;

    @ExcelProperty("左地形位")
    private String leftTerrainPosition;

    @ExcelProperty("右地形位")
    private String rightTerrainPosition;

    @ExcelProperty("左探测范围")
    private String leftDetectionRange;

    @ExcelProperty("右探测范围")
    private String rightDetectionRange;

    @ExcelProperty("自动驾驶状态")
    private String autopilotStatus;

    @ExcelProperty("CWS A")
    private String cwsA;

    @ExcelProperty("CWS B")
    private String cwsB;

    @ExcelProperty("CWS PITCH")
    private String cwsPitch;

    @ExcelProperty("CWS ROLL")
    private String cwsRoll;

    @ExcelProperty("CWS警告")
    private String cwsWarning;

    @ExcelProperty("左襟翼(原始值)")
    private String leftFlapRaw;

    @ExcelProperty("右襟翼(原始值)")
    private String rightFlapRaw;

    @ExcelProperty("备用放襟翼电门")
    private String alternateFlapExtendSwitch;

    @ExcelProperty("左选择航道")
    private String leftSelectedCourse;

    @ExcelProperty("右选择航道")
    private String rightSelectedCourse;

    @ExcelProperty("V1")
    private String v1;

    @ExcelProperty("VR")
    private String vr;

    @ExcelProperty("V2")
    private String v2;

    @ExcelProperty("前空地电门")
    private String forwardAirGroundSwitch;

    @ExcelProperty("左主空地电门")
    private String leftMainAirGroundSwitch;

    @ExcelProperty("右主空地电门")
    private String rightMainAirGroundSwitch;

    @ExcelProperty("主用/备用刹车选择")
    private String mainAlternateBrakeSelect;

    @ExcelProperty("左主刹车压力")
    private String leftMainBrakePressure;

    @ExcelProperty("右主刹车压力")
    private String rightMainBrakePressure;

    @ExcelProperty("左备用刹车压力")
    private String leftAlternateBrakePressure;

    @ExcelProperty("右备用刹车压力")
    private String rightAlternateBrakePressure;

    @ExcelProperty("经度")
    private String longitude;

    @ExcelProperty("纬度")
    private String latitude;

    @ExcelProperty("航迹角")
    private String trackAngle;

    @ExcelProperty("目标航迹角")
    private String targetTrackAngle;

    @ExcelProperty("左中心显示")
    private String leftCenterDisplay;

    @ExcelProperty("右中心显示")
    private String rightCenterDisplay;

    @ExcelProperty("中央下显示组件")
    private String lowerCenterDisplayUnit;

    @ExcelProperty("中央上显示组件")
    private String upperCenterDisplayUnit;

    @ExcelProperty("左内显示组件")
    private String leftInnerDisplayUnit;

    @ExcelProperty("左外显示组件")
    private String leftOuterDisplayUnit;

    @ExcelProperty("右内显示组件")
    private String rightInnerDisplayUnit;

    @ExcelProperty("右外显示组件")
    private String rightOuterDisplayUnit;

    @ExcelProperty("_ALT_BARO(气压高度)")
    private String altBaro;

    @ExcelProperty("_ALT_MODE(高度模式)")
    private String altMode;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @ExcelProperty("创建人")
    private String creator;

    @ExcelProperty("修改时间")
    private LocalDateTime updateTime;

    @ExcelProperty("修改人")
    private String updater;

    @ExcelProperty("备注")
    private String remark;

}
