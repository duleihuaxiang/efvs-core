package cn.iocoder.yudao.module.data.dal.mysql.purchaseproductinfo;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.data.dal.dataobject.purchaseproductinfo.PurchaseProductInfoDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.data.controller.admin.purchaseproductinfo.vo.*;

/**
 * 购买产品信息 Mapper
 *
 * @author admin
 */
@Mapper
public interface PurchaseProductInfoMapper extends BaseMapperX<PurchaseProductInfoDO> {

    default PageResult<PurchaseProductInfoDO> selectPage(PurchaseProductInfoPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PurchaseProductInfoDO>()
                .likeIfPresent(PurchaseProductInfoDO::getProductName, reqVO.getProductName())
                .orderByDesc(PurchaseProductInfoDO::getId));
    }

}