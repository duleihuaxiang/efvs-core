package cn.iocoder.yudao.module.data.service.documentmanagement;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.data.controller.admin.documentmanagement.vo.*;
import cn.iocoder.yudao.module.data.dal.dataobject.documentmanagement.DocumentManagementDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.data.dal.mysql.documentmanagement.DocumentManagementMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.diffList;
import static cn.iocoder.yudao.module.data.enums.ErrorCodeConstants.*;

/**
 * 文档管理 Service 实现类
 *
 * @author admin
 */
@Service
@Validated
public class DocumentManagementServiceImpl implements DocumentManagementService {

    @Resource
    private DocumentManagementMapper documentManagementMapper;

    @Override
    public Integer createDocumentManagement(DocumentManagementSaveReqVO createReqVO) {
        // 插入
        DocumentManagementDO documentManagement = BeanUtils.toBean(createReqVO, DocumentManagementDO.class);
        documentManagementMapper.insert(documentManagement);

        // 返回
        return documentManagement.getId();
    }

    @Override
    public void updateDocumentManagement(DocumentManagementSaveReqVO updateReqVO) {
        // 校验存在
        validateDocumentManagementExists(updateReqVO.getId());
        // 更新
        DocumentManagementDO updateObj = BeanUtils.toBean(updateReqVO, DocumentManagementDO.class);
        documentManagementMapper.updateById(updateObj);
    }

    @Override
    public void deleteDocumentManagement(Integer id) {
        // 校验存在
        validateDocumentManagementExists(id);
        // 删除
        documentManagementMapper.deleteById(id);
    }

    @Override
        public void deleteDocumentManagementListByIds(List<Integer> ids) {
        // 删除
        documentManagementMapper.deleteByIds(ids);
        }


    private void validateDocumentManagementExists(Integer id) {
        if (documentManagementMapper.selectById(id) == null) {
            throw exception(DOCUMENT_MANAGEMENT_NOT_EXISTS);
        }
    }

    @Override
    public DocumentManagementDO getDocumentManagement(Integer id) {
        return documentManagementMapper.selectById(id);
    }

    @Override
    public PageResult<DocumentManagementDO> getDocumentManagementPage(DocumentManagementPageReqVO pageReqVO) {
        return documentManagementMapper.selectPage(pageReqVO);
    }

}