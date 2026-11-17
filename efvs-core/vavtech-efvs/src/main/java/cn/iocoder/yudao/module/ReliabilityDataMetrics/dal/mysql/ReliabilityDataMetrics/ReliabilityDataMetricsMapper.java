package cn.iocoder.yudao.module.ReliabilityDataMetrics.dal.mysql.ReliabilityDataMetrics;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.ReliabilityDataMetrics.dal.dataobject.ReliabilityDataMetrics.ReliabilityDataMetricsDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.ReliabilityDataMetrics.controller.admin.ReliabilityDataMetrics.vo.*;
import org.apache.ibatis.annotations.Param;

/**
 * 可靠性指标 Mapper
 *
 * @author admin
 */
@Mapper
public interface ReliabilityDataMetricsMapper extends BaseMapperX<ReliabilityDataMetricsDO> {

    default PageResult<ReliabilityDataMetricsDO> selectPage(ReliabilityDataMetricsPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ReliabilityDataMetricsDO>()
                .likeIfPresent(ReliabilityDataMetricsDO::getComponentName, reqVO.getComponentName())
                .eqIfPresent(ReliabilityDataMetricsDO::getPartNumber, reqVO.getPartNumber())
                .orderByDesc(ReliabilityDataMetricsDO::getId));
    }

    ReliabilityDataMetricsDO getReliabilityDataMetricsByPatNumber(@Param("partNumber") String partNumber, @Param("tenantId") Long tenantId);
}