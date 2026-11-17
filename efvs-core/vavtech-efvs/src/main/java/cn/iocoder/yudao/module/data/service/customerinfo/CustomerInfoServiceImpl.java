package cn.iocoder.yudao.module.data.service.customerinfo;

import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.geo.dal.dataobject.area.AreaDO;
import cn.iocoder.yudao.module.geo.dal.mysql.area.AreaMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;

import cn.iocoder.yudao.module.data.controller.admin.customerinfo.vo.*;
import cn.iocoder.yudao.module.data.dal.dataobject.customerinfo.CustomerInfoDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.data.dal.mysql.customerinfo.CustomerInfoMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.data.enums.ErrorCodeConstants.*;

/**
 * 客户信息 Service 实现类
 *
 * @author admin
 */
@Service
@Validated
public class CustomerInfoServiceImpl implements CustomerInfoService {

    @Resource
    private CustomerInfoMapper customerInfoMapper;
    @Resource
    private AreaMapper areaMapper;

    @Override
    public Integer createCustomerInfo(CustomerInfoSaveReqVO createReqVO) {
        // 校验客户编号是否已存在
        Long tenantId = TenantContextHolder.getTenantId();
        validateCustomerIdUnique(createReqVO.getCustomerId(), tenantId);
        // 插入
        CustomerInfoDO customerInfo = BeanUtils.toBean(createReqVO, CustomerInfoDO.class);
        customerInfoMapper.insert(customerInfo);
        return customerInfo.getId();
    }

    private void validateCustomerIdUnique(String customerId, Long tenantId) {
        if (customerId != null && customerInfoMapper.selectByCustomerId(customerId, tenantId) != null) {
            throw exception(CUSTOMER_ID_EXISTS);
        }
    }

    @Override
    public void updateCustomerInfo(CustomerInfoSaveReqVO updateReqVO) {
        // 校验存在
        validateCustomerInfoExists(updateReqVO.getId());
        // 更新
        CustomerInfoDO updateObj = BeanUtils.toBean(updateReqVO, CustomerInfoDO.class);
        customerInfoMapper.updateById(updateObj);
    }

    @Override
    public void deleteCustomerInfoById(Integer id) {
        // 校验存在
        validateCustomerInfoExists(id);
        // 删除
        customerInfoMapper.deleteById(id);
    }

    @Override
    public void deleteCustomerInfoByIds(List<Integer> ids) {
        // 删除
        customerInfoMapper.deleteByIds(ids);
    }


    private void validateCustomerInfoExists(Integer id) {
        if (customerInfoMapper.selectById(id) == null) {
            throw exception(CUSTOMER_INFO_NOT_EXISTS);
        }
    }

    @Override
    public CustomerInfoDO getCustomerInfoById(Integer id) {
        return customerInfoMapper.selectById(id);
    }

    @Override
    public PageResult<CustomerInfoDO> getCustomerInfoPage(CustomerInfoPageReqVO pageReqVO) {
        return customerInfoMapper.selectPage(pageReqVO);
    }

    @Override
    public List<CustomerInfoDO> getCustomerInfoAll() {
        Long tenantId = TenantContextHolder.getTenantId();
        return customerInfoMapper.getCustomerInfoAll(tenantId);
    }

