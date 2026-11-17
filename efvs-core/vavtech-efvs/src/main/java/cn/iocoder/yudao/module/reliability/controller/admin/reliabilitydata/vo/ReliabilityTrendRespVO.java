package cn.iocoder.yudao.module.reliability.controller.admin.reliabilitydata.vo;
import lombok.Data;

/**
 * 可靠性趋势统计 Response VO（横轴：月份，纵轴：故障次数）
 */
@Data
public class ReliabilityTrendRespVO {
    private Integer month;

    private Integer issueCount;

    // 构造器（方便快速创建对象）
    public ReliabilityTrendRespVO(Integer month, Integer issueCount) {
        this.month = month;
        this.issueCount = issueCount;
    }
}
