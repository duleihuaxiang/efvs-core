package cn.iocoder.yudao.module.attribute.controller.admin.attribute;

import cn.iocoder.yudao.module.attribute.controller.admin.attribute.vo.AttributePageReqVO;
import cn.iocoder.yudao.module.attribute.controller.admin.attribute.vo.AttributeRespVO;
import cn.iocoder.yudao.module.attribute.controller.admin.attribute.vo.AttributeSaveReqVO;
import cn.iocoder.yudao.module.attribute.dal.dataobject.attribute.AttributeDO;
import cn.iocoder.yudao.module.attribute.service.attribute.AttributeService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;

import javax.validation.constraints.*;
import javax.validation.*;
import javax.servlet.http.*;
import java.util.*;
import java.io.IOException;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;

@RestController
@RequestMapping("/efvs/attribute")
@Validated
public class AttributeController {

    @Resource
    private AttributeService attributeService;

    @PostMapping("/create")
    @PreAuthorize("@ss.hasPermission('efvs:attribute:create')")
    public CommonResult<Integer> createAttribute(@Valid @RequestBody AttributeSaveReqVO createReqVO) {
        return success(attributeService.createAttribute(createReqVO));
    }

    @PutMapping("/update")
    @PreAuthorize("@ss.hasPermission('efvs:attribute:update')")
    public CommonResult<Boolean> updateAttribute(@Valid @RequestBody AttributeSaveReqVO updateReqVO) {
        attributeService.updateAttribute(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @PreAuthorize("@ss.hasPermission('efvs:attribute:delete')")
    public CommonResult<Boolean> deleteAttribute(@RequestParam("id") Integer id) {
        attributeService.deleteAttribute(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
                @PreAuthorize("@ss.hasPermission('efvs:attribute:delete')")
    public CommonResult<Boolean> deleteAttributeList(@RequestParam("ids") List<Integer> ids) {
        attributeService.deleteAttributeListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('efvs:attribute:query')")
    public CommonResult<AttributeRespVO> getAttribute(@RequestParam("id") Integer id) {
        AttributeDO attribute = attributeService.getAttributeByProductId(id);
        return success(BeanUtils.toBean(attribute, AttributeRespVO.class));
    }

    @GetMapping("/page")
    @PreAuthorize("@ss.hasPermission('efvs:attribute:query')")
    public CommonResult<PageResult<AttributeRespVO>> getAttributePage(@Valid AttributePageReqVO pageReqVO) {
        PageResult<AttributeDO> pageResult = attributeService.getAttributePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, AttributeRespVO.class));
    }

    @GetMapping("/export-excel")
    @PreAuthorize("@ss.hasPermission('efvs:attribute:export')")
    public void exportAttributeExcel(@Valid AttributePageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<AttributeDO> list = attributeService.getAttributePage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "产品属性.xls", "数据", AttributeRespVO.class,
                        BeanUtils.toBean(list, AttributeRespVO.class));
    }

}
