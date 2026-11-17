package cn.iocoder.yudao.module.data.service.trainingrequirements;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.data.controller.admin.trainingrequirements.vo.*;
import cn.iocoder.yudao.module.data.dal.dataobject.trainingrequirements.TrainingRequirementsDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 培训需求 Service 接口
 *
 * @author admin
 */
public interface TrainingRequirementsService {

    /**
     * 创建培训需求
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Integer createTrainingRequirements(@Valid TrainingRequirementsSaveReqVO createReqVO);

    /**
     * 更新培训需求
     *
     * @param updateReqVO 更新信息
     */
    void updateTrainingRequirements(@Valid TrainingRequirementsSaveReqVO updateReqVO);

    /**
     * 删除培训需求
     *
     * @param id 编号
     */
    void deleteTrainingRequirements(Integer id);

    /**
    * 批量删除培训需求
    *
    * @param ids 编号
    */
    void deleteTrainingRequirementsListByIds(List<Integer> ids);

    /**
     * 获得培训需求
     *
     * @param id 编号
     * @return 培训需求
     */
    TrainingRequirementsDO getTrainingRequirements(Integer id);

    /**
     * 获得培训需求分页
     *
     * @param pageReqVO 分页查询
     * @return 培训需求分页
     */
    PageResult<TrainingRequirementsDO> getTrainingRequirementsPage(TrainingRequirementsPageReqVO pageReqVO);

}