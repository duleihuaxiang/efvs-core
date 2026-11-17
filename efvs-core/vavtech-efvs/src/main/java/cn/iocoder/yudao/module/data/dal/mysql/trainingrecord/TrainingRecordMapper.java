package cn.iocoder.yudao.module.data.dal.mysql.trainingrecord;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.data.dal.dataobject.trainingrecord.TrainingRecordDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.data.controller.admin.trainingrecord.vo.*;

/**
 * 培训记录 Mapper
 *
 * @author admin
 */
@Mapper
public interface TrainingRecordMapper extends BaseMapperX<TrainingRecordDO> {

    default PageResult<TrainingRecordDO> selectPage(TrainingRecordPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<TrainingRecordDO>()
                .eqIfPresent(TrainingRecordDO::getSerialNumber, reqVO.getSerialNumber())
                .likeIfPresent(TrainingRecordDO::getName, reqVO.getName())
                .likeIfPresent(TrainingRecordDO::getCourseName, reqVO.getCourseName())
                .eqIfPresent(TrainingRecordDO::getInstructor, reqVO.getInstructor())
                .orderByDesc(TrainingRecordDO::getId));
    }

}