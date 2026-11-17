package cn.iocoder.yudao.module.data.dal.mysql.trainingmaterials;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.data.dal.dataobject.trainingmaterials.TrainingMaterialsDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.data.controller.admin.trainingmaterials.vo.*;
import org.apache.ibatis.annotations.Param;

/**
 * 培训资料管理 Mapper
 *
 * @author admin
 */
@Mapper
public interface TrainingMaterialsMapper extends BaseMapperX<TrainingMaterialsDO> {

    default PageResult<TrainingMaterialsDO> selectPage(TrainingMaterialsPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<TrainingMaterialsDO>()
                .eqIfPresent(TrainingMaterialsDO::getTrainingId, reqVO.getTrainingId())
                .likeIfPresent(TrainingMaterialsDO::getDocumentName, reqVO.getDocumentName())
                .eqIfPresent(TrainingMaterialsDO::getContainingDirectory, reqVO.getContainingDirectory())
                .eqIfPresent(TrainingMaterialsDO::getCategory, reqVO.getCategory())
                .inSql(TrainingMaterialsDO::getId, "SELECT DISTINCT biz_id FROM efvs_file_upload WHERE biz_id IS NOT NULL AND file_url IS NOT NULL AND file_url != '' AND deleted = 0 ")
                .eq(TrainingMaterialsDO::getDeleted, 0)
                .eq(TrainingMaterialsDO::getTenantId, TenantContextHolder.getTenantId())
                .orderByDesc(TrainingMaterialsDO::getId));
    }

    TrainingMaterialsDO selectBytrainingMaterialsId(@Param("id") Integer id);

  }