    @Override
    public List<ProvinceCustomerCountVO> statProvinceCustomerCount() {
        // 查询所有地区数据，构建层级映射
        List<AreaDO> allAreaList = areaMapper.selectList(null);
        if (CollectionUtils.isEmpty(allAreaList)) {
            return Collections.emptyList();
        }
        // ID→AreaDO 映射（防重复ID）
//        Map<Integer, AreaDO> areaIdMap = allAreaList.stream()
//                .collect(Collectors.toMap(AreaDO::getId, area -> area, (k1, k2) -> k1));
        // 省ID→下属市ID列表（type=2=省，type=3=市）
        Map<Integer, List<Integer>> province2CityIdsMap = allAreaList.stream()
                .filter(area -> Objects.equals(area.getType(), 2))
                .collect(Collectors.toMap(
                        AreaDO::getId,
                        province -> allAreaList.stream()
                                .filter(city -> Objects.equals(city.getType(), 3) && Objects.equals(city.getParentId(), province.getId()))
                                .map(AreaDO::getId)
                                .collect(Collectors.toList())
                ));
        // 市ID→下属区ID列表（type=3=市，type=4=区/县）
        Map<Integer, List<Integer>> city2DistrictIdsMap = allAreaList.stream()
                .filter(area -> Objects.equals(area.getType(), 3))
                .collect(Collectors.toMap(
                        AreaDO::getId,
                        city -> allAreaList.stream()
                                .filter(district -> Objects.equals(district.getType(), 4) && Objects.equals(district.getParentId(), city.getId()))
                                .map(AreaDO::getId)
                                .collect(Collectors.toList())
                ));

        // 查询所有客户数据，按省份ID分组（提前聚合，提升效率）
        List<CustomerInfoDO> allCustomerList = customerInfoMapper.selectList(null);
        Map<Integer, List<CustomerInfoDO>> provinceId2CustomersMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(allCustomerList)) {
            provinceId2CustomersMap = allCustomerList.stream()
                    .filter(customer -> customer.getProvinceId() != null) // 过滤无省份的脏数据
                    .collect(Collectors.groupingBy(CustomerInfoDO::getProvinceId));
        }

        // 查询【全国所有省份】（核心：保证省份不缺失）
        List<AreaDO> allProvinceList = allAreaList.stream()
                .filter(area -> Objects.equals(area.getType(), 2))
                .sorted(Comparator.comparing(AreaDO::getId)) // 按省份ID排序，返回结果有序
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(allProvinceList)) {
            return Collections.emptyList();
        }

        // 遍历全量省份，构建VO结果（有数据赋值，无数据置0）
        List<ProvinceCustomerCountVO> resultList = new ArrayList<>();
        for (AreaDO province : allProvinceList) {
            Integer provinceId = province.getId();
            String provinceName = province.getName();
            // 获取当前省份的客户数据（无数据则为空列表）
            List<CustomerInfoDO> provinceCustomerList = provinceId2CustomersMap.getOrDefault(provinceId, Collections.emptyList());

            // 初始化VO，基础信息赋值
            ProvinceCustomerCountVO vo = new ProvinceCustomerCountVO();
            vo.setProvinceId(provinceId);
            vo.setProvinceName(provinceName);
            // 省份客户总数：有数据则统计数量，无数据则0
            vo.setProvinceCustomerCount((long) provinceCustomerList.size());

            // 统计市维度客户数：有数据则分组统计，无数据则空Map
            Map<Integer, Long> cityCountMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(provinceCustomerList)) {
                cityCountMap = provinceCustomerList.stream()
                        .filter(customer -> customer.getCityId() != null)
                        // 校验市ID归属当前省份，过滤脏数据
                        .filter(customer -> province2CityIdsMap.getOrDefault(provinceId, Collections.emptyList()).contains(customer.getCityId()))
                        .collect(Collectors.groupingBy(CustomerInfoDO::getCityId, Collectors.counting()));
            }
            vo.setCityCustomerCountMap(cityCountMap);

            // 统计区维度客户数：有数据则分组统计，无数据则空Map
            Map<Integer, Long> districtCountMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(provinceCustomerList)) {
                districtCountMap = provinceCustomerList.stream()
                        .filter(customer -> customer.getDistrictId() != null)
                        // 校验区ID归属当前省份的市，过滤脏数据
                        .filter(customer -> {
                            Integer cityId = customer.getCityId();
                            return cityId != null && city2DistrictIdsMap.getOrDefault(cityId, Collections.emptyList()).contains(customer.getDistrictId());
                        })
                        .collect(Collectors.groupingBy(CustomerInfoDO::getDistrictId, Collectors.counting()));
            }
            vo.setDistrictCustomerCountMap(districtCountMap);

            resultList.add(vo);
        }

        return resultList;
    }

}