package cn.iocoder.yudao.module.qar.dal.mysql.qar;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.data.dal.dataobject.product.ProductDO;
import cn.iocoder.yudao.module.qar.dal.dataobject.qar.QarDO;
import cn.iocoder.yudao.module.qar.controller.admin.qardata.vo.QarPageReqVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.util.CollectionUtils;

/**
 * qar数据管理 Mapper
 *
 * @author admin
 */
@Mapper
public interface QarMapper extends BaseMapperX<QarDO> {

    default PageResult<QarDO> selectPage(QarPageReqVO reqVO) {

        if (CollectionUtils.isEmpty(reqVO.getTimeList())){
            return selectPage(reqVO, new LambdaQueryWrapperX<QarDO>()
                    .likeIfPresent(QarDO::getAirline, reqVO.getAirline())
                    .likeIfPresent(QarDO::getModel, reqVO.getModel())
                    .likeIfPresent(QarDO::getEfvsConfiguration, reqVO.getEfvsConfiguration())
                    .likeIfPresent(QarDO::getName, reqVO.getFileName())
                    .eqIfPresent(QarDO::getFlag, reqVO.getFlag())
                    .inIfPresent(QarDO::getId, reqVO.getIds())
                    .orderByDesc(QarDO::getId));
        }else {
            return selectPage(reqVO, new LambdaQueryWrapperX<QarDO>()
                    .likeIfPresent(QarDO::getAirline, reqVO.getAirline())
                    .likeIfPresent(QarDO::getModel, reqVO.getModel())
                    .likeIfPresent(QarDO::getEfvsConfiguration, reqVO.getEfvsConfiguration())
                    .likeIfPresent(QarDO::getName, reqVO.getFileName())
                    .eqIfPresent(QarDO::getFlag, reqVO.getFlag())
                    .inIfPresent(QarDO::getId, reqVO.getIds())
                    .ge(QarDO::getCreateTime, reqVO.getTimeList().get(0))
                    .le(QarDO::getCreateTime, reqVO.getTimeList().get(1))
                    .orderByDesc(QarDO::getId));
        }
    }

    /**
     * 获取当前连接最后一次自增 id（MySQL）。
     * 在 default-executor-type: BATCH 时，insert 不会立即回填 id，需插入后调用本方法：
     * 会先 flush 批处理再执行 SELECT，从而拿到刚插入的自增 id。
     */
    @Select("SELECT LAST_INSERT_ID()")
    Long selectLastInsertId();
}