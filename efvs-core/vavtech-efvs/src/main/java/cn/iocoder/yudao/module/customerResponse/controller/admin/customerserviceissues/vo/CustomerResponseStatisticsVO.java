package cn.iocoder.yudao.module.customerResponse.controller.admin.customerserviceissues.vo;

import lombok.Data;

import java.util.List;

/**
 * 客户响应统计 VO
 *
 * @author admin
 */
@Data
public class CustomerResponseStatisticsVO {

    private List<Integer> months;

    private List<Long> respondedCounts;

    private List<Long> unrespondedCounts;

    private List<MonthStatistics> monthStatistics;

    @Data
    public static class MonthStatistics {
        private Integer month;

        private Long respondedCount;

        private Long unrespondedCount;

        private Long totalCount;
    }
}
