package cn.iocoder.yudao.module.data.controller.admin.purchaseproductinfo.vo;

import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Data
@ExcelIgnoreUnannotated
public class PurchaseProductInfoRespVO {

    @ExcelProperty("产品编号")
    private Integer id;

    @ExcelProperty("客户id")
    private String customerId;

    @ExcelProperty("产品大类")
    private String productCategory;

    @ExcelProperty("产品名称")
    private String productName;

    @ExcelProperty("产品型号")
    private String productModel;

    @ExcelProperty("产品序列号")
    private String productSerialNum;

    @ExcelProperty("产品数量")
    private Integer productQuantity;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @ExcelProperty("备注")
    private String remark;

}
