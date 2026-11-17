package cn.iocoder.yudao.module.data.service.product;

import cn.idev.excel.util.StringUtils;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.web.multipart.MultipartFile;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import javax.validation.Valid;

import org.springframework.validation.annotation.Validated;

import java.io.InputStream;
import java.util.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import cn.iocoder.yudao.module.data.controller.admin.product.vo.*;
import cn.iocoder.yudao.module.data.dal.dataobject.product.ProductDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.data.dal.mysql.product.ProductMapper;
import cn.iocoder.yudao.module.data.dal.dataobject.productfile.ProductFileDO;
import cn.iocoder.yudao.module.data.dal.mysql.productfile.ProductFileMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.data.enums.ErrorCodeConstants.*;

/**
 * 产品 Service 实现类
 *
 * @author admin
 */
@Service
@Validated
public class ProductServiceImpl implements ProductService {

    @Resource
    private ProductMapper productMapper;

    @Resource
    private ProductFileMapper productFileMapper;

    @Override
    public Integer createProduct(ProductSaveReqVO createReqVO) {
        // 插入
        ProductDO product = BeanUtils.toBean(createReqVO, ProductDO.class);
        ProductDO productDO = new ProductDO();
        productDO.setPartNumber(createReqVO.getPartNumber());
        productDO.setSequenceNumber(createReqVO.getSequenceNumber());
        productDO.setCustomerNumber(createReqVO.getCustomerNumber());
        productDO.setRemark(createReqVO.getRemark());
        productMapper.insert(product);

        // 返回
        return product.getId();
    }

    @Override
    public void updateProduct(@Valid List<ProductSaveReqVO> updateReqVO) {
        // 校验存在
//        validateProductExists(updateReqVO.getId());
        // 更新
        List<ProductDO> productDOS = new ArrayList<>();
        for (ProductSaveReqVO productSaveReqVO : updateReqVO) {
            ProductDO updateObj = BeanUtils.toBean(productSaveReqVO, ProductDO.class);
            productDOS.add(updateObj);
        }
        productMapper.updateBatch(productDOS);
    }

    @Override
    public void deleteProduct(Integer id) {
        // 校验存在
        validateProductExists(id);
        // 删除
        productMapper.deleteById(id);
    }

    @Override
        public void deleteProductListByIds(List<Integer> ids) {
        // 删除
        productMapper.deleteByIds(ids);
        }

    private void validateProductExists(Integer id) {
        if (productMapper.selectById(id) == null) {
            throw exception(PRODUCT_NOT_EXISTS);
        }
    }

    @Override
    public ProductDO getProduct(Integer id) {
        return productMapper.selectById(id);
    }

