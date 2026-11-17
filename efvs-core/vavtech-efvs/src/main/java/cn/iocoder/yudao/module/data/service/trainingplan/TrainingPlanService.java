package cn.iocoder.yudao.module.data.service.trainingplan;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.data.controller.admin.trainingplan.vo.*;
import cn.iocoder.yudao.module.data.dal.dataobject.trainingplan.TrainingPlanDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 培训计划 Service 接口
 *
 * @author admin
 */
public interface TrainingPlanService {

    /**
     * 创建培训计划
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Integer createTrainingPlan(@Valid TrainingPlanSaveReqVO createReqVO);

    /**
     * 更新培训计划
     *
     * @param updateReqVO 更新信息
     */
    void updateTrainingPlan(@Valid TrainingPlanSaveReqVO updateReqVO);

    /**
     * 删除培训计划
     *
     * @param id 编号
     */
    void deleteTrainingPlan(Integer id);

    /**
    * 批量删除培训计划
    *
    * @param ids 编号
    */
    void deleteTrainingPlanListByIds(List<Integer> ids);

    /**
     * 获得培训计划
     *
     * @param id 编号
     * @return 培训计划
     */
    TrainingPlanDO getTrainingPlan(Integer id);

    /**
     * 获得培训计划分页
     *
     * @param pageReqVO 分页查询
     * @return 培训计划分页
     */
    PageResult<TrainingPlanDO> getTrainingPlanPage(TrainingPlanPageReqVO pageReqVO);

    TrainingPlanDO getTrainingPlanByProcessInstanceId(String processInstanceId);

    Map<Integer,String> gettrainingByid(List<Integer> ids);

    List<TrainingPlanDO> getTrainingPlansByProcessInstanceIds(Set<String> processInstanceIds);

    Map<Integer, String> getUserNickNamesByIds(List<Integer> leaderIds);
}