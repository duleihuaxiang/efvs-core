package cn.iocoder.yudao.module.data.controller.admin.productfile.vo;

import lombok.*;
import java.util.*;
import javax.validation.constraints.*;

@Data
public class ProductFileSaveReqVO {

    private Integer id;

    private String partNumber;

    private String sequenceNumber;

    private String name;

    private String drawingNumber;

    private String drawingVersion;

    private String productionNumber;

    private String baseline;

    private String customerNumber;

    private String customerName;

    @NotEmpty(message = "备注不能为空")
    private String remark;

}
