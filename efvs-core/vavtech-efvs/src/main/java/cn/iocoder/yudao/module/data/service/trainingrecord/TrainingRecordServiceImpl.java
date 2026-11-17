package cn.iocoder.yudao.module.data.service.trainingrecord;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.data.controller.admin.trainingrecord.vo.*;
import cn.iocoder.yudao.module.data.dal.dataobject.trainingrecord.TrainingRecordDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.data.dal.mysql.trainingrecord.TrainingRecordMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.diffList;
import static cn.iocoder.yudao.module.data.enums.ErrorCodeConstants.*;

/**
 * 培训记录 Service 实现类
 *
 * @author admin
 */
@Service
@Validated
public class TrainingRecordServiceImpl implements TrainingRecordService {

    @Resource
    private TrainingRecordMapper trainingRecordMapper;

    @Override
    public Integer createTrainingRecord(TrainingRecordSaveReqVO createReqVO) {
        // 插入
        TrainingRecordDO trainingRecord = BeanUtils.toBean(createReqVO, TrainingRecordDO.class);
        trainingRecordMapper.insert(trainingRecord);

        // 返回
        return trainingRecord.getId();
    }

    @Override
    public void updateTrainingRecord(TrainingRecordSaveReqVO updateReqVO) {
        // 校验存在
        validateTrainingRecordExists(updateReqVO.getId());
        // 更新
        TrainingRecordDO updateObj = BeanUtils.toBean(updateReqVO, TrainingRecordDO.class);
        trainingRecordMapper.updateById(updateObj);
    }

    @Override
    public void deleteTrainingRecord(Integer id) {
        // 校验存在
        validateTrainingRecordExists(id);
        // 删除
        trainingRecordMapper.deleteById(id);
    }

    @Override
    public void deleteTrainingRecordListByIds(List<Integer> ids) {
        // 删除
        trainingRecordMapper.deleteByIds(ids);
        }

    private void validateTrainingRecordExists(Integer id) {
        if (trainingRecordMapper.selectById(id) == null) {
            throw exception(TRAINING_RECORD_NOT_EXISTS);
        }
    }

    @Override
    public TrainingRecordDO getTrainingRecord(Integer id) {
        return trainingRecordMapper.selectById(id);
    }

    @Override
    public PageResult<TrainingRecordDO> getTrainingRecordPage(TrainingRecordPageReqVO pageReqVO) {
        return trainingRecordMapper.selectPage(pageReqVO);
    }

}
