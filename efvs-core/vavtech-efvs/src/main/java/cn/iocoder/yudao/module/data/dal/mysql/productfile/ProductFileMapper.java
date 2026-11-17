package cn.iocoder.yudao.module.data.dal.mysql.productfile;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.data.dal.dataobject.productfile.ProductFileDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.data.controller.admin.productfile.vo.*;
import org.apache.ibatis.annotations.Param;

/**
 * 档案 Mapper
 *
 * @author admin
 */
@Mapper
public interface ProductFileMapper extends BaseMapperX<ProductFileDO> {

    default PageResult<ProductFileDO> selectPage(ProductFilePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ProductFileDO>()
                .orderByDesc(ProductFileDO::getId));
    }

    void updateByPartNameAndSequenceNumber(@Param("partNumber") String partNumber,@Param("sequenceNumber") String sequenceNumber,@Param("tenantId") Long tenantId);

    List<ProductFileDO> findSRU(@Param("tenantId") Long tenantId, @Param("partNumber") String partNumber, @Param("version") String version, @Param("serialNumber") String serialNumber);


}