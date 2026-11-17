package cn.iocoder.yudao.module.form.service.externalform;

import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import cn.iocoder.yudao.module.form.controller.admin.externalform.vo.*;
import cn.iocoder.yudao.module.form.dal.dataobject.externalform.ExternalFormDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.form.dal.mysql.externalform.ExternalFormMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.data.enums.ErrorCodeConstants.EXTERNAL_FORM_NOT_EXISTS;

/**
 * 外置表单配置 Service 实现类
 *
 * @author admin
 */
@Service
@Validated
public class ExternalFormServiceImpl implements ExternalFormService {

    @Resource
    private ExternalFormMapper externalFormMapper;

    @Override
    public Long creatProcessActFormConfig(ExternalFormSaveReqVO createReqVO) {
        // 同一租户内，一个流程定义的一个活动节点只允许配置一条外置表单。
        // 已存在时覆盖配置，避免审批节点读取到多条互相冲突的表单定义。
        Long tenantId = TenantContextHolder.getTenantId();
        ExternalFormDO externalForm = externalFormMapper.selectFormByProcessIdAndActivityId(createReqVO.getProcessId(), createReqVO.getActivityId(), tenantId);
        if(externalForm == null){
            // 插入
            externalForm = BeanUtils.toBean(createReqVO, ExternalFormDO.class);
            externalFormMapper.insert(externalForm);
        }else{
            // 更新
            externalForm = externalFormMapper.selectFormByProcessIdAndActivityId(createReqVO.getProcessId(), createReqVO.getActivityId(), tenantId);
            externalForm.setFormType(createReqVO.getFormType());
            externalForm.setFormUrl(createReqVO.getFormUrl());
            externalForm.setRemark(createReqVO.getRemark());
            externalForm.setFormKey(createReqVO.getFormKey());
            externalFormMapper.updateById(externalForm);
        }
        // 返回
        return externalForm.getId();
    }

    @Override
    public void updateExternalForm(ExternalFormSaveReqVO updateReqVO) {
        // 校验存在
        validateExternalFormExists(updateReqVO);
        // 更新
        ExternalFormDO updateObj = BeanUtils.toBean(updateReqVO, ExternalFormDO.class);
        externalFormMapper.updateById(updateObj);
    }

    @Override
    public void deleteExternalForm(Long id) {
        // 校验存在
        validateExternalFormExists(id);
        // 删除
        externalFormMapper.deleteById(id);
    }

    private void validateExternalFormExists(Long id) {
        if (externalFormMapper.selectById(id) == null) {
            throw exception(EXTERNAL_FORM_NOT_EXISTS);
        }
    }

    @Override
    public void deleteExternalFormListByIds(List<Long> ids) {
        // 删除
        externalFormMapper.deleteByIds(ids);
    }



    private void validateExternalFormExists(ExternalFormSaveReqVO updateReqVO) {
        // 外置表单的唯一定位条件是“流程定义 + 活动节点 + 租户”，而非请求中可能缺失的配置主键。
        Long tenantId = TenantContextHolder.getTenantId();
        if (externalFormMapper.selectFormByProcessIdAndActivityId(updateReqVO.getProcessId(), updateReqVO.getActivityId(), tenantId) == null) {
            throw exception(EXTERNAL_FORM_NOT_EXISTS);
        }
    }

    @Override
    public ExternalFormDO getExternalForm(String processId, String activityId) {
        Long tenantId = TenantContextHolder.getTenantId();
        return externalFormMapper.selectFormByProcessIdAndActivityId(processId, activityId, tenantId);
    }

    @Override
    public PageResult<ExternalFormDO> getExternalFormPage(ExternalFormPageReqVO pageReqVO) {
        return externalFormMapper.selectPage(pageReqVO);
    }

}
