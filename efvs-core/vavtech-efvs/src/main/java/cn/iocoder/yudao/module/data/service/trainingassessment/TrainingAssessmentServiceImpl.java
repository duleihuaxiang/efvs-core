package cn.iocoder.yudao.module.data.service.trainingassessment;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.data.controller.admin.trainingassessment.vo.*;
import cn.iocoder.yudao.module.data.dal.dataobject.trainingassessment.TrainingAssessmentDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.data.dal.mysql.trainingassessment.TrainingAssessmentMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.diffList;
import static cn.iocoder.yudao.module.data.enums.ErrorCodeConstants.*;

/**
 * 培训考核 Service 实现类
 *
 * @author admin
 */
@Service
@Validated
public class TrainingAssessmentServiceImpl implements TrainingAssessmentService {

    @Resource
    private TrainingAssessmentMapper trainingAssessmentMapper;

    @Override
    public Integer createTrainingAssessment(TrainingAssessmentSaveReqVO createReqVO) {
        // 插入
        TrainingAssessmentDO trainingAssessment = BeanUtils.toBean(createReqVO, TrainingAssessmentDO.class);
        trainingAssessmentMapper.insert(trainingAssessment);

        // 返回
        return trainingAssessment.getId();
    }

    @Override
    public void updateTrainingAssessment(TrainingAssessmentSaveReqVO updateReqVO) {
        // 校验存在
        validateTrainingAssessmentExists(updateReqVO.getId());
        // 更新
        TrainingAssessmentDO updateObj = BeanUtils.toBean(updateReqVO, TrainingAssessmentDO.class);
        trainingAssessmentMapper.updateById(updateObj);
    }

    @Override
    public void deleteTrainingAssessment(Integer id) {
        // 校验存在
        validateTrainingAssessmentExists(id);
        // 删除
        trainingAssessmentMapper.deleteById(id);
    }

    @Override
    public void deleteTrainingAssessmentListByIds(List<Integer> ids) {
        // 删除
        trainingAssessmentMapper.deleteByIds(ids);
        }

    private void validateTrainingAssessmentExists(Integer id) {
        if (trainingAssessmentMapper.selectById(id) == null) {
            throw exception(TRAINING_ASSESSMENT_NOT_EXISTS);
        }
    }

    @Override
    public TrainingAssessmentDO getTrainingAssessment(Integer id) {
        return trainingAssessmentMapper.selectById(id);
    }

    @Override
    public PageResult<TrainingAssessmentDO> getTrainingAssessmentPage(TrainingAssessmentPageReqVO pageReqVO) {
        return trainingAssessmentMapper.selectPage(pageReqVO);
    }

}
