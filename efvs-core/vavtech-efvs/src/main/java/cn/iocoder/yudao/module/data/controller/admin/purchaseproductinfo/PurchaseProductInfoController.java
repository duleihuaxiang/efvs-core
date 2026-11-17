package cn.iocoder.yudao.module.data.controller.admin.purchaseproductinfo;

import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;

import javax.validation.constraints.*;
import javax.validation.*;
import javax.servlet.http.*;
import java.util.*;
import java.io.IOException;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;


import cn.iocoder.yudao.module.data.controller.admin.purchaseproductinfo.vo.*;
import cn.iocoder.yudao.module.data.dal.dataobject.purchaseproductinfo.PurchaseProductInfoDO;
import cn.iocoder.yudao.module.data.service.purchaseproductinfo.PurchaseProductInfoService;

@RestController
@RequestMapping("/data/purchase-product-info")
@Validated
public class PurchaseProductInfoController {

    @Resource
    private PurchaseProductInfoService purchaseProductInfoService;

    @PostMapping("/create")
    @PreAuthorize("@ss.hasPermission('data:purchase-product-info:create')")
    public CommonResult<Integer> createPurchaseProductInfo(@Valid @RequestBody PurchaseProductInfoSaveReqVO createReqVO) {
        return success(purchaseProductInfoService.createPurchaseProductInfo(createReqVO));
    }

    @PutMapping("/update")
    @PreAuthorize("@ss.hasPermission('data:purchase-product-info:update')")
    public CommonResult<Boolean> updatePurchaseProductInfo(@Valid @RequestBody PurchaseProductInfoSaveReqVO updateReqVO) {
        purchaseProductInfoService.updatePurchaseProductInfo(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @PreAuthorize("@ss.hasPermission('data:purchase-product-info:delete')")
    public CommonResult<Boolean> deletePurchaseProductInfo(@RequestParam("id") Integer id) {
        purchaseProductInfoService.deletePurchaseProductInfo(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
                @PreAuthorize("@ss.hasPermission('data:purchase-product-info:delete')")
    public CommonResult<Boolean> deletePurchaseProductInfoList(@RequestParam("ids") List<Integer> ids) {
        purchaseProductInfoService.deletePurchaseProductInfoListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('data:purchase-product-info:query')")
    public CommonResult<PurchaseProductInfoRespVO> getPurchaseProductInfo(@RequestParam("id") Integer id) {
        PurchaseProductInfoDO purchaseProductInfo = purchaseProductInfoService.getPurchaseProductInfo(id);
        return success(BeanUtils.toBean(purchaseProductInfo, PurchaseProductInfoRespVO.class));
    }

    @GetMapping("/page")
    @PreAuthorize("@ss.hasPermission('data:purchase-product-info:query')")
    public CommonResult<PageResult<PurchaseProductInfoRespVO>> getPurchaseProductInfoPage(@Valid PurchaseProductInfoPageReqVO pageReqVO) {
        PageResult<PurchaseProductInfoDO> pageResult = purchaseProductInfoService.getPurchaseProductInfoPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, PurchaseProductInfoRespVO.class));
    }

    @GetMapping("/export-excel")
    @PreAuthorize("@ss.hasPermission('data:purchase-product-info:export')")
    public void exportPurchaseProductInfoExcel(@Valid PurchaseProductInfoPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<PurchaseProductInfoDO> list = purchaseProductInfoService.getPurchaseProductInfoPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "购买产品信息.xls", "数据", PurchaseProductInfoRespVO.class,
                        BeanUtils.toBean(list, PurchaseProductInfoRespVO.class));
    }

}
