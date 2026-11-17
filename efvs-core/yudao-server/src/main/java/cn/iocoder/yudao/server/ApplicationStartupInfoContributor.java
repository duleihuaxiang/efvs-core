package cn.iocoder.yudao.server;

import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 在 /actuator/info 中补充应用就绪时间与 JVM 启动时间，便于与 process.uptime 等指标对照。
 */
@Component
public class ApplicationStartupInfoContributor implements InfoContributor {

    private volatile Instant applicationReadyTime;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        if (applicationReadyTime == null) {
            applicationReadyTime = Instant.now();
        }
    }

    @Override
    public void contribute(Info.Builder builder) {
        Map<String, Object> application = new LinkedHashMap<>();
        if (applicationReadyTime != null) {
            application.put("readyTime", applicationReadyTime.toString());
            application.put("readyTimeEpochMillis", applicationReadyTime.toEpochMilli());
        }
        application.put("jvmStartTimeEpochMillis",
                java.lang.management.ManagementFactory.getRuntimeMXBean().getStartTime());
        builder.withDetail("application", application);
    }
}
