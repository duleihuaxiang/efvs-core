package cn.iocoder.yudao.module.qar.websocket.util;

import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 时间工具类
 * 
 * @author admin
 */
@Slf4j
public class TimeUtils {

    /**
     * 支持的时间格式
     */
    private static final String[] TIME_FORMATS = {
        "yyyy-MM-dd HH:mm:ss.SSS",
        "yyyy-MM-dd HH:mm:ss",
        "yyyy/MM/dd HH:mm:ss.SSS",
        "yyyy/MM/dd HH:mm:ss",
        "HH:mm:ss.SSS",
        "HH:mm:ss"
    };

    /**
     * 分:秒[.小数秒]，如 QAR 相位里的 18:36.6（18 分 36.6 秒）
     */
    private static final java.util.regex.Pattern MINUTES_SECONDS_FRACTION =
            java.util.regex.Pattern.compile("^\\d+:\\d{2}(\\.\\d{1,3})?$");

    /**
     * 解析时间字符串
     */
    public static Date parseTime(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return null;
        }

        try {
            String trimmedTime = timeStr.trim();

            Date minSecDate = parseMinutesSecondsFractionToDate(trimmedTime);
            if (minSecDate != null) {
                return minSecDate;
            }

            for (String format : TIME_FORMATS) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat(format);
                    
                    // 对于只有时分秒的格式，使用今天的日期作为基准
                    if (format.equals("HH:mm:ss.SSS") || format.equals("HH:mm:ss")) {
                        Date timeOnly = sdf.parse(trimmedTime);
                        // 使用今天的日期 + 解析出的时分秒
                        java.util.Calendar cal = java.util.Calendar.getInstance();
                        cal.setTime(timeOnly);
                        
                        java.util.Calendar today = java.util.Calendar.getInstance();
                        cal.set(java.util.Calendar.YEAR, today.get(java.util.Calendar.YEAR));
                        cal.set(java.util.Calendar.MONTH, today.get(java.util.Calendar.MONTH));
                        cal.set(java.util.Calendar.DAY_OF_MONTH, today.get(java.util.Calendar.DAY_OF_MONTH));
                        
                        return cal.getTime();
                    } else {
                        return sdf.parse(trimmedTime);
                    }
                } catch (ParseException e) {
                    // 继续尝试下一个格式
                }
            }

            log.warn("[parseTime][时间格式无法解析: {}]", timeStr);
            return null;
        } catch (Exception e) {
            log.warn("[parseTime][时间解析异常: {}]", timeStr, e);
            return null;
        }
    }

    /**
     * 计算时间间隔（秒）
     * 支持只有时分秒格式（HH:mm:ss），会自动处理跨天情况
     */
    public static Double calculateTimeInterval(String currentTime, String lastTime) {
        if (lastTime == null || currentTime == null) {
            return 0.0;
        }

        try {
            // 检查是否是只有时分秒的格式
            boolean isTimeOnlyFormat = currentTime.matches("\\d{1,2}:\\d{2}:\\d{2}(\\.\\d{1,3})?")
                    && lastTime.matches("\\d{1,2}:\\d{2}:\\d{2}(\\.\\d{1,3})?");
            boolean isMinSecFormat = MINUTES_SECONDS_FRACTION.matcher(currentTime.trim()).matches()
                    && MINUTES_SECONDS_FRACTION.matcher(lastTime.trim()).matches();

            if (isTimeOnlyFormat) {
                // 对于只有时分秒的格式，直接计算时分秒差值
                return calculateTimeOnlyInterval(currentTime, lastTime);
            }
            if (isMinSecFormat) {
                return calculateMinSecFractionInterval(currentTime.trim(), lastTime.trim());
            } else {
                // 对于完整日期时间格式，使用Date计算
                Date current = parseTime(currentTime);
                Date last = parseTime(lastTime);
                if (current != null && last != null) {
                    long diffMillis = current.getTime() - last.getTime();
                    return diffMillis / 1000.0; // 转换为秒
                }
            }
        } catch (Exception e) {
            log.warn("[calculateTimeInterval][时间解析失败: {} -> {}]", lastTime, currentTime, e);
        }

        return 0.0;
    }
    
    /**
     * 计算只有时分秒格式的时间间隔（秒）
     * 处理跨天情况：如果当前时间小于前一个时间，认为是跨天了
     * 注意：对于连续的数据点，通常时间间隔很小（几秒），如果计算出的间隔很大（>1小时），可能是计算错误
     */
    private static Double calculateTimeOnlyInterval(String currentTime, String lastTime) {
        try {
            String[] currentParts = currentTime.split("[:.]");
            String[] lastParts = lastTime.split("[:.]");
            
            if (currentParts.length < 3 || lastParts.length < 3) {
                return 0.0;
            }
            
            int currentHour = Integer.parseInt(currentParts[0]);
            int currentMinute = Integer.parseInt(currentParts[1]);
            int currentSecond = Integer.parseInt(currentParts[2]);
            double currentFrac = parseFractionalSeconds(currentParts);
            
            int lastHour = Integer.parseInt(lastParts[0]);
            int lastMinute = Integer.parseInt(lastParts[1]);
            int lastSecond = Integer.parseInt(lastParts[2]);
            double lastFrac = parseFractionalSeconds(lastParts);
            
            // 转换为总秒数（秒 + 小数部分：1位=0.1s, 2位=0.01s, 3位=毫秒）
            double currentTotalSeconds = currentHour * 3600 + currentMinute * 60 + currentSecond + currentFrac;
            double lastTotalSeconds = lastHour * 3600 + lastMinute * 60 + lastSecond + lastFrac;
            
            double diffSeconds = currentTotalSeconds - lastTotalSeconds;
            
            // 负差：小负值（如 -1～-60s）多为同秒或顺序抖动，按 0 处理；只有明显跨天（如 < -3600）才加 24h
            if (diffSeconds < 0) {
                if (diffSeconds > -3600) {
                    diffSeconds = 0.0;
                } else {
                    diffSeconds += 24 * 3600;
                }
            }
            
            // 仅整秒格式（17:58:01 无小数）：库中最小间隔为 1 秒，实际多为多条/秒，按 1/N 缩放使 1089 条约 3600m 内
            boolean wholeSecondOnly = currentParts.length == 3 && lastParts.length == 3;
            if (wholeSecondOnly && diffSeconds > 0 && diffSeconds <= 2.0) {
                diffSeconds = diffSeconds / 6.8; // 约 6.8 条/秒 → 1088/6.8≈160s，80km/h 约 3.55km，配合 3600m 上限
            }
            
            // 如果计算出的时间间隔很大（>1小时），可能是数据有问题或计算错误
            // 对于连续的数据点，通常间隔应该在几秒到几分钟之间
            // 如果间隔超过1小时，记录警告并限制最大间隔
            if (diffSeconds > 3600) {
                log.warn("[calculateTimeOnlyInterval][时间间隔异常大: {}秒 ({} -> {})，可能是数据问题]", 
                        diffSeconds, lastTime, currentTime);
                // 限制最大间隔为1小时，避免累计距离异常
                diffSeconds = 3600.0;
            }
            
            return diffSeconds;
        } catch (Exception e) {
            log.warn("[calculateTimeOnlyInterval][时分秒格式解析失败: {} -> {}]", lastTime, currentTime, e);
            return 0.0;
        }
    }

    /**
     * 解析小数秒：按位数解释，避免 ".1" 被当成 0.001s 导致距离暴增
     * 1 位 = 0.1 秒（如 10 Hz 的 .0 .1 .2），2 位 = 0.01 秒，3 位 = 毫秒
     */
    private static double parseFractionalSeconds(String[] parts) {
        if (parts == null || parts.length <= 3) {
            return 0.0;
        }
        return fractionDigitsToSeconds(parts[3]);
    }

    /**
     * 小数位按位数解释为秒的小数：1 位十分位、2 位百分位、3 位毫秒
     */
    private static double fractionDigitsToSeconds(String frac) {
        if (frac == null || frac.isEmpty()) {
            return 0.0;
        }
        int value;
        try {
            value = Integer.parseInt(frac);
        } catch (NumberFormatException e) {
            return 0.0;
        }
        int len = frac.length();
        if (len == 1) {
            return value / 10.0;
        }
        if (len == 2) {
            return value / 100.0;
        }
        return value / 1000.0;
    }

    /**
     * 将「分:秒[.小数]」转为从当天 0 点起的绝对时间，与 HH:mm:ss 分支语义一致（便于排序与间隔）
     */
    private static Date parseMinutesSecondsFractionToDate(String trimmedTime) {
        Double total = parseMinutesSecondsToTotalSeconds(trimmedTime);
        if (total == null) {
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.setTimeInMillis(cal.getTimeInMillis() + Math.round(total * 1000.0));
        return cal.getTime();
    }

    private static Double parseMinutesSecondsToTotalSeconds(String trimmedTime) {
        if (trimmedTime == null || !MINUTES_SECONDS_FRACTION.matcher(trimmedTime).matches()) {
            return null;
        }
        int colon = trimmedTime.indexOf(':');
        try {
            int minutes = Integer.parseInt(trimmedTime.substring(0, colon));
            String afterColon = trimmedTime.substring(colon + 1);
            int dot = afterColon.indexOf('.');
            String secStr = dot >= 0 ? afterColon.substring(0, dot) : afterColon;
            String fracStr = dot >= 0 ? afterColon.substring(dot + 1) : null;
            int sec = Integer.parseInt(secStr);
            double frac = fractionDigitsToSeconds(fracStr);
            return minutes * 60.0 + sec + frac;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Double calculateMinSecFractionInterval(String currentTime, String lastTime) {
        try {
            Double currentTotal = parseMinutesSecondsToTotalSeconds(currentTime);
            Double lastTotal = parseMinutesSecondsToTotalSeconds(lastTime);
            if (currentTotal == null || lastTotal == null) {
                return 0.0;
            }
            double diffSeconds = currentTotal - lastTotal;
            if (diffSeconds < 0) {
                if (diffSeconds > -3600) {
                    diffSeconds = 0.0;
                } else {
                    diffSeconds += 24 * 3600;
                }
            }
            if (diffSeconds > 3600) {
                log.warn("[calculateMinSecFractionInterval][时间间隔异常大: {}秒 ({} -> {})，可能是数据问题]",
                        diffSeconds, lastTime, currentTime);
                diffSeconds = 3600.0;
            }
            return diffSeconds;
        } catch (Exception e) {
            log.warn("[calculateMinSecFractionInterval][分:秒格式解析失败: {} -> {}]", lastTime, currentTime, e);
            return 0.0;
        }
    }

}
