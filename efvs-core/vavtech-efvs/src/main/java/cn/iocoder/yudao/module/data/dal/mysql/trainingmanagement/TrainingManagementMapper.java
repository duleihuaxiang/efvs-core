package cn.iocoder.yudao.module.data.dal.mysql.trainingmanagement;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.data.dal.dataobject.trainingmanagement.TrainingManagementDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.data.controller.admin.trainingmanagement.vo.*;

/**
 * 培训管理 Mapper
 *
 * @author admin
 */
@Mapper
public interface TrainingManagementMapper extends BaseMapperX<TrainingManagementDO> {

    default PageResult<TrainingManagementDO> selectPage(TrainingManagementPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<TrainingManagementDO>()
                .eqIfPresent(TrainingManagementDO::getSerialNumber, reqVO.getSerialNumber())
                .likeIfPresent(TrainingManagementDO::getProcessName, reqVO.getProcessName())
                .betweenIfPresent(TrainingManagementDO::getInitiationTime, reqVO.getInitiationTime())
                .orderByDesc(TrainingManagementDO::getId));
    }

}