package cn.iocoder.yudao.module.data.service.trainingassessment;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.data.controller.admin.trainingassessment.vo.*;
import cn.iocoder.yudao.module.data.dal.dataobject.trainingassessment.TrainingAssessmentDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 培训考核 Service 接口
 *
 * @author admin
 */
public interface TrainingAssessmentService {

    /**
     * 创建培训考核
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Integer createTrainingAssessment(@Valid TrainingAssessmentSaveReqVO createReqVO);

    /**
     * 更新培训考核
     *
     * @param updateReqVO 更新信息
     */
    void updateTrainingAssessment(@Valid TrainingAssessmentSaveReqVO updateReqVO);

    /**
     * 删除培训考核
     *
     * @param id 编号
     */
    void deleteTrainingAssessment(Integer id);

    /**
    * 批量删除培训考核
    *
    * @param ids 编号
    */
    void deleteTrainingAssessmentListByIds(List<Integer> ids);

    /**
     * 获得培训考核
     *
     * @param id 编号
     * @return 培训考核
     */
    TrainingAssessmentDO getTrainingAssessment(Integer id);

    /**
     * 获得培训考核分页
     *
     * @param pageReqVO 分页查询
     * @return 培训考核分页
     */
    PageResult<TrainingAssessmentDO> getTrainingAssessmentPage(TrainingAssessmentPageReqVO pageReqVO);

}