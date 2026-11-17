package cn.iocoder.yudao.module.data.service.product;

import java.util.*;
import javax.validation.*;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.data.controller.admin.product.vo.*;
import cn.iocoder.yudao.module.data.dal.dataobject.product.ProductDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;

/**
 * 产品 Service 接口
 *
 * @author admin
 */
public interface ProductService {

    /**
     * 创建产品
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Integer createProduct(@Valid ProductSaveReqVO createReqVO);

    /**
     * 更新产品
     *
     * @param updateReqVO 更新信息
     */
    void updateProduct(@Valid List<ProductSaveReqVO> updateReqVO);

    /**
     * 删除产品
     *
     * @param id 编号
     */
    void deleteProduct(Integer id);

    /**
    * 批量删除产品
    *
    * @param ids 编号
    */
    void deleteProductListByIds(List<Integer> ids);

    /**
     * 获得产品
     *
     * @param id 编号
     * @return 产品
     */
    ProductDO getProduct(Integer id);

    /**
     * 获得产品分页
     *
     * @param pageReqVO 分页查询
     * @return 产品分页
     */
    PageResult<ProductDO> getProductPage(ProductPageReqVO pageReqVO);


    List<ProductRespVO> getProductSerialNumber(String partNumber, String SerialNumber);

    List<ProductRespVO> getProductFile(String partNumber, String sequenceNumber);

    /**
     * 获得所有产品
     *
     * @return 所有产品列表
     */
    List<ProductDO> getProductAll();



    /**
     * 导入产品档案 Excel（解析并入库）
     *
     * @param file       Excel 文件
     * @return 解析结果
     */
    CommonResult<List<ProductFileImportExcelVO>> importProductFileExcel(MultipartFile file);

    List<HashMap<Object, Object>> selectUsers(List<Integer> userIds);
}