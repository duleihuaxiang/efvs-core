package cn.iocoder.yudao.module.form.dal.mysql.externalform;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.form.dal.dataobject.externalform.ExternalFormDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.form.controller.admin.externalform.vo.*;
import org.apache.ibatis.annotations.Param;
import org.springframework.security.core.parameters.P;

/**
 * 外置表单配置 Mapper
 *
 * @author admin
 */
@Mapper
public interface ExternalFormMapper extends BaseMapperX<ExternalFormDO> {

    default PageResult<ExternalFormDO> selectPage(ExternalFormPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ExternalFormDO>()
                .orderByDesc(ExternalFormDO::getId));
    }

    ExternalFormDO selectFormByProcessIdAndActivityId(@Param("processId") String processId, @Param("activityId") String activityId, @Param("tenantId") Long tenantId);

}