package cn.iocoder.yudao.module.attribute.dal.mysql.attribute;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.attribute.controller.admin.attribute.vo.AttributePageReqVO;
import cn.iocoder.yudao.module.attribute.dal.dataobject.attribute.AttributeDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 产品属性 Mapper
 *
 * @author admin
 */
@Mapper
public interface AttributeMapper extends BaseMapperX<AttributeDO> {

    default PageResult<AttributeDO> selectPage(AttributePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AttributeDO>()
                .eqIfPresent(AttributeDO::getProductId, reqVO.getProductId())
                .orderByDesc(AttributeDO::getId));
    }

    AttributeDO selectByProductId(@Param("productId") Integer id);
}