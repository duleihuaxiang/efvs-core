package cn.iocoder.yudao.module.training.dal.mysql.trainingexperience;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.training.dal.dataobject.trainingexperience.TrainingExperienceDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.training.controller.admin.trainingexperience.vo.*;
import org.springframework.data.repository.query.Param;

/**
 * 培训经历 Mapper
 *
 * @author admin
 */
@Mapper
public interface TrainingExperienceMapper extends BaseMapperX<TrainingExperienceDO> {
    default PageResult<TrainingExperienceDO> selectPage(TrainingExperiencePageReqVO reqVO, Integer userId) {
        // 计算偏移量
        int offset = (reqVO.getPageNo() - 1) * reqVO.getPageSize();
        int limit = reqVO.getPageSize();
        Long tenantId = TenantContextHolder.getTenantId();
        // 查询数据列表
        List<TrainingExperienceDO> list = selectPageWithFile(reqVO, offset, limit ,tenantId, userId);

        // 查询总数
        Long total = selectPageWithFileCount(reqVO, tenantId, userId);
        //new Long(list.size())
        return new PageResult<>(list,total);
    }


    List<TrainingExperienceDO> selectPageWithFile(@Param("reqVO") TrainingExperiencePageReqVO reqVO, @Param("offset") int offset, @Param("limit") int limit,@Param("tenantId") Long tenantId, @Param("userId") Integer userId);

    Long selectPageWithFileCount(@Param("reqVo")TrainingExperiencePageReqVO reqVO, @Param("tenantId") Long tenantId, @Param("userId") Integer userId);


    List<TrainingExperienceDO> selectByIdAndTenantId(@Param("id") Integer id,@Param("tenantId") Long tenantId);

    TrainingExperienceDO selectBytrainingId(@Param("id") Integer id);
}