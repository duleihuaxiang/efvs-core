package cn.iocoder.yudao.module.data.service.trainingrecord;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.data.controller.admin.trainingrecord.vo.*;
import cn.iocoder.yudao.module.data.dal.dataobject.trainingrecord.TrainingRecordDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 培训记录 Service 接口
 *
 * @author admin
 */
public interface TrainingRecordService {

    /**
     * 创建培训记录
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Integer createTrainingRecord(@Valid TrainingRecordSaveReqVO createReqVO);

    /**
     * 更新培训记录
     *
     * @param updateReqVO 更新信息
     */
    void updateTrainingRecord(@Valid TrainingRecordSaveReqVO updateReqVO);

    /**
     * 删除培训记录
     *
     * @param id 编号
     */
    void deleteTrainingRecord(Integer id);

    /**
    * 批量删除培训记录
    *
    * @param ids 编号
    */
    void deleteTrainingRecordListByIds(List<Integer> ids);

    /**
     * 获得培训记录
     *
     * @param id 编号
     * @return 培训记录
     */
    TrainingRecordDO getTrainingRecord(Integer id);

    /**
     * 获得培训记录分页
     *
     * @param pageReqVO 分页查询
     * @return 培训记录分页
     */
    PageResult<TrainingRecordDO> getTrainingRecordPage(TrainingRecordPageReqVO pageReqVO);

}