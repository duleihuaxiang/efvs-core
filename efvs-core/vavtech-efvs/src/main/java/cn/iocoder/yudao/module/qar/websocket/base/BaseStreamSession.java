package cn.iocoder.yudao.module.qar.websocket.base;

import lombok.Data;
import java.util.concurrent.ScheduledFuture;

/**
 * 数据流Session基类
 * 
 * @author admin
 */
@Data
public abstract class BaseStreamSession {

    /**
     * Session ID
     */
    private String sessionId;

    /**
     * 是否正在传输
     */
    private Boolean isRunning;

    /**
     * 是否暂停（暂停时不断开连接，只是停止数据传输）
     */
    private Boolean isPaused;

    /**
     * 当前速度倍数（1, 2, 3, 5, 8, 10）
     */
    private Integer speed;

    /**
     * 当前读取位置（最后一条记录的ID）
     */
    private Integer lastId;

    /**
     * 结束ID（可选，用于限制查询范围，比如按周期查询）
     */
    private Integer endId;

    /**
     * 定时任务Future（用于取消任务）
     */
    private ScheduledFuture<?> scheduledFuture;

    /**
     * 已传输的总记录数
     */
    private Long totalCount;

    /**
     * 当前周期编号（从1开始）
     */
    private Integer currentCycleIndex;

    public BaseStreamSession(String sessionId) {
        this.sessionId = sessionId;
        this.isRunning = false;
        this.isPaused = false;
        this.speed = 1; // 默认1倍速
        this.lastId = 0;
        this.totalCount = 0L;
        this.currentCycleIndex = 1; // 默认从第1个周期开始
    }

}
