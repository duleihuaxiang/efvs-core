package cn.iocoder.yudao.module.data.dal.mysql.product;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.data.dal.dataobject.product.ProductDO;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.data.controller.admin.product.vo.*;
import org.apache.ibatis.annotations.Param;
import java.util.HashMap;

/**
 * 产品 Mapper
 *
 * @author admin
 */
@Mapper
public interface ProductMapper extends BaseMapperX<ProductDO> {

    default PageResult<ProductDO> selectPage(ProductPageReqVO reqVO) {

        PageResult<ProductDO> productDOPageResult = new PageResult<>();
        if (CollectionUtils.isEmpty(reqVO.getDeliveryTime())){
            productDOPageResult = selectPage(reqVO, new LambdaQueryWrapperX<ProductDO>()
                    .likeIfPresent(ProductDO::getName, reqVO.getName())
                    .likeIfPresent(ProductDO::getDeliveryDestination, reqVO.getDeliveryDestination())
                    .eqIfPresent(ProductDO::getBaseline, reqVO.getBaseline())
                    .eqIfPresent(ProductDO::getSignVersion, reqVO.getSignVersion())
                    .eqIfPresent(ProductDO::getPartNumber, reqVO.getPartNumber())
                    .eqIfPresent(ProductDO::getCustomerNumber, reqVO.getCustomerNumber())
                    .orderByDesc(ProductDO::getId));
        }else{
            productDOPageResult = selectPage(reqVO, new LambdaQueryWrapperX<ProductDO>()
                    .likeIfPresent(ProductDO::getName, reqVO.getName())
                    .likeIfPresent(ProductDO::getDeliveryDestination, reqVO.getDeliveryDestination())
                    .eqIfPresent(ProductDO::getBaseline, reqVO.getBaseline())
                    .eqIfPresent(ProductDO::getSignVersion, reqVO.getSignVersion())
                    .eqIfPresent(ProductDO::getPartNumber, reqVO.getPartNumber())
                    .eqIfPresent(ProductDO::getCustomerNumber, reqVO.getCustomerNumber())
                    .ge(ProductDO::getDeliveryTime, reqVO.getDeliveryTime().get(0))
                    .le(ProductDO::getDeliveryTime, reqVO.getDeliveryTime().get(1))
                    .orderByDesc(ProductDO::getId));
        }


        return productDOPageResult;
    }

    List<ProductDO> selectByPartNumber(@Param("partNumber") String partNumber,@Param("tenantId") Long tenantId, @Param("SerialNumber") String SerialNumber);

    List<ProductDO> selectProduct(@Param("partNumber") String partNumber, @Param("tenantId")Long tenantId);

    List<ProductDO> getProductFile(@Param("partNumber") String partNumber, @Param("tenantId") Long tenantId);

    List<ProductDO> selectListByTenant(@Param("tenantId") Long tenantId);

    List<HashMap<Object, Object>> selectUsers(@Param("userIds") List<Integer> userIds);

}