    @Override
    public PageResult<ProductDO> getProductPage(ProductPageReqVO pageReqVO) {
        return productMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ProductRespVO> getProductSerialNumber(String partNumber, String SerialNumber) {
        List<ProductRespVO> res = new ArrayList<>();
        Long tenantId = TenantContextHolder.getTenantId();
        List<ProductDO> productDOS = productMapper.selectByPartNumber(partNumber, tenantId, SerialNumber);
        for (ProductDO productDO : productDOS) {
            ProductRespVO productRespVO = new ProductRespVO();
            productRespVO.setId(productDO.getId());
            productRespVO.setName(productDO.getName());
            productRespVO.setCreateName(productDO.getCreateName());
            productRespVO.setSignVersion(productDO.getSignVersion());
            productRespVO.setPartNumber(productDO.getPartNumber());
            productRespVO.setDocumentNumber(productDO.getDocumentNumber());
            productRespVO.setRevision(productDO.getRevision());
            productRespVO.setCustomerNumber(productDO.getCustomerNumber());
            productRespVO.setCustomerName(productDO.getCustomerName());
            productRespVO.setRemark(productDO.getRemark());
            productRespVO.setCreator(productDO.getCreator());
            productRespVO.setSequenceNumber(productDO.getSequenceNumber());
            productRespVO.setSignVersion(productDO.getSignVersion());
            productRespVO.setBaseline(productDO.getBaseline());
            productRespVO.setDeliveryTime(productDO.getDeliveryTime());
            res.add(productRespVO);
        }
        return res;
    }

    @Override
    public List<ProductRespVO> getProductFile(String partNumber, String sequenceNumber) {
        Long tenantId = TenantContextHolder.getTenantId();
        // 1) 先查 product：用于拿到该 partNumber 下的 sequenceNumber 集合
        String seq = trimToNull(sequenceNumber);
        QueryWrapper<ProductDO> productQuery = new QueryWrapper<ProductDO>()
                .eq("tenant_id", tenantId)
                .eq("part_number", partNumber)
                .eq("deleted", 0);
        if (seq != null) {
            productQuery.eq("sequence_number", seq);
        }

        List<ProductDO> products = productMapper.selectList(productQuery);
        if (CollectionUtils.isEmpty(products)) {
            return Collections.emptyList();
        }

        Set<String> sequenceNumbers = new HashSet<>();
        Map<String, String> baselineBySequence = new HashMap<>();
        for (ProductDO p : products) {
            String sn = trimToNull(p.getSequenceNumber());
            if (sn != null) {
                sequenceNumbers.add(sn);
                baselineBySequence.put(sn, trimToNull(p.getBaseline()));
            }
        }
        if (sequenceNumbers.isEmpty()) {
            return Collections.emptyList();
        }

        // 2) 再查 product_file：通过 (product.part_number + product.sequence_number) 联查
        List<ProductFileDO> productFiles = productFileMapper.selectList(new QueryWrapper<ProductFileDO>()
                .eq("tenant_id", tenantId)
                .eq("part_number", partNumber)
                .in("sequence_number", sequenceNumbers)
                .eq("deleted", 0)
                .orderByAsc("id"));

        // 3) 先组装 productFile 明细，再挂到 product 返回对象中
        Map<String, List<ProductArchiveConstructionRespVO>> productFileListBySequence = new HashMap<>();
        for (ProductFileDO f : productFiles) {
            ProductArchiveConstructionRespVO vo = new ProductArchiveConstructionRespVO();
            vo.setId(f.getId());
            vo.setPartNumber(f.getPartNumber());
            vo.setSequenceNumber(f.getSequenceNumber());
            vo.setPartName(f.getName());
            vo.setDrawingNumber(f.getDrawingNumber());
            vo.setDrawingVersion(f.getDrawingVersion());
            vo.setProductionNumber(f.getProductionNumber());
            String fileBaseline = trimToNull(f.getBaseline());
            String productBaseline = baselineBySequence.get(trimToNull(f.getSequenceNumber()));

            // 重要：不再把 product.baseline 覆盖到 file.baseline 的同名字段，避免“明细缺值却显示有值”的误解
            vo.setFileBaseline(fileBaseline);
            vo.setProductBaseline(productBaseline);
            vo.setBaseline(fileBaseline); // 兼容：baseline 仍然沿用“明细基线”语义
            vo.setCustomerNumber(f.getCustomerNumber());
            vo.setCustomerName(f.getCustomerName());
            vo.setRemark(f.getRemark());
            String sequenceKey = trimToNull(f.getSequenceNumber());
            productFileListBySequence.computeIfAbsent(sequenceKey, k -> new ArrayList<>()).add(vo);
        }
        List<ProductRespVO> res = new ArrayList<>(products.size());
        for (ProductDO p : products) {
            ProductRespVO productRespVO = new ProductRespVO();
            productRespVO.setId(p.getId());
            productRespVO.setName(p.getName());
            productRespVO.setPartNumber(p.getPartNumber());
            productRespVO.setCreator(p.getCreator());
            productRespVO.setRemark(p.getRemark());
            productRespVO.setSequenceNumber(p.getSequenceNumber());
            productRespVO.setCustomerNumber(p.getCustomerNumber());
            productRespVO.setCustomerName(p.getCustomerName());
            productRespVO.setSignVersion(p.getSignVersion());
            productRespVO.setBaseline(p.getBaseline());
            productRespVO.setDocumentNumber(p.getDocumentNumber());
            productRespVO.setRevision(p.getRevision());
            productRespVO.setDeliveryTime(p.getDeliveryTime());
            productRespVO.setDeliveryDestination(p.getDeliveryDestination());
            String sequenceKey = trimToNull(p.getSequenceNumber());
            productRespVO.setProductFileList(
                    productFileListBySequence.getOrDefault(sequenceKey, Collections.emptyList()));
            res.add(productRespVO);
        }
        return res;
    }

    @Override
    public List<ProductDO> getProductAll() {
        Long tenantId = TenantContextHolder.getTenantId();
        return productMapper.selectListByTenant(tenantId);
    }

    @Override
    public CommonResult<List<ProductFileImportExcelVO>> importProductFileExcel(MultipartFile file) {
        String bizNo = "PRODUCT-IMPORT-" + System.currentTimeMillis();
        String fileName = file != null && file.getOriginalFilename() != null
                ? file.getOriginalFilename() : "未命名文件";
        putProductImportLogResult(bizNo, fileName, file, 0, 0);
        Long tenantId = TenantContextHolder.getTenantId();
        ExcelParsedResult parsed = parseFixedTemplate(file);

        // ========== 1) product：第2行+第5行（合并信息） ==========
        ProductDO product = parsed.product;
        if (trimToNull(product.getPartNumber()) == null) {
            putProductImportLogResult(bizNo, fileName, file, 0, 1);
            return CommonResult.error(IMPORT_FAILED);
        }
        // product 去重：产品件号 + 标牌版本 + 序列号
        String productKey = build3Key(product.getPartNumber(), product.getSignVersion(), product.getSequenceNumber());
        if (productKey == null) {
            putProductImportLogResult(bizNo, fileName, file, 0, 1);
            return CommonResult.error(IMPORT_FAILED);
        }
        ProductDO existingProduct = productMapper.selectOne(new QueryWrapper<ProductDO>()
                .eq("tenant_id", tenantId)
                .eq("deleted", 0)
                .eq("part_number", product.getPartNumber())
                .eq("sign_version", product.getSignVersion())
                .eq("sequence_number", product.getSequenceNumber())
                .last("LIMIT 1"));
        CommonResult<List<ProductFileImportExcelVO>> duplicatedRows = new CommonResult<>();
        ArrayList<ProductFileImportExcelVO> list = new ArrayList<>();
        duplicatedRows.setData(list);
        int successCount = 0;
        if (existingProduct == null) {
            productMapper.insert(product);
            successCount++;
        } else {
            ProductFileImportExcelVO duplicatedProduct = new ProductFileImportExcelVO();
            duplicatedProduct.setExcelRowNum(5); // 模板中 product 主数据位于第 5 行
            duplicatedProduct.setPartNumber(product.getPartNumber());
            duplicatedProduct.setSignVersion(product.getSignVersion());
            duplicatedProduct.setSequenceNumber(product.getSequenceNumber());
            duplicatedProduct.setSkipped(true);
            duplicatedProduct.setSkipReason("数据库已存在（产品件号+标牌版本+序列号）");
            duplicatedRows.getData().add(duplicatedProduct);
        }

        // ========== 2) productFile：LRU构建信息下方表格 ==========
        List<ProductFileImportExcelVO> importRows = parsed.productFileRows;
        if (importRows.isEmpty()) {
            // 允许只导入 product（但按你的描述一般会有 productFile）
            putProductImportLogResult(bizNo, fileName, file, successCount, duplicatedRows.getData().size());
            return duplicatedRows;
        }

        // 2.1 Excel 内去重：图纸版本 + 生产编号（重复保留第一条）
        List<ProductFileImportExcelVO> excelDeduped = new ArrayList<>();
        Set<String> seenProductFileKeys = new HashSet<>();
        for (ProductFileImportExcelVO row : importRows) {
            String effectivePartNumber = trimToNull(row.getPartNumber());
            if (effectivePartNumber == null) {
                effectivePartNumber = trimToNull(product.getPartNumber());
                row.setPartNumber(effectivePartNumber);
            }
            String key = build2Key(row.getDrawingVersion(), row.getProductionNumber());
            if (key == null) {
                // 关键字段不完整时，不参与去重，保持导入
                excelDeduped.add(row);
                continue;
            }
            if (seenProductFileKeys.add(key)) {
                excelDeduped.add(row);
            } else {
                row.setSkipped(true);
                row.setSkipReason("Excel 文件内重复（图纸版本+生产编号）");
                duplicatedRows.getData().add(row);
            }
        }

        // 2.2 库内去重：先按 product 主键范围过滤，再比对（图纸版本+生产编号）
        Set<String> existingProductFileKeys = new HashSet<>();
        String productPartNumber = trimToNull(product.getPartNumber());
        String productSequenceNumber = trimToNull(product.getSequenceNumber());
        String productSignVersion = trimToNull(product.getSignVersion());
        if (productPartNumber != null && productSequenceNumber != null && productSignVersion != null) {
            List<ProductFileDO> existing = productFileMapper.selectList(new QueryWrapper<ProductFileDO>()
                    .eq("tenant_id", tenantId)
                    .eq("deleted", 0)
                    .eq("part_number", productPartNumber)
                    .eq("sequence_number", productSequenceNumber)
                    .eq("product_version", productSignVersion));
            for (ProductFileDO ex : existing) {
                String k = build2Key(ex.getDrawingVersion(), ex.getProductionNumber());
                if (k != null) {
                    existingProductFileKeys.add(k);
                }
            }
        }

        for (ProductFileImportExcelVO row : excelDeduped) {
            String effectivePartNumber = trimToNull(row.getPartNumber());
            if (effectivePartNumber == null) {
                effectivePartNumber = trimToNull(product.getPartNumber());
                row.setPartNumber(effectivePartNumber);
            }
            String key = build2Key(row.getDrawingVersion(), row.getProductionNumber());
            if (key != null && existingProductFileKeys.contains(key)) {
                row.setSkipped(true);
                row.setSkipReason("数据库已存在（图纸版本+生产编号）");
                duplicatedRows.getData().add(row);
                continue;
            }
            ProductFileDO entity = new ProductFileDO();
            entity.setSequenceNumber(product.getSequenceNumber());

            entity.setPartNumber(effectivePartNumber);
            entity.setSequenceNumber(row.getSequenceNumber());
            entity.setProductVersion(product.getSignVersion());
            entity.setName(StringUtils.isNotBlank(row.getPartName()) ? row.getPartName() : row.getName());
            entity.setDrawingNumber(row.getDrawingNumber());
            entity.setDrawingVersion(row.getDrawingVersion());
            entity.setProductionNumber(row.getProductionNumber());
            entity.setBaseline(row.getBaseline());
            String detailCustomerNumber = trimToNull(row.getCustomerNumber());
            String detailCustomerName = trimToNull(row.getCustomerName());
            entity.setCustomerNumber(detailCustomerNumber != null ? detailCustomerNumber : product.getCustomerNumber());
            entity.setCustomerName(detailCustomerName != null ? detailCustomerName : product.getCustomerName());
            entity.setRemark(row.getRemark());
            productFileMapper.insert(entity);
            successCount++;
        }

        putProductImportLogResult(bizNo, fileName, file, successCount, duplicatedRows.getData().size());
        return duplicatedRows;
    }

    /**
     * 将产品档案导入的批次、文件元数据和处理统计写入日志上下文。
     */
    private static void putProductImportLogResult(String bizNo, String fileName, MultipartFile file,
                                                  int successCount, int skippedCount) {
        Map<String, Object> importExtra = new LinkedHashMap<>();
        importExtra.put("batchNo", bizNo);
        importExtra.put("fileName", fileName);
        importExtra.put("fileSize", file != null ? file.getSize() : 0);
        importExtra.put("contentType", file != null ? file.getContentType() : null);
        importExtra.put("successCount", successCount);
        importExtra.put("skippedCount", skippedCount);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * @return key 形如：a##b##c；关键字段为空则返回 null
     */
    private String build3Key(String a, String b, String c) {
        String x = trimToNull(a);
        String y = trimToNull(b);
        String z = trimToNull(c);
        if (x == null || y == null || z == null) {
            return null;
        }
        return x + "##" + y + "##" + z;
    }

    private String build2Key(String a, String b) {
        String x = trimToNull(a);
        String y = trimToNull(b);
        if (x == null || y == null) {
            return null;
        }
        return x + "##" + y;
    }

    private static class ExcelParsedResult {
        private final ProductDO product;
        private final List<ProductFileImportExcelVO> productFileRows;

        private ExcelParsedResult(ProductDO product, List<ProductFileImportExcelVO> productFileRows) {
            this.product = product;
            this.productFileRows = productFileRows;
        }
    }

    /**
     * 固定模板解析（按截图样例）：
     * - 第2行 & 第5行：product 信息（key-value 形式）
     * - “LRU构建信息”下方表格：productFile 明细
     */
    private ExcelParsedResult parseFixedTemplate(MultipartFile file) {
        DataFormatter formatter = new DataFormatter();
        try (InputStream in = file.getInputStream(); Workbook workbook = WorkbookFactory.create(in)) {
            Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
            if (sheet == null) {
                throw exception(IMPORT_FAILED, "Excel 中未找到可读取的 sheet");
            }

            // 1) 解析 product：优先按“标签 -> 右侧值”读取，避免固定坐标在合并单元格下错位
            final int row2Idx = 1; // Excel 第2行（头部 key-value）
            // 第 2 行包含多组“标签 + 值”。在存在合并单元格/样式复用时，右侧格子可能仍读到“标签文本”，
            // 这里用“已知标签集合”作为边界，避免把下一个标签当成当前标签的值（例如客户名称被读成客户编号）。
            final Set<String> row2KnownLabels = new HashSet<>(Arrays.asList(
                    normalizeHeader("Document Number"),
                    normalizeHeader("Revision"),
                    normalizeHeader("客户名称"),
                    normalizeHeader("客户编号"),
                    normalizeHeader("客户代号"),
                    normalizeHeader("交付时间"),
                    normalizeHeader("交付去向")
            ));
            // Document Number 固定在 C2（Excel 从 1 开始计数），即 row=1, col=2（从 0 开始）
            String fixedDocumentNumber = getCellStringWithMergedFallback(sheet, 1, 2, formatter);
            String fixedRevision = getLabelRightValue(sheet, row2Idx, formatter, row2KnownLabels, "Revision");
            String fixedCustomerName = getLabelRightValue(sheet, row2Idx, formatter, row2KnownLabels, "客户名称");
            String fixedCustomerNumber = firstNonBlank(
                    getLabelRightValue(sheet, row2Idx, formatter, row2KnownLabels, "客户编号"),
                    getLabelRightValue(sheet, row2Idx, formatter, row2KnownLabels, "客户代号")
            );
            String fixedDeliveryTime = getLabelRightValue(sheet, row2Idx, formatter, row2KnownLabels, "交付时间");
            String fixedDeliveryDestinationFromHeader = getLabelRightValue(sheet, row2Idx, formatter, row2KnownLabels, "交付去向");

            // 2) 主产品信息：先定位“产品名称/产品件号/序列号...”表头行，再读下一行数据
            int productHeaderRowIdx = findMainProductHeaderRow(sheet, formatter);
            int productDataRowIdx = findNextNonEmptyRow(sheet, productHeaderRowIdx + 1, formatter);
            Row productHeaderRow = productHeaderRowIdx >= 0 ? sheet.getRow(productHeaderRowIdx) : null;
            Row productDataRow = productDataRowIdx >= 0 ? sheet.getRow(productDataRowIdx) : null;
            Map<String, Integer> productHeaderMap = productHeaderRow == null
                    ? Collections.emptyMap()
                    : buildHeaderIndex(productHeaderRow, formatter);

            String fixedProductName = productDataRow == null ? null : getCellString(productDataRow,
                    pickCol(productHeaderMap, "产品名称"), formatter);
            String fixedPartNumber = productDataRow == null ? null : getCellString(productDataRow,
                    pickCol(productHeaderMap, "产品件号"), formatter);
            String fixedSignVersion = productDataRow == null ? null : getCellString(productDataRow,
                    pickCol(productHeaderMap, "标牌版本", "系列版本"), formatter);
            String fixedSequenceNumber = productDataRow == null ? null : getCellString(productDataRow,
                    pickCol(productHeaderMap, "序列号", "生产编号/序列号"), formatter);
            String fixedBaseline = productDataRow == null ? null : getCellString(productDataRow,
                    pickCol(productHeaderMap, "对应基线", "基线"), formatter);
            String fixedRemark = productDataRow == null ? null : getCellString(productDataRow,
                    pickCol(productHeaderMap, "备注"), formatter);
            String fixedDeliveryDestination = firstNonBlank(
                    fixedDeliveryDestinationFromHeader,
                    productDataRow == null ? null : getCellString(productDataRow,
                            pickCol(productHeaderMap, "交付去向"), formatter)
            );

            // 兜底：如果主表头定位失败，回退到历史固定坐标，尽量不影响旧模板
            if (trimToNull(fixedProductName) == null) {
                fixedProductName = getCellStringWithMergedFallback(sheet, 4, 1, formatter); // B5
            }
            if (trimToNull(fixedPartNumber) == null) {
                fixedPartNumber = getCellStringWithMergedFallback(sheet, 4, 2, formatter); // C5
            }
            if (trimToNull(fixedSignVersion) == null) {
                fixedSignVersion = getCellStringWithMergedFallback(sheet, 4, 3, formatter); // D5
            }
            if (trimToNull(fixedSequenceNumber) == null) {
                fixedSequenceNumber = getCellStringWithMergedFallback(sheet, 4, 4, formatter); // E5
            }
            if (trimToNull(fixedBaseline) == null) {
                fixedBaseline = getCellStringWithMergedFallback(sheet, 4, 5, formatter); // F5
            }
            if (trimToNull(fixedRemark) == null) {
                fixedRemark = getCellStringWithMergedFallback(sheet, 4, 6, formatter); // G5
            }

            ProductDO product = new ProductDO();
            product.setDocumentNumber(fixedDocumentNumber);
            product.setRevision(fixedRevision);
            product.setCustomerName(fixedCustomerName);
            product.setCustomerNumber(fixedCustomerNumber);
            product.setDeliveryTime(normalizeToMySqlDateTime(fixedDeliveryTime));
            // 业务约定：交付去向优先取模板“交付去向”，为空时回退为“客户名称”
            product.setDeliveryDestination(firstNonBlank(fixedDeliveryDestination, fixedCustomerName));
            product.setName(fixedProductName);
            product.setPartNumber(fixedPartNumber);
            product.setSignVersion(fixedSignVersion);
            product.setSequenceNumber(fixedSequenceNumber);
            product.setBaseline(fixedBaseline);
            product.setRemark(fixedRemark);

            // 2) 找到 LRU 构建信息表头行，并解析 productFile 明细
            int headerRowIdx = findProductFileHeaderRow(sheet, formatter);
            if (headerRowIdx < 0) {
                // 没找到表格，按空处理
                return new ExcelParsedResult(product, Collections.emptyList());
            }

            Row headerRow = sheet.getRow(headerRowIdx);
            Map<String, Integer> colIdx = buildHeaderIndex(headerRow, formatter);
            Integer colPartName = pickCol(colIdx, "零部件名称", "部件名称");
            Integer colDrawingNumber = pickCol(colIdx, "零部件图号", "图号", "零部件图号 ");
            Integer colDrawingVersion = pickCol(colIdx, "图纸版本", "图纸版次");
            Integer colProductionNumber = pickCol(colIdx, "生产编号/序列号", "生产编号", "生产编号/序列号");
            Integer colSequenceNumber = pickCol(colIdx, "生产编号/序列号", "序列号");
            Integer colBaseline = pickCol(colIdx, "对应基线", "基线");
            Integer colRemark = pickCol(colIdx, "备注");

            List<ProductFileImportExcelVO> rows = new ArrayList<>();
            for (int r = headerRowIdx + 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) {
                    continue;
                }
                // 以“零部件名称/图号”为空作为结束判断（遇到整行空也结束）
                String partName = getCellString(row, colPartName, formatter);
                String drawingNumber = getCellString(row, colDrawingNumber, formatter);
                if (isAllBlankRow(row, formatter)) {
                    break;
                }
                if (trimToNull(partName) == null && trimToNull(drawingNumber) == null) {
                    // 遇到空行或分隔行，结束
                    break;
                }

                ProductFileImportExcelVO vo = new ProductFileImportExcelVO();
                vo.setExcelRowNum(r + 1);
                // 仅继承用于关联/判重的主键字段
                vo.setPartNumber(product.getPartNumber());
                vo.setSequenceNumber(product.getSequenceNumber());

                // 明细字段
                vo.setPartName(partName);
                vo.setDrawingNumber(drawingNumber);
                vo.setDrawingVersion(getCellString(row, colDrawingVersion, formatter));
                vo.setProductionNumber(getCellString(row, colProductionNumber, formatter));
                // 如果模板里“生产编号/序列号”实际放的是序列号，则兜底写到 vo.sequenceNumber（但你需求里 productFile 去重使用 product 的序列号，所以这里不覆盖）
                String maybeSn = getCellString(row, colSequenceNumber, formatter);
                if (StringUtils.isBlank(vo.getProductionNumber()) && StringUtils.isNotBlank(maybeSn)) {
                    vo.setProductionNumber(maybeSn);
                }
                String baselineValue = getCellString(row, colBaseline, formatter);
                if (StringUtils.isNotBlank(baselineValue)) {
                    vo.setBaseline(baselineValue);
                }
                vo.setRemark(getCellString(row, colRemark, formatter));

                rows.add(vo);
            }

            return new ExcelParsedResult(product, rows);
        } catch (Exception e) {
            throw exception(IMPORT_FAILED, e.getMessage());
        }
    }

    private String getCellStringWithMergedFallback(Sheet sheet, int rowIdx, int colIdx, DataFormatter formatter) {
        Row row = sheet.getRow(rowIdx);
        if (row != null) {
            String direct = getCellString(row, colIdx, formatter);
            if (trimToNull(direct) != null) {
                return direct;
            }
        }
        for (CellRangeAddress region : sheet.getMergedRegions()) {
            if (!region.isInRange(rowIdx, colIdx)) {
                continue;
            }
            Row firstRow = sheet.getRow(region.getFirstRow());
            if (firstRow == null) {
                return null;
            }
            return getCellString(firstRow, region.getFirstColumn(), formatter);
        }
        return null;
    }

    private String getLabelRightValue(Sheet sheet, int rowIdx, DataFormatter formatter,
                                      Set<String> knownLabelsNormalized,
                                      String... labels) {
        Row row = sheet.getRow(rowIdx);
        if (row == null) {
            return null;
        }
        short lastCell = row.getLastCellNum();
        if (lastCell <= 0) {
            return null;
        }
        Set<String> normalizedLabels = new HashSet<>();
        for (String label : labels) {
            normalizedLabels.add(normalizeHeader(label));
        }
        for (int c = 0; c < lastCell; c++) {
            String current = getCellStringWithMergedFallback(sheet, rowIdx, c, formatter);
            if (current == null) {
                continue;
            }
            String currentNorm = normalizeHeader(current);
            if (!normalizedLabels.contains(currentNorm)) {
                continue;
            }
            for (int r = c + 1; r < lastCell; r++) {
                String candidate = getCellStringWithMergedFallback(sheet, rowIdx, r, formatter);
                if (candidate == null) {
                    continue;
                }
                String candidateNorm = normalizeHeader(candidate);
                // 合并单元格会导致右侧多个格子读到同一个标签文本，这里跳过继续找实际值
                if (candidateNorm.equals(currentNorm)) {
                    continue;
                }
                // 右侧遇到任意“已知标签”，说明已进入下一组 key-value，停止查找
                if (knownLabelsNormalized != null && knownLabelsNormalized.contains(candidateNorm)) {
                    break;
                }
                if (trimToNull(candidate) == null) {
                    continue;
                }
                return candidate;
            }
            return null;
        }
        return null;
    }

    private int findProductFileHeaderRow(Sheet sheet, DataFormatter formatter) {
        // 在全 sheet 扫描，找到同时包含“零部件名称”和“零部件图号/图号”的行作为表头
        int last = sheet.getLastRowNum();
        for (int r = 0; r <= last; r++) {
            Row row = sheet.getRow(r);
            if (row == null) {
                continue;
            }
            boolean hasName = false;
            boolean hasDrawing = false;
            short lastCell = row.getLastCellNum();
            for (int c = 0; c < lastCell; c++) {
                String v = getCellString(row, c, formatter);
                if (v == null) {
                    continue;
                }
                v = v.trim();
                if ("零部件名称".equals(v) || "部件名称".equals(v)) {
                    hasName = true;
                }
                if ("零部件图号".equals(v) || "图号".equals(v)) {
                    hasDrawing = true;
                }
            }
            if (hasName && hasDrawing) {
                return r;
            }
        }
        return -1;
    }

    private int findMainProductHeaderRow(Sheet sheet, DataFormatter formatter) {
        int last = sheet.getLastRowNum();
        for (int r = 0; r <= last; r++) {
            Row row = sheet.getRow(r);
            if (row == null) {
                continue;
            }
            boolean hasName = false;
            boolean hasPartNumber = false;
            short lastCell = row.getLastCellNum();
            for (int c = 0; c < lastCell; c++) {
                String v = getCellString(row, c, formatter);
                if (v == null) {
                    continue;
                }
                String n = normalizeHeader(v);
                if ("产品名称".equals(n)) {
                    hasName = true;
                }
                if ("产品件号".equals(n)) {
                    hasPartNumber = true;
                }
            }
            if (hasName && hasPartNumber) {
                return r;
            }
        }
        return -1;
    }

    private int findNextNonEmptyRow(Sheet sheet, int startRow, DataFormatter formatter) {
        if (startRow < 0) {
            return -1;
        }
        int last = sheet.getLastRowNum();
        for (int r = startRow; r <= last; r++) {
            Row row = sheet.getRow(r);
            if (row == null) {
                continue;
            }
            if (!isAllBlankRow(row, formatter)) {
                return r;
            }
        }
        return -1;
    }

    private Map<String, Integer> buildHeaderIndex(Row headerRow, DataFormatter formatter) {
        Map<String, Integer> map = new HashMap<>();
        short last = headerRow.getLastCellNum();
        for (int c = 0; c < last; c++) {
            String v = getCellString(headerRow, c, formatter);
            if (trimToNull(v) == null) {
                continue;
            }
            map.put(v.trim(), c);
        }
        return map;
    }

    private Integer pickCol(Map<String, Integer> header, String... names) {
        Map<String, Integer> normalized = new HashMap<>();
        for (Map.Entry<String, Integer> e : header.entrySet()) {
            normalized.put(normalizeHeader(e.getKey()), e.getValue());
        }
        for (String n : names) {
            Integer idx = normalized.get(normalizeHeader(n));
            if (idx != null) {
                return idx;
            }
        }
        return null;
    }

    private String normalizeHeader(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace(" ", "")
                .replace("：", "")
                .replace(":", "")
                .trim();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (trimToNull(value) != null) {
                return value;
            }
        }
        return null;
    }

