package cn.iocoder.yudao.module.qar.dal.mysql.qardata;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.qar.dal.dataobject.qardata.QarDataDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.qar.controller.admin.qardata.vo.*;
import org.apache.ibatis.annotations.Param;

/**
 * QAR数据 Mapper
 *
 * @author admin
 */
@Mapper
public interface QarDataMapper extends BaseMapperX<QarDataDO> {

    default PageResult<QarDataDO> selectPage(QarDataPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<QarDataDO>()
                .likeIfPresent(QarDataDO::getEmptySpaceSwitch, reqVO.getEmptySpaceSwitch())
                .orderByAsc(QarDataDO::getId));
    }

    void updateByQarId(@Param("qarId") Long qarId);

    void deleteByQarId(@Param("qarId")Long id);
}