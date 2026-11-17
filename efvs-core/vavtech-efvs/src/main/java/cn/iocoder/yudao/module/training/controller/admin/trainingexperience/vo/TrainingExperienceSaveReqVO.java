package cn.iocoder.yudao.module.training.controller.admin.trainingexperience.vo;

import cn.iocoder.yudao.module.data.controller.admin.uploadzippreview.dto.FileUploadResult;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Data
public class TrainingExperienceSaveReqVO {

    private Integer id;

    private Integer userId;

    private String trainingTheme;

    private LocalDateTime trainingTime;

    private String trainingLocation;

    private Long trainingDuration;

    private String achieveResults;

    private String trainingTeachers;

    private String remark;

    private List<FileUploadResult>  fileUploadResults;

}
