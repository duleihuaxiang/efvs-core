package cn.iocoder.yudao.module.data.controller.admin.customerinfo.vo;
import lombok.Data;

import java.util.Map;

/**
 * 省份客户统计 VO
 * 用于前端展示省/市/区三级客户数量统计
 */
@Data
public class ProvinceCustomerCountVO {

    private Integer provinceId;

    private String provinceName;

    private Long provinceCustomerCount;

    private Map<Integer, Long> cityCustomerCountMap;

    private Map<Integer, Long> districtCustomerCountMap;

}
