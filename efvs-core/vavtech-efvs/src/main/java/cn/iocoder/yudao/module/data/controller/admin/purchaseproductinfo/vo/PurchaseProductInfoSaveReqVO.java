package cn.iocoder.yudao.module.data.controller.admin.purchaseproductinfo.vo;

import lombok.*;
import java.util.*;
import javax.validation.constraints.*;

@Data
public class PurchaseProductInfoSaveReqVO {

    private Integer id;

    private String customerId;

    private String productCategory;

    private String productName;

    private String productModel;

    private String productSerialNum;

    private Integer productQuantity;

    private String remark;

}
