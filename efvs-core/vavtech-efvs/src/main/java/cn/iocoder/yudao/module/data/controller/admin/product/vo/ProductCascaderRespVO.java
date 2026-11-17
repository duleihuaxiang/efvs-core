package cn.iocoder.yudao.module.data.controller.admin.product.vo;
import lombok.Data;

@Data
public class ProductCascaderRespVO {
    private Long id;
    private String partNumber;        // 本产品件号
    private String productPartNumber; // 指向父级的件号
    private String name;
    private Integer level;
}

