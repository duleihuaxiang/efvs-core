package cn.iocoder.yudao.module.data.service.documentmanagement;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.data.controller.admin.documentmanagement.vo.*;
import cn.iocoder.yudao.module.data.dal.dataobject.documentmanagement.DocumentManagementDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 文档管理 Service 接口
 *
 * @author admin
 */
public interface DocumentManagementService {

    /**
     * 创建文档管理
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Integer createDocumentManagement(@Valid DocumentManagementSaveReqVO createReqVO);

    /**
     * 更新文档管理
     *
     * @param updateReqVO 更新信息
     */
    void updateDocumentManagement(@Valid DocumentManagementSaveReqVO updateReqVO);

    /**
     * 删除文档管理
     *
     * @param id 编号
     */
    void deleteDocumentManagement(Integer id);

    /**
    * 批量删除文档管理
    *
    * @param ids 编号
    */
    void deleteDocumentManagementListByIds(List<Integer> ids);

    /**
     * 获得文档管理
     *
     * @param id 编号
     * @return 文档管理
     */
    DocumentManagementDO getDocumentManagement(Integer id);

    /**
     * 获得文档管理分页
     *
     * @param pageReqVO 分页查询
     * @return 文档管理分页
     */
    PageResult<DocumentManagementDO> getDocumentManagementPage(DocumentManagementPageReqVO pageReqVO);

}