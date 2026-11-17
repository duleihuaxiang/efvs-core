package cn.iocoder.yudao.module.form.service.externalform;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.form.controller.admin.externalform.vo.*;
import cn.iocoder.yudao.module.form.dal.dataobject.externalform.ExternalFormDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 外置表单配置 Service 接口
 *
 * @author admin
 */
public interface ExternalFormService {

    /**
     * 创建外置表单配置
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long creatProcessActFormConfig(@Valid ExternalFormSaveReqVO createReqVO);

    /**
     * 更新外置表单配置
     *
     * @param updateReqVO 更新信息
     */
    void updateExternalForm(@Valid ExternalFormSaveReqVO updateReqVO);

    /**
     * 删除外置表单配置
     *
     * @param id 编号
     */
    void deleteExternalForm(Long id);

    /**
     * 批量删除外置表单配置
     *
     * @param ids 编号
     */
    void deleteExternalFormListByIds(List<Long> ids);

    /**
     * 获得外置表单配置
     *
     * @param processId 流程标识
     * @param activityId 活动标识
     * @return 外置表单配置
     */
    ExternalFormDO getExternalForm(String processId, String activityId);

    /**
     * 获得外置表单配置分页
     *
     * @param pageReqVO 分页查询
     * @return 外置表单配置分页
     */
    PageResult<ExternalFormDO> getExternalFormPage(ExternalFormPageReqVO pageReqVO);


}
