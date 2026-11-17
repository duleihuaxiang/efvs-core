package cn.iocoder.yudao.module.data.controller.admin.ppql;

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


import cn.iocoder.yudao.module.data.controller.admin.ppql.vo.*;
import cn.iocoder.yudao.module.data.dal.dataobject.ppql.PpqlDO;
import cn.iocoder.yudao.module.data.service.ppql.PpqlService;

@RestController
@RequestMapping("/data/ppql")
@Validated
public class PpqlController {

    @Resource
    private PpqlService ppqlService;

    @PostMapping("/create")
    @PreAuthorize("@ss.hasPermission('data:ppql:create')")
    public CommonResult<Integer> createPpql(@Valid @RequestBody PpqlSaveReqVO createReqVO) {
        return success(ppqlService.createPpql(createReqVO));
    }

    @PutMapping("/update")
    @PreAuthorize("@ss.hasPermission('data:ppql:update')")
    public CommonResult<Boolean> updatePpql(@Valid @RequestBody PpqlSaveReqVO updateReqVO) {
        ppqlService.updatePpql(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @PreAuthorize("@ss.hasPermission('data:ppql:delete')")
    public CommonResult<Boolean> deletePpql(@RequestParam("id") Integer id) {
        ppqlService.deletePpql(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
                @PreAuthorize("@ss.hasPermission('data:ppql:delete')")
    public CommonResult<Boolean> deletePpqlList(@RequestParam("ids") List<Integer> ids) {
        ppqlService.deletePpqlListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('data:ppql:query')")
    public CommonResult<PpqlRespVO> getPpql(@RequestParam("id") Integer id) {
        PpqlDO ppql = ppqlService.getPpql(id);
        return success(BeanUtils.toBean(ppql, PpqlRespVO.class));
    }

    @GetMapping("/page")
    @PreAuthorize("@ss.hasPermission('data:ppql:query')")
    public CommonResult<PageResult<PpqlRespVO>> getPpqlPage(@Valid PpqlPageReqVO pageReqVO) {
        PageResult<PpqlDO> pageResult = ppqlService.getPpqlPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, PpqlRespVO.class));
    }

    @GetMapping("/export-excel")
    @PreAuthorize("@ss.hasPermission('data:ppql:export')")
    public void exportPpqlExcel(@Valid PpqlPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<PpqlDO> list = ppqlService.getPpqlPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "培训前准备问题.xls", "数据", PpqlRespVO.class,
                        BeanUtils.toBean(list, PpqlRespVO.class));
    }
    @GetMapping("/list")
    public CommonResult<List<PpqlRespVO>> getPpqlList() {
        List<PpqlDO> list = ppqlService.getPpqlList();
        return success(BeanUtils.toBean(list, PpqlRespVO.class));
    }

}
