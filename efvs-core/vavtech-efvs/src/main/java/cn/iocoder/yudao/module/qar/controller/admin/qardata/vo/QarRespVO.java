package cn.iocoder.yudao.module.qar.controller.admin.qardata.vo;

import lombok.*;

import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Data
@ExcelIgnoreUnannotated
public class QarRespVO {

    @ExcelProperty("id")
    private Long id;

    @ExcelProperty("航司")
    private String airline;

    @ExcelProperty("机型")
    private String model;

    @ExcelProperty("efvs构型")
    private String efvsConfiguration;

    @ExcelProperty("文件名称")
    private String name;

//    @ExcelProperty("文件地址")
    private String fileUrl;

    @ExcelProperty("文件大小(kb)")
    private String size;

    @ExcelProperty("文件来源")
    private String fileSource;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @ExcelProperty("创建人名称")
    private String createName;

    @ExcelProperty("备注")
    private String remark;

}
