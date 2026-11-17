package cn.iocoder.yudao.module.geo.dal.mysql.area;

import java.util.*;

import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.geo.dal.dataobject.area.AreaDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 地区 Mapper
 *
 * @author admin
 */
@Mapper
public interface AreaMapper extends BaseMapperX<AreaDO> {

    /**
     * 根据父级ID查询子地区
     */
    default List<AreaDO> selectByParentIdWithOrder(Integer parentId) {
        return selectList(new LambdaQueryWrapperX<AreaDO>()
                .eq(AreaDO::getParentId, parentId)
                .orderByAsc(AreaDO::getId));
    }

}