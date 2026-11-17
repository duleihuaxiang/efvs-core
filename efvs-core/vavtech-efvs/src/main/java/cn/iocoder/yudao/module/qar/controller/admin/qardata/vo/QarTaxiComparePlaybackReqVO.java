package cn.iocoder.yudao.module.qar.controller.admin.qardata.vo;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class QarTaxiComparePlaybackReqVO {

    @NotEmpty(message = "ids 不能为空")
    @Size(max = 20, message = "最多 20 个架次")
    private List<Long> ids;

}

