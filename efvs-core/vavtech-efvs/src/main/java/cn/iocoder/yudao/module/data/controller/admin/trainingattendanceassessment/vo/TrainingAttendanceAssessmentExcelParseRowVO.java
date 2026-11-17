package cn.iocoder.yudao.module.data.controller.admin.trainingattendanceassessment.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 培训签到考核 Excel 解析结果（JSON 字段名与表头一致，便于前端直接使用）
 */
@Data
public class TrainingAttendanceAssessmentExcelParseRowVO {

    private String name;

    private String checkInTime;

    private Integer learningTime;

    private Integer isCompleted;

    private Integer completedForm;

    private Integer score;

    private String traningAttachments;

    private String completionDate;

    private String remark;
}
