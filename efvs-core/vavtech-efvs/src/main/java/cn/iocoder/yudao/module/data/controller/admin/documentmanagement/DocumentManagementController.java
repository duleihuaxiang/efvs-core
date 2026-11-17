package cn.iocoder.yudao.module.data.controller.admin.documentmanagement;

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


import cn.iocoder.yudao.module.data.controller.admin.documentmanagement.vo.*;
import cn.iocoder.yudao.module.data.dal.dataobject.documentmanagement.DocumentManagementDO;
import cn.iocoder.yudao.module.data.service.documentmanagement.DocumentManagementService;

@RestController
@RequestMapping("/data/document-management")
@Validated
public class DocumentManagementController {

    @Resource
    private DocumentManagementService documentManagementService;

    @PostMapping("/create")
    @PreAuthorize("@ss.hasPermission('data:document-management:create')")
    public CommonResult<Integer> createDocumentManagement(@Valid @RequestBody DocumentManagementSaveReqVO createReqVO) {
        return success(documentManagementService.createDocumentManagement(createReqVO));
    }

    @PutMapping("/update")
    @PreAuthorize("@ss.hasPermission('data:document-management:update')")
    public CommonResult<Boolean> updateDocumentManagement(@Valid @RequestBody DocumentManagementSaveReqVO updateReqVO) {
        documentManagementService.updateDocumentManagement(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @PreAuthorize("@ss.hasPermission('data:document-management:delete')")
    public CommonResult<Boolean> deleteDocumentManagement(@RequestParam("id") Integer id) {
        documentManagementService.deleteDocumentManagement(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
                @PreAuthorize("@ss.hasPermission('data:document-management:delete')")
    public CommonResult<Boolean> deleteDocumentManagementList(@RequestParam("ids") List<Integer> ids) {
        documentManagementService.deleteDocumentManagementListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('data:document-management:query')")
    public CommonResult<DocumentManagementRespVO> getDocumentManagement(@RequestParam("id") Integer id) {
        DocumentManagementDO documentManagement = documentManagementService.getDocumentManagement(id);
        return success(BeanUtils.toBean(documentManagement, DocumentManagementRespVO.class));
    }

    @GetMapping("/page")
    @PreAuthorize("@ss.hasPermission('data:document-management:query')")
    public CommonResult<PageResult<DocumentManagementRespVO>> getDocumentManagementPage(@Valid DocumentManagementPageReqVO pageReqVO) {
        PageResult<DocumentManagementDO> pageResult = documentManagementService.getDocumentManagementPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, DocumentManagementRespVO.class));
    }

    @GetMapping("/export-excel")
    @PreAuthorize("@ss.hasPermission('data:document-management:export')")
    public void exportDocumentManagementExcel(@Valid DocumentManagementPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<DocumentManagementDO> list = documentManagementService.getDocumentManagementPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "文档管理.xls", "数据", DocumentManagementRespVO.class,
                        BeanUtils.toBean(list, DocumentManagementRespVO.class));
    }

}
