package cn.iocoder.yudao.module.data.dal.mysql.trainingattendance;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.data.dal.dataobject.trainingattendance.TrainingAttendanceDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.data.controller.admin.trainingattendance.vo.*;

/**
 * 培训签到 Mapper
 *
 * @author admin
 */
@Mapper
public interface TrainingAttendanceMapper extends BaseMapperX<TrainingAttendanceDO> {

    default PageResult<TrainingAttendanceDO> selectPage(TrainingAttendancePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<TrainingAttendanceDO>()
                .eqIfPresent(TrainingAttendanceDO::getSerialNumber, reqVO.getSerialNumber())
                .likeIfPresent(TrainingAttendanceDO::getName, reqVO.getName())
                .likeIfPresent(TrainingAttendanceDO::getUnitName, reqVO.getUnitName())
                .likeIfPresent(TrainingAttendanceDO::getTrainingTheme, reqVO.getTrainingTheme())
                .eqIfPresent(TrainingAttendanceDO::getTrainingCategory, reqVO.getTrainingCategory())
                .orderByDesc(TrainingAttendanceDO::getId));
    }

}