package cn.iocoder.yudao.module.data.controller.admin.customerinfo.vo;

import lombok.*;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

@Data
public class CustomerInfoPageReqVO extends PageParam {

    private String customerName;

    private String customerId;

}
