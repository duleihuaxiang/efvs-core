package cn.iocoder.yudao.module.geo.controller.admin.area.vo;

import lombok.*;

@Data
public class AreaListReqVO {

    private Integer id;

    private String name;

    private Integer type;

    private Integer parentId;

}
