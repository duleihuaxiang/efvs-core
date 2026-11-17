package cn.iocoder.yudao.module.qar.controller.admin.qardata.vo;

import lombok.*;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

@Data
public class QarDataPageReqVO extends PageParam {

    private String emptySpaceSwitch;

    private String pressureAltitude;

}
