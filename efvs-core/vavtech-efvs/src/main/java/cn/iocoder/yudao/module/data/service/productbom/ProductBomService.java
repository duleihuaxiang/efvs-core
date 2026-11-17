package cn.iocoder.yudao.module.data.service.productbom;

import java.util.*;
import javax.validation.*;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.data.controller.admin.productbom.vo.ProductBomImportExcelVO;
import cn.iocoder.yudao.module.data.controller.admin.productbom.vo.*;
import cn.iocoder.yudao.module.data.dal.dataobject.productbom.ProductBomDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import org.springframework.web.multipart.MultipartFile;

/**
 * 产品BOM Service 接口
 *
 * @author admin
 */
public interface ProductBomService {

    /**
     * 创建产品BOM
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Integer createProductBom(@Valid ProductBomSaveReqVO createReqVO);

    /**
     * 更新产品BOM
     *
     * @param updateReqVO 更新信息
     */
    void updateProductBom(@Valid ProductBomSaveReqVO updateReqVO);

    /**
     * 删除产品BOM
     *
     * @param id 编号
     */
    void deleteProductBom(Integer id);

    /**
    * 批量删除产品BOM
    *
    * @param ids 编号
    */
    void deleteProductBomListByIds(List<Integer> ids);

    /**
     * 获得产品BOM
     *
     * @param id 编号
     * @return 产品BOM
     */
    ProductBomDO getProductBom(Integer id);

    /**
     * 获得产品BOM分页
     *
     * @param pageReqVO 分页查询
     * @return 产品BOM分页
     */
    PageResult<ProductBomDO> getProductBomPage(ProductBomPageReqVO pageReqVO);

    List<ProductBomDO> findChildren(List<ProductBomDO> list);

    List<ProductBomRespVO> selectBOMList(String partNumber);

    /**
     * BOM 详情列表：传入父级件号 partNumber，返回其所有层级子节点
     *
     * @param name              名称模糊筛选
     * @param partNumber        父级件号（根节点件号）
     * @param productVersion    版本筛选
     * @param productType       类型筛选
     */
    List<ProductBomRespVO> selectBOMDetailList(String partNumber,
                                               String name,
                                               String productVersion,
                                               Integer productType);

    /**
     * BOM 详情导出 Excel 数据（类型、细类、大类、小类为中文，与导入模版一致）
     *
     * @param partNumber 产品代号
     */
    List<ProductBomDetailExcelVO> listBOMDetailForExcelExport(String partNumber);

    /**
     * 产品 BOM 列表导出 Excel：类型/细类/大类/小类与导入一致的中文；空值与字面量 "null" 不落单元格
     */
    List<ProductBomDetailExcelVO> listProductBomForExcelExport(ProductBomPageReqVO pageReqVO);

    /**
     * 导入产品 Excel
     *
     * @param file Excel 文件
     * @return 导入结果
     */
    CommonResult<List<ProductBomImportExcelVO>> importProductExcel(MultipartFile file);

}