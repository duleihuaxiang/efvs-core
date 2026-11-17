package cn.iocoder.yudao.module.ReliabilityDataMetrics.service.ReliabilityDataMetrics;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import cn.iocoder.yudao.module.ReliabilityDataMetrics.controller.admin.ReliabilityDataMetrics.vo.*;
import cn.iocoder.yudao.module.ReliabilityDataMetrics.dal.dataobject.ReliabilityDataMetrics.ReliabilityDataMetricsDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.ReliabilityDataMetrics.dal.mysql.ReliabilityDataMetrics.ReliabilityDataMetricsMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.data.enums.ErrorCodeConstants.RELIABILITY_DATA_METRICS_NOT_EXISTS;

/**
 * 可靠性指标 Service 实现类
 *
 * @author admin
 */
@Service
@Validated
public class ReliabilityDataMetricsServiceImpl implements ReliabilityDataMetricsService {

    @Resource
    private ReliabilityDataMetricsMapper reliabilityDataMetricsMapper;

    @Override
    public Integer createReliabilityDataMetrics(ReliabilityDataMetricsSaveReqVO createReqVO) {
        // 插入
        ReliabilityDataMetricsDO reliabilityDataMetrics = BeanUtils.toBean(createReqVO, ReliabilityDataMetricsDO.class);
        reliabilityDataMetricsMapper.insert(reliabilityDataMetrics);

        // 返回
        return reliabilityDataMetrics.getId();
    }

    @Override
    public void updateReliabilityDataMetrics(ReliabilityDataMetricsSaveReqVO updateReqVO) {
        // 校验存在
        validateReliabilityDataMetricsExists(updateReqVO.getId());
        // 更新
        ReliabilityDataMetricsDO updateObj = BeanUtils.toBean(updateReqVO, ReliabilityDataMetricsDO.class);
        reliabilityDataMetricsMapper.updateById(updateObj);
    }

    @Override
    public void deleteReliabilityDataMetrics(Integer id) {
        // 校验存在
        validateReliabilityDataMetricsExists(id);
        // 删除
        reliabilityDataMetricsMapper.deleteById(id);
    }

    @Override
        public void deleteReliabilityDataMetricsListByIds(List<Integer> ids) {
        // 删除
        reliabilityDataMetricsMapper.deleteByIds(ids);
        }


    private void validateReliabilityDataMetricsExists(Integer id) {
        if (reliabilityDataMetricsMapper.selectById(id) == null) {
            throw exception(RELIABILITY_DATA_METRICS_NOT_EXISTS);
        }
    }

    @Override
    public ReliabilityDataMetricsDO getReliabilityDataMetrics(Integer id) {
        return reliabilityDataMetricsMapper.selectById(id);
    }

    @Override
    public PageResult<ReliabilityDataMetricsDO> getReliabilityDataMetricsPage(ReliabilityDataMetricsPageReqVO pageReqVO) {
        return reliabilityDataMetricsMapper.selectPage(pageReqVO);
    }

    @Override
    public ReliabilityDataMetricsDO getReliabilityDataMetricsByPatNumber(String partNumber, Long tenantId) {
        // 指标数据按部件号和租户维度隔离，调用方必须提供所属租户，不能跨租户复用指标记录。
        return reliabilityDataMetricsMapper.getReliabilityDataMetricsByPatNumber(partNumber, tenantId);
    }

}