    private String getCellString(Row row, Integer colIdx, DataFormatter formatter) {
        if (colIdx == null || colIdx < 0) {
            return null;
        }
        Cell cell = row.getCell(colIdx);
        if (cell == null) {
            return null;
        }
        String s = formatter.formatCellValue(cell);
        return trimToNull(s);
    }

    private String getCellString(Row row, int colIdx, DataFormatter formatter) {
        Cell cell = row.getCell(colIdx);
        if (cell == null) {
            return null;
        }
        String s = formatter.formatCellValue(cell);
        return trimToNull(s);
    }

    private boolean isAllBlankRow(Row row, DataFormatter formatter) {
        short last = row.getLastCellNum();
        if (last <= 0) {
            return true;
        }
        for (int c = 0; c < last; c++) {
            String v = getCellString(row, c, formatter);
            if (trimToNull(v) != null) {
                return false;
            }
        }
        return true;
    }

    /**
     * 将 Excel 常见日期字符串（如 `3/26/26`）归一为 MySQL 可接受的 datetime 字符串：
     * `yyyy-MM-dd HH:mm:ss`
     */
    private String normalizeToMySqlDateTime(String input) {
        String value = trimToNull(input);
        if (value == null) {
            return null;
        }

        // 防止 Excel 里出现不可见字符导致解析失败
        value = value.replace('\u00A0', ' ').trim();

        // 常见：M/d/yy 例如 3/26/26
        List<DateTimeFormatter> dateTimePatterns = Arrays.asList(
                DateTimeFormatter.ofPattern("M/d/yy H:mm:ss"),
                DateTimeFormatter.ofPattern("M/d/yy H:mm"),
                DateTimeFormatter.ofPattern("M/d/yy HH:mm:ss"),
                DateTimeFormatter.ofPattern("M/d/yy HH:mm"),
                DateTimeFormatter.ofPattern("M/d/yyyy H:mm:ss"),
                DateTimeFormatter.ofPattern("M/d/yyyy H:mm"),
                DateTimeFormatter.ofPattern("M/d/yyyy HH:mm:ss"),
                DateTimeFormatter.ofPattern("M/d/yyyy HH:mm"),
                // 年在前：月份/日期允许 1 位
                DateTimeFormatter.ofPattern("yyyy/M/d H:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy/M/d H:mm"),
                DateTimeFormatter.ofPattern("yyyy/M/d HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy/M/d HH:mm"),
                DateTimeFormatter.ofPattern("yyyy-M-d H:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-M-d H:mm"),
                DateTimeFormatter.ofPattern("yyyy-M-d HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-M-d HH:mm")
        );
        for (DateTimeFormatter f : dateTimePatterns) {
            try {
                LocalDateTime dt = LocalDateTime.parse(value, f);
                return dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } catch (Exception ignored) {
                // try next
            }
        }

        List<DateTimeFormatter> dateOnlyPatterns = Arrays.asList(
                DateTimeFormatter.ofPattern("M/d/yy"),
                DateTimeFormatter.ofPattern("M/d/yyyy"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                // 年在前：如 2026/3/26（支持 1 位月/日）
                DateTimeFormatter.ofPattern("yyyy/M/d"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd"),
                DateTimeFormatter.ofPattern("yyyy-M-d"),
                DateTimeFormatter.ofPattern("yyyy.M.d")
        );
        for (DateTimeFormatter f : dateOnlyPatterns) {
            try {
                LocalDate d = LocalDate.parse(value, f);
                return d.atStartOfDay().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } catch (Exception ignored) {
                // try next
            }
        }

        // 兜底：尽量原样返回（让数据库自己决定/或由你后续提示）
        return value;
    }

    @Override
    public List<HashMap<Object, Object>> selectUsers(List<Integer> userIds) {
        return productMapper.selectUsers(userIds);
    }

}
