package cn.iocoder.yudao.module.data.service.trainingmanagement;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.data.controller.admin.trainingmanagement.vo.*;
import cn.iocoder.yudao.module.data.dal.dataobject.trainingmanagement.TrainingManagementDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 培训管理 Service 接口
 *
 * @author admin
 */
public interface TrainingManagementService {

    /**
     * 创建培训管理
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Integer createTrainingManagement(@Valid TrainingManagementSaveReqVO createReqVO);

    /**
     * 更新培训管理
     *
     * @param updateReqVO 更新信息
     */
    void updateTrainingManagement(@Valid TrainingManagementSaveReqVO updateReqVO);

    /**
     * 删除培训管理
     *
     * @param id 编号
     */
    void deleteTrainingManagement(Integer id);

    /**
    * 批量删除培训管理
    *
    * @param ids 编号
    */
    void deleteTrainingManagementListByIds(List<Integer> ids);

    /**
     * 获得培训管理
     *
     * @param id 编号
     * @return 培训管理
     */
    TrainingManagementDO getTrainingManagement(Integer id);

    /**
     * 获得培训管理分页
     *
     * @param pageReqVO 分页查询
     * @return 培训管理分页
     */
    PageResult<TrainingManagementDO> getTrainingManagementPage(TrainingManagementPageReqVO pageReqVO);

}