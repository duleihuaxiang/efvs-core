package cn.iocoder.yudao.module.data.controller.admin.productbom.vo;

import cn.idev.excel.annotation.*;
import lombok.Data;

/**
 * 产品 Excel 导入 VO
 */
@Data
public class ProductBomImportExcelVO {
    /** Excel 行号（从 1 开始） */
    private Integer excelRowNum;

    /** 是否跳过入库 */
    private Boolean skipped;

    /** 跳过原因 */
    private String skipReason;

    @ExcelProperty("PMN")
    private String pmn;

    @ExcelProperty("层级")
    private Integer level;

    @ExcelProperty("名称")
    private String name;

    @ExcelProperty("所属部件的代号")
    private String productPartNumber;

    @ExcelProperty("版本")
    private String productVersion;

    @ExcelProperty("类型")
    private String productType;

    @ExcelProperty("数量")
    private Integer productNum;

    @ExcelProperty("代号")
    private String partNumber;

    @ExcelProperty("序列号")
    private String sequenceNumber;

    @ExcelProperty("生产厂家")
    private String manufacturer;

    @ExcelProperty("客户编号")
    private String customerNumber;

    @ExcelProperty("客户名称")
    private String customerName;

    @ExcelProperty("单位")
    private String unit;

    @ExcelProperty("备注")
    private String remark;

    @ExcelProperty("标牌版本")
    private String signVersion;

    @ExcelProperty("对应基线")
    private String baseline;

    @ExcelProperty("位号")
    private String tagNumber;

    @ExcelProperty("细类")
    private String subcategory;

    @ExcelProperty("小类")
    private String smallSubcategory;

    @ExcelProperty("大类")
    private String category;

    @ExcelProperty("规格或图号")
    private String drawingNumber;
}
