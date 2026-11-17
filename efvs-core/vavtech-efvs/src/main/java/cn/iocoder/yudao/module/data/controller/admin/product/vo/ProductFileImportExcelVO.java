package cn.iocoder.yudao.module.data.controller.admin.product.vo;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 产品档案导入 Excel VO（表头与模板一致）
 */
@Data
public class ProductFileImportExcelVO {

    private Integer excelRowNum;

    private Boolean skipped;

    private String skipReason;

    @ExcelProperty("Document Number")
    private String documentNumber;

    @ExcelProperty("Revision")
    private String revision;

    @ExcelProperty("客户名称")
    private String customerName;

    @ExcelProperty("客户编号")
    private String customerNumber;

    @ExcelProperty("产品名称")
    private String name;

    @ExcelProperty("产品件号")
    private String partNumber;

    @ExcelProperty("生产编号")
    private String productionNumber;

    @ExcelProperty("序列号")
    private String sequenceNumber;

    @ExcelProperty("标牌版本")
    private String signVersion;

    @ExcelProperty("对应基线")
    private String baseline;

    @ExcelProperty("零部件图号")
    private String drawingNumber;

    @ExcelProperty("图纸版本")
    private String drawingVersion;

    @ExcelProperty("零部件名称")
    private String partName;

    @ExcelProperty("交付去向")
    private String deliveryDestination;

    @ExcelProperty("交付时间")
    private String deliveryTime;

    @ExcelProperty("备注")
    private String remark;
}

