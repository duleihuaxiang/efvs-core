package cn.iocoder.yudao.module.data.controller.admin.trainingplan.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import lombok.Data;

import java.util.List;

@Data
@ExcelIgnoreUnannotated
public class TrainingPlanVO {
    private Integer id;
    private String nickName;
}