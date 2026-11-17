package cn.iocoder.yudao.module.data.controller.admin.productbom;

import cn.iocoder.yudao.module.data.controller.admin.productbom.vo.ProductBomImportExcelVO;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;

import javax.validation.*;
import javax.servlet.http.*;
import java.util.*;
import java.io.IOException;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;


import cn.iocoder.yudao.module.data.controller.admin.productbom.vo.*;
import cn.iocoder.yudao.module.data.dal.dataobject.productbom.ProductBomDO;
import cn.iocoder.yudao.module.data.service.productbom.ProductBomService;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URLEncoder;

@RestController
@RequestMapping("/data/product-bom")
@Validated
public class ProductBomController {

    @Resource
    private ProductBomService productBomService;

    @PostMapping("/create")
    @PreAuthorize("@ss.hasPermission('data:product-bom:create')")
    public CommonResult<Integer> createProductBom(@Valid @RequestBody ProductBomSaveReqVO createReqVO) {
        return success(productBomService.createProductBom(createReqVO));
    }

    @PostMapping("/update")
    @PreAuthorize("@ss.hasPermission('data:product-bom:update')")
    public CommonResult<Boolean> updateProductBom(@Valid @RequestBody ProductBomSaveReqVO updateReqVO) {
        productBomService.updateProductBom(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @PreAuthorize("@ss.hasPermission('data:product-bom:delete')")
    public CommonResult<Boolean> deleteProductBom(@RequestParam("id") Integer id) {
        productBomService.deleteProductBom(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
                @PreAuthorize("@ss.hasPermission('data:product-bom:delete')")
    public CommonResult<Boolean> deleteProductBomList(@RequestParam("ids") List<Integer> ids) {
        productBomService.deleteProductBomListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('data:product-bom:query')")
    public CommonResult<ProductBomRespVO> getProductBom(@RequestParam("id") Integer id) {
        ProductBomDO productBom = productBomService.getProductBom(id);
        return success(BeanUtils.toBean(productBom, ProductBomRespVO.class));
    }

    @GetMapping("/page")
    @PreAuthorize("@ss.hasPermission('data:product-bom:query')")
    public CommonResult<PageResult<ProductBomRespVO>> getProductBomPage(@Valid ProductBomPageReqVO pageReqVO) {

        // 获取分页结果   分页结果都是层级为0
        PageResult<ProductBomDO> pageResult = productBomService.getProductBomPage(pageReqVO);

        // 获取所有子产品（包括多级嵌套）
        List<ProductBomDO> pageChildren = productBomService.findChildren(pageResult.getList());

        // 转换为响应对象
        List<ProductBomRespVO> productBOMRespVos = BeanUtils.toBean(pageChildren, ProductBomRespVO.class);

        return success(new PageResult<>(productBOMRespVos, pageResult.getTotal()));
    }

    @GetMapping("/export-excel")
    @PreAuthorize("@ss.hasPermission('data:product-bom:export')")
    public void exportProductBomExcel(@Valid ProductBomPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        List<ProductBomDetailExcelVO> list = productBomService.listProductBomForExcelExport(pageReqVO);
        ExcelUtils.write(response, "产品BOM.xlsx", "数据", ProductBomDetailExcelVO.class, list);
    }

    @GetMapping("/export-detail-excel")
    @PreAuthorize("@ss.hasPermission('data:product-bom:export')")
    public void exportProductBomDetailExcel(@RequestParam("partNumber") String partNumber,
                                           HttpServletResponse response) throws IOException {
        List<ProductBomDetailExcelVO> list = productBomService.listBOMDetailForExcelExport(partNumber);
        String fileName = "BOM详情-" + (partNumber != null ? partNumber : "导出") + ".xlsx";
        ExcelUtils.write(response, fileName, "BOM详情", ProductBomDetailExcelVO.class, list);
    }

//    @GetMapping("/getBOMList")
//    @Operation(summary = "查看BOM数据")
//    @PreAuthorize("@ss.hasPermission('data:product:bomquery')")
//    public CommonResult<List<ProductBomRespVO>> getBOMList(@RequestParam("partNumber") String partNumber) throws IOException {
//        List<ProductBomRespVO> datas = productBomService.selectBOMList(partNumber);
//        return CommonResult.success(datas);
//    }

    @GetMapping("/getBOMDetailList")
    @PreAuthorize("@ss.hasPermission('data:product-bom:query')")
    public CommonResult<List<ProductBomRespVO>> getBOMDetailList(
            @RequestParam("partNumber") String partNumber,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "productVersion", required = false) String productVersion,
            @RequestParam(value = "productType", required = false) Integer productType) {
        List<ProductBomRespVO> datas = productBomService.selectBOMDetailList(
                partNumber, name, productVersion, productType);
        return CommonResult.success(datas);
    }

    @PostMapping("/import-excel")
    public CommonResult<List<ProductBomImportExcelVO>> importProductExcel(@RequestParam("file") MultipartFile file) {
        CommonResult<List<ProductBomImportExcelVO>> result = productBomService.importProductExcel(file);
        return result;
    }

    @GetMapping("/download-template")
    @PreAuthorize("@ss.hasPermission('data:product-bom:query')")
    public void downloadBomTemplate(HttpServletResponse response) throws IOException {
        // 支持两种路径：resources/template/ 与 resources 根目录
        String[] candidates = new String[] { "template/BOM导入模版.xlsx", "BOM导入模版.xlsx" };
        ClassPathResource resource = null;
        for (String path : candidates) {
            ClassPathResource r = new ClassPathResource(path);
            if (r.exists()) {
                resource = r;
                break;
            }
        }
        if (resource == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":404,\"msg\":\"模板文件未找到，请将 BOM导入模版.xlsx 放入 resources/template/ 目录或 resources 根目录\"}");
            return;
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("BOM导入模版.xlsx", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);

        try (InputStream inputStream = resource.getInputStream()) {
            IOUtils.copy(inputStream, response.getOutputStream());
            response.flushBuffer();
        }
    }

}
