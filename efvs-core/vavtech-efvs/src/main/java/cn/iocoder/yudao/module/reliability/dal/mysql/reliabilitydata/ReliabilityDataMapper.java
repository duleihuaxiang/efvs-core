package cn.iocoder.yudao.module.reliability.dal.mysql.reliabilitydata;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.reliability.dal.dataobject.reliabilitydata.ReliabilityDataDO;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.reliability.controller.admin.reliabilitydata.vo.*;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 可靠性数据收集 Mapper
 *
 * @author admin
 */
@Mapper
public interface ReliabilityDataMapper extends BaseMapperX<ReliabilityDataDO> {

    /**
     * 通用分页查询
     *
     * @param reqVO        查询条件
     * @param includeLevel 是否包含 level 过滤
     * @return 分页结果
     */
    default PageResult<ReliabilityDataDO> selectPage(ReliabilityDataPageReqVO reqVO,
                                                     boolean includeLevel) {
        LambdaQueryWrapperX<ReliabilityDataDO> wrapper = new LambdaQueryWrapperX<>();
        // 公共条件
        wrapper.eqIfPresent(ReliabilityDataDO::getReliabilityDataFormNo, reqVO.getReliabilityDataFormNo())
                .likeIfPresent(ReliabilityDataDO::getCustomerName, reqVO.getCustomerName())
                .likeIfPresent(ReliabilityDataDO::getPartName, reqVO.getPartName())
                .eqIfPresent(ReliabilityDataDO::getPartNumber, reqVO.getPartNumber())
                .eqIfPresent(ReliabilityDataDO::getSerialNumber, reqVO.getSerialNumber())
                .eqIfPresent(ReliabilityDataDO::getModel, reqVO.getModel())
                .eqIfPresent(ReliabilityDataDO::getSeries, reqVO.getSeries())
                .eqIfPresent(ReliabilityDataDO::getMachineNumber, reqVO.getMachineNumber())
                .betweenIfPresent(ReliabilityDataDO::getReportStartDate, reqVO.getReportStartDate())
                .betweenIfPresent(ReliabilityDataDO::getReportEndDate, reqVO.getReportEndDate())
                .betweenIfPresent(ReliabilityDataDO::getRemovalDate, reqVO.getRemovalDate())
                .eqIfPresent(ReliabilityDataDO::getIsProductIssue, reqVO.getIsProductIssue())
                .eqIfPresent(ReliabilityDataDO::getRemovalType, reqVO.getRemovalType())
                .eqIfPresent(ReliabilityDataDO::getRemovedPartNumber, reqVO.getRemovedPartNumber())
                .eqIfPresent(ReliabilityDataDO::getRemovedPartSerialNumber, reqVO.getRemovedPartSerialNumber())
                .betweenIfPresent(ReliabilityDataDO::getCreateTime, reqVO.getCreateTime());

        // 特定条件：支持多 Level 筛选（核心修改）
        if (includeLevel) {
            if (CollectionUtils.isNotEmpty(reqVO.getLevelList())) {
                wrapper.in(ReliabilityDataDO::getLevel, reqVO.getLevelList()); // 多 Level 用 IN 条件
            } else if (reqVO.getLevel() != null) {
                wrapper.eq(ReliabilityDataDO::getLevel, reqVO.getLevel()); // 兼容原有单个 Level 查询
            }
        }
        // 添加排序条件
        wrapper.orderByDesc(ReliabilityDataDO::getId);

        return selectPage(reqVO, wrapper);
    }

    /**
     * 查询指定日期的最大序列号
     * @param dateStr 日期字符串，格式：yyyyMMdd
     * @return 最大序列号
     */
    Integer selectMaxSerialNumber(@Param("dateStr") String dateStr);

    List<reliabilityDataFormNoVO> selectAll(@Param("tenantId") Long tenantId, @Param("reliabilityDataFormNo") String reliabilityDataFormNo);

    /**
     * 根据父级ID列表查询level=2的子集数据
     *
     * @param parentPartNumbers 父级列表
     * @param tenantId          租户ID
     * @return 子集数据列表
     */
    List<ReliabilityDataDO> selectLevel2ChildrenByParentPartNumbers(@Param("parentPartNumbers") List<String> parentPartNumbers,
                                                                    @Param("tenantId") Long tenantId);

    /**
     * 飞行可靠性数据管理分页（SQL 聚合版）
     */
    List<FlightReliableDataMgrRespVO> selectDataPageForMgrBySql(@Param("partName") String partName,
                                                                @Param("partNumber") String partNumber,
                                                                @Param("tenantId") Long tenantId,
                                                                @Param("offset") Integer offset,
                                                                @Param("pageSize") Integer pageSize,
                                                                @Param("startTime") LocalDateTime startTime,
                                                                @Param("endTime") LocalDateTime endTime);

    /**
     * 飞行可靠性数据管理分页总数（SQL 聚合版）
     */
    Long countDataPageForMgrBySql(@Param("partName") String partName,
                                  @Param("partNumber") String partNumber,
                                  @Param("tenantId") Long tenantId,
                                  @Param("startTime") LocalDateTime startTime,
                                  @Param("endTime") LocalDateTime endTime);

    List<ReliabilityDataDO> selectByPartNumber(@Param("tenantId") Long tenantId, @Param("partNumber") String partNumber, @Param("isProductIssue") Integer isProductIssue);

    List<ReliabilityDataDO> selectByPartNumberNoIsProductIssue(@Param("tenantId") Long tenantId, @Param("partNumber") String partNumber);
}