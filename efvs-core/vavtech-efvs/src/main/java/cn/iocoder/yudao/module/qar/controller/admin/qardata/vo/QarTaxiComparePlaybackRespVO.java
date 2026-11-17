package cn.iocoder.yudao.module.qar.controller.admin.qardata.vo;

import lombok.Data;

/**
 * 性能分析（多架次对比）一次性聚合返回。
 *
 * <p>返回结构贴近 WebSocket 帧：外层 {@code type} + {@code content(JSON string)}。</p>
 */
@Data
public class QarTaxiComparePlaybackRespVO {

    /**
     * 外层消息类型：taxi-compare-stream
     */
    private String type;

    /**
     * content：聚合后的 JSON 字符串（前端一次解析后即可绘图）。
     */
    private String content;
}

