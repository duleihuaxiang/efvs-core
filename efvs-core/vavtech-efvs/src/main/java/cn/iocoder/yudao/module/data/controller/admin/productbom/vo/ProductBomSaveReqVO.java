package cn.iocoder.yudao.module.data.controller.admin.productbom.vo;

import lombok.*;
import java.util.*;
import javax.validation.constraints.*;

@Data
public class ProductBomSaveReqVO {

    private Integer id;

    private String pmn;

    private Integer level;

    private String productVersion;

    private Integer productType;

    private String partNumber;

    private String name;

    private String productPartNumber;

    private Integer productNum;

    private String unit;

    private String tagNumber;

    private String subcategory;

    private String drawingNumber;

    private Integer category;

    private Integer smallSubcategory;

    private String remark;

}
