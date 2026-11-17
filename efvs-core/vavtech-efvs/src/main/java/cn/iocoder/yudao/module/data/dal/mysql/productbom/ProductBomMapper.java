package cn.iocoder.yudao.module.data.dal.mysql.productbom;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.data.dal.dataobject.productbom.ProductBomDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.data.controller.admin.productbom.vo.*;
import org.apache.ibatis.annotations.Param;

/**
 * 产品BOM Mapper
 *
 * @author admin
 */
@Mapper
public interface ProductBomMapper extends BaseMapperX<ProductBomDO> {

    default PageResult<ProductBomDO> selectPage(ProductBomPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ProductBomDO>()
                .eqIfPresent(ProductBomDO::getProductType, reqVO.getProductType())
                .eqIfPresent(ProductBomDO::getLevel, 0)
                .eqIfPresent(ProductBomDO::getPartNumber, reqVO.getPartNumber())
                .likeIfPresent(ProductBomDO::getName, reqVO.getName())
                .orderByDesc(ProductBomDO::getId));
    }

    List<ProductBomDO> selectProduct(@Param("partNumber") String partNumber,@Param("tenantId") Long tenantId);

    List<ProductBomRespVO> selectBOMList(Long tenantId);

    List<ProductBomRespVO> selectBOMDetailList(@Param("tenantId") Long tenantId,
                                               @Param("productPartNumber") String productPartNumber,
                                               @Param("name") String name,
                                               @Param("partNumber") String partNumber,
                                               @Param("productVersion") String productVersion,
                                               @Param("productType") Integer productType);
}