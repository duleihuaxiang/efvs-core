package cn.iocoder.yudao.module.data.service.trainingattendanceassessment;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.data.controller.admin.trainingattendanceassessment.vo.*;
import cn.iocoder.yudao.module.data.dal.dataobject.trainingattendanceassessment.TrainingAttendanceAssessmentDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.module.data.dal.dataobject.trainingplan.TrainingPlanDO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 培训签到考核证书 Service 接口
 *
 * @author admin
 */
public interface TrainingAttendanceAssessmentService {

    /**
     * 创建培训签到考核证书
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Integer createTrainingAttendanceAssessment(@Valid TrainingAttendanceAssessmentSaveReqVO createReqVO);

    /**
     * 更新培训签到考核证书
     *
     * @param updateReqVO 更新信息
     */
    void updateTrainingAttendanceAssessment(@Valid TrainingAttendanceAssessmentSaveReqVO updateReqVO);

    /**
     * 删除培训签到考核证书
     *
     * @param id 编号
     */
    void deleteTrainingAttendanceAssessment(Integer id);

    /**
    * 批量删除培训签到考核证书
    *
    * @param ids 编号
    */
    void deleteTrainingAttendanceAssessmentListByIds(List<Integer> ids);

    /**
     * 获得培训签到考核证书
     *
     * @param id 编号
     * @return 培训签到考核证书
     */
    TrainingAttendanceAssessmentDO getTrainingAttendanceAssessment(String id);

    /**
     * 获得培训签到考核证书分页
     *
     * @param pageReqVO 分页查询
     * @return 培训签到考核证书分页
     */
    PageResult<TrainingAttendanceAssessmentDO> getTrainingAttendanceAssessmentPage(TrainingAttendanceAssessmentPageReqVO pageReqVO);

    List<TrainingAttendanceAssessmentDO> getTrainingAttendanceAssessmentByProcessInstanceId(String processInstanceId);

    List<TrainingAttendanceAssessmentDO> getTrainingAttendanceAssessmentByIds(String processInstanceId);

    List<TrainingPlanDO> getTrainingPlansByProcessInstanceIds(Collection<String> processInstanceIds);

    /**
     * 解析培训签到考核 Excel（首行为表头，自第二行起为数据；日期/时间格式由 Excel 显示值统一格式化）
     *
     * @param file Excel 文件
     * @return 解析后的行列表
     */
    List<TrainingAttendanceAssessmentExcelParseRowVO> parseTrainingAttendanceExcel(MultipartFile file);
}