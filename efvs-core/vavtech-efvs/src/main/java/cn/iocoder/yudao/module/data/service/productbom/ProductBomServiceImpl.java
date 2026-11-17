package cn.iocoder.yudao.module.data.service.productbom;

import cn.idev.excel.util.StringUtils;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.data.controller.admin.productbom.vo.ProductBomImportExcelVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.io.InputStream;
import java.util.*;

import cn.iocoder.yudao.module.data.controller.admin.productbom.vo.*;
import cn.iocoder.yudao.module.data.dal.dataobject.productbom.ProductBomDO;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.data.dal.mysql.productbom.ProductBomMapper;
import org.springframework.web.multipart.MultipartFile;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.data.enums.ErrorCodeConstants.*;

/**
 * 产品BOM Service 实现类
 *
 * @author admin
 */
@Service
@Validated
public class ProductBomServiceImpl implements ProductBomService {

    @Resource
    private ProductBomMapper productBomMapper;

    @Override
    public Integer createProductBom(ProductBomSaveReqVO createReqVO) {
        // 插入
        ProductBomDO productBom = BeanUtils.toBean(createReqVO, ProductBomDO.class);
        productBomMapper.insert(productBom);

        // 返回
        return productBom.getId();
    }

    @Override
    public void updateProductBom(ProductBomSaveReqVO updateReqVO) {
        // 校验存在
        validateProductBomExists(updateReqVO.getId());
        // 更新
        ProductBomDO updateObj = BeanUtils.toBean(updateReqVO, ProductBomDO.class);
        productBomMapper.updateById(updateObj);
    }

    @Override
    public void deleteProductBom(Integer id) {
        // 校验存在
        validateProductBomExists(id);
        // 删除
        productBomMapper.deleteById(id);
    }

    @Override
        public void deleteProductBomListByIds(List<Integer> ids) {
        // 删除
        productBomMapper.deleteByIds(ids);
        }

    private void validateProductBomExists(Integer id) {
        if (productBomMapper.selectById(id) == null) {
            throw exception(PRODUCT_BOM_NOT_EXISTS);
        }
    }

    @Override
    public ProductBomDO getProductBom(Integer id) {
        return productBomMapper.selectById(id);
    }

