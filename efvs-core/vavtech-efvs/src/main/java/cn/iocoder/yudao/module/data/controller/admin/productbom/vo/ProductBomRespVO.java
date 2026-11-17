package cn.iocoder.yudao.module.data.controller.admin.productbom.vo;

import lombok.*;
import java.util.*;
import cn.idev.excel.annotation.*;

@Data
@ExcelIgnoreUnannotated
public class ProductBomRespVO {

    @ExcelProperty("产品BOMID")
    private Integer id;

    @ExcelProperty("PMN")
    private String pmn;

    @ExcelProperty("层级")
    private Integer level;

    @ExcelProperty("版本")
    private String productVersion;

    @ExcelProperty("类型")
    private Integer productType;

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
    private Integer category;

    @ExcelProperty("小类")
    private Integer smallSubcategory;

    @ExcelProperty("备注")
    private String remark;

    @ExcelIgnore
    private List<ProductBomRespVO> children;

}
