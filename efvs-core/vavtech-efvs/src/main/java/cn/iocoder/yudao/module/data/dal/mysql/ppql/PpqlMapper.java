package cn.iocoder.yudao.module.data.dal.mysql.ppql;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.data.dal.dataobject.ppql.PpqlDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.data.controller.admin.ppql.vo.*;

/**
 * 培训前准备问题 Mapper
 *
 * @author admin
 */
@Mapper
public interface PpqlMapper extends BaseMapperX<PpqlDO> {

    default PageResult<PpqlDO> selectPage(PpqlPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PpqlDO>()
                .likeIfPresent(PpqlDO::getProblemDescription, reqVO.getProblemDescription())
                .orderByDesc(PpqlDO::getId));
    }

}