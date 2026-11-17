package cn.iocoder.yudao.module.data.service.ppql;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.data.controller.admin.ppql.vo.*;
import cn.iocoder.yudao.module.data.dal.dataobject.ppql.PpqlDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.data.dal.mysql.ppql.PpqlMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.diffList;
import static cn.iocoder.yudao.module.data.enums.ErrorCodeConstants.*;

/**
 * 培训前准备问题 Service 实现类
 *
 * @author admin
 */
@Service
@Validated
public class PpqlServiceImpl implements PpqlService {

    @Resource
    private PpqlMapper ppqlMapper;

    @Override
    public Integer createPpql(PpqlSaveReqVO createReqVO) {
        // 插入
        PpqlDO ppql = BeanUtils.toBean(createReqVO, PpqlDO.class);
        ppqlMapper.insert(ppql);

        // 返回
        return ppql.getId();
    }

    @Override
    public void updatePpql(PpqlSaveReqVO updateReqVO) {
        // 校验存在
        validatePpqlExists(updateReqVO.getId());
        // 更新
        PpqlDO updateObj = BeanUtils.toBean(updateReqVO, PpqlDO.class);
        ppqlMapper.updateById(updateObj);
    }

    @Override
    public void deletePpql(Integer id) {
        // 校验存在
        validatePpqlExists(id);
        // 删除
        ppqlMapper.deleteById(id);
    }

    @Override
    public void deletePpqlListByIds(List<Integer> ids) {
        // 删除
        ppqlMapper.deleteByIds(ids);
        }

    private void validatePpqlExists(Integer id) {
        if (ppqlMapper.selectById(id) == null) {
            throw exception(PPQL_NOT_EXISTS);
        }
    }

    @Override
    public PpqlDO getPpql(Integer id) {
        return ppqlMapper.selectById(id);
    }

    @Override
    public PageResult<PpqlDO> getPpqlPage(PpqlPageReqVO pageReqVO) {
        return ppqlMapper.selectPage(pageReqVO);
    }

    @Override
    public List<PpqlDO> getPpqlList() {
        return ppqlMapper.selectList();
    }

}
