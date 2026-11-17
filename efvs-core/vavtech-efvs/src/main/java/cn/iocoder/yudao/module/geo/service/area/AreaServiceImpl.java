package cn.iocoder.yudao.module.geo.service.area;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import java.util.*;
import cn.iocoder.yudao.module.geo.dal.dataobject.area.AreaDO;
import cn.iocoder.yudao.module.geo.dal.mysql.area.AreaMapper;


/**
 * 地区 Service 实现类
 *
 * @author admin
 */
@Service
@Validated
public class AreaServiceImpl implements AreaService {

    @Resource
    private AreaMapper areaMapper;

    @Override
    public List<AreaDO> getAreaListByParentId(Integer parentId) {
        return areaMapper.selectByParentIdWithOrder(parentId);
    }

}

