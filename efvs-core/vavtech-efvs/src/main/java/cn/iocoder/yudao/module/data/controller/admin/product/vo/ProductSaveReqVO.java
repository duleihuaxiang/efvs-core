package cn.iocoder.yudao.module.data.controller.admin.product.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;

@Data
public class ProductSaveReqVO {

    private Integer id;

    private String pmn;

    private String name;

    private String partNumber;

    private String sequenceNumber;

    private String customerNumber;

    private String customerName;

    private String remark;

    private String signVersion;

    private String baseline;

    private String deliveryDestination;

    private String deliveryTime;

}