    @Override
    public PageResult<ProductBomDO> getProductBomPage(ProductBomPageReqVO pageReqVO) {
        return productBomMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ProductBomDO> findChildren(List<ProductBomDO> list) {
        Long tenantId = TenantContextHolder.getTenantId();
        // 多个根节点共用同一个访问集合，防止异常 BOM 数据形成环时无限递归。
        Set<String> visitedPartNumbers = new HashSet<>();
        for (ProductBomDO data : list) {
            findchildren(data, tenantId, visitedPartNumbers);
        }
        return list;
    }

    @Override
    public List<ProductBomRespVO> selectBOMList(String partNumber) {
        Long tenantId = TenantContextHolder.getTenantId();
        List<ProductBomRespVO> productBOMRespVos = productBomMapper.selectBOMList(tenantId);
        List<ProductBomRespVO> bomLists = filterDataWithoutLevel(productBOMRespVos, partNumber);
        return bomLists;
    }

    @Override
    public List<ProductBomRespVO> selectBOMDetailList(String partNumber,
                                                      String name,
                                                      String productVersion,
                                                      Integer productType) {
        Long tenantId = TenantContextHolder.getTenantId();
        // 需求：传入父级 partNumber，返回该节点下所有层级的子数据（整棵子树，不仅是一层 children）
        // 为避免数据库递归查询的兼容性问题，这里使用全量数据 + 内存遍历构建子树，再做条件过滤
        List<ProductBomRespVO> all = productBomMapper.selectBOMList(tenantId);
        List<ProductBomRespVO> subtree = filterSubTreeByParentPartNumber(all, partNumber);
        return filterSubtreeByConditions(subtree, name, null, productVersion, productType);
    }

    @Override
    public List<ProductBomDetailExcelVO> listBOMDetailForExcelExport(String partNumber) {
        List<ProductBomRespVO> source = selectBOMList(partNumber);
        List<ProductBomDetailExcelVO> rows = new ArrayList<>(source.size());
        for (ProductBomRespVO vo : source) {
            rows.add(toDetailExcelRow(vo));
        }
        return rows;
    }

    @Override
    public List<ProductBomDetailExcelVO> listProductBomForExcelExport(ProductBomPageReqVO pageReqVO) {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ProductBomDO> list = getProductBomPage(pageReqVO).getList();
        List<ProductBomRespVO> vos = BeanUtils.toBean(list, ProductBomRespVO.class);
        List<ProductBomDetailExcelVO> rows = new ArrayList<>(vos.size());
        for (ProductBomRespVO vo : vos) {
            rows.add(toDetailExcelRow(vo));
        }
        return rows;
    }

    /** 与导入模版一致：类型/细类/大类/小类转中文；空与字面量 "null" 不落格 */
    private ProductBomDetailExcelVO toDetailExcelRow(ProductBomRespVO vo) {
        ProductBomDetailExcelVO row = new ProductBomDetailExcelVO();
        row.setId(vo.getId());
        row.setLevel(vo.getLevel());
        row.setProductNum(vo.getProductNum());
        row.setPmn(excelOptionalText(vo.getPmn()));
        row.setProductVersion(excelOptionalText(vo.getProductVersion()));
        row.setProductType(excelOptionalText(productTypeToLabel(vo.getProductType())));
        row.setPartNumber(excelOptionalText(vo.getPartNumber()));
        row.setName(excelOptionalText(vo.getName()));
        row.setProductPartNumber(excelOptionalText(vo.getProductPartNumber()));
        row.setUnit(excelOptionalText(vo.getUnit()));
        row.setTagNumber(excelOptionalText(vo.getTagNumber()));
        row.setSubcategory(excelOptionalText(subcategoryToLabel(vo.getSubcategory())));
        row.setDrawingNumber(excelOptionalText(vo.getDrawingNumber()));
        row.setCategory(excelOptionalText(categoryToLabel(vo.getCategory())));
        row.setSmallSubcategory(excelOptionalText(smallSubcategoryToLabel(vo.getSmallSubcategory())));
        row.setRemark(excelOptionalText(vo.getRemark()));
        return row;
    }

    private List<ProductBomRespVO> filterDataWithoutLevel(List<ProductBomRespVO> productBOMRespVos, String partNumber) {
        List<ProductBomRespVO> result = new ArrayList<>();
        if (productBOMRespVos == null || productBOMRespVos.isEmpty()) {
            return result;
        }

        // 期望：仅返回指定 partNumber 对应的 BOM 子树（用于详情查看/详情导出）。
        // 兼容：partNumber 为空时，退回原行为（返回所有根及其子树）。
        String target = trimToNull(partNumber);

        Set<String> visitedNodes = new HashSet<>();
        if (target == null) {
            for (ProductBomRespVO root : productBOMRespVos) {
                result.add(root);
                findAllChildren(root, productBOMRespVos, result, visitedNodes);
            }
            return result;
        }

        // 找到与 partNumber 匹配的根节点（允许存在多条同件号不同版本/类型的根）
        for (ProductBomRespVO root : productBOMRespVos) {
            if (root == null || root.getPartNumber() == null) {
                continue;
            }
            if (target.equals(root.getPartNumber().trim())) {
                result.add(root);
                findAllChildren(root, productBOMRespVos, result, visitedNodes);
            }
        }
        return result;
}

    /**
     * 以父件号（partNumber）为根，返回其所有后代节点（不包含父节点自身）。
     * 节点关系：child.productPartNumber == parent.partNumber 且 child.level != 0（与现有递归规则保持一致）
     */
    private List<ProductBomRespVO> filterSubTreeByParentPartNumber(List<ProductBomRespVO> allData, String parentPartNumber) {
        List<ProductBomRespVO> result = new ArrayList<>();
        String root = trimToNull(parentPartNumber);
        if (root == null || allData == null || allData.isEmpty()) {
            return result;
        }

        // productPartNumber -> children
        Map<String, List<ProductBomRespVO>> childrenMap = new HashMap<>();
        for (ProductBomRespVO vo : allData) {
            if (vo == null) {
                continue;
            }
            if (vo.getLevel() == null) {
                continue;
            }
            String p = trimToNull(vo.getProductPartNumber());
            if (p == null) {
                continue;
            }
            childrenMap.computeIfAbsent(p, k -> new ArrayList<>()).add(vo);
        }

        Deque<String> queue = new ArrayDeque<>();
        queue.add(root);
        // 只按行 id 去重：避免把“同父子件号但不同版本/类型/位号”的多条记录误去重掉
        Set<Integer> visitedIds = new HashSet<>();
        while (!queue.isEmpty()) {
            String current = queue.poll();
            List<ProductBomRespVO> children = childrenMap.get(current);
            if (children == null || children.isEmpty()) {
                continue;
            }
            for (ProductBomRespVO child : children) {
                if (child.getId() != null && !visitedIds.add(child.getId())) {
                    continue;
                }
                String childPn = trimToNull(child.getPartNumber());
                result.add(child);
                if (childPn != null) {
                    queue.add(childPn);
                }
            }
        }
        return result;
    }

    private List<ProductBomRespVO> filterSubtreeByConditions(List<ProductBomRespVO> source,
                                                            String name,
                                                            String partNumber,
                                                            String productVersion,
                                                            Integer productType) {
        if (source == null || source.isEmpty()) {
            return Collections.emptyList();
        }
        String nameLike = trimToNull(name);
        String pnEq = trimToNull(partNumber);
        String pvEq = trimToNull(productVersion);
        Integer ptEq = productType;

        List<ProductBomRespVO> out = new ArrayList<>(source.size());
        for (ProductBomRespVO vo : source) {
            if (vo == null) {
                continue;
            }
            if (nameLike != null) {
                String n = vo.getName();
                if (n == null || !n.contains(nameLike)) {
                    continue;
                }
            }
            if (pnEq != null) {
                String pn = trimToNull(vo.getPartNumber());
                if (pn == null || !pnEq.equals(pn)) {
                    continue;
                }
            }
            if (pvEq != null) {
                String pv = trimToNull(vo.getProductVersion());
                if (pv == null || !pvEq.equals(pv)) {
                    continue;
                }
            }
            if (ptEq != null) {
                if (vo.getProductType() == null || !ptEq.equals(vo.getProductType())) {
                    continue;
                }
            }
            out.add(vo);
        }
        return out;
    }

    private void findAllChildren(ProductBomRespVO parent, List<ProductBomRespVO> allData,
                             List<ProductBomRespVO> result, Set<String> visitedNodes) {
    // 防止循环引用
    String nodeKey = parent.getPartNumber() + "_" + parent.getProductPartNumber();
    if (visitedNodes.contains(nodeKey)) {
        return; // 避免循环引用导致的无限递归
    }

    visitedNodes.add(nodeKey);

    try {
        List<ProductBomRespVO> children = new ArrayList<>();
        for (ProductBomRespVO data : allData) {
            if (data.getProductPartNumber() != null && parent.getPartNumber() != null){
                if (data.getLevel() != null && data.getLevel() != 0
                        && data.getProductPartNumber().equals(parent.getPartNumber())) {
                    children.add(data);
                }
            }
        }
        result.addAll(children);

        // 递归查找下一级子节点
        for (ProductBomRespVO child : children) {
            findAllChildren(child, allData, result, visitedNodes);
        }
    } finally {
        visitedNodes.remove(nodeKey); // 回溯时移除，允许其他分支使用
    }
}

    private void findchildren(ProductBomDO ProductBomDO, Long tenantId, Set<String> visitedPartNumbers) {
        // 递归关系以“子节点所属件号 == 父节点件号”建立；visitedPartNumbers 用于环检测。
        if (StringUtils.isEmpty(ProductBomDO.getPartNumber()) ||
                visitedPartNumbers.contains(ProductBomDO.getPartNumber())) {
            return;
        }

        visitedPartNumbers.add(ProductBomDO.getPartNumber());

        List<ProductBomDO> childrenData = productBomMapper.selectProduct(ProductBomDO.getPartNumber(), tenantId);
        if (CollectionUtils.isNotEmpty(childrenData)) {
            ProductBomDO.setChildren(childrenData);
            for (ProductBomDO childrenDatum : childrenData) {
                findchildren(childrenDatum, tenantId, visitedPartNumbers);
            }
        }

        visitedPartNumbers.remove(ProductBomDO.getPartNumber()); // 回溯时移除标记
    }

    @Override
    public CommonResult<List<ProductBomImportExcelVO>> importProductExcel(MultipartFile file) {
        String bizNo = "PRODUCT-BOM-IMPORT-" + System.currentTimeMillis();
        String fileName = file != null && file.getOriginalFilename() != null
                ? file.getOriginalFilename() : "未命名文件";
        putProductBomImportLogResult(bizNo, fileName, file, 0, 0);
        // 按当前样例读取：sheet=抽产基线，表头=第2行，数据=第3行开始
        CommonResult<List<ProductBomImportExcelVO>> importDataList = parseProductBomFromTemplate(file);
        CommonResult<List<ProductBomImportExcelVO>> skippedRows = new CommonResult<>();
        skippedRows.setData(new ArrayList<ProductBomImportExcelVO>());
        Long tenantId = TenantContextHolder.getTenantId();

        // 不做 Excel 文件内去重，保留原始导入行用于后续库内去重与入库
        CommonResult<List<ProductBomImportExcelVO>> excelDeduped = importDataList;

        // 2) 库内去重：
        // - 有件号(代号)时：代号 + 版本号
        // - 件号为空时：规格或图号 + 版本号
        Set<String> existingKeys = new HashSet<>();
        Set<String> partNumbers = new HashSet<>();
        Set<String> drawingNumbers = new HashSet<>();
        Set<String> productVersions = new HashSet<>();
        for (ProductBomImportExcelVO row : excelDeduped.getData()) {
            String partNumber = trimToNull(row.getPartNumber());
            String drawingNumber = trimToNull(row.getDrawingNumber());
            String productVersion = trimToNull(row.getProductVersion());
            if (partNumber == null || productVersion == null) {
                // 件号为空时，允许用图号+版本做去重；版本为空无法去重
                if (drawingNumber == null || productVersion == null) {
                    continue;
                }
            }
            if (partNumber != null) {
                partNumbers.add(partNumber);
            } else if (drawingNumber != null) {
                drawingNumbers.add(drawingNumber);
            }
            productVersions.add(productVersion);
        }
        if ((!partNumbers.isEmpty() || !drawingNumbers.isEmpty()) && !productVersions.isEmpty()) {
            QueryWrapper<ProductBomDO> qw = new QueryWrapper<ProductBomDO>()
                    .eq("tenant_id", tenantId)
                    .eq("deleted", 0)
                    .in("product_version", productVersions);
            if (!partNumbers.isEmpty() && !drawingNumbers.isEmpty()) {
                qw.and(w -> w.in("part_number", partNumbers).or().in("drawing_number", drawingNumbers));
            } else if (!partNumbers.isEmpty()) {
                qw.in("part_number", partNumbers);
            } else {
                qw.in("drawing_number", drawingNumbers);
            }
            List<ProductBomDO> existing = productBomMapper.selectList(qw);
            for (ProductBomDO ex : existing) {
                String key = buildBomDedupKey(ex.getPartNumber(), ex.getDrawingNumber(), ex.getProductVersion());
                if (key != null) {
                    existingKeys.add(key);
                }
            }
        }

        // 处理导入数据
        int successCount = 0;
        for (ProductBomImportExcelVO importData : excelDeduped.getData()) {
            String key = buildBomDedupKey(importData.getPartNumber(), importData.getDrawingNumber(), importData.getProductVersion());
            if (key != null && existingKeys.contains(key)) {
                importData.setSkipped(true);
                String pn = trimToNull(importData.getPartNumber());
                importData.setSkipReason(pn != null ? "数据库已存在（代号+版本号）" : "数据库已存在（规格或图号+版本号）");
                skippedRows.getData().add(importData);
                continue;
            }
            try {
                // 转换为保存请求对象
                ProductBomSaveReqVO productBomSaveReqVO = buildProductSaveReqVO(importData);

                // 创建产品
                createProductBom(productBomSaveReqVO);
                successCount++;

                // 标记为成功（可以在importData中添加success字段，这里简化处理）
            } catch (Exception e) {
                importData.setSkipped(true);
                String msg = e.getMessage();
                importData.setSkipReason("入库失败" + (msg != null && !msg.trim().isEmpty() ? "：" + msg : ""));
                skippedRows.getData().add(importData);
            }
        }

        putProductBomImportLogResult(bizNo, fileName, file, successCount, skippedRows.getData().size());
        return skippedRows;
    }

    /**
     * 将产品 BOM 导入的批次、文件元数据和处理统计写入日志上下文。
     */
    private static void putProductBomImportLogResult(String bizNo, String fileName, MultipartFile file,
                                                     int successCount, int skippedCount) {
        Map<String, Object> importExtra = new LinkedHashMap<>();
        importExtra.put("batchNo", bizNo);
        importExtra.put("fileName", fileName);
        importExtra.put("fileSize", file != null ? file.getSize() : 0);
        importExtra.put("contentType", file != null ? file.getContentType() : null);
        importExtra.put("successCount", successCount);
        importExtra.put("skippedCount", skippedCount);
    }

    private CommonResult<List<ProductBomImportExcelVO>> parseProductBomFromTemplate(MultipartFile file) {
        try (InputStream in = file.getInputStream(); Workbook workbook = WorkbookFactory.create(in)) {
            Sheet sheet = workbook.getSheet("投产基线");
            if (sheet == null) {
                return CommonResult.error(NOT_FOUNT_PRODUCTION_BASELINE);
            }

            // 第2行为表头（Excel 从1开始计数）
            final int headerRowIndex = 1;
            Row headerRow = sheet.getRow(headerRowIndex);
            if (headerRow == null) {
                return CommonResult.error(HEADER_IS_EMPTY);
            }

            DataFormatter formatter = new DataFormatter();
            Map<String, Integer> headerIndex = buildHeaderIndex(headerRow, formatter);

            CommonResult<List<ProductBomImportExcelVO>> result = new CommonResult<>();
            result.setData(new ArrayList<ProductBomImportExcelVO>());
            for (int r = headerRowIndex + 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null || isAllBlankRow(row, formatter)) {
                    continue;
                }
                ProductBomImportExcelVO vo = new ProductBomImportExcelVO();
                vo.setExcelRowNum(r + 1);
                vo.setPmn(readString(row, headerIndex, formatter, "PMN"));
                vo.setLevel(readInteger(row, headerIndex, formatter, "层级"));
                vo.setProductVersion(readString(row, headerIndex, formatter, "版本"));
                vo.setName(readString(row, headerIndex, formatter, "名称"));
                vo.setProductPartNumber(readString(row, headerIndex, formatter, "所属部件的代号", "所属部件代号"));
                vo.setPartNumber(readString(row, headerIndex, formatter, "代号"));
                vo.setProductNum(readInteger(row, headerIndex, formatter, "数量"));
                vo.setUnit(readString(row, headerIndex, formatter, "单位"));
                vo.setRemark(readString(row, headerIndex, formatter, "备注"));
                vo.setTagNumber(readString(row, headerIndex, formatter, "位号"));
                vo.setSubcategory(readString(row, headerIndex, formatter, "细类"));
                vo.setDrawingNumber(readString(row, headerIndex, formatter, "规格或图号"));
                vo.setCategory(readString(row, headerIndex, formatter, "大类"));
                vo.setSmallSubcategory(readString(row, headerIndex, formatter, "小类"));

                // 兼容旧模板可能存在的列
                vo.setProductType(readString(row, headerIndex, formatter, "类型"));
                vo.setSequenceNumber(readString(row, headerIndex, formatter, "序列号"));
                vo.setManufacturer(readString(row, headerIndex, formatter, "生产厂家"));
                vo.setCustomerNumber(readString(row, headerIndex, formatter, "客户编号"));
                vo.setCustomerName(readString(row, headerIndex, formatter, "客户名称"));
                vo.setSignVersion(readString(row, headerIndex, formatter, "标牌版本"));
                vo.setBaseline(readString(row, headerIndex, formatter, "对应基线"));

                result.getData().add(vo);
            }
            return result;
        } catch (Exception e) {
            throw exception(IMPORT_FAILED_ONE, e.getMessage());
        }
    }

