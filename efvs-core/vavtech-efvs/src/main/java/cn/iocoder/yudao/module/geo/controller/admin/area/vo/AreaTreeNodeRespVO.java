package cn.iocoder.yudao.module.geo.controller.admin.area.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AreaTreeNodeRespVO {

    private Integer id;

    private String name;

    private Integer type;

    private Integer parentId;

    private List<AreaTreeNodeRespVO> children;

    private LocalDateTime createTime;

}
