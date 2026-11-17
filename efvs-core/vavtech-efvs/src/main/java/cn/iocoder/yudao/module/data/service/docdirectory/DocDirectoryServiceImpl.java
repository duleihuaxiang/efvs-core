package cn.iocoder.yudao.module.data.service.docdirectory;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.data.controller.admin.docdirectory.vo.DocDirectorySaveReqVO;
import cn.iocoder.yudao.module.data.dal.dataobject.documentmanagement.DocumentManagementDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.*;
import cn.iocoder.yudao.module.data.dal.dataobject.docdirectory.DocDirectoryDO;

import cn.iocoder.yudao.module.data.dal.mysql.docdirectory.DocDirectoryMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.data.enums.ErrorCodeConstants.DOCUMENT_MANAGEMENT_NOT_EXISTS;

/**
 * 文档管理 Service 实现类
 *
 * @author admin
 */
@Service
@Validated
public class DocDirectoryServiceImpl implements DocDirectoryService {


    @Autowired
    private DocDirectoryMapper directoryMapper;


    /**
     * 获取完整目录树（逻辑不变，适配新表查询结果）
     */
    @Override
    public List<DocDirectoryDO> getDirectoryTree() {
        //获取父节点
        List<DocDirectoryDO> directories = directoryMapper.selectChildren(0);
        for (DocDirectoryDO directory : directories) {
            assembleChildren(directory);
        }

        return directories;
    }

    // 递归组装子类目
    private void assembleChildren(DocDirectoryDO parentDir) {
        // 查询当前父类目的子类目
        List<DocDirectoryDO> children = directoryMapper.selectChildren(parentDir.getId());
        if (!children.isEmpty()) {
            parentDir.setChildren(children);
            // 递归处理每个子类目（子类目可能还有子类目）
            for (DocDirectoryDO child : children) {
                assembleChildren(child);
            }
        }
    }

    /**
     * 新增目录（补充新字段处理）
     * @param name 目录名称
     * @param parentId 父级ID
     * @param remark 备注（可选）
     */
    @Override
    public void addDirectory(DocDirectorySaveReqVO saveReqVO) {
        DocDirectoryDO dir = new DocDirectoryDO();
        dir.setName(saveReqVO.getName());
        dir.setParentId(saveReqVO.getParentId());
        dir.setRemark(saveReqVO.getRemark());
        dir.setLevel(saveReqVO.getLevel());
        dir.setCreator(saveReqVO.getCreator());
        dir.setCreateTime(LocalDateTime.now());
        dir.setUpdateTime(LocalDateTime.now());
        dir.setUpdater(saveReqVO.getUpdater());


//        // 计算层级
//        if (parentId == 0) {
//            dir.setLevel(1);
//        } else {
//            DocDirectoryDO parent = directoryMapper.selectById(parentId);
//            if (parent == null) {
//                throw new RuntimeException("父级目录不存在");
//            }
//            dir.setLevel(parent.getLevel() + 1);
//        }

        directoryMapper.insertdocDirectory(dir);
    }
    @Override
    public void deleteDirectory(Integer id) {
        // 1. 检查是否存在该目录
        DocDirectoryDO directory = directoryMapper.selectById(id);
        if (directory == null) {
            throw new RuntimeException("目录不存在");
        }

        // 2. 检查是否有子目录（递归删除）
        List<DocDirectoryDO> children = directoryMapper.selectChildren(id);
        if (!children.isEmpty()) {
            // 递归删除所有子目录
            for (DocDirectoryDO child : children) {
                deleteDirectory(child.getId());
            }
        }
        // 3. 删除当前目录
        directoryMapper.deleteById(id);
    }

    @Override
    public void updateDirectory(DocDirectorySaveReqVO updateReqVO) {
        // 校验存在
        validateDocumentManagementExists(updateReqVO.getId());
        // 更新
        DocDirectoryDO updateObj = BeanUtils.toBean(updateReqVO, DocDirectoryDO.class);
        directoryMapper.updateById(updateObj);
    }
    private void validateDocumentManagementExists(Integer id) {
        if (directoryMapper.selectById(id) == null) {
            throw exception(DOCUMENT_MANAGEMENT_NOT_EXISTS);
        }
    }
}