package cn.iocoder.yudao.module.qar.service.qardata;

import java.util.*;
import javax.validation.*;

import cn.iocoder.yudao.module.qar.dal.dataobject.qar.QarDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.qar.controller.admin.qardata.vo.QarPageReqVO;
import cn.iocoder.yudao.module.qar.controller.admin.qardata.vo.QarSaveReqVO;

/**
 * qar数据管理 Service 接口
 *
 * @author admin
 */
public interface QarService {

    /**
     * 创建qar数据管理
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    List<Long> createQar(@Valid QarSaveReqVO createReqVO);

    /**
     * 更新qar数据管理
     *
     * @param updateReqVO 更新信息
     */
    void updateQar(@Valid QarSaveReqVO updateReqVO);

    /**
     * 删除qar数据管理
     *
     * @param id 编号
     */
    void deleteQar(Long id);

    /**
    * 批量删除qar数据管理
    *
    * @param ids 编号
    */
    void deleteQarListByIds(List<Long> ids);

    /**
     * 获得qar数据管理
     *
     * @param id 编号
     * @return qar数据管理
     */
    QarDO getQar(Long id);

    /**
     * 获得qar数据管理分页
     *
     * @param pageReqVO 分页查询
     * @return qar数据管理分页
     */
    PageResult<QarDO> getQarPage(QarPageReqVO pageReqVO);

    /**
     * 根据主键 id 列表批量获取 QAR 记录
     *
     * @param ids QAR 主表 id 列表
     * @return 对应的 QAR 记录列表
     */
    List<QarDO> getQarListByIds(List<Long> ids);

}