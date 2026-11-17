package cn.iocoder.yudao.module.data.dal.mysql.trainingattendanceassessment;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.data.dal.dataobject.trainingattendanceassessment.TrainingAttendanceAssessmentDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.data.controller.admin.trainingattendanceassessment.vo.*;
import org.apache.ibatis.annotations.Param;

/**
 * 培训签到考核证书 Mapper
 *
 * @author admin
 */
@Mapper
public interface TrainingAttendanceAssessmentMapper extends BaseMapperX<TrainingAttendanceAssessmentDO> {

    default PageResult<TrainingAttendanceAssessmentDO> selectPage(TrainingAttendanceAssessmentPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<TrainingAttendanceAssessmentDO>()
                .likeIfPresent(TrainingAttendanceAssessmentDO::getName, reqVO.getName())
                .likeIfPresent(TrainingAttendanceAssessmentDO::getCourseName, reqVO.getCourseName())
                .likeIfPresent(TrainingAttendanceAssessmentDO::getInstructor, reqVO.getInstructor())
                .betweenIfPresent(TrainingAttendanceAssessmentDO::getCheckInTime, reqVO.getCheckInTime())
                .eqIfPresent(TrainingAttendanceAssessmentDO::getCompletedForm, reqVO.getCompletedForm())
                .eqIfPresent(TrainingAttendanceAssessmentDO::getScore, reqVO.getScore())
                .eqIfPresent(TrainingAttendanceAssessmentDO::getReceiveCertificate, reqVO.getReceiveCertificate())
                .orderByDesc(TrainingAttendanceAssessmentDO::getId));
    }

    List<TrainingAttendanceAssessmentDO> getTrainingAttendanceAssessmentByIds(@Param("processInstanceId") String processInstanceId);
}