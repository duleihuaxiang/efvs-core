package cn.iocoder.yudao.module.attribute.service.attribute;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.attribute.controller.admin.attribute.vo.AttributePageReqVO;
import cn.iocoder.yudao.module.attribute.controller.admin.attribute.vo.AttributeSaveReqVO;
import cn.iocoder.yudao.module.attribute.dal.dataobject.attribute.AttributeDO;

/**
 * 产品属性 Service 接口
 *
 * @author admin
 */
public interface AttributeService {

    /**
     * 创建产品属性
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Integer createAttribute(@Valid AttributeSaveReqVO createReqVO);

    /**
     * 更新产品属性
     *
     * @param updateReqVO 更新信息
     */
    void updateAttribute(@Valid AttributeSaveReqVO updateReqVO);

    /**
     * 删除产品属性
     *
     * @param id 编号
     */
    void deleteAttribute(Integer id);

    /**
    * 批量删除产品属性
    *
    * @param ids 编号
    */
    void deleteAttributeListByIds(List<Integer> ids);

    /**
     * 获得产品属性
     *
     * @param id 编号
     * @return 产品属性
     */
    AttributeDO getAttributeByProductId(Integer id);

    /**
     * 获得产品属性分页
     *
     * @param pageReqVO 分页查询
     * @return 产品属性分页
     */
    PageResult<AttributeDO> getAttributePage(AttributePageReqVO pageReqVO);

}