package cn.iocoder.yudao.module.qar.controller.admin.qardata.vo;

import lombok.Data;

/**
 * taxi-phase 3D 复现返回。
 *
 * <p>返回结构：外层 {@code type} + {@code content(JSON string)}。</p>
 */
@Data
public class QarTaxiPhasePlaybackRespVO {

    /**
     * 外层消息类型：taxi-phase-stream
     */
    private String type;

    /**
     * content 为 TaxiPhaseStreamMessage 序列化后的 JSON 字符串。
     */
    private String content;
}

