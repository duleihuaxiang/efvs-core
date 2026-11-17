package cn.iocoder.yudao.module.data.service.pretrainingpreparation;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.data.controller.admin.pretrainingpreparation.vo.*;
import cn.iocoder.yudao.module.data.dal.dataobject.pretrainingpreparation.PreTrainingPreparationDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 培训前准备 Service 接口
 *
 * @author admin
 */
public interface PreTrainingPreparationService {

    /**
     * 创建培训前准备
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Integer createPreTrainingPreparation(@Valid PreTrainingPreparationSaveReqVO createReqVO);

    /**
     * 更新培训前准备
     *
     * @param updateReqVO 更新信息
     */
    void updatePreTrainingPreparation(@Valid PreTrainingPreparationSaveReqVO updateReqVO);

    /**
     * 删除培训前准备
     *
     * @param id 编号
     */
    void deletePreTrainingPreparation(Integer id);

    /**
    * 批量删除培训前准备
    *
    * @param ids 编号
    */
    void deletePreTrainingPreparationListByIds(List<Integer> ids);

    /**
     * 获得培训前准备
     *
     * @param id 编号
     * @return 培训前准备
     */
    PreTrainingPreparationDO getPreTrainingPreparation(Integer id);

    /**
     * 获得培训前准备分页
     *
     * @param pageReqVO 分页查询
     * @return 培训前准备分页
     */
    PageResult<PreTrainingPreparationDO> getPreTrainingPreparationPage(PreTrainingPreparationPageReqVO pageReqVO);

    void createPreTrainingPreparationBatch(@Valid List<PreTrainingPreparationSaveReqVO> createReqVOList);

    List<PreTrainingPreparationDO> getPreTrainingPreparationByProcessInstanceId(String processInstanceId);
}