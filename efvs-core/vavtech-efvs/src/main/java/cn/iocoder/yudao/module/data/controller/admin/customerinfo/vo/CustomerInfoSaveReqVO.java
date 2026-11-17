package cn.iocoder.yudao.module.data.controller.admin.customerinfo.vo;

import lombok.*;

@Data
public class CustomerInfoSaveReqVO {

    private Integer id;

    private String customerName;

    private String customerId;

    private String customerContactPerson;

    private Long phone;

    private String dept;

    private String email;

    private String province;

    private Integer provinceId;

    private String city;

    private Integer cityId;

    private String district;

    private Integer districtId;

    private Integer postalCode;

    private String address;

    private String creator;

    private String dataSource;

    private String remark;

}
