package cn.iocoder.yudao.module.training.service.trainingexperience;

import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.data.controller.admin.uploadzippreview.dto.FileUploadResult;
import cn.iocoder.yudao.module.data.controller.admin.uploadzippreview.service.impl.AttachmentService;
import cn.iocoder.yudao.module.training.dal.dataobject.trainingexperience.FileUploadDO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import cn.iocoder.yudao.module.training.controller.admin.trainingexperience.vo.*;
import cn.iocoder.yudao.module.training.dal.dataobject.trainingexperience.TrainingExperienceDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.training.dal.mysql.trainingexperience.TrainingExperienceMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.data.enums.ErrorCodeConstants.EXPERIENCE_NOT_EXISTS;
import static cn.iocoder.yudao.module.infra.enums.ErrorCodeConstants.FILE_NOT_EXISTS;

/**
 * 培训经历 Service 实现类
 *
 * @author admin
 */
@Service
@Validated
public class TrainingExperienceServiceImpl extends ServiceImpl<TrainingExperienceMapper,TrainingExperienceDO> implements TrainingExperienceService {

    @Resource
    private TrainingExperienceMapper experienceMapper;

    @Resource
    private AttachmentService AttachmentService;

    @Override
    public List<Long> createExperience(TrainingExperienceSaveReqVO createReqVO, Integer userId) {
        List<FileUploadResult> fileUploadResults = createReqVO.getFileUploadResults();
        // 先创建培训经历主记录，取得 trainingId 后再将已上传附件绑定到该业务记录。
        // 附件上传与培训经历保存分两步完成，避免上传接口依赖尚未生成的业务主键。
        TrainingExperienceDO experience = BeanUtils.toBean(createReqVO, TrainingExperienceDO.class);
        if (!fileUploadResults.isEmpty()) {
            experienceMapper.insert(experience);
            Long trainingId = experience.getId();
            if (trainingId != null) {
                Long tenantId = TenantContextHolder.getTenantId();
                List<Integer> zipIds = new ArrayList<>();

                for (FileUploadResult zipId : fileUploadResults) {
                    zipIds.add(zipId.getId());
                }
                // 绑定附件到培训经历；userId 非空时再补齐附件所属用户，用于个人培训经历查询。
                AttachmentService.updateByidByTenantId(zipIds, trainingId, tenantId);
                if(userId != null){
                    AttachmentService.updateByexperienceIdByTenantId(trainingId, userId.longValue(), tenantId);
                }

            }
        }else{
            throw exception(FILE_NOT_EXISTS);
        }
        // 返回
        return Collections.singletonList(experience.getId());
    }

    @Override
    public void updateExperience(TrainingExperienceSaveReqVO updateReqVO) {
        List<FileUploadResult> fileUploadResults = updateReqVO.getFileUploadResults();
        // 校验存在
        validateExperienceExists(updateReqVO.getId());
        // 更新
        TrainingExperienceDO updateObj = BeanUtils.toBean(updateReqVO, TrainingExperienceDO.class);
        experienceMapper.updateById(updateObj);
        // 更新主记录后，将本次提交的附件重新绑定到同一个培训经历，不删除历史已关联附件。
        Long tenantId = TenantContextHolder.getTenantId();
        List<Integer> zipIds = new ArrayList<>();
        if (updateReqVO.getId() != null && fileUploadResults != null) {
            for (FileUploadResult zipId : fileUploadResults) {
                zipIds.add(zipId.getId());
            }
            AttachmentService.updateByidByTenantId(zipIds, updateReqVO.getId().longValue(), tenantId);
        }
    }

    @Override
    public void deleteExperience(Integer id) {
        // 校验存在
        validateExperienceExists(id);
        // 删除
        experienceMapper.deleteById(id);
    }

    @Override
    public void deleteExperienceListByIds(List<Integer> ids) {
        // 删除
        experienceMapper.deleteByIds(ids);
        }

    private void validateExperienceExists(Integer id) {
        Long tenantId = TenantContextHolder.getTenantId();
        if (experienceMapper.selectByIdAndTenantId(id, tenantId) == null) {
            throw exception(EXPERIENCE_NOT_EXISTS);
        }
    }

    @Override
    public TrainingExperienceRespVO getExperience(Integer id) {
        Long tenantId = TenantContextHolder.getTenantId();
        // 1. 查询培训基础信息
        TrainingExperienceDO trainingDO = experienceMapper.selectBytrainingId(id);
        if (trainingDO == null) {
            return null;
        }

        // 2. 转换为VO
        TrainingExperienceRespVO trainingVO = new TrainingExperienceRespVO();
        BeanUtils.copyProperties(trainingDO, trainingVO);

        // 3. 根据bizId（培训ID）查询所有附件（注意类型转换：trainingDO.getId()是Long，需转Integer）
        List<FileUploadDO> fileList = AttachmentService.selectListByBizId(trainingDO.getId().intValue(),tenantId);

        // 4. 将附件数组赋值给VO
        trainingVO.setFileList(fileList);

        return trainingVO;
    }

    @Override
    public PageResult<TrainingExperienceDO> getExperiencePage(TrainingExperiencePageReqVO pageReqVO, Integer userId) {
        return experienceMapper.selectPage(pageReqVO, userId);
    }

}
