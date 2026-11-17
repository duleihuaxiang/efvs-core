package cn.iocoder.yudao.module.reliability.service.reliabilitydata;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.reliability.controller.admin.reliabilitydata.vo.*;
import cn.iocoder.yudao.module.reliability.dal.dataobject.reliabilitydata.ReliabilityDataDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 可靠性数据收集 Service 接口
 *
 * @author admin
 */
public interface ReliabilityDataService {

    /**
     * 创建可靠性数据收集
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Integer createData(@Valid ReliabilityDataSaveReqVO createReqVO);

    /**
     * 更新可靠性数据收集
     *
     * @param updateReqVO 更新信息
     */
    void updateData(@Valid ReliabilityDataSaveReqVO updateReqVO);

    /**
     * 删除可靠性数据收集
     *
     * @param id 编号
     */
    void deleteData(Integer id);

    /**
    * 批量删除可靠性数据收集
    *
    * @param ids 编号
    */
    void deleteDataListByIds(List<Integer> ids);

    /**
     * 获得可靠性数据收集
     *
     * @param id 编号
     * @return 可靠性数据收集
     */
    ReliabilityDataDO getReliabilityDataById(Integer id);

    /**
     * 获得可靠性数据收集分页
     *
     * @param pageReqVO 分页查询
     * @return 可靠性数据收集分页
     */
    PageResult<ReliabilityDataDO> getDataPage(ReliabilityDataPageReqVO pageReqVO);

    /**
     * 获得可靠性数据明细分页
     *
     * @param pageReqVO 分页查询
     * @return 可靠性数据明细分页
     */
    PageResult<ReliabilityDataRespVO> getDataDetailPage(ReliabilityDataPageReqVO pageReqVO);

    /**
     * 计算 MTBF 和 MTBUR 指标
     *
     * @param partNumber 拆卸部件号
     * @return MtbfMtburRespVO 包含计算结果的响应对象
     */
    MtbfMtburRespVO calculateReliabilityMetrics(String partNumber);

    /**
     * 导出MTBF或MTBUR指标数据
     * @param type 导出类型：mtbf 或 mtbur
     * @param pageReqVO 查询条件
     * @return 数据列表
     */
    List<?> exportMetricsData(String type, @Valid ReliabilityDataPageReqVO pageReqVO);

    /**
     * 导出MTBF指标数据
     * @param pageReqVO 查询条件
     * @return 数据列表
     */
    List<MtbfRespVO> getMtbfDataExport(ReliabilityDataPageReqVO pageReqVO);
    /**
     * 导出MTBUR指标数据
     * @param pageReqVO 查询条件
     * @return 数据列表
     */
    List<MtburRespVO> getMtburDataExport(ReliabilityDataPageReqVO pageReqVO);



    List<reliabilityDataFormNoVO> getAll(String reliabilityDataFormNo);

    /**
     * 获取可靠性数据收集表单编号
     *
     * @return 表单编号
     */
    String getGenerateReliabilityDataFormNo();

    /**
     * 可靠性数据管理分页查询
     *
     * @param reqVO 查询条件（含 partName、partNumber）
     * @return 分页结果
     */
    PageResult<FlightReliableDataMgrRespVO> getDataPageForMgr(ReliabilityDataPageReqVO reqVO);

    /**
     * 可靠性数据管理分页查询（SQL 聚合版）
     *
     * @param reqVO 查询条件（含 partName、partNumber）
     * @return 分页结果
     */
    PageResult<FlightReliableDataMgrRespVO> getDataPageForMgrBySql(ReliabilityDataPageReqVO reqVO);


    ReliabilityDataDO getReliabilityByProcessInstanceId(String processInstanceId);

    /**
     * 统计当年可靠性趋势（横轴：月份，纵轴：故障次数）
     * @return 1-12月的趋势数据
     */
    List<ReliabilityTrendRespVO> statReliabilityTrend();

    /**
     * 获取下拉：部件名称（基于产品表 efvs_product 的 name 字段）
     *
     * @return 部件名称列表
     */
    List<Map<String,String>> getPartNameOptions();

    /**
     * 获取下拉：版本/系列（基于产品表 efvs_product 的 sign_version 字段）
     *
     * @param partName 部件名称
     * @return 版本列表
     */
    List<String> getSeriesOptionsByPartName(String partName, String serialNumber);

    /**
     * 获取下拉：序列号（基于产品表 efvs_product 的 sequence_number 字段）
     *
     * @param partName 部件名称
     * @return 序列号列表
     */
    List<String> getSerialNumberOptionsByPartName(String partName, String version);

    /**
     * 回填：按（部件名称 + 版本 + 序列号）找到产品并返回 部件代号/交付时间
     *
     * @param partName 部件名称
     * @param series   版本/系列（efvs_product.sign_version）
     * @param serialNumber 序列号（efvs_product.sequence_number）
     * @return 回填数据
     */
    cn.iocoder.yudao.module.reliability.controller.admin.reliabilitydata.vo.ReliabilityDataPartCascadeFillRespVO getPartCodeAndDeliveryTime(
            String partName,
            String series,
            String serialNumber);

    List<Map<String, String>> findSRU(String partNumber, String version, String serialNumber);

}