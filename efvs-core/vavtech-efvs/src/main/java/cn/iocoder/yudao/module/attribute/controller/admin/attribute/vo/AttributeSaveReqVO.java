package cn.iocoder.yudao.module.attribute.controller.admin.attribute.vo;

import lombok.*;
import java.util.*;
import javax.validation.constraints.*;

@Data
public class AttributeSaveReqVO {

    private Integer id;

    @NotNull(message = "产品id不能为空")
    private Integer productId;

    private String designAttribute;

    private String guaranteeAttribute;

    private String supplyAttribute;

    private String modelAttribute;

    private String faultAttribute;

}
