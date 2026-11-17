package cn.iocoder.yudao.module.geo.service.area;

import java.util.*;
import cn.iocoder.yudao.module.geo.dal.dataobject.area.AreaDO;

/**
 * 地区 Service 接口
 *
 * @author admin
 */
public interface AreaService {

    /**
     * 根据父级ID获取子地区列表
     *
     * @param parentId 父级ID
     * @return 子地区列表
     */
    List<AreaDO> getAreaListByParentId(Integer parentId);

}