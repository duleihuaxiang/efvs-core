package cn.iocoder.yudao.module.qar.service.qardata;

import cn.iocoder.yudao.module.qar.controller.admin.qardata.vo.QarPageReqVO;
import cn.iocoder.yudao.module.qar.controller.admin.qardata.vo.QarSaveReqVO;
import cn.iocoder.yudao.module.qar.controller.admin.qardata.vo.fileVO;
import cn.iocoder.yudao.module.qar.dal.mysql.qardata.QarDataMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

import cn.iocoder.yudao.module.qar.dal.dataobject.qar.QarDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.qar.dal.mysql.qar.QarMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.module.data.enums.ErrorCodeConstants.QAR_FILE_IMPORT_FAILED;
import static cn.iocoder.yudao.module.data.enums.ErrorCodeConstants.QAR_NOT_EXISTS;

/**
 * QAR 主记录及其 CSV 明细导入的编排 Service。
 * <p>
 * QAR 主表与 {@code efvs_qar_data} 明细必须保持一致，因此创建、更新均采用事务：
 * CSV 导入失败时，主记录、旧明细的逻辑删除及本次已导入的明细都会一并回滚。
 *
 * @author admin
 */
@Slf4j
@Service
@Validated
public class QarServiceImpl implements QarService {

    @Resource
    private QarMapper qarMapper;

    @Resource
    private QarDataMapper qarDataMapper;

