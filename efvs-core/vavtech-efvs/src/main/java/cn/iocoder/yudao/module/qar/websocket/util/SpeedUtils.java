package cn.iocoder.yudao.module.qar.websocket.util;

import lombok.extern.slf4j.Slf4j;

/**
 * 速度工具类
 * 
 * @author admin
 */
@Slf4j
public class SpeedUtils {

    /**
     * 解析地速字符串
     */
    public static Double parseSpeed(String speedStr) {
        if (speedStr == null || speedStr.trim().isEmpty() || "NULL".equalsIgnoreCase(speedStr)) {
            return null;
        }

        try {
            return Double.parseDouble(speedStr.trim());
        } catch (NumberFormatException e) {
            log.warn("[parseSpeed][地速解析失败: {}]", speedStr);
            return null;
        }
    }

    /**
     * 将地速转换为米/秒
     * 
     * 注意：现在使用 ground_speed_km 字段，单位是 km/h（公里/小时）
     * 1 km/h = 1000 m / 3600 s = 1/3.6 m/s ≈ 0.278 m/s
     * 
     * 判断逻辑：
     * - 如果速度 >= 10，认为是 km/h，转换为 m/s（除以 3.6）
     * - 如果速度 < 10，可能是 m/s，直接使用
     */
    public static Double convertToMetersPerSecond(Double speed) {
        if (speed == null || speed <= 0) {
            return null;
        }

        // 如果速度 >= 10，认为是 km/h，转换为 m/s
        if (speed >= 10) {
            return speed / 3.6; // km/h 转 m/s
        }

        // 如果速度 < 10，可能是 m/s，直接使用
        return speed;
    }

    /**
     * 计算累计距离增量（米）
     * 公式：地速(m/s) × 时间间隔(秒)
     */
    public static Double calculateDistanceDelta(Double speedMetersPerSecond, Double timeIntervalSeconds) {
        if (speedMetersPerSecond == null || speedMetersPerSecond <= 0) {
            return 0.0;
        }

        if (timeIntervalSeconds == null || timeIntervalSeconds <= 0) {
            return 0.0;
        }

        return speedMetersPerSecond * timeIntervalSeconds;
    }

}
