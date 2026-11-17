package cn.iocoder.yudao.module.ReliabilityDataMetrics.service.ReliabilityDataMetrics;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.ReliabilityDataMetrics.controller.admin.ReliabilityDataMetrics.vo.*;
import cn.iocoder.yudao.module.ReliabilityDataMetrics.dal.dataobject.ReliabilityDataMetrics.ReliabilityDataMetricsDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 可靠性指标 Service 接口
 *
 * @author admin
 */
public interface ReliabilityDataMetricsService {

    /**
     * 创建可靠性指标
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Integer createReliabilityDataMetrics(@Valid ReliabilityDataMetricsSaveReqVO createReqVO);

    /**
     * 更新可靠性指标
     *
     * @param updateReqVO 更新信息
     */
    void updateReliabilityDataMetrics(@Valid ReliabilityDataMetricsSaveReqVO updateReqVO);

    /**
     * 删除可靠性指标
     *
     * @param id 编号
     */
    void deleteReliabilityDataMetrics(Integer id);

    /**
    * 批量删除可靠性指标
    *
    * @param ids 编号
    */
    void deleteReliabilityDataMetricsListByIds(List<Integer> ids);

    /**
     * 获得可靠性指标
     *
     * @param id 编号
     * @return 可靠性指标
     */
    ReliabilityDataMetricsDO getReliabilityDataMetrics(Integer id);

    /**
     * 获得可靠性指标分页
     *
     * @param pageReqVO 分页查询
     * @return 可靠性指标分页
     */
    PageResult<ReliabilityDataMetricsDO> getReliabilityDataMetricsPage(ReliabilityDataMetricsPageReqVO pageReqVO);

    ReliabilityDataMetricsDO getReliabilityDataMetricsByPatNumber(String partNumber, Long tenantId);

}