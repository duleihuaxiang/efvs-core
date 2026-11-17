package cn.iocoder.yudao.module.data.service.faultinformation;

import java.util.*;
import javax.validation.*;

import cn.iocoder.yudao.module.data.controller.admin.vo.FaultInformationPageReqVO;
import cn.iocoder.yudao.module.data.controller.admin.vo.FaultInformationSaveReqVO;
import cn.iocoder.yudao.module.data.dal.dataobject.faultinformation.FaultInformationDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 故障信息 Service 接口
 *
 * @author admin
 */
public interface FaultInformationService {

    /**
     * 创建故障信息
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Integer createFaultInformation(@Valid FaultInformationSaveReqVO createReqVO);

    /**
     * 更新故障信息
     *
     * @param updateReqVO 更新信息
     */
    void updateFaultInformation(@Valid FaultInformationSaveReqVO updateReqVO);

    /**
     * 删除故障信息
     *
     * @param id 编号
     */
    void deleteFaultInformation(Integer id);

    /**
    * 批量删除故障信息
    *
    * @param ids 编号
    */
    void deleteFaultInformationListByIds(List<Integer> ids);

    /**
     * 获得故障信息
     *
     * @param id 编号
     * @return 故障信息
     */
    FaultInformationDO getFaultInformation(Integer id);

    /**
     * 获得故障信息分页
     *
     * @param pageReqVO 分页查询
     * @return 故障信息分页
     */
    PageResult<FaultInformationDO> getFaultInformationPage(FaultInformationPageReqVO pageReqVO);


    List<FaultInformationDO> selectAllFaultInformation();

    List<FaultInformationDO> getFaultInformationList(List<Integer> ids);
}