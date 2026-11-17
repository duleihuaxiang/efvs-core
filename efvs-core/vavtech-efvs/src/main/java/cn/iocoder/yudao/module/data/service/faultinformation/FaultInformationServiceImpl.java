package cn.iocoder.yudao.module.data.service.faultinformation;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.data.controller.admin.vo.FaultInformationPageReqVO;
import cn.iocoder.yudao.module.data.controller.admin.vo.FaultInformationSaveReqVO;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.data.dal.dataobject.faultinformation.FaultInformationDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.data.dal.mysql.faultinformation.FaultInformationMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.diffList;
import static cn.iocoder.yudao.module.data.enums.ErrorCodeConstants.*;

/**
 * 故障信息 Service 实现类
 *
 * @author admin
 */
@Service
@Validated
public class FaultInformationServiceImpl implements FaultInformationService {

    @Resource
    private FaultInformationMapper faultInformationMapper;

    @Override
    public Integer createFaultInformation(FaultInformationSaveReqVO createReqVO) {
        // 插入
        FaultInformationDO faultInformation = BeanUtils.toBean(createReqVO, FaultInformationDO.class);
        faultInformationMapper.insert(faultInformation);

        // 返回
        return faultInformation.getId();
    }

    @Override
    public void updateFaultInformation(FaultInformationSaveReqVO updateReqVO) {
        // 校验存在
        validateFaultInformationExists(updateReqVO.getId());
        // 更新
        FaultInformationDO updateObj = BeanUtils.toBean(updateReqVO, FaultInformationDO.class);
        faultInformationMapper.updateById(updateObj);
    }

    @Override
    public void deleteFaultInformation(Integer id) {
        // 校验存在
        validateFaultInformationExists(id);
        // 删除
        faultInformationMapper.deleteById(id);
    }

    @Override
        public void deleteFaultInformationListByIds(List<Integer> ids) {
        // 删除
        faultInformationMapper.deleteByIds(ids);
        }


    private void validateFaultInformationExists(Integer id) {
        if (faultInformationMapper.selectById(id) == null) {
            throw exception(FAULT_INFORMATION_NOT_EXISTS);
        }
    }

    @Override
    public FaultInformationDO getFaultInformation(Integer id) {
        return faultInformationMapper.selectById(id);
    }

    @Override
    public PageResult<FaultInformationDO> getFaultInformationPage(FaultInformationPageReqVO pageReqVO) {
        return faultInformationMapper.selectPage(pageReqVO);
    }

    @Override
    public List<FaultInformationDO> selectAllFaultInformation() {
        Long tenantId = TenantContextHolder.getTenantId();
        return faultInformationMapper.selectAllFaultInformation(tenantId);
    }

    @Override
    public List<FaultInformationDO> getFaultInformationList(List<Integer> ids) {
        // 过滤空值并检查集合大小
        if (ids != null && !ids.isEmpty()) {
            return faultInformationMapper.selectByIds(ids);
        } else {
            // 处理空集合的情况，可能需要返回空结果或跳过此条件
            return new ArrayList<>(); // 或其他处理逻辑
        }
    }

}