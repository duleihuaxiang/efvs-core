package cn.iocoder.yudao.module.data.controller.admin.customerinfo.vo;

import cn.idev.excel.annotation.write.style.ColumnWidth;
import lombok.*;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Data
@ExcelIgnoreUnannotated
public class CustomerInfoRespVO {

    private Integer id;

    @ExcelProperty("客户名称")
    private String customerName;

    @ExcelProperty("客户编号")
    private String customerId;

    @ExcelProperty("客户联系人")
    private String customerContactPerson;

    @ExcelProperty("联系人电话")
    private Long phone;

    @ExcelProperty("所属部门")
    private String dept;

    @ExcelProperty("邮箱地址")
    private String email;

    @ExcelProperty("客户地址（省）")
    private String province;

    @ExcelProperty("客户地址（省）id")
    private Integer provinceId;

    @ExcelProperty("客户地址（市）")
    private String city;

    @ExcelProperty("客户地址（市）id")
    private Integer cityId;

    @ExcelProperty("客户地址（区）")
    private String district;

    @ExcelProperty("客户地址（区）id")
    private Integer districtId;

    @ExcelProperty("邮编")
    @ColumnWidth(10)
    private Integer postalCode;

    @ExcelProperty("详细地址")
    private String address;

    @ExcelProperty("数据获取渠道")
    private String dataSource;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @ExcelProperty("创建人")
    private String creator;

    @ExcelProperty("创建人名称")
    private String createName;

    @ExcelProperty("修改时间")
    private LocalDateTime updateTime;

    @ExcelProperty("修改人")
    private String updater;

    @ExcelProperty("修改人名称")
    private String updateName;

    @ExcelProperty("备注")
    private String remark;

}
