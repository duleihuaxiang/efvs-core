package cn.iocoder.yudao.module.data.controller.admin.ppql.vo;

import lombok.*;
import java.util.*;
import javax.validation.constraints.*;

@Data
public class PpqlSaveReqVO {

    private Integer id;

    private String problemDescription;

    private String remark;

}
