package cn.iocoder.yudao.module.qar.controller.admin.qardata.vo;

import lombok.*;

import javax.validation.constraints.*;
import java.util.List;

@Data
public class QarSaveReqVO {

    private Long id;

    @NotEmpty(message = "航司不能为空")
    private String airline;

    @NotEmpty(message = "机型不能为空")
    private String model;

    @NotEmpty(message = "efvs构型不能为空")
    private String efvsConfiguration;

    @NotEmpty(message = "文件列表不能为空")
    private List<fileVO> files;

    @NotEmpty(message = "文件来源不能为空")
    private String fileSource;

    private String remark;

    private String flag;

}

