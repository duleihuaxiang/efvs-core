package cn.iocoder.yudao.module.data.service.trainingattendance;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.data.controller.admin.trainingattendance.vo.*;
import cn.iocoder.yudao.module.data.dal.dataobject.trainingattendance.TrainingAttendanceDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 培训签到 Service 接口
 *
 * @author admin
 */
public interface TrainingAttendanceService {

    /**
     * 创建培训签到
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Integer createTrainingAttendance(@Valid TrainingAttendanceSaveReqVO createReqVO);

    /**
     * 更新培训签到
     *
     * @param updateReqVO 更新信息
     */
    void updateTrainingAttendance(@Valid TrainingAttendanceSaveReqVO updateReqVO);

    /**
     * 删除培训签到
     *
     * @param id 编号
     */
    void deleteTrainingAttendance(Integer id);

    /**
    * 批量删除培训签到
    *
    * @param ids 编号
    */
    void deleteTrainingAttendanceListByIds(List<Integer> ids);

    /**
     * 获得培训签到
     *
     * @param id 编号
     * @return 培训签到
     */
    TrainingAttendanceDO getTrainingAttendance(Integer id);

    /**
     * 获得培训签到分页
     *
     * @param pageReqVO 分页查询
     * @return 培训签到分页
     */
    PageResult<TrainingAttendanceDO> getTrainingAttendancePage(TrainingAttendancePageReqVO pageReqVO);

}