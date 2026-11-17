package cn.iocoder.yudao.module.data.dal.mysql.faultinformation;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.data.controller.admin.vo.FaultInformationPageReqVO;
import cn.iocoder.yudao.module.data.dal.dataobject.faultinformation.FaultInformationDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 故障信息 Mapper
 *
 * @author admin
 */
@Mapper
public interface FaultInformationMapper extends BaseMapperX<FaultInformationDO> {

    default PageResult<FaultInformationDO> selectPage(FaultInformationPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<FaultInformationDO>()
                .orderByDesc(FaultInformationDO::getId));
    }

    List<FaultInformationDO> selectAllFaultInformation(Long tenantId);
}