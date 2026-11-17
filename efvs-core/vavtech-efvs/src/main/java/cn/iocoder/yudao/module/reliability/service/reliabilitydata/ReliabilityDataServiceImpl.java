package cn.iocoder.yudao.module.reliability.service.reliabilitydata;

import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.ReliabilityDataMetrics.dal.dataobject.ReliabilityDataMetrics.ReliabilityDataMetricsDO;
import cn.iocoder.yudao.module.ReliabilityDataMetrics.service.ReliabilityDataMetrics.ReliabilityDataMetricsService;
import cn.iocoder.yudao.module.data.dal.dataobject.product.ProductDO;
import cn.iocoder.yudao.module.data.dal.dataobject.productfile.ProductFileDO;
import cn.iocoder.yudao.module.data.dal.mysql.product.ProductMapper;
import cn.iocoder.yudao.module.data.service.productfile.ProductFileService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import cn.iocoder.yudao.module.reliability.controller.admin.reliabilitydata.vo.*;
import cn.iocoder.yudao.module.reliability.dal.dataobject.reliabilitydata.ReliabilityDataDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.reliability.dal.mysql.reliabilitydata.ReliabilityDataMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.data.enums.ErrorCodeConstants.RELIABILITY_DATA_NOT_EXISTS;


/**
 * 可靠性数据收集 Service 实现类
 *
 * @author admin
 */
@Service
@Validated
public class ReliabilityDataServiceImpl extends ServiceImpl<ReliabilityDataMapper, ReliabilityDataDO> implements ReliabilityDataService {

    @Resource
    private ReliabilityDataMapper dataMapper;
    @Resource
    private ProductMapper productMapper;

    @Resource
    private ReliabilityDataMetricsService reliabilityDataMetricsService;

    @Resource
    private ProductFileService productFileService;

    @Override
    public Integer createData(ReliabilityDataSaveReqVO createReqVO) {
        // 如果没有提供可靠性数据收集表单编号，则自动生成
        if (createReqVO.getReliabilityDataFormNo() == null || createReqVO.getReliabilityDataFormNo().isEmpty()) {
            // 生成新的编号
            String reliabilityDataFormNo = generateReliabilityDataFormNo();
            createReqVO.setReliabilityDataFormNo(reliabilityDataFormNo);
        }

        // 插入
        ReliabilityDataDO data = BeanUtils.toBean(createReqVO, ReliabilityDataDO.class);
        dataMapper.insert(data);

        // 返回
        return data.getId();
    }

    @Override
    public String getGenerateReliabilityDataFormNo() {
        // 生成新的编号
        return generateReliabilityDataFormNo();
    }

    /**
     * 生成可靠性数据收集表单编号
     * 格式：R+年月日+3位流水号 (R20260120001)
     */
    private String generateReliabilityDataFormNo() {
        // 获取当前日期
        LocalDate now = LocalDate.now();
        String dateStr = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // 获取下一个序列号
        int serialNumber = getNextSerialNumber(now);

        // 组装编号
        return "R" + dateStr + String.format("%03d", serialNumber);
    }

    /**
     * 获取指定日期的下一个序列号
     *
     * @param date 日期
     * @return 序列号
     */
    private int getNextSerialNumber(LocalDate date) {
        // 查询数据库中同一天的最大序列号
        String dateString = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Integer maxSerialNumber = dataMapper.selectMaxSerialNumber(dateString);

        // 如果没有找到记录，则从1开始
        if (maxSerialNumber == null) {
            return 1;
        }

        // 返回下一个序列号
        return maxSerialNumber + 1;
    }


    @Override
    public void updateData(ReliabilityDataSaveReqVO updateReqVO) {
        // 校验存在
        validateDataExists(updateReqVO.getId());
        // 更新
        ReliabilityDataDO updateObj = BeanUtils.toBean(updateReqVO, ReliabilityDataDO.class);
        dataMapper.updateById(updateObj);
    }

    @Override
    public void deleteData(Integer id) {
        // 校验存在
        validateDataExists(id);
        // 删除
        dataMapper.deleteById(id);
    }

    @Override
    public void deleteDataListByIds(List<Integer> ids) {
        // 删除
        dataMapper.deleteByIds(ids);
    }


    private void validateDataExists(Integer id) {
        if (dataMapper.selectById(id) == null) {
            throw exception(RELIABILITY_DATA_NOT_EXISTS);
        }
    }

    @Override
    public ReliabilityDataDO getReliabilityDataById(Integer id) {
        return dataMapper.selectById(id);
    }

    @Override
    public ReliabilityDataDO getReliabilityByProcessInstanceId(String processInstanceId) {
        // 可靠性数据由流程表单创建后，通过流程实例编号回查，供审批页和后续节点同步状态使用。
        return dataMapper.selectOne(ReliabilityDataDO::getProcessInstanceId, processInstanceId);
    }

    @Override
    public PageResult<ReliabilityDataDO> getDataPage(ReliabilityDataPageReqVO pageReqVO) {
        return dataMapper.selectPage(pageReqVO, false);
    }

