package cn.iocoder.yudao.module.data.service.trainingmanagement;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import cn.iocoder.yudao.module.data.controller.admin.trainingmanagement.vo.*;
import cn.iocoder.yudao.module.data.dal.dataobject.trainingmanagement.TrainingManagementDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.data.dal.mysql.trainingmanagement.TrainingManagementMapper;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.data.enums.ErrorCodeConstants.*;

/**
 * 培训管理 Service 实现类
 *
 * @author admin
 */
@Service
@Validated
public class TrainingManagementServiceImpl implements TrainingManagementService {

    @Resource
    private TrainingManagementMapper trainingManagementMapper;

    @Override
    public Integer createTrainingManagement(TrainingManagementSaveReqVO createReqVO) {
        // 插入
        TrainingManagementDO trainingManagement = BeanUtils.toBean(createReqVO, TrainingManagementDO.class);
        trainingManagementMapper.insert(trainingManagement);

        // 返回
        return trainingManagement.getId();
    }

    @Override
    public void updateTrainingManagement(TrainingManagementSaveReqVO updateReqVO) {
        // 校验存在
        validateTrainingManagementExists(updateReqVO.getId());
        // 更新
        TrainingManagementDO updateObj = BeanUtils.toBean(updateReqVO, TrainingManagementDO.class);
        trainingManagementMapper.updateById(updateObj);
    }

    @Override
    public void deleteTrainingManagement(Integer id) {
        // 校验存在
        validateTrainingManagementExists(id);
        // 删除
        trainingManagementMapper.deleteById(id);
    }

    @Override
    public void deleteTrainingManagementListByIds(List<Integer> ids) {
        // 删除
        trainingManagementMapper.deleteByIds(ids);
        }

    private void validateTrainingManagementExists(Integer id) {
        if (trainingManagementMapper.selectById(id) == null) {
            throw exception(TRAINING_MANAGEMENT_NOT_EXISTS);
        }
    }

    @Override
    public TrainingManagementDO getTrainingManagement(Integer id) {
        return trainingManagementMapper.selectById(id);
    }

    @Override
    public PageResult<TrainingManagementDO> getTrainingManagementPage(TrainingManagementPageReqVO pageReqVO) {
        return trainingManagementMapper.selectPage(pageReqVO);
    }

}
