package cn.iocoder.yudao.module.training.controller.admin.trainingexperience.vo;

import cn.iocoder.yudao.module.training.dal.dataobject.trainingexperience.FileUploadDO;
import lombok.*;
import java.util.*;

import org.springframework.data.annotation.Transient;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Data
@ExcelIgnoreUnannotated
public class TrainingExperienceRespVO {

    @ExcelProperty("ID")
    private Integer id;

    @ExcelProperty("用户ID")
    private Integer userId;

    @ExcelProperty("培训主题")
    private String trainingTheme;

    @ExcelProperty("培训时间")
    private LocalDateTime trainingTime;

    @ExcelProperty("培训地点")
    private String trainingLocation;

    @ExcelProperty("培训时长")
    private Long trainingDuration;

    @ExcelProperty("培训成绩")
    private String achieveResults;

    @ExcelProperty("培训教师")
    private String trainingTeachers;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @ExcelProperty("备注")
    private String remark;

    private String nickName;
//    /**
//     * 文件路径
//     */
//    @Transient
//    private String fileUrl;
//    /**
//     * 文件名称
//     */
//    @Transient
//    private String name;
//    /**
//     * 文件ID
//     */
//    @Transient
//    private Integer fileId;
//
//    /**
//     * 文件大小
//     */
//    @Transient
//    private String size;

    /**
     * 该培训对应的所有附件（数组/集合）
     */
    private List<FileUploadDO> fileList;

}