    @Override
    public PageResult<ReliabilityDataRespVO> getDataDetailPage(ReliabilityDataPageReqVO pageReqVO) {
        Long tenantId = TenantContextHolder.getTenantId();
        // 明细页始终按当前租户过滤；其余条件均为可选条件，日期区间在下方单独处理。
        LambdaQueryWrapperX<ReliabilityDataDO> wrapper = new LambdaQueryWrapperX<>();
        // 添加明细页可选筛选条件（对应图2查询表单）
        wrapper.likeIfPresent(ReliabilityDataDO::getSeries, pageReqVO.getSeries())
                .eq(ReliabilityDataDO::getTenantId, tenantId)
                .eqIfPresent(ReliabilityDataDO::getVersion, pageReqVO.getVersion())
                .likeIfPresent(ReliabilityDataDO::getCustomerName, pageReqVO.getCustomerName())
                .eqIfPresent(ReliabilityDataDO::getRemovedPartSerialNumber, pageReqVO.getRemovedPartSerialNumber())
                .eqIfPresent(ReliabilityDataDO::getDeleted, 0)
                .eqIfPresent(ReliabilityDataDO::getModel, pageReqVO.getModel())
                .eqIfPresent(ReliabilityDataDO::getRemovalType, pageReqVO.getRemovalType())
                .eqIfPresent(ReliabilityDataDO::getSerialNumber, pageReqVO.getSerialNumber())
                .eqIfPresent(ReliabilityDataDO::getMachineNumber, pageReqVO.getMachineNumber())
                .eqIfPresent(ReliabilityDataDO::getRemovalDate, pageReqVO.getRemovalDate() != null && pageReqVO.getRemovalDate().length > 0 ? pageReqVO.getRemovalDate()[0] : null)
                .eqIfPresent(ReliabilityDataDO::getRemovedPartNumber, pageReqVO.getRemovedPartNumber())
                .eqIfPresent(ReliabilityDataDO::getPartName, pageReqVO.getPartName())
                .eqIfPresent(ReliabilityDataDO::getPartNumber, pageReqVO.getPartNumber())
                .eqIfPresent(ReliabilityDataDO::getIsProductIssue, pageReqVO.getIsProductIssue());
        LocalDateTime[] removalDateRange = pageReqVO.getRemovalDate();
        if (removalDateRange != null && removalDateRange.length >= 2) {
            wrapper.ge(ReliabilityDataDO::getRemovalDate, removalDateRange[0])
                    .le(ReliabilityDataDO::getRemovalDate, removalDateRange[1]);
        }
        // 排序（按报告开始日期倒序，最新数据在前）
        wrapper.orderByDesc(ReliabilityDataDO::getReportStartDate);
        // 执行分页查询
        PageResult<ReliabilityDataDO> dataPage = dataMapper.selectPage(pageReqVO, wrapper);
        List<ReliabilityDataDO> dataList = dataPage.getList();
        // DO转VO（映射明细页所需字段）
        List<ReliabilityDataRespVO> voList = dataList.stream()
                .map(doObj -> BeanUtils.toBean(doObj, ReliabilityDataRespVO.class))
                .collect(Collectors.toList());
        // 封装分页结果
        return new PageResult<>(voList, dataPage.getTotal());
    }

    @Override
    public MtbfMtburRespVO calculateReliabilityMetrics(String partNumber) {
        // 根据disassemblyPartNo查询一条相关记录
        ReliabilityDataDO record = dataMapper.selectOne(
                Wrappers.<ReliabilityDataDO>lambdaQuery()
                        .eq(ReliabilityDataDO::getPartNumber, partNumber)
                        .eq(ReliabilityDataDO::getTenantId, TenantContextHolder.getTenantId())
        );
        if (record == null) {
            return new MtbfMtburRespVO();
        }
        // 构造响应对象
        MtbfMtburRespVO respVO = new MtbfMtburRespVO();
        respVO.setPartNo(partNumber);
        respVO.setEquipmentName("涡轮风扇发动机");
        // 当前指标为演示/占位计算：缺少部件首次安装时间、机队飞行小时和实际故障统计来源。
        // 后续接入真实可靠性统计口径时，应替换下方固定参数，避免将演示值用于正式业务决策。
        BigDecimal totalFlightTime = BigDecimal.valueOf(50);
        //获取部件每架装机数、部件故障次数、部件非计划拆换次数
        int totalInstallCount = 89;      // 部件每架装机数
        int failureCount = 6;           // 部件故障次数
        int unplannedReplaceCount = 3;  // 部件非计划拆换次数

        // 计算 MTBF 实际值
        BigDecimal mtbfActual = failureCount > 0 ?
                totalFlightTime.multiply(BigDecimal.valueOf(totalInstallCount))
                        .divide(BigDecimal.valueOf(failureCount), 0, BigDecimal.ROUND_HALF_UP) :
                BigDecimal.ZERO;

        // 计算 MTBUR 实际值
        BigDecimal mtburActual = unplannedReplaceCount > 0 ?
                totalFlightTime.multiply(BigDecimal.valueOf(totalInstallCount))
                        .divide(BigDecimal.valueOf(unplannedReplaceCount), 0, BigDecimal.ROUND_HALF_UP) :
                BigDecimal.ZERO;

        // 计算 FR（故障率）
        BigDecimal fr = failureCount > 0 ?
                BigDecimal.valueOf(failureCount)
                        .divide(totalFlightTime.multiply(BigDecimal.valueOf(totalInstallCount)), 10, BigDecimal.ROUND_HALF_UP)
                        .multiply(BigDecimal.valueOf(1000)).setScale(6, BigDecimal.ROUND_HALF_UP) :
                BigDecimal.ZERO;

        // 计算 URR（非计划拆换率）
        BigDecimal urr = unplannedReplaceCount > 0 ?
                BigDecimal.valueOf(unplannedReplaceCount)
                        .divide(totalFlightTime.multiply(BigDecimal.valueOf(totalInstallCount)), 10, BigDecimal.ROUND_HALF_UP)
                        .multiply(BigDecimal.valueOf(1000)).setScale(6, BigDecimal.ROUND_HALF_UP) :
                BigDecimal.ZERO;

        respVO.setTotalFlightTime(totalFlightTime);
        respVO.setTotalInstallCount(totalInstallCount);
        respVO.setFailureCount(failureCount);
        respVO.setUnplannedReplaceCount(unplannedReplaceCount);
        respVO.setMtbfActual(mtbfActual);
        respVO.setMtbfTarget(fr);
        respVO.setMtburActual(mtburActual);
        respVO.setMtburTarget(urr);
        return respVO;
    }

