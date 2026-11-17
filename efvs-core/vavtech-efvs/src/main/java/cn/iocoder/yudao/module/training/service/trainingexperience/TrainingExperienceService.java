package cn.iocoder.yudao.module.training.service.trainingexperience;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.training.controller.admin.trainingexperience.vo.*;
import cn.iocoder.yudao.module.training.dal.dataobject.trainingexperience.TrainingExperienceDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 培训经历 Service 接口
 *
 * @author admin
 */
public interface TrainingExperienceService {

    /**
     * 创建培训经历
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    List<Long> createExperience(@Valid TrainingExperienceSaveReqVO createReqVO,Integer userId);

    /**
     * 更新培训经历
     *
     * @param updateReqVO 更新信息
     */
    void updateExperience(@Valid TrainingExperienceSaveReqVO updateReqVO);

    /**
     * 删除培训经历
     *
     * @param id 编号
     */
    void deleteExperience(Integer id);

    /**
    * 批量删除培训经历
    *
    * @param ids 编号
    */
    void deleteExperienceListByIds(List<Integer> ids);

    /**
     * 获得培训经历
     *
     * @param id 编号
     * @return 培训经历
     */
    TrainingExperienceRespVO getExperience(Integer id);

    /**
     * 获得培训经历分页
     *
     * @param pageReqVO 分页查询
     * @return 培训经历分页
     */
    PageResult<TrainingExperienceDO> getExperiencePage(TrainingExperiencePageReqVO pageReqVO, Integer userId);

}