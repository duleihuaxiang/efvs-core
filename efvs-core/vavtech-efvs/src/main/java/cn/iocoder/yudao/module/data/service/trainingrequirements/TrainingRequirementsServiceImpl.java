package cn.iocoder.yudao.module.data.service.trainingrequirements;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.data.controller.admin.trainingrequirements.vo.*;
import cn.iocoder.yudao.module.data.dal.dataobject.trainingrequirements.TrainingRequirementsDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.data.dal.mysql.trainingrequirements.TrainingRequirementsMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.diffList;
import static cn.iocoder.yudao.module.data.enums.ErrorCodeConstants.*;

/**
 * 培训需求 Service 实现类
 *
 * @author admin
 */
@Service
@Validated
public class TrainingRequirementsServiceImpl implements TrainingRequirementsService {

    @Resource
    private TrainingRequirementsMapper trainingRequirementsMapper;

    @Override
    public Integer createTrainingRequirements(TrainingRequirementsSaveReqVO createReqVO) {
        // 插入
        TrainingRequirementsDO trainingRequirements = BeanUtils.toBean(createReqVO, TrainingRequirementsDO.class);
        trainingRequirementsMapper.insert(trainingRequirements);

        // 返回
        return trainingRequirements.getId();
    }

    @Override
    public void updateTrainingRequirements(TrainingRequirementsSaveReqVO updateReqVO) {
        // 校验存在
        validateTrainingRequirementsExists(updateReqVO.getId());
        // 更新
        TrainingRequirementsDO updateObj = BeanUtils.toBean(updateReqVO, TrainingRequirementsDO.class);
        trainingRequirementsMapper.updateById(updateObj);
    }

    @Override
    public void deleteTrainingRequirements(Integer id) {
        // 校验存在
        validateTrainingRequirementsExists(id);
        // 删除
        trainingRequirementsMapper.deleteById(id);
    }

    @Override
    public void deleteTrainingRequirementsListByIds(List<Integer> ids) {
        // 删除
        trainingRequirementsMapper.deleteByIds(ids);
        }

    private void validateTrainingRequirementsExists(Integer id) {
        if (trainingRequirementsMapper.selectById(id) == null) {
            throw exception(TRAINING_REQUIREMENTS_NOT_EXISTS);
        }
    }

    @Override
    public TrainingRequirementsDO getTrainingRequirements(Integer id) {
        return trainingRequirementsMapper.selectById(id);
    }

    @Override
    public PageResult<TrainingRequirementsDO> getTrainingRequirementsPage(TrainingRequirementsPageReqVO pageReqVO) {
        return trainingRequirementsMapper.selectPage(pageReqVO);
    }

}
