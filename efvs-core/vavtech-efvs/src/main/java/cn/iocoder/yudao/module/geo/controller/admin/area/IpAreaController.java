package cn.iocoder.yudao.module.geo.controller.admin.area;

import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.*;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import cn.iocoder.yudao.module.geo.controller.admin.area.vo.*;
import cn.iocoder.yudao.module.geo.dal.dataobject.area.AreaDO;
import cn.iocoder.yudao.module.geo.service.area.AreaService;

@RestController
@RequestMapping("/geo")
@Validated
public class IpAreaController {
    @Resource
    private AreaService areaService;

    @GetMapping
    @PreAuthorize("@ss.hasPermission('geo:area:query')")
    public CommonResult<Map<String, List<AreaRespVO>>> getAreaCascaderByParentId(
            @RequestParam(value = "parentId", required = false) Integer parentId) {
        Map<String, List<AreaRespVO>> result = new HashMap<>();
        if (parentId == null) {
            // 如果没有传parentId，默认查询中国下的省份（parentId=1）
            parentId = AreaDO.PARENT_ID_ROOT;
        }
        // 查询指定父级下的所有子地区
        List<AreaDO> areas = areaService.getAreaListByParentId(parentId);
        result.put("areas", BeanUtils.toBean(areas, AreaRespVO.class));
        return success(result);
    }

}
