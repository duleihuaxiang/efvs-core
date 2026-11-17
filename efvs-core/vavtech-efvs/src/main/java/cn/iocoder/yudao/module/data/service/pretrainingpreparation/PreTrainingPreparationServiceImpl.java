package cn.iocoder.yudao.module.data.service.pretrainingpreparation;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.data.controller.admin.pretrainingpreparation.vo.*;
import cn.iocoder.yudao.module.data.dal.dataobject.pretrainingpreparation.PreTrainingPreparationDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.data.dal.mysql.pretrainingpreparation.PreTrainingPreparationMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.diffList;
import static cn.iocoder.yudao.module.data.enums.ErrorCodeConstants.*;

/**
 * 培训前准备 Service 实现类
 *
 * @author admin
 */
@Service
@Validated
public class PreTrainingPreparationServiceImpl extends ServiceImpl<PreTrainingPreparationMapper, PreTrainingPreparationDO> implements PreTrainingPreparationService {

    @Resource
    private PreTrainingPreparationMapper preTrainingPreparationMapper;

    @Override
    public Integer createPreTrainingPreparation(PreTrainingPreparationSaveReqVO createReqVO) {
        // 插入
        PreTrainingPreparationDO preTrainingPreparation = BeanUtils.toBean(createReqVO, PreTrainingPreparationDO.class);
        preTrainingPreparationMapper.insert(preTrainingPreparation);

        // 返回
        return preTrainingPreparation.getId();
    }

    @Override
    public void updatePreTrainingPreparation(PreTrainingPreparationSaveReqVO updateReqVO) {
        // 校验存在
        validatePreTrainingPreparationExists(updateReqVO.getId());
        // 更新
        PreTrainingPreparationDO updateObj = BeanUtils.toBean(updateReqVO, PreTrainingPreparationDO.class);
        preTrainingPreparationMapper.updateById(updateObj);
    }

    @Override
    public void deletePreTrainingPreparation(Integer id) {
        // 校验存在
        validatePreTrainingPreparationExists(id);
        // 删除
        preTrainingPreparationMapper.deleteById(id);
    }

    @Override
    public void deletePreTrainingPreparationListByIds(List<Integer> ids) {
        // 删除
        preTrainingPreparationMapper.deleteByIds(ids);
        }

    private void validatePreTrainingPreparationExists(Integer id) {
        if (preTrainingPreparationMapper.selectById(id) == null) {
            throw exception(PRE_TRAINING_PREPARATION_NOT_EXISTS);
        }
    }

    @Override
    public PreTrainingPreparationDO getPreTrainingPreparation(Integer id) {
        return preTrainingPreparationMapper.selectById(id);
    }

    @Override
    public PageResult<PreTrainingPreparationDO> getPreTrainingPreparationPage(PreTrainingPreparationPageReqVO pageReqVO) {
        return preTrainingPreparationMapper.selectPage(pageReqVO);
    }

    @Override
    @Transactional
    public void createPreTrainingPreparationBatch(List<PreTrainingPreparationSaveReqVO> createReqVOList) {
        if (CollUtil.isEmpty(createReqVOList)) {
            return;
        }

        List<PreTrainingPreparationDO> preTrainingPreparationList = convertList(createReqVOList,
                item -> BeanUtils.toBean(item, PreTrainingPreparationDO.class));

        // 批量插入
        saveBatch(preTrainingPreparationList);
    }

    @Override
    public List<PreTrainingPreparationDO> getPreTrainingPreparationByProcessInstanceId(String processInstanceId) {
        return preTrainingPreparationMapper.selectList(new QueryWrapper<PreTrainingPreparationDO>().eq("process_instance_id", processInstanceId));
    }

}
