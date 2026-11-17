package cn.iocoder.yudao.module.data.dal.mysql.trainingassessment;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.data.dal.dataobject.trainingassessment.TrainingAssessmentDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.data.controller.admin.trainingassessment.vo.*;

/**
 * 培训考核 Mapper
 *
 * @author admin
 */
@Mapper
public interface TrainingAssessmentMapper extends BaseMapperX<TrainingAssessmentDO> {

    default PageResult<TrainingAssessmentDO> selectPage(TrainingAssessmentPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<TrainingAssessmentDO>()
                .likeIfPresent(TrainingAssessmentDO::getName, reqVO.getName())
                .likeIfPresent(TrainingAssessmentDO::getCourseName, reqVO.getCourseName())
                .eqIfPresent(TrainingAssessmentDO::getCompletedForm, reqVO.getCompletedForm())
                .eqIfPresent(TrainingAssessmentDO::getScore, reqVO.getScore())
                .eqIfPresent(TrainingAssessmentDO::getInstructor, reqVO.getInstructor())
                .orderByAsc(TrainingAssessmentDO::getId));
    }

}