    @Resource
    private QarDataService qarDataService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> createQar(QarSaveReqVO createReqVO) {
        List<fileVO> files = createReqVO.getFiles();
        log.info("[createQar] 开始创建QAR，文件数={}, airline={}, model={}", files.size(), createReqVO.getAirline(), createReqVO.getModel());

        List<Long> ids = new ArrayList<>();
        for (fileVO file : files) {
            QarDO qar = new QarDO();
            qar.setAirline(createReqVO.getAirline());
            qar.setModel(createReqVO.getModel());
            qar.setEfvsConfiguration(createReqVO.getEfvsConfiguration());
            qar.setName(file.getName());
            qar.setSize(file.getSize());
            qar.setFileSource(createReqVO.getFileSource());
            qar.setFileUrl(file.getFileUrl());
            qar.setFlag(createReqVO.getFlag());
            qarMapper.insert(qar);
            // 使用 BATCH 执行器时 insert 不会回填 id，通过 LAST_INSERT_ID() 获取并回填（会先 flush 批处理）
            Long qarId = qar.getId();
            if (qarId == null) {
                qarId = qarMapper.selectLastInsertId();
                if (qarId != null && qarId > 0) {
                    qar.setId(qarId);
                }
            }
            if (qarId == null || qarId <= 0) {
                throw new IllegalStateException(
                    "插入 QAR 记录后未获得主键 id。请检查：1) MySQL 下表 efvs_qar 的 id 列是否为 AUTO_INCREMENT；2) 若使用 Oracle/PostgreSQL，是否配置了序列 efvs_qar_seq 且 mybatis-plus 的 id-type 为 INPUT。");
            }
            ids.add(qarId);
            log.debug("[createQar] 已插入qar记录 id={}, name={}", qarId, file.getName());

            // 主记录取得主键后才可导入明细，导入过程会将 qarId 回写到每一行明细数据。
            // 映射固定来自 resources/template/QAR数据_模版文件.xlsx，未定义的 CSV 列不入库。
            if (file.getFileUrl() != null && !file.getFileUrl().trim().isEmpty()) {
                try {
                    qarDataService.importCsvViaLoadDataFromPath(file.getFileUrl().trim(), qarId);
                } catch (Exception e) {
                    String errorMessage = e.getMessage();
                    if (errorMessage == null || errorMessage.trim().isEmpty()) {
                        errorMessage = e.getClass().getSimpleName();
                        if (e.getCause() != null && e.getCause().getMessage() != null) {
                            errorMessage += ": " + e.getCause().getMessage();
                        } else if (e.toString() != null && !e.toString().equals(e.getClass().getName())) {
                            errorMessage = e.toString();
                        }
                    }
                    // 文件入库失败：事务将回滚，本批次已插入的 qar 记录均会回滚，不返回任何已创建的 id
                    log.error("[createQar] 文件入库失败，事务将回滚。当前失败 qarId={}, 本批次已插入的 id 将全部回滚，不返回已创建的 id。失败文件 name={}, 原因: {}",
                            qarId, ids, file.getName(), errorMessage, e);
                    throw exception(QAR_FILE_IMPORT_FAILED, qarId, errorMessage);
                }
            }
        }

        log.info("[createQar] 创建成功，共 {} 条，返回 id 列表: {}", ids.size(), ids);
        return ids;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateQar(QarSaveReqVO updateReqVO) {
        Long qarId = updateReqVO.getId();
        validateQarExists(qarId);
        QarDO qarDO = qarMapper.selectById(qarId);
        log.info("[updateQar] 开始更新QAR id={}", qarId);

        // 文件变更时在同一事务中先逻辑删除旧明细，再按固定模板重新导入。
        // 这样若新文件校验或导入失败，旧明细不会被永久删除。
        String newFileUrl = updateReqVO.getFiles().get(0).getFileUrl();
        boolean fileUrlChanged = newFileUrl == null ? (qarDO.getFileUrl() != null) : !newFileUrl.equals(qarDO.getFileUrl());
        if (fileUrlChanged && newFileUrl != null && !newFileUrl.trim().isEmpty()) {
            log.info("[updateQar] 文件路径有变化，准备逻辑删除原 qar_data 并重新导入 qarId={}", qarId);
            try {
                qarDataService.importCsvViaLoadDataFromPath(updateReqVO.getFiles().get(0).getFileUrl().trim(), qarId, true);
            } catch (Exception e) {
                String errorMessage = e.getMessage();
                if (errorMessage == null || errorMessage.trim().isEmpty()) {
                    errorMessage = e.getClass().getSimpleName();
                    if (e.getCause() != null && e.getCause().getMessage() != null) {
                        errorMessage += ": " + e.getCause().getMessage();
                    } else if (e.toString() != null && !e.toString().equals(e.getClass().getName())) {
                        errorMessage = e.toString();
                    }
                }
                log.error("[updateQar] 重新导入 qar_data 失败，事务将回滚。qarId={}，已执行的删除/更新将全部回滚。原因: {}", qarId, errorMessage, e);
                throw exception(QAR_FILE_IMPORT_FAILED, qarId, errorMessage);
            }
        }

        // 更新 qar 主表（无论是否重新导入都要更新）
        qarDO.setAirline(updateReqVO.getAirline());
        qarDO.setModel(updateReqVO.getModel());
        qarDO.setFileSource(updateReqVO.getFileSource());
        qarDO.setEfvsConfiguration(updateReqVO.getEfvsConfiguration());
        fileVO fileVO = updateReqVO.getFiles().get(0);
        qarDO.setSize(fileVO.getSize());
        qarDO.setFileUrl(fileVO.getFileUrl());
        qarDO.setName(fileVO.getName());
        qarMapper.updateById(qarDO);

        log.info("[updateQar] 更新成功 qarId={}", qarId);
    }

    @Override
    public void deleteQar(Long id) {
        // 校验存在
        validateQarExists(id);
        // 删除
        qarMapper.deleteById(id);
        qarDataService.deleteByQarId(id);
    }

    @Override
        public void deleteQarListByIds(List<Long> ids) {
        // 删除
        qarMapper.deleteByIds(ids);
        }


    private void validateQarExists(Long id) {
        if (qarMapper.selectById(id) == null) {
            throw exception(QAR_NOT_EXISTS);
        }
    }

    @Override
    public QarDO getQar(Long id) {
        return qarMapper.selectById(id);
    }

    @Override
    public PageResult<QarDO> getQarPage(QarPageReqVO pageReqVO) {
        return qarMapper.selectPage(pageReqVO);
    }

    @Override
    public List<QarDO> getQarListByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return qarMapper.selectBatchIds(ids);
    }

}
