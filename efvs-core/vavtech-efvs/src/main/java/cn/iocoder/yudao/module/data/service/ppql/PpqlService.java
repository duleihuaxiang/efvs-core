package cn.iocoder.yudao.module.data.service.ppql;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.data.controller.admin.ppql.vo.*;
import cn.iocoder.yudao.module.data.dal.dataobject.ppql.PpqlDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 培训前准备问题 Service 接口
 *
 * @author admin
 */
public interface PpqlService {

    /**
     * 创建培训前准备问题
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Integer createPpql(@Valid PpqlSaveReqVO createReqVO);

    /**
     * 更新培训前准备问题
     *
     * @param updateReqVO 更新信息
     */
    void updatePpql(@Valid PpqlSaveReqVO updateReqVO);

    /**
     * 删除培训前准备问题
     *
     * @param id 编号
     */
    void deletePpql(Integer id);

    /**
    * 批量删除培训前准备问题
    *
    * @param ids 编号
    */
    void deletePpqlListByIds(List<Integer> ids);

    /**
     * 获得培训前准备问题
     *
     * @param id 编号
     * @return 培训前准备问题
     */
    PpqlDO getPpql(Integer id);

    /**
     * 获得培训前准备问题分页
     *
     * @param pageReqVO 分页查询
     * @return 培训前准备问题分页
     */
    PageResult<PpqlDO> getPpqlPage(PpqlPageReqVO pageReqVO);

    List<PpqlDO> getPpqlList();
}