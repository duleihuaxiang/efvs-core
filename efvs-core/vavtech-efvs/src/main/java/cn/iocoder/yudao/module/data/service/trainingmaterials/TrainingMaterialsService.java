package cn.iocoder.yudao.module.data.service.trainingmaterials;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.data.controller.admin.trainingmaterials.vo.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.data.dal.dataobject.trainingmaterials.TrainingMaterialsDO;

/**
 * 培训资料管理 Service 接口
 *
 * @author admin
 */
public interface TrainingMaterialsService {

    /**
     * 创建培训资料管理
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createTrainingMaterials(@Valid TrainingMaterialsSaveReqVO createReqVO);

    /**
     * 更新培训资料管理
     *
     * @param updateReqVO 更新信息
     */
    void updateTrainingMaterials(@Valid TrainingMaterialsSaveReqVO updateReqVO);

    /**
     * 删除培训资料管理
     *
     * @param id 编号
     */
    void deleteTrainingMaterials(Integer id);

    /**
    * 批量删除培训资料管理
    *
    * @param ids 编号
    */
    void deleteTrainingMaterialsListByIds(List<Integer> ids);

    /**
     * 获得培训资料管理
     *
     * @param id 编号
     * @return 培训资料管理
     */
    TrainingMaterialsRespVO getTrainingMaterials(Integer id);

    /**
     * 获得培训资料管理分页
     *
     * @param pageReqVO 分页查询
     * @return 培训资料管理分页
     */
    PageResult<TrainingMaterialsDO> getTrainingMaterialsPage(TrainingMaterialsPageReqVO pageReqVO);

}