package cn.iocoder.yudao.module.data.dal.mysql.docdirectory;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.data.dal.dataobject.docdirectory.DocDirectoryDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.data.controller.admin.docdirectory.vo.*;

/**
 * 文档管理 Mapper
 *
 * @author admin
 */
@Mapper
public interface DocDirectoryMapper extends BaseMapperX<DocDirectoryDO> {
    // 查询所有目录（按新表名查询）
    List<DocDirectoryDO> selectAll();

    // 根据ID查询目录（用于新增时获取父级层级）
    DocDirectoryDO selectById(Integer id);

    // 新增目录
    void insertdocDirectory(DocDirectoryDO directory);

    List<DocDirectoryDO> selectChildren(Integer id);

    // 根据 ID 删除
    int deleteById(Integer id);
}