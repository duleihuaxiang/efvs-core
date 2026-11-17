package cn.iocoder.yudao.module.training.dal.mysql.trainingexperience;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.FileConfigDO;
import cn.iocoder.yudao.module.training.controller.admin.trainingexperience.vo.FileUploadPageReqVO;
import cn.iocoder.yudao.module.training.dal.dataobject.trainingexperience.FileUploadDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;


/**
 * 文件下载 Mapper
 *
 * @author admin
 */
@Mapper
public interface FileUploadMapper extends BaseMapperX<FileUploadDO> {

    default PageResult<FileUploadDO> selectPage(FileUploadPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<FileUploadDO>()
                .eqIfPresent(FileUploadDO::getBizId, reqVO.getBizId())
                .likeIfPresent(FileUploadDO::getFileName, reqVO.getFileName())
                .eqIfPresent(FileUploadDO::getFileSize, reqVO.getFileSize())
                .eqIfPresent(FileUploadDO::getFileUrl, reqVO.getFileUrl())
                .eqIfPresent(FileUploadDO::getFileAbsoluteUrl, reqVO.getFileAbsoluteUrl())
                .orderByDesc(FileUploadDO::getId));
    }

    void insertBatchSomeColumn(List<FileUploadDO> attachments);

    void updateByidByTenantId(@Param("zipId") List<Integer> zipId, @Param("trainingId")Long trainingId, @Param("tenantId")Long tenantId);


//    @Select("select file_name from efvs_file_upload where id = #{id}")
    String selectNameById(@Param("id")Integer fileId);

    void updateByexperienceIdByTenantId(Long experienceId, Long userId, Long tenantId);

    boolean selectByfileId(@Param("fileId") Integer fileId);

    void updateByfileId(@Param("fileDO")FileUploadDO fileDO,@Param("fileId") Integer fileId);

    List<FileUploadDO> selectListByBizId(@Param ("trainingId")int trainingId,@Param("tenantId") Long tenantId);


    List<FileUploadDO> selectListBytrainingMaterialsIds(@Param("trainingMaterialsIds") List<Long> trainingMaterialsIds,@Param("tenantId") Long tenantId);

    void deleteByBizIdAndTenantId(@Param("bizId") Integer bizId, @Param("tenantId") Long tenantId);

    void deleteByBizIdsAndTenantId(@Param("bizIds") List<Integer> bizIds, @Param("tenantId") Long tenantId);

    List<FileUploadDO> selectListByFileUploadIds(@Param("fileUploadIds")Set<Integer> distinctFileUploadIds);

    List<FileUploadDO> selectListByIds(@Param("allFileUploadIds") List<Integer> allFileUploadIds);
    //查询文件配置
    FileConfigDO selectFileConfig();
}