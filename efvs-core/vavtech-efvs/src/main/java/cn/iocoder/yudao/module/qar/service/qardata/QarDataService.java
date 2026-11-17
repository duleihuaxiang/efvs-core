package cn.iocoder.yudao.module.qar.service.qardata;

import java.io.IOException;
import java.util.*;
import javax.servlet.http.HttpServletResponse;
import javax.validation.*;
import cn.iocoder.yudao.module.qar.controller.admin.qardata.vo.*;
import cn.iocoder.yudao.module.qar.dal.dataobject.qardata.QarDataDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import org.springframework.web.multipart.MultipartFile;

/**
 * QAR数据 Service 接口
 *
 * @author admin
 */
public interface QarDataService {

    /**
     * 创建QAR数据
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Integer createData(@Valid QarDataSaveReqVO createReqVO);

    /**
     * 更新QAR数据
     *
     * @param updateReqVO 更新信息
     */
    void updateData(@Valid QarDataSaveReqVO updateReqVO);

    /**
     * 删除QAR数据
     *
     * @param id 编号
     */
    void deleteData(Integer id);

    /**
    * 批量删除QAR数据
    *
    * @param ids 编号
    */
    void deleteDataListByIds(List<Integer> ids);

    /**
     * 获得QAR数据
     *
     * @param id 编号
     * @return QAR数据
     */
    QarDataDO getData(Integer id);

    /**
     * 获得QAR数据分页
     *
     * @param pageReqVO 分页查询
     * @return QAR数据分页
     */
    PageResult<QarDataDO> getDataPage(QarDataPageReqVO pageReqVO);

    /**
     * 下载QAR数据导入模板
     *
     * @param response 响应对象
     * @throws IOException IO异常
     */
    void downloadQRAMappingTemplate(HttpServletResponse response) throws IOException;

    /**
     * 上传 CSV 文件 -> 使用 LOAD DATA 导入
     *
     * @param file MultipartFile (.csv)
     * @return 导入的行数
     * @throws Exception
     */
//    int importCsvViaLoadData(MultipartFile file) throws Exception;
    //int importCsvViaLoadData(MultipartFile file,String airlineQar,String attachmentName) throws Exception;

    /**
     * 批量上传 CSV 文件 -> 使用 LOAD DATA 导入
     *
     * 使用 resources/template/QAR数据_模版文件.xlsx 作为固定映射模板，
     * 不再从接口上传 mappingExcel。
     *
     * @param csvFiles CSV文件列表（最多20个）
     * @return 导入结果，包含成功导入的文件数和总行数
     * @throws Exception
     */
    Map<String, Object> batchImportCsvViaLoadData(List<MultipartFile> csvFiles) throws Exception;

    /**
     * 批量上传 CSV 文件 -> 使用 LOAD DATA 导入（支持按顺序传入 qarIds，入库时写入 qar_id）
     *
     * 使用 resources/template/QAR数据_模版文件.xlsx 作为固定映射模板，
     * 不再从接口上传 mappingExcel。
     *
     * @param csvFiles CSV文件列表（最多20个）
     * @param qarIds 与 csvFiles 顺序对应的 QAR 主键 id 列表，可为 null；传入时每条导入数据会写入 qar_id
     * @return 导入结果，包含成功导入的文件数和总行数
     * @throws Exception
     */
    Map<String, Object> batchImportCsvViaLoadData(List<MultipartFile> csvFiles, List<Long> qarIds) throws Exception;

    /**
     * 根据文件路径导入单个 CSV（大文件入库），并写入 qar_id。
     * 映射关系固定从 resources/template/QAR数据_模版文件.xlsx 读取，只入库模板中定义的列，其余列忽略。
     *
     * @param csvFilePath CSV 文件所在路径（upload 字段值）
     * @param qarId QAR 主表主键 id，会写入 efvs_qar_data.qar_id
     * @return 导入行数
     * @throws Exception
     */
    int importCsvViaLoadDataFromPath(String csvFilePath, Long qarId) throws Exception;

    /**
     * 根据文件路径导入单个 CSV，可选在导入前于同一连接上按 qarId 逻辑删除原数据（用于 update 时避免新数据被误标删）。
     * 映射关系固定从 resources/template/QAR数据_模版文件.xlsx 读取。
     *
     * @param csvFilePath CSV 文件路径
     * @param qarId QAR 主表 id
     * @param logicalDeleteByQarIdFirst true 时在导入前于同一连接执行逻辑删除（仅更新 deleted=0 的行）
     * @return 导入行数
     */
    int importCsvViaLoadDataFromPath(String csvFilePath, Long qarId, boolean logicalDeleteByQarIdFirst) throws Exception;

    /**
     * 获取所有滑跑周期列表
     * 通过识别时间间隔较大的断点来分离不同的滑跑周期
     *
     * @return 滑跑周期列表
     */
    List<TaxiPhaseCycleVO> getTaxiPhaseCycles();

    void deleteByQarId(Long id);

}