package cn.iocoder.yudao.module.data.service.customerinfo;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.data.controller.admin.customerinfo.vo.*;
import cn.iocoder.yudao.module.data.dal.dataobject.customerinfo.CustomerInfoDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 客户信息 Service 接口
 *
 * @author admin
 */
public interface CustomerInfoService {

    /**
     * 创建客户信息
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Integer createCustomerInfo(@Valid CustomerInfoSaveReqVO createReqVO);

    /**
     * 更新客户信息
     *
     * @param updateReqVO 更新信息
     */
    void updateCustomerInfo(@Valid CustomerInfoSaveReqVO updateReqVO);

    /**
     * 删除客户信息
     *
     * @param id 编号
     */
    void deleteCustomerInfoById(Integer id);

    /**
     * 批量删除客户信息
     *
     * @param ids 编号
     */
    void deleteCustomerInfoByIds(List<Integer> ids);

    /**
     * 获得客户信息
     *
     * @param id 编号
     * @return 客户信息
     */
    CustomerInfoDO getCustomerInfoById(Integer id);

    /**
     * 获得客户信息分页
     *
     * @param pageReqVO 分页查询
     * @return 客户信息分页
     */
    PageResult<CustomerInfoDO> getCustomerInfoPage(CustomerInfoPageReqVO pageReqVO);

    List<CustomerInfoDO> getCustomerInfoAll();

    /**
     * 统计每个省份包含的省/市/区客户个数（按ID维度：省ID→市ID→区ID）
     */
    List<ProvinceCustomerCountVO> statProvinceCustomerCount();

}