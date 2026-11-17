package cn.iocoder.yudao.module.data.service.purchaseproductinfo;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.data.controller.admin.purchaseproductinfo.vo.*;
import cn.iocoder.yudao.module.data.dal.dataobject.purchaseproductinfo.PurchaseProductInfoDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 购买产品信息 Service 接口
 *
 * @author admin
 */
public interface PurchaseProductInfoService {

    /**
     * 创建购买产品信息
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Integer createPurchaseProductInfo(@Valid PurchaseProductInfoSaveReqVO createReqVO);

    /**
     * 更新购买产品信息
     *
     * @param updateReqVO 更新信息
     */
    void updatePurchaseProductInfo(@Valid PurchaseProductInfoSaveReqVO updateReqVO);

    /**
     * 删除购买产品信息
     *
     * @param id 编号
     */
    void deletePurchaseProductInfo(Integer id);

    /**
    * 批量删除购买产品信息
    *
    * @param ids 编号
    */
    void deletePurchaseProductInfoListByIds(List<Integer> ids);

    /**
     * 获得购买产品信息
     *
     * @param id 编号
     * @return 购买产品信息
     */
    PurchaseProductInfoDO getPurchaseProductInfo(Integer id);

    /**
     * 获得购买产品信息分页
     *
     * @param pageReqVO 分页查询
     * @return 购买产品信息分页
     */
    PageResult<PurchaseProductInfoDO> getPurchaseProductInfoPage(PurchaseProductInfoPageReqVO pageReqVO);

}