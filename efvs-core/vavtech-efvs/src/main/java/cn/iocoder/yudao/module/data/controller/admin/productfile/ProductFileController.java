package cn.iocoder.yudao.module.data.controller.admin.productfile;

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


import cn.iocoder.yudao.module.data.controller.admin.productfile.vo.*;
import cn.iocoder.yudao.module.data.dal.dataobject.productfile.ProductFileDO;
import cn.iocoder.yudao.module.data.service.productfile.ProductFileService;

@RestController
@RequestMapping("/data/product-file")
@Validated
public class ProductFileController {

    @Resource
    private ProductFileService productFileService;

    @PostMapping("/create")
    @PreAuthorize("@ss.hasPermission('data:product-file:create')")
    public CommonResult<Integer> createProductFile(@Valid @RequestBody ProductFileSaveReqVO createReqVO) {
        return success(productFileService.createProductFile(createReqVO));
    }

    @PutMapping("/update")
    @PreAuthorize("@ss.hasPermission('data:product-file:update')")
    public CommonResult<Boolean> updateProductFile(@Valid @RequestBody ProductFileSaveReqVO updateReqVO) {
        productFileService.updateProductFile(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @PreAuthorize("@ss.hasPermission('data:product-file:delete')")
    public CommonResult<Boolean> deleteProductFile(@RequestParam("id") Integer id) {
        productFileService.deleteProductFile(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
                @PreAuthorize("@ss.hasPermission('data:product-file:delete')")
    public CommonResult<Boolean> deleteProductFileList(@RequestParam("ids") List<Integer> ids) {
        productFileService.deleteProductFileListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('data:product-file:query')")
    public CommonResult<ProductFileRespVO> getProductFile(@RequestParam("id") Integer id) {
        ProductFileDO productFile = productFileService.getProductFile(id);
        return success(BeanUtils.toBean(productFile, ProductFileRespVO.class));
    }

    @GetMapping("/page")
    @PreAuthorize("@ss.hasPermission('data:product-file:query')")
    public CommonResult<PageResult<ProductFileRespVO>> getProductFilePage(@Valid ProductFilePageReqVO pageReqVO) {
        PageResult<ProductFileDO> pageResult = productFileService.getProductFilePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ProductFileRespVO.class));
    }

    @GetMapping("/export-excel")
    @PreAuthorize("@ss.hasPermission('data:product-file:export')")
    public void exportProductFileExcel(@Valid ProductFilePageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ProductFileDO> list = productFileService.getProductFilePage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "档案.xls", "数据", ProductFileRespVO.class,
                        BeanUtils.toBean(list, ProductFileRespVO.class));
    }

}
