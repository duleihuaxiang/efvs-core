package cn.iocoder.yudao.module.form.controller.admin.externalform.vo;

import lombok.*;
import java.util.*;
import javax.validation.constraints.*;

@Data
public class ExternalFormSaveReqVO {

    private Long id;

    private String processId;

    private String activityId;

    private Integer formType;

    private String formUrl;

    private Integer formKey;

    private String remark;

}
