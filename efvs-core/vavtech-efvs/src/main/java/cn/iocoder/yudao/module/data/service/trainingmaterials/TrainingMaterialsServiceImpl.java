package cn.iocoder.yudao.module.data.service.trainingmaterials;

import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.data.controller.admin.uploadzippreview.dto.FileUploadResult;
import cn.iocoder.yudao.module.data.controller.admin.uploadzippreview.service.impl.AttachmentService;
import cn.iocoder.yudao.module.training.dal.dataobject.trainingexperience.FileUploadDO;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;

import cn.iocoder.yudao.module.data.controller.admin.trainingmaterials.vo.*;
import cn.iocoder.yudao.module.data.dal.dataobject.trainingmaterials.TrainingMaterialsDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.data.dal.mysql.trainingmaterials.TrainingMaterialsMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.module.data.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.infra.enums.ErrorCodeConstants.FILE_NOT_EXISTS;

/**
 * 培训资料管理 Service 实现类
 *
 * @author admin
 */
@Service
@Validated
public class TrainingMaterialsServiceImpl implements TrainingMaterialsService {

    @Resource
    private TrainingMaterialsMapper trainingMaterialsMapper;
    @Resource
    private AttachmentService AttachmentService;

    @Override
    public Long createTrainingMaterials(TrainingMaterialsSaveReqVO createReqVO) {
        List<FileUploadResult> fileUploadResults = createReqVO.getFileUploadResults();
        // 插入
        TrainingMaterialsDO trainingMaterials = BeanUtils.toBean(createReqVO, TrainingMaterialsDO.class);
        if (fileUploadResults != null) {
            trainingMaterialsMapper.insert(trainingMaterials);
            Long trainingMaterialsId = trainingMaterials.getId();
            Long tenantId = TenantContextHolder.getTenantId();
            List<Integer> zipIds = new ArrayList<>();
            if (trainingMaterialsId != null) {
                for (FileUploadResult zipId : fileUploadResults) {
                    zipIds.add(zipId.getId());
                }
                AttachmentService.updateByidByTenantId(zipIds, trainingMaterialsId, tenantId);
            }
        }else {
            throw exception(FILE_NOT_EXISTS);
        }
        // 返回
        return trainingMaterials.getId();
    }

    @Override
    public void updateTrainingMaterials(TrainingMaterialsSaveReqVO updateReqVO) {
        List<FileUploadResult> fileUploadResults = updateReqVO.getFileUploadResults();
        // 校验存在
        validateTrainingMaterialsExists(updateReqVO.getId());
        // 更新
        TrainingMaterialsDO updateObj = BeanUtils.toBean(updateReqVO, TrainingMaterialsDO.class);
        trainingMaterialsMapper.updateById(updateObj);
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
    public void deleteTrainingMaterials(Integer id) {
        // 校验存在
        validateTrainingMaterialsExists(id);
        // 删除
        trainingMaterialsMapper.deleteById(id);
    }

    @Override
    public void deleteTrainingMaterialsListByIds(List<Integer> ids) {
        // 删除
        trainingMaterialsMapper.deleteByIds(ids);
        }

    private void validateTrainingMaterialsExists(Integer id) {
        if (trainingMaterialsMapper.selectById(id) == null) {
            throw exception(TRAINING_MATERIALS_NOT_EXISTS);
        }
    }

    @Override
    public TrainingMaterialsRespVO getTrainingMaterials(Integer id) {
        Long tenantId = TenantContextHolder.getTenantId();
        // 1. 查询培训基础信息
        TrainingMaterialsDO trainingMaterialsDO = trainingMaterialsMapper.selectBytrainingMaterialsId(id);
        if (trainingMaterialsDO == null) {
            return null;
        }

        // 2. 转换为 VO
        TrainingMaterialsRespVO trainingMaterialsVO = new TrainingMaterialsRespVO();
        BeanUtils.copyProperties(trainingMaterialsDO, trainingMaterialsVO);

        // 3. 根据 bizId（培训 ID）查询所有附件
        List<FileUploadDO> fileList = AttachmentService.selectListByBizId(trainingMaterialsDO.getId().intValue(), tenantId);

        // 4. 【核心修改】对附件列表进行去重：按 bizId 分组，保留 createTime 最新的记录
        List<FileUploadDO> latestFileList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(fileList)) {
            Map<Integer, FileUploadDO> latestFileMap = fileList.stream()
                    .collect(Collectors.toMap(
                            FileUploadDO::getBizId,           // Key: bizId
                            file -> file,                     // Value: 文件对象本身
                            (existing, replacement) -> {      // 冲突解决策略：保留时间较新的
                                if (existing.getCreateTime() == null) return replacement;
                                if (replacement.getCreateTime() == null) return existing;
                                // 假设 getCreateTime 返回的是 LocalDateTime 或 Date，此处比较时间大小
                                return replacement.getCreateTime().isAfter(existing.getCreateTime()) ? replacement : existing;
                            }
                    ));
            latestFileList = new ArrayList<>(latestFileMap.values());
        }

        // 5. 将去重后的附件列表赋值给 VO
        trainingMaterialsVO.setFileList(latestFileList);

        return trainingMaterialsVO;
    }

    @Override
    public PageResult<TrainingMaterialsDO> getTrainingMaterialsPage(TrainingMaterialsPageReqVO pageReqVO) {
        PageResult<TrainingMaterialsDO> trainingMaterialsDOPageResult = trainingMaterialsMapper.selectPage(pageReqVO);
        List<Long> trainingMaterialsIds = trainingMaterialsDOPageResult.getList().stream()
                .map(TrainingMaterialsDO::getId)
                .collect(Collectors.toList());

        Long tenantId = TenantContextHolder.getTenantId();
        List<FileUploadDO> fileList = new ArrayList<>();

        if (!CollectionUtils.isEmpty(trainingMaterialsIds)) {
            // 1. 批量查询所有关联的附件
            fileList = AttachmentService.selectListBytrainingMaterialsIds(trainingMaterialsIds, tenantId);

            // 2. 【核心修改】对附件列表进行去重：按 bizId 分组，保留 createTime 最新的记录
            // 假设 FileUploadDO 中有 getCreateTime() 方法返回 Date 或 LocalDateTime
            Map<Integer, FileUploadDO> latestFileMap = fileList.stream()
                    .collect(Collectors.toMap(
                            FileUploadDO::getBizId,           // Key: bizId
                            file -> file,                     // Value: 文件对象本身
                            (existing, replacement) -> {      // 冲突解决策略：保留时间较新的
                                if (existing.getCreateTime() == null) return replacement;
                                if (replacement.getCreateTime() == null) return existing;
                                return replacement.getCreateTime().isAfter(existing.getCreateTime()) ? replacement : existing;
                                // 如果是 LocalDateTime 类型，请使用: return replacement.getCreateTime().isAfter(existing.getCreateTime()) ? replacement : existing;
                            }
                    ));

            // 3. 遍历资料列表，从去重后的 Map 中获取对应的唯一附件
            for (TrainingMaterialsDO item : trainingMaterialsDOPageResult.getList()) {
                List<FileUploadDO> userFiles = new ArrayList<>();
                // 从 Map 中获取该 ID 对应的最新附件，如果没有则为 null
                FileUploadDO latestFile = latestFileMap.get(item.getId().intValue());
                if (latestFile != null) {
                    userFiles.add(latestFile);
                }
                item.setFileList(userFiles);
            }
        }

        return trainingMaterialsDOPageResult;
    }

}
