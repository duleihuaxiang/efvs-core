package cn.iocoder.yudao.module.attribute.service.attribute;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.attribute.controller.admin.attribute.vo.*;
import cn.iocoder.yudao.module.attribute.dal.dataobject.attribute.AttributeDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.attribute.dal.mysql.attribute.AttributeMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.data.enums.ErrorCodeConstants.ATTRIBUTE_NOT_EXISTS;

/**
 * 产品属性 Service 实现类
 *
 * @author admin
 */
@Service
@Validated
public class AttributeServiceImpl implements AttributeService {

    @Resource
    private AttributeMapper attributeMapper;

    @Override
    public Integer createAttribute(AttributeSaveReqVO createReqVO) {
        // 插入
        AttributeDO attribute = BeanUtils.toBean(createReqVO, AttributeDO.class);
        attributeMapper.insert(attribute);

        // 返回
        return attribute.getId();
    }

    @Override
    public void updateAttribute(AttributeSaveReqVO updateReqVO) {
        // 产品属性以 productId 为唯一业务维度：前端的“更新”既可能修改已有属性，也可能是首次补充属性。
        // 因此这里采用按 productId 的 upsert 语义，而不是按属性主键强制校验存在。
        AttributeDO attributeDO = attributeMapper.selectByProductId(updateReqVO.getProductId());
        AttributeDO updateObj = BeanUtils.toBean(updateReqVO, AttributeDO.class);
        if (Objects.isNull(attributeDO)){
            attributeMapper.insert(updateObj);
        }else{
            // 更新
            attributeMapper.updateById(updateObj);
        }
    }

    @Override
    public void deleteAttribute(Integer id) {
        // 校验存在
        validateAttributeExists(id);
        // 删除
        attributeMapper.deleteById(id);
    }

    @Override
        public void deleteAttributeListByIds(List<Integer> ids) {
        // 删除
        attributeMapper.deleteByIds(ids);
        }

    @Override
    public AttributeDO getAttributeByProductId(Integer id) {
        return attributeMapper.selectByProductId(id);
    }


    private void validateAttributeExists(Integer id) {
        if (attributeMapper.selectById(id) == null) {
            throw exception(ATTRIBUTE_NOT_EXISTS);
        }
    }

    @Override
    public PageResult<AttributeDO> getAttributePage(AttributePageReqVO pageReqVO) {
        return attributeMapper.selectPage(pageReqVO);
    }

}
