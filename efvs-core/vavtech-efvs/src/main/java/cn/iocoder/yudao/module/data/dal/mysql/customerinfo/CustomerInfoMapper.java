package cn.iocoder.yudao.module.data.dal.mysql.customerinfo;


import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.data.dal.dataobject.customerinfo.CustomerInfoDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.data.controller.admin.customerinfo.vo.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 客户信息 Mapper
 *
 * @author admin
 */
@Mapper
public interface CustomerInfoMapper extends BaseMapperX<CustomerInfoDO> {

    default PageResult<CustomerInfoDO> selectPage(CustomerInfoPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CustomerInfoDO>()
                .likeIfPresent(CustomerInfoDO::getCustomerName, reqVO.getCustomerName())
                .likeIfPresent(CustomerInfoDO::getCustomerId, reqVO.getCustomerId())
                .orderByDesc(CustomerInfoDO::getId));
    }

    List<CustomerInfoDO> getCustomerInfoAll(@Param("tenantId") Long tenantId);


    CustomerInfoDO selectByCustomerId(@Param("customerId") String customerId, @Param("tenantId") Long tenantId);

}