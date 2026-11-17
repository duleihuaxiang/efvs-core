package cn.iocoder.yudao.module.data.dal.mysql.pretrainingpreparation;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.data.dal.dataobject.pretrainingpreparation.PreTrainingPreparationDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.data.controller.admin.pretrainingpreparation.vo.*;

/**
 * 培训前准备 Mapper
 *
 * @author admin
 */
@Mapper
public interface PreTrainingPreparationMapper extends BaseMapperX<PreTrainingPreparationDO> {

    default PageResult<PreTrainingPreparationDO> selectPage(PreTrainingPreparationPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PreTrainingPreparationDO>()
                .eqIfPresent(PreTrainingPreparationDO::getSerialNumber, reqVO.getSerialNumber())
                .likeIfPresent(PreTrainingPreparationDO::getPreTrainingMatters, reqVO.getPreTrainingMatters())
                .eqIfPresent(PreTrainingPreparationDO::getIsCompleted, reqVO.getIsCompleted())
                .orderByDesc(PreTrainingPreparationDO::getId));
    }

}