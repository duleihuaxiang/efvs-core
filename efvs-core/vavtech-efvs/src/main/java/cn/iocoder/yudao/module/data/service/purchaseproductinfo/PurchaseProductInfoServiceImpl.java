package cn.iocoder.yudao.module.data.service.purchaseproductinfo;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.data.dal.mysql.customerinfo.CustomerInfoMapper;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.data.controller.admin.purchaseproductinfo.vo.*;
import cn.iocoder.yudao.module.data.dal.dataobject.purchaseproductinfo.PurchaseProductInfoDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.data.dal.mysql.purchaseproductinfo.PurchaseProductInfoMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.diffList;
import static cn.iocoder.yudao.module.data.enums.ErrorCodeConstants.*;

/**
 * 购买产品信息 Service 实现类
 *
 * @author admin
 */
@Service
@Validated
public class PurchaseProductInfoServiceImpl implements PurchaseProductInfoService {

    @Resource
    private PurchaseProductInfoMapper purchaseProductInfoMapper;

    @Resource
    private CustomerInfoMapper customerInfoMapper;
    @Override
    public Integer createPurchaseProductInfo(PurchaseProductInfoSaveReqVO createReqVO) {
        // 校验客户信息是否存在
        validateCustomerInfoExists(createReqVO.getCustomerId());
        // 插入
        PurchaseProductInfoDO purchaseProductInfo = BeanUtils.toBean(createReqVO, PurchaseProductInfoDO.class);
        purchaseProductInfoMapper.insert(purchaseProductInfo);

        // 返回
        return purchaseProductInfo.getId();
    }

    @Override
    public void updatePurchaseProductInfo(PurchaseProductInfoSaveReqVO updateReqVO) {
        // 校验存在
        validateCustomerInfoExists(updateReqVO.getCustomerId());
        validatePurchaseProductInfoExists(updateReqVO.getId());
        // 更新
        PurchaseProductInfoDO updateObj = BeanUtils.toBean(updateReqVO, PurchaseProductInfoDO.class);
        purchaseProductInfoMapper.updateById(updateObj);
    }

    @Override
    public void deletePurchaseProductInfo(Integer id) {
        // 校验存在
        validatePurchaseProductInfoExists(id);
        // 删除
        purchaseProductInfoMapper.deleteById(id);
    }

    @Override
        public void deletePurchaseProductInfoListByIds(List<Integer> ids) {
        // 删除
        purchaseProductInfoMapper.deleteByIds(ids);
        }


    private void validatePurchaseProductInfoExists(Integer id) {
        if (purchaseProductInfoMapper.selectById(id) == null) {
            throw exception(PURCHASE_PRODUCT_INFO_NOT_EXISTS);
        }
    }

    @Override
    public PurchaseProductInfoDO getPurchaseProductInfo(Integer id) {
        return purchaseProductInfoMapper.selectById(id);
    }

    @Override
    public PageResult<PurchaseProductInfoDO> getPurchaseProductInfoPage(PurchaseProductInfoPageReqVO pageReqVO) {
        return purchaseProductInfoMapper.selectPage(pageReqVO);
    }

    private void validateCustomerInfoExists(String id) {
        if (customerInfoMapper.selectById(id) == null) {
            throw exception(CUSTOMER_INFO_NOT_EXISTS);
        }
    }

}