package cn.iocoder.yudao.module.data.service.productfile;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.data.controller.admin.productfile.vo.*;
import cn.iocoder.yudao.module.data.dal.dataobject.productfile.ProductFileDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 档案 Service 接口
 *
 * @author admin
 */
public interface ProductFileService {

    /**
     * 创建档案
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Integer createProductFile(@Valid ProductFileSaveReqVO createReqVO);

    /**
     * 更新档案
     *
     * @param updateReqVO 更新信息
     */
    void updateProductFile(@Valid ProductFileSaveReqVO updateReqVO);

    /**
     * 删除档案
     *
     * @param id 编号
     */
    void deleteProductFile(Integer id);

    /**
    * 批量删除档案
    *
    * @param ids 编号
    */
    void deleteProductFileListByIds(List<Integer> ids);

    /**
     * 获得档案
     *
     * @param id 编号
     * @return 档案
     */
    ProductFileDO getProductFile(Integer id);

    /**
     * 获得档案分页
     *
     * @param pageReqVO 分页查询
     * @return 档案分页
     */
    PageResult<ProductFileDO> getProductFilePage(ProductFilePageReqVO pageReqVO);

    void updateByPartNameAndSequenceNumber(String partNumber, String sequenceNumber, Long tenantId);

    List<ProductFileDO> findSRU(Long tenantId, String partNumber, String version, String serialNumber);

}