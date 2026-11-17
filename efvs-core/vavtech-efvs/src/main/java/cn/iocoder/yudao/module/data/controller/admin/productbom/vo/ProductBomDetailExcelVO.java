package cn.iocoder.yudao.module.data.controller.admin.productbom.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * BOM 导出 Excel 专用 VO（列表 / 详情）：类型、细类、大类、小类为中文，与 {@link ProductBomImportExcelVO} 导入模版一致。
 */
@Data
@ExcelIgnoreUnannotated
public class ProductBomDetailExcelVO {

    @ExcelProperty("产品BOMID")
    private Integer id;

    @ExcelProperty("PMN")
    private String pmn;

    @ExcelProperty("层级")
    private Integer level;

    @ExcelProperty("版本")
    private String productVersion;

    @ExcelProperty("类型")
    private String productType;

    @ExcelProperty("产品代号")
    private String partNumber;

    @ExcelProperty("名称")
    private String name;

    @ExcelProperty("所属产品件号")
    private String productPartNumber;

    @ExcelProperty("数量")
    private Integer productNum;

    @ExcelProperty("单位")
    private String unit;

    @ExcelProperty("位号")
    private String tagNumber;

    @ExcelProperty("细类")
    private String subcategory;

    @ExcelProperty("规格或图号")
    private String drawingNumber;

    @ExcelProperty("大类")
    private String category;

    @ExcelProperty("小类")
    private String smallSubcategory;

    @ExcelProperty("备注")
    private String remark;
}
