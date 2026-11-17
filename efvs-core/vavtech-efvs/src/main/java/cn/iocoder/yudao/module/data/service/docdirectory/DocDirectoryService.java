package cn.iocoder.yudao.module.data.service.docdirectory;

import java.util.*;
import javax.validation.*;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.data.controller.admin.docdirectory.vo.*;
import cn.iocoder.yudao.module.data.dal.dataobject.docdirectory.DocDirectoryDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 文档管理 Service 接口
 *
 * @author admin
 */
public interface DocDirectoryService {
    // 获取完整的目录树形结构
    List<DocDirectoryDO> getDirectoryTree();

    // 新增目录（支持新增层级）
    void addDirectory(@Valid DocDirectorySaveReqVO saveReqVO);

    // 删除目录
    void deleteDirectory(Integer id);

    // 修改目录
    void updateDirectory(DocDirectorySaveReqVO saveReqVO);
}