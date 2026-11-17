package cn.iocoder.yudao.module.data.controller.admin.product;

import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.data.service.productfile.ProductFileService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;

import javax.validation.*;
import javax.servlet.http.*;
import java.util.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.stream.Collectors;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;



import cn.iocoder.yudao.module.data.controller.admin.product.vo.*;
import cn.iocoder.yudao.module.data.dal.dataobject.product.ProductDO;
import cn.iocoder.yudao.module.data.service.product.ProductService;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/data/product")
@Validated
public class ProductController {

    @Resource
    private ProductService productService;

    @Resource
    private ProductFileService productFileService;

    @PostMapping("/create")
    @PreAuthorize("@ss.hasPermission('data:product:create')")
    public CommonResult<Integer> createProduct(@Valid @RequestBody ProductSaveReqVO createReqVO) {
        return success(productService.createProduct(createReqVO));
    }

    @PutMapping("/update")
    @PreAuthorize("@ss.hasPermission('data:product:update')")
    public CommonResult<Boolean> updateProduct(@Valid @RequestBody List<ProductSaveReqVO> updateReqVOS) {
        productService.updateProduct(updateReqVOS);
        return success(true);
    }

    @DeleteMapping("/delete")
    @PreAuthorize("@ss.hasPermission('data:product:delete')")
    public CommonResult<Boolean> deleteProduct(@RequestParam("id") Integer id) {
        ProductDO product = productService.getProduct(id);
        Long tenantId = TenantContextHolder.getTenantId();
        productFileService.updateByPartNameAndSequenceNumber(product.getPartNumber(), product.getSequenceNumber(), tenantId);
        productService.deleteProduct(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @PreAuthorize("@ss.hasPermission('data:product:delete')")
    public CommonResult<Boolean> deleteProductList(@RequestParam("ids") List<Integer> ids) {

        //先删除product_file表数据
        for (Integer id : ids) {
            ProductDO product = productService.getProduct(id);
            Long tenantId = TenantContextHolder.getTenantId();
            productFileService.updateByPartNameAndSequenceNumber(product.getPartNumber(), product.getSequenceNumber(), tenantId);
        }
        productService.deleteProductListByIds(ids);

        return success(true);
    }

    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('data:product:query')")
    public CommonResult<ProductRespVO> getProduct(@RequestParam("id") Integer id) {
        ProductDO product = productService.getProduct(id);
        ProductRespVO productRespVO = new ProductRespVO();
        productRespVO.setName(product.getName());
        productRespVO.setPartNumber(product.getPartNumber());
        productRespVO.setCreator(product.getCreator());
        productRespVO.setRemark(product.getRemark());
        productRespVO.setId(product.getId());
        productRespVO.setSequenceNumber(product.getSequenceNumber());
        productRespVO.setCustomerNumber(product.getCustomerNumber());
        productRespVO.setCustomerName(product.getCustomerName());
        productRespVO.setSignVersion(product.getSignVersion());
        productRespVO.setBaseline(product.getBaseline());
        productRespVO.setDocumentNumber(product.getDocumentNumber());
        productRespVO.setDeliveryTime(product.getDeliveryTime());
        productRespVO.setDeliveryDestination(product.getDeliveryDestination());
        return success(productRespVO);
    }

    @GetMapping("/page")
    @PreAuthorize("@ss.hasPermission('data:product:query')")
    public CommonResult<PageResult<ProductRespVO>> getProductPage(@Valid ProductPageReqVO pageReqVO) {
        PageResult<ProductDO> pageResult = productService.getProductPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ProductRespVO.class));
    }

    @GetMapping("/export-excel")
    @PreAuthorize("@ss.hasPermission('data:product:export')")
    public void exportProductExcel(@Valid ProductPageReqVO pageReqVO,
                                   HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ProductDO> list = productService.getProductPage(pageReqVO).getList();
        // 批量查询创建人昵称（creator 为 system_users.id）
        List<Integer> userIds = list.stream()
                .map(ProductDO::getCreator)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> {
                    try {
                        return Integer.parseInt(s);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        List<HashMap<Object, Object>> userMap = userIds.isEmpty()
                ? new ArrayList<>()
                : productService.selectUsers(userIds);

        List<ProductRespVO> voList = BeanUtils.toBean(list, ProductRespVO.class);
        for (ProductRespVO vo : voList) {
            if (vo.getCreator() == null || userMap.isEmpty()) {
                continue;
            }
            for (HashMap<Object, Object> map : userMap) {
                if (map.get("id") != null && vo.getCreator().trim().equals(map.get("id").toString())) {
                    vo.setCreateName(map.get("nickname") != null ? map.get("nickname").toString() : null);
                    break;
                }
            }
        }
        // 导出 Excel（ProductRespVO 中 List 类型字段已用 @ExcelIgnore 排除，避免 Converter 报错）
        ExcelUtils.write(response, "产品.xlsx", "数据", ProductRespVO.class, voList);
    }

    private List<Integer> parseList(String value) {
        if (value == null || value.isEmpty() || value.equals("[]")) {
            return new ArrayList<>();
        }

        // 移除首尾方括号（如果存在）
        if (value.startsWith("[") && value.endsWith("]")) {
            value = value.substring(1, value.length() - 1);
        }

        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::valueOf)
                .collect(Collectors.toList());
    }

    @GetMapping("/getProductSerialNumber")
    @PreAuthorize("@ss.hasPermission('data:product:getProductSerialNumber')")
    public CommonResult<List<ProductRespVO>> getProductSerialNumber(@RequestParam("partNumber") String partNumber, @RequestParam(name = "SerialNumber",required = false) String SerialNumber ) {
        List<ProductRespVO> datas = productService.getProductSerialNumber(partNumber, SerialNumber);
        return success(datas);
    }

    @GetMapping("/getProductFile")
    public CommonResult<List<ProductRespVO>> getProductFile(@RequestParam("partNumber") String partNumber,
                                                            @RequestParam(name = "sequenceNumber") String sequenceNumber) {
        List<ProductRespVO> datas = productService.getProductFile(partNumber, sequenceNumber);
        return success(datas);
    }

    @PostMapping("/import-file-excel")
    @PreAuthorize("@ss.hasPermission('data:product:import')")
    public CommonResult<List<ProductFileImportExcelVO>> importProductFileExcel(
            @RequestParam("file") MultipartFile file) {
        CommonResult<List<ProductFileImportExcelVO>> result = productService.importProductFileExcel(file);
        return result;
    }

    @GetMapping("/getProductAll")
    public CommonResult<List<ProductRespVO>> getProductAll() {
        List<ProductDO> productList = productService.getProductAll();
        return success(BeanUtils.toBean(productList, ProductRespVO.class));
    }

    @GetMapping("/download-archive-template")
    @PreAuthorize("@ss.hasPermission('data:product:query')")
    public void downloadArchiveTemplate(HttpServletResponse response) throws IOException {
        // 固定下载 resources/template/档案模版.xlsx
        String resourcePath = "template/档案模版.xlsx";
        ClassPathResource resource = new ClassPathResource(resourcePath);
        if (!resource.exists()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":404,\"msg\":\"模板文件未找到，请将 档案模版.xlsx 放入 resources/template/ 目录\"}");
            return;
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("档案模版.xlsx", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);

        try (InputStream inputStream = resource.getInputStream()) {
            IOUtils.copy(inputStream, response.getOutputStream());
            response.flushBuffer();
        }
    }
}
