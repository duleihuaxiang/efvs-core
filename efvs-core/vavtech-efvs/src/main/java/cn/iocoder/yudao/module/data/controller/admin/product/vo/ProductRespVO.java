package cn.iocoder.yudao.module.data.controller.admin.product.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;
import cn.idev.excel.annotation.*;

@Data
@ExcelIgnoreUnannotated
public class ProductRespVO {

    @ExcelProperty("商品编号")
    private Integer id;

    @ExcelProperty("名称")
    private String name;

    @ExcelProperty("产品件号")
    private String partNumber;

    @ExcelIgnore
    private String creator;

    @ExcelProperty("备注")
    private String remark;

    @ExcelIgnore
    private List<ProductArchiveConstructionRespVO> productFileList;

    private String sequenceNumber;

    private String customerNumber;

    private String customerName;

    private String signVersion;

    private String baseline;
    /**
     * 文档编号
     */
    private String documentNumber;
    /**
     * 版次/Revision
     */
    private String revision;

    @ExcelProperty("交付去向")
    private String deliveryDestination;
    @ExcelProperty("交付时间")
    private String deliveryTime;

    /**
     * 创建者名称
     */
    @ExcelProperty("创建人")
    private String createName;
    /**
     * 安装时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime installationDate;

}
