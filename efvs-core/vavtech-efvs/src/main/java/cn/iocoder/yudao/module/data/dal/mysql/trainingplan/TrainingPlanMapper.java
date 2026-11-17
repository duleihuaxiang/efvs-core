package cn.iocoder.yudao.module.data.dal.mysql.trainingplan;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.data.dal.dataobject.trainingplan.TrainingPlanDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.data.controller.admin.trainingplan.vo.*;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 培训计划 Mapper
 *
 * @author admin
 */
@Mapper
public interface TrainingPlanMapper extends BaseMapperX<TrainingPlanDO> {

    default PageResult<TrainingPlanDO> selectPage(TrainingPlanPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<TrainingPlanDO>()
                .eqIfPresent(TrainingPlanDO::getAnnualPlanProjectNumberId, reqVO.getAnnualPlanProjectNumberId())
                .likeIfPresent(TrainingPlanDO::getTrainingProgramsCourses, reqVO.getTrainingProgramsCourses())
                .likeIfPresent(TrainingPlanDO::getTrainingLocation, reqVO.getTrainingLocation())
                .eqIfPresent(TrainingPlanDO::getProjectLeader, reqVO.getProjectLeader())
                .orderByDesc(TrainingPlanDO::getId));
    }
    List<TrainingPlanVO> gettrainingListByid(@Param("ids") List<Integer> ids, @Param("tenantId") Long tenantId);

    @Select("SELECT u.nickName as projectLeaderNickName FROM system_users u WHERE u.id = #{projectLeader}")
    String selectOneByProjectLeader(@Param("projectLeader") Integer projectLeader);
    @Select({
            "<script>",
            "SELECT ",
            "  u.id as id, ",
            "  u.nickName as nickName ",
            "FROM system_users u ",
            "WHERE 1=1 ",
            "<if test='leaderIds != null and leaderIds.size() > 0'>",
            "  AND u.id IN ",
            "  <foreach collection='leaderIds' item='id' open='(' separator=',' close=')'>",
            "    #{id}",
            "  </foreach>",
            "</if>",
            "</script>"
    })
    List<TrainingPlanVO> getUserNickNamesByIds(@Param("leaderIds") List<Integer> leaderIds);
}