    private Map<String, Integer> buildHeaderIndex(Row headerRow, DataFormatter formatter) {
        Map<String, Integer> map = new HashMap<>();
        short last = headerRow.getLastCellNum();
        for (int c = 0; c < last; c++) {
            String v = formatter.formatCellValue(headerRow.getCell(c));
            if (v == null) {
                continue;
            }
            String key = normalizeHeader(v);
            if (!key.isEmpty()) {
                map.put(key, c);
            }
        }
        return map;
    }

    private String readString(Row row, Map<String, Integer> headerIndex, DataFormatter formatter, String... names) {
        Integer col = pickCol(headerIndex, names);
        if (col == null) {
            return null;
        }
        String value = formatter.formatCellValue(row.getCell(col));
        return excelOptionalText(value);
    }

    private Integer readInteger(Row row, Map<String, Integer> headerIndex, DataFormatter formatter, String... names) {
        String text = readString(row, headerIndex, formatter, names);
        if (text == null) {
            return null;
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Integer pickCol(Map<String, Integer> headerIndex, String... names) {
        for (String name : names) {
            Integer idx = headerIndex.get(normalizeHeader(name));
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
        return value.replace(" ", "").replace("\n", "").replace("\r", "").replace("：", "").replace(":", "").trim();
    }

    private boolean isAllBlankRow(Row row, DataFormatter formatter) {
        short last = row.getLastCellNum();
        if (last <= 0) {
            return true;
        }
        for (int c = 0; c < last; c++) {
            String v = formatter.formatCellValue(row.getCell(c));
            if (excelOptionalText(v) != null) {
                return false;
            }
        }
        return true;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String buildBomDedupKey(String partNumber, String drawingNumber, String productVersion) {
        String pv = trimToNull(productVersion);
        if (pv == null) {
            return null;
        }
        String pn = trimToNull(partNumber);
        if (pn != null) {
            return "PN##" + pn + "##" + pv;
        }
        String dn = trimToNull(drawingNumber);
        if (dn != null) {
            return "DN##" + dn + "##" + pv;
        }
        return null;
    }

    private ProductBomSaveReqVO buildProductSaveReqVO(ProductBomImportExcelVO importData) {
        ProductBomSaveReqVO productBomSaveReqVO = new ProductBomSaveReqVO();
        productBomSaveReqVO.setPmn(importData.getPmn());
        // 设置默认值（如果需要）
        if (importData.getLevel() == null) {
            productBomSaveReqVO.setLevel(0);
        }else{
            productBomSaveReqVO.setLevel(importData.getLevel());
        }
        productBomSaveReqVO.setPmn(importData.getPmn());
        productBomSaveReqVO.setName(importData.getName());
        productBomSaveReqVO.setProductPartNumber(importData.getProductPartNumber());
        productBomSaveReqVO.setProductVersion(importData.getProductVersion());
        productBomSaveReqVO.setProductType(StringUtils.isNotBlank(importData.getProductType())? productTypeConverterInteger(importData.getProductType()): null);
        productBomSaveReqVO.setProductNum(importData.getProductNum());
        productBomSaveReqVO.setPartNumber(importData.getPartNumber());
        productBomSaveReqVO.setUnit(importData.getUnit());
        productBomSaveReqVO.setRemark(importData.getRemark());
        productBomSaveReqVO.setTagNumber(importData.getTagNumber());
        if (StringUtils.isNotBlank(importData.getSubcategory())) {
            Integer subCode = SubcategoryConverterInteger(importData.getSubcategory());
            productBomSaveReqVO.setSubcategory(subCode != null ? String.valueOf(subCode) : null);
        } else {
            productBomSaveReqVO.setSubcategory(null);
        }
        productBomSaveReqVO.setSmallSubcategory(StringUtils.isNotBlank(importData.getSmallSubcategory()) ? Small_subcategoryConverterInteger(importData.getSmallSubcategory()) : null);
        productBomSaveReqVO.setCategory(StringUtils.isNotBlank(importData.getCategory()) ? CategoryConverterInteger(importData.getCategory()) : null);
        productBomSaveReqVO.setDrawingNumber(importData.getDrawingNumber());
        return productBomSaveReqVO;
    }

    private Integer productTypeConverterInteger(String productType) {
        Integer productTypeInteger = null;
        switch (productType) {
            case "装配":
                productTypeInteger = 1;
                break;
            case "机加":
                productTypeInteger = 2;
                break;
            case "虚拟部件":
                productTypeInteger = 3;
                break;
            case "标准件":
                productTypeInteger = 4;
                break;
            case "外购件":
                productTypeInteger = 5;
                break;
            case "电路模块":
                productTypeInteger = 6;
                break;
            case "电子元器件（供应商组装用）":
                productTypeInteger = 7;
                break;
            case "消辅材料":
                productTypeInteger = 8;
                break;
        }
        return productTypeInteger;
    }

    private Integer Small_subcategoryConverterInteger(String smallSubcategory) {
        Integer smallSubcategoryInteger = null;
        switch (smallSubcategory) {
            case "紧固件":
                smallSubcategoryInteger = 1;
                break;
            case "插头/插座":
                smallSubcategoryInteger = 2;
                break;
            case "其它":
                smallSubcategoryInteger = 3;
                break;
            case "电阻":
                smallSubcategoryInteger = 4;
                break;
            case "模拟集成电路":
                smallSubcategoryInteger = 5;
                break;
            case "传感器":
                smallSubcategoryInteger = 6;
                break;
            case "外购件":
                smallSubcategoryInteger = 7;
                break;
            case "胶粘剂":
                smallSubcategoryInteger = 8;
                break;
        }
        return smallSubcategoryInteger;
    }
    private Integer SubcategoryConverterInteger(String subcategory) {
        Integer subcategoryInteger = null;
        switch (subcategory) {
            case "不锈钢垫圈（钝化）":
                subcategoryInteger = 1;
                break;
            case "不锈钢螺钉(钝化)":
                subcategoryInteger = 2;
                break;
            case "垫圈":
                subcategoryInteger = 3;
                break;
            case "圆形连接器":
                subcategoryInteger = 4;
                break;
            case "MT夹":
                subcategoryInteger = 5;
                break;
            case "不锈钢螺钉（10.9级）":
                subcategoryInteger = 6;
                break;
            case "光纤跳线":
                subcategoryInteger = 7;
                break;
            case "矩形连接器":
                subcategoryInteger = 8;
                break;
            case "一般电阻":
                subcategoryInteger = 9;
                break;
            case "霍尔集成电路":
                subcategoryInteger = 10;
                break;
            case "环境光传感器":
                subcategoryInteger = 11;
                break;
            case "不锈钢螺钉（10.9级黑色）":
                subcategoryInteger = 12;
                break;
            case "平板像源":
                subcategoryInteger = 13;
                break;
            case "底剂":
                subcategoryInteger = 14;
                break;
            case "硅橡胶":
                subcategoryInteger = 15;
                break;
            case "航空气门芯":
                subcategoryInteger = 16;
                break;
            case "不锈钢垫圈（黑色）":
                subcategoryInteger = 17;
                break;
            case "不锈钢螺钉(黑色)":
                subcategoryInteger = 18;
                break;
        }
        return subcategoryInteger;
    }

    private Integer CategoryConverterInteger(String category) {
        Integer categoryInteger = null;
        switch (category) {
            case "标准件":
                categoryInteger = 1;
                break;
            case "电子元器件":
                categoryInteger = 2;
                break;
            case "外购件":
                categoryInteger = 3;
                break;
            case "非金属":
                categoryInteger = 4;
                break;
        }
        return categoryInteger;
    }

    /** 与 {@link #productTypeConverterInteger(String)} 互逆，供详情导出 Excel */
    private String productTypeToLabel(Integer productType) {
        if (productType == null) {
            return null;
        }
        switch (productType) {
            case 1:
                return "装配";
            case 2:
                return "机加";
            case 3:
                return "虚拟部件";
            case 4:
                return "标准件";
            case 5:
                return "外购件";
            case 6:
                return "电路模块";
            case 7:
                return "电子元器件（供应商组装用）";
            case 8:
                return "消辅材料";
            default:
                return String.valueOf(productType);
        }
    }

    private String smallSubcategoryToLabel(Integer smallSubcategory) {
        if (smallSubcategory == null) {
            return null;
        }
        switch (smallSubcategory) {
            case 1:
                return "紧固件";
            case 2:
                return "插头/插座";
            case 3:
                return "其它";
            case 4:
                return "电阻";
            case 5:
                return "模拟集成电路";
            case 6:
                return "传感器";
            case 7:
                return "外购件";
            case 8:
                return "胶粘剂";
            default:
                return String.valueOf(smallSubcategory);
        }
    }

    /**
     * 无数据或字面量 "null" 时不写入 Excel（避免单元格显示 null）
     */
    private static String excelOptionalText(String value) {
        if (value == null) {
            return null;
        }
        String t = value.trim();
        if (t.isEmpty() || "null".equalsIgnoreCase(t)) {
            return null;
        }
        return t;
    }

    private String subcategoryToLabel(String subcategory) {
        if (excelOptionalText(subcategory) == null) {
            return null;
        }
        String trimmed = subcategory.trim();
        Integer code;
        try {
            code = Integer.parseInt(trimmed);
        } catch (NumberFormatException e) {
            return excelOptionalText(subcategory);
        }
        switch (code) {
            case 1:
                return "不锈钢垫圈（钝化）";
            case 2:
                return "不锈钢螺钉(钝化)";
            case 3:
                return "垫圈";
            case 4:
                return "圆形连接器";
            case 5:
                return "MT夹";
            case 6:
                return "不锈钢螺钉（10.9级）";
            case 7:
                return "光纤跳线";
            case 8:
                return "矩形连接器";
            case 9:
                return "一般电阻";
            case 10:
                return "霍尔集成电路";
            case 11:
                return "环境光传感器";
            case 12:
                return "不锈钢螺钉（10.9级黑色）";
            case 13:
                return "平板像源";
            case 14:
                return "底剂";
            case 15:
                return "硅橡胶";
            case 16:
                return "航空气门芯";
            case 17:
                return "不锈钢垫圈（黑色）";
            case 18:
                return "不锈钢螺钉(黑色)";
            default:
                return trimmed;
        }
    }

    private String categoryToLabel(Integer category) {
        if (category == null) {
            return null;
        }
        switch (category) {
            case 1:
                return "标准件";
            case 2:
                return "电子元器件";
            case 3:
                return "外购件";
            case 4:
                return "非金属";
            default:
                return String.valueOf(category);
        }
    }

}
