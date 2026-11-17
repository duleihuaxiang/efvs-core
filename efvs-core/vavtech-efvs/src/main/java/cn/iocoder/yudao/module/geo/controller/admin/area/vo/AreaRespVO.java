package cn.iocoder.yudao.module.geo.controller.admin.area.vo;

import lombok.*;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Data
@ExcelIgnoreUnannotated
public class AreaRespVO {

    @ExcelProperty("地区的唯一标识符（ID）")
    private Integer id;

    @ExcelProperty("地区的名称")
    private String name;

    @ExcelProperty("地区的类型1 表示国家（例如中国）2 表示省份或直辖市（例如北京市、天津市）3 表示地级市（例如石家庄市）4 表示区/县（例如长安区）")
    private Integer type;

    @ExcelProperty("该地区的父级地区 ID")
    private Integer parentId;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @ExcelProperty("备注")
    private String remark;

}