    @Override
    public List<?> exportMetricsData(String type, ReliabilityDataPageReqVO pageReqVO) {
        // 设置分页大小为无限制，获取所有数据
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);

        // 获取分页结果
        PageResult<ReliabilityDataDO> pageResult = getDataPage(pageReqVO);
        List<ReliabilityDataDO> dataList = pageResult.getList();

        // 创建结果列表
        List<Object> result = new ArrayList<>();

        // 计算总飞行时间、装机数、故障次数和非计划拆换次数
        ////缺少部件首次安装时间，用于计算机队总飞行小时数（当前日期 - 安装日期）*8 换算为小时；
        BigDecimal totalFlightTime = BigDecimal.valueOf(50);
        int totalInstallCount = 0;
        int failureCount = 0;
        int unplannedReplaceCount = 0;
        String partNo = null;
        String equipmentName = null;

        // 遍历数据计算各项指标
        for (ReliabilityDataDO data : dataList) {

            // 1. 部件每架装机数（即数据条数）
            totalInstallCount++;

            // 2. 部件故障次数（failureConfirmation为CF时）
            if (data.getIsProductIssue() == 1) {
                failureCount++;
            }

            // 3. 部件非计划拆换次数（removalType为U时）
            if ("U".equals(data.getRemovalType())) {
                unplannedReplaceCount++;
            }
            partNo = data.getPartNumber();
            equipmentName = data.getPartName();
        }

        // 根据类型创建不同的响应对象
        if ("mtbf".equalsIgnoreCase(type)) {
            MtbfRespVO respVO = new MtbfRespVO();
            respVO.setPartNo(partNo);
            respVO.setEquipmentName(equipmentName);
            respVO.setTotalFlightTime(totalFlightTime);
            respVO.setTotalInstallCount(totalInstallCount);
            respVO.setFailureCount(failureCount);

            // 计算 MTBF
            BigDecimal mtbfActual = failureCount > 0 ?
                    totalFlightTime.multiply(BigDecimal.valueOf(totalInstallCount))
                            .divide(BigDecimal.valueOf(failureCount), 2, BigDecimal.ROUND_HALF_UP) :
                    BigDecimal.ZERO;

            respVO.setMtbfActual(mtbfActual);
            respVO.setMtbfTarget(BigDecimal.valueOf(1));

            result.add(respVO);
        } else if ("mtbur".equalsIgnoreCase(type)) {
            MtburRespVO respVO = new MtburRespVO();
            respVO.setPartNo(partNo);
            respVO.setEquipmentName(equipmentName);
            respVO.setTotalFlightTime(totalFlightTime);
            respVO.setTotalInstallCount(totalInstallCount);
            respVO.setUnplannedReplaceCount(unplannedReplaceCount);

            // 计算 MTBUR
            BigDecimal mtburActual = unplannedReplaceCount > 0 ?
                    totalFlightTime.multiply(BigDecimal.valueOf(totalInstallCount))
                            .divide(BigDecimal.valueOf(unplannedReplaceCount), 2, BigDecimal.ROUND_HALF_UP) :
                    BigDecimal.ZERO;

            respVO.setMtburActual(mtburActual);
            respVO.setMtburTarget(BigDecimal.valueOf(1));

            result.add(respVO);
        }

