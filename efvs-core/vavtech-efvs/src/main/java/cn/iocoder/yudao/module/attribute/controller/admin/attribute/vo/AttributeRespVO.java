package cn.iocoder.yudao.module.attribute.controller.admin.attribute.vo;

import lombok.*;
import java.util.*;
import cn.idev.excel.annotation.*;

@Data
@ExcelIgnoreUnannotated
public class AttributeRespVO {

    @ExcelProperty("属性id")
    private Integer id;

    @ExcelProperty("产品id")
    private Integer productId;

    @ExcelProperty("设计属性")
    private String designAttribute;

    @ExcelProperty("保障属性")
    private String guaranteeAttribute;

    @ExcelProperty("供应属性")
    private String supplyAttribute;

    @ExcelProperty("机型属性")
    private String modelAttribute;

    @ExcelProperty("保障属性")
    private String faultAttribute;

}
