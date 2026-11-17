package cn.iocoder.yudao.module.data.service.trainingattendance;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.data.controller.admin.trainingattendance.vo.*;
import cn.iocoder.yudao.module.data.dal.dataobject.trainingattendance.TrainingAttendanceDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.data.dal.mysql.trainingattendance.TrainingAttendanceMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.diffList;
import static cn.iocoder.yudao.module.data.enums.ErrorCodeConstants.*;

/**
 * 培训签到 Service 实现类
 *
 * @author admin
 */
@Service
@Validated
public class TrainingAttendanceServiceImpl implements TrainingAttendanceService {

    @Resource
    private TrainingAttendanceMapper trainingAttendanceMapper;

    @Override
    public Integer createTrainingAttendance(TrainingAttendanceSaveReqVO createReqVO) {
        // 插入
        TrainingAttendanceDO trainingAttendance = BeanUtils.toBean(createReqVO, TrainingAttendanceDO.class);
        trainingAttendanceMapper.insert(trainingAttendance);

        // 返回
        return trainingAttendance.getId();
    }

    @Override
    public void updateTrainingAttendance(TrainingAttendanceSaveReqVO updateReqVO) {
        // 校验存在
        validateTrainingAttendanceExists(updateReqVO.getId());
        // 更新
        TrainingAttendanceDO updateObj = BeanUtils.toBean(updateReqVO, TrainingAttendanceDO.class);
        trainingAttendanceMapper.updateById(updateObj);
    }

    @Override
    public void deleteTrainingAttendance(Integer id) {
        // 校验存在
        validateTrainingAttendanceExists(id);
        // 删除
        trainingAttendanceMapper.deleteById(id);
    }

    @Override
    public void deleteTrainingAttendanceListByIds(List<Integer> ids) {
        // 删除
        trainingAttendanceMapper.deleteByIds(ids);
        }

    private void validateTrainingAttendanceExists(Integer id) {
        if (trainingAttendanceMapper.selectById(id) == null) {
            throw exception(TRAINING_ATTENDANCE_NOT_EXISTS);
        }
    }

    @Override
    public TrainingAttendanceDO getTrainingAttendance(Integer id) {
        return trainingAttendanceMapper.selectById(id);
    }

    @Override
    public PageResult<TrainingAttendanceDO> getTrainingAttendancePage(TrainingAttendancePageReqVO pageReqVO) {
        return trainingAttendanceMapper.selectPage(pageReqVO);
    }

}
