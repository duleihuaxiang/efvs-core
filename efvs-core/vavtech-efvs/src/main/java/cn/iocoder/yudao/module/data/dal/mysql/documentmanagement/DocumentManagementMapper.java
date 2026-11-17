package cn.iocoder.yudao.module.data.dal.mysql.documentmanagement;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.data.dal.dataobject.documentmanagement.DocumentManagementDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.data.controller.admin.documentmanagement.vo.*;

/**
 * 文档管理 Mapper
 *
 * @author admin
 */
@Mapper
public interface DocumentManagementMapper extends BaseMapperX<DocumentManagementDO> {

    default PageResult<DocumentManagementDO> selectPage(DocumentManagementPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<DocumentManagementDO>()
                .likeIfPresent(DocumentManagementDO::getName, reqVO.getName())
                .eqIfPresent(DocumentManagementDO::getParentId, reqVO.getParentId())
                .eqIfPresent(DocumentManagementDO::getNodeType, reqVO.getNodeType())
                .orderByDesc(DocumentManagementDO::getId));
    }

}