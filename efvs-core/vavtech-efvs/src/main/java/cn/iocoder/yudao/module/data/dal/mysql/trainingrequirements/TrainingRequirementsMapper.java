package cn.iocoder.yudao.module.data.dal.mysql.trainingrequirements;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.data.dal.dataobject.trainingrequirements.TrainingRequirementsDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.data.controller.admin.trainingrequirements.vo.*;

/**
 * 培训需求 Mapper
 *
 * @author admin
 */
@Mapper
public interface TrainingRequirementsMapper extends BaseMapperX<TrainingRequirementsDO> {

    default PageResult<TrainingRequirementsDO> selectPage(TrainingRequirementsPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<TrainingRequirementsDO>()
                .likeIfPresent(TrainingRequirementsDO::getTrainingTheme, reqVO.getTrainingTheme())
                .eqIfPresent(TrainingRequirementsDO::getCustomerName, reqVO.getCustomerName())
                .likeIfPresent(TrainingRequirementsDO::getTrainingContent, reqVO.getTrainingContent())
                .eqIfPresent(TrainingRequirementsDO::getTrainingType, reqVO.getTrainingType())
                .eqIfPresent(TrainingRequirementsDO::getTrainingTarget, reqVO.getTrainingTarget())
                .orderByDesc(TrainingRequirementsDO::getId));
    }

}