        return result;
    }

    /**
     * 判断指定部件号是否为层级1的子集（包括子集的子集）
     *
     * @param sruNumber SRU代号
     * @param dataList  数据列表
     * @return 是否为层级1的子集
     */
    private boolean isChildOfLevel1(String sruNumber, List<ReliabilityDataDO> dataList) {
        // 查找父级记录
        ReliabilityDataDO parentData = dataList.stream()
                .filter(data -> data.getPartNumber() != null && data.getPartNumber().equals(sruNumber))
                .findFirst()
                .orElse(null);

        // 如果父级记录存在且层级为1，则返回true
        if (parentData != null && parentData.getLevel() == 1) {
            return true;
        }

        // 如果父级记录存在但层级不是1，递归检查父级的父级
        if (parentData != null) {
            return isChildOfLevel1(parentData.getPartNumber(), dataList);
        }
        // 如果父级记录不存在，尝试查找直接为1的父级数据
        if (sruNumber != null) {
            Long tenantId = TenantContextHolder.getTenantId();
            List<ProductDO> productList = productMapper.getProductFile(sruNumber, tenantId);
            return isChildOfLevel1InProductList(sruNumber, productList);
        }
        return false;
    }

    /**
     * 在产品列表中判断指定部件号是否为层级1的子集（包括子集的子集）
     *
     * @param partNumber  代号
     * @param productList 产品列表
     * @return 是否为层级1的子集
     */
    private boolean isChildOfLevel1InProductList(String partNumber, List<ProductDO> productList) {
        // 查找直接父级
        ProductDO directParentData = productList.stream()
                .filter(data -> data.getPartNumber() != null)
                .findFirst()
                .orElse(null);

        // 如果找到直接父级且层级为1，则返回true
        if (directParentData != null) {
            return true;
        }

        // 如果直接父级存在但层级不是1，递归检查其父级
        if (directParentData != null) {
            return isChildOfLevel1InProductList(directParentData.getPartNumber(), productList);
        }

        return false;
    }

    /**
     * 计算飞行时间和装机总数
     *
     * @return 包含总飞行时间和装机总数的Map
     */
    private Map<String, Object> calculateFlightTimeAndInstallCount() {
        // 计算总飞行时间（当前日期减去两天前的时间）
        LocalDate currentDate = LocalDate.now();
        LocalDate twoDaysAgo = currentDate.minusDays(2);
        long daysDifference = ChronoUnit.DAYS.between(twoDaysAgo, currentDate);
        BigDecimal totalFlightTime = BigDecimal.valueOf(daysDifference * 8); // 假设每天按8小时计算

        // 计算装机总数（数据条数）
        int totalInstallCount = 1;
        // 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("totalFlightTime", totalFlightTime);
        result.put("totalInstallCount", totalInstallCount);
        return result;
    }

    @Override
    public List<reliabilityDataFormNoVO> getAll(String reliabilityDataFormNo) {
        Long tenantId = TenantContextHolder.getTenantId();
        List<reliabilityDataFormNoVO> list = dataMapper.selectAll(tenantId, reliabilityDataFormNo);
        return list;
    }

    @Override
    public PageResult<FlightReliableDataMgrRespVO> getDataPageForMgr(ReliabilityDataPageReqVO reqVO) {
        // 筛选 level=1 + isProductIssue=1 的父级数据
        Long tenantId = TenantContextHolder.getTenantId();
        reqVO.setLevel(1);
        PageResult<ReliabilityDataDO> parentPage = dataMapper.selectPage(reqVO, true);
        List<ReliabilityDataDO> parentList = parentPage.getList();
        if (parentList.isEmpty()) {
            // 无父级数据，直接返回空分页（避免后续无效查询）
            return new PageResult<>(Collections.emptyList(), 0L);
        }

        // 批量查询所有父级对应的 level=2 子集数据（解决 N+1 问题）
        List<String> parentPartNumbers = parentList.stream()
                .map(ReliabilityDataDO::getPartNumber) // 提取父级部件号列表
                .collect(Collectors.toList());
        List<ReliabilityDataDO> childList = dataMapper.selectLevel2ChildrenByParentPartNumbers(parentPartNumbers, tenantId);

        // 用 Map 缓存「父级部件号→子集 issueCount 总和」
        Map<String, Integer> parentChildIssueCountMap = childList.stream()
                // 基础过滤：排除null元素 + sruNumber为空
                .filter(Objects::nonNull)
                .filter(child -> child.getSruNumber() != null)
                // 分组统计：按sruNumber分组，统计每组的记录条数
                .collect(Collectors.groupingBy(
                        ReliabilityDataDO::getSruNumber,
                        Collectors.summingInt(child -> 1) // 每条记录计1，累加即为条数
                ));

        // 父级数据转 VO + 叠加子集 issueCount（核心统计逻辑）
        List<FlightReliableDataMgrRespVO> voList = parentList.stream()
                .map(parent -> {
                    // 父级数据转 VO（基础字段映射）
                    FlightReliableDataMgrRespVO vo = BeanUtils.toBean(parent, FlightReliableDataMgrRespVO.class);
                    // 计算最终 issueCount：父级自身 + 子集总和（子集无数据则加0）
                    Integer parentIssueCount = parent.getIsProductIssue() == 1 ? 1 : 0; // 当前父级isProductIssue为1时，值为1，否则为0
                    Integer childTotalIssueCount = parentChildIssueCountMap.getOrDefault(parent.getPartNumber(), 0);
                    vo.setIssueCount(parentIssueCount + childTotalIssueCount);
                    // 补充最新报告日期（可选：若需显示子集最新日期，可在此扩展）
                    return vo;
                })
                .collect(Collectors.toList());

        // 封装分页结果（复用父级分页的总条数，子集不占分页名额）
        return new PageResult<>(voList, parentPage.getTotal());
    }

    @Override
    public PageResult<FlightReliableDataMgrRespVO> getDataPageForMgrBySql(ReliabilityDataPageReqVO reqVO) {
        Long tenantId = TenantContextHolder.getTenantId();
        int pageNo = reqVO.getPageNo() == null ? 1 : reqVO.getPageNo();
        int pageSize = reqVO.getPageSize() == null ? 20 : reqVO.getPageSize();
        int offset = (pageNo - 1) * pageSize;
        List<FlightReliableDataMgrRespVO> list = new ArrayList<>();
        Long total = 0L;
        if (reqVO.getRemovalDate() == null) {
             list = dataMapper.selectDataPageForMgrBySql(
                    reqVO.getPartName(), reqVO.getPartNumber(), tenantId, offset, pageSize, null, null);
            total = dataMapper.countDataPageForMgrBySql(reqVO.getPartName(), reqVO.getPartNumber(), tenantId , null, null);
        }else {
            list = dataMapper.selectDataPageForMgrBySql(
                    reqVO.getPartName(), reqVO.getPartNumber(), tenantId, offset, pageSize, reqVO.getRemovalDate()[0], reqVO.getRemovalDate()[1]);
            total = dataMapper.countDataPageForMgrBySql(reqVO.getPartName(), reqVO.getPartNumber(), tenantId, reqVO.getRemovalDate()[0], reqVO.getRemovalDate()[1]);
        }

        return new PageResult<>(list, total == null ? 0L : total);
    }

    @Override
    public List<ReliabilityTrendRespVO> statReliabilityTrend() {
        int currentYear = Year.now().getValue(); // 获取当前年份
        // Long tenantId = TenantContextHolder.getTenantId();

/*        // 查询当年所有符合条件的数据（Level=1 + Level=2 + isProductIssue=1 + reportStartDate在当年）
        ReliabilityDataPageReqVO parentReq = new ReliabilityDataPageReqVO();
        // 核心修改1：Level 同时包含 1 和 2（原仅 Level=1）
        parentReq.setLevelList(Arrays.asList(1, 2)); // 假设 ReqVO 支持 List 类型的 Level 参数（若不支持需扩展 ReqVO）
        parentReq.setIsProductIssue(1); // 仅统计产品问题数据
        parentReq.setApprovalStatuslList(Arrays.asList(0, 1));
        // 修复日期查询参数，使用范围查询（原仅设置开始日期，未设置结束日期）
        parentReq.setReportStartDate(new LocalDateTime[]{
                LocalDateTime.of(currentYear, 1, 1, 0, 0), // 开始日期：当年1月1日
                LocalDateTime.of(currentYear, 12, 31, 23, 59) // 结束日期：当年12月31日
        });
        // 调用 Mapper 查询（包含 Level=1 和 Level=2 的所有数据）
        PageResult<ReliabilityDataDO> allDataPage = dataMapper.selectPage(parentReq, true);
        List<ReliabilityDataDO> allDataList = allDataPage.getList();*/

        // 初始化 pageReqVO 并设置分页参数（如 pageSize 和 pageNo）
        ReliabilityDataPageReqVO pageReqVO = new ReliabilityDataPageReqVO();
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE); // 设置无限制分页
        pageReqVO.setPageNo(1); // 默认第一页
        // 构建查询条件：Level ∈ {1, 2} + isProductIssue = 1 + reportStartDate 在当年范围内
        LambdaQueryWrapperX<ReliabilityDataDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(ReliabilityDataDO::getIsProductIssue, 1)
                .in(ReliabilityDataDO::getApprovalStatus, Arrays.asList(0, 1))
                .between(ReliabilityDataDO::getReportStartDate,
                        LocalDateTime.of(currentYear, 1, 1, 0, 0),
                        LocalDateTime.of(currentYear, 12, 31, 23, 59));
        // 执行查询（直接传 wrapper，不依赖 ReqVO 自动解析）-执行查询：直接返回 PageResult
        PageResult<ReliabilityDataDO> allDataPage = dataMapper.selectPage(pageReqVO, wrapper);
        List<ReliabilityDataDO> allDataList = allDataPage.getList();
        if (CollectionUtils.isEmpty(allDataList)) {
            return buildEmptyTrendData(); // 无数据时返回1-12月空数据
        }

        // 统计所有数据的月度故障次数（Level=1 和 Level=2 统一按月份汇总）
        Map<Integer, Integer> monthlyIssueCountMap = allDataList.stream()
                .filter(Objects::nonNull)
                .filter(data -> data.getPartNumber() != null)
                .filter(data -> data.getSruNumber() != null)
                .filter(data -> data.getReportStartDate() != null) // 过滤无报告日期的数据
                .filter(data -> data.getIsProductIssue() != null && data.getIsProductIssue() == 1) // 再次确认产品问题（双重防护）
                .collect(Collectors.groupingBy(
                        data -> data.getReportStartDate().getMonthValue(), // 按月份分组（1-12）
                        Collectors.summingInt(data -> 1) // 每条符合条件的记录计1次故障
                ));

        // 构建完整的月度数据（1-12月，无故障则为0）
        Map<Integer, Integer> completeMonthlyMap = new TreeMap<>(); // TreeMap 自动按月份排序
        for (int month = 1; month <= 12; month++) {
            completeMonthlyMap.put(month, monthlyIssueCountMap.getOrDefault(month, 0));
        }

        // 转换为返回VO列表（按月份升序排列）
        return completeMonthlyMap.entrySet().stream()
                .map(entry -> new ReliabilityTrendRespVO(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * 构建空趋势数据（1-12月故障次数均为0）
     */
    private List<ReliabilityTrendRespVO> buildEmptyTrendData() {
        List<ReliabilityTrendRespVO> emptyList = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            emptyList.add(new ReliabilityTrendRespVO(month, 0));
        }
        return emptyList;
    }

    // MTBF导出
    @Override
    public List<MtbfRespVO> getMtbfDataExport(ReliabilityDataPageReqVO pageReqVO) {
        List<MtbfRespVO> result = new ArrayList<>();
        Long tenantId = TenantContextHolder.getTenantId();
        //查询条件根据件号、租户id、是产品问题
        List<ReliabilityDataDO> res = dataMapper.selectByPartNumber(tenantId, pageReqVO.getPartNumber(), 1);
        if (CollectionUtils.isEmpty(res)) {
            throw new RuntimeException("未查询到部件数据：partNumber=" + pageReqVO.getPartNumber());
        }
        //只要一条
        ReliabilityDataDO reliabilityDataDO = res.get(0);
        //计算机队总飞行时间    当前时间减去交付时间乘以8
        LocalDateTime currentDate = LocalDateTime.now();
        long daysBetween = ChronoUnit.DAYS.between(reliabilityDataDO.getInstallationDate().toLocalDate(), currentDate.toLocalDate());
        // 每天按8小时计算   总飞行小时
        BigDecimal totalFlightTime = BigDecimal.valueOf(daysBetween * 8);
        //获取部件每架装机数   通过partNumber
        ReliabilityDataMetricsDO ReliabilityDataMetricsDO = reliabilityDataMetricsService.getReliabilityDataMetricsByPatNumber(pageReqVO.getPartNumber(), tenantId);
        if (ReliabilityDataMetricsDO == null) {
            ReliabilityDataMetricsDO = new ReliabilityDataMetricsDO();
            ReliabilityDataMetricsDO.setCount(0);
            ReliabilityDataMetricsDO.setMtbf(1);
        }
        MtbfRespVO respVO = new MtbfRespVO();
        respVO.setPartNo(reliabilityDataDO.getPartNumber());
        respVO.setEquipmentName(reliabilityDataDO.getPartName());
        respVO.setTotalFlightTime(totalFlightTime);
        respVO.setTotalInstallCount(ReliabilityDataMetricsDO.getCount());
        respVO.setFailureCount(res.size());
        // 计算MTBF实际值：(总飞行时间 * 部件每架装机数) / 故障次数
        BigDecimal mtbfActual = totalFlightTime.multiply(BigDecimal.valueOf(ReliabilityDataMetricsDO.getCount()))
                        .divide(BigDecimal.valueOf(res.size()), 2, BigDecimal.ROUND_HALF_UP);
        respVO.setMtbfActual(mtbfActual);
        respVO.setMtbfTarget(new BigDecimal(ReliabilityDataMetricsDO.getMtbf()));
        result.add(respVO);
//=========================================== yuanwen的代码=========================================

//        PageResult<FlightReliableDataMgrRespVO> mgrPageResult = getDataPageForMgr(pageReqVO);
//        if (CollectionUtils.isEmpty(mgrPageResult.getList())) {
//            throw new RuntimeException("未查询到部件数据：partName=" + pageReqVO.getPartName() + ", partNumber=" + pageReqVO.getPartNumber());
//        }
//        // 确保仅取一条数据（getDataPageForMgr已按partNumber精确查询，理论上仅一条）
//        FlightReliableDataMgrRespVO mgrVO = mgrPageResult.getList().get(0);
//
//        // 计算飞行时间和装机总数（核心修改：按当前部件查询ProductDO的installationDate）
//        Map<String, Object> metrics = calculateFlightTimeAndInstallCount(pageReqVO.getSerialNumber(), pageReqVO.getPartNumber());
//        BigDecimal totalFlightTime = (BigDecimal) metrics.get("totalFlightTime");
//        Integer totalInstallCount = (Integer) metrics.get("totalInstallCount");
//
//        // 故障次数为getDataPageForMgr返回的issueCount
//        Integer failureCount = mgrVO.getIssueCount() == null ? 0 : mgrVO.getIssueCount();
//
//        // 封装MTBF响应VO（单条数据）
//        MtbfRespVO respVO = new MtbfRespVO();
////        respVO.setId(mgrVO.getId());
//        respVO.setPartNo(mgrVO.getPartNumber());
//        respVO.setEquipmentName(mgrVO.getPartName());
//        respVO.setTotalFlightTime(totalFlightTime);
//        respVO.setTotalInstallCount(totalInstallCount);
//        respVO.setFailureCount(failureCount);
//        // 计算MTBF实际值：(总飞行时间 * 总装机数) / 故障次数，无故障则为0
//        BigDecimal mtbfActual = failureCount > 0 ?
//                totalFlightTime.multiply(BigDecimal.valueOf(totalInstallCount))
//                        .divide(BigDecimal.valueOf(failureCount), 2, BigDecimal.ROUND_HALF_UP) :
//                BigDecimal.ZERO;
//        respVO.setMtbfActual(mtbfActual);
//        respVO.setMtbfTarget(BigDecimal.valueOf(1)); // 指标值逻辑默认1，后续需修改
//
//        result.add(respVO);
        return result;
    }

    // MTBUR导出
    @Override
    public List<MtburRespVO> getMtburDataExport(ReliabilityDataPageReqVO pageReqVO) {
        List<MtburRespVO> result = new ArrayList<>();
        Long tenantId = TenantContextHolder.getTenantId();
        //查询条件根据件号、租户id、是否被删除等条件去查询数据
        List<ReliabilityDataDO> res = dataMapper.selectByPartNumberNoIsProductIssue(tenantId, pageReqVO.getPartNumber());
        if (CollectionUtils.isEmpty(res)) {
            throw new RuntimeException("未查询到部件数据：partNumber=" + pageReqVO.getPartNumber());
        }
        //只要一条
        ReliabilityDataDO reliabilityDataDO = res.get(0);
        //计算机队总飞行时间    当前时间减去交付时间乘以8
        LocalDateTime currentDate = LocalDateTime.now();
        long daysBetween = ChronoUnit.DAYS.between(reliabilityDataDO.getInstallationDate().toLocalDate(), currentDate.toLocalDate());
        // 每天按8小时计算   总飞行小时
        BigDecimal totalFlightTime = BigDecimal.valueOf(daysBetween * 8);
        //获取部件每架装机数   通过partNumber
        ReliabilityDataMetricsDO ReliabilityDataMetricsDO = reliabilityDataMetricsService.getReliabilityDataMetricsByPatNumber(pageReqVO.getPartNumber(), tenantId);
        if (ReliabilityDataMetricsDO == null) {
            ReliabilityDataMetricsDO = new ReliabilityDataMetricsDO();
            ReliabilityDataMetricsDO.setCount(0);
//            ReliabilityDataMetricsDO.setMtbf(1);
            ReliabilityDataMetricsDO.setMtbur(1);
        }
         //封装MTBUR响应VO（单条数据）
        MtburRespVO respVO = new MtburRespVO();
        respVO.setPartNo(reliabilityDataDO.getPartNumber());
        respVO.setEquipmentName(reliabilityDataDO.getPartName());
        respVO.setTotalFlightTime(totalFlightTime);
        respVO.setTotalInstallCount(ReliabilityDataMetricsDO.getCount());
        // 部件非计划拆换次数：removalType = U（计划拆换 S 不计入）
        long unplannedReplaceCount = res.stream()
                .filter(d -> "U".equals(d.getRemovalType()))
                .count();
        respVO.setUnplannedReplaceCount((int) unplannedReplaceCount);
        // 计算MTBUR实际值：(总飞行时间 * 部件每架装机数) / 部件非计划拆换次数，无非计划拆换则为0
        BigDecimal mtburActual = unplannedReplaceCount > 0
                ? totalFlightTime.multiply(BigDecimal.valueOf(ReliabilityDataMetricsDO.getCount()))
                        .divide(BigDecimal.valueOf(unplannedReplaceCount), 2, BigDecimal.ROUND_HALF_UP)
                : BigDecimal.ZERO;
        respVO.setMtburActual(mtburActual);
        respVO.setMtburTarget(BigDecimal.valueOf(ReliabilityDataMetricsDO.getMtbur()));

        result.add(respVO);


        //===========================================yuanwen的代码====================================================
        // 调用getDataPageForMgr查询单条部件数据
//        PageResult<FlightReliableDataMgrRespVO> mgrPageResult = getDataPageForMgr(pageReqVO);
//        if (CollectionUtils.isEmpty(mgrPageResult.getList())) {
//            throw new RuntimeException("未查询到部件数据：partName=" + pageReqVO.getPartName() + ", partNumber=" + pageReqVO.getPartNumber());
//        }
//        FlightReliableDataMgrRespVO mgrVO = mgrPageResult.getList().get(0);
//
//        // 计算飞行时间和装机总数（同MTBF逻辑，按部件查询装机时间）
//        Map<String, Object> metrics = calculateFlightTimeAndInstallCount(pageReqVO.getSerialNumber(), pageReqVO.getPartNumber());
//        BigDecimal totalFlightTime = (BigDecimal) metrics.get("totalFlightTime");
//        Integer totalInstallCount = (Integer) metrics.get("totalInstallCount");
//
//        // 非计划拆换次数：查询当前部件的removalType=U的记录数（包括父级和子集）
//        Integer unplannedReplaceCount = Math.toIntExact(dataMapper.selectCount(
//                new LambdaQueryWrapper<ReliabilityDataDO>()
//                        .eq(ReliabilityDataDO::getPartNumber, mgrVO.getPartNumber())
//                        .eq(ReliabilityDataDO::getLevel, 1)
//                        .eq(ReliabilityDataDO::getRemovalType, "U")
//        ));
//
//        // 查询子集的非计划拆换次数：根据PartNumber查询sruNumber值为PartNumber，且Level值为2、RemovalType值为U
//        Integer childUnplannedReplaceCount = Math.toIntExact(dataMapper.selectCount(
//                new LambdaQueryWrapper<ReliabilityDataDO>()
//                        .eq(ReliabilityDataDO::getSruNumber, mgrVO.getPartNumber())
//                        .eq(ReliabilityDataDO::getLevel, 2)
//                        .eq(ReliabilityDataDO::getRemovalType, "U")
//        ));
//
//        // 总非计划拆换次数 = 父级 + 子集
//        unplannedReplaceCount = unplannedReplaceCount + childUnplannedReplaceCount;
//
//        // 封装MTBUR响应VO（单条数据）
//        MtburRespVO respVO = new MtburRespVO();
////        respVO.setId(mgrVO.getId());
//        respVO.setPartNo(mgrVO.getPartNumber());
//        respVO.setEquipmentName(mgrVO.getPartName());
//        respVO.setTotalFlightTime(totalFlightTime);
//        respVO.setTotalInstallCount(totalInstallCount);
//        respVO.setUnplannedReplaceCount(unplannedReplaceCount);
//        // 计算MTBUR实际值
//        BigDecimal mtburActual = unplannedReplaceCount > 0 ?
//                totalFlightTime.multiply(BigDecimal.valueOf(totalInstallCount))
//                        .divide(BigDecimal.valueOf(unplannedReplaceCount), 2, BigDecimal.ROUND_HALF_UP) :
//                BigDecimal.ZERO;
//        respVO.setMtburActual(mtburActual);
//        respVO.setMtburTarget(BigDecimal.valueOf(1)); // 指标值默认1，后续需需改
//
//        result.add(respVO);
        return result;
    }

    // 计算飞行时间和装机总数
    private Map<String, Object> calculateFlightTimeAndInstallCount(String serialNumber, String partNumber) {
        Map<String, Object> result = new HashMap<>();
        // 1. 获取当前部件的装机时间（ProductDO的installationDate）
        LocalDateTime installationDate = getProductInstallationDate(serialNumber, partNumber);
        if (installationDate == null) {
            throw new RuntimeException("未查询到部件装机时间：serialNumber=" + serialNumber);
        }
        // 2. 计算总飞行时间（当前日期 - 装机时间）* 8小时/天
        LocalDateTime currentDate = LocalDateTime.now();
        long daysBetween = ChronoUnit.DAYS.between(installationDate.toLocalDate(), currentDate.toLocalDate());
        BigDecimal totalFlightTime = BigDecimal.valueOf(daysBetween * 8); // 每天按8小时计算
        // 若未查询到飞行时间，默认0
        if (totalFlightTime == null) {
            totalFlightTime = BigDecimal.ZERO;
        }
        // 3. 计算总装机数（查询ProductDO中该部件的总数量）
        Integer totalInstallCount = 1;
        result.put("totalFlightTime", totalFlightTime);
        result.put("totalInstallCount", totalInstallCount);
        return result;
    }

    //查询装机时间
    public LocalDateTime getProductInstallationDate(String serialNumber, String partNumber) {
        // 按partName（模糊）和partNumber（精确）查询ProductDO的装机时间
        LambdaQueryWrapper<ReliabilityDataDO> wrapper = new LambdaQueryWrapper<ReliabilityDataDO>()
                .eq(ReliabilityDataDO::getSerialNumber, serialNumber)
                .eq(ReliabilityDataDO::getPartNumber, partNumber);
        List<ReliabilityDataDO> list = dataMapper.selectList(wrapper);
        return list.isEmpty() ? null : list.get(0).getInstallationDate();
    }

    @Override
    public List<Map<String,String>> getPartNameOptions() {
        Long tenantId = TenantContextHolder.getTenantId();
        List<ProductDO> products = productMapper.selectList(new QueryWrapper<ProductDO>()
                .eq("tenant_id", tenantId)
                .eq("deleted", 0)
                .isNotNull("name")
                .ne("name", ""));

        List<Map<String, String>> res = new ArrayList<>();
        Set<String> seenNames = new HashSet<>();
        for (ProductDO product : products) {
            String name = product.getName() == null ? null : product.getName().trim();
            if (name == null || name.isEmpty() || !seenNames.add(name)) {
                continue;
            }
            Map<String, String> map = new HashMap<>();
            map.put("partName", name);
            map.put("partNumber", product.getPartNumber());
            map.put("partNameAndPartNumber", name + ":" + product.getPartNumber());
            map.put("uuid", UUID.randomUUID().toString());
            res.add(map);
        }
        return res;
    }

    @Override
    public List<String> getSeriesOptionsByPartName(String partName, String serialNumber) {
        String effectivePartName = trimToNull(partName);
        if (effectivePartName == null) {
            return Collections.emptyList();
        }

        Long tenantId = TenantContextHolder.getTenantId();

        List<ProductDO> products = new ArrayList<>();
        if(StringUtils.isNotBlank(serialNumber)){
            products = productMapper.selectList(new QueryWrapper<ProductDO>()
                    .eq("tenant_id", tenantId)
                    .eq("deleted", 0)
                    .eq("name", effectivePartName)
                    .eq("sequence_number", serialNumber)
                    .isNotNull("sign_version")
                    .ne("sign_version", ""));
        }else{
            products = productMapper.selectList(new QueryWrapper<ProductDO>()
                    .eq("tenant_id", tenantId)
                    .eq("deleted", 0)
                    .eq("name", effectivePartName)
                    .isNotNull("sign_version")
                    .ne("sign_version", ""));
        }


        return products.stream()
                .map(ProductDO::getSignVersion)
                .filter(s -> s != null && !s.trim().isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getSerialNumberOptionsByPartName(String partName, String series) {
        String effectivePartName = trimToNull(partName);
        if (effectivePartName == null) {
            return Collections.emptyList();
        }

        Long tenantId = TenantContextHolder.getTenantId();
        List<ProductDO> products = new ArrayList<>();
        if (StringUtils.isNotBlank(series)) {
            products = productMapper.selectList(new QueryWrapper<ProductDO>()
                    .eq("tenant_id", tenantId)
                    .eq("deleted", 0)
                    .eq("name", effectivePartName)
                    .eq("sign_version", series)
                    .isNotNull("sequence_number")
                    .ne("sequence_number", ""));
        }else {
            products = productMapper.selectList(new QueryWrapper<ProductDO>()
                    .eq("tenant_id", tenantId)
                    .eq("deleted", 0)
                    .eq("name", effectivePartName)
                    .isNotNull("sequence_number")
                    .ne("sequence_number", ""));
        }



        return products.stream()
                .map(ProductDO::getSequenceNumber)
                .filter(s -> s != null && !s.trim().isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public ReliabilityDataPartCascadeFillRespVO getPartCodeAndDeliveryTime(String partName, String series, String serialNumber) {
        ReliabilityDataPartCascadeFillRespVO respVO = new ReliabilityDataPartCascadeFillRespVO();

        String effectivePartName = trimToNull(partName);
        String effectiveSeries = trimToNull(series);
        String effectiveSerialNumber = trimToNull(serialNumber);
        if (effectivePartName == null || effectiveSeries == null || effectiveSerialNumber == null) {
            return respVO;
        }

        Long tenantId = TenantContextHolder.getTenantId();
        ProductDO product = productMapper.selectOne(new QueryWrapper<ProductDO>()
                .eq("tenant_id", tenantId)
                .eq("deleted", 0)
                .eq("name", effectivePartName)
                .eq("sign_version", effectiveSeries)
                .eq("sequence_number", effectiveSerialNumber)
                .last("LIMIT 1"));

        if (product == null) {
            return respVO;
        }

        String deliveryTime = trimToNull(product.getDeliveryTime());
        respVO.setPartCode(trimToNull(product.getPartNumber()));
        respVO.setPartNumber(respVO.getPartCode()); // 兼容：部分前端可能期望字段名是 partNumber
        respVO.setDeliveryTime(deliveryTime);
        respVO.setInstallationDate(deliveryTime);
        return respVO;
    }

    @Override
    public List<Map<String, String>> findSRU(String partNumber, String version, String serialNumber) {
        Long tenantId = TenantContextHolder.getTenantId();
        List<Map<String, String>> result = new ArrayList<>();
        List<ProductFileDO> productFileDO = productFileService.findSRU(tenantId, partNumber, version, serialNumber);
        for (ProductFileDO fileDO : productFileDO) {
            Map<String, String> map = new HashMap<>();
            map.put("partNumber", fileDO.getPartNumber());
            map.put("name", fileDO.getName());
            result.add(map);
        }

        return result;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private LocalDateTime parseDeliveryTimeToLocalDateTime(String deliveryTime) {
        String value = trimToNull(deliveryTime);
        if (value == null) {
            return null;
        }

        // import 时 normalizeToMySqlDateTime 的格式：yyyy-MM-dd HH:mm:ss
        List<DateTimeFormatter> dateTimePatterns = Arrays.asList(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        );
        for (DateTimeFormatter formatter : dateTimePatterns) {
            try {
                return LocalDateTime.parse(value, formatter);
            } catch (Exception ignored) {
                // try next
            }
        }

        // 如果数据库里只有日期：yyyy-MM-dd
        try {
            return LocalDate.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay();
        } catch (Exception ignored) {
            // fall through
        }

        // 兜底：尝试原生解析（可能是带 T 的格式）
        try {
            return LocalDateTime.parse(value.replace('T', ' '));
        } catch (Exception ignored) {
            return null;
        }
    }

}
