package cn.iocoder.yudao.module.data.service.productfile;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.data.controller.admin.productfile.vo.*;
import cn.iocoder.yudao.module.data.dal.dataobject.productfile.ProductFileDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.data.dal.mysql.productfile.ProductFileMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.diffList;
import static cn.iocoder.yudao.module.data.enums.ErrorCodeConstants.*;

/**
 * 档案 Service 实现类
 *
 * @author admin
 */
@Service
@Validated
public class ProductFileServiceImpl implements ProductFileService {

    @Resource
    private ProductFileMapper productFileMapper;

    @Override
    public Integer createProductFile(ProductFileSaveReqVO createReqVO) {
        // 插入
        ProductFileDO productFile = BeanUtils.toBean(createReqVO, ProductFileDO.class);
        productFileMapper.insert(productFile);

        // 返回
        return productFile.getId();
    }

    @Override
    public void updateProductFile(ProductFileSaveReqVO updateReqVO) {
        // 校验存在
        validateProductFileExists(updateReqVO.getId());
        // 更新
        ProductFileDO updateObj = BeanUtils.toBean(updateReqVO, ProductFileDO.class);
        productFileMapper.updateById(updateObj);
    }

    @Override
    public void deleteProductFile(Integer id) {
        // 校验存在
        validateProductFileExists(id);
        // 删除
        productFileMapper.deleteById(id);
    }

    @Override
        public void deleteProductFileListByIds(List<Integer> ids) {
        // 删除
        productFileMapper.deleteByIds(ids);
        }


    private void validateProductFileExists(Integer id) {
        if (productFileMapper.selectById(id) == null) {
            throw exception(PRODUCT_FILE_NOT_EXISTS);
        }
    }

    @Override
    public ProductFileDO getProductFile(Integer id) {
        return productFileMapper.selectById(id);
    }

    @Override
    public PageResult<ProductFileDO> getProductFilePage(ProductFilePageReqVO pageReqVO) {
        return productFileMapper.selectPage(pageReqVO);
    }

    @Override
    public void updateByPartNameAndSequenceNumber(String partNumber, String sequenceNumber, Long tenantId) {
        productFileMapper.updateByPartNameAndSequenceNumber(partNumber, sequenceNumber, tenantId);
    }

    @Override
    public List<ProductFileDO> findSRU(Long tenantId, String partNumber, String version, String serialNumber) {
        return productFileMapper.findSRU(tenantId, partNumber, version, serialNumber);
    }

}